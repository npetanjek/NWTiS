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
public class Avion {
    private String callsign;
    private String estarrivalairport;
    private String estdepartureairport;
    private String icao24;
    private Integer id;

    public String getCallsign() {
        return callsign;
    }

    public void setCallsign(String callsign) {
        this.callsign = callsign;
    }

    public String getEstarrivalairport() {
        return estarrivalairport;
    }

    public void setEstarrivalairport(String estarrivalairport) {
        this.estarrivalairport = estarrivalairport;
    }

    public String getEstdepartureairport() {
        return estdepartureairport;
    }

    public void setEstdepartureairport(String estdepartureairport) {
        this.estdepartureairport = estdepartureairport;
    }

    public String getIcao24() {
        return icao24;
    }

    public void setIcao24(String icao24) {
        this.icao24 = icao24;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }
}
