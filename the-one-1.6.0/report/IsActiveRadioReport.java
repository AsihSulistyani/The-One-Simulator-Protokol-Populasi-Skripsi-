/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package report;

import core.DTNHost;
import core.Settings;
import core.UpdateListener;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 *
 * @author Asih
 */
public class IsActiveRadioReport extends Report implements UpdateListener {

    /**
     * Record occupancy every nth second -setting id ({@value}). Defines the
     * interval how often (seconds) a new snapshot of buffer occupancy is taken
     * previous:5
     */
    public static final String BUFFER_REPORT_INTERVAL = "occupancyInterval";
    /**
     * Default value for the snapshot interval
     */
    public static final int DEFAULT_BUFFER_REPORT_INTERVAL = 1800;
    private double lastRecord = Double.MIN_VALUE;
    private int interval;
    private Map<DTNHost, ArrayList<Boolean>> bufferCounts = new HashMap<DTNHost, ArrayList<Boolean>>();

    public IsActiveRadioReport() {
        super();

        Settings settings = getSettings();
        if (settings.contains(BUFFER_REPORT_INTERVAL)) {
            interval = settings.getInt(BUFFER_REPORT_INTERVAL);
        } else {
            interval = -1;
            /* not found; use default */
        }

        if (interval < 0) {
            /* not found or invalid value -> use default */
            interval = DEFAULT_BUFFER_REPORT_INTERVAL;
        }
    }

    public void updated(List<DTNHost> hosts) {
        double simTime = getSimTime();
        if (isWarmup()) {
            return;
        }

        if (simTime - lastRecord >= interval) {
            //lastRecord = SimClock.getTime();
            printLine(hosts);
            this.lastRecord = simTime - simTime % interval;
        }
        /**
         * for (DTNHost ho : hosts ) { double temp = ho.getBufferOccupancy();
         * temp = (temp<=100.0)?(temp):(100.0); if
         * (bufferCounts.containsKey(ho.getAddress()))
         * bufferCounts.put(ho.getAddress(),
         * (bufferCounts.get(ho.getAddress()+temp))/2); else
         * bufferCounts.put(ho.getAddress(), temp); } }
         */
    }

    /**
     * Prints a snapshot of the average buffer occupancy
     *
     * @param hosts The list of hosts in the simulation
     */
    private void printLine(List<DTNHost> hosts) {
        /**
         * double bufferOccupancy = 0.0; double bo2 = 0.0;
         *
         * for (DTNHost h : hosts) { double tmp = h.getBufferOccupancy(); tmp =
         * (tmp<=100.0)?(tmp):(100.0); bufferOccupancy += tmp; bo2 +=
         * (tmp*tmp)/100.0; } double E_X = bufferOccupancy / hosts.size();
         * double Var_X = bo2 / hosts.size() - (E_X*E_X)/100.0;
         *
         * String output = format(SimClock.getTime()) + " " + format(E_X) + " "
         * + format(Var_X); write(output);
         */
        for (DTNHost h : hosts) {
            ArrayList<Boolean> bufferList = new ArrayList<Boolean>();
            boolean temp = h.isRadioActive();
//			temp = (temp<=100.0)?(temp):(100.0);
            if (bufferCounts.containsKey(h)) {
                //bufferCounts.put(h, (bufferCounts.get(h)+temp)/2); seems WRONG
                //bufferCounts.put(h, bufferCounts.get(h)+temp);
                //write (""+ bufferCounts.get(h));
                bufferList = bufferCounts.get(h);
                bufferList.add(temp);
                bufferCounts.put(h, bufferList);
            } else {
                bufferCounts.put(h, bufferList);
                //write (""+ bufferCounts.get(h));
            }
        }
    }

    @Override
    public void done() {
        for (Map.Entry<DTNHost, ArrayList<Boolean>> entry : bufferCounts.entrySet()) {
            /*DTNHost a = entry.getKey();
			Integer b = a.getAddress();
			Double avgBuffer = entry.getValue()/updateCounter;*/
            String printHost = "Node " + entry.getKey().getAddress() + "\t";
            for (Boolean bufferList : entry.getValue()) {
                printHost = printHost + "\t" + bufferList;
            }
            write(printHost);
            //write("" + b + ' ' + entry.getValue());
        }
        super.done();
    }

}
