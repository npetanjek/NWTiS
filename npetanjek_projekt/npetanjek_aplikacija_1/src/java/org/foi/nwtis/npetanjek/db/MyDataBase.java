/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.foi.nwtis.npetanjek.db;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.foi.nwtis.npetanjek.konfiguracije.Konfiguracija;
import org.foi.nwtis.npetanjek.konfiguracije.bp.BP_Konfiguracija;
import org.foi.nwtis.npetanjek.web.podaci.DnevnikRada;
import org.foi.nwtis.npetanjek.web.podaci.Korisnik;
import org.foi.nwtis.npetanjek.web.slusaci.SlusacAplikacije;
import org.foi.nwtis.npetanjek.ws.klijenti.Aerodrom;
import org.foi.nwtis.npetanjek.ws.klijenti.Lokacija;
import org.foi.nwtis.rest.podaci.AvionLeti;

/**
 *
 * @author Nikola
 */
public class MyDataBase {

    BP_Konfiguracija bpk;
    Konfiguracija konfiguracija;
    String url;
    String korisnik;
    String lozinka;
    String driver;
    Statement s;

    int pocetak = 0;
    private boolean preuzmiSve;
    int brojLinija;

    private MyDataBase() {
        konfiguracija = SlusacAplikacije.getKonfiguracija();
        bpk = SlusacAplikacije.getBpk();
        url = bpk.getServerDatabase() + bpk.getUserDatabase();
        korisnik = bpk.getUserUsername();
        lozinka = bpk.getUserPassword();
        driver = bpk.getDriverDatabase();
        s = stvoriStatement();
        preuzmiSve = true;
        brojLinija = Integer.parseInt(konfiguracija.dajPostavku("pregledKorisnika.brojLinija"));
    }

    public static MyDataBase getInstance() {
        return MyDataBaseHolder.INSTANCE;
    }

    public void setPreuzmiSve(boolean preuzmiSve) {
        this.preuzmiSve = preuzmiSve;
    }

    private static class MyDataBaseHolder {

        private static final MyDataBase INSTANCE = new MyDataBase();
    }

    public void sljedecaStranica() {
        pocetak += brojLinija;
    }

    public void prethodnaStranica() {
        pocetak -= brojLinija;
    }

    public void resetiraj() {
        pocetak = 0;
    }

    public int dohvatiBrojZapisa(String tablica, String filter) {
        String upit = "SELECT COUNT(*) FROM " + tablica;
        int brojZapisa = 0;
        if (filter != null) {
            upit += " WHERE " + filter;
        }
        ResultSet rs;
        try {
            rs = s.executeQuery(upit);
            if (rs.next()) {
                brojZapisa = rs.getInt(1);
            }
        } catch (SQLException ex) {
            Logger.getLogger(MyDataBase.class.getName()).log(Level.SEVERE, null, ex);
        }
        return brojZapisa;
    }

    public List<Aerodrom> dohvatiAerodrome(String upit) {
        List<Aerodrom> aerodromi = new ArrayList<>();
        ResultSet rs;
        try {
            rs = s.executeQuery(upit);
            if (rs != null) {
                while (rs.next()) {
                    String crdnts[] = rs.getString("coordinates").split(",");
                    String ident = rs.getString("ident");
                    String name = rs.getString("name");
                    String iso_country = rs.getString("iso_country");
                    String lat = crdnts[0].trim();
                    String lon = crdnts[1].trim();
                    Lokacija lokacija = new Lokacija();
                    lokacija.setLatitude(lat);
                    lokacija.setLongitude(lon);
                    Aerodrom a = new Aerodrom();
                    a.setIcao(ident);
                    a.setNaziv(name);
                    a.setDrzava(iso_country);
                    a.setLokacija(lokacija);
                    aerodromi.add(a);
                }
            }
        } catch (SQLException ex) {
            Logger.getLogger(MyDataBase.class.getName()).log(Level.SEVERE, null, ex);
        }
        if (aerodromi.isEmpty()) {
            System.out.println("Nije pronađen ni jedan aerodrom");
        }
        return aerodromi;
    }

    public List<Korisnik> dohvatiKorisnike() {
        String upit = "SELECT * FROM korisnici";
        List<Korisnik> korisnici = new ArrayList<>();
        ResultSet rs;
        try {
            if (!preuzmiSve) {
                upit += " LIMIT " + pocetak + ", " + brojLinija;
            }
            rs = s.executeQuery(upit);

            if (rs != null) {
                while (rs.next()) {
                    Korisnik k = new Korisnik();
                    k.setKorIme(rs.getString("korisnicko_ime"));
                    k.setLozinka(rs.getString("lozinka"));
                    k.setIme(rs.getString("ime"));
                    k.setPrezime(rs.getString("prezime"));
                    k.setEmail(rs.getString("email"));
                    korisnici.add(k);
                }

            }
        } catch (SQLException ex) {
            Logger.getLogger(MyDataBase.class.getName()).log(Level.SEVERE, null, ex);
        }
        if (korisnici.isEmpty()) {
            System.out.println("Nije pronađen ni jedan korisnik");
        }
        return korisnici;
    }

