package TrackExtractionJava;

import ij.ImagePlus;
import ij.ImageStack;
import ij.gui.Roi;
import ij.measure.ResultsTable;
import ij.plugin.ImageCalculator;
import ij.plugin.filter.EDM;
import ij.plugin.filter.ParticleAnalyzer;
import ij.plugin.frame.RoiManager;
import ij.process.ByteProcessor;
import ij.process.ImageProcessor;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Vector;

public class DistanceMapSplitter {
	
	public static int numFailed = 0;
	
//	public static void main(String[] args){
//	}
	
	public static Vector<TrackPoint> splitPoint(ImTrackPoint itp, int npts, int rethreshVal, int targetArea, ExtractionParameters ep, int[] frameSize, Communicator comm){
		//Prefs.blackBackground = false;
		boolean debug = true;
		
		
		ImagePlus rawIm = new ImagePlus("Raw im from point",itp.getRawIm());
		if (debug) rawIm.show();
		ImageStack twoMagStack = new ImageStack(rawIm.getWidth(), rawIm.getHeight());
		String magInfo = "Dist map, newmask, maskedim, threshim";
		ImageStack mag0Stack = new ImageStack(rawIm.getWidth(), rawIm.getHeight());
		ImageStack mag1Stack = new ImageStack(rawIm.getWidth(), rawIm.getHeight());
		twoMagStack.addSlice(rawIm.getProcessor());
		
		ImagePlus rethreshIm = new ImagePlus("Thresh im Frame "+itp.frameNum+"("+rethreshVal+", orig="+itp.thresh+")", itp.im.getBufferedImage());

		double mint = rethreshIm.getProcessor().getMinThreshold();
		double maxt = rethreshIm.getProcessor().getMaxThreshold();
		
		rethreshIm.getProcessor().setThreshold((double) rethreshVal, (double) 255, ImageProcessor.NO_LUT_UPDATE);
		
		rethreshIm.getProcessor().threshold(rethreshVal);
		if (debug) rethreshIm.show();
		twoMagStack.addSlice(rethreshIm.getProcessor());

		
		//Find particle rois
		boolean showResults = false;
		ResultsTable rt = new ResultsTable();
		ParticleAnalyzer pa = new ParticleAnalyzer(CVUtils.getPointFindingOptions(showResults, false, true), CVUtils.getPointFindingMeasurements(), rt, ep.minSubMaggotArea, ep.maxArea);
		RoiManager rm = RoiManager.getInstance();
		if (rm==null){
			rm = new RoiManager(true);
		}
		ParticleAnalyzer.setRoiManager(rm);
		pa.analyze(rethreshIm);
		Roi[] rois = rm.getRoisAsArray();
		rethreshIm.getProcessor().setThreshold(mint, maxt, ImageProcessor.NO_LUT_UPDATE);
		
		
		if (rm.getCount()<npts){
			if (debug && comm!=null){//numFailed<10){
				comm.message("Point Splitter: Too few points found ("+rm.getCount()+", not "+npts+") after rethresholding (new thresh "+rethreshVal+"); frame"+itp.frameNum+" point "+itp.pointID+"("+(int)itp.x+","+(int)itp.y+")", VerbLevel.verb_error);
			}
			numFailed++;
			return null;
		}
		rm.removeAll();
		rm.close();
		
		//Generate distance maps
		int[] imSize = {itp.getRawIm().getWidth(),itp.getRawIm().getHeight()}; 
		Vector<ImagePlus> dist_maps = DistanceMapSplitter.generateDistanceMaps(rois, rt, imSize);
		if (dist_maps.size()<2){
			if (comm!=null){
				comm.message("Point Splitter: Only one distance map was created", VerbLevel.verb_warning);
			}
			numFailed++;
			return null;
		}
		mag0Stack.addSlice(dist_maps.get(0).getProcessor().duplicate());
		mag1Stack.addSlice(dist_maps.get(1).getProcessor().duplicate());
		
		
		Vector<ImagePlus> newMasks = new Vector<ImagePlus>();
		for (int j=0; j<dist_maps.size(); j++){
			newMasks.add(CVUtils.lessThan(dist_maps.get(j), dist_maps, j, true));
		}
		mag0Stack.addSlice(newMasks.get(0).getProcessor().duplicate());
		mag1Stack.addSlice(newMasks.get(1).getProcessor().duplicate());
		
		Vector<ImageProcessor> maskedIms = new Vector<ImageProcessor>();
		Vector<ImageProcessor> threshIms = new Vector<ImageProcessor>();
		Vector<TrackPoint> spPts = new Vector<TrackPoint>();
		ImageCalculator ic = new ImageCalculator();
		
		for (int k=0; k<newMasks.size(); k++){
			
			//Mask the original image
			ImagePlus maskedIm = ic.run("AND create", newMasks.get(k), new ImagePlus("",itp.im)); 
			maskedIms.add(maskedIm.getProcessor().duplicate());
			ImagePlus threshIm = new ImagePlus("Thresh im Frame "+itp.frameNum, maskedIm.getProcessor().duplicate());
			
			threshIm.getProcessor().threshold(ep.globalThreshValue);
			threshIms.add(threshIm.getProcessor().duplicate());			
			
			
			//get the new point
			try{
				Vector<TrackPoint> newPt = PointExtractor.findPtsInIm(itp.frameNum, new ImagePlus("maskedIm "+k, maskedIms.get(k)), threshIm, ep.globalThreshValue, frameSize, itp.rect, ep, false, null);
				
				//Add point to return list
				if (newPt.size()==1){
					spPts.add(newPt.firstElement());
				} else {
					System.out.println("Error splitting point "+itp.pointID+": masked im has multiple points ");
				}
			} catch (Exception e){
				StringWriter sw = new StringWriter();
				PrintWriter prw = new PrintWriter(sw);
				e.printStackTrace(prw);
			}
			
			
		}
		
		if (debug){
			mag0Stack.addSlice(maskedIms.get(0));
			new ImagePlus("maskedIm=currentIm", maskedIms.get(1)).show();
			mag1Stack.addSlice(maskedIms.get(1));
			
			ImageStack maskedImStack = new ImageStack(maskedIms.get(0).getWidth(), maskedIms.get(0).getHeight());
			for (int i=0; i<maskedIms.size(); i++){
				maskedImStack.addSlice(maskedIms.get(i));
			}
			new ImagePlus("MaskedIms", maskedImStack).show();
			
			mag0Stack.addSlice(threshIms.get(0));
			mag1Stack.addSlice(threshIms.get(1));
			
			new ImagePlus("Original collision", twoMagStack).show();
			new ImagePlus("M0: "+magInfo, mag0Stack).show();
			new ImagePlus("M1: "+magInfo, mag1Stack).show();
			
			
			//Show points
			Vector<TrackPoint> showPts = new Vector<TrackPoint>();
			showPts.add(itp);
			showPts.addAll(spPts);
			Track newTr = new Track(showPts, 0);
			newTr.playMovie();
		}
		
		
		return spPts;
	}
	
	
	public static Vector<ImagePlus> generateDistanceMaps(Roi[] rois, ResultsTable rt, int[] imSize){
		EDM distMapMaker = new EDM();//EDM=Euclidean Distance Map
		Vector<ImagePlus> dist_maps = new Vector<ImagePlus>();
		for (int i=0; i<(rt.getCounter()); i++){
			//Generate distance map from roi mask 
			ImagePlus mapFromMask = new ImagePlus("Map from mask "+i, new ByteProcessor(imSize[0], imSize[1])); 
			ImageProcessor mask = rois[i].getMask();
			new ImagePlus("mask "+i, mask).show();
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
