/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.foi.nwtis.npetanjek.mqtt.slusaci;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import java.net.URISyntaxException;
import java.sql.Timestamp;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.jms.Connection;
import javax.jms.ConnectionFactory;
import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageProducer;
import javax.jms.Session;
import javax.jms.TextMessage;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import org.foi.nwtis.npetanjek.ejb.eb.MqttPoruke;
import org.foi.nwtis.npetanjek.ejb.sb.MqttPorukeFacade;
import org.foi.nwtis.npetanjek.konfiguracije.Konfiguracija;
import org.fusesource.hawtbuf.Buffer;
import org.fusesource.hawtbuf.UTF8Buffer;
import org.fusesource.mqtt.client.Callback;
import org.fusesource.mqtt.client.CallbackConnection;
import org.fusesource.mqtt.client.Listener;
import org.fusesource.mqtt.client.MQTT;
import org.fusesource.mqtt.client.QoS;
import org.fusesource.mqtt.client.Topic;

/**
 *
 * @author Nikola
 */
public class SlusacMqtt extends Thread {

    String user;
    String password;
    String host;
    int port;
    String topic;
    MQTT mqtt;
    CallbackConnection connection;
    boolean kraj = false;
    MqttPorukeFacade mqttPorukeFacade;

    public SlusacMqtt(Konfiguracija konfiguracija, MqttPorukeFacade mqttPorukeFacade) {
        this.mqttPorukeFacade = mqttPorukeFacade;
        user = konfiguracija.dajPostavku("korisnik");
        password = konfiguracija.dajPostavku("lozinka");
        host = konfiguracija.dajPostavku("mqtt.host");
        port = Integer.parseInt(konfiguracija.dajPostavku("mqtt.port"));
        topic = konfiguracija.dajPostavku("mqtt.topic");
        System.out.println("user: " + user);
        System.out.println("password: " + password);
        System.out.println("host: " + host);
        System.out.println("port: " + port);
        System.out.println("topic: " + topic);
    }

    @Override
    public void interrupt() {
        System.out.println("Gasim MQTT slušača...");
        kraj = true;
        connection.disconnect(new Callback<Void>() {
            @Override
            public void onSuccess(Void t) {
                System.out.println("disconnected");
            }

            @Override
            public void onFailure(Throwable thrwbl) {
                System.out.println("disconnect failed");
            }
        });
        super.interrupt();
    }

