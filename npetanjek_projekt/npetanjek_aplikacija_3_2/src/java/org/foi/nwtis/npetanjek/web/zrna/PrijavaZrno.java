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
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.ejb.EJB;
import javax.faces.context.FacesContext;
import javax.servlet.http.HttpSession;
import org.foi.nwtis.npetanjek.ejb.sb.AutentikacijaSB;

/**
 *
 * @author Nikola
 */
@Named(value = "prijavaZrno")
@SessionScoped
public class PrijavaZrno implements Serializable {

    @EJB
    private AutentikacijaSB autentikacijaSB;

    
    
    private String korisnik;
    private String lozinka;

    /**
     * Creates a new instance of PrijavaZrno
     */
    public PrijavaZrno() {
    }
    
    public void autenticiraj() {
        if (autentikacijaSB.autenticiraj(korisnik, lozinka)) {
            HttpSession sesija = (HttpSession) FacesContext.getCurrentInstance().getExternalContext().getSession(true);
            sesija.setAttribute("korisnik", korisnik);
            sesija.setAttribute("lozinka", lozinka);
            korisnik = "";
            lozinka = "";
            try {
                FacesContext.getCurrentInstance().getExternalContext().redirect("index.xhtml");
            } catch (IOException ex) {
                Logger.getLogger(PrijavaZrno.class.getName()).log(Level.SEVERE, null, ex);
            }
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
    
}
