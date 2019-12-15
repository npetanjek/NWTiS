/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.foi.nwtis.npetanjek.ejb.sb;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.net.Socket;
import java.net.URLEncoder;
import java.nio.charset.Charset;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.annotation.PreDestroy;
import javax.ejb.EJB;
import javax.ejb.LocalBean;
import javax.ejb.Stateful;
import javax.ws.rs.ClientErrorException;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.WebTarget;
import org.foi.nwtis.npetanjek.ejb.podaci.Korisnik;
import org.foi.nwtis.npetanjek.konfiguracije.Konfiguracija;
import org.foi.nwtis.npetanjek.mqtt.slusaci.SlusacMqtt;

/**
 *
 * @author Nikola
 */
@Stateful
@LocalBean
public class AutentikacijaSB {

    @EJB
    private MqttPorukeFacade mqttPorukeFacade;
    
    Socket socket;
    private SlusacMqtt slusacMqtt;
    Korisnik k;
    private Konfiguracija konfiguracija;

    public boolean autenticiraj(String korisnik, String lozinka) throws UnsupportedEncodingException {
        KorisniciREST_JerseyClient client = new KorisniciREST_JerseyClient();
        Gson gson = new Gson();
        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("lozinka", lozinka);
        String odgovor;
        try {
            odgovor = client.autentikacijaJson(gson.toJson(jsonObject), korisnik);
            return provjeriStatus(gson.fromJson(odgovor, JsonObject.class));
        } catch (ClientErrorException ex) {
            Logger.getLogger(AutentikacijaSB.class.getName()).log(Level.SEVERE, null, ex);
        }
        return false;
    }
    
    public Korisnik dajKorisnika(String korisnik) throws UnsupportedEncodingException {
        KorisniciREST_JerseyClient client = new KorisniciREST_JerseyClient();
        String odgovor;
        try {
            Gson gson = new Gson();
            odgovor = client.autentikacijaJson(null, korisnik);
            JsonObject jsonObject = gson.fromJson(odgovor, JsonObject.class);
            JsonElement jsonElement = jsonObject.get("odgovor");
            Korisnik k = gson.fromJson(jsonElement, Korisnik.class);
            System.out.println("KORISNIK: " + k.getKorIme());
            return k;
        } catch (ClientErrorException ex) {
            Logger.getLogger(AutentikacijaSB.class.getName()).log(Level.SEVERE, null, ex);
        }
        return null;
    }
    
    private boolean provjeriStatus (JsonObject jsonObject) {
        JsonElement jsonElement = jsonObject.get("status");
        if (jsonElement.getAsString().equals("ERR")) {
            return false;
        }
        return true;
    }
    
    public String registrirajGrupu(String korisnik, String lozinka) {
        String komanda = "KORISNIK " + korisnik + "; LOZINKA " + lozinka + "; GRUPA DODAJ;";
        saljiKomandu(komanda);
        return citaj();
    }
    
    public String aktivirajGrupu(String korisnik, String lozinka) {
        String komanda = "KORISNIK " + korisnik + "; LOZINKA " + lozinka + "; GRUPA KRENI;";
        saljiKomandu(komanda);
        return citaj();
    }
    
    public void slusajMqtt(Konfiguracija konfiguracija) {
        slusacMqtt = new SlusacMqtt(konfiguracija, mqttPorukeFacade);
        slusacMqtt.start();
        System.out.println("Slusam na MQTT...");
    }
    
    private void saljiKomandu(String komanda) {
        BufferedWriter bw;
        String host = konfiguracija.dajPostavku("server.host");
        int port = Integer.parseInt(konfiguracija.dajPostavku("server.port"));
        try {
            socket = new Socket(host, port);
            bw = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream(), Charset.forName("UTF-8")));
            bw.write(komanda);
            bw.flush();
            socket.getOutputStream().flush();
            socket.shutdownOutput();
        } catch (IOException ex) {
            Logger.getLogger(AutentikacijaSB.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
     private String citaj() {
        StringBuilder stringBuilder = new StringBuilder();
        try {
            BufferedReader br = new BufferedReader(new InputStreamReader(socket.getInputStream(), Charset.forName("UTF-8")));
            int znak;
            while ((znak = br.read()) != -1) {
                stringBuilder.append((char) znak);
            }
            socket.shutdownInput();
            socket.close();
        } catch (IOException ex) {
            Logger.getLogger(AutentikacijaSB.class.getName()).log(Level.SEVERE, null, ex);
        }
        return stringBuilder.toString();
    }
     
     public void odjava() {
         System.out.println("AutentikacijaSB:odjava");
         if (slusacMqtt != null) {
             System.out.println("AutentikacijaSB:gasim MQTT slusaca");
             slusacMqtt.interrupt();
         } else {
             System.out.println("AutentikacijaSB:MQTT slusac je null");
         }
     }
     
     @PreDestroy
     void destroy() {
         if (slusacMqtt != null && slusacMqtt.isAlive()) {
             slusacMqtt.interrupt();
         }
     }

    public SlusacMqtt getSlusacMqtt() {
        return slusacMqtt;
    }

    public void setKonfiguracija(Konfiguracija konfiguracija) {
        this.konfiguracija = konfiguracija;
    }

    static class KorisniciREST_JerseyClient {

        private WebTarget webTarget;
        private Client client;
        private static final String BASE_URI = "http://localhost:8084/npetanjek_aplikacija_3_2/webresources";

        public KorisniciREST_JerseyClient() {
            client = javax.ws.rs.client.ClientBuilder.newClient();
            webTarget = client.target(BASE_URI).path("korisnici");
        }

        public String putJson(Object requestEntity, String id) throws ClientErrorException {
            return webTarget.path(java.text.MessageFormat.format("{0}", new Object[]{id})).request(javax.ws.rs.core.MediaType.APPLICATION_JSON).put(javax.ws.rs.client.Entity.entity(requestEntity, javax.ws.rs.core.MediaType.APPLICATION_JSON), String.class);
        }

        public String autentikacijaJson(String auth, String id) throws ClientErrorException, UnsupportedEncodingException {
            WebTarget resource = webTarget;
            if (auth != null) {
                resource = resource.queryParam("auth", URLEncoder.encode(auth, "UTF-8"));
            }
            resource = resource.path(java.text.MessageFormat.format("{0}", new Object[]{id}));
            return resource.request(javax.ws.rs.core.MediaType.APPLICATION_JSON).get(String.class);
        }

        public String dodajKorisnikaJson(Object requestEntity) throws ClientErrorException {
            return webTarget.request(javax.ws.rs.core.MediaType.APPLICATION_JSON).post(javax.ws.rs.client.Entity.entity(requestEntity, javax.ws.rs.core.MediaType.APPLICATION_JSON), String.class);
        }

        public String dajSveKorisnike() throws ClientErrorException {
            WebTarget resource = webTarget;
            return resource.request(javax.ws.rs.core.MediaType.APPLICATION_JSON).get(String.class);
        }

        public void close() {
            client.close();
        }
    }

    
}
