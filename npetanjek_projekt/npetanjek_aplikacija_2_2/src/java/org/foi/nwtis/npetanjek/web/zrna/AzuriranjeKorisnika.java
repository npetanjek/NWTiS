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
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.faces.context.FacesContext;
import javax.servlet.http.HttpSession;
import javax.ws.rs.ClientErrorException;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.WebTarget;

/**
 *
 * @author Nikola
 */
@Named(value = "azurirnjeKorisnika")
@SessionScoped
public class AzuriranjeKorisnika implements Serializable {
    
    private boolean renderAzuriranjeKorisnika = false;
    private boolean renderButton = true;
    private String korisnik;
    private String lozinka;
    private String ponovljenaLozinka;
    private String prezime;
    private String ime;
    private String email;
    private String poruka;

    /**
     * Creates a new instance of AzurirnjeKorisnika
     */
    public AzuriranjeKorisnika() {
        HttpSession sesija = (HttpSession) FacesContext.getCurrentInstance().getExternalContext().getSession(false);
        if (sesija != null && sesija.getAttribute("korisnik") != null) {
            korisnik = sesija.getAttribute("korisnik").toString();
        }
    }
    
    public void azurirajPodatke() {
        if (!provjeriObrazac())
            return;
        KorisniciREST_JerseyClient client = new KorisniciREST_JerseyClient();
        JsonObject jsonObject = new JsonObject();
        Gson gson = new Gson();
        jsonObject.addProperty("lozinka", lozinka);
        jsonObject.addProperty("prezime", prezime);
        jsonObject.addProperty("ime", ime);
        jsonObject.addProperty("email", email);
        System.out.println("korisnik: " + korisnik);
        String odgovor = client.putJson(gson.toJson(jsonObject), korisnik);
        if (provjeriStatus(gson.fromJson(odgovor, JsonObject.class))) {
            poruka = "Podaci su uspješno ažurirani";
            renderAzuriranjeKorisnika = false;
            renderButton = true;
        }
    }
    
    private boolean provjeriStatus (JsonObject jsonObject) {
        JsonElement jsonElement = jsonObject.get("status");
        return jsonElement.getAsString().equals("OK");
    }
    
    private boolean provjeriObrazac() {
        if (lozinka.equals("") || ponovljenaLozinka.equals("") || prezime.equals("") || ime.equals("") || email.equals("")) {
            poruka = "Niste unijeli sve podatke!";
            return false;
        }
        Pattern pattern;
        Matcher m;
        final String LOZINKA_REGEX = "^[a-zA-Z0-9ČčĆćŠšĐđŽž!#_-]{1,10}$";
        final String PREZ_IME_REGEX = "^[a-zA-Z]{1,30}$";
        final String EMAIL_REGEX = "^[a-z]{1,10}@[a-z]{1,}\\.[a-z]{1,}$";
        pattern = Pattern.compile(LOZINKA_REGEX);
        m = pattern.matcher(lozinka);
        if (!m.matches()) {
            poruka = "Neispravan format lozinke!";
            return false;
        }
        pattern = Pattern.compile(PREZ_IME_REGEX);
        m = pattern.matcher(prezime);
        if (!m.matches()) {
            poruka = "Neispravan format prezimena!";
            return false;
        }
        pattern = Pattern.compile(PREZ_IME_REGEX);
        m = pattern.matcher(ime);
        if (!m.matches()) {
            poruka = "Neispravan format imena!";
            return false;
        }
        pattern = Pattern.compile(EMAIL_REGEX);
        m = pattern.matcher(email);
        if (!m.matches()) {
            poruka = "Neispravan format email-a!";
            return false;
        }
        if (!lozinka.equals(ponovljenaLozinka)) {
            poruka = "Lozinke se ne podudaraju!";
            return false;
        }
        return true;
    }

    public boolean isRenderAzuriranjeKorisnika() {
        return renderAzuriranjeKorisnika;
    }

    public void setRenderAzuriranjeKorisnika(boolean renderAzuriranjeKorisnika) {
        renderButton = false;
        this.renderAzuriranjeKorisnika = renderAzuriranjeKorisnika;
    }

    public String getKorisnik() {
        return korisnik;
    }

    public void setKorisnik(String korisnik) {
        this.korisnik = korisnik;
    }

    public String getLozinka() {
        return lozinka;
    }

    public void setLozinka(String lozinka) {
        this.lozinka = lozinka;
    }

    public String getPonovljenaLozinka() {
        return ponovljenaLozinka;
    }

    public void setPonovljenaLozinka(String ponovljenaLozinka) {
        this.ponovljenaLozinka = ponovljenaLozinka;
    }

    public String getPrezime() {
        return prezime;
    }

    public void setPrezime(String prezime) {
        this.prezime = prezime;
    }

    public String getIme() {
        return ime;
    }

    public void setIme(String ime) {
        this.ime = ime;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPoruka() {
        return poruka;
    }

    public boolean isRenderButton() {
        return renderButton;
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
