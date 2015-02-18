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
	

	protected double[] weights;
	
	
	public Force(double[] weights){
		type = -1;
		name = "Null";
		this.weights = weights;
	}
	
	protected Force(double[] weights, int type, String name){
		this.type = type;
		this.name = name;
		this.weights = weights;
	}
	
	protected void init(){
		
	}
	
	public FloatPolygon getTargetPoints(int btpInd, Vector<BackboneTrackPoint> allBTPs){
		return null;
	}
	

	public double[] getWeights(){
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
