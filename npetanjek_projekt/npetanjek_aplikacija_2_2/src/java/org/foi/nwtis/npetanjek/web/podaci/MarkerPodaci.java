/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.foi.nwtis.npetanjek.web.podaci;

import org.foi.nwtis.npetanjek.ws.klijenti.MeteoPodaci;

/**
 *
 * @author Nikola
 */
public class MarkerPodaci {
    MeteoPodaci meteoPodaci;
    Aerodrom aerodrom;
    private String koordinate;
    private String temperatura;
    private String vlaznost;

    public MarkerPodaci(Aerodrom aerodrom, MeteoPodaci meteoPodaci) {
        this.meteoPodaci = meteoPodaci;
        this.aerodrom = aerodrom;
    }

    public String getKoordinate() {
        this.koordinate = aerodrom.getLokacija().getLatitude() + ", " + aerodrom.getLokacija().getLongitude();
        return koordinate;
    }

    public String getTemperatura() {
        this.temperatura = meteoPodaci.getTemperatureValue() + " " + meteoPodaci.getTemperatureUnit();
        return temperatura;
    }

    public String getVlaznost() {
        this.vlaznost = meteoPodaci.getHumidityValue() + " " + meteoPodaci.getHumidityUnit();
        return vlaznost;
    }
    
    public String getPodaci() {
        return getKoordinate() + " | " + getTemperatura() + " | " + getVlaznost();
    }
    
}
