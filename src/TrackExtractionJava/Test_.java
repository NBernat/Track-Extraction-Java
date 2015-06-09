package TrackExtractionJava;


import java.awt.Rectangle;
import java.io.File;
import java.util.Vector;

import ij.IJ;
import ij.ImagePlus;
import ij.ImageStack;
import ij.WindowManager;
import ij.gui.ImageWindow;
import ij.measure.ResultsTable;
import ij.plugin.PlugIn;
import ij.process.Blitter;
import ij.process.ByteProcessor;
import ij.process.ImageProcessor;
import ij.text.TextWindow;


public class Test_ implements PlugIn {//extends JFrame

	
	

	public static void main(String[] args) {

		//////////////
		// TEST FINDTHRESHFORNUMPTS
		//////////////
		//Get the info from the track
		String path = "C:\\Users\\Natalie\\Documents\\TestExProc\\Collision testing\\berlin@berlin__LIGHT_RANDOM_WALK_S1_112Hz_201402121807_MTP.jav";
		Experiment ex = Experiment.fromPath(path);
		Track tr = ex.getTrack(7);
		MaggotTrackPoint tp = (MaggotTrackPoint)tr.getPoint(0);
		ExtractionParameters ep = new ExtractionParameters();
		ep.excludeEdges = false;
		
		//Play the movie
		MaggotDisplayParameters mdp = new MaggotDisplayParameters();
		mdp.expandFac = 1;
		mdp.setAllFalse();
		tr.playMovie(mdp);
		
		//Show the image at various stages
		ImageProcessor im = tp.getRawIm();
		ImageStack is = new ImageStack(im.getWidth(), im.getHeight());
		is.addSlice(im);
		//Add a thresholded image to the stack
		ImageProcessor thIm = im.duplicate();
		thIm.threshold((int)ep.globalThreshValue);
		is.addSlice(thIm);
		
		//Find the threshold for 2 maggots and show that thresholded image
		int targetArea = (int)tr.getPoints().lastElement().area;
		
		//look at the "goodness values" in order to debug findThreshforNumPts
		/*
		StringBuilder sb = new StringBuilder();
		for(int i=(int)ep.globalThreshValue; i<=255; i++){
			ImageProcessor thIm3 = im.duplicate();
			thIm3.threshold(i);
			double goodness = CVUtils.findGoodness(new ImagePlus("im", thIm3), ep, 2, (int)ep.minArea, (int)ep.maxArea, targetArea);
			sb.append("Thresh "+i+": gv="+goodness+"\n");
		}
		new TextWindow("Goodness values", sb.toString(), 500, 500);
		*/
		
		//Final Version of findThreshforNumPts
		int thFor2Mags = CVUtils.findThreshforNumPts(new ImagePlus("im", im.duplicate()), ep, 2, (int)ep.minArea, (int)ep.maxArea, targetArea, tp.thresh, 255);
		
		ImageProcessor thIm2 = im.duplicate();
		thIm2.threshold(thFor2Mags);
		is.addSlice(thIm2);
		//Show the stack
		ImagePlus imp = new ImagePlus("Frame "+tp.frameNum+"; ("+tp.rect.x+","+tp.rect.y+") ; Thresholding; target area ="+targetArea+"; auto val ="+thFor2Mags, is);
		imp.show();
		
		//////////////
		// TEST SPLITPT2NPTS
		//////////////
		//Generate a pointExtractor to use in splitting points
		IJ.showStatus("Opening MMF...");	
		String dir = "C:\\Users\\Natalie\\Documents\\TestExProc\\Collision testing";
		String filename = "berlin@berlin__LIGHT_RANDOM_WALK_S1_112Hz_201402121807.mmf";
		IJ.run("Import MMF", "path=["+new File(dir, filename).getPath()+"]");
		ImageWindow mmfWin = WindowManager.getCurrentWindow();
		ImagePlus mmfStack = mmfWin.getImagePlus();
		Communicator c = new Communicator();
		c.setVerbosity(VerbLevel.verb_debug);
		PointExtractor pe = new PointExtractor(mmfStack.getStack(), c, ep);
		mmfWin.close();
		
		
		//Set the proper Analysis rect
		pe.setAnalysisRect(tp.rect);

		//Do extractPoints() machinery to generate splitPts
//		pe.loadFrame(tp.frameNum);//in pe.extractPoints
//		pe.rethresh(thFor2Mags);//in pe.extractPoints
//		c.message("Finding points in AR=("+pe.getAnalysisRect().x+","+pe.getAnalysisRect().y+") w="+pe.getAnalysisRect().width+", h="+pe.getAnalysisRect().height, VerbLevel.verb_debug);
//		ResultsTable pointTable = CVUtils.findPoints(pe.threshIm, pe.getAnalysisRect(), ep, false);//in pe.findpointsinim
//		pointTable.show("Points...");
//		Vector<TrackPoint> splitPts = pe.rt2TrackPoints(pointTable, pe.currentFrameNum, thFor2Mags);//in pe.findpointsinim

		//Extract points to generate splitPts 
//		pe.extractPoints(tp.frameNum, thFor2Mags);
//		Vector<TrackPoint> splitPts = pe.getPoints();
		
		//Final version of splitPts2NPts 
		Vector<TrackPoint> splitPts = MaggotTrackPoint.splitPt2NPts(tp, 2, targetArea, pe, ep);
		c.message("\n \n \nPoint Info:", VerbLevel.verb_debug);
		for (TrackPoint sp : splitPts){
			c.message(sp.getTPDescription(), VerbLevel.verb_debug);
		}
		
		if (splitPts!=null){
			
			
			if (splitPts.size()==2){
				for (TrackPoint p : splitPts){
					MaggotTrackPoint mp = (MaggotTrackPoint) p;
					new ImageWindow(new ImagePlus("", mp.getIm()));
					new ImageWindow(new ImagePlus("", mp.getMask()));
				}
				
				
				
				//////////////
				// TEST MATCHNPTS2NTRACKS
				//////////////
				
				Vector<Track> colTracks = new Vector<Track>();
				colTracks.add(ex.getTrack(4));
				colTracks.add(ex.getTrack(5));
				
				Vector<TrackMatch> matches = new Vector<TrackMatch>();
				//compare each
				double dist1 = colTracks.get(0).distFromEnd(splitPts.get(0))+colTracks.get(1).distFromEnd(splitPts.get(1));
				double dist2 = colTracks.get(0).distFromEnd(splitPts.get(1))+colTracks.get(1).distFromEnd(splitPts.get(0));
				if (dist1<dist2){
					matches.add(new TrackMatch(colTracks.get(0), splitPts.get(0), ep.maxMatchDist));
					matches.add(new TrackMatch(colTracks.get(1), splitPts.get(1), ep.maxMatchDist));
				} else{
					matches.add(new TrackMatch(colTracks.get(0), splitPts.get(1), ep.maxMatchDist));
					matches.add(new TrackMatch(colTracks.get(1), splitPts.get(0), ep.maxMatchDist));
				}
				
				
				c.message("\n \n \nMatch Making:", VerbLevel.verb_debug);
				matches.get(0).spillInfoToCommunicator(c);
				matches.get(1).spillInfoToCommunicator(c);
				
				
				
				
				//Final version of matchNPts2NTracks
//				Vector<TrackMatch> newMatches = TrackMatch.matchNPts2NTracks(splitPts, colTracks, ep.maxMatchDist);
				
				
			} else {
				ImageStack ptSt = new ImageStack(500, 500);
				for (TrackPoint p : splitPts){
					ImageProcessor pr = p.getIm();
//					if (pr.getHeight()<=500 && pr.getWidth()<=500){
						ByteProcessor bp = new ByteProcessor(500, 500);
						bp.copyBits(pr, 0, 0, Blitter.COPY);
						ptSt.addSlice(bp);
	//					stb.append(p.getTPDescription()+"\n");
//					}
				}
				new ImagePlus("SplitPts", ptSt).show();
//				new TextWindow(":\\", stb.toString(), 500, 500);
			}
			
			new TextWindow("Point Extraction, Matching", c.outString, 500, 500);
			
			
		} else {
			new TextWindow(":(", "No points", 500, 500);
		}
		
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
