import java.awt.Rectangle;

import ij.ImagePlus;
import ij.ImageStack;
import ij.measure.Measurements;
import ij.measure.ResultsTable;
import ij.plugin.filter.Analyzer;
import ij.process.ImageProcessor;


public class FrameLoader {
	
	/**
	 * The stack of images from which the frames are loaded
	 */
	ImageStack imageStack; 
	/**
	 * The last image that was loaded
	 */
	ImagePlus loadIm;
	/**
	 * The image to return from getImage
	 */
	ImageProcessor returnIm;
	/**
	 * Index of the last frame to be loaded
	 */
	int lastFrameLoaded;
	
	/**
	 * The analysis rectangle to be used as an ROI
	 */
	Rectangle ar;
	
	/**
	 * Message handler
	 */
	Communicator comm;
	
	//TODO
	_frame_normalization_methodT fnm;
	
	/////////////////////
	// Constructors
	/////////////////////
	
	/**
	 * Constructs a FrameLoader object
	 * @param comm Message handler
	 * @param IS Image stack from which frames are to be loaded
	 */
	FrameLoader(Communicator comm, ImageStack IS){
		init(comm, IS);
	}
	
	/**
	 * Sets initial field values
	 * @param comm Message handler
	 * @param IS Image stack from which frames are to be loaded
	 */
	void init(Communicator comm, ImageStack IS){
		this.comm = comm;
		imageStack = IS;
		ar = new Rectangle(0,0,-1,-1);
		lastFrameLoaded = -10000;
		
		fnm = _frame_normalization_methodT._frame_none;
		
	}
	
	/**
	 * Sets the analysis rectangle
	 * @param r Analysis rectangle
	 */
	void setAnalysisRect(Rectangle r){
		ar = r;
		String s = "analysis rectangle set to location ("+r.x+","+r.y+"), with width of "+r.getWidth()+"pixels and height of "+r.getHeight()+"pixels.";
		comm.message(s, VerbLevel.verb_message);
	}
	
	
	/**
	 * Sets returnIm as the specified image as an ImagePlus with default settings
	 * @param frameNum Frame number to load
	 * @return status, 0 means all is good
	 */
	int getFrame(int frameNum){
		return getFrame(frameNum, _frame_normalization_methodT._frame_none, 0);
		
	}
	
	/**
	 * Sets returnIm as a normalized frame image that is cropped to the current ROI
	 * @param frameNumber Frame number to load
	 * @param fnm Frame Normalization method (See enum {@link #_frame_normalization_methodT} )
	 * @param normTarget The target normalization factor
	 * @return status, 0 means all is good
	 */
	int getFrame(int frameNumber, _frame_normalization_methodT fnm, double normTarget){
		
		comm.message("getFrame called", VerbLevel.verb_debug);
		if (lastFrameLoaded!=frameNumber) {
			if (loadImage(frameNumber)!=0){
	            return -1;
	        }
		}
		
		if (loadIm==null) {
			comm.message("Failed to load frame "+frameNumber, VerbLevel.verb_error);
			return -1;
		}
		
		checkAr();
		ImageProcessor convertedIm = (ImageProcessor)loadIm.getProcessor().clone();
		convertedIm.setRoi(ar);
		
		
		if (fnm==_frame_normalization_methodT._frame_none) {
			comm.message("Returning cloned image", VerbLevel.verb_debug);
		} else {
			
			if (normTarget<=0) {
				comm.message("Normalization target is less than zero, returning cloned image", VerbLevel.verb_warning);
				
			} else {
				
				double nf = getFrameNormFactor (frameNumber, fnm);
				if (nf>0) {
					String s = "Scaling frame: target norm factor = "+normTarget;
					s+= " and this frame has nf = "+nf;
					
					nf = normTarget/nf;
					
					s+= " so I am multiplying it by "+nf;
					
					convertedIm.multiply(nf);
					comm.message(s, VerbLevel.verb_verbose);
					
				} else {	
					comm.message("Norm factor returned a value <= 0, returning cloned image", VerbLevel.verb_message);
					
				}	
			}
		}
		
		//crop the image to the ROI
		returnIm = convertedIm.crop();
		return 0; 
	}
	
