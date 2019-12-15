/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.foi.nwtis.npetanjek.web.podaci;

/**
 *
 * @author Nikola
 */
public enum AerodromStatus {
    PASIVAN,
    AKTIVAN,
    BLOKIRAN,
    NEPOSTOJI;
    
    public String value() {
        return name();
    }

    public static AerodromStatus fromValue(String v) {
        return valueOf(v);
    }
    
}
