package TrackExtractionJava;

import ij.process.FloatPolygon;

import java.util.Arrays;
import java.util.Vector;



public class ImageForce extends Force {


	static final String defaultName = "Image"; 
	
	private float[] persistentWeights;
	
	public ImageForce(float[] weights, float totalWeight){
		super(weights, totalWeight, 1,"Image");
		persistentWeights = weights.clone();
	}
	
	public FloatPolygon getTargetPoints(int btpInd, Vector<BackboneTrackPoint> allBTPs){
		
		weights = persistentWeights.clone();
		
		BackboneTrackPoint btp = allBTPs.get(btpInd);
		int numBBPts = btp.getNumBBPoints();
		
		float[] targetX = new float[numBBPts];
		Arrays.fill(targetX, 0);
		float[] targetY = new float[numBBPts];
		Arrays.fill(targetY, 0);
		
		float[] norm = new float[numBBPts];
		Arrays.fill(norm, 0);

		//Sum the coordinates and build the normalization factor from the weights
		if (btp.getClusterMethod()==0){
			for (int pix=0; pix<btp.getNumPix(); pix++){
				int k=btp.getClusterInds(pix);
				
				if (k>=0){
					targetX[k] += btp.getMagPixI(pix)*(btp.getMagPixX(pix));
					targetY[k] += btp.getMagPixI(pix)*(btp.getMagPixY(pix));
					norm[k] += btp.getMagPixI(pix);
				} else {
					
				}
			}
		} else {
			for (int pix=0; pix<btp.getNumPix(); pix++){
				for (int k=0; k<numBBPts; k++){
					targetX[k] += btp.getMagPixI(pix)*(btp.getMagPixX(pix))*(btp.getClusterWeight(k, pix));
					targetY[k] += btp.getMagPixI(pix)*(btp.getMagPixY(pix))*(btp.getClusterWeight(k, pix));
					norm[k] += btp.getMagPixI(pix)*(btp.getClusterWeight(k, pix));
				}
			}
		}
		
		//Normalize the coordinates 
		//int numOut = 0 ;
		for (int k=0; k<numBBPts; k++){
			if (norm[k]!=0){
				targetX[k] = targetX[k]/norm[k];
				targetY[k] = targetY[k]/norm[k];
			} else {
				//if no cluster associated with point, move to nearest contour point and reduce image weight. 
				double alpha = 0.1;
				int[] ncp = btp.getNearestContourPoint(btp.bbOld.xpoints[k], btp.bbOld.ypoints[k]);
				targetX[k] = ncp[0];
				targetY[k] = ncp[1];
				weights[k] *= alpha;
				// numOut++; //for alternate method below
				
			}
		}
		/* alternate method - use nearest cluster location
		//Move target points back in the maggot region
		float fracToCluster = 0.2f;
		for (int k=0; numOut>0 && k<numBBPts; k++){
			if (targetX[k]==0 && targetY[k]==0){
				
				//Find the nearest cluster to the location of the point
				int kk = findNearestCluster(btp, k, targetX, targetY);
				float[] clst = new float[2];
				if (kk>=0){
					clst[0] = targetX[kk];
					clst[1] = targetY[kk];
				} else {
					//There should always be a closest cluster, but just in case...
					System.out.println("WARNING Image target coord assigned to COM: track"+btp.track.getTrackID()+" pt "+btp.pointID+" crd "+k);
					clst = btp.getCOM(); 
				}
				
				//Move target point towards nearest nearest cluster
				targetX[k] = fracToCluster*clst[0] + (1-fracToCluster)*btp.bbOld.xpoints[k];
				targetY[k] = fracToCluster*clst[1] + (1-fracToCluster)*btp.bbOld.ypoints[k];
				
				numOut--;
			}
		}
		*/
		
		
		return new FloatPolygon(targetX, targetY);
	}
	
	/**
	 * Method for finding nearest cluster optimized to ImageEnergy's targetPoint calculations
	 * @param btp
	 * @param crdInd
	 * @param targetX
	 * @param targetY
	 * @return
	 */
	private int findNearestCluster(BackboneTrackPoint btp, int crdInd, float[] targetX, float[] targetY){
		
		float[] crdLoc = {btp.bbOld.xpoints[crdInd], btp.bbOld.ypoints[crdInd]};
		
		int nearest = -1;
		float mindist = Float.POSITIVE_INFINITY;
		//Check each target point (i.e. com of cluster) to 
		for (int k=0; k<targetX.length; k++){
			
			float[] clstLoc = {targetX[k], targetY[k]};
			
			if (crdInd!=k && clstLoc[0]!=0 && clstLoc[1]!=0){
				float dist = (crdLoc[0]-clstLoc[0])*(crdLoc[0]-clstLoc[0]);
				dist += (crdLoc[1]-clstLoc[1])*(crdLoc[1]-clstLoc[1]);
				if (dist<mindist){
					mindist = dist;
					nearest = k;
				}
			}
		}
		return nearest;
	}
	
	
}
