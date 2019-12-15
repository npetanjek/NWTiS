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
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.UriInfo;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.Produces;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PUT;
import javax.ws.rs.PathParam;
import javax.ws.rs.core.MediaType;
import org.foi.nwtis.npetanjek.db.MyDataBase;
import org.foi.nwtis.npetanjek.konfiguracije.Konfiguracija;
import org.foi.nwtis.npetanjek.web.slusaci.SlusacAplikacije;
import org.foi.nwtis.npetanjek.ws.klijenti.Aerodrom;
import org.foi.nwtis.npetanjek.ws.klijenti.AerodromStatus;
import org.foi.nwtis.npetanjek.ws.klijenti.Avion;

/**
 * REST Web Service
 *
 * @author Nikola
 */
@Path("aerodromi")
public class AerodromiREST {

    @Context
    private UriInfo context;

    Gson gson;
    JsonObject jsonObject;
    String korisnik;
    String lozinka;
    Konfiguracija konfiguracija;
    MyDataBase mdb;
    static int id = 1;

    /**
     * Creates a new instance of AerodromiREST
     */
    public AerodromiREST() {
        gson = new Gson();
        mdb = MyDataBase.getInstance();
        konfiguracija = SlusacAplikacije.getKonfiguracija();
        korisnik = konfiguracija.dajPostavku("korisnik");
        lozinka = konfiguracija.dajPostavku("lozinka");
    }

    /**
     * Retrieves representation of an instance of
     * org.foi.nwtis.npetanjek.rest.AerodromiREST
     *
     * @return an instance of java.lang.String
     */
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public String dajSveAerodrome() {
        jsonObject = new JsonObject();
        List<Aerodrom> aerodromi = dajSveAerodromeGrupe(korisnik, lozinka);
        if (aerodromi.isEmpty()) {
            jsonObject.addProperty("status", "ERR");
            jsonObject.addProperty("poruka", "Nije pronaden ni jedan aerodrom");
        } else {
            jsonObject.add("odgovor", gson.toJsonTree(aerodromi));
            jsonObject.addProperty("status", "OK");
        }
        return gson.toJson(jsonObject);
    }

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public String dodajAerodrom(String icao) {
        jsonObject = gson.fromJson(icao, JsonObject.class);
        String upit = "SELECT * FROM airports WHERE ident = '" + jsonObject.get("icao").getAsString() + "'";
        List<Aerodrom> aerodromi = mdb.dohvatiAerodrome(upit);
        if (aerodromi.isEmpty()) {
            jsonObject = new JsonObject();
            jsonObject.addProperty("status", "ERR");
            jsonObject.addProperty("poruka", "Nije pronaden aerodrom s tom ICAO oznakom");
        } else {
            Aerodrom aerodrom = aerodromi.get(0);
            jsonObject = new JsonObject();
            //aerodrom.setStatus(AerodromStatus.AKTIVAN);
            System.out.println("Aerodrom: " + aerodrom.getNaziv());
            if (!dodajAerodromGrupi(korisnik, lozinka, aerodrom)) {
                jsonObject.add("odgovor", new JsonArray());
                jsonObject.addProperty("status", "OK");
                String ident = aerodrom.getIcao();
                String naziv = aerodrom.getNaziv();
                String drzava = aerodrom.getDrzava();
                String lon = aerodrom.getLokacija().getLatitude();
                String lat = aerodrom.getLokacija().getLongitude();
                upit = "INSERT INTO myairports VALUES ('" + ident + "', '" + naziv + "', '" + drzava + "', '" + lat + ", " + lon + "', '" + new Timestamp(System.currentTimeMillis()) + "')";
                mdb.insert(upit);
            } else {
                jsonObject.addProperty("status", "ERR");
                jsonObject.addProperty("poruka", "Pogreska kod dodavanja aerodroma");
            }
        }
        return gson.toJson(jsonObject);
    }

