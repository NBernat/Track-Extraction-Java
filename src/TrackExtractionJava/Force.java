package TrackExtractionJava;

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
	
	static final String defaultName = "Null"; 
	

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
	

	public float getEnergy(int btpInd, Vector<BackboneTrackPoint> allBTPs) {
		//TODO make sure the btps are prepped for getTargetPoints
		return getEnergy(getTargetPoints(btpInd, allBTPs), allBTPs.get(btpInd));
	}
	
	
	public static float getEnergy(FloatPolygon targetBackbone, BackboneTrackPoint btp){
		
		float energy=0;
		float[] bbOldX = btp.bbOld.xpoints;
		float[] bbOldY = btp.bbOld.ypoints;
		float[] targetX = targetBackbone.xpoints;
		float[] targetY = targetBackbone.ypoints;
		for (int i=0; i<btp.bbOld.npoints; i++){
			energy+=(bbOldX[i]-targetX[i])*(bbOldX[i]-targetX[i]);
			energy+=(bbOldY[i]-targetY[i])*(bbOldY[i]-targetY[i]);
		}
		
		return energy;
		
	}
	
	public float[][] getForce(int btpInd, Vector<BackboneTrackPoint> allBTPs){
		//TODO make sure the btps are prepped for getTargetPoints
		return getForce(getTargetPoints(btpInd, allBTPs), allBTPs.get(btpInd));
	}
	
	public static float[][] getForce(FloatPolygon targetBackbone, BackboneTrackPoint btp){
		
		float[] bbOldX = btp.bbOld.xpoints;
		float[] bbOldY = btp.bbOld.ypoints;
		float[] targetX = targetBackbone.xpoints;
		float[] targetY = targetBackbone.ypoints;
		float[][] forces = new float[2][btp.bbOld.npoints];
		for (int i=0; i<btp.bbOld.npoints; i++){
			forces[0][i]+=targetX[i]-bbOldX[i];
			forces[1][i]+=targetY[i]-bbOldY[i];
			
		}
		
		return forces;
		
	}
	
	public String getName(){
		return name;
	}
	
	public boolean prevValid(Vector<BackboneTrackPoint> allBTPs, int ind, int numPrev){
		return ind-numPrev>=0 &&
				ind-numPrev<allBTPs.size() &&
				allBTPs.get(ind-numPrev)!=null && 
				allBTPs.get(ind-numPrev).bbOld!=null && 
				allBTPs.get(ind-numPrev).bbOld.npoints!=0 && 
				!allBTPs.get(ind-numPrev).hidden;// &&
//				!allBTPs.get(ind-numPrev).frozen; //EITHER we don't care about frozen, or we do and it's not frozen
	}
	
	
	public boolean nextValid(Vector<BackboneTrackPoint> allBTPs, int ind, int numNext){
		return prevValid(allBTPs, ind, -numNext);
	}
	
	
}
