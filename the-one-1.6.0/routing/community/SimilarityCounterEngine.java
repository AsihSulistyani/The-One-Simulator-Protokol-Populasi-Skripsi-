/*
 * @(#)CentralityCounterEngine.java
 *
 * Copyright 2017 by University of Sanata Dharma
 * 
 */
package routing.community;

import java.util.Map;

import core.DTNHost;

/**
 * Declares a RoutingDecisionEngine object to also perform similarity detection
 * in some fashion. This is needed for Betweenness Centrality Reports that need
 * to print out the betweenness centrality value by each node and possibly other 
 * classes that want the community of a given node. 
 * 
 * @author Elisabeth Kusuma Adi P., University of Sanata Dharma
 */
public interface SimilarityCounterEngine
{
	/**
	 * Return each node' Similarity value
	 */
	public Map<DTNHost, Double> getSimilarity();
}
