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
import javax.faces.context.FacesContext;
import javax.servlet.http.HttpSession;

/**
 *
 * @author Nikola
 */
@Named(value = "prijavljenZrno")
@SessionScoped
public class PrijavljenZrno implements Serializable {

    private String korisnik;

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
    
    public void odjava() {
        HttpSession sesija = (HttpSession) FacesContext.getCurrentInstance().getExternalContext().getSession(false);
        if (sesija != null) {
            sesija.invalidate();
            try {
                FacesContext.getCurrentInstance().getExternalContext().redirect("prijava.xhtml");
            } catch (IOException ex) {
                Logger.getLogger(PrijavljenZrno.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }

    public String getKorisnik() {
        return korisnik;
    }
    
}
