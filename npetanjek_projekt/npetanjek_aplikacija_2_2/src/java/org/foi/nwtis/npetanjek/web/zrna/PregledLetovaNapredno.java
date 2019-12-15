/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.foi.nwtis.npetanjek.web.zrna;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import javax.inject.Named;
import javax.enterprise.context.SessionScoped;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import javax.faces.context.FacesContext;
import javax.servlet.http.HttpSession;
import javax.ws.rs.ClientErrorException;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.WebTarget;
import javax.xml.ws.WebServiceRef;
import org.foi.nwtis.npetanjek.konfiguracije.Konfiguracija;
//import org.foi.nwtis.npetanjek.web.podaci.Aerodrom;
import org.foi.nwtis.npetanjek.web.podaci.Lokacija;
import org.foi.nwtis.npetanjek.web.slusaci.SlusacAplikacije;
import org.foi.nwtis.npetanjek.ws.klijenti.AvioniWS_Service;
import org.foi.nwtis.npetanjek.ws.klijenti.Aerodrom;

/**
 *
 * @author Nikola
 */
@Named(value = "pregledLetovaNapredno")
@SessionScoped
public class PregledLetovaNapredno implements Serializable {

    @WebServiceRef(wsdlLocation = "WEB-INF/wsdl/localhost_8084/npetanjek_aplikacija_1/AvioniWS.wsdl")
    private AvioniWS_Service service;
    
    private final String korisnik;
    private final String lozinka;
    private final transient AerodromiREST_JerseyClient client;
    private List<Aerodrom> aerodromi;
    private String odabraniAerodrom;
    private List<String> odabraniAerodromi;
    private String poruka;
    private String prviAerodromLat;
    private String prviAerodronLon;
    private String drugiAerodromLat;
    private String drugiAerodromLon;
    private double udaljenost;
    private final int brojAerodromaMeni;
    Konfiguracija konfiguracija;
    Gson gson;
    private double minUdaljenost;
    private double maxUdaljenost;
    private List<String> aerodromiUnutarGranica;
    private final int brojLinija;

    /**
     * Creates a new instance of PregledLetovaNapredno
     */
    public PregledLetovaNapredno() {
        HttpSession sesija = (HttpSession) FacesContext.getCurrentInstance().getExternalContext().getSession(false);
        korisnik = sesija.getAttribute("korisnik").toString();
        lozinka = sesija.getAttribute("lozinka").toString();
        client = new AerodromiREST_JerseyClient();
        gson = new Gson();
        konfiguracija = SlusacAplikacije.getKonfiguracija();
        brojAerodromaMeni = Integer.parseInt(konfiguracija.dajPostavku("brojAerodromaMeni"));
        brojLinija = Integer.parseInt(konfiguracija.dajPostavku("pregledLetova.brojLinija"));
    }
    
    private boolean provjeriStatus(JsonObject jsonObject) {
        JsonElement jsonElement = jsonObject.get("status");
        if (jsonElement.getAsString().equals("ERR")) {
            this.poruka = jsonObject.get("poruka").getAsString();
            return false;
        }
        return true;
    }

    public String getKorisnik() {
        return korisnik;
    }

    public List<Aerodrom> getAerodromi() {
        JsonObject jsonObject = gson.fromJson(client.dajSveAerodrome(), JsonObject.class);
        if (!provjeriStatus(jsonObject)) {
            return null;
        }
        JsonElement jsonElement = jsonObject.get("odgovor");
        this.aerodromi = gson.fromJson(jsonElement, List.class);
        return aerodromi;
    }
    
