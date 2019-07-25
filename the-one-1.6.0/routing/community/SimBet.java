/*
 * @(#)SimBet.java
 *
 * Copyright 2017 by University of Sanata Dharma
 * 
 */
package routing.community;

import java.util.*;
import core.*;
import routing.*;

/**
 *  
 * 
 * @author Elisabeth Kusuma Adi P., University of Sanata Dharma
 */

public class SimBet implements RoutingDecisionEngine {

	public static final String CENTRALITY_ALG_SETTING = "centralityAlg";
	public static final String SIMILARITY_SETTING = "similarityAlg";
	public static final String A_SETTING = "a";	

	private Map<DTNHost, Set<DTNHost>> neighboursNode; // menyimpan daftar tetangga dari ego node

	private double[][] matrixEgoNetwork; // menyimpan nilai matrix ego network
	private double[][] indirectNodeMatrix; //menyimpan matrix indirect node
	
	private double betweennessCentrality;// menyimpan nilai betweenness centrality
	
	private double a; //menyimpan konstanta untuk variabel similarity
	
	private ArrayList<DTNHost> directNode; //menyimpan direct node+host => n
	private Set<DTNHost> indirectNode; //menyimpan indirect node => m

	private SimilarityCounter similarity;
	private CentralityDetection centrality;

	public SimBet(Settings s) {

		 if (s.contains(CENTRALITY_ALG_SETTING))
			 this.centrality = (CentralityDetection) s.createIntializedObject(s.getSetting(CENTRALITY_ALG_SETTING));
		 else
			 this.centrality = new BetweennessCentrality(s);

		if (s.contains(SIMILARITY_SETTING))
			this.similarity = (SimilarityCounter) s.createIntializedObject(s.getSetting(SIMILARITY_SETTING));
		else
			this.similarity = new NeighbourhoodSimilarity(s);
		
		this.a = s.getDouble(A_SETTING);
	}

	public SimBet(SimBet proto) {
		
		neighboursNode = new HashMap<DTNHost, Set<DTNHost>>();
		indirectNode= new HashSet<>();
		directNode= new ArrayList<DTNHost>();
		this.a = proto.a;		
		this.centrality = proto.centrality.replicate();
		this.similarity = proto.similarity.replicate();
	}

	@Override
	public void connectionUp(DTNHost thisHost, DTNHost peer) {}

	@Override
	public void connectionDown(DTNHost thisHost, DTNHost peer) {}

	@Override
	public void doExchangeForNewConnection(Connection con, DTNHost peer) {

		DTNHost myHost = con.getOtherNode(peer);
		SimBet de = this.getOtherDecisionEngine(peer);

		if (this.neighboursNode.containsKey(peer)) { // jika node sudah pernah bertemu sebelumnya

			// daftar tetangga dari node peer akan diperbarui
			de.neighboursNode.replace(myHost, this.neighboursNode.keySet());
			this.neighboursNode.replace(peer, de.neighboursNode.keySet());
		}

		else { // jika node baru pertama kali ditemui

			// node baru akan ditambahkan ke dalam daftar tetangga
			// beserta tetangga yang sudah ditemui node peer
			de.neighboursNode.put(myHost, this.neighboursNode.keySet());
			this.neighboursNode.put(peer, de.neighboursNode.keySet());
		}

		this.updateBetweenness(myHost); // mengupdate nilai betweenness
		this.updateSimilarity(myHost); //mengupdate indirect node 
	}

	@Override
	public boolean shouldSendMessageToHost(Message m, DTNHost otherHost) {
		
		SimBet de = getOtherDecisionEngine(otherHost);
		DTNHost dest = m.getTo();
		
		if (isFinalDest(m, otherHost))
			return true;

		//hitung nilai simbet util saya
		double  mySimbetUtil = this.countSimBetUtil(de.getSimilarity(dest),de.getBetweennessCentrality(), 
				this.getSimilarity(dest), this.getBetweennessCentrality());
		
		//hitung nilai simbet util peer saya
		double peerSimBetUtil = this.countSimBetUtil(this.getSimilarity(dest), this.getBetweennessCentrality(), 
				de.getSimilarity(dest), de.getBetweennessCentrality());
		
		/*routing dengan kombinasi similarity & betweenness*/
		if ( peerSimBetUtil > mySimbetUtil)
			 return true;
		else
			 return false;
	}
	

	// ambil nilai similarity ke node dest		
//	@Override
	public double getSimilarity(DTNHost dest) {
		
		int index=0; //digunakan untuk membantu penghitungan index
		
		//cek apakah node dest merupakan node dalam matrix direct-neigh
		if (this.directNode.contains(dest)){	
			for (DTNHost dtnHost : this.directNode) {
				
				if (dtnHost == dest) {
					return this.similarity.countSimilarity(this.matrixEgoNetwork, null , index);
				}
				index++;
			}
		}
		
		//cek apakah node dest merupakan indirect node
		if(this.indirectNode.contains(dest)){
			
			//bangun matrix adjacency indirect node
			this.buildIndirectNodeMatrix(this.neighboursNode, dest); 
		
			//hitung nilai similarity
			return this.similarity.countSimilarity(this.matrixEgoNetwork, this.indirectNodeMatrix , 0);		
		}
		
		return 0;
	}

	// mengambil nilai betweenness yang sudah dihitung
	private double getBetweennessCentrality() {
		return this.betweennessCentrality;
	}
	
	// mengupdate nilai betweenness centrality
	private void updateBetweenness(DTNHost myHost) {
		this.buildEgoNetwork(this.neighboursNode, myHost); // membangun ego network
		//menghitung nilai betweenness centrality
		this.betweennessCentrality = this.centrality.getCentrality(this.matrixEgoNetwork); 
	}

