import ij.ImagePlus;
import ij.gui.Roi;

import java.awt.Rectangle;


public class ImTrackPoint extends TrackPoint{
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	ImagePlus im;
	
//	int[] imOrigin;

	ImTrackPoint(double x, double y, Rectangle rect, double area, int frame,
			int thresh) {
		super(x, y, rect, area, frame, thresh);
	}

	ImTrackPoint(double x, double y, Rectangle rect, double area, double[] cov,
			int frame, int ID, int thresh) {
		super(x, y, rect, area, cov, frame, ID, thresh);
	}

	ImTrackPoint(double x, double y, Rectangle rect, double area, double[] cov,
			int frame, int thresh) {
		super(x, y, rect, area, cov, frame, thresh);
	}
	
	ImTrackPoint(TrackPoint point, ImagePlus frameIm){
		super(point);
		findAndStoreIm(frameIm);
	}
	
	public void setImage(ImagePlus im){
		this.im = im;
	}
	
	public void findAndStoreIm(ImagePlus frameIm){
		Roi oldRoi = frameIm.getRoi();
		frameIm.setRoi(rect);
		im = new ImagePlus("Frame "+pointID, frameIm.getProcessor().crop());//Does not affect frameIm's image
		frameIm.setRoi(oldRoi);
	}
	
}
