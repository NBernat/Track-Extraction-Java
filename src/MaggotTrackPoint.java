import ij.ImagePlus;
import ij.gui.Overlay;
import ij.gui.PolygonRoi;
import ij.gui.Roi;
import ij.gui.Wand;
import ij.process.ImageProcessor;

import java.awt.Color;
import java.awt.Point;
import java.awt.Rectangle;
import java.util.Vector;


public class MaggotTrackPoint extends ImTrackPoint {




	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	MaggotTrackPoint prev;
	MaggotTrackPoint next;
	
//	Vector<Point> contour;
	private Point contourStart;
	int[] contourX;
	int[] contourY;
	Vector<Point> midline;
	
	Point head;
	Point mid;
	Point tail;
	
	boolean htValid;
	
	

	MaggotTrackPoint(double x, double y, Rectangle rect, double area,
			double[] cov, int frame, int thresh) {
		super(x, y, rect, area, cov, frame, thresh);
		// TODO Auto-generated constructor stub
	}

	
	MaggotTrackPoint(double x, double y, Rectangle rect, double area,
			double[] cov, int frame, int ID, int thresh) {
		super(x, y, rect, area, cov, frame, ID, thresh);
		// TODO Auto-generated constructor stub
	}
	
	MaggotTrackPoint(double x, double y, Rectangle rect, double area,
			int frame, int thresh) {
		super(x, y, rect, area, frame, thresh);
		// TODO Auto-generated constructor stub
	}

//	MaggotTrackPoint(TrackPoint point) {
//		super(point);
//	}
	
	
	public void findContours(){
		ImagePlus thrIm = new ImagePlus("", im.getBufferedImage());//copies image
		ImageProcessor thIm = thrIm.getProcessor();
		thIm.threshold(thresh);
		Wand wand = new Wand(thIm);
		wand.autoOutline(getStart().x-rect.x, getStart().y-rect.y);
//		contour = wand2Contour(wand);
		contourX = wand.xpoints;
		for (int i=0;i<contourX.length;i++) contourX[i]+=rect.x; 
		contourY = wand.ypoints;
		for (int i=0;i<contourY.length;i++) contourY[i]+=rect.y;
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
	
	
	
//	public Vector<Point> wand2Contour(Wand wand){
//		
//		Vector<Point> con = new Vector<Point>();
//		
//		for (int i=0; i<wand.npoints; i++){
//			con.add(new Point(wand.xpoints[i], wand.ypoints[i]));
//		}
//		
//		return con;
//	}
	
	
	
//	public Overlay contourOverlay(){
//		
//		PolygonRoi pRoi = new PolygonRoi(contourX, contourY, contourX.length, Roi.POLYLINE);
//		return new Overlay(pRoi);
//		
//	}
	
	@Override
	public ImageProcessor getIm() {
		ImageProcessor im = super.getIm();
		drawContour(im);
		return im;
	}
	
	
	public void drawContour(ImageProcessor im){
		
		
		im.setColor(Color.WHITE);
		
		for (int i=0; i<(contourX.length-1); i++){
			im.drawLine(contourX[i]-imOriginX, contourY[i]-imOriginY, contourX[i+1]-imOriginX, contourY[i+1]-imOriginY);
		}
		if (contourX.length>0){
			im.drawLine(contourX[contourX.length-1]-imOriginX, contourY[contourY.length-1]-imOriginY, contourX[1]-imOriginX, contourY[1]-imOriginY);			
		}
		
		
	}
	
	
	
//	
//	public int[] findInterior(ImageProcessor im){
//		//find an interior point
//		int startCoord[] = {(int)x,(int)y};
//		int inCoord[];
//		
//		
//		int numSteps = 5;
//		int[] incr = {1,-1};
//		
//		
//		//Check the NUMSTEPS pixels on each of the four sides of the center 
//		for (int i=0; i<2; i++){//i selects x or y coord for incrementing
//			
//			for(int j=0; j<2; j++){//j selects direction of increment
//				inCoord = startCoord.clone();
//				
//				//Increment the pixel loc (up to numSteps times) and check for an interior pixel
//				int count = 0;
//				while(count<numSteps){
//					count++;
//					inCoord[i] = inCoord[i]+incr[j]; 
//					
//					if(im.getPixel(inCoord[0],inCoord[0])>thresh){
//						return inCoord;
//					}
//					
//				}
//			}
//		}
//		
//		
//		//check all the pixels in the bounding box
//		
//		
//		return {-1, -1};
//		
//	}
//	
	
	
	
	
	
	
	
	
	
	
}
