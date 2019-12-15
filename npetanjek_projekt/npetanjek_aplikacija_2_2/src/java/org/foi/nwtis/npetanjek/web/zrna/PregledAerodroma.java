/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.foi.nwtis.npetanjek.web.zrna;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
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
import org.foi.nwtis.npetanjek.web.podaci.Aerodrom;
import org.foi.nwtis.npetanjek.web.podaci.MarkerPodaci;
import org.foi.nwtis.npetanjek.web.slusaci.SlusacAplikacije;
import org.foi.nwtis.npetanjek.ws.klijenti.AvioniWS_Service;
import org.foi.nwtis.npetanjek.ws.klijenti.MeteoPodaci;
import org.primefaces.event.map.OverlaySelectEvent;
import org.primefaces.model.map.DefaultMapModel;
import org.primefaces.model.map.LatLng;
import org.primefaces.model.map.MapModel;
import org.primefaces.model.map.Marker;
import org.primefaces.model.map.Overlay;

/**
 *
 * @author Nikola
 */
@Named(value = "pregledAerodroma")
@SessionScoped
public class PregledAerodroma implements Serializable {

    @WebServiceRef(wsdlLocation = "WEB-INF/wsdl/localhost_8084/npetanjek_aplikacija_1/AvioniWS.wsdl")
    private AvioniWS_Service service;

    private final String korisnik;
    private final String lozinka;
    private List<Aerodrom> aerodromi;
    AerodromiREST_JerseyClient client;
    Gson gson;
    private String poruka;
    private String odabraniAerodrom;
    private String icao;
    private Aerodrom aerodrom;
    private MeteoPodaci meteoPodaci;
    Overlay overlay;
    private Marker odabraniMarker;
    private MapModel mapModel;
    MarkerPodaci markerPodaci;
    private final int brojAerodromaMeni;
    Konfiguracija konfiguracija;

    /**
     * Creates a new instance of PregledAerodroma
     */
    public PregledAerodroma() {
        HttpSession sesija = (HttpSession) FacesContext.getCurrentInstance().getExternalContext().getSession(false);
        korisnik = sesija.getAttribute("korisnik").toString();
        lozinka = sesija.getAttribute("lozinka").toString();
        gson = new Gson();
        client = new AerodromiREST_JerseyClient();
        konfiguracija = SlusacAplikacije.getKonfiguracija();
        brojAerodromaMeni = Integer.parseInt(konfiguracija.dajPostavku("brojAerodromaMeni"));
    }

    public String getKorisnik() {
        return korisnik;
    }

    public String getLozinka() {
        return lozinka;
    }

    private boolean provjeriStatus(JsonObject jsonObject) {
        JsonElement jsonElement = jsonObject.get("status");
        return !jsonElement.getAsString().equals("ERR");
    }

    public List<Aerodrom> getAerodromi() {
        //client = new AerodromiREST_JerseyClient();
        JsonObject jsonObject = gson.fromJson(client.dajSveAerodrome(), JsonObject.class);
        if (!provjeriStatus(jsonObject)) {
            return null;
        }
        JsonElement jsonElement = jsonObject.get("odgovor");
        this.aerodromi = gson.fromJson(jsonElement, List.class);
        return aerodromi;
    }

    public void dodajAerodrom() {
        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("icao", icao);
        String odgovor = client.dodajAerodrom(gson.toJson(jsonObject));
        jsonObject = gson.fromJson(odgovor, JsonObject.class);
        if (!provjeriStatus(jsonObject)) {
            poruka = jsonObject.get("poruka").getAsString();
        } else {
            poruka = "Aerodrom je uspješno dodan";
        }
    }

    public void brisiAerodrom() {
        String odgovor = client.brisiAerodrom(odabraniAerodrom);
        JsonObject jsonObject = gson.fromJson(odgovor, JsonObject.class);
        if (provjeriStatus(jsonObject)) {
            poruka = "Aerodrom je obrisan";
        } else {
            poruka = jsonObject.get("poruka").getAsString();
        }
    }

    public void dajPodatkeAerodroma() {
        String odgovor = client.dajPodatkeAerodroma(odabraniAerodrom);
        JsonObject jsonObject = gson.fromJson(odgovor, JsonObject.class);
        aerodrom = gson.fromJson(jsonObject.get("odgovor"), Aerodrom.class);
        dajMeteoPodatke();
        markerPodaci = new MarkerPodaci(aerodrom, meteoPodaci);
        LatLng latLng = new LatLng(Double.parseDouble(aerodrom.getLokacija().getLatitude()), Double.parseDouble(aerodrom.getLokacija().getLongitude()));
        overlay = new Marker(latLng, aerodrom.getNaziv());
        overlay.setData(markerPodaci.getPodaci());
        mapModel = new DefaultMapModel();
        mapModel.addOverlay(overlay);
    }

    public void blokirajAerodrom() {
        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("status", "BLOKIRAN");
        client.azurirajAerodrom(gson.toJson(jsonObject), odabraniAerodrom);
    }

    public void aktivirajAerodrom() {
        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("status", "AKTIVAN");
        client.azurirajAerodrom(gson.toJson(jsonObject), odabraniAerodrom);
    }

    public void dajMeteoPodatke() {
        meteoPodaci = dajMeteoPodatke(korisnik, lozinka, odabraniAerodrom);
        
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

    public String getIcao() {
        return icao;
    }

    public void setIcao(String icao) {
        this.icao = icao;
    }

    public Aerodrom getAerodrom() {
        return aerodrom;
    }

    public MeteoPodaci getMeteoPodaci() {
        return meteoPodaci;
    }
    
    public void onMarkerSelect(OverlaySelectEvent event) {
        odabraniMarker = (Marker) event.getOverlay();
    }

    

    private MeteoPodaci dajMeteoPodatke(java.lang.String korisnik, java.lang.String lozinka, java.lang.String icao) {
        // Note that the injected javax.xml.ws.Service reference as well as port objects are not thread safe.
        // If the calling of port operations may lead to race condition some synchronization is required.
        org.foi.nwtis.npetanjek.ws.klijenti.AvioniWS port = service.getAvioniWSPort();
        return port.dajMeteoPodatke(korisnik, lozinka, icao);
    }

    public Marker getOdabraniMarker() {
        return odabraniMarker;
    }

    public MapModel getMapModel() {
        return mapModel;
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

}
