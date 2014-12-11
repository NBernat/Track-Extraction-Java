import ij.ImagePlus;

import java.awt.Rectangle;


public class ImTrackPoint extends TrackPoint {


	public ImagePlus im;
	
	ImTrackPoint(double x, double y, Rectangle rect, double area, int frame, ImagePlus im) {
		super (x, y, rect, area, frame);
		this.im = im;
	    
	}

	ImTrackPoint(double x, double y, Rectangle rect, double area,  double[] cov, int frame, ImagePlus im) {
	     super(x, y, rect, area, cov, frame, ++lastIDNum);
	     this.im = im;
	 }

	ImTrackPoint(double x, double y, Rectangle rect, double area, double[] cov, int frame, int ID, ImagePlus im) {
		super(x, y, rect, area, cov, frame, ID);
		this.im = im;
	 }
	

	public ImagePlus getIm(){
		return im;
	}
	
	
	

}
