/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.foi.nwtis.npetanjek.web.zrna;

import javax.inject.Named;
import javax.enterprise.context.SessionScoped;
import java.io.Serializable;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.foi.nwtis.npetanjek.db.MyDataBase;
import org.foi.nwtis.npetanjek.konfiguracije.Konfiguracija;
import org.foi.nwtis.npetanjek.web.podaci.DnevnikRada;
import org.foi.nwtis.npetanjek.web.slusaci.SlusacAplikacije;

/**
 *
 * @author Nikola
 */
@Named(value = "pregledDnevnikaRada")
@SessionScoped
public class PregledDnevnikaRada implements Serializable {

    private List<DnevnikRada> dnevnik;
    Konfiguracija konfiguracija;
    private final int brojLinija;
    MyDataBase mdb;
    private int brojStranica;
    int brojZapisa;
    private int ukBrojZapisa;
    private boolean disabled = false;
    private int trenutnaStranica = 1;
    private String filter;
    boolean filtriraj = false;
    private String odVremena;
    private String doVremena;

    /**
     * Creates a new instance of PregledDnevnikaRada
     */
    public PregledDnevnikaRada() {
        konfiguracija = SlusacAplikacije.getKonfiguracija();
        brojLinija = Integer.parseInt(konfiguracija.dajPostavku("pregledDnevnika.brojLinija"));
        mdb = MyDataBase.getInstance();
        reset();
    }

    public void preuzmiDnevnikRada() {
        brojStranica = 0;
        String constraint = null;
        mdb.setPreuzmiSve(false);
        if (filtriraj) {
            if (!filter.equals(""))
                constraint = " vrstaZahtjeva = '" + filter + "'";
            else
                constraint = "";
            if (!odVremena.equals("") && !doVremena.equals("")) {
                if (!filter.equals(""))
                    constraint += " AND";
                constraint += " vrijeme >= '" + convertDate(odVremena) + "' AND vrijeme <= '" + convertDate(doVremena) + "'";
            }
        }
        System.out.println(constraint);
        dnevnik = mdb.dohvatiDnevnikRada(constraint);
        ukBrojZapisa = mdb.dohvatiBrojZapisa("dnevnik", constraint);
        brojStranica = (int) (ukBrojZapisa / brojLinija);
        if (ukBrojZapisa % brojLinija != 0) {
            brojStranica += 1;
        }
        brojZapisa = dnevnik.size();
        if (trenutnaStranica >= brojStranica) {
            disabled = true;
        }
    }

    private String convertDate(String date) {
        DateFormat originalSDF = new SimpleDateFormat("dd.MM.yyyy HH:mm:ss");
        DateFormat targetSDF = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        Date datum;
        try {
            datum = originalSDF.parse(date);
            return targetSDF.format(datum);
        } catch (ParseException ex) {
            Logger.getLogger(PregledDnevnikaRada.class.getName()).log(Level.SEVERE, null, ex);
            return "";
        }
    }

    public void filtriraj() {
        filtriraj = !(filter.equals("") && odVremena.equals("") && doVremena.equals(""));
        //filtriraj = !filter.equals("");
        //disabled = false;
        reset();
    }

    public void prethodnaStranica() {
        if (trenutnaStranica > 1) {
            trenutnaStranica--;
            mdb.prethodnaStranica();
            disabled = false;
        }
    }

    public void sljedecaStranica() {
        mdb.sljedecaStranica();
        trenutnaStranica++;
    }

    public void reset() {
        trenutnaStranica = 1;
        disabled = false;
        mdb.resetiraj();
        preuzmiDnevnikRada();
    }

    public List<DnevnikRada> getDnevnik() {
        return dnevnik;
    }

    public int getBrojStranica() {
        return brojStranica;
    }

    public int getBrojLinija() {
        return brojLinija;
    }

    public int getTrenutnaStranica() {
        return trenutnaStranica;
    }

    public int getUkBrojZapisa() {
        return ukBrojZapisa;
    }

    public boolean isDisabled() {
        return disabled;
    }

    public String getFilter() {
        return filter;
    }

    public void setFilter(String filter) {
        this.filter = filter;
    }

    public String getOdVremena() {
        return odVremena;
    }

    public void setOdVremena(String odVremena) {
        this.odVremena = odVremena;
    }

    public String getDoVremena() {
        return doVremena;
    }

    public void setDoVremena(String doVremena) {
        this.doVremena = doVremena;
    }

}
