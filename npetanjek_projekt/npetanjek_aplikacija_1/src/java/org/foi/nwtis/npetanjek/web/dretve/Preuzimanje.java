/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.foi.nwtis.npetanjek.web.dretve;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.servlet.ServletContext;
import org.foi.nwtis.npetanjek.db.MyDataBase;
import org.foi.nwtis.npetanjek.konfiguracije.Konfiguracija;
import org.foi.nwtis.npetanjek.web.slusaci.SlusacAplikacije;
import org.foi.nwtis.npetanjek.ws.klijenti.Aerodrom;
import org.foi.nwtis.rest.klijenti.OSKlijent;
import org.foi.nwtis.rest.podaci.AvionLeti;

/**
 *
 * @author Nikola
 */
public class Preuzimanje extends Thread {

    volatile boolean kraj = false;

    Konfiguracija konfiguracija;
    String os_korisnik;
    String os_lozinka;
    int inicijalniPocetakIntervala;
    int pocetakIntervala;
    int krajIntervala;
    int trajanjeIntervala;
    int ciklusDretve;
    int redniBrojCiklusa;
    String datoteka;
    MyDataBase mdb;
    volatile boolean pasivno = false;

    public void pauziraj() {
        synchronized (this) {
            try {
                System.out.println("Preuzimanje pauzirano");
                wait();
            } catch (InterruptedException ex) {
                Logger.getLogger(Preuzimanje.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }

    public void nastavi() {
        synchronized (this) {
            System.out.println("Preuzimanje se nastavlja");
            notify();
        }
    }

    @Override
    public void interrupt() {
        System.out.println("Prekidam preuzimanje podataka za aerodrome...");
        kraj = true;
        super.interrupt();
    }

    // TODO sortirati avione
    @Override
    public void run() {
        while (!kraj) {
            if (pasivno) {
                pauziraj();
            }
            try {
                long pocetakDretve = System.currentTimeMillis();
                // ispis vremena pocetka ciklusa
                SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss.SSS");
                Date resultDate = new Date(pocetakDretve);
                System.out.println(sdf.format(resultDate));
                System.out.println("Pocetak intervala: " + new SimpleDateFormat("MM/dd/yyyy HH:mm:ss").format(new Date((long) pocetakIntervala * 1000)));
                System.out.println("Kraj intervala: " + new SimpleDateFormat("MM/dd/yyyy HH:mm:ss").format(new Date((long) krajIntervala * 1000)));

                OSKlijent osk = new OSKlijent(os_korisnik, os_lozinka);
                List<Aerodrom> aerodromi = dohvatiIzabranSkupAerodroma();
                for (Aerodrom aerodrom : aerodromi) {
                    List<AvionLeti> departures = osk.getDepartures(aerodrom.getIcao(), pocetakIntervala, krajIntervala);
                    for (AvionLeti departure : departures) {
                        String icao24 = departure.getIcao24();
                        int firstSeen = departure.getFirstSeen();
                        String estDepartureAirport = departure.getEstDepartureAirport();
                        int lastSeen = departure.getLastSeen();
                        String estArrivalAirport = departure.getEstArrivalAirport();
                        String callSign = departure.getCallsign();
                        int estDepartureAirportHorDist = departure.getEstDepartureAirportHorizDistance();
                        int estDepartureAirportVerDist = departure.getEstDepartureAirportVertDistance();
                        int estArrivalAirportHorDist = departure.getEstArrivalAirportHorizDistance();
                        int estArrivalAirportVerDist = departure.getEstArrivalAirportVertDistance();
                        int departureAirportCandidatesCount = departure.getDepartureAirportCandidatesCount();
                        int arrivalAirportCandidatesCount = departure.getDepartureAirportCandidatesCount();
                        if (provjeriNullVrijednosti(departure)) {
                            break;
                        }
                        Timestamp stored = new Timestamp(System.currentTimeMillis());
                        String dohvatiSpremljeneAvione = "SELECT * FROM AIRPLANES";
                        List<AvionLeti> spremljeniAvioni = mdb.dohvatiAvione(dohvatiSpremljeneAvione);
                        boolean postoji = false;
                        if (!spremljeniAvioni.isEmpty()) {
                            for (AvionLeti avionLeti : spremljeniAvioni) {
                                if (avionLeti.getIcao24().equals(departure.getIcao24()) && avionLeti.getLastSeen() == departure.getLastSeen()
                                        && avionLeti.getEstArrivalAirport().equals(departure.getEstArrivalAirport())) {
                                    postoji = true;
                                    break;
                                }
                            }
                        }
                        if (!postoji) {
                            String spremi = "INSERT INTO AIRPLANES VALUES(DEFAULT, '" + icao24 + "', " + firstSeen + ", '" + estDepartureAirport + "', "
                                    + "" + lastSeen + ", '" + estArrivalAirport + "', '" + callSign + "', " + estDepartureAirportHorDist + ", " + estDepartureAirportVerDist + ", "
                                    + "" + estArrivalAirportHorDist + ", " + estArrivalAirportVerDist + ", " + departureAirportCandidatesCount + ", " + arrivalAirportCandidatesCount + ", "
                                    + "'" + stored + "')";
                            mdb.insert(spremi);
                        } else {
                            String azuriraj = "UPDATE AIRPLANES SET lastSeen = " + lastSeen + " WHERE icao24 = '" + icao24 + "'";
                            mdb.update(azuriraj);
                        }
                    }
                }
                redniBrojCiklusa++;
                spremiRadDretve();
                pocetakIntervala = krajIntervala;
                if (pocetakIntervala >= (new Date().getTime() / 1000)) {
                    pocetakIntervala = postaviInicijalnuVrijednost(inicijalniPocetakIntervala);
                }
                krajIntervala = pocetakIntervala + trajanjeIntervala;
                // korekcija ciklusa dretve
                long trajanje = System.currentTimeMillis() - pocetakDretve;
                System.out.println("ciklus dretve=" + (ciklusDretve * 1000 * 60));
                System.out.println("spavanje=" + (ciklusDretve * 1000 * 60 - trajanje));
                Thread.sleep(ciklusDretve * 1000 * 60 - trajanje);
            } catch (InterruptedException ex) {
                Logger.getLogger(Preuzimanje.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }

    private String dohvatiPutanjuDatoteke(String datoteka) {
        ServletContext sc = SlusacAplikacije.getSc();
        String putanja = sc.getRealPath("/WEB-INF");
        String[] polje = putanja.split("npetanjek_aplikacija_1");
        polje[0] += "npetanjek_aplikacija_1\\web\\WEB-INF\\" + datoteka;
        return polje[0];
    }

    private void ucitajDatoteku() {
        ServletContext sc = SlusacAplikacije.getSc();
        String nazivDatoteke = sc.getInitParameter("datotekaRadaDretve");
        datoteka = dohvatiPutanjuDatoteke(nazivDatoteke);
        File f = new File(datoteka);
        if (!f.exists()) {
            try {
                f.createNewFile();
                redniBrojCiklusa = 0;
                pocetakIntervala = postaviInicijalnuVrijednost(inicijalniPocetakIntervala);
            } catch (IOException ex) {
                Logger.getLogger(Preuzimanje.class.getName()).log(Level.SEVERE, null, ex);
            }
        } else {
            try {
                if (f.length() == 0) {
                    redniBrojCiklusa = 0;
                    pocetakIntervala = postaviInicijalnuVrijednost(inicijalniPocetakIntervala);
                } else {
                    BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(datoteka), Charset.forName("UTF-8")));
                    StringBuilder sb = new StringBuilder();
                    String linija;
                    while ((linija = br.readLine()) != null) {
                        sb.append(linija);
                    }
                    JsonParser parser = new JsonParser();
                    JsonElement element = parser.parse(sb.toString());
                    JsonObject jsonObject = element.getAsJsonObject();
                    redniBrojCiklusa = jsonObject.get("redniBrojCiklusa").getAsInt();
                    pocetakIntervala = jsonObject.get("pocetakIntervala").getAsInt();
                }
            } catch (FileNotFoundException ex) {
                Logger.getLogger(Preuzimanje.class.getName()).log(Level.SEVERE, null, ex);
            } catch (IOException ex) {
                Logger.getLogger(Preuzimanje.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }

    private void spremiRadDretve() {
        JsonObject jsonObject = new JsonObject();
        Gson gson = new Gson();
        try {
            FileOutputStream out = new FileOutputStream(datoteka);
            jsonObject.addProperty("pocetakIntervala", krajIntervala);
            jsonObject.addProperty("redniBrojCiklusa", redniBrojCiklusa);
            String zapis = gson.toJson(jsonObject);
            out.write(zapis.getBytes());
        } catch (FileNotFoundException ex) {
            Logger.getLogger(Preuzimanje.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(Preuzimanje.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    private boolean provjeriNullVrijednosti(AvionLeti departure) {
        int firstSeen = departure.getFirstSeen();
        String estDepartureAirport = departure.getEstDepartureAirport();
        int lastSeen = departure.getLastSeen();
        String estArrivalAirport = departure.getEstArrivalAirport();
        String callSign = departure.getCallsign();
        if (firstSeen == 0 || estDepartureAirport == null || lastSeen == 0 || estArrivalAirport == null || callSign == null) {
            return true;
        }
        return false;
    }

    private List<Aerodrom> dohvatiIzabranSkupAerodroma() {
        String upit = "SELECT * FROM myairports";
        List<Aerodrom> aerodromi = mdb.dohvatiAerodrome(upit);
        return aerodromi;
    }

    @Override
    public synchronized void start() {

        konfiguracija = SlusacAplikacije.getKonfiguracija();
        os_korisnik = konfiguracija.dajPostavku("OpenSkyNetwork.korisnik");
        os_lozinka = konfiguracija.dajPostavku("OpenSkyNetwork.lozinka");
        inicijalniPocetakIntervala = Integer.parseInt(konfiguracija.dajPostavku("preuzimanje.pocetak"));
        ucitajDatoteku();
        trajanjeIntervala = Integer.parseInt(konfiguracija.dajPostavku("preuzimanje.trajanje")) * 60 * 60;
        krajIntervala = pocetakIntervala + trajanjeIntervala;
        ciklusDretve = Integer.parseInt(konfiguracija.dajPostavku("preuzimanje.ciklus"));
        mdb = MyDataBase.getInstance();
        super.start();
    }

    private int postaviInicijalnuVrijednost(int inicijalniPocetakIntervala) {
        pocetakIntervala = (int) (new Date().getTime() / 1000) - (inicijalniPocetakIntervala * 60 * 60);
        return pocetakIntervala;
    }

}
