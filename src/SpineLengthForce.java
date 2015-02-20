import ij.process.FloatPolygon;

import java.util.Arrays;
import java.util.Vector;



public class SpineLengthForce extends Force{
	
	public SpineLengthForce(float[] weights, float totalWeight){
		super(weights, totalWeight, 2, "Spine-Length");
	}
	
	
	public FloatPolygon getTargetPoints(int btpInd, Vector<BackboneTrackPoint> allBTPs){
		
		BackboneTrackPoint btp = allBTPs.get(btpInd);
		int numBBPts = btp.numBBPts;

		float[] targetX = new float[numBBPts];
		Arrays.fill(targetX, 0);
		float[] targetY = new float[numBBPts];
		Arrays.fill(targetY, 0);
		
		
		//Set head end point 
		int k=0;
		targetX[k] = btp.bbOld.xpoints[k+1];
		targetY[k] = btp.bbOld.ypoints[k+1];
		//Set middle points
		for (k=1; k<(btp.numBBPts-1); k++){
			targetX[k] = .5f*(btp.bbOld.xpoints[k-1]+btp.bbOld.xpoints[k+1]);
			targetY[k] = .5f*(btp.bbOld.ypoints[k-1]+btp.bbOld.ypoints[k+1]);
		}
		//Set tail end point
		k=(btp.numBBPts-1);
		targetX[k] = btp.bbOld.xpoints[k-1];
		targetY[k] = btp.bbOld.ypoints[k-1];
		
		return new FloatPolygon(targetX, targetY);
	}
	
	
}
