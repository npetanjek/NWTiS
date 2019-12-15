/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.foi.nwtis.npetanjek.web.slusaci;

import java.io.File;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.ejb.EJB;
import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import org.foi.nwtis.npetanjek.ejb.sb.SingletonSB;
import org.foi.nwtis.npetanjek.konfiguracije.Konfiguracija;
import org.foi.nwtis.npetanjek.konfiguracije.KonfiguracijaApstraktna;
import org.foi.nwtis.npetanjek.konfiguracije.NeispravnaKonfiguracija;
import org.foi.nwtis.npetanjek.konfiguracije.NemaKonfiguracije;

/**
 * Web application lifecycle listener.
 *
 * @author Nikola
 */
public class Slusac implements ServletContextListener {

    public static Konfiguracija getKonfiguracija() {
        return konfiguracija;
    }

    @EJB
    SingletonSB singletonSB;
    
    ServletContext sc;
    private static Konfiguracija konfiguracija;

    @Override
    public void contextInitialized(ServletContextEvent sce) {
        sc = sce.getServletContext();
        String putanja = sc.getRealPath("/WEB-INF");
        String datotekaKonfiguracije = putanja + File.separator + sc.getInitParameter("konfiguracija");
        try {
            konfiguracija = KonfiguracijaApstraktna.preuzmiKonfiguraciju(datotekaKonfiguracije);
            String nazivDatotekeSerijalizacije = konfiguracija.dajPostavku("datoteka.serijalizacije");
            String datotekaSerijalizacije = dohvatiPutanjuDatoteke(nazivDatotekeSerijalizacije);
            singletonSB.setDatotekaSerijalizacije(datotekaSerijalizacije);
            try {
                singletonSB.deserijalizirajJMSPoruke();
            } catch (Exception ex) {
                Logger.getLogger(Slusac.class.getName()).log(Level.SEVERE, null, ex);
            }
        } catch (NemaKonfiguracije | NeispravnaKonfiguracija ex) {
            Logger.getLogger(Slusac.class.getName()).log(Level.SEVERE, null, ex);
        }

    }
    
    private String dohvatiPutanjuDatoteke(String datoteka) {
        String putanja = sc.getRealPath("/WEB-INF");
        String[] polje = putanja.split("npetanjek_aplikacija_3_2");
        polje[0] += "npetanjek_aplikacija_3_2\\web\\WEB-INF\\" + datoteka;
        return polje[0];
    }

    @Override
    public void contextDestroyed(ServletContextEvent sce) {
        
    }
}
