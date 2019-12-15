/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.foi.nwtis.npetanjek.web.zrna;

import javax.inject.Named;
import javax.enterprise.context.SessionScoped;
import java.io.Serializable;
import java.util.List;
import javax.annotation.PostConstruct;
import javax.ejb.EJB;
import org.foi.nwtis.npetanjek.ejb.podaci.KomandaJMS;
import org.foi.nwtis.npetanjek.ejb.sb.SingletonSB;
import org.foi.nwtis.npetanjek.konfiguracije.Konfiguracija;
import org.foi.nwtis.npetanjek.web.InformatorPoruka;
import org.foi.nwtis.npetanjek.web.slusaci.Slusac;

/**
 *
 * @author Nikola
 */
@Named(value = "pregledJMSPoruka")
@SessionScoped
public class PregledJMSPoruka implements Serializable {

    @EJB
    private SingletonSB singletonSB;
    
    private List<KomandaJMS> jmsPoruke;
    private final int brojLinija;
    Konfiguracija konfiguracija;
    
    /**
     * Creates a new instance of pregledJMSPoruka
     */
    public PregledJMSPoruka() {
        konfiguracija = Slusac.getKonfiguracija();
        brojLinija = Integer.parseInt(konfiguracija.dajPostavku("pregledDnevnikaRada.brojLinija"));
    }
    
    @PostConstruct
    void init() {
        singletonSB.setWebSocketSucelje(new InformatorPoruka());
    }
    
    public void preuzmiJmsPoruke() {
        jmsPoruke = singletonSB.getJmsPoruke();
    }
    
    public void brisiJmsPoruke() {
        singletonSB.brisiJmsPoruke();
    }

    public List<KomandaJMS> getJmsPoruke() {
        jmsPoruke = singletonSB.getJmsPoruke();
        return jmsPoruke;
    }

    public int getBrojLinija() {
        return brojLinija;
    }
    
}
