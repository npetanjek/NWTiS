/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.foi.nwtis.npetanjek.ejb.sb;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.ejb.Stateful;
import javax.ejb.LocalBean;
import javax.ws.rs.ClientErrorException;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.WebTarget;

/**
 *
 * @author Nikola
 */
@Stateful
@LocalBean
public class AutentikacijaSB {
    
    public boolean autenticiraj(String korisnik, String lozinka) {
        KorisniciREST_JerseyClient client = new KorisniciREST_JerseyClient();
        Gson gson = new Gson();
        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("lozinka", lozinka);
        System.out.println(gson.toJson(jsonObject));
        String odgovor;
        try {
            odgovor = client.autentikacijaJson(gson.toJson(jsonObject), korisnik);
            return provjeriStatus(gson.fromJson(odgovor, JsonObject.class));
        } catch (ClientErrorException | UnsupportedEncodingException ex) {
            Logger.getLogger(AutentikacijaSB.class.getName()).log(Level.SEVERE, null, ex);
        }
        return false;
    }
    
    private boolean provjeriStatus (JsonObject jsonObject) {
        JsonElement jsonElement = jsonObject.get("status");
        if (jsonElement.getAsString().equals("ERR")) {
            return false;
        }        
        return true;
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