    public void izracunajUdaljenostIzmeduAerodroma() {
        System.out.println(odabraniAerodromi.get(0) + " " + odabraniAerodromi.get(1));
        String odgovor = client.dajPodatkeAerodroma(odabraniAerodromi.get(0));
        JsonObject jsonPrviAerodrom = gson.fromJson(odgovor, JsonObject.class);
        System.out.println("jsonPrviAerodrom: " + jsonPrviAerodrom);
        JsonElement jsonElement = jsonPrviAerodrom.get("odgovor");
        System.out.println("jsonElement: " + jsonElement);
        jsonPrviAerodrom = gson.fromJson(jsonElement, JsonObject.class);
        Lokacija lokacija = gson.fromJson(jsonPrviAerodrom.get("lokacija"), Lokacija.class);
        System.out.println("Lokacija: " + lokacija.getLatitude() + " " + lokacija.getLongitude());
        prviAerodromLat = lokacija.getLatitude();
        prviAerodronLon = lokacija.getLongitude();
        odgovor = client.dajPodatkeAerodroma(odabraniAerodromi.get(1));
        JsonObject jsonDrugiAerodrom = gson.fromJson(odgovor, JsonObject.class);
        jsonElement = jsonDrugiAerodrom.get("odgovor");
        jsonDrugiAerodrom = gson.fromJson(jsonElement, JsonObject.class);
        lokacija = gson.fromJson(jsonDrugiAerodrom.get("lokacija"), Lokacija.class);
        System.out.println("Druga lokacija: " + lokacija.getLatitude() + " " + lokacija.getLongitude());
        drugiAerodromLat = lokacija.getLatitude();
        drugiAerodromLon = lokacija.getLongitude();
        udaljenost = dajUdaljenostIzmeduAerodroma(korisnik, lozinka, odabraniAerodromi.get(0), odabraniAerodromi.get(1));
    }
    
    public void dohvatiAerodromeUnutarGranica() {
        aerodromiUnutarGranica = new ArrayList<>();
        List<Aerodrom> aerodromiUnutarGr = dajAerodromeUnutarGranice(odabraniAerodromi.get(0), minUdaljenost, maxUdaljenost);
        for (Aerodrom a : aerodromiUnutarGr) {
            aerodromiUnutarGranica.add(a.getIcao() + " " + a.getNaziv() + " " + dajUdaljenostIzmeduAerodroma(korisnik, lozinka, odabraniAerodromi.get(0), a.getIcao()) + " km");
        }
        if (aerodromiUnutarGranica.isEmpty()) {
            System.out.println("EMPTY");
        }
    }

    public String getPoruka() {
        return poruka;
    }

    public String getOdabraniAerodrom() {
        return odabraniAerodrom;
    }

    public void setOdabraniAerodrom(String odabraniAerodrom) {
        this.odabraniAerodrom = odabraniAerodrom;
    }

    public List<String> getOdabraniAerodromi() {
        return odabraniAerodromi;
    }

    public void setOdabraniAerodromi(List<String> odabraniAerodromi) {
        this.odabraniAerodromi = odabraniAerodromi;
    }

    public String getPrviAerodromLat() {
        return prviAerodromLat;
    }

    public String getPrviAerodronLon() {
        return prviAerodronLon;
    }

    public String getDrugiAerodromLat() {
        return drugiAerodromLat;
    }

    public String getDrugiAerodromLon() {
        return drugiAerodromLon;
    }

    public double getUdaljenost() {
        return udaljenost;
    }

    public int getBrojAerodromaMeni() {
        return brojAerodromaMeni;
    }

    public double getMinUdaljenost() {
        return minUdaljenost;
    }

    public void setMinUdaljenost(double minUdaljenost) {
        this.minUdaljenost = minUdaljenost;
    }

    public double getMaxUdaljenost() {
        return maxUdaljenost;
    }

    public void setMaxUdaljenost(double maxUdaljenost) {
        this.maxUdaljenost = maxUdaljenost;
    }

    public List<String> getAerodromiUnutarGranica() {
        return aerodromiUnutarGranica;
    }

    public int getBrojLinija() {
        return brojLinija;
    }

    static class AerodromiREST_JerseyClient {

        private WebTarget webTarget;
        private Client client;
        private static final String BASE_URI = "http://localhost:8084/npetanjek_aplikacija_1/webresources";

