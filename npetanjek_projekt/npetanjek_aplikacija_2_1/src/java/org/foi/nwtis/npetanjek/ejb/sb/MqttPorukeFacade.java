/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.foi.nwtis.npetanjek.ejb.sb;

import java.util.List;
import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;
import org.foi.nwtis.npetanjek.ejb.eb.MqttPoruke;

/**
 *
 * @author Nikola
 */
@Stateless
public class MqttPorukeFacade extends AbstractFacade<MqttPoruke> {

    @PersistenceContext(unitName = "NWTiS_npetanjek_PU")
    private EntityManager em;

    @Override
    protected EntityManager getEntityManager() {
        return em;
    }

    public MqttPorukeFacade() {
        super(MqttPoruke.class);
    }
    
    public List<MqttPoruke> preuzmiMqttPoruke() {
        CriteriaBuilder cb = getEntityManager().getCriteriaBuilder();
        CriteriaQuery cq = cb.createQuery();
        Root<MqttPoruke> mqttPoruke = cq.from(MqttPoruke.class);
        cq.select(mqttPoruke);
        Query q = em.createQuery(cq);
        List<MqttPoruke> rezultat = q.getResultList();
        return rezultat;
    }
    
}
