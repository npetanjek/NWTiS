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
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.faces.context.FacesContext;
import javax.servlet.http.HttpSession;
import javax.ws.rs.ClientErrorException;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.WebTarget;
import javax.xml.ws.WebServiceRef;
import org.foi.nwtis.npetanjek.konfiguracije.Konfiguracija;
import org.foi.nwtis.npetanjek.web.podaci.Aerodrom;
import org.foi.nwtis.npetanjek.web.slusaci.SlusacAplikacije;
import org.foi.nwtis.npetanjek.ws.klijenti.AvionLeti;
import org.foi.nwtis.npetanjek.ws.klijenti.AvioniWS_Service;

/**
 *
 * @author Nikola
 */
@Named(value = "pregledLetova")
@SessionScoped
public class PregledLetova implements Serializable {

    @WebServiceRef(wsdlLocation = "WEB-INF/wsdl/localhost_8084/npetanjek_aplikacija_1/AvioniWS.wsdl")
    private AvioniWS_Service service;
    
    private final String korisnik;
    private final String lozinka;
    AerodromiREST_JerseyClient client;
    private String poruka;
    private List<Aerodrom> aerodromi;
    private String odabraniAerodrom;
    private String odVremena;
    private String doVremena;
    private List<AvionLeti> avioni;
    private List<AvionLeti> avionKrozAerodrome;
    private boolean renderAvioni = true;
    private boolean renderLetovi = false;
    Gson gson;
    private final int brojLinija;
    private final int brojAerodromaMeni;
    Konfiguracija konfiguracija;

    /**
     * Creates a new instance of PregledLetova
     */
    public PregledLetova() {
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

    public String getLozinka() {
        return lozinka;
    }

    public String getPoruka() {
        return poruka;
    }
    
    private String convertDate(String date) {
        DateFormat originalSDF = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss");
        DateFormat targetSDF = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        Date datum;
        try {
            datum = originalSDF.parse(date);
            return targetSDF.format(datum);
        } catch (ParseException ex) {
            Logger.getLogger(PregledLetova.class.getName()).log(Level.SEVERE, null, ex);
            return "";
        }        
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
    
    public void preuzmiAvione() {
        String pocetak = convertDate(odVremena);
        String kraj = convertDate(doVremena);
        avioni = dajAvionesAerodroma(korisnik, lozinka, odabraniAerodrom, pocetak, kraj);
        renderLetovi = false;
        renderAvioni = true;
    }
    
    public void preuzmiLetoveAviona(String icao24) {
        String pocetak = convertDate(odVremena);
        String kraj = convertDate(doVremena);
        avionKrozAerodrome = dajAvionKrozAerodrome(korisnik, lozinka, icao24, pocetak, kraj);
        renderAvioni = false;
        renderLetovi = true;
    }

    public String getOdabraniAerodrom() {
        return odabraniAerodrom;
    }

    public void setOdabraniAerodrom(String odabraniAerodrom) {
        this.odabraniAerodrom = odabraniAerodrom;
    }

    public String getOdVremena() {
        return odVremena;
    }

    public void setOdVremena(String odVremena) {
        this.odVremena = odVremena;
    }

    public String getDoVremena() {
        return doVremena;
    }

    public void setDoVremena(String doVremena) {
        this.doVremena = doVremena;
    }

    public List<AvionLeti> getAvioni() {
        return avioni;
    }

    public List<AvionLeti> getAvionKrozAerodrome() {
        return avionKrozAerodrome;
    }

    public boolean isRenderAvioni() {
        return renderAvioni;
    }

    public boolean isRenderLetovi() {
        return renderLetovi;
    }

    public int getBrojLinija() {
        return brojLinija;
    }

    public int getBrojAerodromaMeni() {
        return brojAerodromaMeni;
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

    private java.util.List<org.foi.nwtis.npetanjek.ws.klijenti.AvionLeti> dajAvionesAerodroma(java.lang.String korisnik, java.lang.String lozinka, java.lang.String icao, java.lang.String odVremena, java.lang.String doVremena) {
        // Note that the injected javax.xml.ws.Service reference as well as port objects are not thread safe.
        // If the calling of port operations may lead to race condition some synchronization is required.
        org.foi.nwtis.npetanjek.ws.klijenti.AvioniWS port = service.getAvioniWSPort();
        return port.dajAvionesAerodroma(korisnik, lozinka, icao, odVremena, doVremena);
    }

    private java.util.List<org.foi.nwtis.npetanjek.ws.klijenti.AvionLeti> dajAvionKrozAerodrome(java.lang.String korisnik, java.lang.String lozinka, java.lang.String icao24, java.lang.String odDatuma, java.lang.String doDatuma) {
        // Note that the injected javax.xml.ws.Service reference as well as port objects are not thread safe.
        // If the calling of port operations may lead to race condition some synchronization is required.
        org.foi.nwtis.npetanjek.ws.klijenti.AvioniWS port = service.getAvioniWSPort();
        return port.dajAvionKrozAerodrome(korisnik, lozinka, icao24, odDatuma, doDatuma);
    }

    
}
