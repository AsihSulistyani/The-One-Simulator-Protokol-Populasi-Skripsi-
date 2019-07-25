/*
 * @(#)CentralityCounterEngine.java
 *
 * Copyright 2017 by University of Sanata Dharma
 * 
 */
package routing.community;

/**
 * Declares a RoutingDecisionEngine object to also perform similarity detection
 * in some fashion. This is needed for Betweenness Centrality Reports that need
 * to print out the betweenness centrality value by each node and possibly other 
 * classes that want the community of a given node. 
 * 
 * @author Elisabeth Kusuma Adi P., University of Sanata Dharma
 */
public interface CentralityCounterEngine
{
	/**
	 * Return each node' BC value
	 */
	public double getBC();
}
