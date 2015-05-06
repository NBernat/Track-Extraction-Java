package TrackExtractionJava;

//import ContourPlotter_;
import ij.*;
import ij.gui.Roi;
import ij.measure.ResultsTable;
//import ij.plugin.ImageCalculator;
import ij.process.ImageProcessor;

import java.awt.Rectangle;
import java.util.Vector;


/**
 * Extracts TrakPoints from an image
 * @author Natalie
 *
 */
public class PointExtractor {

	/**
	 * The stack of images to extract points from 
	 */
//	ImageStack imageStack;
	/**
	 * Parameters used for extracting points
	 */
	ExtractionParameters ep;
	
	/**
	 * Message handler
	 */
	Communicator comm;
	/**
	 * Image Process calculator
	 * <p>
	 * See IJ_Props.txt for calculator function strings 
	 */
//	ImageCalculator IC;
	
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
	
	/**
	 * Whether or not the frame being processed is the first one
	 */
	boolean isFirstRun;
	
	
	/**
	 * Object that retrieves frame from image
	 */
	FrameLoader fl;
	/**
	 * Normalization type (See class below)
	 */
	_frame_normalization_methodT fnm;
	/**
	 * Normalization Factor
	 */
	double normFactor;
	
	

	/**
	 * The current region of analysis
	 */
	Rectangle analysisRegion;
	/**
	 * The image being processed
	 */
	ImagePlus currentIm;
	/**
	 * Thresholded image
	 */
	ImagePlus threshIm;
	
	/**
	 * The resultsTable made by finding particles in the currentIm
	 * <p>
	 * Includes points out of the size range 
	 */
	ResultsTable pointTable;
	
	/**
	 * background image
	 */
//	ImagePlus backgroundIm;
	

	/**
	 * Foreground image
	 */
//	ImagePlus foregroundIm;
	

	/**
	 * Threshold comparison Image, used for non-global thresholding
	 */
//	ImagePlus threshCompIm;

	/**
	 * Index of the last frame where the background image is valid (and doesn't need to be reloaded)
	 */
	int backValidUntil;
	
	
	////////////////////////////
	// Point Extracting methods 
	////////////////////////////
	
	/**
	 * Constructs a Point Extractor for a given stack
	 * @param stack
	 * @param comm
	 * @param ep
	 */
	public PointExtractor(ImageStack stack, Communicator comm, ExtractionParameters ep){
		init(ep.startFrame, stack, comm, ep);
	}
	
	/**
	 * Machinery for constructing a PointExtractor
	 * @param startFrame
	 * @param stack
	 * @param comm
	 * @param ep
	 */
	private void init(int startFrame, ImageStack stack, Communicator comm, ExtractionParameters ep){
		this.startFrameNum = startFrame;
//		imageStack = stack;
		this.comm = comm;
		this.ep = ep;
		endFrameNum = stack.getSize()-1;
//		endFrameNum = imageStack.getSize()-1;//.getNFrames()-1;// 
		increment = ep.increment;
		lastFrameExtracted=-1;
		isFirstRun=true;
		fl = new FrameLoader(comm, stack);
//		IC = new ImageCalculator();
		fnm = fl.fnm;
	}
	