	/**
	 * Loads the specified frame into the FrameLoader field loadIm
	 * @param frameNum Frame to load into FrameLoader object
	 * @return status, -1 for frameNum out of bounds error, 1 for null image error, 0 for all good
	 */
	int loadImage(int frameNum){
		
		if (frameNum<1 || frameNum>=imageStack.getSize()){
			comm.message("Tried to load frame number "+frameNum+", which is out of bounds", VerbLevel.verb_warning);
			return -1;
		}
		
		loadIm = new ImagePlus("Frame "+frameNum,imageStack.getProcessor(frameNum));
		lastFrameLoaded = frameNum;
		
		if (loadIm.getProcessor()==null) {
			comm.message("Frame "+frameNum+" returned a null image", VerbLevel.verb_warning);
			return 1;
		}
		
		
		return 0;
	}
	
	
	/**
	 * Calculates the normalization factor of the frame based on the normalization method, to be used in {@link #PointExtractor}
	 * @param frameNumber Index of the frame to be normalized
	 * @param fnm Frame Normalization method (See enum {@link #_frame_normalization_methodT} )
	 * @return Normalization factor, or -1 if the image is null
	 */
	public int getFrameNormFactor (int frameNumber,  _frame_normalization_methodT fnm) {
	    
	    if (lastFrameLoaded != frameNumber) {
	    	if (loadImage(frameNumber)!=0){
	            return -1;
	        }
	    };
	    double pixelSum;
	    switch (fnm){
	        case _frame_none:
	            return 0;
		case _frame_wholeImage:
	        	pixelSum = makeMeasurementOnROI(loadIm, Measurements.INTEGRATED_DENSITY);
	            return (int) pixelSum;
		case _frame_excerptedRect:
	            checkAr();
	            Rectangle roi = loadIm.getProcessor().getRoi();
	            loadIm.setRoi(ar);
	            pixelSum = makeMeasurementOnROI(loadIm, Measurements.INTEGRATED_DENSITY);
	            loadIm.setRoi(roi);
	            return (int) pixelSum;
	    }
	    return 0; //should never reach this point
	}
	
	/**
	 * Validates the analysis rectangle
	 */
	void checkAr() {
	    String ss = "";
	    ss+= "checkAr: ar was (x,y,w,h) " + ar.x + ", " + ar.y + ", " + ar.width + ", " + ar.height;
	    comm.message(ss, VerbLevel.verb_debug);
	    if (ar.width < 0 || ar.height < 0) {
	        comm.message("analysis rectangle was not set", VerbLevel.verb_warning);
	        ar.setLocation(0, 0);
	        if (loadIm == null) {
	            comm.message("loadIm is NULL.", VerbLevel.verb_error);
	        } else {
	            ar.width = loadIm.getWidth();
	            ar.height = loadIm.getHeight();
	        }
	    }
	    if (loadIm != null) {
	        if (ar.width + ar.x > loadIm.getWidth()) {
	            ss = "";
	            ss += "analysis rectangle width reduced from " + ar.width;
	            ar.width = loadIm.getWidth() - ar.x;
	            ss += " to " + ar.width + " to fit image width of " + loadIm.getWidth();
	            comm.message(ss, VerbLevel.verb_warning);
	        }
	        if (ar.height + ar.y > loadIm.getHeight()) {
	            ss = "";
	            ss += "analysis rectangle height reduced from " + ar.height;
	            ar.height = loadIm.getHeight() - ar.y;
	            ss += " to " + ar.height + " to fit image height of " + loadIm.getHeight();
	            comm.message(ss, VerbLevel.verb_warning);
	        }
	    }
	    ss = "";
	    ss += "checkAr: ar is (x,y,w,h) " + ar.x + ", " + ar.y + ", " + ar.width + ", " + ar.height;
	    comm.message(ss, VerbLevel.verb_debug);
	}

	/**
	 * Makes the specified ImageJ measurement on the image
	 * @param ip The image to be analyzed
	 * @param measurement The integer representing the measurement, see ij.measure.Measurements for static Measurement numbers
	 * @return The specified measurement
	 */
	double makeMeasurementOnROI(ImagePlus image, int measurement){
		
        ResultsTable rt = new ResultsTable();
        Analyzer anlz = new Analyzer(image, measurement, rt);
        anlz.measure();
        int col = rt.getLastColumn();
        int row = rt.getCounter();
        return rt.getValueAsDouble(col, row);
		
	}
	
	
}


/**
 * Type indicating the type of frame analysis to be used in the Frame Loader
 * @author Natalie
 *
 */
enum _frame_normalization_methodT {_frame_none, _frame_wholeImage, _frame_excerptedRect};

