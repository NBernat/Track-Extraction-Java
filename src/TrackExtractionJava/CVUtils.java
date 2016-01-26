package TrackExtractionJava;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.util.Vector;

import ij.ImagePlus;
import ij.gui.Roi;
import ij.measure.Measurements;
import ij.measure.ResultsTable;
import ij.plugin.ImageCalculator;
import ij.plugin.filter.ParticleAnalyzer;
import ij.process.FloatPolygon;
import ij.process.FloatProcessor;
import ij.process.ImageProcessor;


public class CVUtils {
	
	public static double[][] fPoly2Array(FloatPolygon fp, int offX, int offY){
		
		double[][] ar = new double[2][fp.npoints];
		for (int i=0; i<fp.npoints; i++){
			ar[0][i] = fp.xpoints[i]+offX;
			ar[1][i] = fp.ypoints[i]+offY;
		}
		
		return ar;
	}
	
	
	

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
	

	static int findThreshforNumPts(ImagePlus image, ExtractionParameters ep, int numPts, int minArea, int maxArea, int targetArea){
		return findThreshforNumPts(image, ep, numPts, minArea, maxArea, targetArea, 0, 255);
	}
	
	/**
	 * Takes an image and tries to find the lowest threshold at which the point can be split into multiple points of an appropriate area
	 */
	static int findThreshforNumPts(ImagePlus image, ExtractionParameters ep, int numPts, int minArea, int maxArea, int targetArea, int minTh, int maxThres){

//		String debS = "";
		
	    boolean excl = ep.excludeEdges;
	    ep.excludeEdges = false;
		
	    
	    int bestThres = -1;
	    double goodness, bestGoodness = -200*numPts;
	    for (int j=minTh; j<=maxThres; j++) {
	    	
	    	ImageProcessor thrIm = image.getProcessor().duplicate();
	    	thrIm.threshold(j);
	    	
//	    	debS+="thresh="+j+", numAbove0="+ct+"; ";
	    	
	    		goodness = findGoodness(new ImagePlus("", thrIm), j, ep, numPts, minArea, maxArea, targetArea);
//	    		debS+="goodness="+goodness;
	    		if (goodness > bestGoodness) {
//	    			debS+="; BEST";
	    			bestGoodness = goodness;
	    			bestThres = j;
	        	}
//	    	debS+="\n";
	    }

	    ep.excludeEdges = excl;

//	    new TextWindow("ThreshFinder debugger", debS, 500, 500);
	    
		return bestThres;
	}
	/*
	private static int countNonZero(ImagePlus image){
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
	*/
	
	public static double findGoodness(ImagePlus threshIm, int thresh, ExtractionParameters ep, int nregions, int minArea, int maxArea, int bestArea) {
		
		double goodness = 0;
	    //-100 for every contour# you are away from nregions
	    //-10 for every contour below minArea or above maxArea
	    //-1 * (area - bestArea)^2 / (maxArea - minArea)^2 for each contour
	    int area;
	    ResultsTable rt = findPoints(threshIm, thresh, null, ep, minArea, maxArea, false);
	    int nc = rt.getCounter();
//	    goodness -= Math.abs(nc - nregions) * 100;
	    //nc = number of results; iterate through resulting areas
	    for (int i=0; i<rt.getCounter(); i++) {
	        area = (int) rt.getValueAsDouble(ResultsTable.AREA, i);
	        if (area < minArea || area > maxArea) {
	            goodness -= 10;
	            nc--;
	        }
	        goodness -= 1.0*(area - bestArea)*(area - bestArea) / ((maxArea-minArea)*(maxArea - minArea));
	    }

	    goodness -= Math.abs(nc - nregions) * 100;
	    
	    return goodness;

	}
	
	/**
	 * 
	 * @param threshIm Thresholded image to analyze
	 * @param ep Extraction Parameters
	 * @return A ResultsTable with the appropriate info
	 * @return
	 */
	static ResultsTable findPoints(ImagePlus threshIm, int thresh, ExtractionParameters ep, boolean showResults) {
		return findPoints(threshIm, thresh, null, ep, (int)ep.minArea, (int)ep.maxArea, showResults);
	}

