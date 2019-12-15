/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.foi.nwtis.npetanjek.web.dretve;

import com.google.gson.JsonObject;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.Socket;
import java.nio.charset.Charset;
import java.sql.Timestamp;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
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
import org.foi.nwtis.npetanjek.db.MyDataBase;
import org.foi.nwtis.npetanjek.konfiguracije.Konfiguracija;
import org.foi.nwtis.npetanjek.web.slusaci.SlusacAplikacije;
import org.foi.nwtis.npetanjek.ws.klijenti.StatusKorisnika;

/**
 *
 * @author Nikola
 */
public class RadnaDretva extends Thread {

    boolean kraj = false;
    private Socket socket;
    String[] komandaPolje;
    String komanda;
    String vrstaZahtjeva;
    String odgovor;
    String zahtjev;
    Konfiguracija konfiguracija;
    String korisnik;
    String lozinka;
    static int status = 11;
    MyDataBase mdb;
    String k;
    static int rbrJMSPoruke;

    @Override
    public void interrupt() {
        System.out.println("Gasim radnu dretvu");
        kraj = true;
        try {
            socket.close();
        } catch (IOException ex) {
            Logger.getLogger(RadnaDretva.class.getName()).log(Level.SEVERE, null, ex);
        }
        super.interrupt();
    }

    @Override
    public void run() {
        while (!kraj) {
            try {
                switch (vrstaZahtjeva) {
                    case "autentifikacija":
                        if (!autentificiraj()) {
                            odgovor = "ERR 11;";
                        } else {
                            odgovor = "OK 10;";
                        }
                        break;
                    case "komandeZaPosluzitelja":
                        obradiKomanduZaPosluzitelja();
                        break;
                    case "komandeZaGrupu":
                        if (status == 13 || status == 14) {
                            saljiOdgovor("ERR " + status + ";");
                            break;
                        }
                        obradiKomanduZaGrupu();
                        break;
                }

                System.out.println("Odgovor: " + odgovor);
                if (odgovor == null) {
                    odgovor = "Pogreska";
                }
                saljiOdgovor(odgovor);
                mdb.zapisiuDnevnik(zahtjev, vrstaZahtjeva, this.getClass().getSimpleName(), k);
                posaljiJMSPoruku(rbrJMSPoruke++, zahtjev);
                synchronized (this) {
                    wait();
                }
            } catch (InterruptedException ex) {
                Logger.getLogger(RadnaDretva.class.getName()).log(Level.SEVERE, null, ex);
                kraj = true; // treba biti tu?
            }
        }
    }

    @Override
    public synchronized void start() {
        System.out.println("Pokrenuta radna dretva");
        dohvatiVrstuKomande();
        konfiguracija = SlusacAplikacije.getKonfiguracija();
        korisnik = konfiguracija.dajPostavku("korisnik");
        lozinka = konfiguracija.dajPostavku("lozinka");
        mdb = MyDataBase.getInstance();
        rbrJMSPoruke = 1;
        super.start();
    }

