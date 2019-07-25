/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package report;

import core.*;
import java.util.*;
import routing.*;
import static routing.DecisionEngineRouter.PUBSUB_NS;
import routing.community.Headcount;
/**
 *
 * @author fans
 */
public class Convergence_TokenPerNodePerTime extends Report implements UpdateListener {

    public static final String INTERVAL_COUNT = "Interval";
    public static final int DEFAULT_INTERVAL1_COUNT = 3600;
    public static final String ENGINE_SETTING = "decisionEngine";
    private double lastRecord = Double.MIN_VALUE;
    private int interval;

    private Map<DTNHost, ArrayList<Integer>> counitngArry = new HashMap<DTNHost, ArrayList<Integer>>();

    public Convergence_TokenPerNodePerTime() {
        super();
        Settings settings = getSettings();
        if (settings.contains(INTERVAL_COUNT)) {
            interval = settings.getInt(INTERVAL_COUNT);
        } else {
            interval = -1;
        }
        if (interval < 0) {
            interval = DEFAULT_INTERVAL1_COUNT;
        }
    }
    
    @Override
    public void updated(List<DTNHost> hosts) {
        double simTime = getSimTime();
        if(isWarmup()){
            return;
        }
            if(simTime - lastRecord >= interval){
            printLine(hosts);
            this.lastRecord = simTime - simTime % interval;
        }
        
    }
    private void printLine( List <DTNHost> hosts){
        for(DTNHost h : hosts){
            MessageRouter r = h.getRouter();
            if (!(r instanceof DecisionEngineRouter)) 
                continue;
            RoutingDecisionEngine de = ((DecisionEngineRouter) r).getDecisionEngine();
            if (!(de instanceof Headcount)) 
                continue;
            Headcount n = (Headcount)de;
            ArrayList <Integer> listHC = new ArrayList<>();
            int temp = n.getToken();
            
            if(counitngArry.containsKey(h)){
                listHC = counitngArry.get(h);
                listHC.add(temp);
                counitngArry.put(h, listHC);
            }else{
                counitngArry.put(h, listHC);
            }
        }
    }
    
    public void done() {
        for (Map.Entry<DTNHost, ArrayList<Integer>> entry : counitngArry.entrySet()) {
            String printHost = "Node " + entry.getKey() + "\t";
            for (Integer countList : entry.getValue()) {
                printHost = printHost + "\t" + countList;
            }
            write(printHost);
//            write(printHost);
        }
        super.done();
    }

}