        public AerodromiREST_JerseyClient() {
            client = javax.ws.rs.client.ClientBuilder.newClient();
            webTarget = client.target(BASE_URI).path("aerodromi");
        }

        public String dodajAerodrom(Object requestEntity) throws ClientErrorException {
            return webTarget.request(javax.ws.rs.core.MediaType.APPLICATION_JSON).post(javax.ws.rs.client.Entity.entity(requestEntity, javax.ws.rs.core.MediaType.APPLICATION_JSON), String.class);
        }

        public String dajAvioneAerodroma(String id) throws ClientErrorException {
            WebTarget resource = webTarget;
            resource = resource.path(java.text.MessageFormat.format("{0}/avion", new Object[]{id}));
            return resource.request(javax.ws.rs.core.MediaType.APPLICATION_JSON).get(String.class);
        }

        public String dodajAvionAerodromu(Object requestEntity, String id) throws ClientErrorException {
            return webTarget.path(java.text.MessageFormat.format("{0}/avion", new Object[]{id})).request(javax.ws.rs.core.MediaType.APPLICATION_JSON).post(javax.ws.rs.client.Entity.entity(requestEntity, javax.ws.rs.core.MediaType.APPLICATION_JSON), String.class);
        }

        public String dajPodatkeAerodroma(String id) throws ClientErrorException {
            WebTarget resource = webTarget;
            resource = resource.path(java.text.MessageFormat.format("{0}", new Object[]{id}));
            return resource.request(javax.ws.rs.core.MediaType.APPLICATION_JSON).get(String.class);
        }

        public String dajSveAerodrome() throws ClientErrorException {
            WebTarget resource = webTarget;
            return resource.request(javax.ws.rs.core.MediaType.APPLICATION_JSON).get(String.class);
        }

        public String brisiAvionAerodroma(String id, String aid) throws ClientErrorException {
            return webTarget.path(java.text.MessageFormat.format("{0}/avion/{1}", new Object[]{id, aid})).request().delete(String.class);
        }

        public String azurirajAerodrom(Object requestEntity, String id) throws ClientErrorException {
            return webTarget.path(java.text.MessageFormat.format("{0}", new Object[]{id})).request(javax.ws.rs.core.MediaType.APPLICATION_JSON).put(javax.ws.rs.client.Entity.entity(requestEntity, javax.ws.rs.core.MediaType.APPLICATION_JSON), String.class);
        }

        public String brisiAerodrom(String id) throws ClientErrorException {
            return webTarget.path(java.text.MessageFormat.format("{0}", new Object[]{id})).request().delete(String.class);
        }

        public String brisiSveAvioneAerodroma(String id) throws ClientErrorException {
            return webTarget.path(java.text.MessageFormat.format("{0}/avion", new Object[]{id})).request().delete(String.class);
        }

        public void close() {
            client.close();
        }
    }

    private double dajUdaljenostIzmeduAerodroma(java.lang.String korisnik, java.lang.String lozinka, java.lang.String prviAerodrom, java.lang.String drugiAerodrom) {
        // Note that the injected javax.xml.ws.Service reference as well as port objects are not thread safe.
        // If the calling of port operations may lead to race condition some synchronization is required.
        org.foi.nwtis.npetanjek.ws.klijenti.AvioniWS port = service.getAvioniWSPort();
        return port.dajUdaljenostIzmeduAerodroma(korisnik, lozinka, prviAerodrom, drugiAerodrom);
    }

    private java.util.List<org.foi.nwtis.npetanjek.ws.klijenti.Aerodrom> dajAerodromeUnutarGranice(java.lang.String icao, double minUdaljenost, double maxUdaljenost) {
        // Note that the injected javax.xml.ws.Service reference as well as port objects are not thread safe.
        // If the calling of port operations may lead to race condition some synchronization is required.
        org.foi.nwtis.npetanjek.ws.klijenti.AvioniWS port = service.getAvioniWSPort();
        return port.dajAerodromeUnutarGranice(icao, minUdaljenost, maxUdaljenost);
    }

    
    
}
