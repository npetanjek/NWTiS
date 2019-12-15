/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.foi.nwtis.npetanjek.web.zrna;

import java.io.File;
import java.io.IOException;
import javax.inject.Named;
import javax.enterprise.context.SessionScoped;
import java.io.Serializable;
import java.io.UnsupportedEncodingException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.ejb.EJB;
import javax.faces.context.FacesContext;
import javax.servlet.ServletContext;
import javax.servlet.http.HttpSession;
import org.foi.nwtis.npetanjek.ejb.podaci.Korisnik;
import org.foi.nwtis.npetanjek.ejb.sb.AutentikacijaSB;
import org.foi.nwtis.npetanjek.ejb.sb.MqttPorukeFacade;
import org.foi.nwtis.npetanjek.ejb.sb.SingletonSB;
import org.foi.nwtis.npetanjek.konfiguracije.Konfiguracija;
import org.foi.nwtis.npetanjek.konfiguracije.KonfiguracijaApstraktna;
import org.foi.nwtis.npetanjek.konfiguracije.NeispravnaKonfiguracija;
import org.foi.nwtis.npetanjek.konfiguracije.NemaKonfiguracije;
import org.foi.nwtis.npetanjek.mqtt.slusaci.SlusacMqtt;

/**
 *
 * @author Nikola
 */
@Named(value = "prijavaZrno")
@SessionScoped
public class PrijavaZrno implements Serializable {

    @EJB
    private MqttPorukeFacade mqttPorukeFacade;

    @EJB
    private SingletonSB singletonSB;

    public static void setRenderPrijava(boolean aRenderPrijava) {
        renderPrijava = aRenderPrijava;
    }

    public static void setRenderRegistracija(boolean aRenderRegistracija) {
        renderRegistracija = aRenderRegistracija;
    }

    public String getPoruka() {
        return poruka;
    }

    public static void setPoruka(String aPoruka) {
        poruka = aPoruka;
    }

    @EJB
    private AutentikacijaSB autentikacijaSB;
    
    private String korisnik;
    private String lozinka;
    private static boolean renderPrijava = true;
    private boolean renderPogled = false;
    private static boolean renderRegistracija = false;
    private static String poruka;
    Konfiguracija konfiguracija;
    SlusacMqtt slusacMqtt;

    /**
     * Creates a new instance of PrijavaZrno
     */
    public PrijavaZrno() {
        poruka = "";
        try {
            preuzmiKonfiguraciju();
        } catch (NemaKonfiguracije | NeispravnaKonfiguracija ex) {
            Logger.getLogger(PrijavaZrno.class.getName()).log(Level.SEVERE, null, ex);
        }
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
    
    public void autenticiraj() throws UnsupportedEncodingException {
        if (autentikacijaSB.autenticiraj(korisnik, lozinka)) {
            HttpSession sesija = (HttpSession) FacesContext.getCurrentInstance().getExternalContext().getSession(true);
            sesija.setAttribute("korisnik", korisnik);
            sesija.setAttribute("lozinka", lozinka);
            slusacMqtt = new SlusacMqtt(konfiguracija, mqttPorukeFacade);
            slusacMqtt.start();
            sesija.setAttribute("slusac", slusacMqtt);
            Korisnik k = autentikacijaSB.dajKorisnika(korisnik);
            singletonSB.dodajKorisnika(k);
            //autentikacijaSB.slusajMqtt(konfiguracija);
            korisnik = "";
            lozinka = "";
            try {
                FacesContext.getCurrentInstance().getExternalContext().redirect("index.xhtml");
            } catch (IOException ex) {
                Logger.getLogger(PrijavaZrno.class.getName()).log(Level.SEVERE, null, ex);
            }
        } else {
            poruka = "Neispravni podaci";
        }
    }
    
    private void preuzmiKonfiguraciju() throws NemaKonfiguracije, NeispravnaKonfiguracija {
        ServletContext sc = (ServletContext) FacesContext.getCurrentInstance().getExternalContext().getContext();
        String putanja = sc.getRealPath("/WEB-INF");
        String datoteka = putanja + File.separator + sc.getInitParameter("konfiguracija");
        konfiguracija = KonfiguracijaApstraktna.preuzmiKonfiguraciju(datoteka);
    }
    
    public void natrag() {
        renderRegistracija = false;
        renderPrijava = true;
        poruka = "";
    }
    
    public void registriraj() {
        renderPrijava = false;
        renderRegistracija = true;
        poruka = "";
    }

    public boolean isRenderPrijava() {
        return renderPrijava;
    }

    public boolean isRenderPogled() {
        return renderPogled;
    }

    public boolean isRenderRegistracija() {
        return renderRegistracija;
    }
    
}
