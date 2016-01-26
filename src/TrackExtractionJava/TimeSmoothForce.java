package TrackExtractionJava;

import ij.process.FloatPolygon;

import java.util.Arrays;
import java.util.Vector;



public class TimeSmoothForce extends Force {

	
	public TimeSmoothForce(float[] weights, float totalWeight){
		super(weights, totalWeight, 5, "Time-Smooth");
	}
	
	public FloatPolygon getTargetPoints(int btpInd, Vector<BackboneTrackPoint> allBTPs){
			
		BackboneTrackPoint btp = allBTPs.get(btpInd);
		int numBBPts = btp.getNumBBPoints();
	
		float[] targetX = new float[numBBPts];
		Arrays.fill(targetX, 0);
		float[] targetY = new float[numBBPts];
		Arrays.fill(targetY, 0);
		
		if (btpInd>1 && btpInd<(allBTPs.size()-2)){
			for (int k=0; k<btp.getNumBBPoints(); k++){
				targetX[k] = (2.0f/3.0f)*allBTPs.get(btpInd-1).bbOld.xpoints[k];
				targetX[k] +=(2.0f/3.0f)*allBTPs.get(btpInd+1).bbOld.xpoints[k];
				targetX[k] -=(1.0f/6.0f)*allBTPs.get(btpInd-2).bbOld.xpoints[k];
				FloatPolygon bbOld = allBTPs.get(btpInd+2).bbOld;
				targetX[k] -=(1.0f/6.0f)*bbOld.xpoints[k];
				targetY[k] = (2.0f/3.0f)*allBTPs.get(btpInd-1).bbOld.ypoints[k]+(2.0f/3.0f)*allBTPs.get(btpInd+1).bbOld.ypoints[k]-(1.0f/6.0f)*allBTPs.get(btpInd-2).bbOld.ypoints[k]-(1.0f/6.0f)*allBTPs.get(btpInd+2).bbOld.ypoints[k];
			}
		} else if(btpInd==0){
			for (int k=0; k<btp.getNumBBPoints(); k++){
				targetX[k] = 2*allBTPs.get(btpInd+1).bbOld.xpoints[k]-allBTPs.get(btpInd+2).bbOld.xpoints[k];
				targetY[k] = 2*allBTPs.get(btpInd+1).bbOld.ypoints[k]-allBTPs.get(btpInd+2).bbOld.ypoints[k];
			}
		} else if(btpInd==1){
			for (int k=0; k<btp.getNumBBPoints(); k++){
				targetX[k] = .4f*allBTPs.get(btpInd-1).bbOld.xpoints[k]+.8f*allBTPs.get(btpInd+1).bbOld.xpoints[k]-.2f*allBTPs.get(btpInd+2).bbOld.xpoints[k];
				targetY[k] = .4f*allBTPs.get(btpInd-1).bbOld.ypoints[k]+.8f*allBTPs.get(btpInd+1).bbOld.ypoints[k]-.2f*allBTPs.get(btpInd+2).bbOld.ypoints[k];
			}
		}  else if (btpInd==(allBTPs.size()-2)){
			for (int k=0; k<btp.getNumBBPoints(); k++){
				targetX[k] = .4f*allBTPs.get(btpInd+1).bbOld.xpoints[k]+.8f*allBTPs.get(btpInd-1).bbOld.xpoints[k]-.2f*allBTPs.get(btpInd-2).bbOld.xpoints[k];
				targetY[k] = .4f*allBTPs.get(btpInd+1).bbOld.ypoints[k]+.8f*allBTPs.get(btpInd-1).bbOld.ypoints[k]-.2f*allBTPs.get(btpInd-2).bbOld.ypoints[k];
			}
		}else if (btpInd==(allBTPs.size()-1)){
			for (int k=0; k<btp.getNumBBPoints(); k++){
				targetX[k] = 2*allBTPs.get(btpInd-1).bbOld.xpoints[k]-allBTPs.get(btpInd-2).bbOld.xpoints[k];
				targetY[k] = 2*allBTPs.get(btpInd-1).bbOld.ypoints[k]-allBTPs.get(btpInd-2).bbOld.ypoints[k];
			}
		}
		
		return new FloatPolygon(targetX, targetY);
	}
	
}
