/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.foi.nwtis.npetanjek.ejb.podaci;

import java.io.Serializable;
import java.sql.Timestamp;

/**
 *
 * @author Nikola
 */
public class KomandaJMS implements Serializable {
    private int id;
    private String komanda;
    private Timestamp vrijeme;

    public KomandaJMS() {
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getKomanda() {
        return komanda;
    }

    public void setKomanda(String komanda) {
        this.komanda = komanda;
    }

    public Timestamp getVrijeme() {
        return vrijeme;
    }

    public void setVrijeme(Timestamp vrijeme) {
        this.vrijeme = vrijeme;
    }
}