    private void posaljiJMSPoruku(int rbrJMSPoruke, String zahtjev) {
        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("id", rbrJMSPoruke);
        jsonObject.addProperty("komanda", zahtjev);
        jsonObject.addProperty("vrijeme", convertDate(new Timestamp(System.currentTimeMillis()).toString()));
        String msg = jsonObject.toString();
        try {
            sendJMSMessageToNWTiS_npetanjek_1(msg);
        } catch (JMSException | NamingException ex) {
            Logger.getLogger(RadnaDretva.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    private String convertDate(String date) {
        DateFormat originalSDF = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
        DateFormat targetSDF = new SimpleDateFormat("dd.MM.yyyy HH:mm:ss.SSS");
        Date datum;
        try {
            datum = originalSDF.parse(date);
            return targetSDF.format(datum);
        } catch (ParseException ex) {
            Logger.getLogger(RadnaDretva.class.getName()).log(Level.SEVERE, null, ex);
            return "";
        }
    }

    private void obradiKomanduZaPosluzitelja() {
        komanda = komandaPolje[2].trim();
        System.out.println("Komanda: " + komanda);
        switch (komanda) {
            case "STANJE":
                odgovor = "OK " + status + ";";
                break;
            case "PAUZA":
                if (status != 13 && status != 14) {
                    status = 13;
                    odgovor = "OK 10;";
                } else {
                    odgovor = "ERR 12;";
                }
                break;
            case "KRENI":
                if (status == 13 || status == 14) {
                    if (status == 14) {
                        status = 12;
                    } else {
                        status = 11;
                    }
                    odgovor = "OK 10;";
                } else {
                    odgovor = "ERR 13;";
                }
                break;
            case "PASIVNO":
                if (status == 12 || status == 14) {
                    odgovor = "ERR 14;";
                } else {
                    SlusacAplikacije.getPreuzimanje().pasivno = true;
                    if (status == 13) {
                        status = 14;
                    } else {
                        status = 12;
                    }
                    odgovor = "OK 10;";
                }
                break;
            case "AKTIVNO":
                if (status == 11 || status == 13) {
                    odgovor = "ERR 15;";
                } else {
                    SlusacAplikacije.getPreuzimanje().pasivno = false;
                    SlusacAplikacije.getPreuzimanje().nastavi();
                    if (status == 14) {
                        status = 13;
                    } else {
                        status = 11;
                    }
                    odgovor = "OK 10;";
                }
                break;
            case "STANI":
                if (SlusacAplikacije.getPreuzimanje().kraj == true) {
                    odgovor = "ERR 16;";
                } else {
                    odgovor = "OK 10;";
                    SlusacAplikacije.getPreuzimanje().kraj = true;
                    SlusacAplikacije.getPreuzimanje().interrupt();
                    SlusacAplikacije.getServer().zaustaviServer = true;

                }
                break;
        }
    }

    private void obradiKomanduZaGrupu() {
        komanda = komandaPolje[2].trim();
        System.out.println("Komanda: " + komanda);
        StatusKorisnika status = dajStatusGrupe(korisnik, lozinka);
        switch (komanda) {
            case "GRUPA STANJE":
                //StatusKorisnika status = dajStatusGrupe(korisnik, lozinka);
                switch (status) {
                    case AKTIVAN:
                        odgovor = "OK 21;";
                        break;
                    case BLOKIRAN:
                        odgovor = "OK 22;";
                        break;
                    case NEPOSTOJI:
                        odgovor = "ERR 21;";
                        break;
                    case DEREGISTRIRAN:
                        odgovor = "ERR 21;";
                        break;
                    default:
                        odgovor = "ERR 11;" + status;
                        break;
                }
                break;
            case "GRUPA DODAJ":
                if (status.equals(StatusKorisnika.NEPOSTOJI) || !status.equals(StatusKorisnika.REGISTRIRAN)) {
                    if (registrirajGrupu(korisnik, lozinka)) {
                        odgovor = "OK 20;";
                    }
                } else {
                    odgovor = "ERR 20;";
                }
                break;
            case "GRUPA PREKID":
                if (status.equals(StatusKorisnika.REGISTRIRAN) || status.equals(StatusKorisnika.AKTIVAN) || status.equals(StatusKorisnika.BLOKIRAN)) {
                    if (deregistrirajGrupu(korisnik, lozinka)) {
                        odgovor = "OK 20;";
                    }
                } else {
                    odgovor = "ERR 21;";
                }
                break;
            case "GRUPA KRENI":
                if (!status.equals(StatusKorisnika.AKTIVAN) || status.equals(StatusKorisnika.BLOKIRAN)) {
                    if (aktivirajGrupu(korisnik, lozinka)) {
                        odgovor = "OK 20;";
                    }
                } else if (status.equals(StatusKorisnika.AKTIVAN)) {
                    odgovor = "ERR 22;";
                } else if (status.equals(StatusKorisnika.NEPOSTOJI)) {
                    odgovor = "ERR 21;";
                }
                break;
            case "GRUPA PAUZA":
                if (status.equals(StatusKorisnika.AKTIVAN)) {
                    if (blokirajGrupu(korisnik, lozinka)) {
                        odgovor = "OK 20;";
                    }
                }
                if (!status.equals(StatusKorisnika.AKTIVAN)) {
                    odgovor = "ERR 23;";
                } else if (status.equals(StatusKorisnika.NEPOSTOJI)) {
                    odgovor = "ERR 21;";
                }
                break;
        }
    }

    private void dohvatiVrstuKomande() {
        try {
            BufferedReader br = new BufferedReader(new InputStreamReader(socket.getInputStream(), Charset.forName("UTF-8")));
            int znak;
            StringBuilder sb = new StringBuilder();
            while ((znak = br.read()) != -1) {
                sb.append((char) znak);
            }
            zahtjev = sb.toString();
            System.out.println("Primljeno: " + sb.toString());
            komandaPolje = sb.toString().split(";");
            switch (odrediVrstuZahtjeva(sb.toString())) {
                case "autentifikacija":
                    vrstaZahtjeva = "autentifikacija";
                    break;
                case "komandeZaPosluzitelja":
                    vrstaZahtjeva = "komandeZaPosluzitelja";
                    break;
                case "komandeZaGrupu":
                    vrstaZahtjeva = "komandeZaGrupu";
                    break;
                default:
                    vrstaZahtjeva = "";
                    break;
            }
        } catch (IOException ex) {
            Logger.getLogger(RadnaDretva.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    private boolean autentificiraj() {
        k = komandaPolje[0].replaceAll("[KORISNIK;]", "").trim();
        System.out.println("Korisnik: |" + korisnik + "|");
        String l = komandaPolje[1].replaceAll("[LOZINKA;]", "").trim();
        System.out.println("Lozinka: |" + lozinka + "|");
        String upit = "SELECT * FROM korisnici WHERE korisnicko_ime = '" + k + "' AND lozinka = '" + l + "'";
        return mdb.find(upit);
    }

    private String odrediVrstuZahtjeva(String zahtjev) {
        System.out.println("Zahtjev: " + zahtjev);
        Pattern pattern;
        Matcher m;
        final String autentifikacija = "^KORISNIK [a-zA-Z0-9ČčĆćŠšĐđŽž_-]{1,30}; LOZINKA [a-zA-Z0-9ČčĆćŠšĐđŽž!#_-]{1,30};$";
        final String komandeZaPosluzitelja = "^KORISNIK [a-zA-Z0-9ČčĆćŠšĐđŽž_-]{1,30}; LOZINKA [a-zA-Z0-9ČčĆćŠšĐđŽž!#_-]{1,30}; (PAUZA|KRENI|PASIVNO|AKTIVNO|STANI|STANJE);$";
        final String komandeZaGrupu = "^KORISNIK [a-zA-Z0-9ČčĆćŠšĐđŽž_-]{1,30}; LOZINKA [a-zA-Z0-9ČčĆćŠšĐđŽž!#_-]{1,30}; GRUPA (DODAJ|PREKID|KRENI|PAUZA|STANJE);$";
        pattern = Pattern.compile(autentifikacija);
        m = pattern.matcher(zahtjev);
        if (m.matches()) {
            return "autentifikacija";
        }
        pattern = Pattern.compile(komandeZaPosluzitelja);
        m = pattern.matcher(zahtjev);
        if (m.matches()) {
            return "komandeZaPosluzitelja";
        }
        pattern = Pattern.compile(komandeZaGrupu);
        m = pattern.matcher(zahtjev);
        if (m.matches()) {
            return "komandeZaGrupu";
        }
        return "";
    }

    private void saljiOdgovor(String odgovor) {
        try {
            BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream(), Charset.forName("UTF-8")));
            bw.write(odgovor);
            bw.flush();
            zatvoriSocket();
        } catch (IOException ex) {
            Logger.getLogger(RadnaDretva.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    private void zatvoriSocket() {
        try {
            socket.getOutputStream().close();
            if (!socket.isClosed()) {
                socket.shutdownOutput();
                socket.shutdownInput();
                socket.close();
            }
        } catch (IOException ex) {
            Logger.getLogger(RadnaDretva.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public void setSocket(Socket socket) {
        this.socket = socket;
    }

    private static Boolean registrirajGrupu(java.lang.String korisnickoIme, java.lang.String korisnickaLozinka) {
        org.foi.nwtis.npetanjek.ws.klijenti.AerodromiWS_Service service = new org.foi.nwtis.npetanjek.ws.klijenti.AerodromiWS_Service();
        org.foi.nwtis.npetanjek.ws.klijenti.AerodromiWS port = service.getAerodromiWSPort();
        return port.registrirajGrupu(korisnickoIme, korisnickaLozinka);
    }

    private static StatusKorisnika dajStatusGrupe(java.lang.String korisnickoIme, java.lang.String korisnickaLozinka) {
        org.foi.nwtis.npetanjek.ws.klijenti.AerodromiWS_Service service = new org.foi.nwtis.npetanjek.ws.klijenti.AerodromiWS_Service();
        org.foi.nwtis.npetanjek.ws.klijenti.AerodromiWS port = service.getAerodromiWSPort();
        return port.dajStatusGrupe(korisnickoIme, korisnickaLozinka);
    }

    private static Boolean deregistrirajGrupu(java.lang.String korisnickoIme, java.lang.String korisnickaLozinka) {
        org.foi.nwtis.npetanjek.ws.klijenti.AerodromiWS_Service service = new org.foi.nwtis.npetanjek.ws.klijenti.AerodromiWS_Service();
        org.foi.nwtis.npetanjek.ws.klijenti.AerodromiWS port = service.getAerodromiWSPort();
        return port.deregistrirajGrupu(korisnickoIme, korisnickaLozinka);
    }

    private static Boolean aktivirajGrupu(java.lang.String korisnickoIme, java.lang.String korisnickaLozinka) {
        org.foi.nwtis.npetanjek.ws.klijenti.AerodromiWS_Service service = new org.foi.nwtis.npetanjek.ws.klijenti.AerodromiWS_Service();
        org.foi.nwtis.npetanjek.ws.klijenti.AerodromiWS port = service.getAerodromiWSPort();
        return port.aktivirajGrupu(korisnickoIme, korisnickaLozinka);
    }

    private static Boolean blokirajGrupu(java.lang.String korisnickoIme, java.lang.String korisnickaLozinka) {
        org.foi.nwtis.npetanjek.ws.klijenti.AerodromiWS_Service service = new org.foi.nwtis.npetanjek.ws.klijenti.AerodromiWS_Service();
        org.foi.nwtis.npetanjek.ws.klijenti.AerodromiWS port = service.getAerodromiWSPort();
        return port.blokirajGrupu(korisnickoIme, korisnickaLozinka);
    }

    private Message createJMSMessageForjmsNWTiS_npetanjek_1(Session session, Object messageData) throws JMSException {
        // TODO create and populate message to send
        TextMessage tm = session.createTextMessage();
        tm.setText(messageData.toString());
        return tm;
    }

    private void sendJMSMessageToNWTiS_npetanjek_1(Object messageData) throws JMSException, NamingException {
        Context c = new InitialContext();
        ConnectionFactory cf = (ConnectionFactory) c.lookup("java:comp/env/jms/NWTiS_npetanjek_QF");
        Connection conn = null;
        Session s = null;
        try {
            conn = cf.createConnection();
            s = conn.createSession(false, s.AUTO_ACKNOWLEDGE);
            Destination destination = (Destination) c.lookup("java:comp/env/jms/NWTiS_npetanjek_1");
            MessageProducer mp = s.createProducer(destination);
            mp.send(createJMSMessageForjmsNWTiS_npetanjek_1(s, messageData));
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
