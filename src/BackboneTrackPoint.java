import ij.gui.PolygonRoi;

import java.awt.Rectangle;



public class BackboneTrackPoint extends MaggotTrackPoint{
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	/**
	 * Identitfies the point as a BACKBONETRACKPOINT
	 */
	final int pointType = 3;
	
	int[] MagPixX;
	int[] MagPixY;
	int[] MagPixI;
	
	int[] clusterInds;
	int numBBPts;
	
	PolygonRoi backbone;
	
	
	public BackboneTrackPoint(double x, double y, Rectangle rect, double area,
			int frame, int thresh) {
		super(x, y, rect, area, frame, thresh);
	}
	
	protected void setNumBBPts(int num){
		numBBPts = num;
		clusterInds = new int[num];
	}
	
	public void findMagPix(){
		
	}
	
	
	
	
	/**
	 * 
	 * @param mtp
	 * @param numBBPts
	 * @return
	 */
	public static BackboneTrackPoint convertMTPtoBTP(MaggotTrackPoint mtp, int numBBPts){
		
		//Copy all info
		BackboneTrackPoint btp = new BackboneTrackPoint(mtp.x, mtp.y, mtp.rect, mtp.area, mtp.frameNum, mtp.thresh);
		mtp.copyInfoIntoBTP(btp);
		
		
		//Make new BackbonePoint stuff
		btp.setNumBBPts(numBBPts);
		btp.findMagPix();
		
		return btp;
	}
}
