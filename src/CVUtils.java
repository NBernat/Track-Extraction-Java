import java.awt.Color;
import java.awt.Graphics;
import java.awt.image.BufferedImage;

import ij.ImagePlus;
import ij.gui.Roi;
import ij.measure.Measurements;
import ij.measure.ResultsTable;
//import ij.plugin.filter.GaussianBlur;
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
	
	/**
	 * Takes an image and tries to find the lowest threshold at which the point can be split into multiple points of an appropriate area
	 */
	static int findThreshforNumPts(ImagePlus image, ExtractionParameters ep, int numPts, int minArea, int maxArea, int targetArea){
		int minTotal = numPts*minArea;
	    int maxTotal = numPts*maxArea;
	    if (maxTotal>=(image.getHeight()*image.getWidth())) {
	    	maxTotal = (image.getHeight()*image.getWidth())-1;
	    } 
	    
	    ImagePlus thrIm = (ImagePlus) image.clone();
	    
	    int bestThres = -1;
	    double goodness, bestGoodness = -200*numPts;
	    for (int j=1; j<255; j++) {
	    	thrIm.getProcessor().threshold(j);
	    	int ct = countNonZero(image);
	    	if (ct<minTotal) {
	    		break;
	    	}
	    	if (ct <= maxTotal){
	    		goodness = findGoodness(thrIm, ep, numPts, minArea, maxArea, targetArea);
	    		if (goodness > bestGoodness) {
	    			bestGoodness = goodness;
	    			bestThres = j;
	        	}
	    	}
	    }
		
		return bestThres;
	}
	
	static int countNonZero(ImagePlus image){
		int count = 0;
		for (int h=0; h<image.getHeight(); h++) {
			for (int w=0; w<image.getWidth(); w++) {
				if (image.getPixel(w, h)[0]>0){
					count++;
				}
			}
		}
		return count;
	}
	
	
	static double findGoodness(ImagePlus threshIm, ExtractionParameters ep, int nregions, int minArea, int maxArea, int bestArea) {
	    double goodness = 0;
	    //-100 for every contour# you are away from nregions
	    //-10 for every contour below minArea or above maxArea
	    //-1 * (area - bestArea)^2 / (maxArea - minArea)^2 for each contour
	    int nc = 0;
	    int area;
	    ResultsTable rt = findPoints(threshIm, ep, false);
	    nc = rt.getCounter();
	    goodness -= Math.abs(nc - nregions) * 100;
	    //nc = number of results; iterate through resulting areas
	    for (int i=1; i<=nc; i++) {
	        area = (int) rt.getValueAsDouble(ResultsTable.AREA, i);;
	        if (area < minArea || area > maxArea) {
	            goodness -= 10;
	        }
	        goodness -= 1.0*(area - bestArea)*(area - bestArea) / ((maxArea-minArea)*(maxArea - minArea));
	    }
	    return goodness;

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
	 * 
	 * @param threshIm Thresholded image to analyze
	 * @param ep Extraction Parameters
	 * @return A ResultsTable with the appropriate info
	 */
	static ResultsTable findPoints(ImagePlus threshIm, ExtractionParameters ep, boolean showResults) {
		
		int options = getPointFindingOptions(showResults, ep.trackPointType>=2);
		int measurements = getPointFindingMeasurements();
		ResultsTable rt = new ResultsTable();
		
		ParticleAnalyzer partAn = new ParticleAnalyzer(options, measurements, rt, ep.minArea, ep.maxArea);
		
		//Populate the results table
		Roi r = threshIm.getRoi();
		threshIm.deleteRoi();
		partAn.analyze(threshIm);
		threshIm.setRoi(r);
		return rt;
	}
	
	/**
	 * Returns a flag word created by ORing the appropriate constants (SHOW_RESULTS, EXCLUDE_EDGE_PARTICLES, etc.)
	 * @return
	 */
	public static int getPointFindingOptions(boolean showResults, boolean contourStart) {
		
		//Don't show anything, don't exclude edgepoints. basically we have no special options
		int opInt=0;
		opInt+=ParticleAnalyzer.EXCLUDE_EDGE_PARTICLES;
		if (contourStart){
			opInt+=ParticleAnalyzer.RECORD_STARTS;
		}
		if (showResults) {
			opInt+=ParticleAnalyzer.SHOW_RESULTS;
		} else {
			opInt+=ParticleAnalyzer.SHOW_NONE;
		}
		
		return opInt;
	}
	
	/**
	 * Returns a flag word created by ORing the appropriate constants which are defined in the Measurements interface
	 * @return
	 */
	public static int getPointFindingMeasurements() {
		
		int measInt=0;
		
		measInt += Measurements.CENTROID;

		measInt += Measurements.RECT;
	
		measInt += Measurements.AREA;
		
		return measInt;
	}
	
	
	public static ImageProcessor padAndCenter(ImagePlus image, int newWidth, int newHeight, int centerX, int centerY){
		
		BufferedImage newIm = new BufferedImage(newWidth, newHeight, image.getBufferedImage().getType());
		Graphics g = newIm.getGraphics();
		g.setColor(Color.black);
		g.fillRect(0,0,newWidth,newHeight);
		int offsetX = (newWidth/2)+1-centerX;
		int offsetY = (newHeight/2)+1-centerY;
		g.drawImage(image.getBufferedImage(), offsetX, offsetY, null);
		
		ImagePlus retIm = new ImagePlus("Padded "+image.getTitle(), newIm);
		
		return retIm.getProcessor();
		
		
		
	}
	
	//TODO 
	public static float[] interp1D(float first, float second, int numbetween){
		
		float[] between = new float[numbetween];
		float dif = (second-first)/(numbetween+1);
		for(int i=0; i<numbetween; i++) between[i]=first+dif*(i+1);
		
		return between;
	}
	
	
	//TODO 
	public static float[] rotateCoord(float x, float y, double thetha){
		
		float[] newCoord = new float[2];
		
		
		
		return newCoord;
		
	}

	
}
