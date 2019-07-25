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
import routing.DecisionEngineRouter;
import routing.MessageRouter;
import routing.RoutingDecisionEngine;

/**
 *
 * @author Asih Sulistyani (Universitas Sanata Dharma)
 * Pada class ini mengimplement class interface yaitu Headcount, dimana class tersebut berisi method 
 * yang nantinya akan dipanggil pada class report.
 * Apabila node bertemu dengan node relay maka akan melakukan pemilihan nilai random atau acak untuk
 * menentukan pengumpul token atau collector. Penentuan ini ada pada method doExchangeForNewConnection.
 */
public class SprayAndWait_Random implements RoutingDecisionEngine, Headcount {

    public static final String BINARY_MODE = "binaryMode";
    public static final String SPRAYANDWAIT_NS = "SprayAndWait_Random";
    public static final String MSG_COUNT_PROPERTY = SPRAYANDWAIT_NS + "."
            + "copies";
    public static final String T_SETTING = "T";
    public static final int DEFAULT_T = 1;
    protected int token;
    protected int headCount;
    public boolean isBinary;

    public SprayAndWait_Random(Settings s) {
        if (s.contains(BINARY_MODE)) {
            isBinary = s.getBoolean(BINARY_MODE);
        } else {
            this.isBinary = false;
        }
        if (s.contains(T_SETTING)) {
            token = s.getInt(T_SETTING);
        } else {
            token = DEFAULT_T;
        }
    }

    public SprayAndWait_Random(SprayAndWait_Random sw) {
        this.token = sw.token;
        this.isBinary = sw.isBinary;
    }

    @Override
    public void connectionUp(DTNHost thisHost, DTNHost peer) {
    }

    @Override
    public void connectionDown(DTNHost thisHost, DTNHost peer) {
    }

    @Override
    public void doExchangeForNewConnection(Connection con, DTNHost peer) {
        DTNHost thisHost = con.getOtherNode(peer);
        SprayAndWait_Random partner = getOtherSnFDecisionEngine(peer);
        double h = Math.random();
        double p = Math.random();

        int headMax = Math.max(partner.headCount, this.headCount); 

        if (this.token == 0 && partner.token == 0) { //cek apakah kedua node memiliki nilai token=0 
            partner.headCount = this.headCount = Math.max(partner.headCount, this.headCount); //jika ya maka akan mengupdate nilai headcount yang paling tinggi
        } else {
           
            if (h > p) { //Melakukan pemilihan random atau acak untuk menentukan siapa yang menjadi pengumpul token
                this.token = this.token + partner.token; //jika this yang paling tinggi maka tambahkan nilai token
                partner.token = 0; //partner menjadi 0
                if (headMax > this.token) { //apabila this yang menang namun tokennya lebih kecil maka akan dibandingkan dulu token siapa yg lebih tinggi
                    partner.headCount = this.headCount = headMax;
                } else {
                    partner.headCount = this.headCount = this.token;
                }
            } else {
                partner.token = partner.token + this.token;
                this.token = 0;
                if (headMax > partner.token) {
                    this.headCount = partner.headCount = headMax;
                } else {
                    this.headCount = partner.headCount = partner.token;
                }
            }
        }
    }

    @Override
    public boolean newMessage(Message m) {
        m.addProperty(MSG_COUNT_PROPERTY, Counting());
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
        if (m.getTo() == otherHost) {
            return false;
        }
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

    @Override
    public RoutingDecisionEngine replicate() {
        return new SprayAndWait_Random(this);
    }

    private SprayAndWait_Random getOtherSnFDecisionEngine(DTNHost h) {
        MessageRouter otherRouter = h.getRouter();
        assert otherRouter instanceof DecisionEngineRouter : "This router only works "
                + " with other routers of same type";

        return (SprayAndWait_Random) ((DecisionEngineRouter) otherRouter).getDecisionEngine();
    }

    @Override
    public int getHeadcount() { //untuk mengambil nilai countingnya
        return this.headCount;
    }

    public int Counting() { //nilai counting atau headcount dibagi 2 untuk menentukan copy pesan
        return this.headCount / 2;
    }

    @Override
    public int getToken() { //untuk mengambil nilai token
        return this.token;
    }

}
