/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.foi.nwtis.npetanjek.ejb.eb;

import java.io.Serializable;
import java.util.Date;
import javax.persistence.Basic;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.Table;
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
@Table(name = "MQTT_PORUKE")
@XmlRootElement
@NamedQueries({
    @NamedQuery(name = "MqttPoruke.findAll", query = "SELECT m FROM MqttPoruke m")})
public class MqttPoruke implements Serializable {

    private static final long serialVersionUID = 1L;
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Basic(optional = false)
    private Integer id;
    @Basic(optional = false)
    @NotNull
    @Size(min = 1, max = 20)
    private String korisnik;
    @Basic(optional = false)
    @NotNull
    @Size(min = 1, max = 10)
    private String aerodrom;
    @Basic(optional = false)
    @NotNull
    @Size(min = 1, max = 10)
    private String avion;
    @Basic(optional = false)
    @NotNull
    @Size(min = 1, max = 10)
    private String oznaka;
    @Basic(optional = false)
    @NotNull
    @Size(min = 1, max = 255)
    private String poruka;
    @Basic(optional = false)
    @NotNull
    @Temporal(TemporalType.TIMESTAMP)
    private Date vrijeme;

    public MqttPoruke() {
    }

    public MqttPoruke(Integer id) {
        this.id = id;
    }

    public MqttPoruke(Integer id, String korisnik, String aerodrom, String avion, String oznaka, String poruka, Date vrijeme) {
        this.id = id;
        this.korisnik = korisnik;
        this.aerodrom = aerodrom;
        this.avion = avion;
        this.oznaka = oznaka;
        this.poruka = poruka;
        this.vrijeme = vrijeme;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getKorisnik() {
        return korisnik;
    }

    public void setKorisnik(String korisnik) {
        this.korisnik = korisnik;
    }

    public String getAerodrom() {
        return aerodrom;
    }

    public void setAerodrom(String aerodrom) {
        this.aerodrom = aerodrom;
    }

    public String getAvion() {
        return avion;
    }

    public void setAvion(String avion) {
        this.avion = avion;
    }

    public String getOznaka() {
        return oznaka;
    }

    public void setOznaka(String oznaka) {
        this.oznaka = oznaka;
    }

    public String getPoruka() {
        return poruka;
    }

    public void setPoruka(String poruka) {
        this.poruka = poruka;
    }

    public Date getVrijeme() {
        return vrijeme;
    }

    public void setVrijeme(Date vrijeme) {
        this.vrijeme = vrijeme;
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
        if (!(object instanceof MqttPoruke)) {
            return false;
        }
        MqttPoruke other = (MqttPoruke) object;
        if ((this.id == null && other.id != null) || (this.id != null && !this.id.equals(other.id))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "org.foi.nwtis.npetanjek.ejb.eb.MqttPoruke[ id=" + id + " ]";
    }
    
}
