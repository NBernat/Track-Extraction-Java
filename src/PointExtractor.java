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
	 * The points which were extracted in the last call to extractFrame() 
	 * <p>
	 * Only publicly accessible via getPoints()
	 */
	private Vector<TrackPoint> extractedPoints;
	/**
	 * Index of the last frame that was run through the point extractor
	 */
	int lastFrameExtracted;
	/**
	 * How many frames the extractor moves forward when nextFrame() is called
	 */
	int increment;
	/**
	 * Index of the last frame in the stack 
	 */
	int endFrameNum;
	
	////////////////////////////
	//Other fields
	//background images and that sort of thing
	
	
	////////////////////////////
	// Point Extracting methods 
	////////////////////////////
	
	//TODO constructor
	
	/**
	 * Calculates the index of the next frame
	 * @return Index of the frame following the last frame loaded
	 */
	public int nextFrame(){
		return lastFrameExtracted+increment;
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
		
		
		return 0;
	}
		//calculate background image
		//error checks on the frame (framenum in range, frameloader not null, no frameloading error)
		//subtract background image from frame 
		//make foregroundim: use backsubtracted, or max of backsubtracted and current foreground [WHEN IS THE LATTER CASE USED?]
		//threshold the image
	
	//TODO getPoints
		//find contours
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
	
	
	
	
	//Images:
	//currentFrame
	//backgroundIm
	//backSub (background subtracted im)
	//thresholdCompareIm
	//threshIm
	//foregroundIm
	
	//FrameLoader [getframe]
	
	//rectangle analysisRect
	//int firstFrame
	//int lastFrame
	//int frameNum
	//int increment?
	//int backValidUntil?
	
	//ExtractionParams ep
		//double minArea,maxArea

	//int 
	
	
}
