import java.util.Vector;

import ij.*;
//import opencv stuff
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
	int startFrame;
	/**
	 * Index of the last frame in the stack 
	 */
	int endFrame;
	
	////////////////////////////
	//Other fields
	//background images and that sort of thing
	////////////////////////////
	//Images:
		//currentFrame
		//backgroundIm
		//backSub (background subtracted im)
		//thresholdCompareIm
		//threshIm
		//foregroundIm
		
		//FrameLoader [getframe]
		
		//rectangle analysisRect
		//int frameNum
		//int backValidUntil?
	
	////////////////////////////
	// Point Extracting methods 
	////////////////////////////
	
	
	public PointExtractor(ImageStack stack){
		init(0, stack);
	}
	
	//TODO
	public void init(int startFrame, ImageStack stack){
		this.startFrame = startFrame;
		imageStack = stack;
		endFrame = imageStack.getSize()-1;
		increment = ep.increment;
	}
	
	/**
	 * Calculates the index of the next frame
	 * @return Index of the frame following the last frame loaded
	 */
	public int nextFrame(){
		if (lastFrameExtracted == -1){
			return startFrame;
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
		//loadFrame
		if(loadFrame(frameNum)>0){
			return 1;
		}
		//points=getPoints
		lastFrameExtracted = frameNum;
		return 0;
	}
	
	
	//TODO loadFrame
	public int loadFrame(int frameNum){
		//do the work
		
		return 0;
	}
		//calculate background image
		//error checks on the frame (framenum in range, frameloader not null, no frameloading error)
		//subtract background image from frame 
		//make foregroundim: use backsubtracted, or max of backsubtracted and current foreground [WHEN IS THE LATTER CASE USED?]
		//threshold the image
	
	//TODO getPoints
		//find contours (method below)
		//convert contours to trackPoints
	
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