    @Path("{id}")
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public String dajPodatkeAerodroma(@PathParam("id") String icao) {
        jsonObject = new JsonObject();
        String upit = "SELECT * FROM myairports WHERE ident = '" + icao + "'";
        List<Aerodrom> aerodromi = mdb.dohvatiAerodrome(upit);
        if (aerodromi.isEmpty()) {
            jsonObject.addProperty("status", "ERR");
            jsonObject.addProperty("poruka", "Nije pronaden aerodrom s ICAO oznakom " + icao);
        } else {
            Aerodrom aerodrom = aerodromi.get(0);
            AerodromStatus statusAerodroma = dajStatusAerodromaGrupe(korisnik, lozinka, icao);
            aerodrom.setStatus(statusAerodroma);
            jsonObject.add("odgovor", gson.toJsonTree(aerodrom));
            jsonObject.addProperty("status", "OK");
        }
        return gson.toJson(jsonObject);
    }

    /**
     * PUT method for updating or creating an instance of AerodromiREST
     *
     * @param icao
     * @param content representation for the resource
     * @return
     */
    @Path("{id}")
    @PUT
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public String azurirajAerodrom(@PathParam("id") String icao, String content) {
        jsonObject = gson.fromJson(content, JsonObject.class);
        String status = jsonObject.get("status").getAsString();
        if (status.equals("BLOKIRAN")) {
            blokirajAerodromGrupe(korisnik, lozinka, icao);
            jsonObject = new JsonObject();
            jsonObject.add("odgovor", new JsonArray());
            jsonObject.addProperty("status", "OK");
        }
        if (status.equals("AKTIVAN")) {
            aktivirajAerodromGrupe(korisnik, lozinka, icao);
            jsonObject = new JsonObject();
            jsonObject.add("odgovor", new JsonArray());
            jsonObject.addProperty("status", "OK");
        }
        return gson.toJson(jsonObject);
    }

    private boolean provjeriStatus(JsonObject jsonObject) {
        JsonElement jsonElement = jsonObject.get("status");
        if (jsonElement.getAsString().equals("ERR")) {
            return false;
        }
        return true;
    }

    @Path("{id}")
    @DELETE
    @Produces(MediaType.APPLICATION_JSON)
    public String brisiAerodrom(@PathParam("id") String icao) {
        String avioniAerodroma = dajAvioneAerodroma(icao);
        jsonObject = gson.fromJson(avioniAerodroma, JsonObject.class);
        if (!provjeriStatus(jsonObject)) {
            jsonObject = new JsonObject();
            if (obrisiAerodromGrupe(korisnik, lozinka, icao)) {
                String upit = "DELETE FROM MYAIRPORTS WHERE ident='" + icao + "'";
                mdb.update(upit);
                jsonObject.add("odgovor", new JsonArray());
                jsonObject.addProperty("status", "OK");
            } else {
                jsonObject.addProperty("STATUS", "ERR");
                jsonObject.addProperty("poruka", "Pogreska prilikom brisanja aerodroma");
            }
        } else {
            jsonObject = new JsonObject();
            jsonObject.addProperty("status", "ERR");
            jsonObject.addProperty("poruka", "Pogreska prilikom brisanja aerodroma -- aerodrom ima pridruzene avione");
        }
        return gson.toJson(jsonObject);
    }

    @Path("{id}/avion")
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public String dajAvioneAerodroma(@PathParam("id") String icao) {
        jsonObject = new JsonObject();
        List<Avion> avioni = dajSveAvioneAerodromaGrupe(korisnik, lozinka, icao);
        if (!avioni.isEmpty()) {
            boolean imaAviona = false;
            for (Avion avion : avioni) {
                if (avion.getEstdepartureairport().equals(icao) || avion.getEstarrivalairport().equals(icao)) {
                    imaAviona = true;
                }
            }
            if (imaAviona) {
                jsonObject.add("odgovor", gson.toJsonTree(avioni));
                jsonObject.addProperty("status", "OK");
            } else {
                jsonObject.addProperty("status", "ERR");
                jsonObject.addProperty("poruka", "Nisu pronadeni avioni aerodroma");
            }
        } else {
            jsonObject.addProperty("status", "ERR");
            jsonObject.addProperty("poruka", "Pogreška prilikom dohvaćanja aviona aerodroma");
        }
        return gson.toJson(jsonObject);
    }

