import org.opencv.imgproc.Imgproc;

//import ContourPlotter_;
import ij.*;
import ij.gui.Roi;

import java.util.Vector;



public class PointExtractor {
	
	
	////////////////////////////
	//  
	////////////////////////////
	/**
	 * The stack of images to extract points from 
	 */
	ImageStack imageStack;
	/**
	 * Parameters used for extracting points
	 */
	ExtractionParameters ep;
	
	/**
	 * Message handler
	 */
	Communicator comm;
	
	/**
	 * The points which were extracted in the last call to extractFrame() 
	 * <p>
	 * Only publicly accessible via getPoints()
	 */
	private Vector<TrackPoint> extractedPoints;
	/**
	 * Index of the last frame that was run through the point extractor
	 */
	int lastFrameExtracted=-1;
	/**
	 * How many frames the extractor moves forward when nextFrame() is called
	 */
	int increment;
	/**
	 * First frame to be extracted
	 */
	int startFrameNum;
	/**
	 * Index of the last frame in the stack 
	 */
	int endFrameNum;
	/**
	 * The frame being processed
	 */
	int currentFrameNum;
	
	////////////////////////////
	//Other fields
	//background images and that sort of thing
	////////////////////////////
	//Images:
		//currentFrame
	/**
	 * 
	 */
	Roi analysisRegion;
	/**
	 * The image being processed
	 */
	ImagePlus currentIm;
	
	/**
	 * ??//TODO
	 */
	ImagePlus backgroundIm;
	
	/**
	 * Background-removed Image
	 */
	ImagePlus backSubIm;
	
	/**
	 * ForegroundIm??//TODO
	 */
	ImagePlus foregroundIm;
		//backgroundIm
		//backSub (background subtracted im)
		//thresholdCompareIm
		//threshIm
		//foregroundIm
		
		//FrameLoader [getframe]
		
		//rectangle analysisRect
	/**
	 * Index of the last frame where the background image is valid (and doesn't need to be reloaded)
	 */
	int backValidUntil;
	
	////////////////////////////
	// Point Extracting methods 
	////////////////////////////
	
	
	public PointExtractor(ImageStack stack, Communicator comm){
		init(0, stack, comm);
	}
	
	//TODO
	public void init(int startFrame, ImageStack stack, Communicator comm){
		this.startFrameNum = startFrame;
		imageStack = stack;
		this.comm = comm;
		endFrameNum = imageStack.getSize()-1;//.getNFrames()-1;// 
		increment = ep.increment;
	}
	
	/**
	 * Calculates the index of the next frame
	 * @return Index of the frame following the last frame loaded
	 */
	public int nextFrame(){
		if (lastFrameExtracted == -1){
			return startFrameNum;
		} else {
			return lastFrameExtracted+increment;
		}
	}

	
	/**
	 * 
	 * @param frameNum Index of the stack slice to load
	 * @return
	 */
	public int extractFrame(int frameNum) {
		
		if(loadFrame(frameNum)>0){
			return 1;
		}
		//extractPoints
		lastFrameExtracted = frameNum;
		return 0;
	}
	
	
	//TODO loadFrame
		//calculate background image
		//error checks on the frame (framenum in range, frameloader not null, no frameloading error)
		//subtract background image from frame 
		//make foregroundim: use backsubtracted, or max of backsubtracted and current foreground [WHEN IS THE LATTER CASE USED?]
		//threshold the image
	public int loadFrame(int frameNum){
		
		currentFrameNum = frameNum;
		if (currentFrameNum>endFrameNum) {
			comm.message("Attempt to load frame "+currentFrameNum+" but the stack ends at frame "+endFrameNum, VerbLevel.verb_error);
			return 1;
		}
		
		calculateBackground();
		
		currentIm = new ImagePlus("Frame"+currentFrameNum, imageStack.getProcessor(currentFrameNum));//.getFrame(currentFrame)// 
		if (currentIm==null) {
			comm.message("The image in rame "+currentFrameNum+" was not returned from the ImageStack", VerbLevel.verb_error);
			return 2;
		}
		
		createBackSubIm();
		createForegroundIm();
		
		
		return 0;
	}
	
	
	//TODO
	public void calculateBackground() {
		
	}
	
	//TODO
	public void createBackSubIm() {
		//subtract the background image from the currentframe
	}
	
	//TODO
	public void createForegroundIm() {
		if (foregroundIm == null) {
			foregroundIm = (ImagePlus)backSubIm.clone();
		} else {
			
			//foregroundIm = max(backSubIm,foregroundIm)
		}
	}
	
	
	//TODO extractPoints
		//find contours (method below)
		//convert contours to trackPoints
	public void extractPoints() {
		
	}
	
	//TODO findContours
		//call openCV function to find contours
		//http://docs.opencv.org/java/index.html?org/opencv/imgproc/Imgproc.html findContours
	
	
	//TODO splitPoint
		//rethreshold
		//find contours
		//find the relevant contour
		//take the proper subimage
		//Rethreshold to a number of subregions (CVUtilsPlus) 
		//
	
	//TODO accessors
	public Vector<TrackPoint> getPoints(){
		return extractedPoints;
	}
	
	
	
		
}
