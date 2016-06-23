package TrackExtractionJava;

import ij.process.ImageProcessor;

import java.awt.Color;
import java.awt.Rectangle;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.PrintWriter;
import java.io.Serializable;
import java.util.HashMap;
import java.util.ListIterator;
import java.util.Vector;


public class TrackPoint implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	/**
	 * Unique id for the point
	 */
	protected int pointID;
	/**
	 * Identitfies the point as a TRACKPOINT
	 */
	final static int pointType = 0;
	/**
	 * X location of the point
	 */
	protected double x;
	/**
	 * y location of the point
	 */
	protected double y;
	/**
	 * ROI containing this point
	 */
	protected Rectangle rect;
	/**
	 * Index of the frame containing this point 
	 */
	protected int frameNum;
	/**
	 * Area inside the countour
	 */
	protected double area;
	/**
	 * Covariance matrix for the image
	 */
	protected double[] covariance; 
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
	protected Track track;
	/*
	 * The threshold value used to find this trackpoint
	 */
	protected int thresh;
	

	protected TrackPoint prev;
	protected TrackPoint next;
	
	protected HashMap<String, Double> energies;
	
	////////////////////////////////////
	// Constructors & Related Methods
	////////////////////////////////////
	
	TrackPoint(double x, double y, Rectangle rect, double area, int frame, int thresh) {
	    init(x, y, rect, area, null, frame, ++lastIDNum, thresh);
	}
	
	TrackPoint(TrackPoint point) {
		init(point.x, point.y, (Rectangle)point.rect.clone(), point.area, point.covariance, point.frameNum, ++lastIDNum, thresh);
	}
	
	public TrackPoint(){
		
	}
	
	/**
	 * Helper method for constructors 
	 */
	protected void init(double x, double y, Rectangle rect, double area, double[] cov, int frame, int ID, int thresh){
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
	protected TrackPoint nearestInList2Pt(Vector<TrackPoint> list){
		
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
	protected Vector<TrackPoint> nearestNPts2Pt(Vector<TrackPoint> list, int nPts){
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
	
	protected Vector<ContourPoint> findContours(){
		return null;
	}
	
	protected void convertCPtoArrays(Vector<ContourPoint> c){
		
	}

//	
//	public static Vector<Force> getApplicableForces(FittingParameters fp){
//		Vector<Force> forces = fp.getForces(0);
//		//Remove forces that cannot be applied to this type of point
//		return forces;
//	}
//	
//	protected void calcEnergies(FittingParameters fp){
//		Vector<Force> forces = getApplicableForces(fp);
//		for (Force f : forces){
////			energies.put(f.name, f.getPointEnergy());
//		}
//	}
	
	/**
	 * Angle formed between two points and this point as the vertex
	 * @param ptA
	 * @param ptC
	 * @return Angle formed by the points A(this)C, in radians, from 0 to pi
	 */
	protected double VertexAngle(TrackPoint ptA, TrackPoint ptC){
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
	
	public ImageProcessor getRawIm(){
		ImageProcessor trPtIm = track.tb.pe.fl.imageStack.getProcessor(frameNum).duplicate();
		int newCornerX = (int)x - track.tb.ep.trackWindowWidth/2;
		int newCornerY = (int)y - track.tb.ep.trackWindowHeight/2;
		Rectangle newRect = new Rectangle(newCornerX, newCornerY, track.tb.ep.trackWindowWidth, track.tb.ep.trackWindowHeight);
		trPtIm.setRoi(newRect);
		trPtIm = trPtIm.crop();
		return trPtIm;

	}
	
	public ImageProcessor getIm(MaggotDisplayParameters mdp){
		return getIm();
	}
	
	protected void setNumMatches(int num){
		numMatches = num;
	}
	
	protected int getNumMatches() {
		return numMatches;
	}
		
	protected void setThresh(int thresh){
		this.thresh = thresh;
	}
	protected void setTrack(Track track){
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
		return getTPDescription(-1);
//		return frameNum+" ptID"+pointID+" ("+(int)x+","+(int)y+") A="+area;
	}
	
	public String getTPDescription(int pointNum){
		return String.format("%5d: %5d %8d %13s %10d", pointNum, frameNum, pointID, "("+(int)x+","+(int)y+")", (int)area );
	}
	
	public static String getEmptyCSVinfo(CSVPrefs prefs){
		return getEmptyCSVinfo(prefs, 0, prefs.fieldNames.size()-1);
	}
	
	public static String getEmptyCSVinfo(CSVPrefs prefs, int startInd, int endInd){
		
		String info = "";
		int lastInd = (endInd<prefs.lastInd())? endInd : prefs.lastInd();
		for (int i=startInd; i<=lastInd; i++){
			if (prefs.includeValue[i]) {
				info+=","+prefs.emptyValue.get(i);
			}
		}
		
		return info;
	}
	
	public String getCSVfieldVal(int ind){
		
		
		switch (ind) {
		case 0: 
			return pointID+"";
		case 1:
			return pointType+"";
		case 2: 
			return track.getTrackID()+"";
		case 3: 	
			return x+"";
		case 4: 	
			return y+"";
		case 5: 	
			return rect.x+"";
		case 6: 	
			return rect.y+"";
		case 7: 	
			return rect.width+"";
		case 8: 	
			return rect.height+"";
		case 9: 	
			return area+"";
		case 10: 	
			return thresh+"";
		default: 
			return "";
		
		}
        
	}

	
	public String getCSVinfo(CSVPrefs prefs, boolean includeSubclassFields){
		return CSVinfo(prefs, CSVPrefs.maxInd(getTypeName()));
	}
	
	protected String CSVinfo(CSVPrefs prefs, int maxInd){
		
		
		String info = "";
		for (int i=0; i<=maxInd; i++){
			if (prefs.includeValue[i]){
				info+=","+getCSVfieldVal(i);
			}
			
		}
			
		for(int j=(maxInd+1); j<prefs.numFields(false); j++){
			if (prefs.includeValue[j]){
				info+=","+prefs.getEmptyVal(j);
			}
		}
		
		return info;
		
	}
	
	
	public boolean equals(TrackPoint pt){
		return pt.pointID==pointID;
	}

	
	protected void strip(){
		covariance = null;
	}
	
	/**
	 * Generates Serializable forms of any non-serializable ImageJ objects; for basic TrackPoints, nothing is done
	 */
	protected void preSerialize(){
		return;
	}
	
	/**
	 * Recreates any non-serializable ImageJ objects; for basic TrackPoints, nothing is done
	 */
	protected void postDeserialize(){
		return;
	}
	
	public int toDisk(DataOutputStream dos, PrintWriter pw){
		
		//Write info
		try {
			dos.writeInt(frameNum);
			dos.writeInt(pointID);
			dos.writeDouble(x);
			dos.writeDouble(y);
			dos.writeInt(rect.x);
			dos.writeInt(rect.y);
			dos.writeInt(rect.width);
			dos.writeInt(rect.height);
			dos.writeDouble(area);
			dos.writeInt(thresh);
			
		} catch (Exception e) {
			if (pw!=null) pw.println("Error writing TrackPoint Info for point "+pointID+"; aborting save");
			return 1;
		}
		
		return 0;
	}
	
	public int sizeOnDisk(){
		return 6*Integer.SIZE/Byte.SIZE + 3*java.lang.Double.SIZE/Byte.SIZE;
	}
	
	public static TrackPoint fromDisk(DataInputStream dis, Track t, PrintWriter pw){
		
		TrackPoint tp = new TrackPoint();
		
		if (tp.loadFromDisk(dis,t,pw)==0){
			return tp;
		} else {
			return null;
		}
	}
	
	protected int loadFromDisk(DataInputStream dis, Track t, PrintWriter pw){
		
		track = t;
		pointID = ++lastIDNum;
		
		try {
			frameNum = dis.readInt();
			pointID = dis.readInt();
			x = dis.readDouble();
			y = dis.readDouble();
			rect = new Rectangle(dis.readInt(), dis.readInt(), dis.readInt(), dis.readInt());
			area = dis.readDouble();
			thresh = dis.readInt();
		} catch (Exception e) {
			if (pw!=null) pw.println("Error writing TrackPoint Info");
			return 1;
		}
		
		return 0;
	}
	public double getX(){
		return x;
	}
	
	public double getY(){
		return y;
	}
	
	public int getPointID(){
		return pointID;
	}
	
	public int getFrameNum(){
		return frameNum;
	}
	
	public double getArea(){
		return area;
	}
	
	public int getThresh(){
		return thresh;
	}
	
	public int[] getRect(){
		int[] info = {rect.x, rect.y, rect.width, rect.height}; 
		return info;
	}

	public TrackPoint getPrev(){
		return prev;
	}
	
	public TrackPoint getNext(){
		return next;
	}
	
	public Track getTrack(){
		return track;
	}
	
	public double getEnergy(String energyName){
		return energies.get(energyName);
	}
	
	public HashMap<String, Double> getEnergies(){
		return energies;
	}
	
	public int getPointType(){
		return TrackPoint.pointType;
	}
	public String getTypeName(){
		return "TrackPoint";
	}
}
