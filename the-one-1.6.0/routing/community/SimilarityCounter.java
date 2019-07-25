/*
 * @(#)SimilarityCounter.java
 * 
 * Copyright 2017 by Elisabeth Kusuma, University of Sanata Dharma.
 * 
 */

package routing.community;

/**
 *  
 * 
 * @author Elisabeth Kusuma Adi P., University of Sanata Dharma
 */

public interface SimilarityCounter
{
	public double countSimilarity(double[][] matrixEgoNetwork, double[][] matrixIndirectNode, int index);
	
	public SimilarityCounter replicate();
}
