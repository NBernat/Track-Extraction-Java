import ij.ImagePlus;
import ij.measure.Measurements;
import ij.measure.ResultsTable;
import ij.plugin.filter.GaussianBlur;
import ij.plugin.filter.ParticleAnalyzer;
import ij.process.Blitter;
import ij.process.ImageProcessor;


public class CVUtils {

	//rethreshold to a specified number of regions
	
	/**
	 * Returns the image processed by the blitter interface
	 * @param im1
	 * @param im2
	 * @param blitterMode
	 * @return
	 */
	static ImagePlus blitterProcessing(ImagePlus im1, ImagePlus im2, int blitterMode){
		try {
			ImageProcessor ip1 = (ImageProcessor) im1.getProcessor().clone();
	        ImageProcessor ip2 = (ImageProcessor) im2.getProcessor().clone();
	        
	        ip1.copyBits(ip2, 0, 0, blitterMode);
			
			return new ImagePlus("Processed im of "+im1.getTitle()+" and "+im2.getTitle(), ip1);
					
		} catch ( Exception e) {
			return null;
		}
        
	}
	

	//http://docs.opencv.org/modules/imgproc/doc/miscellaneous_transformations.html
	/**
	 * Creates a copy of image in which all pixels below globalThreshValue are set to zero 
	 * @param image Image to be thresholded
	 * @param globalThreshValue Value below which pixles are discarded
	 * @return Thresholded image
	 */
	static ImagePlus thresholdImtoZero(ImagePlus image, double globalThreshValue) {
		//clone image
		
		ImagePlus maskIm =  (ImagePlus) image.clone();
		maskIm.getProcessor().threshold((int)globalThreshValue);
		
		ImagePlus cloneIm = maskCopy(image, maskIm);
		
		//return clone
		return cloneIm;
	}
	
	
	//http://docs.opencv.org/modules/core/doc/old_basic_structures.html
	/**
	 * Creates a masked copy of image using a mask
	 * @param image Image to be masked
	 * @param mask Mask image
	 * @return Masked image
	 */
	static ImagePlus maskCopy(ImagePlus image, ImagePlus mask){
		
		try {
			ImagePlus newIm = (ImagePlus) image.clone();
			ImagePlus maskIm = (ImagePlus) mask.clone();
			maskIm.getProcessor().multiply(1/255.0);
			newIm = blitterProcessing(newIm, maskIm, Blitter.MULTIPLY);
			
			return newIm;
		} catch (Exception e ) {
			return null;
		}
	}
	
	
	/**
	 * Creates a blurred copy of image
	 * @param image Image to be blurred
	 * @param sigma Sigma value used in Gaussian blurring
	 * @return Blurred image
	 */
	static ImageProcessor blurIm(ImageProcessor image, double sigma){
		
		ImageProcessor cloneIm = (ImageProcessor) image.clone();
		GaussianBlur GB = new GaussianBlur();
        GB.blurGaussian(image, sigma, sigma, .02);
        return cloneIm;
	}
	
	/**
	 * Creates a blurred copy of image
	 * @param image Image to be blurred
	 * @param sigma Sigma value used in Gaussian blurring
	 * @return Blurred image
	 */
	static ImagePlus blurIm(ImagePlus image, double sigma) {
		ImageProcessor cloneIm = ((ImagePlus)image.clone()).getProcessor();
		return new ImagePlus(image.getTitle(), blurIm(cloneIm,sigma));
	}
	
	
	//TODO
	//http://docs.opencv.org/modules/core/doc/operations_on_arrays.html
	static ImagePlus compGE(ImagePlus threshIm, ImagePlus threshCompIm){
		ImagePlus compdIm = null;
		
		return compdIm;
		
	}
	
	//TODO 
	//http://rsb.info.nih.gov/ij/developer/api/ij/measure/ResultsTable.html#ResultsTable()
	//http://rsb.info.nih.gov/ij/developer/api/index.html?ij/plugin/filter/ParticleAnalyzer.html
	/**
	 * 
	 * @param threshIm Thresholded image to analyze
	 * @param ep Extraction Parameters
	 * @return A ResultsTable with the appropriate info
	 */
	static ResultsTable findPoints(ImagePlus threshIm, ExtractionParameters ep) {
		
		int options = getPointFindingOptions();
		int measurements = getPointFindingMeasurements();
		ResultsTable rt = new ResultsTable();
		
		ParticleAnalyzer partAn = new ParticleAnalyzer(options, measurements, rt, ep.minArea, ep.maxArea);
		
		//Populate the results table
		partAn.analyze(threshIm);
		
		return rt;
	}
	
	/**
	 * Returns a flag word created by ORing the appropriate constants (SHOW_RESULTS, EXCLUDE_EDGE_PARTICLES, etc.)
	 * @return
	 */
	public static int getPointFindingOptions() {
		
		//Don't show anything, don't exclude edgepoints. basically we have no special options
		int opInt=0;
		
		return opInt;
	}
	
	/**
	 * Returns a flag word created by ORing the appropriate constants which are defined in the Measurements interface
	 * @return
	 */
	public static int getPointFindingMeasurements() {
		
		int measInt=0;
//		double x = rt.getValue("X", row);
//		double y = rt.getValue("Y", row);
		measInt += Measurements.CENTROID;

//		double boundX = rt.getValue("BX", row);
//		double boundY = rt.getValue("BY", row);
//		double width = rt.getValue("Width", row);
//		double height = rt.getValue("Height", row);
		measInt += Measurements.RECT;
		
//		double area = rt.getValue("Area", row);
		measInt += Measurements.AREA;
		
		return measInt;
	}
	
	
}
