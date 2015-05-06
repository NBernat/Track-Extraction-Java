package TrackExtractionJava;

import ij.ImagePlus;
import ij.gui.Roi;
import ij.io.FileSaver;
import ij.io.Opener;
import ij.process.ByteProcessor;
import ij.process.ImageProcessor;

import java.awt.Rectangle;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.PrintWriter;


public class ImTrackPoint extends TrackPoint{
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	transient ImageProcessor im;
	protected byte[] serializableIm;
	protected int imOriginX;
	protected int imOriginY;
	
	protected int trackWindowWidth;
	protected int trackWindowHeight;
	
	/**
	 * Identitfies the point as an IMTRACKPOINT
	 */
	final int pointType = 1;
	
	
	public ImTrackPoint() {
	}

	public ImTrackPoint(double x, double y, Rectangle rect, double area, int frame,
			int thresh) {
		super(x, y, rect, area, frame, thresh);
	}

	
	public ImTrackPoint(TrackPoint point, ImagePlus frameIm){
		super(point);
		findAndStoreIm(frameIm);
	}
	
	public void setImage (ImageProcessor im, int dispWidth, int dispHeight){
		this.im = im;
		trackWindowWidth = dispWidth;
		trackWindowHeight = dispHeight;
	}
	
	public ImageProcessor getIm(){
		//pad the image so be the same dimensions as the rest in the stack
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
	protected void preSerialize(){
		FileSaver fs = new FileSaver(new ImagePlus("ImTrackPoint "+pointID, im));
		serializableIm = fs.serialize();
	}
	
	/**
	 * Recreates any non-serializable ImageJ objects 
	 * <p>
	 * For ImTrackPoints, the byte array is converted back into an ImageProcessor
	 */
	protected void postDeserialize(){
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
			dos.writeByte(im.getWidth());
			dos.writeByte(im.getHeight());
			for (int j=0; j<im.getWidth(); j++){
				for (int k=0; k<im.getHeight(); k++){
					dos.writeByte(im.getPixel(j,k));
				}
			}
			
		} catch (Exception e) {
			if (pw!=null) pw.println("Error writing ImTrackPoint image for point "+pointID+"; aborting save");
			return 1;
		}
		
		return 0;
	}
	
	public int sizeOnDisk(){
		
		int size = super.sizeOnDisk();
		size += (2+im.getWidth()*im.getHeight());//Size of byte=1
		return size;
	}

	public static ImTrackPoint fromDisk(DataInputStream dis, Track t, PrintWriter pw){
		
		ImTrackPoint itp = new ImTrackPoint();
		
		if (itp.loadFromDisk(dis,t,pw)==0){
			return itp;
		} else {
			return null;
		}
	}
	
	protected int loadFromDisk(DataInputStream dis, Track t, PrintWriter pw){
		
		//Load all superclass info
		if (super.loadFromDisk(dis, t,pw)!=0){
			return 1;
		}
		
		//read new data: image
		try {
			trackWindowWidth = t.exp.getEP().trackWindowWidth;
			trackWindowHeight = t.exp.getEP().trackWindowHeight;
			int w = dis.readByte();
			int h = dis.readByte();
//			pw.println("Image ("+w+"x"+h+")");
			byte[] pix = new byte[w*h];
			for (int x=0; x<w; x++){
				for(int y=0; y<h; y++){
					pix[y*w+x] = dis.readByte();
				}
			}
			ImagePlus imp = new ImagePlus("new",new ByteProcessor(w, h, pix));

			im = imp.getProcessor();
			
		} catch (Exception e) {
			if (pw!=null) pw.println("Error writing ImTrackPoint Info");
			return 3;
		}
		
		return 0;
	}
	
	
}
