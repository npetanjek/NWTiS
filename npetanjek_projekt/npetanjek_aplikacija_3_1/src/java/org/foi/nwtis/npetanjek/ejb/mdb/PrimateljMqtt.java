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
import org.foi.nwtis.npetanjek.ejb.podaci.MqttJMS;
import org.foi.nwtis.npetanjek.ejb.sb.SingletonSB;

/**
 *
 * @author Nikola
 */
@MessageDriven(activationConfig = {
    @ActivationConfigProperty(propertyName = "destinationLookup", propertyValue = "jms/NWTiS_npetanjek_2"),
    @ActivationConfigProperty(propertyName = "destinationType", propertyValue = "javax.jms.Queue")
})
public class PrimateljMqtt implements MessageListener {

    @EJB
    private SingletonSB singletonSB;
    
    public PrimateljMqtt() {
    }
    
    @Override
    public void onMessage(Message message) {
        TextMessage mqttPoruka = (TextMessage) message;
        try {
            System.out.println("Primljena MQTT poruka: " + mqttPoruka.getText());
            Gson gson = new Gson();
            JsonObject jsonObject = gson.fromJson(mqttPoruka.getText(), JsonObject.class);
            int id = jsonObject.get("id").getAsInt();
            String poruka = jsonObject.get("poruka").getAsString();
            String vrijeme = jsonObject.get("vrijeme").getAsString();
            MqttJMS mqttJMS = new MqttJMS();
            mqttJMS.setId(id);
            mqttJMS.setPoruka(poruka);
            mqttJMS.setVrijeme(stringToTimestamp(vrijeme));
            singletonSB.spremiMQTTPoruku(mqttJMS);
        } catch (JMSException ex) {
            Logger.getLogger(PrimateljMqtt.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    private Timestamp stringToTimestamp(String vrijeme) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
        Timestamp timestamp = null;
        try {
            Date parsedDate = sdf.parse(vrijeme);
            timestamp = new Timestamp(parsedDate.getTime());
        } catch (ParseException ex) {
            Logger.getLogger(PrimateljMqtt.class.getName()).log(Level.SEVERE, null, ex);
        }
        return timestamp;
    }
    
}
