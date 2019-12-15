/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.foi.nwtis.npetanjek.web.zrna;

import java.io.IOException;
import javax.inject.Named;
import javax.enterprise.context.SessionScoped;
import java.io.Serializable;
import java.io.UnsupportedEncodingException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.ejb.EJB;
import javax.faces.context.FacesContext;
import javax.servlet.http.HttpSession;
import org.foi.nwtis.npetanjek.ejb.podaci.Korisnik;
import org.foi.nwtis.npetanjek.ejb.sb.AutentikacijaSB;
import org.foi.nwtis.npetanjek.ejb.sb.SingletonSB;
import org.foi.nwtis.npetanjek.mqtt.slusaci.SlusacMqtt;

/**
 *
 * @author Nikola
 */
@Named(value = "prijavljenZrno")
@SessionScoped
public class PrijavljenZrno implements Serializable {

    @EJB
    private SingletonSB singletonSB;

    @EJB
    private AutentikacijaSB autentikacijaSB;

    private String korisnik;
    String lozinka;

    /**
     * Creates a new instance of PrijavljenZrno
     */
    public PrijavljenZrno() {
    }

    public void autentificiraj() {
        HttpSession sesija = (HttpSession) FacesContext.getCurrentInstance().getExternalContext().getSession(false);
        if (sesija != null && sesija.getAttribute("korisnik") != null) {
            korisnik = sesija.getAttribute("korisnik").toString();
        }
    }

    public String getKorisnik() {
        return korisnik;
    }

    public void odjava() {
        HttpSession sesija = (HttpSession) FacesContext.getCurrentInstance().getExternalContext().getSession(false);
        if (sesija != null) {
            //autentikacijaSB.odjava();
            SlusacMqtt slusacMqtt = (SlusacMqtt) sesija.getAttribute("slusac");
            if (slusacMqtt != null)
                slusacMqtt.interrupt();
            Korisnik k = null;
            try {
                k = autentikacijaSB.dajKorisnika(korisnik);
            } catch (UnsupportedEncodingException ex) {
                Logger.getLogger(PrijavljenZrno.class.getName()).log(Level.SEVERE, null, ex);
            }
            if (singletonSB.brisiKorisnika(k)) {
                System.out.println("Odjavljen korisnik " + korisnik);
            }
            sesija.invalidate();
            try {
                FacesContext.getCurrentInstance().getExternalContext().redirect("prijava.xhtml");
            } catch (IOException ex) {
                Logger.getLogger(PrijavljenZrno.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }

}
