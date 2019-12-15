/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.foi.nwtis.npetanjek.ws.serveri;

import java.sql.Timestamp;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.jws.WebService;
import javax.jws.WebMethod;
import javax.jws.WebParam;
import org.foi.nwtis.npetanjek.db.MyDataBase;
import org.foi.nwtis.npetanjek.konfiguracije.Konfiguracija;
import org.foi.nwtis.npetanjek.web.podaci.Korisnik;
import org.foi.nwtis.npetanjek.web.slusaci.SlusacAplikacije;
import org.foi.nwtis.npetanjek.ws.klijenti.Aerodrom;
import org.foi.nwtis.rest.klijenti.OWMKlijent;
import org.foi.nwtis.rest.podaci.AvionLeti;
import org.foi.nwtis.rest.podaci.MeteoPodaci;

/**
 *
 * @author Nikola
 */
@WebService(serviceName = "AvioniWS")
public class AvioniWS {

    MyDataBase mdb = MyDataBase.getInstance();

    private boolean autentificiraj(String korisnik, String lozinka) {
        String upit = "SELECT * FROM korisnici WHERE korisnicko_ime = '" + korisnik + "' AND lozinka = '" + lozinka + "'";
        return mdb.find(upit);
    }

    private int timeStampToEpoch(Timestamp timestamp) {
        String pattern = "dd-MM-yyyy HH:mm:ss";
        SimpleDateFormat sdf = new SimpleDateFormat(pattern);
        try {
            Date date = sdf.parse(sdf.format(timestamp));
            long epoch = date.getTime();
            return (int) (epoch / 1000);
        } catch (ParseException ex) {
            Logger.getLogger(AvioniWS.class.getName()).log(Level.SEVERE, null, ex);
            return 0;
        }
    }

    /**
     * Web service operation
     *
     * @param korisnik
     * @param lozinka
     * @param icao
     * @return
     */
    @WebMethod(operationName = "dajZadnjePreuzetePodatkeoAvionima")
    public AvionLeti dajZadnjePreuzetePodatkeoAvionima(@WebParam(name = "korisnik") String korisnik, @WebParam(name = "lozinka") String lozinka, @WebParam(name = "icao") String icao) {
        if (!autentificiraj(korisnik, lozinka)) {
            return null;
        }
        String upit = "SELECT * FROM airplanes WHERE estdepartureairport = '" + icao + "' ORDER BY stored DESC";
        List<AvionLeti> avioni = mdb.dohvatiAvione(upit);
        mdb.zapisiuDnevnik("SOAP - zadnje preuzeti podaci za " + icao, "Poziv web servisa", this.getClass().getSimpleName(), korisnik);
        return avioni.get(0);
    }

    /**
     * Web service operation
     *
     * @param korisnik
     * @param lozinka
     * @param icao
     * @param brojPodataka
     * @return
     */
    @WebMethod(operationName = "dajPosljednjihnPodatakaoAvionima")
    public List<AvionLeti> dajPosljednjihnPodatakaoAvionima(@WebParam(name = "korisnik") String korisnik, @WebParam(name = "lozinka") String lozinka, @WebParam(name = "icao") String icao, @WebParam(name = "brojPodataka") int brojPodataka) {
        if (!autentificiraj(korisnik, lozinka)) {
            return null;
        }
        String upit = "SELECT * FROM airplanes WHERE estdepartureairport = '" + icao + "' ORDER BY stored DESC";
        List<AvionLeti> avioni = mdb.dohvatiAvione(upit);
        List<AvionLeti> podaci = new ArrayList<>();
        for (int i = 0; i < brojPodataka; i++) {
            podaci.add(avioni.get(i));
        }
        mdb.zapisiuDnevnik("SOAP - posljednjih n podataka o avionima za " + icao, "Poziv web servisa", this.getClass().getSimpleName(), korisnik);
        return podaci;
    }

