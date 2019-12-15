/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.foi.nwtis.npetanjek.web.podaci;

import java.util.List;

/**
 *
 * @author Nikola
 */
public class Aerodrom {
    private List<Avion> avioni;
    private String drzava;
    private String icao;
    private Lokacija lokacija;
    private String naziv;
    private AerodromStatus status;

    public List<Avion> getAvioni() {
        return avioni;
    }

    public String getDrzava() {
        return drzava;
    }

    public void setDrzava(String drzava) {
        this.drzava = drzava;
    }

    public String getIcao() {
        return icao;
    }

    public void setIcao(String icao) {
        this.icao = icao;
    }

    public Lokacija getLokacija() {
        return lokacija;
    }

    public void setLokacija(Lokacija lokacija) {
        this.lokacija = lokacija;
    }

    public String getNaziv() {
        return naziv;
    }

    public void setNaziv(String naziv) {
        this.naziv = naziv;
    }

    public AerodromStatus getStatus() {
        return status;
    }

    public void setStatus(AerodromStatus status) {
        this.status = status;
    }

    public void setAvioni(List<Avion> avioni) {
        this.avioni = avioni;
    }
}
