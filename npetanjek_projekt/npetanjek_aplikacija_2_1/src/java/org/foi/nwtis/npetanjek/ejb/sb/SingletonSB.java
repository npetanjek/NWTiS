/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.foi.nwtis.npetanjek.ejb.sb;

import java.util.ArrayList;
import java.util.List;
import javax.annotation.PreDestroy;
import javax.ejb.Singleton;
import javax.ejb.LocalBean;
import org.foi.nwtis.npetanjek.ejb.podaci.Korisnik;

/**
 *
 * @author Nikola
 */
@Singleton
@LocalBean
public class SingletonSB {
    private List<Korisnik> korisnici = new ArrayList<>();
    
    public boolean dodajKorisnika(Korisnik k) {
        return korisnici.add(k);
    }
    
    public boolean brisiKorisnika(Korisnik k) {
        for (Korisnik kor : korisnici) {
            if (kor.getKorIme().equals(k.getKorIme())) {
                korisnici.remove(kor);
                return true;
            }
        }
        return false;
    }

    public List<Korisnik> getKorisnici() {
        return korisnici;
    }
    
    @PreDestroy
    void destroy() {
        korisnici.clear();
    }
}