    /**
     * Web service operation
     *
     * @param korisnik
     * @param lozinka
     * @param icao
     * @param odDatuma
     * @param doDatuma
     * @return
     */
    @WebMethod(operationName = "dajAvionesAerodroma")
    public List<AvionLeti> dajAvionesAerodroma(@WebParam(name = "korisnik") String korisnik, @WebParam(name = "lozinka") String lozinka, @WebParam(name = "icao") String icao, @WebParam(name = "odVremena") String odDatuma, @WebParam(name = "doVremena") String doDatuma) {
        // TODO promijeniti parametre iz String u Timestamp
        if (!autentificiraj(korisnik, lozinka)) {
            return null;
        }
        int pocetakIntervala = timeStampToEpoch(Timestamp.valueOf(odDatuma));
        int krajIntervala = timeStampToEpoch(Timestamp.valueOf(doDatuma));
        String upit = "SELECT * FROM airplanes WHERE estdepartureairport = '" + icao + "' AND firstseen > " + pocetakIntervala + " AND lastseen < " + krajIntervala + " ORDER BY firstseen ASC";
        mdb.zapisiuDnevnik("SOAP - avioni s aerodroma " + icao, "Poziv web servisa", this.getClass().getSimpleName(), korisnik);
        return mdb.dohvatiAvione(upit);
    }

    /**
     * Web service operation
     *
     * @param korisnik
     * @param lozinka
     * @param icao24
     * @param odDatuma
     * @param doDatuma
     * @return
     */
    @WebMethod(operationName = "dajAvionKrozAerodrome")
    public List<AvionLeti> dajAvionKrozAerodrome(@WebParam(name = "korisnik") String korisnik, @WebParam(name = "lozinka") String lozinka, @WebParam(name = "icao24") String icao24, @WebParam(name = "odDatuma") String odDatuma, @WebParam(name = "doDatuma") String doDatuma) {
        if (!autentificiraj(korisnik, lozinka)) {
            return null;
        }
        int pocetakIntervala = timeStampToEpoch(Timestamp.valueOf(odDatuma));
        int krajIntervala = timeStampToEpoch(Timestamp.valueOf(doDatuma));
        System.out.println("pocetak: " + pocetakIntervala + " kraj: " + krajIntervala);
        String upit = "SELECT * FROM airplanes WHERE icao24 = '" + icao24 + "' AND firstseen > " + pocetakIntervala + " AND lastseen < " + krajIntervala + " ORDER BY firstseen ASC";
        mdb.zapisiuDnevnik("SOAP - avion " + icao24 + " kroz aerodrome", "Poziv web servisa", this.getClass().getSimpleName(), korisnik);
        return mdb.dohvatiAvione(upit);
    }

    /**
     * Web service operation
     *
     * @param korisnik
     * @param lozinka
     * @param icao24
     * @param odDatuma
     * @param doDatuma
     * @return
     */
    @WebMethod(operationName = "dajProlazakKrozAerodrome")
    public List<String> dajProlazakKrozAerodrome(@WebParam(name = "korisnik") String korisnik, @WebParam(name = "lozinka") String lozinka, @WebParam(name = "icao24") String icao24, @WebParam(name = "odDatuma") String odDatuma, @WebParam(name = "doDatuma") String doDatuma) {
        if (!autentificiraj(korisnik, lozinka)) {
            return null;
        }
        int pocetakIntervala = timeStampToEpoch(Timestamp.valueOf(odDatuma));
        int krajIntervala = timeStampToEpoch(Timestamp.valueOf(doDatuma));
        String upit = "SELECT * FROM airplanes WHERE icao24 = '" + icao24 + "' AND firstseen > " + pocetakIntervala + " AND lastseen < " + krajIntervala + " ORDER BY firstseen ASC";
        // TODO Provjeriti upit
        List<AvionLeti> avioni = mdb.dohvatiAvione(upit);
        List<String> prolasci = new ArrayList<>();
        for (AvionLeti al : avioni) {
            prolasci.add(al.getEstArrivalAirport());
        }
        mdb.zapisiuDnevnik("SOAP - prolazak aviona " + icao24 + " kroz aerodrome", "Poziv web servisa", this.getClass().getSimpleName(), korisnik);
        return prolasci;
    }

