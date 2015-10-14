package TrackExtractionJava;


import java.io.File;

import ij.ImagePlus;
import ij.plugin.PlugIn;


public class Test_ implements PlugIn {//extends JFrame

	
	

	public static void main(String[] args) {
		
		
		//Grab experiment
		String dir = "C:\\Users\\Natalie\\Documents\\Testing\\Building and Fitting - TestExProc\\Fitting from Prejav\\Divergence investigation\\2 magpix bug fixed";
		String filename = "divergedTrackExp.prejav";
		Experiment ex = new Experiment(Experiment.fromPath(new File(dir, filename).getPath())); 

		//Grab point
		int dataInd = 6;
		int[] trackNums = {10, 0,   3, 16, 17, 19, 20};
		int[] pointInds = { 0, 0, 184,  0,  0,  0,  0};
		int[] numPts    = { 2, 2,   2,  2,  3,  2,  2};
		int trackNum = trackNums[dataInd];
		Track tr = ex.getTrack(trackNum);
		ImTrackPoint itp = (ImTrackPoint)tr.points.get(pointInds[dataInd]);
		
		ExtractionParameters ep = new ExtractionParameters();
		int nPts = numPts[dataInd];

		int targetArea = 50;
		int rethreshVal = CVUtils.findThreshforNumPts(new ImagePlus("",itp.getRawIm().duplicate()), ep, nPts, (int)ep.minSubMaggotArea, (int)ep.maxArea, targetArea, itp.thresh, 255);//117;//139;//
		
		int[] frameSize = {2592,1944}; 
		
		DistanceMapSpliter.splitPoint(itp, nPts, rethreshVal, targetArea, ep, frameSize);
		
		
		
		/*
		ImagePlus rawIm = new ImagePlus("Raw im from point",itp.getRawIm());
//		ra0wIm.show();
		ImageStack twoMagStack = new ImageStack(rawIm.getWidth(), rawIm.getHeight());
		//Dist map, newmask, maskedim, threshim
		String magInfo = "Dist map, newmask, maskedim, threshim";
		ImageStack mag0Stack = new ImageStack(rawIm.getWidth(), rawIm.getHeight());
		ImageStack mag1Stack = new ImageStack(rawIm.getWidth(), rawIm.getHeight());
		twoMagStack.addSlice(rawIm.getProcessor());
		
		
		//Find new thresh
//		ExtractionParameters ep = new ExtractionParameters();
//		int nPts = numPts[dataInd];
		
//		int targetArea = 50;
//		int rethreshVal = CVUtils.findThreshforNumPts(new ImagePlus("",itp.getRawIm().duplicate()), ep, nPts, (int)ep.minSubMaggotArea, (int)ep.maxArea, targetArea, itp.thresh, 255);//117;//139;//
		
		//Threshold im
		ImagePlus rethreshIm = new ImagePlus("Thresh im Frame "+itp.frameNum+"("+rethreshVal+", orig="+itp.thresh+")", itp.im.getBufferedImage());
		rethreshIm.getProcessor().threshold(rethreshVal);
//		rethreshIm.show();
		twoMagStack.addSlice(rethreshIm.getProcessor());
		String outMessage = "Rethreshed im to threshVal="+rethreshVal+"\n";
		new TextWindow("Rethresh", outMessage, 500, 500);

		//Find particle rois
		boolean showResults = false;
		ResultsTable rt = new ResultsTable();
		ParticleAnalyzer pa = new ParticleAnalyzer(CVUtils.getPointFindingOptions(showResults, ep.excludeEdges, true), CVUtils.getPointFindingMeasurements(), rt, ep.minSubMaggotArea, ep.maxArea);
		RoiManager rm = new RoiManager();
		ParticleAnalyzer.setRoiManager(rm);
		pa.analyze(rethreshIm);
		Roi[] rois = rm.getRoisAsArray();
		if (rm.getCount()<=1){
			new TextWindow("Point Splitter: Error message", "Only one point was found after rethresholding", 500, 500);
			rethreshIm.setTitle("Error: only one point (thresholded image)");
			rethreshIm.show();
			new ImagePlus("Error: original image", itp.getRawIm()).show();;
			return;
		}
//		int nRois = rm.getCount();
//		outMessage += "After analyzing particles: results table("+rt.getCounter()+") roi manager("+rm.getCount()+") roi array("+rois.length+") \n";

		
		//Generate distance maps
		int[] imSize = {itp.getRawIm().getWidth(),itp.getRawIm().getHeight()}; 
		Vector<ImagePlus> dist_maps = DistanceMapSpliter.generateDistanceMaps(rois, rt, imSize);
		if (dist_maps.size()<2){
			new TextWindow("Point Splitter: Error message", "Only one distance map was created", 500, 500);
			return;
		}
		mag0Stack.addSlice(dist_maps.get(0).getProcessor().duplicate());
		mag1Stack.addSlice(dist_maps.get(1).getProcessor().duplicate());
//		for (int n=0; n<dist_maps.size(); n++){
//			dist_maps.get(n).show();
//		}
		
		
		//Generate masks
//		ImageCalculator imc = new ImageCalculator();
//		ImagePlus result = imc.run("Subtract create", dist_maps.get(1), dist_maps.get(0));
//		result.show();
//		result.getProcessor().duplicate().threshold(1);
//		result.show();
		
		
		Vector<ImagePlus> newMasks = new Vector<ImagePlus>();
		for (int j=0; j<dist_maps.size(); j++){
			newMasks.add(CVUtils.lessThan(dist_maps.get(j), dist_maps, j, true));
//			newMasks.get(j).show();
		}
		mag0Stack.addSlice(newMasks.get(0).getProcessor().duplicate());
		mag1Stack.addSlice(newMasks.get(1).getProcessor().duplicate());
		
		Vector<ImageProcessor> maskedIms = new Vector<ImageProcessor>();
		Vector<ImageProcessor> threshIms = new Vector<ImageProcessor>();
		Vector<TrackPoint> spPts = new Vector<TrackPoint>();
		ImageCalculator ic = new ImageCalculator();
//		int[] frameSize = {2592,1944}; 
		//Make new points 
		for (int k=0; k<newMasks.size(); k++){
			
			//Mask the original image
			ImagePlus maskedIm = ic.run("AND create", newMasks.get(k), new ImagePlus("",itp.im)); 
//			new ImagePlus("maskedIm "+k+" before", maskedIm.getProcessor().duplicate()).show();
			maskedIms.add(maskedIm.getProcessor().duplicate());
//			new ImagePlus("maskedIm "+k+" before", maskedIm.getProcessor().duplicate()).show();
			ImagePlus threshIm = new ImagePlus("Thresh im Frame "+itp.frameNum, maskedIm.getProcessor().duplicate());
//			new ImagePlus("threshIm "+k+" before", threshIm.getProcessor().duplicate()).show();
			maskedIms.get(k).resetMinAndMax(); 
			threshIm.getProcessor().threshold(ep.globalThreshValue);
			threshIms.add(threshIm.getProcessor().duplicate());			
//			new ImagePlus("threshIm "+k+" after", threshIm.getProcessor().duplicate()).show();
//			new ImagePlus("maskedIm "+k+" after", maskedIm.getProcessor().duplicate()).show();
			//get the new point
			try{
//				new ImagePlus("maskedIm=currentIm", maskedIms.get(k)).show();
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
//				outMessage+="Error making point "+k+":\n"+sw.toString();
			}
			
			
		}
//		new ImagePlus("maskedIm=currentIm", maskedIms.get(0)).show();
		mag0Stack.addSlice(maskedIms.get(0));
//		new ImagePlus("maskedIm=currentIm", maskedIms.get(1)).show();
		mag1Stack.addSlice(maskedIms.get(1));
		
//		ImageStack maskedImStack = new ImageStack(maskedIms.get(0).getWidth(), maskedIms.get(0).getHeight());
//		for (int i=0; i<maskedIms.size(); i++){
//			maskedImStack.addSlice(maskedIms.get(i));
//		}
//		new ImagePlus("MaskedIms", maskedImStack).show();
		
		mag0Stack.addSlice(threshIms.get(0));
		mag1Stack.addSlice(threshIms.get(1));
		
//		new TextWindow("Rethreshold test output", outMessage, 500, 500);
		new ImagePlus("Original collision", twoMagStack).show();
		new ImagePlus("M0: "+magInfo, mag0Stack).show();
		new ImagePlus("M1: "+magInfo, mag1Stack).show();
		
		Track newTr = new Track(spPts, 0);
		newTr.playMovie();
		*/
//		ImageStack contourStack = new ImageStack();
		
//		for (TrackPoint mtp: tr.points){
//						
//			if (mtp.thresh!=20){
////				ByteProcessor thIm = new ByteProcessor(mtp.im.getBufferedImage());
////				thIm.threshold(mtp.thresh);
////				ImagePlus imp = new ImagePlus("Frame "+mtp.frameNum+", thresh "+mtp.thresh, thIm);
////				imp.show();
//			}
//			
//			Vector<ContourPoint> c = mtp.findContours();
//			mtp.convertCPtoArrays(c);
//			
//		}
//		tr.playMovie();
		
		
		/*
		String path = "C:\\Users\\Natalie\\Documents\\1cm_checkerboard.png";
		
		if (args!=null && args.length>=1){
			path = args[0];
		}
		
		
		File f = new File(path);
		
		System.out.println(path);
		System.out.println(f.getParent());
		 */
//
//		//////////////
//		// TEST FINDTHRESHFORNUMPTS
//		//////////////
//		//Get the info from the track
//		String path = "C:\\Users\\Natalie\\Documents\\TestExProc\\Collision testing\\berlin@berlin__LIGHT_RANDOM_WALK_S1_112Hz_201402121807_MTP.jav";
//		Experiment ex = Experiment.fromPath(path);
//		Track tr = ex.getTrack(7);
//		MaggotTrackPoint tp = (MaggotTrackPoint)tr.getPoint(0);
//		ExtractionParameters ep = new ExtractionParameters();
//		ep.excludeEdges = false;
//		
//		//Play the movie
//		MaggotDisplayParameters mdp = new MaggotDisplayParameters();
//		mdp.expandFac = 1;
//		mdp.setAllFalse();
//		tr.playMovie(mdp);
//		
//		//Show the image at various stages
//		ImageProcessor im = tp.getRawIm();
//		ImageStack is = new ImageStack(im.getWidth(), im.getHeight());
//		is.addSlice(im);
//		//Add a thresholded image to the stack
//		ImageProcessor thIm = im.duplicate();
//		thIm.threshold((int)ep.globalThreshValue);
//		is.addSlice(thIm);
//		
//		//Find the threshold for 2 maggots and show that thresholded image
//		int targetArea = (int)tr.getPoints().lastElement().area;
//		
//		//look at the "goodness values" in order to debug findThreshforNumPts
//		/*
//		StringBuilder sb = new StringBuilder();
//		for(int i=(int)ep.globalThreshValue; i<=255; i++){
//			ImageProcessor thIm3 = im.duplicate();
//			thIm3.threshold(i);
//			double goodness = CVUtils.findGoodness(new ImagePlus("im", thIm3), ep, 2, (int)ep.minArea, (int)ep.maxArea, targetArea);
//			sb.append("Thresh "+i+": gv="+goodness+"\n");
//		}
//		new TextWindow("Goodness values", sb.toString(), 500, 500);
//		*/
//		
//		//Final Version of findThreshforNumPts
//		int thFor2Mags = CVUtils.findThreshforNumPts(new ImagePlus("im", im.duplicate()), ep, 2, (int)ep.minArea, (int)ep.maxArea, targetArea, tp.thresh, 255);
//		
//		ImageProcessor thIm2 = im.duplicate();
//		thIm2.threshold(thFor2Mags);
//		is.addSlice(thIm2);
//		//Show the stack
//		ImagePlus imp = new ImagePlus("Frame "+tp.frameNum+"; ("+tp.rect.x+","+tp.rect.y+") ; Thresholding; target area ="+targetArea+"; auto val ="+thFor2Mags, is);
//		imp.show();
//		
//		//////////////
//		// TEST SPLITPT2NPTS
//		//////////////
//		//Generate a pointExtractor to use in splitting points
//		IJ.showStatus("Opening MMF...");	
//		String dir = "C:\\Users\\Natalie\\Documents\\TestExProc\\Collision testing";
//		String filename = "berlin@berlin__LIGHT_RANDOM_WALK_S1_112Hz_201402121807.mmf";
//		IJ.run("Import MMF", "path=["+new File(dir, filename).getPath()+"]");
//		ImageWindow mmfWin = WindowManager.getCurrentWindow();
//		ImagePlus mmfStack = mmfWin.getImagePlus();
//		Communicator c = new Communicator();
//		c.setVerbosity(VerbLevel.verb_debug);
//		PointExtractor pe = new PointExtractor(mmfStack.getStack(), c, ep);
//		mmfWin.close();
//		
//		
//		//Set the proper Analysis rect
//		pe.setAnalysisRect(tp.rect);
//
//		//Do extractPoints() machinery to generate splitPts
////		pe.loadFrame(tp.frameNum);//in pe.extractPoints
////		pe.rethresh(thFor2Mags);//in pe.extractPoints
////		c.message("Finding points in AR=("+pe.getAnalysisRect().x+","+pe.getAnalysisRect().y+") w="+pe.getAnalysisRect().width+", h="+pe.getAnalysisRect().height, VerbLevel.verb_debug);
////		ResultsTable pointTable = CVUtils.findPoints(pe.threshIm, pe.getAnalysisRect(), ep, false);//in pe.findpointsinim
////		pointTable.show("Points...");
////		Vector<TrackPoint> splitPts = pe.rt2TrackPoints(pointTable, pe.currentFrameNum, thFor2Mags);//in pe.findpointsinim
//
//		//Extract points to generate splitPts 
////		pe.extractPoints(tp.frameNum, thFor2Mags);
////		Vector<TrackPoint> splitPts = pe.getPoints();
//		
//		//Final version of splitPts2NPts 
//		Vector<TrackPoint> splitPts = MaggotTrackPoint.splitPt2NPts(tp, 2, targetArea, pe, ep);
//		c.message("\n \n \nPoint Info:", VerbLevel.verb_debug);
//		for (TrackPoint sp : splitPts){
//			c.message(sp.getTPDescription(), VerbLevel.verb_debug);
//		}
//		
//		if (splitPts!=null){
//			
//			
//			if (splitPts.size()==2){
//				for (TrackPoint p : splitPts){
//					MaggotTrackPoint mp = (MaggotTrackPoint) p;
//					new ImageWindow(new ImagePlus("", mp.getIm()));
//					new ImageWindow(new ImagePlus("", mp.getMask()));
//				}
//				
//				
//				
//				//////////////
//				// TEST MATCHNPTS2NTRACKS
//				//////////////
//				
//				Vector<Track> colTracks = new Vector<Track>();
//				colTracks.add(ex.getTrack(4));
//				colTracks.add(ex.getTrack(5));
//				
//				Vector<TrackMatch> matches = new Vector<TrackMatch>();
//				//compare each
//				double dist1 = colTracks.get(0).distFromEnd(splitPts.get(0))+colTracks.get(1).distFromEnd(splitPts.get(1));
//				double dist2 = colTracks.get(0).distFromEnd(splitPts.get(1))+colTracks.get(1).distFromEnd(splitPts.get(0));
//				if (dist1<dist2){
//					matches.add(new TrackMatch(colTracks.get(0), splitPts.get(0), ep.maxMatchDist, null));
//					matches.add(new TrackMatch(colTracks.get(1), splitPts.get(1), ep.maxMatchDist, null));
//				} else{
//					matches.add(new TrackMatch(colTracks.get(0), splitPts.get(1), ep.maxMatchDist, null));
//					matches.add(new TrackMatch(colTracks.get(1), splitPts.get(0), ep.maxMatchDist, null));
//				}
//				
//				
//				c.message("\n \n \nMatch Making:", VerbLevel.verb_debug);
//				matches.get(0).spillInfoToCommunicator(c);
//				matches.get(1).spillInfoToCommunicator(c);
//				
//				
//				
//				
//				//Final version of matchNPts2NTracks
////				Vector<TrackMatch> newMatches = TrackMatch.matchNPts2NTracks(splitPts, colTracks, ep.maxMatchDist);
//				
//				
//			} else {
//				ImageStack ptSt = new ImageStack(500, 500);
//				for (TrackPoint p : splitPts){
//					ImageProcessor pr = p.getIm();
////					if (pr.getHeight()<=500 && pr.getWidth()<=500){
//						ByteProcessor bp = new ByteProcessor(500, 500);
//						bp.copyBits(pr, 0, 0, Blitter.COPY);
//						ptSt.addSlice(bp);
//	//					stb.append(p.getTPDescription()+"\n");
////					}
//				}
//				new ImagePlus("SplitPts", ptSt).show();
////				new TextWindow(":\\", stb.toString(), 500, 500);
//			}
//			
//			new TextWindow("Point Extraction, Matching", c.outString, 500, 500);
//			
//			
//		} else {
//			new TextWindow(":(", "No points", 500, 500);
//		}
		
	}
	
	public void run(String arg0) {
		main(null);
//		OpenDialog od = new OpenDialog("Choose an experiment (.mmf or .ser)", null);
//		String path = od.getPath();
////		String path = "C:\\Users\\Natalie\\Documents\\TestExProc\\unmaskTest.jpg";
//		ImagePlus imp = IJ.openImage(path);
//		imp.show();

//		IJ.showStatus("SIGN OF LIFE");
//		String path = "C:\\Users\\Natalie\\Documents\\TestExProc\\Collision testing\\berlin@berlin__LIGHT_RANDOM_WALK_S1_112Hz_201402121807_MTP.jav";
//		Experiment ex = Experiment.fromPath(path);
//		Track tr = ex.getTrack(7);
//		tr.playMovie();
//		IJ.showStatus("Playing track");
	}

}
