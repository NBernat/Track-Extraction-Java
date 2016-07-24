package TrackExtractionJava;

import ij.process.FloatPolygon;

import java.util.Arrays;
import java.util.Vector;


/* expands/contracts backbone along lines from center of mass to each point
 * so that the new length of the backbone is targetLength
 * 
 */
public class SpineExpansionForce extends Force{

	static final String defaultName = "Spine-Expansion";
	private float targetLength;
	
	public SpineExpansionForce(float[] weights, float totalWeight, float targetLength){
		super(weights, totalWeight, 2, "Spine-Length");
		this.setTargetLength(targetLength);
	}
	
	
	public FloatPolygon getTargetPoints(int btpInd, Vector<BackboneTrackPoint> allBTPs){
		
		
		
		BackboneTrackPoint btp = allBTPs.get(btpInd);
		
		
		double x[] = MathUtils.castFloatArray2Double(btp.bbOld.xpoints);
		double y[] = MathUtils.castFloatArray2Double(btp.bbOld.ypoints);
		
		double cx = MathUtils.mean(x);
		double cy = MathUtils.mean(y);
		
		double alpha = targetLength/MathUtils.curveLength(x, y);
		
		
		int numBBPts = btp.getNumBBPoints();

		float[] targetX = new float[numBBPts];
		Arrays.fill(targetX, 0);
		float[] targetY = new float[numBBPts];
		Arrays.fill(targetY, 0);
		
		for (int k = 0; k < x.length; ++k) {
			targetX[k] = (float) (alpha*(x[k] - cx) + cx);
			targetY[k] = (float) (alpha*(y[k] - cy) + cy);
		}
		
		
		return new FloatPolygon(targetX, targetY);
	}


	public float getTargetLength() {
		return targetLength;
	}


	public void setTargetLength(float targetLength) {
		this.targetLength = targetLength;
	}
	
	
}