    /**
     * Web service operation
     *
     * @param korisnik
     * @param lozinka
     * @param icao
     * @return
     */
    @WebMethod(operationName = "dajMeteoPodatke")
    public MeteoPodaci dajMeteoPodatke(@WebParam(name = "korisnik") String korisnik, @WebParam(name = "lozinka") String lozinka, @WebParam(name = "icao") String icao) {
        if (!autentificiraj(korisnik, lozinka)) {
            return null;
        }
        Konfiguracija konfiguracija = SlusacAplikacije.getKonfiguracija();
        String apikey = konfiguracija.dajPostavku("OpenWeatherMap.apikey");
        MeteoPodaci meteoPodaci = null;
        Aerodrom aerodrom;
        String upit = "SELECT * FROM myairports WHERE ident = '" + icao + "'";
        List<Aerodrom> aerodromi = mdb.dohvatiAerodrome(upit);
        if (!aerodromi.isEmpty()) {
            aerodrom = aerodromi.get(0);
            OWMKlijent owmk = new OWMKlijent(apikey);
            meteoPodaci = owmk.getRealTimeWeather(aerodrom.getLokacija().getLatitude(), aerodrom.getLokacija().getLongitude());
        }
        mdb.zapisiuDnevnik("SOAP - meteopodaci za aerodrom " + icao, "Poziv web servisa", this.getClass().getSimpleName(), korisnik);
        return meteoPodaci;
    }

    /**
     * Web service operation
     *
     * @param korime
     * @param loz
     * @param korisnik
     * @return
     */
    @WebMethod(operationName = "dodajKorisnika")
    public boolean dodajKorisnika(@WebParam(name = "korime") String korime, @WebParam(name = "loz") String loz, @WebParam(name = "korisnik") Korisnik korisnik) {
        if (!autentificiraj(korime, loz)) {
            return false;
        }
        String korIme = korisnik.getKorIme();
        String lozinka = korisnik.getLozinka();
        String prezime = korisnik.getPrezime();
        String ime = korisnik.getIme();
        String email = korisnik.getEmail();
        String upit = "INSERT INTO korisnici VALUES(DEFAULT, '" + korIme + "', '" + lozinka + "', '" + prezime + "', '" + ime + "', '" + email + "')";
        mdb.zapisiuDnevnik("SOAP - dodaj korisnika " + korIme, "Poziv web servisa", this.getClass().getSimpleName(), korime);
        return mdb.insert(upit);
    }

    /**
     * Web service operation
     *
     * @param korime
     * @param loz
     * @param korisnik
     * @return
     */
    @WebMethod(operationName = "azurirajKorisnika")
    public boolean azurirajKorisnika(@WebParam(name = "korime") String korime, @WebParam(name = "loz") String loz, @WebParam(name = "korisnik") Korisnik korisnik) {
        if (!autentificiraj(korime, loz)) {
            return false;
        }
        String korIme = korisnik.getKorIme();
        String lozinka = korisnik.getLozinka();
        String prezime = korisnik.getPrezime();
        String ime = korisnik.getIme();
        String email = korisnik.getEmail();
        String upit = "UPDATE korisnici SET lozinka = '" + lozinka + "', prezime = '" + prezime + "', ime = '" + ime + "', email = '" + email + "' WHERE korisnicko_ime = '" + korIme + "'";
        mdb.zapisiuDnevnik("SOAP - azuriranje korisnika " + korIme, "Poziv web servisa", this.getClass().getSimpleName(), korime);
        return mdb.update(upit);
    }

    /**
     * Web service operation
     *
     * @param korisnik
     * @param lozinka
     * @return
     */
    @WebMethod(operationName = "dajPodatkeoKorisnicima")
    public List<Korisnik> dajPodatkeoKorisnicima(@WebParam(name = "korisnik") String korisnik, @WebParam(name = "lozinka") String lozinka) {
        if (!autentificiraj(korisnik, lozinka)) {
            return null;
        }
        mdb.zapisiuDnevnik("SOAP - dohvacanje podataka o korisnicima", "Poziv web servisa", this.getClass().getSimpleName(), korisnik);
        mdb.setPreuzmiSve(true);
        return mdb.dohvatiKorisnike();
    }

