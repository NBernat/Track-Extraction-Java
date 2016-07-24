package TrackExtractionJava;

import ij.gui.Plot;

import java.awt.Color;

public class MathUtils {

	
	public static double[] removeNonFinite(double[] vals) {
		int n = 0;
		for (double v : vals) {
			if (!Double.isNaN(v) && !Double.isInfinite(v)) {
				++n;
			}
		}
		double[] newvals = new double[n];
		int j = 0;
		for (double v : vals) {
			if (!Double.isNaN(v) && !Double.isInfinite(v)) {
				newvals[j++] = v;
			}
		}
		return newvals;
		
	}
	
	public static double mean(double[] vals) {
		return mean(vals, true);
	}
	
	public static double mean(double[] vals, boolean cleanInfinities){
		if (cleanInfinities) {
			vals = removeNonFinite(vals);
		}
		double sum=0;
		for(int i=0; i<vals.length; i++){
			sum += vals[i];
		}
		return sum/vals.length;
	}
	
	public static double stdDev(double[] vals){
		vals = removeNonFinite(vals);
		return stdDev(vals, mean(vals,false), false);
	}
	
	public static double stdDev(double[] vals, double mean, boolean cleanInfinities){
		if (cleanInfinities) {
			vals = removeNonFinite(vals);
		}
		double varSum = 0;
		for(int i=0; i<vals.length; i++){
			varSum += (vals[i]-mean)*(vals[i]-mean);
		}
		
		return Math.sqrt(varSum/vals.length);
	}
	public static double stdDev(double[] vals, double mean){
		return stdDev(vals, mean, true);
	}
	
	/**
	 * Plots data, mean, and standard deviation. Requires an instance of ImageJ, e.g.
	 * IJ ij = new IJ();
	 * ...
	 * plotDataMeanStdDev(data,title,dataLabel);
	 * ...
	 * ij.quit();
	 * 
	 * 
	 * @param data
	 * @param title
	 * @param dataLabel
	 */
	public static void plotDataMeanStdDev(double[] data, String title, String dataLabel){
		
		double mean = MathUtils.mean(data);
		double stdDev = MathUtils.stdDev(data, mean);
		
		plotDataMeanStdDev(data, mean, stdDev, title, dataLabel, 0);
	}	
		
	public static void plotDataMeanStdDev(double[] data, double mean, double stdDev,  String title, String dataLabel, int startFrame){
		
		int dataNum = 500;
		if (data.length>dataNum){
			// TODO calc mean and std dev NOW
			
			
			int numPlots = data.length/dataNum;
			for (int i=0; i<numPlots; i++){
				double[] dataTemp = new double[dataNum];
				for (int j=0; j<dataNum; j++){
					dataTemp[j] = data[i*dataNum+j];
				}
				plotDataMeanStdDev(dataTemp, mean, stdDev, title+" (Frames "+(i*dataNum)+"-"+((i+1)*dataNum-1)+")", dataLabel, i*dataNum);
				
			}
			
			if (data.length%dataNum>0){
				double[] dataTemp = new double[data.length%dataNum];
				int i=numPlots;
				for (int j=0; j<data.length%dataNum; j++){
					dataTemp[j] = data[i*dataNum+j];
				}
				plotDataMeanStdDev(dataTemp, mean, stdDev, title+" (Frames "+(i*dataNum)+"-"+((i+1)*dataNum-1)+")", dataLabel, i*dataNum);
				
			}
			
			
			return;
		}
		
		int scaleFactor = 1;
		
		double[] frame = new double[data.length*scaleFactor];
		double[] dataScaled = new double[data.length*scaleFactor];
		double maxY = 0;
		double maxYi = -1;
		for (int i=1;i<=data.length; i++) {
			if (data[i-1]>maxY){
				maxY = data[i-1];
				maxYi = i-1;
			}
			
 			for (int j=0;j<scaleFactor; j++) {
 				frame[(i-1)*scaleFactor+j]=(startFrame-1+i)*scaleFactor+j;
 				dataScaled[(i-1)*scaleFactor+j] = data[i-1];
 			}
		}
		Plot p = new Plot(title, "frame", dataLabel, frame, dataScaled);
//		Dimension d = p.getSize(); //apparently does not exist
//		int sf = 3;
//		p.setSize(d.width*sf, d.height*sf);
//		p.setScale(3); //apparently does not exist
		p.setColor(Color.BLUE);
		p.drawDottedLine((startFrame-1)*scaleFactor, mean, (startFrame-1)*scaleFactor+data.length*scaleFactor+1, mean, 1);
		p.setColor(Color.RED);
		p.drawDottedLine((startFrame-1)*scaleFactor, mean+stdDev, (startFrame-1)*scaleFactor+data.length*scaleFactor+1, mean+stdDev, 1);
		p.drawDottedLine((startFrame-1)*scaleFactor, mean-stdDev, (startFrame-1)*scaleFactor+data.length*scaleFactor+1, mean-stdDev, 1);
		p.show();
//		p.makeHighResolution(title, 3, true, true); //apparently does not exist
	}
	
	public static void plotDataMeanStdDev(Float[] data, String title, String dataLabel){
		plotDataMeanStdDev(castFloatArray2Double(data), title, dataLabel);
		
	}
	
	public static double[] castFloatArray2Double(Float[] data){
		double[] d = new double[data.length];
		for (int i=0; i<data.length; i++) d[i] = (double)data[i];
		return d;
	}
	public static double[] castFloatArray2Double(float[] data){
		double[] d = new double[data.length];
		for (int i=0; i<data.length; i++) d[i] = (double)data[i];
		return d;
	}
	
	public static double[] castIntArray2Double(int[] data){
		double[] d = new double[data.length];
		for (int i=0; i<data.length; i++) d[i] = (double)data[i];
		return d;
	}

	public static double mean(int[] vals) {
		return mean(castIntArray2Double(vals));
	}
	public static double mean(float[] vals) {
		return mean(castFloatArray2Double(vals));
	}
	
	public static double curveLength(double[] x, double[] y) {
		double len = 0;
		for (int i = 1; i < x.length; ++i) {
			len += Math.sqrt(Math.pow(x[i]-x[i-1], 2) + Math.pow(y[i]-y[i-1], 2));
		}
		return len;
	}
	
}
