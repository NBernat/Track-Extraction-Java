import ij.ImagePlus;
import ij.gui.PolygonRoi;
import ij.gui.Roi;
import ij.gui.Wand;
import ij.process.ImageProcessor;

import java.awt.Color;
import java.awt.Point;
import java.awt.Rectangle;
import java.util.ListIterator;
import java.util.Vector;

import com.sun.org.apache.xerces.internal.impl.dv.ValidatedInfo;


public class MaggotTrackPoint extends ImTrackPoint {




	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	MaggotTrackPoint prev;
	MaggotTrackPoint next;
	
	private Point contourStart;
	int nConPts;
	
	PolygonRoi contour;
	
	PolygonRoi midline;
	
	Point head;
	Point mid;
	Point tail;
	
	boolean htValid;
	
	

	MaggotTrackPoint(double x, double y, Rectangle rect, double area,
			double[] cov, int frame, int thresh) {
		super(x, y, rect, area, cov, frame, thresh);
	}

	
	MaggotTrackPoint(double x, double y, Rectangle rect, double area,
			double[] cov, int frame, int ID, int thresh) {
		super(x, y, rect, area, cov, frame, ID, thresh);
	}
	
	MaggotTrackPoint(double x, double y, Rectangle rect, double area,
			int frame, int thresh) {
		super(x, y, rect, area, frame, thresh);
	}


	
	
	public void findContours(){
		ImagePlus thrIm = new ImagePlus("", im.getBufferedImage());//copies image
		ImageProcessor thIm = thrIm.getProcessor();
		thIm.threshold(thresh);
		Wand wand = new Wand(thIm);
		wand.autoOutline(getStart().x-rect.x, getStart().y-rect.y);
		nConPts = wand.npoints;
		int[] contourX = wand.xpoints;//Arrays.copyOfRange(wand.xpoints, 0, wand.npoints-1);
//		for (int i=0;i<nConPts;i++) contourX[i]+=rect.x; 
		int[] contourY = wand.ypoints;//Arrays.copyOfRange(wand.ypoints, 0, wand.npoints-1);
//		for (int i=0;i<nConPts;i++) contourY[i]+=rect.y;
		
		contour = new PolygonRoi(contourX, contourY, nConPts, Roi.POLYGON);
		contour = new PolygonRoi(contour.getInterpolatedPolygon(1.0, false), Roi.POLYGON);//This makes the spacing between coordinates = 1.0 pixels apart, smoothing=false
		
	}
	


	public void findHTMidline(double maxAngle, int numMidPts){
		
		findHT(maxAngle);
		
		deriveMidline(numMidPts);
		
		//Decide number of re-sampling points by measuring length along contour segments
		
		//Re-sample sides
		
		
		//Average each point
	}
	
