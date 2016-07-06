package TrackExtractionJava;

import ij.process.FloatPolygon;

import java.util.Arrays;
import java.util.Vector;



public class TimeLengthForce extends Force {

	static final String defaultName = "Time-Length";
	
	public TimeLengthForce(float[] weights, float totalWeight){
		super(weights, totalWeight, 4, "Time-Length");
	}
	
	
	public FloatPolygon getTargetPoints(int btpInd, Vector<BackboneTrackPoint> allBTPs){
		
		
		BackboneTrackPoint btp = allBTPs.get(btpInd);
		int numBBPts = btp.getNumBBPoints();

		float[] targetX = new float[numBBPts];
		Arrays.fill(targetX, 0);
		float[] targetY = new float[numBBPts];
		Arrays.fill(targetY, 0);
		
		
		//The Diff's account for the differences in 
		
		boolean pv = prevValid(allBTPs, btpInd, 1);
		boolean nv = nextValid(allBTPs, btpInd, 1);
		
		if (pv && nv){//btpInd>0 && btpInd<(allBTPs.size()-1)){
			for (int k=0; k<btp.getNumBBPoints(); k++){
				targetX[k] = .5f*(allBTPs.get(btpInd-1).bbOld.xpoints[k]+allBTPs.get(btpInd+1).bbOld.xpoints[k]);
				targetY[k] = .5f*(allBTPs.get(btpInd-1).bbOld.ypoints[k]+allBTPs.get(btpInd+1).bbOld.ypoints[k]);
			}
		} else if(!pv && nv){//btpInd==0){
			for (int k=0; k<btp.getNumBBPoints(); k++){
				targetX[k] = allBTPs.get(btpInd+1).bbOld.xpoints[k];
				targetY[k] = allBTPs.get(btpInd+1).bbOld.ypoints[k];
			}
		}  else if (pv && !nv){//btpInd==(allBTPs.size()-1)){
			for (int k=0; k<btp.getNumBBPoints(); k++){
				targetX[k] = allBTPs.get(btpInd-1).bbOld.xpoints[k];
				targetY[k] = allBTPs.get(btpInd-1).bbOld.ypoints[k];
			}
		}
		
		
		return new FloatPolygon(targetX, targetY);
	}

	
	
	
}
