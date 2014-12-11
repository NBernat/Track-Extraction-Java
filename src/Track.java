import ij.IJ;
import ij.ImagePlus;
import ij.ImageStack;
import ij.gui.ImageWindow;
import ij.process.ImageProcessor;

import java.awt.Rectangle;
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
	 * Maximum ROI height, for playing movies
	 */
	int maxHeight;
	
	/**
	 * Maximum image height, for playing movies
	 */
	int maxWidth;
	
	
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
	public Track(){
		points = new Vector<TrackPoint>();
		trackID = lastIDNum;
		lastIDNum++;

		maxHeight=0;
		maxWidth=0;
	}
	
	
	public Track(TrackPoint firstPt, TrackBuilder tb){
		points = new Vector<TrackPoint>();
		points.add(firstPt);
		trackID = lastIDNum;
		lastIDNum++;
		this.tb = tb;
		
		maxHeight=0;
		maxWidth=0;
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
		pt.setTrack(this);
		
		if(pt.rect.height>maxHeight){
			maxHeight = pt.rect.height; 
		}
		
		if(pt.rect.width>maxWidth){
			maxWidth = pt.rect.width; 
		}
		
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
	
	
	public void playMovie() {
		playMovie(trackID);
	}
	
	public void playMovie(int labelInd){
		
		tb.comm.message("This track has "+points.size()+"points", VerbLevel.verb_message);
		ListIterator<TrackPoint> tpIt = points.listIterator();
		if (tpIt.hasNext()) {
		
			IJ.showMessage("Playing track"+labelInd);
			TrackPoint point = points.firstElement();
//			ImageWindow window = point.showTrackPoint(null,"Track "+labelInd+": Frames "+points.firstElement().frameNum+"-"+points.lastElement().frameNum);
			
			//Remove vvv
			
			ImageProcessor trPtIm = tb.pe.imageStack.getProcessor(point.frameNum).duplicate();
			trPtIm.setRoi(point.rect);
			ImageProcessor crIm = trPtIm.crop();
			//crIm.resize(crIm.getWidth()*tb.ep.trackZoomFac);
			ImagePlus img = new ImagePlus("", crIm);
			
//			ImageStack trackStack = new ImageStack();
//			trackStack.addSlice(crIm);
//			img.getProcessor().drawDot((int)point.x, (int)point.y);
			
			img.show();
			//Show the rest of the images
			ImageWindow window = img.getWindow();
			window.setTitle("Track "+labelInd+": Frames "+points.firstElement().frameNum+"-"+points.lastElement().frameNum);
			
//			Rectangle rect = window.getBounds(null);
//			window.setBounds(x, y, width, height);
//			window.getCanvas().setMagnification(tb.ep.trackZoomFac);
			
			//Remove ^^^
			
			
			while(tpIt.hasNext()){
				point = tpIt.next();
//				point.showTrackPoint(window, "Track "+labelInd+": Frames "+points.firstElement().frameNum+"-"+points.lastElement().frameNum);
				
				
				//Remove vvv
				trPtIm = tb.pe.imageStack.getProcessor(point.frameNum).duplicate();
				trPtIm.setRoi(point.rect);
				crIm = trPtIm.crop();
//				int centerX = (int)(point.x-point.rect.x);
//				int centerY = (int)(point.y-point.rect.y);
//				trackStack.addSlice(CVUtils.padAndCenter(new ImagePlus("Track "+trackID+" frame "+point.frameNum,crIm), tb.ep.trackImWidth, tb.ep.trackImHeight, centerX, centerY));
				img = new ImagePlus("", crIm);
				window.setImage(img);
				window.getCanvas().setMagnification(tb.ep.trackZoomFac);
//				img.getProcessor().drawDot((int)point.x, (int)point.y);
				//Remove ^^^
				pause(frameLength);
			}
				
//			ImagePlus trackPlus = new ImagePlus("Track "+trackID ,trackStack);
//			trackPlus.show();
				
			
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
