/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.foi.nwtis.npetanjek.rest.serveri;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.Socket;
import java.nio.charset.Charset;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.UriInfo;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.Produces;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PUT;
import javax.ws.rs.PathParam;
import javax.ws.rs.QueryParam;
import org.foi.nwtis.npetanjek.konfiguracije.Konfiguracija;
import org.foi.nwtis.npetanjek.web.slusaci.Slusac;
import org.foi.nwtis.npetanjek.ws.klijenti.Korisnik;

/**
 * REST Web Service
 *
 * @author Nikola
 */
@Path("korisnici")
public class KorisniciREST {

    @Context
    private UriInfo context;

    Gson gson;
    JsonObject jsonObject;
    String korime;
    String pw;
    String korisnik;
    String lozinka;
    String komanda;
    String host;
    int port;
    Socket socket;
    Konfiguracija konfiguracija;

    /**
     * Creates a new instance of KorisniciREST
     */
    public KorisniciREST() {
        konfiguracija = Slusac.getKonfiguracija();
        korime = "npetanjek";
        pw = "37870123";
        gson = new Gson();
        host = konfiguracija.dajPostavku("server.host");
        port = Integer.parseInt(konfiguracija.dajPostavku("server.port"));
    }

    /**
     * Retrieves representation of an instance of
     * org.foi.nwtis.npetanjek.rest.serveri.KorisniciREST
     *
     * @return an instance of java.lang.String
     */
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public String dajSveKorisnike() {
        jsonObject = new JsonObject();
        List<Korisnik> korisnici = dajPodatkeoKorisnicima(korime, pw);
        if (!korisnici.isEmpty()) {
            jsonObject.add("odgovor", gson.toJsonTree(korisnici));
            jsonObject.addProperty("status", "OK");
        } else {
            jsonObject.addProperty("status", "ERR");
            jsonObject.addProperty("poruka", "Pogreska prilikom dohvacanja korisnika");
        }
        return gson.toJson(jsonObject);
    }

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public String dodajKorisnikaJson(String podaci) {
        jsonObject = gson.fromJson(podaci, JsonObject.class);
        korisnik = jsonObject.get("korisnik").getAsString();
        lozinka = jsonObject.get("lozinka").getAsString();
        String prezime = jsonObject.get("prezime").getAsString();
        String ime = jsonObject.get("ime").getAsString();
        String email = jsonObject.get("email").getAsString();
        Korisnik k = new Korisnik();
        k.setKorIme(korisnik);
        k.setLozinka(lozinka);
        k.setPrezime(prezime);
        k.setIme(ime);
        k.setEmail(email);
        jsonObject = new JsonObject();
        if (dodajKorisnika(korime, pw, k)) {
            jsonObject.add("odgovor", new JsonArray());
            jsonObject.addProperty("status", "OK");
        } else {
            jsonObject.addProperty("status", "ERR");
            jsonObject.addProperty("poruka", "Pogreska prilikom dodavanja korisnika");
        }
        return gson.toJson(jsonObject);
    }
    
    private JsonObject pronadiKorisnika(String kor) {
        boolean pronaden = false;
        jsonObject = gson.fromJson(dajSveKorisnike(), JsonObject.class);
        JsonElement jsonElement = jsonObject.get("odgovor");
        jsonObject = new JsonObject();
        for (JsonElement e : jsonElement.getAsJsonArray()) {
            Korisnik k = gson.fromJson(e, Korisnik.class);
            if (k.getKorIme().equals(kor)) {
                jsonObject.add("odgovor", gson.toJsonTree(k));
                jsonObject.addProperty("status", "OK");
                pronaden = true;
            }
        }
        if (!pronaden) {
            jsonObject.addProperty("status", "ERR");
            jsonObject.addProperty("poruka", "Korisnik nije pronaden");
        }
        return jsonObject;
    }
    