    @Path("{id}/avion")
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public String dodajAvionAerodromu(String icao24, @PathParam("id") String icao) {

        String upit = "SELECT * FROM myairports WHERE ident='" + icao + "'";
        List<Aerodrom> aerodromi = mdb.dohvatiAerodrome(upit);
        if (aerodromi.isEmpty()) {
            jsonObject = new JsonObject();
            jsonObject.addProperty("status", "ERR");
            jsonObject.addProperty("poruka", "Grupi nije dodan aerodrom s ICAO oznakom " + icao);
            return gson.toJson(jsonObject);
        }
        Aerodrom aerodrom = aerodromi.get(0);
        Avion avion = new Avion();
        jsonObject = gson.fromJson(icao24, JsonObject.class);
        String icao24String = jsonObject.get("icao24").getAsString();
        String callsign = jsonObject.get("callsign").getAsString();
        String estArrivalAirport = jsonObject.get("estarrivalairport").getAsString();
        avion.setIcao24(icao24String);
        avion.setId(id++);
        avion.setCallsign(callsign);
        avion.setEstdepartureairport(aerodrom.getIcao());
        avion.setEstarrivalairport(estArrivalAirport);
        if (dodajAvionGrupi(korisnik, lozinka, avion)) {
            jsonObject = new JsonObject();
            jsonObject.add("odgovor", new JsonArray());
            jsonObject.addProperty("status", "OK");
        }
        return gson.toJson(jsonObject);
    }

    @Path("{id}/avion/") // Provjeriti ovu putanju
    @DELETE
    @Produces(MediaType.APPLICATION_JSON)
    public String brisiSveAvioneAerodroma(@PathParam("id") String icao) {
        List<Avion> avioni = dajSveAvioneAerodromaGrupe(korisnik, lozinka, icao);
        if (!avioni.isEmpty()) {
            for (Avion avion : avioni) {
                if (avion.getEstdepartureairport().equals(icao) || avion.getEstarrivalairport().equals(icao)) {

                }
            }
        }
        return gson.toJson(jsonObject);
    }

    @Path("{id}/avion/{aid}")
    @DELETE
    @Produces(MediaType.APPLICATION_JSON)
    public String brisiAvionAerodroma(@PathParam("aid") String icao24, @PathParam("id") String icao) {
        return gson.toJson(jsonObject);
    }

    private static java.util.List<org.foi.nwtis.npetanjek.ws.klijenti.Aerodrom> dajSveAerodromeGrupe(java.lang.String korisnickoIme, java.lang.String korisnickaLozinka) {
        org.foi.nwtis.npetanjek.ws.klijenti.AerodromiWS_Service service = new org.foi.nwtis.npetanjek.ws.klijenti.AerodromiWS_Service();
        org.foi.nwtis.npetanjek.ws.klijenti.AerodromiWS port = service.getAerodromiWSPort();
        return port.dajSveAerodromeGrupe(korisnickoIme, korisnickaLozinka);
    }

    private static Boolean dodajAerodromGrupi(java.lang.String korisnickoIme, java.lang.String korisnickaLozinka, org.foi.nwtis.npetanjek.ws.klijenti.Aerodrom serodrom) {
        org.foi.nwtis.npetanjek.ws.klijenti.AerodromiWS_Service service = new org.foi.nwtis.npetanjek.ws.klijenti.AerodromiWS_Service();
        org.foi.nwtis.npetanjek.ws.klijenti.AerodromiWS port = service.getAerodromiWSPort();
        return port.dodajAerodromGrupi(korisnickoIme, korisnickaLozinka, serodrom);
    }