	public void findHT(double maxAngle){
		///////////////////////////////
		///// Make a list of candidates 
		///////////////////////////////
		
		//Make a list of the points 
		Vector<ContourPoint> ptList = new Vector<ContourPoint>();
		int ptN = contour.getNCoordinates();
		for(int ind=0; ind<ptN; ind++){
			ptList.add(new ContourPoint(contour.getXCoordinates()[ind], contour.getYCoordinates()[ind]));
		}
		
		//Link the points to each other, measure the angle between points at the given spacing, and mark whether or not they're in the convex hull
		PolygonRoi cvxHull = new PolygonRoi(contour.getConvexHull(), Roi.POLYGON);
		int spacing = ptN/6;
		ContourPoint thisPt;
		for(int ind=0; ind<ptN; ind++){
			thisPt = ptList.get(ind);
			
			//Link to the points immediately preceding/following the point
			int prevInd = (ptN+ind-1)%ptN;
			int nextInd = (ptN+ind+1)%ptN;
			thisPt.setPrev(ptList.get(prevInd));
			thisPt.setNext(ptList.get(nextInd));
			
			//Measure from the points at a specified spacing apart
			prevInd = (ptN+ind-spacing)%ptN;
			nextInd = (ptN+ind+spacing)%ptN;
			thisPt.measureAngle(ptList.get(prevInd), ptList.get(nextInd));
			
			//Mark whether or not it's in the convex hull, or if the angle is too big
			thisPt.sethtCand(cvxHull.contains((int)thisPt.x, (int)thisPt.y) && thisPt.angle<=maxAngle);
			
		}
		
		//NOTE: Up until now, the order of the list indicated the order around the perimeter of the contour, so 
		//      ptList.get(ind) could be used to access neighbors.
		//		From here on out, neighbors must be accessed via the linked list
		
		//////////////////////////////////////////////////////
		///// Weed out the points which are not h/t candidates  
		//////////////////////////////////////////////////////
		
		//Sort the points in order of their angles
		
		
		//Remove points from list that are not in the convex hull or that are within (contourLen)/4 points of the top points
		ContourPoint prevPt;
		ContourPoint nextPt;
		spacing = ptN/4;
		ListIterator<ContourPoint> cpIt = ptList.listIterator();
		while (cpIt.hasNext()){
			 
			thisPt = cpIt.next();
			
			if (!thisPt.htCand){//if the point is not a candidate, might as well remove it while we're here 
				cpIt.remove();
			} else {//otherwise, mark the specified number of points on either side of the point for removal 
				nextPt = thisPt.nextPt;
				prevPt = thisPt.prevPt;
				for (int i=0; i<spacing; i++){
					
					nextPt.htCand = false;
					prevPt.htCand = false;
					
					nextPt = nextPt.nextPt;
					prevPt = prevPt.prevPt;
					
				}
			}
			
		}
		//Remove any remaining points that are not candidates. 
		//This is necessary because listIterators are fussy about when you can remove an element. But the list should be pretty short by now, so it's not so bad
		cpIt = ptList.listIterator();
		while (cpIt.hasNext()){
			thisPt = cpIt.next();
			
			if (!thisPt.htCand){ 
				cpIt.remove();
			}
		}
		
		
		//////////////////////////////////////////////////////
		///// Assign the head/tail based on the list  
		//////////////////////////////////////////////////////
		htValid = (ptList.size()==2);
		
		if (htValid){
			head = ptList.get(0);
			tail = ptList.get(1);
		}
		else if (ptList.size()==1){
			
		}
		
		
		
		
		
	}
	
	public void deriveMidline(int numMidPts){
		
		
		
		
		
	}
	
	
	
	
	 /* inline void linkBehind(MaggotTrackPoint *prev)
     * inline void linkAhead(MaggotTrackPoint *next)
     * 
     * sets the previous and forward pointers for this MTP
     * linking behind causes the previous point to link ahead, but linking
     * ahead does not cause the next point to link behind
     */
    public void linkBehind(MaggotTrackPoint prev) {
        this.prev = prev;
        if (prev != null) {
            prev.linkAhead(this);
        }
    }
    public void linkAhead(MaggotTrackPoint next) {
        this.next = next;
    }
    
	public void setStart(int stX, int stY){
		contourStart = new Point(stX, stY);
	}

	public Point getStart(){
		return contourStart;
	}
	

	

	

	
	
	
	public ImageProcessor getIm() {
		imOriginX = (int)x-(track.tb.ep.trackWindowWidth/2)-1;
		imOriginY = (int)y-(track.tb.ep.trackWindowHeight/2)-1;
		im.snapshot();
		drawContour(im);
		return CVUtils.padAndCenter(new ImagePlus("Point "+pointID, im), track.tb.ep.trackWindowWidth, track.tb.ep.trackWindowHeight, (int)x-rect.x, (int)y-rect.y);
		
	}
	
	
	public void drawContour(ImageProcessor im){
		
		
		im.setColor(Color.WHITE);
		im.drawRoi(contour);
		
		
		
	}
	
//	public String infoSpill(){
//		String s = super.infoSpill();
//		for (int i=0; i<contourX.length; i++){
//			s +="\n\t"+"Contour point "+i+": ("+contourX[i]+","+contourY[i]+")";
//		}
//		return s;
//	}
		
}
