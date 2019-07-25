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
public class Average_Headcount extends Report implements UpdateListener {

    public static final String HEADCOUNT_REPORT_INTERVAL = "headcountInterval";
    public static final int DEFAULT_HEADCOUNT_REPORT_INTERVAL = 3600 ; // 

    private double lastRecord = Double.MIN_VALUE;
    private int interval;

    public Average_Headcount() {
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
        double headcount = 0;

        for (DTNHost h : hosts) {
            MessageRouter r = h.getRouter();
            if (!(r instanceof DecisionEngineRouter)) {
                continue;
            }
            RoutingDecisionEngine de = ((DecisionEngineRouter) r).getDecisionEngine();
            Headcount hc = (Headcount) de;
            int temp = hc.getHeadcount();
            headcount += temp;
        }
        double averageHeadcount = headcount / hosts.size();
        String output = format((int) SimClock.getTime()) + " \t " + format(averageHeadcount);
        write(output);

    }

}
