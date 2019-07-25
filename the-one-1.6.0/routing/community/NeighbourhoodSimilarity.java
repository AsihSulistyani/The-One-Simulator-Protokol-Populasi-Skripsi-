/*
 * @(#)NeighbourhoodSimilarity.java
 * 
 * Copyright 2017 by Elisabeth Kusuma, University of Sanata Dharma.
 * 
 */
package routing.community;

import core.*;

/**
 *  
 * 
 * @author Elisabeth Kusuma Adi P., University of Sanata Dharma
 */

public class NeighbourhoodSimilarity implements SimilarityCounter {

	public NeighbourhoodSimilarity(Settings s) {	}
	public NeighbourhoodSimilarity(NeighbourhoodSimilarity proto) {	}
	
	public double countSimilarity(double[][] matrixEgoNetwork, double[][] matrixIndirectNode, int index) {

		if (matrixIndirectNode == null) {
			return this.countDirectSimilarity(matrixEgoNetwork, index);
		}
		
		double sim=0;
		
		for (int i = 0; i < matrixEgoNetwork.length; i++) {
			
			if (matrixEgoNetwork[i][0]==1 && matrixIndirectNode[i][index]==1) {
				sim++;				
			}
		}
		
		return sim;
	}
	
	private double countDirectSimilarity(double[][] matrixEgoNetwork, int index) {
		double sim=0;
		
		for (int i = 0; i < matrixEgoNetwork.length; i++) {
			
			if (matrixEgoNetwork[i][0]==1 && matrixEgoNetwork[i][index]==1) {
					sim++;				
			}
		}
		
		return sim;
		
	}
	
	@Override
	public SimilarityCounter replicate() {
		// TODO Auto-generated method stub
		return new NeighbourhoodSimilarity(this);

	}
}
