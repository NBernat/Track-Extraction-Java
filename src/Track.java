import ij.ImagePlus;
import ij.ImageStack;
import ij.process.ImageProcessor;

import java.awt.Rectangle;
import java.util.Collections;
import java.util.ListIterator;
import java.util.Vector;


public class Track {
	
	/**
	 * Constituent TrackPoints
	 */
	Vector<TrackPoint> points;
	/**
	 * Unique identifier for the track 
	 */
	int trackID;
	/**
	 * Used to generate unique IDs for the TrackPoints
	 * <p> 
	 * Incremented each time a new track is made
	 */
	static int lastIDNum=0;
	//isActive?
	
	/**
	 * Maximum ROI height, for playing movies
	 */
	int maxHeight;
	
	/**
	 * Maximum image height, for playing movies
	 */
	int maxWidth;
	
	
	Vector<Boolean> isCollision;
	
//	Vector<Collision> collisions;
	
	
	private TrackMatch match;
	
	/**
	 * Length of time to pause between frames, ie 1/frameRate, in ms
	 */
	int frameLength = 71;//FrameRate=~14fps
	/**
	 * Access to the TrackBuilder
	 */
	TrackBuilder tb;
	
		
	///////////////////////////
	// Constructors
	///////////////////////////	
	public Track(TrackBuilder tb){
		maxHeight=0;
		maxWidth=0;
		
		points = new Vector<TrackPoint>();
		isCollision = new Vector<Boolean>();
//		collisions = new Vector<Collision>(); 
		
		trackID = lastIDNum;
		lastIDNum++;
		this.tb = tb;

	}
	
	
	public Track(TrackPoint firstPt, TrackBuilder tb){
		maxHeight=0;
		maxWidth=0;
		
		points = new Vector<TrackPoint>();
		isCollision = new Vector<Boolean>();
//		collisions = new Vector<Collision>(); 
		
		extendTrack(firstPt);
//		points.add(firstPt);
		
		trackID = lastIDNum;
		lastIDNum++;
		this.tb = tb;
		
	}
	
	///////////////////////////
	// Track building methods
	///////////////////////////	
	
	/**
	 * Adds the given point to the end of the Track
	 * @param pt
	 */
	public void extendTrack(TrackPoint pt){
		points.add(pt);
//		pt.setTrack(this);
		isCollision.addElement(false);
		
		if(pt.rect.height>maxHeight){
			maxHeight = pt.rect.height; 
		}
		
		if(pt.rect.width>maxWidth){
			maxWidth = pt.rect.width; 
		}
		
	}
	
//	public void markCollision(int frameNum, Collision coll){
//		
//		int ptInd = frameNum-points.firstElement().frameNum;
//		
//		isCollision.set(ptInd, true);
//		if (coll!=null){
//			collisions.add(coll);
//		}
//	}
//	
//	public Collision getCollision(int frameNum){
//		for (int i=1; i<=collisions.size(); i++){
//			Collision colli = collisions.get(i);
//			if (colli.startFrame<=frameNum && (frameNum<=colli.endFrame || colli.endFrame<0) ){
//				return colli;
//			}
//		}
//		return null;
//	}
	
	///////////////////////////
	// Distance methods
	///////////////////////////	
	
	/**
	 * Finds the nearest point in a list to the last point in the track 
	 * @param list List of points to search over
	 * @return Nearest point in the list to the last point in the track
	 */
	public TrackPoint nearestPointinList2End(Vector<TrackPoint> list){
		return points.lastElement().nearestInList2Pt(list);
	}
	
	/**
	 * Finds nearest points in a list to the last point in the track, up to NPTS points
	 * @param list List of points to search over
	 * @param nPts Max number of nearest points to find
	 * @return Nearest point in the list to the last point in the track, up to NPTS
	 */
	public Vector<TrackPoint> nearestNPts2End(Vector<TrackPoint> list, int nPts){
		return points.lastElement().nearestNPts2Pt(list, nPts);
	}
	
	/**
	 * Distance from last point in track to query point
	 * @param pt Query point
	 * @return Distance from last point in track to query point
	 */
	public double distFromEnd(TrackPoint pt){
		if ( (pt!=null) && (!points.isEmpty()) ){
			return pt.dist(points.lastElement());
		}
		return -1;
	}
	
