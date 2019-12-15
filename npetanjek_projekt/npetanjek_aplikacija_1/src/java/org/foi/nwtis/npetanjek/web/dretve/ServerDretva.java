/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.foi.nwtis.npetanjek.web.dretve;

import java.io.DataOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.foi.nwtis.npetanjek.konfiguracije.Konfiguracija;
import org.foi.nwtis.npetanjek.web.slusaci.SlusacAplikacije;

/**
 *
 * @author Nikola
 */
public class ServerDretva extends Thread {

    public static int getStatus() {
        return status;
    }

    public static void setStatus(int aStatus) {
        status = aStatus;
    }

    volatile boolean kraj = false;
    volatile boolean zaustaviServer = false;
    int port;
    ServerSocket serverSocket;
    private static int status = 11;

    @Override
    public void interrupt() {
        System.out.println("Gasim server");
        kraj = true;
        try {
            serverSocket.close();
        } catch (IOException ex) {
            Logger.getLogger(ServerDretva.class.getName()).log(Level.SEVERE, null, ex);
        }
        super.interrupt();
    }

    @Override
    public void run() {
        try {
            serverSocket = new ServerSocket(port);
            System.out.println("Otvoren socket");
            while (!kraj) {
                if (zaustaviServer) {
                    synchronized (this) {
                        interrupt();
                    }
                }
                if (!serverSocket.isClosed()) {
                    Socket socket = serverSocket.accept();
                    RadnaDretva rd = new RadnaDretva();
                    rd.setSocket(socket);
                    rd.start();
                }
            }
        } catch (IOException ex) {
            System.out.println("Pogreška kod otvaranja socketa");
            Logger.getLogger(ServerDretva.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    @Override
    public synchronized void start() {
        Konfiguracija konfiguracija = SlusacAplikacije.getKonfiguracija();
        port = Integer.parseInt(konfiguracija.dajPostavku("server.port"));
        super.start();
    }

}
