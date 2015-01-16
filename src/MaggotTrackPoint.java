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
	
//	PolygonRoi contour;
	Vector<ContourPoint> cont;
	
	PolygonRoi midline;
	
	Point head;
	int headi;
	Point mid;
	Point tail;
	int taili;
	
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
		PolygonRoi contour = new PolygonRoi(contourX, contourY, nConPts, Roi.POLYGON);
		comm.message("Initial polygonRoi was made", VerbLevel.verb_debug);
		contour = new PolygonRoi(contour.getInterpolatedPolygon(1.0, false), Roi.POLYGON);//This makes the spacing between coordinates = 1.0 pixels apart, smoothing=false
//		rect = contour.getBounds();
//		im.setRoi(rect);
//		im = im.crop();
		
		comm.message("Making ContourPoints", VerbLevel.verb_debug);
		int ptN = contour.getNCoordinates();
		cont = new Vector<ContourPoint>();
		for(int ind=0; ind<ptN; ind++){
				cont.add(new ContourPoint(contour.getXCoordinates()[ind]+contour.getBounds().x, contour.getYCoordinates()[ind]+contour.getBounds().y));
//				cont.add(new ContourPoint(contour.getXCoordinates()[ind], contour.getYCoordinates()[ind]));
		}
		
		
		
		
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
		@SuppressWarnings("unchecked")
		Vector<ContourPoint> ptList = (Vector<ContourPoint>) cont.clone();
		int ptN = ptList.size();//contour.getNCoordinates();
		
		if (comm!=null){
			comm.message("Initially, there are "+ptN+" points", VerbLevel.verb_debug);
		}
		
//		for(int ind=0; ind<ptN; ind++){
//			if(contour.getXCoordinates()[ind]!=0 || contour.getYCoordinates()[ind]!=0){
//				
//				ptList.add(new ContourPoint(contour.getXCoordinates()[ind], contour.getYCoordinates()[ind]));
//			}
//		}
//		ptN = ptList.size();
		
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
			headi = cont.indexOf(head);
//			head.x+=contour.getBounds().x;
//			head.y+=contour.getBounds().y;
			tail = ptList.get(1);
			taili= cont.indexOf(tail);
//			tail.x+=contour.getBounds().x;
//			tail.y+=contour.getBounds().y;
			
			if (comm!=null){
				comm.message("Head: i="+headi+"("+head.x+","+head.y+") Tail: i="+taili+"("+tail.x+","+tail.y+")", VerbLevel.verb_debug);
			}
		}
//		else if (ptList.size()==1){
//			
//		}
		
		if (comm!=null){
			comm.message("ptList has "+ptList.size()+" pts, contour has "+cont.size()+"pts", VerbLevel.verb_debug);
		}
		
		if (comm!=null){
			comm.message("Exiting findHT", VerbLevel.verb_debug);
		}
		
	}
	
	public void deriveMidline(int numMidPts){
		
		comm.message("Entering Midline creation", VerbLevel.verb_debug);
		if(cont.get(headi)!=null && cont.get(taili)!=null){
			//Turn each side of the maggot into a polygonRoi 
			//	take the x-coords & y-coords, find indices of h&t
			//  make array for each, create polygonRoi
			int contNum = cont.size();
			int leftNum = (contNum+headi-taili+1)%contNum;
			int rightNum = (contNum+taili-headi+1)%contNum;
			int[] leftX = new int[leftNum];
			int[] leftY = new int[leftNum];
			int[] rightX = new int[rightNum];
			int[] rightY = new int[rightNum];
			for (int i=0; i<leftNum; i++){
				int ind = (contNum+taili+i)%contNum;
				leftX[i] = cont.get(ind).x;
				leftY[i] = cont.get(ind).y;
			}
			for (int i=0; i<rightNum; i++){
				int ind = (contNum+headi+i)%contNum;
				rightX[i] = cont.get(ind).x;
				rightY[i] = cont.get(ind).y;
			}
			

			PolygonRoi leftSeg = new PolygonRoi(leftX, leftY, leftNum, Roi.POLYLINE);
			PolygonRoi rightSeg = new PolygonRoi(leftX, leftY, leftNum, Roi.POLYLINE);
			
			
			//Interpolate each into numMidPts points (divide by numMidPts+1)
			double leftSpacing = ((double)leftNum)/(numMidPts+1);
			double rightSpacing = ((double)rightNum)/(numMidPts+1);
			leftSeg = new PolygonRoi(leftSeg.getInterpolatedPolygon(leftSpacing, true), Roi.POLYLINE);
			rightSeg = new PolygonRoi(rightSeg.getInterpolatedPolygon(rightSpacing, true), Roi.POLYLINE);
			
			comm.message("LeftSeg has "+leftSeg.getNCoordinates()+" points", VerbLevel.verb_debug);
			comm.message("RightSeg has "+rightSeg.getNCoordinates()+" points", VerbLevel.verb_debug);
			
			//Average the coordinates, one by one
			float[] midX;
			float[] midY;
			PolygonRoi midline;
			if (leftSeg.getNCoordinates()==rightSeg.getNCoordinates()){
				midX = new float[leftSeg.getNCoordinates()-2];
				midY = new float[leftSeg.getNCoordinates()-2];
				for (int i=1;i<leftSeg.getNCoordinates()-1; i++){
					midX[i] = (float) ((leftSeg.getXCoordinates()[i]+rightSeg.getXCoordinates()[i])/2.0);
					midY[i] = (float) ((leftSeg.getYCoordinates()[i]+rightSeg.getYCoordinates()[i])/2.0);
				}
				
				midline = new PolygonRoi(midX,  midY, midX.length, Roi.POLYLINE); 
				
				
			} else {
				comm.message("Segments have different numbers of Coordinates!!", VerbLevel.verb_error);
			}
			
			
			
			
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
//		ImageProcessor cIm = drawFeatures(im);
		ImageProcessor pIm = CVUtils.padAndCenter(new ImagePlus("Point "+pointID, im), track.tb.ep.trackWindowWidth, track.tb.ep.trackWindowHeight, (int)x-rect.x, (int)y-rect.y);
		int offX = rect.x-imOriginX;
		int offY = rect.y-imOriginY;
		return drawFeatures(pIm, offX, offY);
		
	}
	
	
	public ImageProcessor drawFeatures(ImageProcessor grayIm, int offX, int offY){
		
		ImageProcessor im = grayIm.convertToRGB();
		
		im.setColor(Color.YELLOW);
		for (int i=0; i<(cont.size()-1); i++){
			im.drawLine(cont.get(i).x+offX, cont.get(i).y+offY, cont.get(i+1).x+offX, cont.get(i+1).y+offY);
		}
		im.drawLine(cont.get(cont.size()-1).x+offX, cont.get(cont.size()-1).y+offY, cont.get(0).x+offX, cont.get(0).y+offY);
//		im.drawRoi(contour);
		
		im.setColor(Color.GREEN);
		im.drawDot(offX, offY);//Top Right
		im.drawDot(rect.width-1+offX, rect.height-1+offY);//Bottom Right
		
		im.setColor(Color.BLUE);
		im.drawDot((int)x-rect.x+offX, (int)y-rect.y+offY);//Center
		im.drawDot(getStart().x-rect.x+offX, getStart().y-rect.y+offY);//First pt in contour algorithm
		
		im.setColor(Color.RED);
		if (head!=null){
			im.drawDot((int)head.x+offX, (int)head.y+offY);
		}
		if (tail!=null){
			im.drawDot((int)tail.x+offX, (int)tail.y+offY);
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
