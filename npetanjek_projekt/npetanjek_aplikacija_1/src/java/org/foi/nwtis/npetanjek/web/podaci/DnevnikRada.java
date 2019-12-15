/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.foi.nwtis.npetanjek.web.podaci;

import java.sql.Timestamp;

/**
 *
 * @author Nikola
 */
public class DnevnikRada {
    private int id;
    private Timestamp vrijeme;
    private String zahtjev;
    private String vrstaZahtjeva;
    private String dioAplikacije;
    private String korisnik;

    public DnevnikRada() {
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public Timestamp getVrijeme() {
        return vrijeme;
    }

    public void setVrijeme(Timestamp vrijeme) {
        this.vrijeme = vrijeme;
    }

    public String getZahtjev() {
        return zahtjev;
    }

    public void setZahtjev(String zahtjev) {
        this.zahtjev = zahtjev;
    }

    public String getVrstaZahtjeva() {
        return vrstaZahtjeva;
    }

    public void setVrstaZahtjeva(String vrstaZahtjeva) {
        this.vrstaZahtjeva = vrstaZahtjeva;
    }

    public String getDioAplikacije() {
        return dioAplikacije;
    }

    public void setDioAplikacije(String dioAplikacije) {
        this.dioAplikacije = dioAplikacije;
    }

    public String getKorisnik() {
        return korisnik;
    }

    public void setKorisnik(String korisnik) {
        this.korisnik = korisnik;
    }
}
