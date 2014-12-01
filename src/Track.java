import ij.ImagePlus;
import ij.gui.ImageWindow;

import java.util.Collections;
import java.util.Iterator;
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
	 * Length of time to pause between frames, ie 1/frameRate, in ms
	 */
	int frameLength = 71;//FrameRate=~14fps
	
		
	///////////////////////////
	// Constructors
	///////////////////////////	
	public Track(){
		points = new Vector<TrackPoint>();
		trackID = lastIDNum;
		lastIDNum++;
	}
	
	
	public Track(TrackPoint firstPt){
		points = new Vector<TrackPoint>();
		points.add(firstPt);
		trackID = lastIDNum;
		lastIDNum++;
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
	}
	
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
	
	public void playMovie(){
		
		ListIterator<TrackPoint> tpIt = points.listIterator();
		if (tpIt.hasNext()) {
			ImagePlus firstIm = tpIt.next().getIm();
			firstIm.show();
			ImageWindow window = firstIm.getWindow(); 
			while(tpIt.hasNext()){
				window.updateImage(tpIt.next().getIm());
				pause(frameLength);
			}
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
	
	
		
}