	private void updateSimilarity(DTNHost myHost) {
		
		//simpan data indirect node
		this.indirectNode=this.searchIndirectNeighbours(this.neighboursNode);
	}
	
	private Set<DTNHost> searchIndirectNeighbours(Map<DTNHost, Set<DTNHost>> neighboursNode) {

		// mengambil daftar tetangga yang sudah ditemui secara langsung
		Set<DTNHost> directNeighbours = neighboursNode.keySet();

		// variabel untuk menyimpan daftar node yang belum pernah ditemui secara
		// langsung
		Set<DTNHost> setOfIndirectNeighbours = new HashSet<>();

		for (DTNHost dtnHost : directNeighbours) {

			// mengambil daftar tetangga dari peer yang sudah ditemui langsung
			Set<DTNHost> neighboursOfpeer = neighboursNode.get(dtnHost);

			for (DTNHost dtnHost1 : neighboursOfpeer) {

				// jika dtnHost1 belum pernah ditemui secara langsung
				if (!directNeighbours.contains(dtnHost1)) {

					// cek apakah listOfUndirectNeighbours masih kosong
					if (setOfIndirectNeighbours.isEmpty()) {

						// jika masih kosong masukkan langsung dtnHost1 ke dalam
						// listOfIndirectNeighbours
						setOfIndirectNeighbours.add(dtnHost1);

					} else {// jika listOfUndirectNeighbours tidak kosong

						// cek apakah dtnHost1 sudah pernah dicatat ke dalam
						// listOfindirectNeighbours
						if (!setOfIndirectNeighbours.contains(dtnHost1)) {
							setOfIndirectNeighbours.add(dtnHost1);
						}
					}
				}
			}
		}

		return setOfIndirectNeighbours;
	}
	
	// method yang digunakan untuk membangun matriks indirect neighbour
	private void buildIndirectNodeMatrix(Map<DTNHost, Set<DTNHost>> neighboursNode, DTNHost dest) {
			ArrayList<DTNHost> dummyArrayN = this.directNode;
			
			
			double[][] neighboursAdj = new double[dummyArrayN.size()][1];

			for (int i = 0; i < dummyArrayN.size(); i++) {
				for (int j = 0; j < 1; j++) {
					if (i==0) {
						neighboursAdj[i][j]=0;
					}
					else if (neighboursNode.get(dummyArrayN.get(i)).contains(dest)) {
						neighboursAdj[i][j] = 1;
						
					} else {
						neighboursAdj[i][j] = 0;
						
					}
				}
			}

			this.indirectNodeMatrix = neighboursAdj;
		}

	// method yang digunakan untuk membangun matriks ego network
	private void buildEgoNetwork(Map<DTNHost, Set<DTNHost>> neighboursNode, DTNHost host) {
		ArrayList<DTNHost> dummyArray = buildDummyArray(neighboursNode, host);

		double[][] neighboursAdj = new double[dummyArray.size()][dummyArray.size()];

		for (int i = 0; i < dummyArray.size(); i++) {
			for (int j = i; j < dummyArray.size(); j++) {
				if (i == j) {
					neighboursAdj[i][j] = 0;
				} else if (neighboursNode.get(dummyArray.get(j)).contains(dummyArray.get(i))) {
					neighboursAdj[i][j] = 1;
					neighboursAdj[j][i] = neighboursAdj[i][j];
				} else {
					neighboursAdj[i][j] = 0;
					neighboursAdj[j][i] = neighboursAdj[i][j];
				}
			}
		}

		this.matrixEgoNetwork = neighboursAdj;
	}

	private ArrayList<DTNHost> buildDummyArray(Map<DTNHost, Set<DTNHost>> neighbours, DTNHost myHost) {
		ArrayList<DTNHost> dummyArray = new ArrayList<>();
		dummyArray.add(myHost);
		dummyArray.addAll(neighbours.keySet());
		this.directNode = dummyArray; //mengisi himpunan n pada matrix 
		return dummyArray;
	}
	
	private double countSimBetUtil(double simPeerForDest, double betweennessPeer, double mySimForDest, double myBetweenness ){
		double simBetUtil, simUtilForDest, betUtil;
		
		simUtilForDest=mySimForDest/(mySimForDest+simPeerForDest);
		
		betUtil= myBetweenness/(myBetweenness+betweennessPeer);
		
		simBetUtil = (this.a*simUtilForDest) + ((1-this.a)*betUtil);
		
		return simBetUtil;
	}

	@Override
	public boolean newMessage(Message m) {
		return true;
	}

	@Override
	public boolean isFinalDest(Message m, DTNHost aHost) {
		return m.getTo() == aHost;
	}

	@Override
	public boolean shouldSaveReceivedMessage(Message m, DTNHost thisHost) {
		// TODO Auto-generated method stub
		return m.getTo() != thisHost;
	}

	private SimBet getOtherDecisionEngine(DTNHost otherHost) {
		MessageRouter otherRouter = otherHost.getRouter();
		assert otherRouter instanceof DecisionEngineRouter : "This router only works "
				+ " with other routers of same type";

		return (SimBet) ((DecisionEngineRouter) otherRouter).getDecisionEngine();
	}

	@Override
	public boolean shouldDeleteSentMessage(Message m, DTNHost otherHost) {
		return false;
	}

	@Override
	public boolean shouldDeleteOldMessage(Message m, DTNHost hostReportingOld) {
		return false;
	}

	@Override
	public RoutingDecisionEngine replicate() {
		return new SimBet(this);
	}

}
