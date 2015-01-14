import ij.ImagePlus;
import ij.gui.Roi;
import ij.process.ImageProcessor;

import java.awt.Rectangle;


public class ImTrackPoint extends TrackPoint{
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	ImageProcessor im;
	int imOriginX;
	int imOriginY;
	
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
	
	public void setImage(ImageProcessor im){
		this.im = im;
	}
	
	public ImageProcessor getIm(){
		//pad the image
//		track.tb.ep.trackWindowHeight;
		imOriginX = (int)x-(track.tb.ep.trackWindowWidth/2)-1;
		imOriginY = (int)y-(track.tb.ep.trackWindowHeight/2)-1;
		return CVUtils.padAndCenter(new ImagePlus("Point "+pointID, im), track.tb.ep.trackWindowWidth, track.tb.ep.trackWindowHeight, (int)x-rect.x, (int)y-rect.y);
		 
	}
	
	public void findAndStoreIm(ImagePlus frameIm){
		Roi oldRoi = frameIm.getRoi();
		frameIm.setRoi(rect);
		im = frameIm.getProcessor().crop();//Does not affect frameIm's image
		frameIm.setRoi(oldRoi);
	}
	
}