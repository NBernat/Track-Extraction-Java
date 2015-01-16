import ij.ImagePlus;
import ij.gui.PolygonRoi;
import ij.gui.Roi;
import ij.gui.Wand;
import ij.process.ImageProcessor;

import java.awt.Color;
import java.awt.Point;
import java.awt.Rectangle;
//import java.util.Arrays;
import java.util.Collections;
import java.util.ListIterator;
import java.util.Vector;

//import com.sun.corba.se.impl.orbutil.closure.Constant;
//import com.sun.org.apache.xerces.internal.impl.dv.ValidatedInfo;


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
	
	int minX;
	int minY;
	
	boolean htValid;
	
	private final double maxContourAngle = Math.PI/2.0;
	private final int numMidCoords = 11;
	
	Communicator comm;
	

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

	
	public void setCommunicator(Communicator comm){
		this.comm = comm;
	}
	

	public void extractFeatures(){
		findContours();
		findHTMidline(maxContourAngle, numMidCoords);
	}
	
	public void findContours(){
		comm.message("Finding Contours", VerbLevel.verb_debug);
		ImagePlus thrIm = new ImagePlus("", im.getBufferedImage());//copies image
		ImageProcessor thIm = thrIm.getProcessor();
		thIm.threshold(thresh);
		Wand wand = new Wand(thIm);
		wand.autoOutline(getStart().x-rect.x, getStart().y-rect.y);
		
		nConPts = wand.npoints;
		comm.message("Wand arrays have "+wand.xpoints.length+" points, and report "+nConPts+" points", VerbLevel.verb_debug);
		int[] contourX = wand.xpoints;//Arrays.copyOfRange(wand.xpoints, 0, wand.npoints);//
//		for (int i=0;i<nConPts;i++) contourX[i]+=rect.x; 
		int[] contourY = wand.ypoints;//Arrays.copyOfRange(wand.ypoints, 0, wand.npoints);//
//		for (int i=0;i<nConPts;i++) contourY[i]+=rect.y;
		
		comm.message("Point coords were gathered", VerbLevel.verb_debug);
		contour = new PolygonRoi(contourX, contourY, nConPts, Roi.POLYGON);
		comm.message("Initial polygonRoi was made", VerbLevel.verb_debug);
		contour = new PolygonRoi(contour.getInterpolatedPolygon(1.0, false), Roi.POLYGON);//This makes the spacing between coordinates = 1.0 pixels apart, smoothing=false
		
	}
	


	public void findHTMidline(double maxAngle, int numMidPts){
		
		findHT(maxAngle);
		
//		deriveMidline(numMidPts);
		
	}
	
	public void findHT(double maxAngle){
		///////////////////////////////
		///// Make a list of candidates 
		///////////////////////////////
		
		//Make a list of the points 
		if (comm!=null){
			comm.message("Entering findHT", VerbLevel.verb_debug);
		}
		Vector<ContourPoint> ptList = new Vector<ContourPoint>();
		int ptN = contour.getNCoordinates();
		
		if (comm!=null){
			comm.message("Initially, there are "+ptN+" points", VerbLevel.verb_debug);
		}
		
		for(int ind=0; ind<ptN; ind++){
			if(contour.getXCoordinates()[ind]!=0 || contour.getYCoordinates()[ind]!=0){
				
				ptList.add(new ContourPoint(contour.getXCoordinates()[ind], contour.getYCoordinates()[ind]));
			}
		}
		ptN = ptList.size();
		
		//Link the points to each other, measure the angle between points at the given spacing, and mark whether or not they're in the convex hull
//		PolygonRoi cvxHull = new PolygonRoi(contour.getConvexHull(), Roi.POLYGON);
//		cvxHull = new PolygonRoi(cvxHull.getInterpolatedPolygon(1.0, false), Roi.POLYGON);
//		cvxHull  = new PolygonRoi(cvxHull.getXCoordinates(), cvxHull.getYCoordinates(), cvxHull.getNCoordinates(), Roi.POLYGON);
//		cvxHull = new PolygonRoi(cvxHull.getInterpolatedPolygon(1.0, false), Roi.POLYGON);
		int spacing = ptN/6;
		ContourPoint thisPt;
		int nCan = 0;
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
//			thisPt.sethtCand(cvxHull.contains(thisPt.x, thisPt.y) && thisPt.angle<=maxAngle);
			thisPt.sethtCand(thisPt.angle<=maxAngle);
			if (thisPt.htCand){
				nCan++;
			}
		}
		
		String s;
		if (comm!=null){
			comm.message("After creation, ptList has "+ptList.size()+" points, with "+nCan+" HT candidates", VerbLevel.verb_debug);
			s = "Initial HTCans:";
			for (int i=0; i<ptList.size(); i++){
				if(ptList.get(i).htCand){
					s+="("+ptList.get(i).x+","+ptList.get(i).y+")";
				}
			}
			comm.message(s, VerbLevel.verb_debug);
		}
		
		
		//NOTE: Up until now, the order of the list indicated the order around the perimeter of the contour, so 
		//      ptList.get(ind) could be used to access neighbors.
		//		From here on out, neighbors must be accessed via the linked list
		
		//////////////////////////////////////////////////////
		///// Weed out the points which are not h/t candidates  
		//////////////////////////////////////////////////////
		
		//Sort the points in order of their angles
		Collections.sort(ptList);		
		
		//Remove points from list that are not in the convex hull or that are within (contourLen)/4 points of the top points
		ContourPoint prevPt;
		ContourPoint nextPt;
		spacing = ptN/4;
		ListIterator<ContourPoint> cpIt = ptList.listIterator();
		s="";
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
		
		if (comm!=null){
			comm.message("After processing, ptList has "+ptList.size()+" points", VerbLevel.verb_debug);
			s="HT:";
			for(int i=0; i<ptList.size(); i++){
				s+="PT"+i+"("+ptList.get(i).x+","+ptList.get(i).y+")";
			}
			comm.message(s, VerbLevel.verb_debug);
		}
		htValid = (ptList.size()==2);
		
		if (htValid){
			head = ptList.get(0);
			head.x+=contour.getBounds().x;
			head.y+=contour.getBounds().y;
			tail = ptList.get(1);
			tail.x+=contour.getBounds().x;
			tail.y+=contour.getBounds().y;
			
			if (comm!=null){
				comm.message("Head: ("+head.x+","+head.y+") Tail: ("+tail.x+","+tail.y+")", VerbLevel.verb_debug);
			}
		}
