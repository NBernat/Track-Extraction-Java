package TrackExtractionJava;

import ij.ImagePlus;
import ij.ImageStack;
import ij.gui.Roi;
import ij.measure.ResultsTable;
import ij.plugin.filter.EDM;
import ij.plugin.filter.ParticleAnalyzer;
import ij.plugin.frame.RoiManager;
import ij.process.ByteProcessor;
import ij.process.ImageProcessor;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Vector;

public class DistanceMapSplitter {
	
	
	public static Vector<TrackPoint> splitPoint(ImTrackPoint itp, int npts, int rethreshVal, int targetArea, ExtractionParameters ep, int[] frameSize, Communicator comm){
		
		//Setup debug output
		int debug = 0;//2=intermediate steps; 1=final points; 0=nothing
		ImagePlus rawIm = new ImagePlus("Raw im from point",itp.getRawIm());
		if (debug>1) rawIm.show();
		ImageStack twoMagStack = new ImageStack(rawIm.getWidth(), rawIm.getHeight());
		String magInfo = "Dist map, newmask, maskedim, threshim";
		ImageStack mag0Stack = new ImageStack(rawIm.getWidth(), rawIm.getHeight());
		ImageStack mag1Stack = new ImageStack(rawIm.getWidth(), rawIm.getHeight());
		if (debug>1) twoMagStack.addSlice(rawIm.getProcessor());
		
		
		//Rethreshold the image
		ImagePlus rethreshIm = new ImagePlus("Thresh im Frame "+itp.frameNum+"("+rethreshVal+", orig="+itp.thresh+")", itp.im.getBufferedImage());
		double mint = rethreshIm.getProcessor().getMinThreshold();
		double maxt = rethreshIm.getProcessor().getMaxThreshold();
		rethreshIm.getProcessor().setThreshold((double) rethreshVal, (double) 255, ImageProcessor.NO_LUT_UPDATE);
		rethreshIm.getProcessor().threshold(rethreshVal);
		
		if (debug>1){
			rethreshIm.show();
			twoMagStack.addSlice(rethreshIm.getProcessor());
		}
		
		//Find new particle roi's
		boolean showResults = false;
		ResultsTable rt = new ResultsTable();
		ParticleAnalyzer pa = new ParticleAnalyzer(CVUtils.getPointFindingOptions(showResults, false, true), CVUtils.getPointFindingMeasurements(), rt, ep.minSubMaggotArea, ep.maxArea);
		RoiManager rm = RoiManager.getInstance();
		if (rm==null) rm = new RoiManager(true);// This constructor "constructs an ROIManager without displaying it." regardless of the argument's value
		ParticleAnalyzer.setRoiManager(rm);
		pa.analyze(rethreshIm);
		Roi[] rois = rm.getRoisAsArray();
		rethreshIm.getProcessor().setThreshold(mint, maxt, ImageProcessor.NO_LUT_UPDATE);//reset the threshold range 
		
		//Quit if the rethresholding didn't provide the proper # of points
		if (rm.getCount()<npts){
			if (debug>1 && comm!=null){//numFailed<10){
				comm.message("Point Splitter: Too few points found ("+rm.getCount()+", not "+npts+") after rethresholding (new thresh "+rethreshVal+"); frame"+itp.frameNum+" point "+itp.pointID+"("+(int)itp.x+","+(int)itp.y+")", VerbLevel.verb_error);
			}
			return null;
		}
		rm.removeAll();
		rm.close();
		
		//Generate distance maps
		int[] imSize = {itp.getRawIm().getWidth(),itp.getRawIm().getHeight()}; 
		Vector<ImagePlus> dist_maps = DistanceMapSplitter.generateDistanceMaps(rois, rt, imSize);
		
		//Quit if the distance map produced an error
		if (dist_maps.size()<2){
			if (comm!=null){
				comm.message("Point Splitter: Only one distance map was created", VerbLevel.verb_warning);
			}
			return null;
		}
		if (debug>1){
			mag0Stack.addSlice(dist_maps.get(0).getProcessor().duplicate());
			mag1Stack.addSlice(dist_maps.get(1).getProcessor().duplicate());
		}
		
		//Split the frame by distance, and save masks
		Vector<ImagePlus> newMasks = new Vector<ImagePlus>();
		for (int j=0; j<dist_maps.size(); j++){
			ImagePlus dm = dist_maps.remove(j);
			newMasks.add(CVUtils.compareImages(CVUtils.LTE, dm, dist_maps));
			dist_maps.add(j, dm);
		}
		if (debug>1){
			mag0Stack.addSlice(newMasks.get(0).getProcessor().duplicate());
			mag1Stack.addSlice(newMasks.get(1).getProcessor().duplicate());
		}
		
		//Setup for debugging
		Vector<ImageProcessor> maskedIms = new Vector<ImageProcessor>();
		Vector<ImageProcessor> threshIms = new Vector<ImageProcessor>();
		
		//Generate new points
		Vector<TrackPoint> spPts = new Vector<TrackPoint>();
		for (int k=0; k<newMasks.size(); k++){
			
			//Mask all but the k'th larva out of the original image
			ImagePlus maskedIm = CVUtils.maskIm(new ImagePlus("",itp.im), newMasks.get(k));
			if (debug>1) maskedIms.add(maskedIm.getProcessor().duplicate());
			
			//Use original threshold to generate new points from the masked im
			ImagePlus threshIm = new ImagePlus("Thresh im Frame "+itp.frameNum, maskedIm.getProcessor().duplicate());
			threshIm.getProcessor().setThreshold((double) ep.globalThreshValue, (double) 255, ImageProcessor.NO_LUT_UPDATE);
			threshIm.getProcessor().threshold(ep.globalThreshValue);
			if (debug>1) threshIms.add(threshIm.getProcessor().duplicate());			
			
			
			//Get the new point
			try{
				Vector<TrackPoint> newPt = PointExtractor.findPtsInIm(itp.frameNum, maskedIm, threshIm, ep.globalThreshValue, frameSize, itp.rect, ep, false, null);
				
				//Add point to return list
				if (newPt.size()==1){
					spPts.add(newPt.firstElement());
				} else {
					if (debug>1) System.out.println("Error splitting point "+itp.pointID+": masked im has multiple points ");
				}
			} catch (Exception e){
				StringWriter sw = new StringWriter();
				PrintWriter prw = new PrintWriter(sw);
				e.printStackTrace(prw);
			}
			
		}
		
		if (debug>1){
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
				
		}

		if (debug>0){
			//Show points
			Vector<TrackPoint> showPts = new Vector<TrackPoint>();
			showPts.add(itp);
			showPts.addAll(spPts);
			Track newTr = new Track(showPts, 0);
			newTr.playMovie();
		}
		
		return spPts;
	}
	
	
	private static Vector<ImagePlus> generateDistanceMaps(Roi[] rois, ResultsTable rt, int[] imSize){
		EDM distMapMaker = new EDM();//EDM=Euclidean Distance Map
		Vector<ImagePlus> dist_maps = new Vector<ImagePlus>();
		for (int i=0; i<(rt.getCounter()); i++){
			
			//Generate distance map from roi mask 
			ImagePlus mapFromMask = new ImagePlus("Map from mask "+i, new ByteProcessor(imSize[0], imSize[1])); 
			ImageProcessor mask = rois[i].getMask();
			
			//Paste mask onto image 
			int xloc = (int)rt.getValueAsDouble(ResultsTable.ROI_X, i);
			int yloc = (int)rt.getValueAsDouble(ResultsTable.ROI_Y, i);
			mapFromMask.getProcessor().insert(mask, xloc, yloc);
			mapFromMask.getProcessor().invert();
			
			distMapMaker.toEDM(mapFromMask.getProcessor());
			dist_maps.add(mapFromMask);
		}
		return dist_maps;
	}
	
}