	/**
	 * Generates a ResultsTable filled with the points in the image. Note that all coordinates are RELATIVE TO THE IMAGE
	 * @param threshIm Thresholded image to analyze
	 * @param ep Extraction Parameters
	 * @return A ResultsTable with the appropriate info
	 */
	static ResultsTable findPoints(ImagePlus threshIm, int thresh, Rectangle analysisRect, ExtractionParameters ep, int minArea, int maxArea, boolean showResults) {
		
		
		boolean excludeEdges = analysisRect==null && ep.excludeEdges;
		int options = getPointFindingOptions(showResults, excludeEdges, ep.trackPointType>=2);
		int measurements = getPointFindingMeasurements();
		ResultsTable rt = new ResultsTable();
		
		ParticleAnalyzer partAn = new ParticleAnalyzer(options, measurements, rt, minArea, maxArea);
		
		//ParticleAnalyzer partAn = new ParticleAnalyzer(options, measurements, rt, 1, 100000);
		
		
		//Populate the results table
		Roi r = threshIm.getRoi();
		
		double mint = threshIm.getProcessor().getMinThreshold();
		double maxt = threshIm.getProcessor().getMaxThreshold();
		
		threshIm.getProcessor().setThreshold((double) thresh, (double) 255, ImageProcessor.NO_LUT_UPDATE);
		
		if (analysisRect!=null){
			threshIm.getProcessor().setRoi(analysisRect);
		} else {
			threshIm.deleteRoi();
		}
		
//		threshIm.show();
		
		
		if (!partAn.analyze(threshIm)) {
			System.out.println ("partAN returned error");
		};
		threshIm.getProcessor().setThreshold(mint, maxt, ImageProcessor.NO_LUT_UPDATE);
		threshIm.setRoi(r);
		
		
		return rt;
	}
	
	/**
	 * Returns a flag word created by ORing the appropriate constants (SHOW_RESULTS, EXCLUDE_EDGE_PARTICLES, etc.)
	 * @return
	 */
	public static int getPointFindingOptions(boolean showResults, boolean excludeEdges, boolean contourStart) {
		
		//Don't show anything, exclude edgepoints
		int opInt=0;
		if (excludeEdges){
			opInt+=ParticleAnalyzer.EXCLUDE_EDGE_PARTICLES;
		}
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
		
		int type = image.getBufferedImage().getType();
		BufferedImage newIm = new BufferedImage(newWidth, newHeight, type);
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
		newCoord[0] = (float)(x*Math.cos(thetha)-y*Math.sin(thetha));
		newCoord[1] = (float)(x*Math.sin(thetha)+y*Math.cos(thetha));
		return newCoord;
		
	}
	
	
	public static ImageProcessor plot(ImageProcessor im, float[] x, float[] y, Color color){
		
		int numCoords = x.length; 
		if(y.length!=numCoords) return null;
		
		int width = 4000;
		int height = 4000;
		int originX = width/2;
		int originY = height/2;
		int expand = 100;
		if (im==null){
			BufferedImage raw = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
			ImagePlus pl = new ImagePlus();
			pl.setImage(raw);
			im = pl.getProcessor();
		}
		
		im.setColor(color);
		for(int i=0; i<numCoords; i++){
			int xx = (int)(expand*x[i] + originX);
			int yy = (int)(expand*y[i] + originY);
			im.drawOval(xx, yy, 10,10);
		}
		
		
		
		
		return im;
		
	}
	
	public static ImagePlus lessThan(ImagePlus im1, Vector<ImagePlus> im2, boolean orEqualTo){
		return lessThan(im1, im2, -1, orEqualTo);
	}
	public static ImagePlus lessThan(ImagePlus im1, Vector<ImagePlus> im2, int skipInd, boolean orEqualTo){
		
		if(im2.size()==1){
			return lessThan(im1, im2.firstElement(), orEqualTo);
		}
		
		ImageCalculator ic = new ImageCalculator();
		ImagePlus result = new ImagePlus("", new BufferedImage(im1.getWidth(), im1.getHeight(), BufferedImage.TYPE_BYTE_GRAY));
		result.getProcessor().setPixels(1, new FloatProcessor(im1.getWidth(), im1.getHeight()));
		result.getProcessor().invert();//Sets image white
		for (int i=0; i<im2.size(); i++){
			if (i!=skipInd){
				result = ic.run("AND create", result, lessThan(im1, im2.get(i), orEqualTo));
			}
		}

		result.getProcessor().threshold(1);
		//TODO Convert to binary
		return result;
	}
	
	
	
//	public static ImagePlus lessThan(ImagePlus im1, ImagePlus im2 ){
//		return lessThan(im1, im2, false);
//	}
//	
	
	public static ImagePlus lessThan(ImagePlus im1, ImagePlus im2, boolean orEqualTo){
		ImageCalculator ic = new ImageCalculator();
//		ImagePlus result = new ImagePlus();
//		result.getProcessor().setPixels(1, new FloatProcessor(im1.getWidth(), im1.getHeight()));
//		result.getProcessor().invert();//Sets image white
//		result = ic.run("AND", result, ic.run("Subtract", im2, im1));
		if (orEqualTo){
			im2.getProcessor().add(1);
		}
		ImagePlus result = ic.run("Subtract create", im2, im1);
		
		//TODO Convert to binary
		return result;
	}
	
	
}
