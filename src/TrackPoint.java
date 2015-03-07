import ij.process.ImageProcessor;

import java.awt.Color;
import java.awt.Point;
import java.awt.Rectangle;
import java.util.ListIterator;
import java.util.Vector;


public class TrackPoint extends Point {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	/**
	 * Unique id for the point
	 */
	int pointID;
	/**
	 * Identitfies the point as a TRACKPOINT
	 */
	final int pointType = 0;
	/**
	 * X location of the point
	 */
	double x;
	/**
	 * y location of the point
	 */
	double y;
	/**
	 * ROI containing this point
	 */
	Rectangle rect;
	/**
	 * Index of the frame containing this point 
	 */
	int frameNum;
	/**
	 * Area inside the countour
	 */
	double area;
	/**
	 * Covariance matrix for the image
	 */
	double[] covariance; 
	/**
	 * Used to generate unique IDs for the TrackPoints
	 * <p> 
	 * Incremented each time a new TrackPoint is made
	 */
	static int lastIDNum=0;
	
	/**
	 * The number of tracks that the point is initially matched to
	 */
	protected int numMatches;
	/**
	 * The track to which this point belongs
	 */
	Track track;
	/*
	 * The threshold value used to find this trackpoint
	 */
	protected int thresh;
	
	
	
	////////////////////////////////////
	// Constructors & Related Methods
	////////////////////////////////////
	
	TrackPoint(double x, double y, Rectangle rect, double area, int frame, int thresh) {
	    init(x, y, rect, area, null, frame, ++lastIDNum, thresh);
	}
//
//	TrackPoint (double x, double y, Rectangle rect, double area,  double[] cov, int frame, int thresh) {
//	     init(x, y, rect, area, cov, frame, ++lastIDNum, thresh);
//	 }
//
//	TrackPoint (double x, double y, Rectangle rect, double area, double[] cov, int frame, int ID, int thresh) {
//	     init(x, y, rect, area, cov, frame, ID, thresh);
//	 }
	
	TrackPoint(TrackPoint point) {
		init(point.x, point.y, (Rectangle)point.rect.clone(), point.area, point.covariance, point.frameNum, ++lastIDNum, thresh);
	}
	/**
	 * Helper method for constructors 
	 */
	public void init(double x, double y, Rectangle rect, double area, double[] cov, int frame, int ID, int thresh){
		this.x = x;
		this.y = y;
		this.rect = rect;
		this.area = area;
		covariance = cov;
		frameNum = frame;
		pointID = ID;
		numMatches = 0;
		this.thresh = thresh;
//		track = null;
		
	}
	
	
	
	/////////////////////
	// Distance Methods
	/////////////////////
	
	/**
	 * Distance squared from given point to this point
	 * @param pt2 TrackPoint for comparison
	 * @return Distance squared from pt2 to this point
	 */
	public double distSquared(TrackPoint pt2){
		return (pt2.x-x)*(pt2.x-x) + (pt2.y-y)*(pt2.y-y);
	}

	 /**
	  * Distance from given point to this point
	  * @param pt2 TrackPoint for comparison
	  * @return Distance from pt2 to this point
	  */
	public double dist(TrackPoint pt2){
		return Math.sqrt(distSquared(pt2));
	}
	
	/**
	 * Gives the point in the given list that is closest to this point
	 * @param list List of TrackPoints to search through
	 * @return Closest point in the list to the query point
	 */
	public TrackPoint nearestInList2Pt(Vector<TrackPoint> list){
		
		if (list.isEmpty()){
			return null;
		}
		
		TrackPoint nearestPt = list.firstElement();
		double shortestDist = java.lang.Double.POSITIVE_INFINITY;//The Point2D (superclass of Point) class apparently overwrites Double   
		ListIterator<TrackPoint> ptListIter = list.listIterator();
		while (ptListIter.hasNext()){
			TrackPoint pt = ptListIter.next();
			double dist = distSquared(pt); 
			if (dist<shortestDist){
				nearestPt = pt;
				shortestDist = dist;
			}
		}
		
		return nearestPt;
	}
	