    private static boolean obrisiAerodromGrupe(java.lang.String korisnickoIme, java.lang.String korisnickaLozinka, java.lang.String idAerodrom) {
        org.foi.nwtis.npetanjek.ws.klijenti.AerodromiWS_Service service = new org.foi.nwtis.npetanjek.ws.klijenti.AerodromiWS_Service();
        org.foi.nwtis.npetanjek.ws.klijenti.AerodromiWS port = service.getAerodromiWSPort();
        return port.obrisiAerodromGrupe(korisnickoIme, korisnickaLozinka, idAerodrom);
    }

    private static AerodromStatus dajStatusAerodromaGrupe(java.lang.String korisnickoIme, java.lang.String korisnickaLozinka, java.lang.String idAerodrom) {
        org.foi.nwtis.npetanjek.ws.klijenti.AerodromiWS_Service service = new org.foi.nwtis.npetanjek.ws.klijenti.AerodromiWS_Service();
        org.foi.nwtis.npetanjek.ws.klijenti.AerodromiWS port = service.getAerodromiWSPort();
        return port.dajStatusAerodromaGrupe(korisnickoIme, korisnickaLozinka, idAerodrom);
    }

    private static java.util.List<org.foi.nwtis.npetanjek.ws.klijenti.Avion> dajSveAvioneAerodromaGrupe(java.lang.String korisnickoIme, java.lang.String korisnickaLozinka, java.lang.String idAerodrom) {
        org.foi.nwtis.npetanjek.ws.klijenti.AerodromiWS_Service service = new org.foi.nwtis.npetanjek.ws.klijenti.AerodromiWS_Service();
        org.foi.nwtis.npetanjek.ws.klijenti.AerodromiWS port = service.getAerodromiWSPort();
        return port.dajSveAvioneAerodromaGrupe(korisnickoIme, korisnickaLozinka, idAerodrom);
    }

    private static boolean dodajAvionGrupi(java.lang.String korisnickoIme, java.lang.String korisnickaLozinka, org.foi.nwtis.npetanjek.ws.klijenti.Avion avionNovi) {
        org.foi.nwtis.npetanjek.ws.klijenti.AerodromiWS_Service service = new org.foi.nwtis.npetanjek.ws.klijenti.AerodromiWS_Service();
        org.foi.nwtis.npetanjek.ws.klijenti.AerodromiWS port = service.getAerodromiWSPort();
        return port.dodajAvionGrupi(korisnickoIme, korisnickaLozinka, avionNovi);
    }

    private static boolean postaviAvioneGrupe(java.lang.String korisnickoIme, java.lang.String korisnickaLozinka, java.util.List<org.foi.nwtis.npetanjek.ws.klijenti.Avion> avioniNovi) {
        org.foi.nwtis.npetanjek.ws.klijenti.AerodromiWS_Service service = new org.foi.nwtis.npetanjek.ws.klijenti.AerodromiWS_Service();
        org.foi.nwtis.npetanjek.ws.klijenti.AerodromiWS port = service.getAerodromiWSPort();
        return port.postaviAvioneGrupe(korisnickoIme, korisnickaLozinka, avioniNovi);
    }

    private static boolean blokirajAerodromGrupe(java.lang.String korisnickoIme, java.lang.String korisnickaLozinka, java.lang.String idAerodrom) {
        org.foi.nwtis.npetanjek.ws.klijenti.AerodromiWS_Service service = new org.foi.nwtis.npetanjek.ws.klijenti.AerodromiWS_Service();
        org.foi.nwtis.npetanjek.ws.klijenti.AerodromiWS port = service.getAerodromiWSPort();
        return port.blokirajAerodromGrupe(korisnickoIme, korisnickaLozinka, idAerodrom);
    }

    private static boolean aktivirajAerodromGrupe(java.lang.String korisnickoIme, java.lang.String korisnickaLozinka, java.lang.String idAerodrom) {
        org.foi.nwtis.npetanjek.ws.klijenti.AerodromiWS_Service service = new org.foi.nwtis.npetanjek.ws.klijenti.AerodromiWS_Service();
        org.foi.nwtis.npetanjek.ws.klijenti.AerodromiWS port = service.getAerodromiWSPort();
        return port.aktivirajAerodromGrupe(korisnickoIme, korisnickaLozinka, idAerodrom);
    }
}
