import ij.process.FloatPolygon;

import java.util.Vector;


public class Force {
	
	/**
	 * ID for the type of force
	 */
	final int type;
	/**
	 * Label for the force
	 */
	final String name;
	

	protected float[] weights;
	
	
	public Force(float[] weights, float totalWeight){
		type = -1;
		name = "Null";
		this.weights = weights;
		for (int i=0; i<this.weights.length; i++) this.weights[i] = this.weights[i]*totalWeight;
	}
	
	protected Force(float[] weights, float totalWeight, int type, String name){
		this.type = type;
		this.name = name;
		this.weights = weights;
		for (int i=0; i<this.weights.length; i++) this.weights[i] = this.weights[i]*totalWeight;
	}

	public FloatPolygon getTargetPoints(int btpInd, Vector<BackboneTrackPoint> allBTPs){
		return null;
	}
	

	public float[] getWeights(){
		return weights;
	}
	
	public void getEnergies(BackboneTrackPoint BTP, Vector<BackboneTrackPoint> allBTPs){
		
	}
	
	public void getForces(BackboneTrackPoint BTP, Vector<BackboneTrackPoint> allBTPs){
		
	}
	
	public String getName(){
		return name;
	}
	
}
