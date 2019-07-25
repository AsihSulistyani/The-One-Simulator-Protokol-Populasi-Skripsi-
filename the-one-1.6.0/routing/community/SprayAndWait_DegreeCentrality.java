/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package routing.community;

import core.Connection;
import core.DTNHost;
import core.Message;
import core.Settings;
import core.SimClock;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import routing.DecisionEngineRouter;
import routing.MessageRouter;
import routing.RoutingDecisionEngine;
import routing.community.Centrality;
import routing.community.DegreeCentrality;
import routing.community.Duration;

/**
 *
 * @author Asih Sulistyani (Universitas Sanata Dharma)
 * Pada class ini mengimplement class interface yaitu Headcount, dimana class tersebut berisi method 
 * yang nantinya akan dipanggil pada class report.
 * Apabila node bertemu dengan node relay maka akan membandingkan nilai degree centralitynya.
 * Node akan mencatat nilai degree pada method connection down, apabila node bertemu dengan relay tersebut lagi
 * maka akan tetap dicatat 1.
 * Penentuan pengumpul token atau collector yaitu pada mathod doExchangeForNewConnection.
 */
public class SprayAndWait_DegreeCentrality implements RoutingDecisionEngine, Headcount {

    public static final String BINARY_MODE = "binaryMode";
    public static final String SPRAYANDWAIT_NS = "SprayAndWait_DegreeCentrality";
    public static final String MSG_COUNT_PROPERTY = SPRAYANDWAIT_NS + "."
            + "copies";

    public static final String CENTRALITY_ALG_SETTING = "centralityAlg";
    public static final String T_SETTING = "T";
    public static final int DEFAULT_T = 1;

    protected Map<DTNHost, Double> startTimestamps;
    protected Map<DTNHost, List<Duration>> connHistory;

    protected int token;
    protected int headCount;
    protected boolean isBinary;

    protected Centrality centrality;
    
    public SprayAndWait_DegreeCentrality(Settings s) {


        if (s.contains(BINARY_MODE)) {
            isBinary = s.getBoolean(BINARY_MODE);
        } else {
            this.isBinary = false;
        }

        if (s.contains(T_SETTING)) {
            this.token = s.getInt(T_SETTING);
        } else {
            this.token = DEFAULT_T;
        }

        if (s.contains(CENTRALITY_ALG_SETTING)) {
            this.centrality = (Centrality) s.createIntializedObject(s.getSetting(CENTRALITY_ALG_SETTING));
        } else {
            this.centrality = new DegreeCentrality(s);
        }

    }

    public SprayAndWait_DegreeCentrality(SprayAndWait_DegreeCentrality proto) {
        this.isBinary = proto.isBinary;
        this.token = proto.token;
        this.centrality = proto.centrality.replicate();
        this.headCount = proto.headCount;
        connHistory = new HashMap<DTNHost, List<Duration>>();
        startTimestamps = new HashMap<DTNHost, Double>();
    }

    @Override
    public void connectionUp(DTNHost thisHost, DTNHost peer) {
    }

    @Override
    public void connectionDown(DTNHost thisHost, DTNHost peer) {
        double time = startTimestamps.get(peer);
        double etime = SimClock.getTime();

        // Find or create the connection history list
        List<Duration> history;
        if (!connHistory.containsKey(peer)) {
            history = new LinkedList<Duration>();
            connHistory.put(peer, history);
        } else {
            history = connHistory.get(peer);
        }

        // add this connection to the list
        if (etime - time > 0) {
            history.add(new Duration(time, etime));
        }
        startTimestamps.remove(peer);
    }

