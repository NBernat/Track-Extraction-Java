








//import ContourPlotter_;
import ij.*;
import ij.measure.ResultsTable;
import ij.plugin.ImageCalculator;
import ij.plugin.filter.GaussianBlur;
import ij.process.Blitter;

import java.awt.Rectangle;
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
	 * Image Process calculator
	 * <p>
	 * See IJ_Props.txt for calculator function strings 
	 */
	ImageCalculator IC;
	
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
	
	
	////////////////////////////
	//Other fields
	//background images and that sort of thing
	////////////////////////////
	//Images:
		//currentFrame
	/**
	 * The current region of analysis
	 */
	Rectangle analysisRegion;
	/**
	 * The image being processed
	 */
	ImagePlus currentIm;
	
	/**
	 * The resultsTable made by finding particles in the currentIm
	 * <p>
	 * Includes points out of the size range 
	 * <p>
	 * defaultHeadings = {"Area","Mean","StdDev","Mode","Min","Max",
     *  "X","Y","XM","YM","Perim.","BX","BY","Width","Height","Major","Minor","Angle",
     *  "Circ.", "Feret", "IntDen", "Median","Skew","Kurt", "%Area", "RawIntDen", "Ch", "Slice", "Frame", 
     *   "FeretX", "FeretY", "FeretAngle", "MinFeret", "AR", "Round", "Solidity"}
	 */
	ResultsTable pointTable;
	
	/**
	 * background image
	 */
	ImagePlus backgroundIm;
	
	/**
	 * Background-removed Image
	 */
	ImagePlus backSubIm;
	
	/**
	 * Foreground image
	 */
	ImagePlus foregroundIm;
	
	/**
	 * Thresholded image
	 */
	ImagePlus threshIm;
	/**
	 * Threshold comparison Image, used for non-global thresholding
	 */
	ImagePlus threshCompIm;
	
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
	
	
	public PointExtractor(ImageStack stack, Communicator comm, ExtractionParameters ep){
		init(ep.startFrame, stack, comm, ep);
	}
	
	//TODO
	public void init(int startFrame, ImageStack stack, Communicator comm, ExtractionParameters ep){
		this.startFrameNum = startFrame;
		imageStack = stack;
		this.comm = comm;
		this.ep = ep;
		endFrameNum = imageStack.getSize()-1;//.getNFrames()-1;// 
		increment = ep.increment;
		lastFrameExtracted=-1;
		isFirstRun=true;
		fl = new FrameLoader(comm, stack);
		IC = new ImageCalculator();
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
		
		
		backSubIm = currentIm;
		//VVVVVVVVV delete
		//Create a background-subtracted image for the current im
//		backSubIm = CVUtils.blitterProcessing(currentIm, backgroundIm, Blitter.SUBTRACT);//IC.run("sub", currentIm, backgroundIm);
//		comm.message("Created background subtracted image when loading frame "+frameNum+" in pe.loadFrame", VerbLevel.verb_debug);
//
//		//Create a foreground image...not sure when the "else" is ever used...
//		if (foregroundIm==null) {
//			comm.message ("Foreground image doesn't exist, cloning background subtracted image", VerbLevel.verb_debug);
//			try {
//				foregroundIm = (ImagePlus) backSubIm.clone();
//			} catch (Exception e) {
//				comm.message("failed clone of background subtracted image", VerbLevel.verb_debug);
//			}
//		} else {
//			comm.message ("Foreground image already exists, taking max of FG im and BS im", VerbLevel.verb_debug);
//			foregroundIm = CVUtils.blitterProcessing(backSubIm, foregroundIm, Blitter.MAX);
//			//foregroundIm = IC.run("max", backSubIm, foregroundIm);
//			
//		}
		//^^^^^^^^^^^^^^^^^^^^ delete
		
		comm.message("Thresholding image to zero...", VerbLevel.verb_debug);
		threshToZero();
		comm.message("...finished thresholding image to zero", VerbLevel.verb_debug);
		
		return 0;
	}
	
	
	/**
	 * Calculate the background image
	 */
	public void calculateBackground() {
		//if the background image is still valid, leave it as-is
		if (!isFirstRun && currentFrameNum <= backValidUntil) {   
	        return;
	     }
		
		comm.message("Calculating background", VerbLevel.verb_message);
		
		if (imageStack==null) {
			
			comm.message("The image stack is empty, no background image could be made", VerbLevel.verb_warning);			
			return;
		}
		
		//Set the interval of images (from which the background im is constructed) properly
		int first = currentFrameNum;
		first = (first + ep.resampleInterval <= endFrameNum) ? first : endFrameNum - ep.resampleInterval;
		first = (first >= startFrameNum) ? first : startFrameNum;
		
		int last = first + ep.resampleInterval;
		last = (last <= endFrameNum) ? last : endFrameNum;
		double delta = (last - first) / (ep.nBackgroundFrames - 1);
		
		//Get the frame normalization factor, if necessary
		if(fnm!=_frame_normalization_methodT._frame_none){
			double normSum = fl.getFrameNormFactor(first, fnm);
			if (normSum<=0) {
				comm.message("Frame normalization reported an error", VerbLevel.verb_error);
			}
			int i;
			for (i=1; i<ep.nBackgroundFrames; i++) {
				String s = "norm sum = "+normSum;
				comm.message(s, VerbLevel.verb_debug);
				int nf = fl.getFrameNormFactor((int)(first + i*delta + .5), fnm);
				if (nf<=0) {
					comm.message("Frame normalization reported an error", VerbLevel.verb_error);
				}
				normSum+=nf;
			}
			
			
			if (normSum <= 0) {
	            comm.message ("sum of norm factors gives non positive number", VerbLevel.verb_error);
	        }
			if (ep.nBackgroundFrames <= 0) {
	            comm.message ("number of background frames is nonpositive", VerbLevel.verb_error);
	        }
			
			
			normFactor = (int) (normSum / ep.nBackgroundFrames);
			
			String s = "norm sum is "+normSum+" nBackgroundFrames is "+ep.nBackgroundFrames+" norm factor is "+normFactor;
			comm.message(s, VerbLevel.verb_message);
			
		} else {
	        comm.message("frame normalization is turned off", VerbLevel.verb_message);
	        normFactor = -1;
	    }
		
		//Construct the background image from the properly normalized, in-range, frames
		comm.message ("calling get frame", VerbLevel.verb_debug);
		
		if (fl.getFrame(first, fnm, normFactor)!=0) {
			comm.message ("frame loader reports error", VerbLevel.verb_error);
		} else {
			backgroundIm = new ImagePlus("BackgroundIm_"+first+"_"+last, fl.returnIm);
		}
		
		if (backgroundIm==null) {
			comm.message("Initial background image is null", VerbLevel.verb_warning);
		}
		
		analysisRegion = fl.ar;
		
		int i;
		for (i=1; i<ep.nBackgroundFrames; i++){
			
			//TODO currentIm is returning null!
			int nextFrame = (int) (first + i*delta + 0.5);
			comm.message("Adding frame "+nextFrame+" to the background image", VerbLevel.verb_debug);
			if (fl.getFrame(nextFrame, fnm, normFactor) == 0) {
				currentIm = new ImagePlus("", fl.returnIm);
	            if (currentIm == null || backgroundIm == null) {
	                comm.message ("current frame or background im is NULL", VerbLevel.verb_error);
	            } else {
	            	backgroundIm = CVUtils.blitterProcessing(currentIm, backgroundIm, Blitter.MIN);
	            	//backgroundIm = IC.run("min", currentIm, backgroundIm);
	            }
	            
	            
	            if (backgroundIm==null) {
	    			comm.message("Background image is null after adding contributing frame number "+i, VerbLevel.verb_warning);
	    		}
	            
	        } else {
	            comm.message("frame loader reports error", VerbLevel.verb_error);
	        }
			
		}
		
		backValidUntil = first + ep.resampleInterval;
	    
		//Blur the background image, if needed
	    if (ep.blurSigma > 0) {
	        
	    	comm.message ("blurring background ", VerbLevel.verb_verbose);
	        
	        GaussianBlur GB = new GaussianBlur();
	        GB.blurGaussian(backgroundIm.getProcessor(), ep.blurSigma, ep.blurSigma, ep.blurAccuracy);
	        
	    }
	}
	

	/**
	 * Thresholds backSubIm according to the method set in the extraction parameters, and stores the results in threshIm  
	 */
	void threshToZero() {
		
		threshIm = (ImagePlus) backSubIm.clone();
		threshIm.getProcessor().threshold((int) ep.globalThreshValue);
//		if (lastFrameExtracted%25==0) {
//			threshIm.show();
//		}
		
		
		
		
//		
//	    comm.message ("pe threshto0", VerbLevel.verb_debug);
//	    if (ep.useGlobalThresh) {
//	        comm.message ("global threshold used in pe", VerbLevel.verb_debug);
//	        if (ep.blurSigma > 0) {
//	        	
//	            threshIm = (ImagePlus) backSubIm.clone();
//	            comm.message("threshIm created from cloned backSubIm", VerbLevel.verb_debug);
//	             
//	            CVUtils.blurIm(threshIm.getProcessor(), ep.blurSigma);
//		        comm.message ("im blurred", VerbLevel.verb_debug);
//	            
//		        
//		        threshIm = CVUtils.thresholdImtoZero(threshIm, ep.globalThreshValue);
//		        if (threshIm!=null){
//		        	comm.message ("im thresholded", VerbLevel.verb_debug);
//		        } else {
//		        	comm.message ("Thresholded failed", VerbLevel.verb_warning);
//		        }
//	            
//	        } else {
//	        	threshIm = CVUtils.thresholdImtoZero(backSubIm, ep.globalThreshValue);
//	            
//	        }
//	    } else {
//	    	comm.message("Non-global thresholding in pe", VerbLevel.verb_debug);
//	        ImagePlus maskIm = (ImagePlus) backSubIm.clone();
//	        if (ep.blurSigma > 0) {
//	        	
//	            threshIm = CVUtils.blurIm(backSubIm, ep.blurSigma);
//	            maskIm = CVUtils.compGE(threshIm, threshCompIm);
//	        } else {
//	            maskIm = CVUtils.compGE(threshIm, threshCompIm);
//	        }
//	        
//	        threshIm = CVUtils.maskCopy(backSubIm, maskIm);
//	    }
//	    comm.message ("pe threshto0 done", VerbLevel.verb_debug);
	}
	
	/**
	 * Sets the fields needed to do a non-global threshold 
	 * @param thrCmpIm The image used for comparison to generate local "threshold"
	 */
	void setThresholdCompareIm (ImagePlus thrCmpIm) {
		threshCompIm = (ImagePlus) thrCmpIm.clone();
	    ep.useGlobalThresh = false;
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
	    pointTable = CVUtils.findPoints(threshIm, ep, showResults);
	    comm.message("Frame "+currentFrameNum+": "+pointTable.getCounter()+" points in ResultsTable", VerbLevel.verb_message);
	    
	    
	    extractedPoints = CVUtils.rt2TrackPoints(pointTable, currentFrameNum, comm, ep);
	    
	    String s = "Frame "+currentFrameNum+": Extracted "+extractedPoints.size()+" new points";
	    comm.message(s, VerbLevel.verb_message);
	    		
	}
	

	
	
	//TODO splitPoint
		//rethreshold
		//find contours
		//find the relevant contour
		//take the proper subimage
		//Rethreshold to a number of subregions (CVUtilsPlus) 
		//
	
	
	/**
	 * Returns the extracted points
	 * @return Extracted points
	 */
	public Vector<TrackPoint> getPoints(){
		return extractedPoints;
	}
	
	
}





