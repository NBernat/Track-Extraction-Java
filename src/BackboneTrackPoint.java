import ij.gui.PolygonRoi;
import ij.process.FloatPolygon;

import java.awt.Point;
import java.awt.Rectangle;
import java.util.Vector;



public class BackboneTrackPoint extends MaggotTrackPoint{
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	/**
	 * Identifies the point as a BACKBONETRACKPOINT
	 */
	final int pointType = 3;
	
	int[] MagPixX;
	int[] MagPixY;
	int[] MagPixI;
	
	int[] clusterInds;
	int numBBPts;
	
	PolygonRoi backbone;
	
	protected transient FloatPolygon bbOld;
	protected transient FloatPolygon bbNew;
	
	private transient Vector<Point> targetPoints;
	
	
	public BackboneTrackPoint(double x, double y, Rectangle rect, double area,
			int frame, int thresh) {
		super(x, y, rect, area, frame, thresh);
	}
	
	
		
	
	
	public void bbRelaxationStep(FittingParameters params, Vector<BackboneTrackPoint> before, Vector<BackboneTrackPoint> after){
		
		findVoronoiClusters();
		
		for (int k=0; k<numBBPts; k++){
			//update the k'th bbCoord with the proper parameters 
			bbCoordRelaxationStep(k, params, before, after);
		}
		
		
	}
	
	private void findVoronoiClusters(){
		//Perform the matrix algebra on MagPixX/MagPixY/MagPixI to determine the voronoi clusters
	}
	
	private void bbCoordRelaxationStep(int bbInd, FittingParameters params, Vector<BackboneTrackPoint> before, Vector<BackboneTrackPoint> after){
		//generate the k'th bbNew Coord using the cluster and the bbOld coords (from this and the before/after points)
		
		//Get the term multipliers

		// Calculate and store each target point
		getTargetPoints();
		
		
		
	}
	
	private void getTargetPoints(){
		
	}
	
	
	protected double calcPointShift(){
		//Calculate the change between the old and new backbones
		return -1.0;
	}
	
	
	
	public void finalizeBackbone(){
		backbone = new PolygonRoi(bbNew, PolygonRoi.POLYLINE);
	}
	
	public void editBBNewPoint(int bbInd, int XorY, float val){
		switch (XorY){
			case 0:
				bbNew.xpoints[bbInd]=val;
				break;
			case 1:
				bbNew.ypoints[bbInd]=val;
				break;
		}
	}
	
	public void editBBOldPoint(int bbInd, int XorY, float val){
		switch (XorY){
			case 0:
				bbOld.xpoints[bbInd]=val;
				break;
			case 1:
				bbOld.ypoints[bbInd]=val;
				break;
		}
	}
	
	public void setBBOld(FloatPolygon replacement){
		bbOld = replacement;
	}
	
	public FloatPolygon getBBOld(){
		return bbOld;
	}

	
	public void setBBNew(FloatPolygon replacement){
		bbNew = replacement;
	}
	
	public FloatPolygon getBBNew(){
		return bbNew;
	}
	
	


	/**
	 * Creates a new BackboneTrackPoint by copying the info in a MaggotTrackPoint
	 * @param mtp The MagggotTrackPoint to be copied
	 * @param numBBPts The number of points in the new Backbone
	 * @return The new BackboneTrackPoint
	 */
	public static BackboneTrackPoint convertMTPtoBTP(MaggotTrackPoint mtp, int numBBPts){
		
		//Copy all info
		BackboneTrackPoint btp = new BackboneTrackPoint(mtp.x, mtp.y, mtp.rect, mtp.area, mtp.frameNum, mtp.thresh);
		mtp.copyInfoIntoBTP(btp);
		
		//Make new BackbonePoint stuff
		btp.setInitialBB(mtp.midline, numBBPts);
		btp.findMagPix();
		
		return btp;
	}
	
	
	private void setInitialBB(PolygonRoi initBB, int num){
		numBBPts = num;
		backbone = (PolygonRoi) initBB.clone();
		if (initBB.getNCoordinates()!=numBBPts){
			 //TODO INTERPOLATE IT 
		}
		//bbOld = initBB;
		//bbNew = initBB;
		
	}
	
	private void findMagPix(){
		//Use the im and thresh to make a list of im points along with their x/y coords and with their Intensities
	}
	
}
