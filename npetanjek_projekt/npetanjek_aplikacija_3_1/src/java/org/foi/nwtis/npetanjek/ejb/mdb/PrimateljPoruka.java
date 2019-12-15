/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.foi.nwtis.npetanjek.ejb.mdb;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import java.sql.Timestamp;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.ejb.ActivationConfigProperty;
import javax.ejb.EJB;
import javax.ejb.MessageDriven;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageListener;
import javax.jms.TextMessage;
import org.foi.nwtis.npetanjek.ejb.podaci.KomandaJMS;
import org.foi.nwtis.npetanjek.ejb.sb.SingletonSB;

/**
 *
 * @author Nikola
 */
@MessageDriven(activationConfig = {
    @ActivationConfigProperty(propertyName = "destinationLookup", propertyValue = "jms/NWTiS_npetanjek_1"),
    @ActivationConfigProperty(propertyName = "destinationType", propertyValue = "javax.jms.Queue")
})
public class PrimateljPoruka implements MessageListener {

    @EJB
    private SingletonSB singletonSB;
    
    transient Gson gson;

    public PrimateljPoruka() {
    }

    @Override
    public void onMessage(Message message) {
        TextMessage jmsPoruka = (TextMessage) message;
        try {
            System.out.println("Primljena JMS poruka: " + jmsPoruka.getText());
            gson = new Gson();
            JsonObject jsonObject = gson.fromJson(jmsPoruka.getText(), JsonObject.class);
            int id = jsonObject.get("id").getAsInt();
            String komanda = jsonObject.get("komanda").getAsString();
            String vrijeme = jsonObject.get("vrijeme").getAsString();
            KomandaJMS komandaJMS = new KomandaJMS();
            komandaJMS.setId(id);
            komandaJMS.setKomanda(komanda);
            komandaJMS.setVrijeme(stringToTimestamp(vrijeme));
            singletonSB.spremiJMSPoruku(komandaJMS);
        } catch (JMSException ex) {
            Logger.getLogger(PrimateljPoruka.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    private Timestamp stringToTimestamp(String vrijeme) {
        SimpleDateFormat sdf = new SimpleDateFormat("dd.MM.yyyy HH:mm:ss.SSSS");
        Timestamp timestamp = null;
        try {
            Date parsedDate = sdf.parse(vrijeme);
            timestamp = new Timestamp(parsedDate.getTime());
        } catch (ParseException ex) {
            Logger.getLogger(PrimateljPoruka.class.getName()).log(Level.SEVERE, null, ex);
        }
        return timestamp;
    }

}
