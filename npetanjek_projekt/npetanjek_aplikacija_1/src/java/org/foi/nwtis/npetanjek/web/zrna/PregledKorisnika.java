/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.foi.nwtis.npetanjek.web.zrna;

import javax.inject.Named;
import javax.enterprise.context.SessionScoped;
import java.io.Serializable;
import java.util.List;
import org.foi.nwtis.npetanjek.db.MyDataBase;
import org.foi.nwtis.npetanjek.konfiguracije.Konfiguracija;
import org.foi.nwtis.npetanjek.web.podaci.Korisnik;
import org.foi.nwtis.npetanjek.web.slusaci.SlusacAplikacije;

/**
 *
 * @author Nikola
 */
@Named(value = "pregledKorisnika")
@SessionScoped
public class PregledKorisnika implements Serializable {
    
    Konfiguracija konfiguracija;
    MyDataBase mdb;
    private final int brojLinija;
    private List<Korisnik> korisnici;
    private int trenutnaStranica = 1;
    private boolean disabled = false;
    private final int pocetak = 1;
    private int brojKorisnika;
    private int ukBrojKorisnika;
    private int brojStranica;

    /**
     * Creates a new instance of PregledKorisnika
     */
    public PregledKorisnika() {
        konfiguracija = SlusacAplikacije.getKonfiguracija();
        brojLinija = Integer.parseInt(konfiguracija.dajPostavku("pregledKorisnika.brojLinija"));
        mdb = MyDataBase.getInstance();
        reset();
    }

    public int getBrojLinija() {
        return brojLinija;
    }
    
    public void preuzmiKorisnike() {
        brojStranica = 0;
        ukBrojKorisnika = mdb.dohvatiBrojZapisa("korisnici", null);
        brojStranica = (int) (ukBrojKorisnika / brojLinija + 1);
        mdb.setPreuzmiSve(false);
        korisnici = mdb.dohvatiKorisnike();
        brojKorisnika = korisnici.size();
        if (trenutnaStranica >= brojStranica)
            disabled = true;
    }

    public List<Korisnik> getKorisnici() {
        return korisnici;
    }

    public int getTrenutnaStranica() {
        return trenutnaStranica;
    }
    
    public void prethodnaStranica() {
        if (trenutnaStranica > 1) {
            //pocetak = ukBrojKorisnika - brojKorisnika;
            trenutnaStranica--;
            mdb.prethodnaStranica();
            //preuzmiKorisnike(); 
            disabled = false;
        }
    }
    
    public void sljedecaStranica() {
        mdb.sljedecaStranica();        
        trenutnaStranica++;
        //preuzmiKorisnike();
                
    }
    
    public void reset() {
        trenutnaStranica = 1;
        disabled = false;
        mdb.resetiraj();
        preuzmiKorisnike();
    }

    public boolean isDisabled() {
        return disabled;
    }

    public int getBrojKorisnika() {
        return brojKorisnika;
    }

    public int getUkBrojKorisnika() {
        return ukBrojKorisnika;
    }

    public int getPocetak() {
        return pocetak;
    }

    public int getBrojStranica() {
        return brojStranica;
    }
    
}