//		else if (ptList.size()==1){
//			
//		}
		
		
		
		if (comm!=null){
			comm.message("Exiting findHT", VerbLevel.verb_debug);
		}
		
	}
	
	public void deriveMidline(int numMidPts){
		
		if(head!=null && tail!=null){
			//Turn each side of the maggot into a polygonRoi 
			//	take the x-coords & y-coords, find indices of h&t
			//  make array for each, create polygonRoi
			
			
			//Interpolate each into numMidPts points (divide by numMidPts+1)
			
			
			//Average the coordinates, one by one
			
		}
		
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
		ImageProcessor cIm = drawFeatures(im);
//		draw
		return CVUtils.padAndCenter(new ImagePlus("Point "+pointID, cIm), track.tb.ep.trackWindowWidth, track.tb.ep.trackWindowHeight, (int)x-rect.x, (int)y-rect.y);
		
	}
	
	
	public ImageProcessor drawFeatures(ImageProcessor grayIm){
		
		ImageProcessor im = grayIm.convertToRGB();
		
		im.setColor(Color.YELLOW);
		im.drawRoi(contour);
		
		im.setColor(Color.GREEN);
		im.drawDot(0, 0);//Top Right
		im.drawDot(rect.width-1, rect.height-1);//Bottom Right
		
		im.setColor(Color.BLUE);
		im.drawDot((int)x-rect.x, (int)y-rect.y);//Center
		im.drawDot(getStart().x-rect.x, getStart().y-rect.y);//First pt in contour algorithm
		
		im.setColor(Color.RED);
		if (head!=null){
			im.drawDot((int)head.x, (int)head.y);
		}
		if (tail!=null){
			im.drawDot((int)tail.x, (int)tail.y);
		}
		
		return im;
	}
	
//	public String infoSpill(){
//		String s = super.infoSpill();
//		for (int i=0; i<contourX.length; i++){
//			s +="\n\t"+"Contour point "+i+": ("+contourX[i]+","+contourY[i]+")";
//		}
//		return s;
//	}
		
}
