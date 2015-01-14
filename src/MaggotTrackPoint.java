import ij.ImagePlus;
import ij.gui.PolygonRoi;
import ij.gui.Roi;
import ij.gui.Wand;
import ij.process.ImageProcessor;

import java.awt.Color;
import java.awt.Point;
import java.awt.Rectangle;


public class MaggotTrackPoint extends ImTrackPoint {




	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	MaggotTrackPoint prev;
	MaggotTrackPoint next;
	

	private Point contourStart;
	int nConPts;
//	int[] contourX;
//	int[] contourY;
	
	PolygonRoi contour;
	
	PolygonRoi midline;
	
	double[] midlineX;
	double[] midlineY;
	
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
		
	}
	


	public void findHTM(){
		
		//Make list of contour points from contour
		
		//Find the convex hull and mark the points in the list
		PolygonRoi cvxHull = new PolygonRoi(contour.getConvexHull(), Roi.POLYGON);
		
		//Find the pointy ends: sort the list of points, take the top in cvh, remove the nearby points from the list, take the next top in cvh  
		
		
		
		//Decide number of re-sampling points by measuring length along contour segments
		
		//Re-sample sides
		
		
		//Average each point
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
