/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.foi.nwtis.npetanjek.web.slusaci;

import java.io.File;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import org.foi.nwtis.npetanjek.konfiguracije.Konfiguracija;
import org.foi.nwtis.npetanjek.konfiguracije.KonfiguracijaApstraktna;
import org.foi.nwtis.npetanjek.konfiguracije.NeispravnaKonfiguracija;
import org.foi.nwtis.npetanjek.konfiguracije.NemaKonfiguracije;
import org.foi.nwtis.npetanjek.konfiguracije.bp.BP_Konfiguracija;
import org.foi.nwtis.npetanjek.web.dretve.Preuzimanje;
import org.foi.nwtis.npetanjek.web.dretve.ServerDretva;

/**
 * Web application lifecycle listener.
 *
 * @author Nikola
 */
public class SlusacAplikacije implements ServletContextListener {
    
    private static ServletContext sc;
    private static Preuzimanje preuzimanje;
    private static ServerDretva server;
    private static Konfiguracija konfiguracija;
    private static BP_Konfiguracija bpk;

    public static ServletContext getSc() {
        return sc;
    }

    public static ServerDretva getServer() {
        return server;
    }

    @Override
    public void contextInitialized(ServletContextEvent sce) {
        sc = sce.getServletContext();
        String putanja = sc.getRealPath("/WEB-INF");
        String datoteka = putanja + File.separator + sc.getInitParameter("konfiguracija");
        try {
            bpk = new BP_Konfiguracija(datoteka);
            sc.setAttribute("BP_Konfig", bpk);
            konfiguracija = KonfiguracijaApstraktna.preuzmiKonfiguraciju(datoteka);
            sc.setAttribute("My_Konfig", konfiguracija);
            System.out.println("Učitana konfiguracija");
            preuzimanje = new Preuzimanje();
            preuzimanje.start();
            server = new ServerDretva();
            server.start();
        } catch (NemaKonfiguracije | NeispravnaKonfiguracija ex) {
            System.out.println("Konfiguracija nije učitana");
            Logger.getLogger(SlusacAplikacije.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    @Override
    public void contextDestroyed(ServletContextEvent sce) {
        if (preuzimanje != null)
            preuzimanje.interrupt();
        if (server != null)
            server.interrupt();
        sc = sce.getServletContext();
        sc.removeAttribute("BP_Konfig");
        sc.removeAttribute("My_Konfig");
    }
    
    public static Konfiguracija getKonfiguracija() {
        return konfiguracija;
    }

    public static BP_Konfiguracija getBpk() {
        return bpk;
    }

    public static Preuzimanje getPreuzimanje() {
        return preuzimanje;
    }
}
