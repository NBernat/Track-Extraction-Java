//import opencv stuff
public class PointExtractor {
	
	/**
	 * The points which were extracted in the last call to extractFrame() 
	 * <p>
	 * Only publicly accessible via getPoints()
	 */
	private TrackPoint[] extractedPoints;
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
	
	//TODO constructor
	
	/**
	 * 
	 * @return Index of the frame following the last frame loaded
	 */
	public int nextFrame(){
		return lastFrameExtracted+increment;
	}
		//increment the framenum
		//load frame LOADFRAME
	
	public void extractFrame(int frameNum) {
		//loadFrame
		//points=getPoints
	}
	
	
	//TODO loadFrame
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
	public TrackPoint[] getPoints(){
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
