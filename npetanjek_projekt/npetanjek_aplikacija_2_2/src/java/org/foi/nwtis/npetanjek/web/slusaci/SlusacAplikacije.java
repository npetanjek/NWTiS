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
import org.foi.nwtis.npetanjek.ejb.sb.AutentikacijaSB;
import org.foi.nwtis.npetanjek.konfiguracije.Konfiguracija;
import org.foi.nwtis.npetanjek.konfiguracije.KonfiguracijaApstraktna;
import org.foi.nwtis.npetanjek.konfiguracije.NeispravnaKonfiguracija;
import org.foi.nwtis.npetanjek.konfiguracije.NemaKonfiguracije;

/**
 * Web application lifecycle listener.
 *
 * @author Nikola
 */
public class SlusacAplikacije implements ServletContextListener {

    public static Konfiguracija getKonfiguracija() {
        return konfiguracija;
    }
    
    @EJB
    private AutentikacijaSB autentikacijaSB;
    
    private static Konfiguracija konfiguracija;

    @Override
    public void contextInitialized(ServletContextEvent sce) {
        ServletContext sc = sce.getServletContext();
        String putanja = sc.getRealPath("/WEB-INF");
        String datoteka = putanja + File.separator + sc.getInitParameter("konfiguracija");
        try {
            konfiguracija = KonfiguracijaApstraktna.preuzmiKonfiguraciju(datoteka);
            autentikacijaSB.setKonfiguracija(konfiguracija);
        } catch (NemaKonfiguracije | NeispravnaKonfiguracija ex) {
            Logger.getLogger(SlusacAplikacije.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    @Override
    public void contextDestroyed(ServletContextEvent sce) {
        autentikacijaSB.odjava();
    }
}