    @Override
    public void run() {
        connection = mqtt.callbackConnection();
        connection.listener(new Listener() {
            int count = 0;

            @Override
            public void onConnected() {
                System.out.println("Otvorena veza na MQTT");
            }

            @Override
            public void onDisconnected() {
                System.out.println("Prekinuta veza na MQTT");
            }

            @Override
            public void onPublish(UTF8Buffer utfb, Buffer buffer, Runnable r) {
                String body = buffer.utf8().toString();
                System.out.println("Stigla poruka br: " + count);
                System.out.println("Sadrzaj poruke: " + body);
                Gson gson = new Gson();
                JsonObject jsonObject = gson.fromJson(body, JsonObject.class);
                spremiPorukuUBazu(jsonObject);
                posaljiJMSPoruku(count, jsonObject.toString(), stringToTimestamp(jsonObject.get("vrijeme").getAsString()));
                count++;
            }

            @Override
            public void onFailure(Throwable thrwbl) {
                System.out.println("Problem u vezi na MQTT");
            }
        });
        connection.connect(new Callback<Void>() {
            @Override
            public void onSuccess(Void t) {
                Topic[] topics = {new Topic(topic, QoS.AT_MOST_ONCE)};
                connection.subscribe(topics, new Callback<byte[]>() {
                    @Override
                    public void onSuccess(byte[] t) {
                        System.out.println("Pretplata na: " + topic);
                    }

                    @Override
                    public void onFailure(Throwable thrwbl) {
                        System.out.println("Problem kod pretplate na: " + topic);
                    }
                });
            }

            @Override
            public void onFailure(Throwable thrwbl) {
                System.out.println("Problem kod pretplate na: " + topic);
            }
        });
        synchronized (SlusacMqtt.class) {
            while (!kraj && !this.isInterrupted()) {
                try {
                    SlusacMqtt.class.wait();
                } catch (InterruptedException ex) {
                    Logger.getLogger(SlusacMqtt.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        }
    }

    private void spremiPorukuUBazu(JsonObject jsonObject) {
        MqttPoruke mqttPoruke = new MqttPoruke();
        String korisnik = jsonObject.get("korisnik").getAsString();
        String aerodrom = jsonObject.get("aerodrom").getAsString();
        String avion = jsonObject.get("avion").getAsString();
        String oznaka = jsonObject.get("oznaka").getAsString();
        String poruka = jsonObject.get("poruka").getAsString();
        String vrijeme = jsonObject.get("vrijeme").getAsString();
        mqttPoruke.setKorisnik(korisnik);
        mqttPoruke.setAerodrom(aerodrom);
        mqttPoruke.setAvion(avion);
        mqttPoruke.setOznaka(oznaka);
        mqttPoruke.setPoruka(poruka);
        mqttPoruke.setVrijeme(stringToTimestamp(vrijeme));        
        mqttPorukeFacade.create(mqttPoruke);
    }
    
    private void posaljiJMSPoruku(int id, String poruka, Timestamp vrijeme) {
        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("id", id);
        jsonObject.addProperty("poruka", poruka);
        jsonObject.addProperty("vrijeme", vrijeme.toString());
        String msg = jsonObject.toString();
        try {
            sendJMSMessageToNWTiS_npetanjek_2(msg);
        } catch (JMSException | NamingException ex) {
            Logger.getLogger(SlusacMqtt.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    private Timestamp stringToTimestamp(String vrijeme) {
        SimpleDateFormat sdf = new SimpleDateFormat("dd.MM.yyyy HH:mm:ss.SSSS");
        Timestamp timestamp = null;
        try {
            Date parsedDate = sdf.parse(vrijeme);
            timestamp = new Timestamp(parsedDate.getTime());
        } catch (ParseException ex) {
            Logger.getLogger(SlusacMqtt.class.getName()).log(Level.SEVERE, null, ex);
        }
        return timestamp;
    }

    @Override
    public synchronized void start() {
        mqtt = new MQTT();
        try {
            mqtt.setHost(host, port);
            mqtt.setUserName(user);
            mqtt.setPassword(password);
            System.out.println("mqtthost: " + mqtt.getHost());
            System.out.println("mqttusername: " + mqtt.getUserName());
            System.out.println("mqttpassword: " + mqtt.getPassword());
        } catch (URISyntaxException ex) {
            Logger.getLogger(SlusacMqtt.class.getName()).log(Level.SEVERE, null, ex);
        }
        super.start();
    }

    private Message createJMSMessageForjmsNWTiS_npetanjek_2(Session session, Object messageData) throws JMSException {
        // TODO create and populate message to send
        TextMessage tm = session.createTextMessage();
        tm.setText(messageData.toString());
        return tm;
    }

    private void sendJMSMessageToNWTiS_npetanjek_2(Object messageData) throws JMSException, NamingException {
        Context c = new InitialContext();
        ConnectionFactory cf = (ConnectionFactory) c.lookup("jms/NWTiS_npetanjek_QF_2");
        Connection conn = null;
        Session s = null;
        try {
            conn = cf.createConnection();
            s = conn.createSession(false, s.AUTO_ACKNOWLEDGE);
            Destination destination = (Destination) c.lookup("jms/NWTiS_npetanjek_2");
            MessageProducer mp = s.createProducer(destination);
            mp.send(createJMSMessageForjmsNWTiS_npetanjek_2(s, messageData));
        } finally {
            if (s != null) {
                try {
                    s.close();
                } catch (JMSException e) {
                    Logger.getLogger(this.getClass().getName()).log(Level.WARNING, "Cannot close session", e);
                }
            }
            if (conn != null) {
                conn.close();
            }
        }
    }

    

    
}
