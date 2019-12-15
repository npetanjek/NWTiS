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
import org.foi.nwtis.npetanjek.ejb.eb.MqttPoruke;
import org.foi.nwtis.npetanjek.ejb.sb.MqttPorukeFacade;
import org.foi.nwtis.npetanjek.konfiguracije.Konfiguracija;
import org.foi.nwtis.npetanjek.web.slusaci.SlusacAplikacije;

/**
 *
 * @author Nikola
 */
@Named(value = "pregledMqttPoruka")
@SessionScoped
public class PregledMqttPoruka implements Serializable {

    @EJB
    private MqttPorukeFacade mqttPorukeFacade;
    
    private String korisnik;
    String lozinka;
    private List<MqttPoruke> mqttPoruke;
    private final int brojLinija;
    private String poruka;
    Konfiguracija konfiguracija;

    /**
     * Creates a new instance of PregledMqttPoruka
     */
    public PregledMqttPoruka() {
        HttpSession sesija = (HttpSession) FacesContext.getCurrentInstance().getExternalContext().getSession(false);
        if (sesija != null && sesija.getAttribute("korisnik") != null && sesija.getAttribute("lozinka") != null) {
            korisnik = sesija.getAttribute("korisnik").toString();
            lozinka = sesija.getAttribute("lozinka").toString();
        }
        konfiguracija = SlusacAplikacije.getKonfiguracija();
        brojLinija = Integer.parseInt(konfiguracija.dajPostavku("pregledMqttPoruka.brojLinija"));
        
    }
    
    public void brisiSveMqttPorukeKorisnika() {
        List<MqttPoruke> svePoruke = mqttPorukeFacade.findAll();
        for (MqttPoruke poruka : svePoruke) {
                mqttPorukeFacade.remove(poruka);
        }
        /*for (Integer i : pronadiSvePorukeKorisnika()) {
            MqttPoruke poruka = mqttPorukeFacade.find(i);
            if (poruka != null) {
                mqttPorukeFacade.remove(poruka);
                return true;
            }
        }*/
    }
    
    private List<Integer> pronadiSvePorukeKorisnika() {
        List<Integer> porukeKorisnika = new ArrayList<>();
        for (MqttPoruke p : mqttPoruke) {
            porukeKorisnika.add(p.getId());
        }
        return porukeKorisnika;
    }

    public String getKorisnik() {
        return korisnik;
    }

    public List<MqttPoruke> getMqttPoruke() {
        mqttPoruke = mqttPorukeFacade.preuzmiMqttPoruke();
        return mqttPoruke;
    }

    public int getBrojLinija() {
        return brojLinija;
    }

    public String getPoruka() {
        return poruka;
    }
    
}
