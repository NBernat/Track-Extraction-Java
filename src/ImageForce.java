import ij.process.FloatPolygon;

import java.util.Arrays;
import java.util.Vector;



public class ImageForce extends Force {

	
	public ImageForce(float[] weights, float totalWeight){
		super(weights, totalWeight, 1,"Image");
		
	}
	
	public FloatPolygon getTargetPoints(int btpInd, Vector<BackboneTrackPoint> allBTPs){
		
		BackboneTrackPoint btp = allBTPs.get(btpInd);
		int numBBPts = btp.numBBPts;
		
		float[] targetX = new float[numBBPts];
		Arrays.fill(targetX, 0);
		float[] targetY = new float[numBBPts];
		Arrays.fill(targetY, 0);
		
		int[] norm = new int[numBBPts];
		Arrays.fill(norm, 0);

		//Calculate the center of mass of each backbone cluster
		
		//Sum the coordinates and build the normalization factor from the weights
		for (int pix=0; pix<btp.getNumPix(); pix++){
			int k=btp.getClusterInds(pix);
			
			targetX[k] += btp.getMagPixI(pix)*(btp.getMagPixX(pix));
			targetY[k] += btp.getMagPixI(pix)*(btp.getMagPixY(pix));
			norm[k] += btp.getMagPixI(pix);
		}
		//Normalize the coordinates
		for (int k=0; k<numBBPts; k++){
			targetX[k] = targetX[k]/norm[k];
			targetY[k] = targetY[k]/norm[k];
		}
		
		
		return new FloatPolygon(targetX, targetY);
	}
	
}
