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
import org.foi.nwtis.npetanjek.ejb.podaci.MqttJMS;
import org.foi.nwtis.npetanjek.ejb.sb.SingletonSB;
import org.foi.nwtis.npetanjek.konfiguracije.Konfiguracija;
import org.foi.nwtis.npetanjek.web.InformatorPoruka;
import org.foi.nwtis.npetanjek.web.slusaci.Slusac;

/**
 *
 * @author Nikola
 */
@Named(value = "pregledMQTTPoruka")
@SessionScoped
public class PregledMQTTPoruka implements Serializable {

    @EJB
    private SingletonSB singletonSB;
    
    private List<MqttJMS> mqttPoruke;
    private final int brojLinija;
    Konfiguracija konfiguracija;

    /**
     * Creates a new instance of PregledMQTTPoruka
     */
    public PregledMQTTPoruka() {
        konfiguracija = Slusac.getKonfiguracija();
        brojLinija = Integer.parseInt(konfiguracija.dajPostavku("pregledMqttPoruka.brojLinija"));
    }
    
    @PostConstruct
    void init() {
        singletonSB.setWebSocketSucelje(new InformatorPoruka());
    }
    
    public void preuzmiMqttPoruke() {
        mqttPoruke = singletonSB.getMqttPoruke();
    }
    
    public void brisiMqttPoruke() {
        singletonSB.brisiMqttPoruke();
    }

    public List<MqttJMS> getMqttPoruke() {
        mqttPoruke = singletonSB.getMqttPoruke();
        return mqttPoruke;
    }

    public int getBrojLinija() {
        return brojLinija;
    }
    
}
