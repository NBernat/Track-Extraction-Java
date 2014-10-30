//import opencv stuff
public class PointExtractor {
		
	//TODO constructor
	
	//TODO nextFrame
		//increment the framenum
		//load frame LOADFRAME
	
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
