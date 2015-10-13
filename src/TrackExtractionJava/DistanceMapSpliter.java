package TrackExtractionJava;

import ij.ImagePlus;
import ij.gui.Roi;
import ij.measure.ResultsTable;
import ij.plugin.ImageCalculator;
import ij.plugin.filter.EDM;
import ij.plugin.filter.ParticleAnalyzer;
import ij.plugin.frame.RoiManager;
import ij.process.ByteProcessor;
import ij.process.ImageProcessor;

import java.util.Vector;

public class DistanceMapSpliter {
	
	public static void main(String[] args){
		
		
		
		
	}
	
	public static Vector<TrackPoint> splitPoint(MaggotTrackPoint itp, int rethreshVal, ExtractionParameters ep, int[] frameSize){
		
		//Threshold im
		ImagePlus rethreshIm = new ImagePlus("Thresh im Frame "+itp.frameNum, itp.im.getBufferedImage());
		rethreshIm.getProcessor().threshold(rethreshVal);
		
		//Find particle rois
		boolean showResults = true;
		ResultsTable rt = new ResultsTable();
		ParticleAnalyzer pa = new ParticleAnalyzer(CVUtils.getPointFindingOptions(showResults, ep.excludeEdges, true), CVUtils.getPointFindingMeasurements(), rt, ep.minArea, ep.maxArea);
		RoiManager rm = new RoiManager();
		ParticleAnalyzer.setRoiManager(rm);
		pa.analyze(rethreshIm);
		Roi[] rois = rm.getRoisAsArray();
		
		//Generate distance maps
		int[] imSize = {itp.getRawIm().getWidth(),itp.getRawIm().getHeight()}; 
		Vector<ImagePlus> dist_maps = generateDistanceMaps(rois, rt, imSize);
				
		//Generate masks
		Vector<ImagePlus> newMasks = new Vector<ImagePlus>();
		for (int j=0; j<dist_maps.size(); j++){
			newMasks.add(CVUtils.lessThan(dist_maps.get(j), dist_maps, j));
		}
		
		Vector<TrackPoint> spPts = new Vector<TrackPoint>();
		ImageCalculator ic = new ImageCalculator();
		//Make new points 
		for (int k=0; k<newMasks.size(); k++){
			
			//Mask the original image
			ImagePlus maskedIm = ic.run("AND", newMasks.get(k), new ImagePlus("",itp.im)); //TODO
			ImagePlus threshIm = new ImagePlus("Thresh im Frame "+itp.frameNum, maskedIm.getProcessor().getBufferedImage());
			threshIm.getProcessor().threshold(ep.globalThreshValue);
			
			//get the new point
			Vector<TrackPoint> newPt = PointExtractor.findPtsInIm(itp.frameNum, maskedIm, threshIm, ep.globalThreshValue, frameSize, itp.rect, ep, false, null);
			
			//Add point to return list
			if (newPt.size()==1){
				spPts.add(newPt.firstElement());
			} else {
				System.out.println("Error splitting point "+itp.pointID+": masked im has multiple points ");
			}
			
		}
		return spPts;
	}
	
	
	public static Vector<ImagePlus> generateDistanceMaps(Roi[] rois, ResultsTable rt, int[] imSize){
		EDM distMapMaker = new EDM();//EDM=Euclidean Distance Map
		Vector<ImagePlus> dist_maps = new Vector<ImagePlus>();
		for (int i=0; i<rois.length; i++){
			//Generate distance map from roi mask 
			ImagePlus mapFromMask = new ImagePlus("Map from mask "+i, new ByteProcessor(imSize[0], imSize[1])); 
			ImageProcessor mask = rois[i].getMask();
			//Paste mask onto image 
			int xloc = (int)rt.getValueAsDouble(ResultsTable.ROI_X, i);
			int yloc = (int)rt.getValueAsDouble(ResultsTable.ROI_Y, i);
			mapFromMask.getProcessor().insert(mask, xloc, yloc);
			mapFromMask.getProcessor().invert();
			
//			new ImagePlus("Mask "+i, mapFromMask.getProcessor().duplicate()).show();
			distMapMaker.toEDM(mapFromMask.getProcessor());
			dist_maps.add(mapFromMask);
		}
		return dist_maps;
	}
	
}