	/**
	 * Finds nearest points in list to this point, up to NPTS points 
	 * @param list List of TrackPoints to search through
	 * @param nPts Max number of points to return
	 * @return Closest points (up to NPTS) in the list to the query point
	 */
	@SuppressWarnings("unchecked")
	public Vector<TrackPoint> nearestNPts2Pt(Vector<TrackPoint> list, int nPts){
		Vector<TrackPoint> copyList = (Vector<TrackPoint>) list.clone();
		Vector<TrackPoint> nearestPts = new Vector<TrackPoint>(); 
		while(nearestPts.size()<nPts && copyList.size()>0){
			TrackPoint tp = nearestInList2Pt(copyList);
//			if (!copyList.contains(tp)){
				nearestPts.add(tp);
//			}
		
			copyList.remove(tp);
			
		}
		
		return nearestPts;
	}
	
	/**
	 * Angle formed between two points and this point as the vertex
	 * @param ptA
	 * @param ptC
	 * @return Angle formed by the points A(this)C, in radians, from 0 to pi
	 */
	public double VertexAngle(TrackPoint ptA, TrackPoint ptC){
		return Math.acos(((x - ptA.x)*(x - ptC.x) + (y - ptA.y)*(y - ptC.y))/(dist(ptA)*dist(ptC)));
	}
	
	public ImageProcessor getIm(){
		ImageProcessor trPtIm = track.tb.pe.fl.imageStack.getProcessor(frameNum).duplicate();
		int newCornerX = (int)x - track.tb.ep.trackWindowWidth/2;
		int newCornerY = (int)y - track.tb.ep.trackWindowHeight/2;
		Rectangle newRect = new Rectangle(newCornerX, newCornerY, track.tb.ep.trackWindowWidth, track.tb.ep.trackWindowHeight);
		trPtIm.setRoi(newRect);
		trPtIm = trPtIm.crop();
		trPtIm.setColor(Color.WHITE);
//		trPtIm.drawDot(5, 5);
		trPtIm.drawDot((int)x - newCornerX, (int)y - newCornerY);
//		trPtIm.drawRect(rect.x, rect.y, rect.width, rect.height);
		return trPtIm;
		
	}
	
	public void setNumMatches(int num){
		numMatches = num;
	}
	
	public int getNumMatches() {
		return numMatches;
	}
	
	public int getThresh(){
		return thresh;
	}
		
	public void setThresh(int thresh){
		this.thresh = thresh;
	}
	public void setTrack(Track track){
		this.track = track;
	}
	
	public String infoSpill(){
		String s = "pointID "+pointID;
		s += "; frame "+frameNum;
		s += "; ("+(int)x+","+(int)y+")";
		s += "; area "+(int)area;
		return s;
	}
	
	public String getTPDescription(){
		return "ID="+pointID+" ("+(int)x+","+(int)y+")";
	}
	
//	public ImageProcessor getIm( )
	
//	public ImageWindow showTrackPoint(ImageWindow window){
//		return showTrackPoint(window, "Track point "+pointID);
//	}
//	
//	
//	public ImageWindow showTrackPoint(ImageWindow window, String label){
//		ImageProcessor trPtIm = track.tb.pe.imageStack.getProcessor(frameNum).duplicate();
//		trPtIm.setRoi(rect);
//		ImageProcessor crIm = trPtIm.crop();
//		//crIm.resize(crIm.getWidth()*tb.ep.trackZoomFac);
//		ImagePlus img = new ImagePlus("", crIm);
//		track.tb.comm.message("Showing Track point...", VerbLevel.verb_message);
//		if (window==null){
//			img.show();
//			ImageWindow win = img.getWindow();
//			win.setTitle(label);
//			return win;
//
//		}else {
//			window.setImage(img);
//			//window.getCanvas().setMagnification(tb.ep.trackZoomFac);
//			return window;
//		}
//	}
	

	public boolean equals(TrackPoint pt){
		return pt.pointID==pointID;
	}

	
	/**
	 * Generates Serializable forms of any non-serializable ImageJ objects; for basic TrackPoints, nothing is done
	 */
	public void preSerialize(){
		return;
	}
	
	/**
	 * Recreates any non-serializable ImageJ objects; for basic TrackPoints, nothing is done
	 */
	public void postDeserialize(){
		return;
	}
	
	
}
