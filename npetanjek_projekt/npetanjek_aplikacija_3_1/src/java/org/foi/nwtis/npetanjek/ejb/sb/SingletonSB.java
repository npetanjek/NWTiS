/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.foi.nwtis.npetanjek.ejb.sb;

import java.io.EOFException;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.ejb.Singleton;
import javax.ejb.LocalBean;
import org.foi.nwtis.npetanjek.ejb.podaci.KomandaJMS;
import org.foi.nwtis.npetanjek.ejb.podaci.MqttJMS;
import org.foi.nwtis.npetanjek.ejb.sucelja.WebSocketSucelje;

/**
 *
 * @author Nikola
 */
@Singleton
@LocalBean
public class SingletonSB {

    public void setDatotekaSerijalizacije(String aDatotekaSerijalizacije) {
        datotekaSerijalizacije = aDatotekaSerijalizacije;
        File f = new File(datotekaSerijalizacije);
        if (!f.exists()) {
            try {
                f.createNewFile();
            } catch (IOException ex) {
                Logger.getLogger(SingletonSB.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }

    private List<KomandaJMS> jmsPoruke = new ArrayList<>();
    private List<MqttJMS> mqttPoruke = new ArrayList<>();
    private String datotekaSerijalizacije;
    private WebSocketSucelje webSocketSucelje;
    File f;

    @PostConstruct
    void init() {
        
    }
    
    public void brisiJmsPoruke() {
        this.jmsPoruke.clear();
    }
    
    public void brisiMqttPoruke() {
        this.mqttPoruke.clear();
    }

    public boolean spremiJMSPoruku(KomandaJMS msg) {
        if (webSocketSucelje != null)
            webSocketSucelje.saljiObavijest("K");
        return this.jmsPoruke.add(msg);
    }
    
    public boolean spremiMQTTPoruku(MqttJMS msg) {
        if (webSocketSucelje != null)
            webSocketSucelje.saljiObavijest("M");
        return this.mqttPoruke.add(msg);
    }

    private void serijalizirajJMSPoruke() throws IOException {
        System.out.println("Serijaliziram podatke...");
        FileOutputStream out = new FileOutputStream(datotekaSerijalizacije);
        ObjectOutputStream s = new ObjectOutputStream(out);
        if (jmsPoruke.isEmpty())
            System.out.println("nema jms poruka");
        for (KomandaJMS k : jmsPoruke) {
            s.writeObject(k);
        }
        for (MqttJMS m : mqttPoruke) {
            s.writeObject(m);
        }
        s.close();
    }

    public void deserijalizirajJMSPoruke() throws Exception {
        System.out.println("Datoteka serijalizacije: " + datotekaSerijalizacije);
        System.out.println("Deserijaliziram podatke");
        
        FileInputStream in = new FileInputStream(datotekaSerijalizacije);

        ObjectInputStream s = new ObjectInputStream(in);
        try {
            while (true) {
                Object o = s.readObject();
                if (o instanceof KomandaJMS)
                    jmsPoruke.add((KomandaJMS) o);
                else if (o instanceof MqttJMS)
                    mqttPoruke.add((MqttJMS) o);
                else
                    System.out.println("Neočekivani tip objekta");
            }
        } catch (EOFException ex) {
            s.close();
        }
    }

    @PreDestroy
    void destroy() {
        try {
            serijalizirajJMSPoruke();
        } catch (IOException ex) {
            Logger.getLogger(SingletonSB.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public List<KomandaJMS> getJmsPoruke() {
        return jmsPoruke;
    }

    public void setJmsPoruke(List<KomandaJMS> jmsPoruke) {
        this.jmsPoruke = jmsPoruke;
    }

    public List<MqttJMS> getMqttPoruke() {
        return mqttPoruke;
    }

    public void setMqttPoruke(List<MqttJMS> mqttPoruke) {
        this.mqttPoruke = mqttPoruke;
    }

    public void setWebSocketSucelje(WebSocketSucelje webSocketSucelje) {
        this.webSocketSucelje = webSocketSucelje;
    }

}