    @Path("{id}")
    @GET
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public String autentikacijaJson(@PathParam("id") String korisnik, @QueryParam("auth") String auth) {
        jsonObject = new JsonObject();
        if (auth != null) {
            lozinka = gson.fromJson(auth, JsonObject.class).get("lozinka").getAsString();
            komanda = "KORISNIK " + korisnik + "; LOZINKA " + lozinka + ";";
            saljiKomandu(komanda);
            if (citaj().contains("OK")) {
                jsonObject.add("odgovor", new JsonArray());
                jsonObject.addProperty("status", "OK");
            } else {
                jsonObject.addProperty("status", "ERR");
                jsonObject.addProperty("poruka", "Neuspjesna autentikacija");
            }
        } else {
            jsonObject = pronadiKorisnika(korisnik);
        }
        return gson.toJson(jsonObject);
    }

    /**
     * PUT method for updating or creating an instance of KorisniciREST
     *
     * @param korisnik
     * @param podaci
     * @return
     */
    @Path("{id}")
    @PUT
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public String putJson(@PathParam("id") String korisnik, String podaci) {
        try {
            jsonObject = gson.fromJson(podaci, JsonObject.class);
            lozinka = jsonObject.get("lozinka").getAsString();
            String prezime = jsonObject.get("prezime").getAsString();
            String ime = jsonObject.get("ime").getAsString();
            String email = jsonObject.get("email").getAsString();
            Korisnik k = new Korisnik();
            k.setKorIme(korisnik);
            k.setLozinka(lozinka);
            k.setPrezime(prezime);
            k.setIme(ime);
            k.setEmail(email);
            jsonObject = new JsonObject();
            if (azurirajKorisnika(korime, pw, k)) {
                jsonObject.add("odgovor", new JsonArray());
                jsonObject.addProperty("status", "OK");
            } else {
                jsonObject.addProperty("status", "ERR");
                jsonObject.addProperty("poruka", "Pogreska prilikom azuriranja korisnika");
            }
        } catch (Exception ex) {
            jsonObject = new JsonObject();
            jsonObject.addProperty("status", "ERR");
            jsonObject.addProperty("poruka", "greska:" + ex.getMessage());
        }
        return gson.toJson(jsonObject);
    }

    private void saljiKomandu(String komanda) {
        BufferedWriter bw;
        try {
            socket = new Socket(host, port);
            bw = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream(), Charset.forName("UTF-8")));
            bw.write(komanda);
            bw.flush();
            socket.getOutputStream().flush();
            socket.shutdownOutput();
        } catch (IOException ex) {
            Logger.getLogger(KorisniciREST.class.getName()).log(Level.SEVERE, null, ex);
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
            Logger.getLogger(KorisniciREST.class.getName()).log(Level.SEVERE, null, ex);
        }
        return stringBuilder.toString();
    }

    private static java.util.List<org.foi.nwtis.npetanjek.ws.klijenti.Korisnik> dajPodatkeoKorisnicima(java.lang.String korisnik, java.lang.String lozinka) {
        org.foi.nwtis.npetanjek.ws.klijenti.AvioniWS_Service service = new org.foi.nwtis.npetanjek.ws.klijenti.AvioniWS_Service();
        org.foi.nwtis.npetanjek.ws.klijenti.AvioniWS port = service.getAvioniWSPort();
        return port.dajPodatkeoKorisnicima(korisnik, lozinka);
    }

    private static boolean dodajKorisnika(java.lang.String korime, java.lang.String loz, org.foi.nwtis.npetanjek.ws.klijenti.Korisnik korisnik) {
        org.foi.nwtis.npetanjek.ws.klijenti.AvioniWS_Service service = new org.foi.nwtis.npetanjek.ws.klijenti.AvioniWS_Service();
        org.foi.nwtis.npetanjek.ws.klijenti.AvioniWS port = service.getAvioniWSPort();
        return port.dodajKorisnika(korime, loz, korisnik);
    }

    private static boolean azurirajKorisnika(java.lang.String korime, java.lang.String loz, org.foi.nwtis.npetanjek.ws.klijenti.Korisnik korisnik) {
        org.foi.nwtis.npetanjek.ws.klijenti.AvioniWS_Service service = new org.foi.nwtis.npetanjek.ws.klijenti.AvioniWS_Service();
        org.foi.nwtis.npetanjek.ws.klijenti.AvioniWS port = service.getAvioniWSPort();
        return port.azurirajKorisnika(korime, loz, korisnik);
    }

    
}
