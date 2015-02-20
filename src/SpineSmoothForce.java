import ij.process.FloatPolygon;

import java.util.Arrays;
import java.util.Vector;



public class SpineSmoothForce extends Force{

	
	public SpineSmoothForce(float[] weights, float totalWeight){
		super(weights, totalWeight, 3, "Spine-Smooth");
	}
	
	public FloatPolygon getTargetPoints(int btpInd, Vector<BackboneTrackPoint> allBTPs){
			
		BackboneTrackPoint btp = allBTPs.get(btpInd);
		int numBBPts = btp.numBBPts;
	
		float[] targetX = new float[numBBPts];
		Arrays.fill(targetX, 0);
		float[] targetY = new float[numBBPts];
		Arrays.fill(targetY, 0);
		
			
		//Set head end points
		int k=0;
		targetX[k] = 2*btp.bbOld.xpoints[k+1]-btp.bbOld.xpoints[k+2];
		targetY[k] = 2*btp.bbOld.ypoints[k+1]-btp.bbOld.ypoints[k+2];
		k=1;
		targetX[k] = .4f*btp.bbOld.xpoints[k-1]+.8f*btp.bbOld.xpoints[k+1]-.2f*btp.bbOld.xpoints[k+2];
		targetY[k] = .4f*btp.bbOld.ypoints[k-1]+.8f*btp.bbOld.ypoints[k+1]-.2f*btp.bbOld.ypoints[k+2];
		//Set middle points
		for (k=2; k<(btp.numBBPts-2); k++){
			targetX[k] = (2.0f/3.0f)*btp.bbOld.xpoints[k-1]+(2.0f/3.0f)*btp.bbOld.xpoints[k+1]-(1.0f/6.0f)*btp.bbOld.xpoints[k-2]-(1.0f/6.0f)*btp.bbOld.xpoints[k+2];
			targetY[k] = (2.0f/3.0f)*btp.bbOld.ypoints[k-1]+(2.0f/3.0f)*btp.bbOld.ypoints[k+1]-(1.0f/6.0f)*btp.bbOld.ypoints[k-2]-(1.0f/6.0f)*btp.bbOld.ypoints[k+2];
		}
		//Set tail end points
		k=(btp.numBBPts-2);
		targetX[k] = .4f*btp.bbOld.xpoints[k+1]+.8f*btp.bbOld.xpoints[k-1]-.2f*btp.bbOld.xpoints[k-2];
		targetY[k] = .4f*btp.bbOld.ypoints[k+1]+.8f*btp.bbOld.ypoints[k-1]-.2f*btp.bbOld.ypoints[k-2];
		k=(btp.numBBPts-1);
		targetX[k] = 2*btp.bbOld.xpoints[k-1]-btp.bbOld.xpoints[k-2];
		targetY[k] = 2*btp.bbOld.ypoints[k-1]-btp.bbOld.ypoints[k-2];
		
		return new FloatPolygon(targetX, targetY);
	}
	
	
}