    @Override
    public void doExchangeForNewConnection(Connection con, DTNHost peer) {
        DTNHost thisHost = con.getOtherNode(peer);
        SprayAndWait_DegreeCentrality de = getOtherSnFDecisionEngine(peer);

        int maxHead = Math.max(this.headCount, de.headCount);

        if (this.token == 0 && de.token == 0) { //cek apakah kedua node memiliki nilai token=0 
            de.headCount = this.headCount = Math.max(this.headCount, de.headCount); //jika ya maka akan mengupdate nilai headcount yang paling tinggi
        } else {
            if (this.getGlobalCentrality() > de.getGlobalCentrality()) { //cek nilai degree this dan de(node relay) yang paling tinggi
                this.token = this.token + de.token; //jika this yang paling tinggi maka tambahkan nilai token
                de.token = 0; //nilai token de menjadi 0
                if (maxHead > this.token) { //apabila this yang menang namun tokennya lebih kecil maka akan dibandingkan dulu token siapa yg lebih tinggi
                    de.headCount = this.headCount = maxHead; 
                } else {
                    de.headCount = this.headCount = this.token;
                }
            } else {
                de.token = de.token + this.token;
                this.token = 0;
                if (maxHead > de.token) {
                    this.headCount = de.headCount = maxHead;
                } else {
                    this.headCount = de.headCount = de.token;
                }
            }
        }

        this.startTimestamps.put(peer, SimClock.getTime());
        de.startTimestamps.put(thisHost, SimClock.getTime());

    }

    @Override
    public boolean newMessage(Message m) {
        m.addProperty(MSG_COUNT_PROPERTY, Counting()); //untuk menentukan copy pesan
        return true;
    }

    @Override
    public boolean isFinalDest(Message m, DTNHost aHost) {
        return m.getTo() == aHost;
    }

    @Override
    public boolean shouldSaveReceivedMessage(Message m, DTNHost thisHost) {
        Integer nrofCopies = (Integer) m.getProperty(MSG_COUNT_PROPERTY);
        if (isBinary) {
            nrofCopies = (int) Math.ceil(nrofCopies / 2.0);
        } else {
            nrofCopies = 1;
        }
        m.updateProperty(MSG_COUNT_PROPERTY, nrofCopies);
        return m.getTo() != thisHost;
    }

    @Override
    public boolean shouldSendMessageToHost(Message m, DTNHost otherHost) {
        if (m.getTo() == otherHost) {
            return true;
        }
        Integer nrofCopies = (Integer) m.getProperty(MSG_COUNT_PROPERTY);
        if (nrofCopies > 1) {
            return true;
        }
        return false;
    }

    @Override
    public boolean shouldDeleteSentMessage(Message m, DTNHost otherHost) {
        Integer nrofCopies = (Integer) m.getProperty(MSG_COUNT_PROPERTY);
        if (isBinary) {
            nrofCopies /= 2;
        } else {
            nrofCopies--;
        }
        m.updateProperty(MSG_COUNT_PROPERTY, nrofCopies);
        return false;
    }

    @Override
    public boolean shouldDeleteOldMessage(Message m, DTNHost hostReportingOld) {
        return m.getTo() == hostReportingOld;
    }

    private SprayAndWait_DegreeCentrality getOtherSnFDecisionEngine(DTNHost h) {
        MessageRouter otherRouter = h.getRouter();
        assert otherRouter instanceof DecisionEngineRouter : "This router only works "
                + " with other routers of same type";

        return (SprayAndWait_DegreeCentrality) ((DecisionEngineRouter) otherRouter).getDecisionEngine();
    }

    private double getGlobalCentrality() { //Mengambil nilai degree centrality nya
        return this.centrality.getGlobalCentrality(this.connHistory);
    }

    @Override
    public RoutingDecisionEngine replicate() {
        return new SprayAndWait_DegreeCentrality(this);
    }

    @Override
    public int getHeadcount() { //Untuk ambil nilai headcount/hasil countingnya
        return this.headCount;
    }

    @Override
    public int getToken() { //Untuk ambil nilai token
        return this.token;
    }

    //Hasil counting nya kemudian dibagi 2
    private int Counting() {
        return this.headCount / 2;
    }
}
