/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.foi.nwtis.npetanjek.ejb.eb;

import java.io.Serializable;
import java.util.Date;
import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import javax.xml.bind.annotation.XmlRootElement;

/**
 *
 * @author Nikola
 */
@Entity
@XmlRootElement
@NamedQueries({
    @NamedQuery(name = "Dnevnik.findAll", query = "SELECT d FROM Dnevnik d")})
public class Dnevnik implements Serializable {

    private static final long serialVersionUID = 1L;
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Basic(optional = false)
    private Integer id;
    @Basic(optional = false)
    @NotNull
    @Size(min = 1, max = 255)
    private String url;
    @Basic(optional = false)
    @NotNull
    @Size(min = 1, max = 20)
    @Column(name = "IP_ADRESA")
    private String ipAdresa;
    @Basic(optional = false)
    @NotNull
    @Size(min = 1, max = 20)
    @Column(name = "KORISNICKO_IME")
    private String korisnickoIme;
    @Basic(optional = false)
    @NotNull
    @Column(name = "VRIJEME_PRIJEMA")
    @Temporal(TemporalType.TIMESTAMP)
    private Date vrijemePrijema;
    @Basic(optional = false)
    @NotNull
    @Size(min = 1, max = 20)
    @Column(name = "TRAJANJE_OBRADE")
    private String trajanjeObrade;

    public Dnevnik() {
    }

    public Dnevnik(Integer id) {
        this.id = id;
    }

    public Dnevnik(Integer id, String url, String ipAdresa, String korisnickoIme, Date vrijemePrijema, String trajanjeObrade) {
        this.id = id;
        this.url = url;
        this.ipAdresa = ipAdresa;
        this.korisnickoIme = korisnickoIme;
        this.vrijemePrijema = vrijemePrijema;
        this.trajanjeObrade = trajanjeObrade;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getIpAdresa() {
        return ipAdresa;
    }

    public void setIpAdresa(String ipAdresa) {
        this.ipAdresa = ipAdresa;
    }

    public String getKorisnickoIme() {
        return korisnickoIme;
    }

    public void setKorisnickoIme(String korisnickoIme) {
        this.korisnickoIme = korisnickoIme;
    }

    public Date getVrijemePrijema() {
        return vrijemePrijema;
    }

    public void setVrijemePrijema(Date vrijemePrijema) {
        this.vrijemePrijema = vrijemePrijema;
    }

    public String getTrajanjeObrade() {
        return trajanjeObrade;
    }

    public void setTrajanjeObrade(String trajanjeObrade) {
        this.trajanjeObrade = trajanjeObrade;
    }

    @Override
    public int hashCode() {
        int hash = 0;
        hash += (id != null ? id.hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object object) {
        // TODO: Warning - this method won't work in the case the id fields are not set
        if (!(object instanceof Dnevnik)) {
            return false;
        }
        Dnevnik other = (Dnevnik) object;
        if ((this.id == null && other.id != null) || (this.id != null && !this.id.equals(other.id))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "org.foi.nwtis.npetanjek.ejb.eb.Dnevnik[ id=" + id + " ]";
    }
    
}
