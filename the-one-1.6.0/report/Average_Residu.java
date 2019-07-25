/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package report;

import com.sun.xml.internal.ws.api.config.management.policy.ManagementAssertion;
import core.DTNHost;
import core.Settings;
import core.SimClock;
import core.UpdateListener;
import java.util.List;
import routing.DecisionEngineRouter;
import routing.MessageRouter;
import routing.RoutingDecisionEngine;
import routing.community.Headcount;

/**
 *
 * @author Asih
 */
public class Average_Residu extends Report implements UpdateListener {

    public static final String HEADCOUNT_REPORT_INTERVAL = "headcountInterval";
    public static final int DEFAULT_HEADCOUNT_REPORT_INTERVAL = 3600; // 

    private double lastRecord = Double.MIN_VALUE;
    private int interval;

    public Average_Residu() {
        super();
        Settings settings = getSettings();
        if (settings.contains(HEADCOUNT_REPORT_INTERVAL)) {
            interval = settings.getInt(HEADCOUNT_REPORT_INTERVAL);
        } else {
            interval = DEFAULT_HEADCOUNT_REPORT_INTERVAL;
        }
    }

    @Override
    public void updated(List<DTNHost> hosts) {
        if (SimClock.getTime() - lastRecord >= interval) {
            lastRecord = SimClock.getTime();
            printLine(hosts);
        }
    }

    private void printLine(List<DTNHost> hosts) {
//        Settings s = new Settings();
//        int nrofNode1 = s.getInt("Group1.nrofHosts");
//        int nrofNode2 = s.getInt("Group2.nrofHosts");

//        int nrofNode = nrofNode1 + nrofNode2;
        int nrofNode = 96;
        
        int Residu = 0;

        for (DTNHost h : hosts) {
            MessageRouter r = h.getRouter();
            if (!(r instanceof DecisionEngineRouter)) {
                continue;
            }

            RoutingDecisionEngine de = ((DecisionEngineRouter) r).getDecisionEngine();
            Headcount hc = (Headcount) de;
            int temp = hc.getHeadcount();
            if (temp < nrofNode ) {
                Residu++;
            }
        }
        int totalResidu = Residu;
        String output = format((int) SimClock.getTime()) + " \t " + format(totalResidu);
        write(output);

    }

}