    public List<DnevnikRada> dohvatiDnevnikRada(String filter) {
        String upit = "SELECT * FROM dnevnik";
        List<DnevnikRada> dnevnik = new ArrayList<>();
        ResultSet rs;
        try {
            if (filter != null) {
                upit += " WHERE " + filter;
            }
            if (!preuzmiSve) {
                upit += " LIMIT " + pocetak + ", " + brojLinija;
            }
            rs = s.executeQuery(upit);
            if (rs != null) {
                while (rs.next()) {
                    DnevnikRada d = new DnevnikRada();
                    d.setId(rs.getInt(1));
                    d.setVrijeme(rs.getTimestamp(2));
                    d.setZahtjev(rs.getString(3));
                    d.setVrstaZahtjeva(rs.getString(4));
                    d.setDioAplikacije(rs.getString(5));
                    d.setKorisnik(rs.getString(6));
                    dnevnik.add(d);
                }
            }
        } catch (SQLException ex) {
            Logger.getLogger(MyDataBase.class.getName()).log(Level.SEVERE, null, ex);
        }
        return dnevnik;
    }

    public List<AvionLeti> dohvatiAvione(String upit) {
        List<AvionLeti> avioni = new ArrayList<>();
        ResultSet rs;
        try {
            rs = s.executeQuery(upit);
            if (rs != null) {
                while (rs.next()) {
                    AvionLeti al = new AvionLeti();
                    al.setIcao24(rs.getString("icao24"));
                    al.setFirstSeen(rs.getInt("firstseen"));
                    al.setEstDepartureAirport(rs.getString("estdepartureairport"));
                    al.setLastSeen(rs.getInt("lastseen"));
                    al.setEstArrivalAirport(rs.getString("estarrivalairport"));
                    al.setCallsign(rs.getString("callsign"));
                    al.setEstDepartureAirportHorizDistance(rs.getInt("EstDepartureAirportHorizDistance"));
                    al.setEstDepartureAirportVertDistance(rs.getInt("EstDepartureAirportVertDistance"));
                    al.setEstArrivalAirportHorizDistance(rs.getInt("EstArrivalAirportHorizDistance"));
                    al.setEstArrivalAirportVertDistance(rs.getInt("EstArrivalAirportVertDistance"));
                    al.setDepartureAirportCandidatesCount(rs.getInt("DepartureAirportCandidatesCount"));
                    al.setArrivalAirportCandidatesCount(rs.getInt("ArrivalAirportCandidatesCount"));
                    avioni.add(al);
                }
            }
        } catch (SQLException ex) {
            Logger.getLogger(MyDataBase.class.getName()).log(Level.SEVERE, null, ex);
        }
        if (avioni.isEmpty()) {
            //System.out.println("Nije pronađen ni jedan avion");
        }
        return avioni;
    }

    public boolean insert(String upit) {
        try {
            s.executeUpdate(upit);
            return true;
        } catch (SQLException ex) {
            Logger.getLogger(MyDataBase.class.getName()).log(Level.SEVERE, null, ex);
        }
        return false;
    }

    public boolean update(String upit) {
        return insert(upit);
    }

    public boolean find(String upit) {
        try {
            if (s.executeQuery(upit).next()) {
                return true;
            }
        } catch (SQLException ex) {
            Logger.getLogger(MyDataBase.class.getName()).log(Level.SEVERE, null, ex);
        }
        return false;
    }

    public boolean zapisiuDnevnik(String zahtjev, String vrstaZahtjeva, String dioAplikacije, String korisnik) {
        String upit = "INSERT INTO dnevnik VALUES (DEFAULT, '" + new Timestamp(System.currentTimeMillis()) + "', '" + zahtjev + "', '" + vrstaZahtjeva + "', '" + dioAplikacije + "', '" + korisnik + "')";
        try {
            s.executeUpdate(upit);
            return true;
        } catch (SQLException ex) {
            Logger.getLogger(MyDataBase.class.getName()).log(Level.SEVERE, null, ex);
        }
        return false;
    }

    private Statement stvoriStatement() {

        try {
            Class.forName(driver);
        } catch (ClassNotFoundException ex) {
            Logger.getLogger(MyDataBase.class.getName()).log(Level.SEVERE, null, ex);
        }

        Connection con;
        try {
            con = DriverManager.getConnection(url, korisnik, lozinka);
            s = con.createStatement();
        } catch (SQLException ex) {
            Logger.getLogger(MyDataBase.class.getName()).log(Level.SEVERE, null, ex);
        }

        return s;
    }
}
