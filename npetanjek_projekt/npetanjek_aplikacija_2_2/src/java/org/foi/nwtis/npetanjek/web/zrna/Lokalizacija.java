/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.foi.nwtis.npetanjek.web.zrna;

import javax.inject.Named;
import javax.enterprise.context.SessionScoped;
import java.io.Serializable;

/**
 *
 * @author Nikola
 */
@Named(value = "lokalizacija")
@SessionScoped
public class Lokalizacija implements Serializable {
    
    private String jezik = "hr";

    /**
     * Creates a new instance of Lokalizacija
     */
    public Lokalizacija() {
    }

    public String getJezik() {
        return jezik;
    }

    public void setJezik(String jezik) {
        this.jezik = jezik;
    }
    
    public Object odaberiJezik() {
        return "jezik";
    }
    
}
