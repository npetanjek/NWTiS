/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.foi.nwtis.npetanjek.web;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.websocket.EndpointConfig;
import javax.websocket.OnClose;
import javax.websocket.OnMessage;
import javax.websocket.OnOpen;
import javax.websocket.Session;
import javax.websocket.server.ServerEndpoint;
import org.foi.nwtis.npetanjek.ejb.sucelja.WebSocketSucelje;
        
/**
 *
 * @author Nikola
 */
@ServerEndpoint("/infoPoruka")
public class InformatorPoruka implements WebSocketSucelje {
    
    static List<Session> sesije = new ArrayList<>();
    
    @OnOpen
    public void onOpen(Session session, EndpointConfig config) {
        sesije.add(session);
        System.out.println("Ostvarena veza");
    }

    @OnMessage
    public void onMessage(String message) {
        System.out.println("infoPoruka: primljena poruka: " + message);
        for (Session s : sesije) {
            try {
                s.getBasicRemote().sendText(message);
            } catch (IOException ex) {
                Logger.getLogger(InformatorPoruka.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }
    
    @OnClose
    public void onClose(Session session) {
        sesije.remove(session);
        System.out.println("Zatvorena veza");
    }
    
    @Override
    public void saljiObavijest(String msg) {
        for (Session s : sesije) {
            try {
                s.getBasicRemote().sendText(msg);
            } catch (IOException ex) {
                Logger.getLogger(InformatorPoruka.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }
    
}