	///////////////////////////
	// Area methods
	///////////////////////////	
	/**
	 * Calculates the mean area of the contours in the track
	 * @return Mean area of the contours in the track
	 */
	public double meanArea(){
		double sum = 0;
		ListIterator<TrackPoint> ptIter = points.listIterator();
		while (ptIter.hasNext()){
			sum += ptIter.next().area;
		}
		return ((double)sum)/points.size();
		
	}
	
	/**
	 * Calculates the median area of the contours in the track
	 * @return Median area of the contours in the track
	 */
	public double medianArea(){
		if (points.isEmpty()) {
	        return 0;
	    }
		Vector<Double> areas = new Vector<Double>();
		ListIterator<TrackPoint> ptIter = points.listIterator();
		while (ptIter.hasNext()){
			areas.add((Double)ptIter.next().area);
		}
		Collections.sort(areas);
		return areas.get(areas.size()/2);
		
	}
	
	
	
	///////////////////////////
	// Accessors
	///////////////////////////	
	public TrackPoint getEnd(){
		return points.lastElement();
	}
	
	
	
	//TODO to&from disk?
		
	//TODO accessors: (get first last and nth, length, startFrame, endFrame,trackLen)
	//TODO draw methods
	//TODO playMovie method
	
	
	public void playMovie() {
		playMovie(trackID);
	}
	
	public void playMovie(int labelInd){
		
		tb.comm.message("This track has "+points.size()+"points", VerbLevel.verb_message);
		ListIterator<TrackPoint> tpIt = points.listIterator();
		if (tpIt.hasNext()) {
		
			
			
			
			
			
			//IJ.showMessage("Playing track "+labelInd);
			TrackPoint point = points.firstElement();
			point.setTrack(this);
			
			//Get the first image
			/*
			ImageProcessor trPtIm = tb.pe.imageStack.getProcessor(point.frameNum).duplicate();
			Rectangle newRect = new Rectangle((int)point.x - tb.ep.trackWindowWidth/2, (int)point.y - tb.ep.trackWindowHeight/2, tb.ep.trackWindowWidth, tb.ep.trackWindowHeight);
			trPtIm.setRoi(newRect);
			ImageProcessor crIm = trPtIm.crop();
			
			//Create a stack of images, and add the first frame
			ImageStack trackStack = new ImageStack(tb.ep.trackWindowWidth, tb.ep.trackWindowHeight);
			trackStack.addSlice(crIm);
			*/
			ImageProcessor firstIm = point.getIm();
			
			ImageStack trackStack = new ImageStack(firstIm.getWidth(), firstIm.getHeight());
			
			trackStack.addSlice(firstIm);
			
			//Add the rest of the images to the movie
			while(tpIt.hasNext()){
				point = tpIt.next();
				point.setTrack(this);
				//Get the next image
				/*
				trPtIm = tb.pe.imageStack.getProcessor(point.frameNum).duplicate();
				newRect = new Rectangle((int)point.x - tb.ep.trackWindowWidth/2, (int)point.y - tb.ep.trackWindowHeight/2, tb.ep.trackWindowWidth, tb.ep.trackWindowHeight);
				trPtIm.setRoi(newRect);
				crIm = trPtIm.crop();
				trackStack.addSlice(crIm);
				*/
				trackStack.addSlice(point.getIm());
			}
				
			//Show the stack
			ImagePlus trackPlus = new ImagePlus("Track "+trackID ,trackStack);
			trackPlus.show();
				
			
		}
	}
	
	public static void pause (int time)
	{
	     try                            //opens an exception handling statement
	     {
	          Thread.sleep(time);
	     }
	     catch(InterruptedException e)  //captures the exception
	     {
	     }
	}
	
	
	public String infoString(){
		String info = "";
		
		info += "Track "+trackID+": "+points.size()+" points,";
		if (points.size()!=0){
			info += " frames "+points.firstElement().frameNum+"-"+points.lastElement().frameNum;
		}
		
		for (int i=0; i<points.size(); i++){
			info += "\n Point "+i+": "+points.get(i).infoSpill();
		}
		
		return info;
		
	}
	
	public void setMatch(TrackMatch tm){
		match = tm;
	}
	
	public TrackMatch getMatch(){
		return match;
	}
	
	public boolean isCollisionTrack(){
		return false;
	}
	
		
}