	/**
	 * Calculates the index of the next frame
	 * @return Index of the frame following the last frame loaded
	 */
	public int nextFrameNum(){
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
	public int extractFramePoints(int frameNum) {
		
		if(loadFrame(frameNum)>0){
			comm.message("Frame "+frameNum+" was NOT successfully loaded in Point Extractor", VerbLevel.verb_debug);
			return 1; 
		}
		comm.message("Frame "+frameNum+" successfully loaded in Point Extractor", VerbLevel.verb_debug);
		extractPoints(frameNum);
		lastFrameExtracted = frameNum;
		return 0;
	}
	
	
	/**
	 * Loads the frame into the currentIm, backSubIm, ForegroundIm, and threshIm images, and loads the proper backgroundIm 
	 * @param frameNum Index of the frame to be loaded
	 * @return status, 0 means all is good, otherwise error
	 */
	public int loadFrame(int frameNum){
		
		if (frameNum==currentFrameNum){
			return 0;
		}
		
		//Set the current frame
		currentFrameNum = frameNum;
		if (currentFrameNum>endFrameNum) {
			comm.message("Attempt to load frame "+currentFrameNum+" but the stack ends at frame "+endFrameNum, VerbLevel.verb_error);
			return 1;
		}
		
		//Calculate the background image
//		calculateBackground();
		
		//Set the current image and analysis region
		if (fl.getFrame(frameNum, fnm, normFactor)!=0) {
			comm.message("Frame Loader returned error", VerbLevel.verb_warning);
			return 2;
		} else {
			comm.message("No error from frame loader when loading frame "+frameNum+" in pe.loadFrame", VerbLevel.verb_debug);
			currentIm = new ImagePlus("Frame "+frameNum,fl.returnIm);
		}
		assert (currentIm!=null);
		analysisRegion = fl.ar;
				
		comm.message("Thresholding image to zero...", VerbLevel.verb_debug);
		defaultThresh();
		comm.message("...finished thresholding image to zero", VerbLevel.verb_debug);
		
		return 0;
	}
	
	

	/**
	 * Thresholds backSubIm according to the method set in the extraction parameters, and stores the results in threshIm  
	 */
	void defaultThresh() {
		
		threshIm = new ImagePlus("Thresh im Frame "+currentFrameNum, currentIm.getProcessor().getBufferedImage());
//		threshIm = (ImagePlus) currentIm.clone();
		threshIm.getProcessor().threshold((int) ep.globalThreshValue);

	}
	
	/**
	 * Extracts points from the specified frame, storing them in extractedPoints
	 * @param frameNum
	 */
	public void extractPoints(int frameNum) {
		
		if (currentFrameNum!= frameNum){
			loadFrame(frameNum);
		}
		
	    comm.message("extract points called", VerbLevel.verb_debug);
	    
	    boolean showResults =  ep.showSampleData>=2 && ep.sampleInd==frameNum;
	    if (showResults) {
	    	threshIm.show();
	    }
	    //
//	    pointTable = CVUtils.findPoints(threshIm, ep, showResults);
//	    comm.message("Frame "+currentFrameNum+": "+pointTable.getCounter()+" points in ResultsTable", VerbLevel.verb_message);
//	    
//	    extractedPoints = CVUtils.rt2TrackPoints(pointTable, currentFrameNum, comm, ep);
	    extractedPoints = findPtsInIm(threshIm, (int) ep.globalThreshValue, showResults);
	    
	    
	    String s = "Frame "+currentFrameNum+": Extracted "+extractedPoints.size()+" new points";
	    comm.message(s, VerbLevel.verb_message);
	    		
	}
	
	public Vector<TrackPoint> findPtsInIm(ImagePlus im, int thresh, boolean showResults){
		pointTable = CVUtils.findPoints(im, ep, showResults);
		if (showResults) {
			comm.message("Frame "+currentFrameNum+": "+pointTable.getCounter()+" points in ResultsTable", VerbLevel.verb_message);
	    }
		//Vector<TrackPoint> pts = CVUtils.rt2TrackPoints(pointTable, currentFrameNum, comm, ep, thresh);
		Vector<TrackPoint> pts = rt2TrackPoints(pointTable, currentFrameNum, thresh);
		return pts;
	}
	
	
	
	
	/**
	 * Adds a row from the results table to the list of TrackPoints, if the point is the proper size according to the extraction parameters
	 * @param rt Results Table containing point info 
	 * @param frameNum Frame number
	 * @return List of Trackpoints within the 
	 */
	public Vector<TrackPoint> rt2TrackPoints (ResultsTable rt, int frameNum, int thresh) {
		
		Vector<TrackPoint> tp = new Vector<TrackPoint>();
		
		for (int row=1; row<rt.getCounter(); row++) {
			comm.message("Gathering info for Point "+row+" from ResultsTable", VerbLevel.verb_debug);
			double area = rt.getValueAsDouble(ResultsTable.AREA, row);
			comm.message("Point "+row+": area="+area, VerbLevel.verb_debug);
			double x = rt.getValueAsDouble(ResultsTable.X_CENTROID, row);
			double y = rt.getValueAsDouble(ResultsTable.Y_CENTROID, row);
			double width = rt.getValueAsDouble(ResultsTable.ROI_WIDTH, row);
			double height = rt.getValueAsDouble(ResultsTable.ROI_HEIGHT, row);
			double boundX = rt.getValueAsDouble(ResultsTable.ROI_X, row);
			double boundY = rt.getValueAsDouble(ResultsTable.ROI_Y, row);
			Rectangle rect = new Rectangle((int)boundX-ep.roiPadding, (int)boundY-ep.roiPadding, (int)width+2*ep.roiPadding, (int)height+2*ep.roiPadding);
//			Rectangle rect = new Rectangle((int)boundX, (int)boundY, (int)width, (int)height);
			//Rectangle rect = new Rectangle((int)x-ep.roiPadding, (int)y-ep.roiPadding, (int)2*ep.roiPadding, (int)2*ep.roiPadding);
			
			
			comm.message("Converting Point "+row+" "+"("+(int)x+","+(int)y+")"+"to TrackPoint", VerbLevel.verb_debug);
			if (ep.properPointSize(area)) {
				
				switch (ep.trackPointType){
					case 1: //ImTrackPoint
						ImTrackPoint iTPt = new ImTrackPoint(x,y,rect,area,frameNum,thresh);
						if (currentFrameNum!=frameNum){
							loadFrame(frameNum);
						}
						Roi oldRoi = currentIm.getRoi();
						currentIm.setRoi(rect);
						ImageProcessor im = currentIm.getProcessor().crop(); //does not affect currentIm
						currentIm.setRoi(oldRoi);
						iTPt.setImage(im, ep.trackWindowWidth, ep.trackWindowHeight);
						tp.add(iTPt);
						break;
					case 2: //MaggotTrackPoint
						MaggotTrackPoint mtPt = new MaggotTrackPoint(x,y,rect,area,frameNum,thresh);
						mtPt.setCommunicator(comm);
						if (currentFrameNum!=frameNum){
							loadFrame(frameNum);
						}
						Roi roi = currentIm.getRoi();
						currentIm.setRoi(rect);
						ImageProcessor im2 = currentIm.getProcessor().crop(); //does not affect currentIm
						currentIm.setRoi(roi);
						mtPt.setImage(im2, ep.trackWindowWidth, ep.trackWindowHeight);
						mtPt.setStart((int)rt.getValue("XStart", row), (int)rt.getValue("YStart", row));
						mtPt.extractFeatures();
						tp.add(mtPt);
						break;
					default:
						TrackPoint newPt = new TrackPoint(x,y,rect,area,frameNum,thresh); 
						tp.add(newPt);
						comm.message("Point "+row+" has pointID "+newPt.pointID, VerbLevel.verb_debug);
				}
				
			} else{
				comm.message("Point was not proper size: not made into a point", VerbLevel.verb_debug);
			}
			
		}
		
		return tp;
		
	}
	
	
	/**
	 * Tries to find a pixel threshold which can achieve the desired number of points. If successful, returns a list of the new points.
	 * @param point The point to be split
	 * @param numDesiredPts the number of points which "should" be in that image
	 * @return The new points if a threshold was found, otherwise an empty list 
	 */
	public Vector<TrackPoint> splitPoint(TrackPoint point, int numDesiredPts, int targetArea){
		
		Vector<TrackPoint> newPoints = new Vector<TrackPoint>();
		
		loadFrame(point.frameNum);
		
		ImagePlus crIm = (ImagePlus) currentIm.clone();
		crIm.setRoi(point.rect);
		crIm.getProcessor().crop();
		//Try to find the threshold (CVUtils)
		int minArea = (int) (targetArea*(1-ep.fracChangeForSplitting));
		int maxArea = (int) (targetArea*(1+ep.fracChangeForSplitting));
		int newThres = CVUtils.findThreshforNumPts(crIm, ep, numDesiredPts, minArea, maxArea, targetArea);
		if (newThres>0){
			//Threshold the image
			crIm.getProcessor().threshold(newThres);
			//Find the points 
			newPoints = findPtsInIm(crIm, newThres, false);
			//Fix the offsets
			for (int i=1; i<=newPoints.size(); i++){
				Rectangle rect = newPoints.get(i).rect;
				rect.x += point.rect.x;
				rect.y += point.rect.y;
				newPoints.get(i).rect = rect;
			}
			
			
		}
		
		return newPoints;
	}

	
	/**
	 * Returns the extracted points
	 * @return Extracted points
	 */
	public Vector<TrackPoint> getPoints(){
		return extractedPoints;
	}
	
	
}





