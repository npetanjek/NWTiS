/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.foi.nwtis.npetanjek.web.zrna;

import javax.inject.Named;
import javax.enterprise.context.SessionScoped;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import javax.ejb.EJB;
import javax.faces.context.FacesContext;
import javax.servlet.http.HttpSession;
import org.foi.nwtis.npetanjek.ejb.eb.Dnevnik;
import org.foi.nwtis.npetanjek.ejb.sb.DnevnikFacade;
import org.foi.nwtis.npetanjek.konfiguracije.Konfiguracija;
import org.foi.nwtis.npetanjek.web.slusaci.SlusacAplikacije;

/**
 *
 * @author Nikola
 */
@Named(value = "pregledDnevnikaRada")
@SessionScoped
public class PregledDnevnikaRada implements Serializable {

    @EJB
    private DnevnikFacade dnevnikFacade;

    private String korisnik;
    private List<Dnevnik> dnevnik;
    private String poruka;
    private final int brojLinija;
    Konfiguracija konfiguracija;

    /**
     * Creates a new instance of PregledDnevnikaRada
     */
    public PregledDnevnikaRada() {
        HttpSession sesija = (HttpSession) FacesContext.getCurrentInstance().getExternalContext().getSession(false);
        if (sesija != null && sesija.getAttribute("korisnik") != null) {
            korisnik = sesija.getAttribute("korisnik").toString();
        }
        konfiguracija = SlusacAplikacije.getKonfiguracija();
        brojLinija = Integer.parseInt(konfiguracija.dajPostavku("pregledDnevnikaRada.brojLinija"));
    }

    public void brisiSveZapise() {
        List<Dnevnik> zapisi = dnevnikFacade.findAll();
        for (Dnevnik d : zapisi) {
            dnevnikFacade.remove(d);
        }
    }

    public String getKorisnik() {
        return korisnik;
    }

    public List<Dnevnik> getDnevnik() {
        return dnevnikFacade.findAll();
    }

    public int getBrojLinija() {
        return brojLinija;
    }

    public String getPoruka() {
        return poruka;
    }

}