    /**
     * Web service operation
     *
     * @param korisnik
     * @param lozinka
     * @param prviAerodrom
     * @param drugiAerodrom
     * @return
     */
    @WebMethod(operationName = "dajUdaljenostIzmeduAerodroma")
    public double dajUdaljenostIzmeduAerodroma(@WebParam(name = "korisnik") String korisnik, @WebParam(name = "lozinka") String lozinka, @WebParam(name = "prviAerodrom") String prviAerodrom, @WebParam(name = "drugiAerodrom") String drugiAerodrom) {
        if (!autentificiraj(korisnik, lozinka)) {
            return 0.0;
        }
        double lat1 = 0;
        double lat2 = 0;
        double lon1 = 0;
        double lon2 = 0;
        String upit = "SELECT * FROM myairports WHERE ident = '" + prviAerodrom + "' OR ident = '" + drugiAerodrom + "'";
        List<Aerodrom> aerodromi = mdb.dohvatiAerodrome(upit);
        Aerodrom prviArdrm, drugiArdrm;
        if (!aerodromi.isEmpty() && aerodromi.size() == 2) {
            prviArdrm = aerodromi.get(0);
            drugiArdrm = aerodromi.get(1);
            lat1 = Double.parseDouble(prviArdrm.getLokacija().getLatitude());
            lon1 = Double.parseDouble(prviArdrm.getLokacija().getLongitude());
            lat2 = Double.parseDouble(drugiArdrm.getLokacija().getLatitude());
            lon2 = Double.parseDouble(drugiArdrm.getLokacija().getLongitude());
        }
        mdb.zapisiuDnevnik("SOAP - udaljenost izmedu aerodroma " + prviAerodrom + " i " + drugiAerodrom, "Poziv web servisa", this.getClass().getSimpleName(), korisnik);
        return distance(lat1, lat2, lon1, lon2);
    }

    private static double distance(double lat1, double lat2, double lon1, double lon2) {

        final double R = 6371;

        double dLat = Math.toRadians(lat2 - lat1);
        double dLon = Math.toRadians(lon2 - lon1);
        double a = Math.pow(Math.sin(dLat/2.0),2) + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2)) * Math.pow(Math.sin(dLon/2.0), 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1.0 - a));
        double distance = (double) (R * c);
        return distance;
    }

    /**
     * Web service operation
     * @param icao
     * @param minUdaljenost
     * @param maxUdaljenost
     * @return 
     */
    @WebMethod(operationName = "dajAerodromeUnutarGranice")
    public List<Aerodrom> dajAerodromeUnutarGranice(@WebParam(name="icao") String icao, @WebParam(name = "minUdaljenost") double minUdaljenost, @WebParam(name = "maxUdaljenost") double maxUdaljenost) {
        List<Aerodrom> rezultat = new ArrayList<>();
        double lat1 = 0;
        double lon1 = 0;
        double lat2 = 0;
        double lon2 = 0;
        Aerodrom aerodrom;
        String upit = "SELECT * FROM myairports WHERE ident = '" + icao + "'";
        List<Aerodrom> aerodromi = mdb.dohvatiAerodrome(upit);
        if (!aerodromi.isEmpty()) {
            aerodrom = aerodromi.get(0);
            lat1 = Double.parseDouble(aerodrom.getLokacija().getLatitude());
            lon1 = Double.parseDouble(aerodrom.getLokacija().getLongitude());
        }
        upit = "SELECT * FROM myairports";
        aerodromi = mdb.dohvatiAerodrome(upit);
        for (Aerodrom a : aerodromi) {
            if (a.getIcao().equals(icao)) {
                break;
            }
            lat2 = Double.parseDouble(a.getLokacija().getLatitude());
            lon2 = Double.parseDouble(a.getLokacija().getLongitude());
            if (distance(lat1, lat2, lon1, lon2) >= minUdaljenost && distance(lat1, lat2, lon1, lon2) <= maxUdaljenost) {
                rezultat.add(a);
            }
        }
        return rezultat;
    }

}
