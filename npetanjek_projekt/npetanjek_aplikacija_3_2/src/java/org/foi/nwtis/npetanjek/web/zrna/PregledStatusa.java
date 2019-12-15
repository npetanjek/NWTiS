/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.foi.nwtis.npetanjek.web.zrna;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import javax.inject.Named;
import javax.enterprise.context.SessionScoped;
import java.io.Serializable;
import java.net.Socket;
import java.nio.charset.Charset;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.faces.context.FacesContext;
import javax.servlet.http.HttpSession;
import org.foi.nwtis.npetanjek.konfiguracije.Konfiguracija;
import org.foi.nwtis.npetanjek.web.slusaci.Slusac;

/**
 *
 * @author Nikola
 */
@Named(value = "pregledStatusa")
@SessionScoped
public class PregledStatusa implements Serializable {

    private final String korisnik;
    String lozinka;
    String komanda;
    String host;
    int port;
    Socket socket;
    private String poruka;
    Konfiguracija konfiguracija;

    /**
     * Creates a new instance of PregledStatusa
     */
    public PregledStatusa() {
        HttpSession sesija = (HttpSession) FacesContext.getCurrentInstance().getExternalContext().getSession(false);
        korisnik = sesija.getAttribute("korisnik").toString();
        lozinka = sesija.getAttribute("lozinka").toString();
        konfiguracija = Slusac.getKonfiguracija();
        host = konfiguracija.dajPostavku("server.host");
        port = Integer.parseInt(konfiguracija.dajPostavku("server.port"));
    }

    public String getKorisnik() {
        return korisnik;
    }

    private void postaviKomandu() {
        komanda = "KORISNIK " + korisnik + "; LOZINKA " + lozinka + ";";
    }

    public void provjeriStanjeGrupe() {
        postaviKomandu();
        komanda += " GRUPA STANJE;";
        saljiKomandu(komanda);
        poruka = prevediOdgovor();
    }

    public void registrirajGrupu() {
        postaviKomandu();
        komanda += " GRUPA DODAJ;";
        saljiKomandu(komanda);
        poruka = prevediOdgovor();
    }
    
    public void deregistrirajGrupu() {
        postaviKomandu();
        komanda += " GRUPA PREKID;";
        saljiKomandu(komanda);
        poruka = prevediOdgovor();
    }
    
    public void aktivirajGrupu() {
        postaviKomandu();
        komanda += " GRUPA KRENI;";
        saljiKomandu(komanda);
        poruka = prevediOdgovor();
    }
    
    public void blokirajGrupu() {
        postaviKomandu();
        komanda += " GRUPA PAUZA;";
        saljiKomandu(komanda);
        poruka = prevediOdgovor();
    }
    
    public void provjeriStanjePosluzitelja() {
        postaviKomandu();
        komanda += " STANJE;";
        saljiKomandu(komanda);
        poruka = prevediOdgovor();
    }
    
    public void dozvoliSamoKomandePosluzitelja() {
        postaviKomandu();
        komanda += " PAUZA;";
        saljiKomandu(komanda);
        poruka = prevediOdgovor();
    }
    
    public void dozvoliSveKomande() {
        postaviKomandu();
        komanda += " KRENI;";
        saljiKomandu(komanda);
        poruka = prevediOdgovor();
    }
    
    public void pauzirajPreuzimanjeAviona() {
        postaviKomandu();
        komanda += " PASIVNO;";
        saljiKomandu(komanda);
        poruka = prevediOdgovor();
    }
    
    public void nastaviPreuzimanjeAviona() {
        postaviKomandu();
        komanda += " AKTIVNO;";
        saljiKomandu(komanda);
        poruka = prevediOdgovor();
    }
    
    public void zaustaviPreuzimanjeAviona() {
        postaviKomandu();
        komanda += " STANI;";
        saljiKomandu(komanda);
        poruka = prevediOdgovor();
    }

    private void saljiKomandu(String komanda) {
        BufferedWriter bw;
        try {
            socket = new Socket(host, port);
            bw = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream(), Charset.forName("UTF-8")));
            bw.write(komanda);
            bw.flush();
            socket.getOutputStream().flush();
            socket.shutdownOutput();
        } catch (IOException ex) {
            Logger.getLogger(PregledStatusa.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    private String citaj() {
        StringBuilder stringBuilder = new StringBuilder();
        try {
            BufferedReader br = new BufferedReader(new InputStreamReader(socket.getInputStream(), Charset.forName("UTF-8")));
            int znak;
            while ((znak = br.read()) != -1) {
                stringBuilder.append((char) znak);
            }
            socket.shutdownInput();
            socket.close();
        } catch (IOException ex) {
            Logger.getLogger(PregledStatusa.class.getName()).log(Level.SEVERE, null, ex);
        }
        return stringBuilder.toString();
    }
    
    private String prevediOdgovor() {
        String odgovor = citaj();
        switch (odgovor) {
            case "OK 10;":
                return "OK 10;";
            case "OK 11;":
                return "OK 11; Poslužitelj preuzima sve komande i preuzima podatke za aerodrome";
            case "OK 12;":
                return "OK 12; Poslužitelj preuzima sve komande i ne preuzima podatke za aerodrome";
            case "OK 13;":
                return "OK 13; Poslužitelj preuzima samo poslužiteljske komande i preuzima podatke za aerodrome";
            case "OK 14;":
                return "Poslužitelj preuzima samo poslužiteljske komande i ne preuzima podatke za aerodrome";
            case "OK 20;":
                return "OK 20;";
            case "OK 21;":
                return "OK 21; Grupa je aktivna";
            case "OK 22;":
                return "OK 22; Grupa je blokirana";
            case "ERR 12;":
                return "ERR 12; Poslužitelj je već u pauzi";
            case "ERR 13;":
                return "ERR 13; Poslužitelj nije u pauzi";
            case "ERR 14;":
                return "ERR 14; Poslužitelj je već u pasivnom radu";
            case "ERR 15;":
                return "ERR 15; Poslužitelj je već u aktivnom radu";
            case "ERR 16;":
                return "ERR 16; Poslužitelj je već u postupku prekida";
            case "ERR 20;":
                return "ERR 20; Grupa je već registrirana";
            case "ERR 21;":
                return "ERR 21; Grupa nije registrirana (ne postoji)";
            case "ERR 22;":
                return "ERR 22; Grupa je već aktivna/blokirana";
            case "ERR 23;":
                return "ERR 23; Grupa nije aktivna";
            default:
                return odgovor;
        }
    }

    public String getPoruka() {
        return poruka;
    }

}
