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
	
	public Force(){
		type = -1;
		name = "Null";
	}
	
	public FloatPolygon getTargetPoints(int btpInd, Vector<BackboneTrackPoint> allBTPs){
		return null;
	}
	
	public void getEnergies(BackboneTrackPoint BTP, Vector<BackboneTrackPoint> allBTPs){
		
	}
	
	public void getForces(BackboneTrackPoint BTP, Vector<BackboneTrackPoint> allBTPs){
		
	}
	
	public String getName(){
		return name;
	}
	
}
