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
import org.foi.nwtis.npetanjek.ws.klijenti.Korisnik;

/**
 *
 * @author Nikola
 */
@Named(value = "pregledKorisnika")
@SessionScoped
public class PregledKorisnika implements Serializable {
    
    private String korisnik;
    private List<Korisnik> korisnici;
    private List<Korisnik> korisniciStranicenje;
    private int trenutnaStranica = 1;
    int pocetak = 0;
    private int brojLinija = 5;

    /**
     * Creates a new instance of PregledKorisnika
     */
    public PregledKorisnika() {
        HttpSession sesija = (HttpSession) FacesContext.getCurrentInstance().getExternalContext().getSession(false);
        if (sesija != null && sesija.getAttribute("korisnik") != null) {
            korisnik = sesija.getAttribute("korisnik").toString();
        }
    }

    public String getKorisnik() {
        return korisnik;
    }
    
    public void sljedecaStranica() {
        
        trenutnaStranica++;
    }

    public List<Korisnik> getKorisnici() {
        // probati napraviti stranicenje
        KorisniciREST_JerseyClient client = new KorisniciREST_JerseyClient();
        Gson gson = new Gson();
        String odgovor = client.dajSveKorisnike();
        JsonObject jsonObject = gson.fromJson(odgovor, JsonObject.class);
        JsonElement jsonElement = jsonObject.get("odgovor");
        korisnici = gson.fromJson(jsonElement, List.class);
        brojLinija = korisnici.size();
        return korisnici;
    }

    public int getTrenutnaStranica() {
        return trenutnaStranica;
    }

    public int getBrojLinija() {
        return brojLinija;
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

        public String dajKorisnika(String id) throws ClientErrorException {
            WebTarget resource = webTarget;
            resource = resource.path(java.text.MessageFormat.format("{0}", new Object[]{id}));
            return resource.request(javax.ws.rs.core.MediaType.APPLICATION_JSON).get(String.class);
        }

        public String autentikacijaJson(Object requestEntity, String id) throws ClientErrorException {
            return webTarget.path(java.text.MessageFormat.format("{0}", new Object[]{id})).request(javax.ws.rs.core.MediaType.APPLICATION_JSON).post(javax.ws.rs.client.Entity.entity(requestEntity, javax.ws.rs.core.MediaType.APPLICATION_JSON), String.class);
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
