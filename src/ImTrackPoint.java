import ij.ImagePlus;
import ij.gui.Roi;
import ij.io.FileSaver;
import ij.io.Opener;
import ij.process.ImageProcessor;

import java.awt.Rectangle;
import java.io.DataOutputStream;
import java.io.PrintWriter;


public class ImTrackPoint extends TrackPoint{
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	transient ImageProcessor im;
	byte[] serializableIm;
	int imOriginX;
	int imOriginY;
	
	protected int trackWindowWidth;
	protected int trackWindowHeight;
	
	/**
	 * Identitfies the point as an IMTRACKPOINT
	 */
	final int pointType = 1;
	
//	int[] imOrigin;

	ImTrackPoint(double x, double y, Rectangle rect, double area, int frame,
			int thresh) {
		super(x, y, rect, area, frame, thresh);
	}

//	ImTrackPoint(double x, double y, Rectangle rect, double area, double[] cov,
//			int frame, int ID, int thresh) {
//		super(x, y, rect, area, cov, frame, ID, thresh);
//	}

//	ImTrackPoint(double x, double y, Rectangle rect, double area, double[] cov,
//			int frame, int thresh) {
//		super(x, y, rect, area, cov, frame, thresh);
//	}
	
	ImTrackPoint(TrackPoint point, ImagePlus frameIm){
		super(point);
		findAndStoreIm(frameIm);
	}
	
//	public void setImage(ImageProcessor im){
//		setImage(im, im.getWidth(), im.getHeight());
//	}
	
	public void setImage (ImageProcessor im, int dispWidth, int dispHeight){
		this.im = im;
		trackWindowWidth = dispWidth;
		trackWindowHeight = dispHeight;
	}
	
	public ImageProcessor getIm(){
		//pad the image
//		track.tb.ep.trackWindowHeight;
		imOriginX = (int)x-(trackWindowWidth/2)-1;
		imOriginY = (int)y-(trackWindowHeight/2)-1;
		return CVUtils.padAndCenter(new ImagePlus("Point "+pointID, im), trackWindowWidth, trackWindowHeight, (int)x-rect.x, (int)y-rect.y);
		 
	}
	
	public void findAndStoreIm(ImagePlus frameIm){
		Roi oldRoi = frameIm.getRoi();
		frameIm.setRoi(rect);
		im = frameIm.getProcessor().crop();//Does not affect frameIm's image
		frameIm.setRoi(oldRoi);
	}
	
	/**
	 * Generates Serializable forms of any non-serializable ImageJ objects 
	 * <p>
	 * For ImTrackPoints, the image is converted to a byte array
	 */
	public void preSerialize(){
		FileSaver fs = new FileSaver(new ImagePlus("ImTrackPoint "+pointID, im));
		serializableIm = fs.serialize();
	}
	
	/**
	 * Recreates any non-serializable ImageJ objects 
	 * <p>
	 * For ImTrackPoints, the byte array is converted back into an ImageProcessor
	 */
	public void postDeserialize(){
		Opener op = new Opener();
		ImagePlus im2 = op.deserialize(serializableIm);
		im = im2.getProcessor();		
	}
	
	public int toDisk(DataOutputStream dos, PrintWriter pw){
		
		//Write all TrackPoint data
		super.toDisk(dos, pw);
		
		//Image offest, width, and height already written in TrackPoint
		
		//Write image
		try {
			preSerialize();
			dos.write(serializableIm);
		} catch (Exception e) {
			if (pw!=null) pw.println("Error writing ImTrackPoint image for point "+pointID+"; aborting save");
			return 1;
		}
		
		return 0;
	}
	
	public int sizeOnDisk(){
		
		int size = super.sizeOnDisk();
		size += serializableIm.length;
		return size;
	}
	
}
