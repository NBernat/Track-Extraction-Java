package TrackExtractionJava;

import ij.ImageJ;
import ij.ImagePlus;

import java.awt.BorderLayout;
import java.io.BufferedOutputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.util.Scanner;
import java.util.Vector;

import javax.swing.JFrame;




public class Test {//extends JFrame

	
	public int fld1;
	public String fld2;
	
		

	public static void main(String[] args) {
		
		fitExperimentNewScheme();
		
		/*
		testBadTrackFinder();
		*/
		
		//testFitterPauseDisplay();
		/*
		*/
		
		/*
		testSubsetInchInwards();
		*/
		
		/*
		fitExperimentNewScheme();
		*/
		
		/*
		testNewBBFScheme();
		*/
		
		/*
		testFreezeDiverged();
		*/
		
		/*
		testBBFsubset();
		*/
			
		/*
		testNewBBFArchitecture();
		*/
		
		/*
		generateEnergyProfiles();
		*/
		
		/*
		runDefaultFitting();
		*/
		
		/*
		testNumPtsOnDiverged();
		*/
		
		/*
		analyzeTrackBreakdown();
		*/

		/*
		testTrackBreakdown();
		*/
		
		/*
		testDefaultFitterParams();
		*/
		
		
		/*
		testAreaSplitter();
		*/
		
		/*
		testFittingParams();
		*/
		
		/*
		testEndClippingForDivergance();
		*/
		
		
		/*
		testDMSinProcessing();
		*/
		
		
		/*
		testDistanceMapSplitter();
		*/
		
		/*
		testImDerivs();
		*/
		
		/*
		testParamFromDisk();
		*/
		
		/*
		testDiagIm();
		*/
		
		/*
		testParamToDisk();
		*/
		
		/*
		testEx_Proc();
		*/
		
		
		/*
		testExParamaPanel();
		*/
		
		
		/*
		testProcParamPanel();
		*/
		
		
		/*
		testExractionWindow();
		*/
		
		
		/*
		testCSVwriterFrame();
		*/
		
		
		/*
		testCSVPanel();
		*/
		
		/*
		testToCSV();
		*/
		
		/*
		String fname = "C:\\Users\\Natalie\\Documents\\TestJavaMat\\extracted\\SA\\CantonS@CantonS\\C_Bl_2uW#N_Re_B0to255s3_120Hz_800uW\\CantonS@CantonS_C_Bl_2uW#N_Re_B0to255s3_120Hz_800uW_201507201614.jav";
		Experiment ex = Experiment.fromPath(fname);
		System.out.println("Tracks: "+ex.getNumTracks());
		Track t = Experiment.getTrack(12, fname);
		*/
		
		
		/*
		Experiment_Processor ep = new Experiment_Processor();
		ep.main(args);
		*/
		
		/*
		String path = "C:\\Users\\Natalie\\Documents\\1cm_checkerboard.png";
		
		if (args!=null && args.length>=1){
			path = args[0];
		}
		
		
		File f = new File(path);
		
		System.out.println(path);
		System.out.println(f.getParent());
		*/

		/*
		String path = "C:\\Users\\Natalie\\Documents\\TestJavaMat\\data\\phototaxis\\berlin@berlin\\LIGHT_RANDOM_WALK_S1_112Hz\\201402121840\\berlin@berlin_LIGHT_RANDOM_WALK_S1_112Hz_201402121840.mmf";
		String dstDir = "C:\\Users\\Natalie\\Documents\\";
		Path p = Paths.get(path.replace(".mmf", ".mdat"));
		String savepath = new File(dstDir, p.getFileName().toString()).getAbsolutePath();
		System.out.println(savepath);
		*/
		
		
		/*
		String inDir = "C:\\Users\\Natalie\\Documents\\TestJavaMat\\data\\201406122014";
		
		System.out.println(inDir);
		System.out.println(ProcessingParameters.getOutFromInDir(inDir));
		*/
		
		
		/*
		ImagePlus imp = new ImagePlus("C:\\Users\\Natalie\\Documents\\TestExProc\\unmaskTest.tif");
		imp.show();
		*/
		
		/*
		testGapDilation();
		*/
		
	}
	
	public static void testBadTrackFinder(){
		ImageJ ij = new ImageJ();
		
		String outputDir = "E:\\testing\\Java Backbone Fitting\\test badness fixer\\";
		String inputFileName = outputDir+"4 After Param adjustment\\Berlin@Berlin_2NDs_B_Square_SW_96-160_201411201541.jav";//"E:\\data\\phototaxis\\berlin@berlin\\2NDs_B_Square_SW_96-160\\201411201541\\Berlin@Berlin_2NDs_B_Square_SW_96-160_201411201541.mmf";
		
		Experiment ex = new Experiment(inputFileName);
		Experiment badEx = ex.flagBadTracks();
		
		if (badEx.getNumTracks()>0) {
			badEx.showEx();
		} else {
			System.out.println("No bad tracks found in experiment");
		}
		
		ij.quit();
	}
	
	public static void testFitterPauseDisplay(){
		ImageJ ij = new ImageJ();
		
		String outputDir = "E:\\testing\\Java Backbone Fitting\\test badness fixer\\";
		String inputFileName = outputDir+"0 Before any fixing\\Berlin@Berlin_2NDs_B_Square_SW_96-160_201411201541.prejav";//"E:\\data\\phototaxis\\berlin@berlin\\2NDs_B_Square_SW_96-160\\201411201541\\Berlin@Berlin_2NDs_B_Square_SW_96-160_201411201541.mmf";
		
		
		int trackID = 18;
		Experiment ex = new Experiment(inputFileName);
		Track t = ex.getTrack(trackID);
		
		t.showFitting();
		BackboneFitter bbf = new BackboneFitter(t);
//		bbf.doPause = true;
////		bbf.userIn = new Scanner(System.in);
//		bbf.userOut = System.out;
//		
//		bbf.fitTrackNewScheme();
		
		
//		if (bbf.getTrack()!=null){
//			Vector<Track> newTracks = new Vector<Track>();
//			newTracks.add(bbf.getTrack());
//			Experiment newExperiment = new Experiment(ex, newTracks);
//			newExperiment.showEx();
//		}
		
		ij.quit();
	}
	
	public static void testSubsetInchInwards(){
		ImageJ ij = new ImageJ();
		
		String outputDir = "E:\\testing\\Java Backbone Fitting\\test bbf subset\\newEnergySavingWFrozenDiverged\\";
		String inputFileName = outputDir+"Berlin@Berlin_2NDs_B_Square_SW_96-160_201411201541.prejav";
		int trackID = 8;
		Experiment ex = new Experiment(inputFileName);
		Track t = ex.getTrack(trackID);
		BackboneFitter bbf = new BackboneFitter(t);
		
		bbf.fitTrackNewScheme();
//		bbf.resetForNextExectution();
//		bbf.patchGap_InchInwards(new Gap(1730, 1800), 1);
//		FittingParameters spp = FittingParameters.getSinglePassParams();
//		spp.leaveFrozenBackbonesAlone = true;//This tells the plg not to re-initialize the frozen bb's
//		spp.freezeDiverged = true;
//		spp.leaveBackbonesInPlace = true;
//		bbf.resetParams(spp);
//		bbf.fitTrack();
		
		
		if (bbf.getTrack()!=null){
			Vector<Track> newTracks = new Vector<Track>();
			newTracks.add(bbf.getTrack());
			Experiment newExperiment = new Experiment(ex, newTracks);
			newExperiment.showEx();
		}
		
		
		ij.quit();
	}
	
	public static void fitExperimentNewScheme(){
		
		ImageJ ij = new ImageJ();
		
		String outputDir = "E:\\test\\trackTest\\output\\";
		String inputFileNamePreJav = "E:\\test\\trackTest\\output\\Or42a(3)@Chrimson(3)_R_WN_S3_112Hz_B_5P_2ohm_atR+_201407211915.prejav";
		String inputFileName = "E:\\test\\trackTest\\201407211915\\Or42a(3)@Chrimson(3)_R_WN_S3_112Hz_B_5P_2ohm_atR+_201407211915.mmf";
		
		String args[] = new String[2];
		args[0] = inputFileName;
		args[1] = "E:\\testing\\Java Backbone Fitting\\test communicator output\\";// outputDir+"7 Coord to contour\\";
		
		Experiment_Processor ep = new Experiment_Processor();
		ep.runningFromMain = true;
		ExtractionParameters exP = new ExtractionParameters();
		exP.subset = true;
		exP.startFrame = 2000;
		exP.endFrame = 4000;

		ProcessingParameters pp = new ProcessingParameters();
		pp.doFitting = true;
		pp.fitType=1;
		
		FittingParameters fp = new FittingParameters();
		fp.storeEnergies = false;
		
		ep.extrParams = exP;
		ep.prParams = pp;
		ep.fitParams = fp;
		
		ep.run(args);
		
		ij.quit();
	}
	
	public static void testNewBBFScheme(){
		
		ImageJ ij = new ImageJ();
		
		String outputDir = "E:\\testing\\Java Backbone Fitting\\test bbf subset\\";
		String inputFileName = outputDir+"Berlin@Berlin_2NDs_B_Square_SW_96-160_201411201541.prejav";
		int trackID = 4;
		Experiment ex = new Experiment(inputFileName);
		Track t = ex.getTrack(trackID);
		BackboneFitter bbf = new BackboneFitter(t);
		
		bbf.fitTrackNewScheme();
//		bbf.patchTrackSubset(new Gap(1730, 1800), 32*5);
		
		if (bbf.getTrack()!=null){
			Vector<Track> newTracks = new Vector<Track>();
			newTracks.add(t);
			newTracks.add(bbf.getTrack());
			Experiment newExperiment = new Experiment(ex, newTracks);
			newExperiment.showEx();
		}
		
//		bbf.resetForNextExectution();
//		bbf.setFrozen(0, bbf.workingTrack.points.size(), true);
//		bbf.params.leaveFrozenBackbonesAlone = true;
//		bbf.runSingleIteration();
		double[] totalE = MathUtils.castFloatArray2Double(bbf.energyProfiles.lastElement().getLastEnergies());
		double mean = MathUtils.mean(totalE);
		double stdDev = MathUtils.stdDev(totalE, mean);
		MathUtils.plotDataMeanStdDev(totalE,mean,stdDev, "Total Energy vs frame", "total e", bbf.workingTrack.points.firstElement().frameNum);
		
		ij.quit();
	}
	
	
	public static void testBBFsubset(){
		
		ImageJ ij = new ImageJ();
		
		String outputDir = "E:\\testing\\Java Backbone Fitting\\test bbf subset\\";
		String inputFileName = outputDir+"Berlin@Berlin_2NDs_B_Square_SW_96-160_201411201541.prejav";
			
		int trackInd = 46;
		Vector<Track> newTracks = new Vector<Track>();
		
		Experiment ex = new Experiment(inputFileName);
		
		Track t = ex.getTrackFromInd(trackInd);
		MaggotDisplayParameters mdp = new MaggotDisplayParameters();
		mdp.ht = true;
		mdp.mid = true;
		t.playMovie(mdp);
		
		FittingParameters fp = new FittingParameters();
		fp.storeEnergies = true;
		BackboneFitter bbf = new BackboneFitter(t,fp);
		bbf.runSingleIteration();
		
		double[] dists = t.getHTdists();
		dists[501]=0;
		MathUtils.plotDataMeanStdDev(dists, "HT Dist (Before Fitting)", "dstSqr");
//		MathUtils.plotDataMeanStdDev(bbf.energyProfiles.lastElement().energies.firstElement(), "Total Backbone Energy (Before Fitting)", "total E");
//		MathUtils.plotDataMeanStdDev(bbf.energyProfiles.get(1).energies.firstElement(), "Backbone Length Energy (Before Fitting)", "length E");
//		MathUtils.plotDataMeanStdDev(bbf.energyProfiles.get(2).energies.firstElement(), "Backbone Smooth Energy (Before Fitting)", "smooth E");
		
		
		
		
		bbf.fitTrack();
		if (bbf.getTrack()!=null) newTracks.add(bbf.getTrack());
		
		
		
		/*
		int startInd = 1200;
		int endInd = 1900;
		BackboneFitter bbf2 = new BackboneFitter(ex.getTrackFromInd(trackInd));
		bbf2.fitTrackSubset_IgnoreSurrounding_old(startInd, endInd);
		if (bbf2.getTrack()!=null) newTracks.add(bbf2.getTrack());
		

		BackboneFitter bbf3 = new BackboneFitter(ex.getTrackFromInd(trackInd));
		bbf3.fitTrackSubset_IncludeSurrounding_old(startInd, endInd);
		bbf3.resetFitter();
		bbf3.fitTrackSubset_IncludeSurrounding_old(900, 1400); 
		if (bbf3.getTrack()!=null) newTracks.add(bbf3.getTrack());
		*/
		
		Vector<Gap> subsets1 = new Vector<Gap>();
		subsets1.add(new Gap(200,900));
		subsets1.add(new Gap(1200,1900));
		
		Vector<Gap> subsets2 = new Vector<Gap>();
		subsets2.add(new Gap(901,1199));
		
		FittingParameters spp = FittingParameters.getSinglePassParams();
		
		BackboneFitter bbf4 = new BackboneFitter(ex.getTrackFromInd(trackInd));
		bbf4.resetParams(spp);
		bbf4.fitSubsets(subsets1, false);
//		bbf4.resetFitter();
		
//		if (bbf4.getTrack()!=null) newTracks.add(bbf4.getTrack());
//		Experiment newExperiment = new Experiment(ex, newTracks);
//		newExperiment.showEx();

		
		spp.leaveFrozenBackbonesAlone = true;
		bbf4.resetParams(spp);
		bbf4.fitSubsets(subsets2,false);
		
//		Float[] e1 = bbf4.energyProfiles.lastElement().energies.get(bbf4.numIters.firstElement()-1);
//		Float[] e2 = bbf4.energyProfiles.lastElement().energies.get(bbf4.numIters.firstElement()+bbf4.numIters.lastElement()-1);
		
		
		bbf4.patchTrackSubset(new Gap(1200, 1500), 32*5);
//		bbf4.patchTrackSubset(new Gap(900, 1200), 32*5);
		
//		if (bbf4.getTrack()!=null) newTracks.add(bbf4.getTrack());
		
//		BackboneFitter bbf5 = new BackboneFitter(ex.getTrackFromInd(trackInd));
//		spp.leaveFrozenBackbonesAlone = false;
//		bbf5.resetParams(spp);
//		bbf5.fitSubsets(subsets2, true);
//		if (bbf5.getTrack()!=null) newTracks.add(bbf5.getTrack());
		
		if (bbf4.getTrack()!=null) newTracks.add(bbf4.getTrack());
		Experiment newExperiment = new Experiment(ex, newTracks);
		newExperiment.showEx();
		
		
		
		
		
		ij.quit();
	}
	
	
	
	public static void testFreezeDiverged(){
		
		/*
		ImageJ IJ = new ImageJ();
		String baseDir = "E:\\testing\\Java Backbone Fitting\\testSinglePass_freezeDiverged\\";//testClusterEdits\\voronoi\\subset_runonshortenedexp\\";
		
		String[] args = new String[2];
		args[0] = "E:\\testing\\Java Backbone Fitting\\test contour fix\\full\\Berlin@Berlin_2NDs_B_Square_SW_96-160_201411201541.prejav";		
		args[1] = baseDir;
		
		
		FittingParameters fP = FittingParameters.getSinglePassParams();
		fP.storeEnergies = true;
		fP.freezeDiverged = true;
		fP.leaveFrozenBackbonesAlone = true;
		
		ProcessingParameters prP = new ProcessingParameters();
		prP.diagnosticIm = false;
		
		Experiment_Processor ep = new Experiment_Processor();
		ep.runningFromMain = true;
		ep.prParams = prP;
		ep.fitParams = fP;
		
		ep.run(args);
		
		
		IJ.quit();
		 */
		
		
		
		ImageJ ij = new ImageJ();
		
		String outputDir = "E:\\testing\\Java Backbone Fitting\\test interpolation fix\\";
		String inputFileName = outputDir+"Berlin@Berlin_2NDs_B_Square_SW_96-160_201411201541.prejav";
			
		FittingParameters spp = FittingParameters.getSinglePassParams();
		spp.freezeDiverged = true;
		spp.leaveFrozenBackbonesAlone = true;
		

		Experiment ex = new Experiment(inputFileName);
		Vector<Track> newTracks = new Vector<Track>();
//		int trackInd = 47;
		for (int trackInd=67; trackInd<75; trackInd++){
			
			BackboneFitter bbf = new BackboneFitter(ex.getTrackFromInd(trackInd), spp);
			bbf.fitTrack();
			if (bbf.getTrack()!=null) newTracks.add(bbf.getTrack());
		}
		Experiment newExperiment = new Experiment(ex, newTracks);
		newExperiment.showEx();
		
		
		
		ij.quit();
		
	}
	
	public static void testNewBBFArchitecture(){
		
		String outputDir = "E:\\testing\\Java Backbone Fitting\\test orientation fix\\";
		String inputFileName = outputDir+"Berlin@Berlin_2NDs_B_Square_SW_96-160_201411201541.prejav";
			
		Experiment ex = new Experiment(inputFileName);
		
		
		BackboneFitter bbf = new BackboneFitter(ex.getTrackFromInd(50));
		bbf.fitTrack();
		
		BackboneFitter bbfOld = new BackboneFitter();
		bbfOld.fitTrack(ex.getTrackFromInd(50));
		
		System.out.println("Done");
	}
	
	
	public static void generateEnergyProfiles(){
		
		ImageJ IJ = new ImageJ();
		String baseDir = "E:\\testing\\Java Backbone Fitting\\testClusterEdits\\voronoi\\subset_runonshortenedexp\\";
		
		String[] args = new String[2];
		args[0] = baseDir+"divergedTrackExp.prejav";		
		args[1] = baseDir+"divergedOutput\\";
		
		
		FittingParameters fP = new FittingParameters();
		fP.clusterMethod=0;
		fP.storeEnergies = true;
		
		ProcessingParameters prP = new ProcessingParameters();
		prP.diagnosticIm = false;
		
		Experiment_Processor ep = new Experiment_Processor();
		ep.runningFromMain = true;
		ep.prParams = prP;
		ep.fitParams = fP;
		
		ep.run(args);
		
		
		IJ.quit();
	}
	
	public static void testNumPtsOnDiverged(){
		String srcName = "E:\\testing\\Java Backbone Fitting\\Fitting Params\\fullExptWithAreaSplit_0.7-1.4_otherPtSplit\\divergedTrackExp.prejav";
		String dstBaseDir = "E:\\testing\\Java Backbone Fitting\\Targeted PointNum Variation\\";
		
		String[] args = new String[2];
		args[0] = srcName;
		
		FittingParameters fitPar = new FittingParameters();
		
		
		args[1] = dstBaseDir+"5pts\\";
		fitPar = new FittingParameters();
		fitPar.numBBPts = 5;
		float[] timeLengthWeight_m5 = {3.0f, .1f, .1f};
		float[] timeSmoothWeight_m5 = {1.0f, 0.1f, 0.1f};
		fitPar.timeLengthWeight = timeLengthWeight_m5;
		fitPar.timeSmoothWeight = timeSmoothWeight_m5;
		float[] imageWeights_m5 = {1,1,1, 1,1};
		float[] spineLengthWeights_m5 = {0,1,1, 1,1};
		float[] spineSmoothWeights_m5 = {.6f,1,1, 1,1};
		float[][] timeLengthWeights_m5 = { {1,1,1, 1,1},
										{1,1,1, 1,1},
										{1,1,1, 1,1} };
		float[][] timeSmoothWeights_m5 = { {1,1,1, 1,1},
										{1,1,1, 1,1},
										{1,1,1, 1,1} };
		fitPar.imageWeights = imageWeights_m5;
		fitPar.spineLengthWeights = spineLengthWeights_m5;
		fitPar.spineSmoothWeights = spineSmoothWeights_m5;
		fitPar.timeLengthWeights = timeLengthWeights_m5;
		fitPar.timeSmoothWeights = timeSmoothWeights_m5;
		
		Experiment_Processor ep = new Experiment_Processor();
		ep.runningFromMain = true;
		ep.fitParams = fitPar;
		ep.run(args);
		
	}
	
	public static void analyzeTrackBreakdown(){
		
		String fname = "E:\\testing\\Java Backbone Fitting\\Track Breakdown\\tracks\\SystemDOTout.txt";
//		String outputFname = "E:\\testing\\Java Backbone Fitting\\Track Breakdown\\analysis.txt";
		
		Vector<Integer> numFit = new Vector<Integer>();
		Vector<Integer> numTotal = new Vector<Integer>();
		
		Scanner s = null;
		
		try {
			
//			System.setOut(new PrintStream(new BufferedOutputStream(new FileOutputStream(outputFname))));
			
			s = new Scanner(new File(fname));
			
			String signal = "...Done fitting: ";
			//Iterate over each line
			while (s.hasNextLine()){
				//When the line contains a result.... 
				String line = s.nextLine();
				if (line.startsWith(signal)){
					//...parse the info and store it
					
					//collect the string containing the fraction of tracks, e.g. "4/5"
					String info = line.substring(signal.length(), line.indexOf(" ", signal.length()));
					
					
					//break that into two nums and store
					String[] numStrs = info.split("/");
					numFit.add(Integer.valueOf(numStrs[0]));
					numTotal.add(Integer.valueOf(numStrs[1]));
					

					System.out.println(info+"\t"+"("+(100*Integer.valueOf(numStrs[0]))/Integer.valueOf(numStrs[1])+")");
					
				}
			}
			
			
			
			
			
			
			
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		
		if (s!=null) s.close();
	}
	
	public static void testTrackBreakdown(){
		ImageJ imj = new ImageJ(ImageJ.NO_SHOW);
		
//		Experiment_Processor ep;
		
//		ProcessingParameters prParam = new ProcessingParameters();
//		prParam.diagnosticIm = false;
		
		//Set src and dest
		String srcName = "E:\\testing\\Java Backbone Fitting\\Fitting Params\\fullExptWithAreaSplit_0.7-1.4_otherPtSplit\\divergedTrackExp.prejav";
		String dstBaseDir = "E:\\testing\\Java Backbone Fitting\\Track Breakdown\\";
		
		Experiment ex = new Experiment(srcName);
		//Find a long track
		
//		int len = 16726;
		//Find a track that's the length of the experiment
//		System.out.println("Finding full-experiment track in "+ex.tracks.size()+" tracks...");
		Track longTrack = null;
//		int i;
//		for (i=0; (i<ex.tracks.size() && longTrack==null); i++){
//			if (ex.getTrackFromInd(i).points.size()==len) longTrack=ex.getTrackFromInd(i);
//		}
//		System.out.println("Found track (ind="+i+")");
		
		
		Vector<Track> fits = new Vector<Track>();
		Vector<Track> divs = new Vector<Track>();
		BackboneFitter bbf;
		for (int j=0; j<ex.tracks.size(); j++){
			
			longTrack = ex.tracks.get(j);
			int len = longTrack.points.size();
			int clipLen = 500;
			if (len>(clipLen*3)){
				Vector<Track> fitTracks = new Vector<Track>();
				Vector<Track> divTracks = new Vector<Track>();
				
				System.out.println("Clipping and Fitting track...");
				for (int i=0; i<=len/clipLen; i++){
				
					bbf = new BackboneFitter();
		//			bbf.clipEnds = true;
					int sf = 1+i*clipLen;
					int ef = (len<((i+1)*clipLen))? len-1: (i+1)*clipLen;
					
					Track clipTrack = new Track(longTrack.getPoints().subList(sf, ef), i);
							
					bbf.fitTrack(clipTrack);
					
					if (bbf.getTrack()!=null){
						fitTracks.add(bbf.getTrack());				
					} else {
						divTracks.add(clipTrack);
					}
					
				}
				fits.addAll(fitTracks);
				divs.addAll(divTracks);
				
				System.out.println("...Done fitting: "+fitTracks.size()+"/"+(fitTracks.size()+divTracks.size()+" were fit properly"));
				
				Experiment fitEx = new Experiment();
				fitEx.tracks = fitTracks;
				Experiment divEx = new Experiment();
				divEx.tracks = divTracks;
				
				try {
					File f = new File(dstBaseDir+"tracks\\track"+j+"\\");
					if (!f.exists()) f.mkdirs();
					
					
					f = new File(dstBaseDir+"tracks\\track"+j+"\\"+"fitTrackExp.jav");
					System.out.println("Saving fit track experiment to "+f.getPath());
					try{
						DataOutputStream dos = new DataOutputStream(new BufferedOutputStream(new FileOutputStream(f))); 
								
						fitEx.toDisk(dos, null);
						dos.close();
						System.out.println("Done saving fit tracks");
					} catch(Exception e){
						System.out.println("Save error");
					}
					
					f = new File(dstBaseDir+"tracks\\track"+j+"\\"+"divergedTrackExp.prejav");
					System.out.println("Saving error track experiment to "+f.getPath());
					try{
						DataOutputStream dos = new DataOutputStream(new BufferedOutputStream(new FileOutputStream(f))); 
								
						divEx.toDisk(dos, null);
						dos.close();
						System.out.println("Done saving diverged tracks");
					} catch(Exception e){
						System.out.println("Save error");
					}
		
				} catch (Exception e){
					
				}
			} else {
			}
		}
		
		
		Experiment fitEx = new Experiment();
		fitEx.tracks = fits;
		Experiment divEx = new Experiment();
		divEx.tracks = divs;
		
		try {
			File f = new File(dstBaseDir+"allFitTracks.jav");
			System.out.println("Saving fit track experiment to "+f.getPath());
			try{
				DataOutputStream dos = new DataOutputStream(new BufferedOutputStream(new FileOutputStream(f))); 
						
				fitEx.toDisk(dos, null);
				dos.close();
				System.out.println("Done saving fit tracks");
			} catch(Exception e){
				System.out.println("Save error");
			}
			
			f = new File(dstBaseDir+"allDivTracks.prejav");
			System.out.println("Saving error track experiment to "+f.getPath());
			try{
				DataOutputStream dos = new DataOutputStream(new BufferedOutputStream(new FileOutputStream(f))); 
						
				divEx.toDisk(dos, null);
				dos.close();
				System.out.println("Done saving diverged tracks");
			} catch(Exception e){
				System.out.println("Save error");
			}

		} catch (Exception e){
			
		}
		
		
		imj.quit();
	}
	
	public static void runDefaultFitting(){
		
		ImageJ IJ = new ImageJ();
//		String srcName = "E:\\testing\\Java Backbone Fitting\\Fitting Params\\fullExptWithAreaSplit_0.7-1.4_otherPtSplit\\Berlin@Berlin_2NDs_B_Square_SW_96-160_201411201541.prejav";
//		String dstBaseDir = "E:\\testing\\Java Backbone Fitting\\";
		
		String[] args = new String[2];
//		args[0] = srcName;
//		args[1] = dstBaseDir+"testClusterEdits\\voronoi\\subset\\";//gaussianMixture
//		
//		FittingParameters fP = new FittingParameters();
//		fP.clusterMethod=0;
//		fP.subset = true;
//		fP.startInd=1;
//		fP.endInd=2000;
//		ExtractionParameters exP = new ExtractionParameters();
//		
//		ProcessingParameters prP = new ProcessingParameters();
//		prP.diagnosticIm = false;
//		
//		Experiment_Processor ep = new Experiment_Processor();
//		ep.runningFromMain = true;
//		ep.prParams = prP;
//		ep.extrParams = exP;
//		ep.fitParams = fP;
//		
//		ep.run(args);
//		
//		args[1] = dstBaseDir+"testClusterEdits\\gaussianMixture\\subset\\";//
//		
//		fP = new FittingParameters();
//		fP.clusterMethod=1;
//		fP.subset = true;
//		fP.startInd=1;
//		fP.endInd=2000;
//		exP = new ExtractionParameters();
//		
//		prP = new ProcessingParameters();
//		prP.diagnosticIm = false;
//		
//		ep = new Experiment_Processor();
//		ep.runningFromMain = true;
//		ep.prParams = prP;
//		ep.extrParams = exP;
//		ep.fitParams = fP;
//		
//		ep.run(args);
		
//		args[0] =  "E:\\data\\phototaxis2\\berlin@berlin\\2NDs_B_Square_SW_96-160\\201411201541\\Berlin@Berlin_2NDs_B_Square_SW_96-160_201411201541.mmf";
//		args[1] = dstBaseDir+"testClusterEdits\\voronoi\\subset_runonshortenedexp\\";//gaussianMixture
//		
//		FittingParameters fP = new FittingParameters();
//		fP.clusterMethod=0;
//		ExtractionParameters exP = new ExtractionParameters();
//		exP.subset = true;
//		exP.startFrame = 1;
//		exP.endFrame = 2000;
//		
//		ProcessingParameters prP = new ProcessingParameters();
//		prP.diagnosticIm = false;
//		
//		Experiment_Processor ep = new Experiment_Processor();
//		ep.runningFromMain = true;
//		ep.prParams = prP;
//		ep.extrParams = exP;
//		ep.fitParams = fP;
//		
//		ep.run(args);
		
		
		
//		args[0] = dstBaseDir+"testClusterEdits\\voronoi\\subset_runonshortenedexp\\Berlin@Berlin_2NDs_B_Square_SW_96-160_201411201541.prejav";
//		args[1] = dstBaseDir+"testClusterEdits\\gaussianMixture\\subset_runonshortenedexp\\";//gaussianMixture
//		
//		fP = new FittingParameters();
//		fP.clusterMethod=1;
//		exP = new ExtractionParameters();
//		
//		prP = new ProcessingParameters();
//		prP.diagnosticIm = false;
//		
//		ep = new Experiment_Processor();
//		ep.runningFromMain = true;
//		ep.prParams = prP;
//		ep.extrParams = exP;
//		ep.fitParams = fP;
//		
//		ep.run(args);
		
//		args[0] = "E:\\data\\phototaxis2\\berlin@berlin\\2NDs_B_Square_SW_96-160\\201411201541\\Berlin@Berlin_2NDs_B_Square_SW_96-160_201411201541.mmf";
//		args[1] = "E:\\testing\\Java Backbone Fitting\\test contour fix\\full\\";
//		
//		FittingParameters fP = new FittingParameters();
//		fP.clusterMethod=0;
//		fP.storeEnergies = true;
//		ExtractionParameters exP = new ExtractionParameters();
//		exP.startFrame = 1;
//		exP.endFrame = 2000;
//		
//		ProcessingParameters prP = new ProcessingParameters();
//		prP.diagnosticIm = false;
//		
//		Experiment_Processor ep = new Experiment_Processor();
//		ep.runningFromMain = true;
//		ep.prParams = prP;
//		ep.extrParams = exP;
//		ep.fitParams = fP;
//		
//		ep.run(args);
		
		
//		args[0] = "E:\\data\\phototaxis2\\berlin@berlin\\2NDs_B_Square_SW_96-160\\201411201541\\Berlin@Berlin_2NDs_B_Square_SW_96-160_201411201541.mmf";
		args[1] = "E:\\testing\\Java Backbone Fitting\\test divergence fix\\";
		args[0] = args[1]+"divergedTrackExp.jav";//+"Berlin@Berlin_2NDs_B_Square_SW_96-160_201411201541.prejav";
		args[1] += "divergedTracks\\";//"testparams\\t_p3p3p1_sl_p7_ss_p7";
		
		FittingParameters fP = new FittingParameters();
		float[] timeLengthWeight = {.3f, 0.3f, 0.1f};
		float[] timeSmoothWeight = {.3f, 0.3f, 0.1f}; 
		fP.timeLengthWeight = timeLengthWeight;
		fP.timeSmoothWeight = timeSmoothWeight;
//		fP.spineLengthWeight = .7f;
//		fP.spineSmoothWeight = 0.9f;
//		fP.imageWeight = 0.9f;
		fP.clusterMethod=0;
		fP.storeEnergies = true;
		fP.refitDiverged = true;
		ExtractionParameters exP = new ExtractionParameters();
		exP.subset = true;
		exP.startFrame = 1;
		exP.endFrame = 2000;
		
		ProcessingParameters prP = new ProcessingParameters();
		prP.diagnosticIm = false;
//		prP.showFitEx = true;
		
		Experiment_Processor ep = new Experiment_Processor();
		ep.runningFromMain = true;
		ep.prParams = prP;
		ep.extrParams = exP;
		ep.fitParams = fP;
		
		ep.run(args);
		
		
		
		IJ.quit();
	}
	
	public static void testDefaultFitterParams(){
		ImageJ imj = new ImageJ(ImageJ.NO_SHOW);
		
		Experiment_Processor ep;
		
		ProcessingParameters prParam = new ProcessingParameters();
		prParam.diagnosticIm = false;
		ExtractionParameters exParam = new ExtractionParameters();
		
		//Set src and dest
		String[] args = new String[2];//[src path, dst dir]
		String fName = "E:\\testing\\Java Backbone Fitting\\Fitting Params\\fullExptWithAreaSplit_0.7-1.4_otherPtSplit\\Berlin@Berlin_2NDs_B_Square_SW_96-160_201411201541.prejav";
		String baseDir = "E:\\testing\\Java Backbone Fitting\\Fitting Params\\fullExptWithAreaSplit_0.7-1.4_otherPtSplit\\";
		args[0] = fName;
		
		
		
		ep = new Experiment_Processor();
		ep.runningFromMain = true;
		exParam.subset = false;
		FittingParameters fp = new FittingParameters();
		fp.spineLengthWeight = 0.5f;
		ep.extrParams = exParam;
		ep.prParams = prParam;
		ep.fitParams = fp;
		args[1] = baseDir+"uppedSpineLength\\";
		System.out.println("Fitting tracks");
		ep.run(args);
		System.out.println("DONE fitting tracks");
		
		ep = new Experiment_Processor();
		ep.runningFromMain = true;
		exParam.subset = false;
		fp = new FittingParameters();
		int[] gr = {8,4,1};
		fp.grains = gr;
		ep.extrParams = exParam;
		ep.prParams = prParam;
		ep.fitParams = fp;
		args[1] = baseDir+"grain8-4-1\\";
		System.out.println("Fitting tracks");
		ep.run(args);
		System.out.println("DONE fitting tracks");
		
		imj.quit();
		
		
		
		
	}
	
	public static void testAreaSplitter(){
		
		ImageJ imj = new ImageJ(ImageJ.NO_SHOW);
		
		Experiment_Processor ep;
		
		ProcessingParameters prParam = new ProcessingParameters();
		prParam.diagnosticIm = false;
		ExtractionParameters exParam = new ExtractionParameters();
		exParam.subset = true;
		exParam.startFrame = 1;
		exParam.endFrame = 1000;
		
		//Set src and dest
		String[] args = new String[2];//[src path, dst dir]
		String mmfName = "E:\\data\\phototaxis2\\berlin@berlin\\2NDs_B_Square_SW_96-160\\201411201541\\Berlin@Berlin_2NDs_B_Square_SW_96-160_201411201541.mmf";
//		String dstBaseDir = "E:\\testing\\Java Extraction\\Area Splitting\\";
		String fullExptBaseDir = "E:\\testing\\Java Backbone Fitting\\Fitting Params\\";
		args[0] = mmfName;
		/*
		ep = new Experiment_Processor();
		ep.runningFromMain = true;
		exParam.splitMatchesByAreaFrac = false;
		ep.extrParams = exParam;
		ep.prParams = prParam;
		args[1] = dstBaseDir+"withoutAreaSplit\\";
		System.out.println("Extracting tracks without area split");
		ep.run(args);
		System.out.println("Without area split: DONE");
		
		ep = new Experiment_Processor();
		ep.runningFromMain = true;
		exParam.splitMatchesByAreaFrac = true;
		exParam.lowerAreaFrac = 0.5;
		exParam.upperAreaFrac = 1.5;
		ep.extrParams = exParam;
		ep.prParams = prParam;
		args[1] = dstBaseDir+"withAreaSplit_0.5-1.5\\";
		System.out.println("Extracting tracks with area split 0.5-1.5");
		ep.run(args);
		System.out.println("With area split 0.5-1.5: DONE");
		
		ep = new Experiment_Processor();
		ep.runningFromMain = true;
		exParam.splitMatchesByAreaFrac = true;
		exParam.lowerAreaFrac = 0.7;
		exParam.upperAreaFrac = 1.4;
		ep.extrParams = exParam;
		ep.prParams = prParam;
		args[1] = dstBaseDir+"withAreaSplit_0.7-1.4_otherPtSplit\\";
		System.out.println("Extracting tracks with area split 0.7-1.4");
		ep.run(args);
		System.out.println("With area split 0.7-1.4: DONE");
		

		
		ep = new Experiment_Processor();
		ep.runningFromMain = true;
		exParam.subset = false;
		exParam.splitMatchesByAreaFrac = true;
		exParam.lowerAreaFrac = 0.7;
		exParam.upperAreaFrac = 1.4;
		ep.extrParams = exParam;
		ep.prParams = prParam;
		args[1] = fullExptBaseDir+"fullExptWithAreaSplit_0.7-1.4_otherPtSplit\\";
		System.out.println("Extracting tracks");
		ep.run(args);
		System.out.println("DONE extracting tracks");
		
		ep = new Experiment_Processor();
		ep.runningFromMain = true;
		exParam.subset = false;
		exParam.splitMatchesByAreaFrac = true;
		exParam.lowerAreaFrac = 0.7;
		exParam.upperAreaFrac = 1.4;
		FittingParameters fp = new FittingParameters();
		int[] gr = {8,4,1};
		fp.grains = gr;
		fp.imageWeight = 0.6f;
		fp.spineLengthWeight = 0.4f;
		ep.extrParams = exParam;
		ep.prParams = prParam;
		ep.fitParams = fp;
		args[1] = fullExptBaseDir+"fullExptWithAreaSplit_0.7-1.4_otherPtSplit_grain8-4-1_loweredImageTerm\\";
		System.out.println("Extracting tracks");
		ep.run(args);
		System.out.println("DONE extracting tracks");
		
		ep = new Experiment_Processor();
		ep.runningFromMain = true;
		exParam.subset = false;
		exParam.splitMatchesByAreaFrac = true;
		exParam.lowerAreaFrac = 0.7;
		exParam.upperAreaFrac = 1.4;
		FittingParameters fp = new FittingParameters();
		fp.imageWeight = 0.6f;
		fp.spineLengthWeight = 0.6f;
		ep.extrParams = exParam;
		ep.prParams = prParam;
		ep.fitParams = fp;
		args[1] = fullExptBaseDir+"fullExptWithAreaSplit_0.7-1.4_otherPtSplit_loweredImageUppedSpineLengthTerm\\";
		System.out.println("Extracting tracks");
		ep.run(args);
		System.out.println("DONE extracting tracks");
		
		ep = new Experiment_Processor();
		ep.runningFromMain = true;
		exParam.subset = false;
		exParam.splitMatchesByAreaFrac = true;
		exParam.lowerAreaFrac = 0.7;
		exParam.upperAreaFrac = 1.4;
		fp = new FittingParameters();
		int[] gr = {8,4,1};
		fp.grains = gr;
		fp.imageWeight = 0.4f;
		fp.spineLengthWeight = 0.6f;
		ep.extrParams = exParam;
		ep.prParams = prParam;
		ep.fitParams = fp;
		args[1] = fullExptBaseDir+"fullExptWithAreaSplit_0.7-1.4_otherPtSplit_grain8-4-1_loweredImageUppedSpineLengthTerm\\";
		System.out.println("Extracting tracks");
		ep.run(args);
		System.out.println("DONE extracting tracks");

		*/
		ep = new Experiment_Processor();
		ep.runningFromMain = true;
		exParam.subset = false;
		exParam.splitMatchesByAreaFrac = true;
		exParam.lowerAreaFrac = 0.7;
		exParam.upperAreaFrac = 1.4;
		FittingParameters fp = new FittingParameters();
		ep.extrParams = exParam;
		ep.prParams = prParam;
		ep.fitParams = fp;
		args[1] = fullExptBaseDir+"test\\";
		System.out.println("Extracting tracks");
		ep.run(args);
		System.out.println("DONE extracting tracks");
		
		
		imj.quit();
		
		
	}
	
	public static void testFittingParams(){

		
		
		ImageJ imj = new ImageJ(ImageJ.NO_SHOW);
		
		//Setup I/O
		String[] args = new String[2];//[src path, dst dir]
		args[0] = "E:\\data\\phototaxis2\\berlin@berlin\\2NDs_B_Square_SW_96-160\\201411201541\\Berlin@Berlin_2NDs_B_Square_SW_96-160_201411201541.mmf";
		String dstBaseDir = "E:\\testing\\Java Backbone Fitting\\Fitting Params\\NumPts\\"; 
//		String outputFileName = "SDout.txt";
//		try {
//			//Set system.out to a file
//			System.setOut(new PrintStream(new BufferedOutputStream(new FileOutputStream(dstBaseDir+outputFileName))));
//		} catch (FileNotFoundException e) {
//			e.printStackTrace();
//			return;
//		}
		
		//Setup parameters for all tests
		Experiment_Processor ep;
		FittingParameters fitPar;
//		ExtractionParameters exPar;
//		exPar = new ExtractionParameters();
//		exPar.subset = true;
//		exPar.startFrame = 1;
//		exPar.endFrame = 5000;
		
		
		//FIRST RUN: 9 BB points, old params
		args[1] = dstBaseDir+"9pts_oldParams\\";
		fitPar = new FittingParameters();

		ep = new Experiment_Processor();
		ep.runningFromMain = true;
		ep.fitParams = fitPar;
		ep.run(args);
		
		
		//SECOND RUN: 7 BB points, old params (modified)
		args[1] = dstBaseDir+"7pts_oldParams\\";
		fitPar = new FittingParameters();
		float[] imageWeights = {1,1,1, 1,1,1, 1};
		float[] spineLengthWeights = {.7f,1,1, 1,1,1, 1};
		float[] spineSmoothWeights = {.8f,1,1, 1,1,1, 1};
		float[][] timeLengthWeights = { {1,1,1, 1,1,1, 1},
										{1,1,1, 1,1,1, 1},
										{1,1,1, 1,1,1, 1} };
		float[][] timeSmoothWeights = { {1,1,1, 1,1,1, 1},
										{1,1,1, 1,1,1, 1},
										{1,1,1, 1,1,1, 1} };
		fitPar.numBBPts = 7;
		fitPar.imageWeights = imageWeights;
		fitPar.spineLengthWeights = spineLengthWeights;
		fitPar.spineSmoothWeights = spineSmoothWeights;
		fitPar.timeLengthWeights = timeLengthWeights;
		fitPar.timeSmoothWeights = timeSmoothWeights;
		
		ep = new Experiment_Processor();
		ep.runningFromMain = true;
		ep.fitParams = fitPar;
		ep.run(args);
		
		//THIRD RUN: 7 BB points, matlab params
		args[1] = dstBaseDir+"7pts_matlabParams\\";
		fitPar = new FittingParameters();
		fitPar.numBBPts = 7;
		fitPar.imageWeight = 1;
		fitPar.spineLengthWeight = .4f;
		fitPar.spineSmoothWeight = .8f;
		float[] timeLengthWeight_m = {3.0f, .1f, .1f};
		float[] timeSmoothWeight_m = {1.0f, 0.1f, 0.1f};
		fitPar.timeLengthWeight = timeLengthWeight_m;
		fitPar.timeSmoothWeight = timeSmoothWeight_m;
		float[] imageWeights_m = {1,1,1, 1,1,1, 1};
		float[] spineLengthWeights_m = {0,1,1, 1,1,1, 1};
		float[] spineSmoothWeights_m = {.6f,1,1, 1,1,1, 1};
		float[][] timeLengthWeights_m = { {1,1,1, 1,1,1, 1},
										{1,1,1, 1,1,1, 1},
										{1,1,1, 1,1,1, 1} };
		float[][] timeSmoothWeights_m = { {1,1,1, 1,1,1, 1},
										{1,1,1, 1,1,1, 1},
										{1,1,1, 1,1,1, 1} };
		fitPar.imageWeights = imageWeights_m;
		fitPar.spineLengthWeights = spineLengthWeights_m;
		fitPar.spineSmoothWeights = spineSmoothWeights_m;
		fitPar.timeLengthWeights = timeLengthWeights_m;
		fitPar.timeSmoothWeights = timeSmoothWeights_m;
		
		ep = new Experiment_Processor();
		ep.runningFromMain = true;
		ep.fitParams = fitPar;
		ep.run(args);
		
		//FOURTH RUN:
		args[1] = dstBaseDir+"5pts_matlabParams\\";
		fitPar = new FittingParameters();
		fitPar.numBBPts = 5;
		fitPar.imageWeight = 1;
		fitPar.spineLengthWeight = .4f;
		fitPar.spineSmoothWeight = .8f;
		float[] timeLengthWeight_m5 = {3.0f, .1f, .1f};
		float[] timeSmoothWeight_m5 = {1.0f, 0.1f, 0.1f};
		fitPar.timeLengthWeight = timeLengthWeight_m5;
		fitPar.timeSmoothWeight = timeSmoothWeight_m5;
		float[] imageWeights_m5 = {1,1,1, 1,1};
		float[] spineLengthWeights_m5 = {0,1,1, 1,1};
		float[] spineSmoothWeights_m5 = {.6f,1,1, 1,1};
		float[][] timeLengthWeights_m5 = { {1,1,1, 1,1},
										{1,1,1, 1,1},
										{1,1,1, 1,1} };
		float[][] timeSmoothWeights_m5 = { {1,1,1, 1,1},
										{1,1,1, 1,1},
										{1,1,1, 1,1} };
		fitPar.imageWeights = imageWeights_m5;
		fitPar.spineLengthWeights = spineLengthWeights_m5;
		fitPar.spineSmoothWeights = spineSmoothWeights_m5;
		fitPar.timeLengthWeights = timeLengthWeights_m5;
		fitPar.timeSmoothWeights = timeSmoothWeights_m5;
		
		ep = new Experiment_Processor();
		ep.runningFromMain = true;
		ep.fitParams = fitPar;
		ep.run(args);
		
		imj.quit();
	}
	
	public static void testEndClippingForDivergance(){
		
		
		/* PASTE THIS VVV INTO Experiment_Processor.fitTrack();
		int numFramesClipped = 140; //10 seconds
		bbf.clipEnds = true;
		bbf.BTPstartFrame = tr.getStart().frameNum+numFramesClipped;
		bbf.BTPendFrame = tr.getEnd().frameNum-numFramesClipped;
		
		if (bbf.BTPendFrame-bbf.BTPstartFrame < prParams.minTrackLen){
			System.out.println("Too short after clipping");
			return null;
		}
		*/
		
		
		String baseDir = "C:\\Users\\Natalie\\Documents\\Testing\\Building and Fitting - TestExProc\\Fitting diverged with clipped ends\\";
		String fileName = "divergedTrackExp.prejav";
		
		Experiment_Processor ep = new Experiment_Processor();
		ExtractionParameters exParams = new ExtractionParameters();
		exParams.subset = true;
		exParams.endFrame = 2000;
		ep.extrParams = exParams;
		ProcessingParameters prParams= new ProcessingParameters();
		prParams.saveErrors = false;
		prParams.diagnosticIm = false;
		ep.prParams = prParams;
		
		ep.run(baseDir+fileName);
		
		
		
	}
	
	public static void testDMSinProcessing(){
		
		String baseDir = "C:\\Users\\Natalie\\Documents\\Testing\\Building and Fitting - TestExProc\\Point Splitting\\";
		String threshDir = "2000\\Rethresh\\";
		String DMDir = "2000\\DistanceMap\\";
		String mmfName = "Or42a@Chrimson(3)_N_Bl_B0to159s13_120Hz_50W_S1-3#T_Bl_Sq_0to96_30#C_Re_400uW_201603081647.mmf";
		
		ExtractionParameters exParams = new ExtractionParameters();
		exParams.subset = true;
		exParams.endFrame = 2000;
		
		String[] args = new String[3]; //Name-of-mmf; dstdir; dstname
		args[0] = baseDir+mmfName;
		args[2] = mmfName.substring(0, mmfName.length()-4);//remove extension
		
		//Run extraction with basic rethresholding method of point splitting
		exParams.pointSplittingMethod = 1;
		args[1] = baseDir+threshDir;
		Experiment_Processor ep = new Experiment_Processor();
		ep.runningFromMain = true;
		ep.extrParams = exParams;
		ep.run(args);
		
		//Run extraction with distance map method of point splitting
		exParams.pointSplittingMethod = 2;
		args[1] = baseDir+DMDir;
		ep = new Experiment_Processor();
		ep.runningFromMain = true;
		ep.extrParams = exParams;
		ep.run(args);
		
	}
	
	public static void testDistanceMapSplitter(){
		
		ImageJ IJ = new ImageJ();
//		Prefs p = new Prefs();
//		p.blackBackground = false;
		
		
		
		//Grab experiment
		String dir = "C:\\Users\\Natalie\\Documents\\Testing\\Building and Fitting - TestExProc\\Fitting from Prejav\\Divergence investigation\\5 before new pointsplitting";
		String filename = "divergedTrackExp.prejav";
		Experiment ex = new Experiment(Experiment.fromPath(new File(dir, filename).getPath())); 

		//Grab point
		int dataInd = 3;
		int[] trackNums = { 17, 21, 23, 26};
		int[] pointInds = {197, 32, 77, 10};
		int[] numPts    = {  2,  2,  2,  2};
		
		
		int trackNum = trackNums[dataInd];
		Track tr = ex.getTrack(trackNum);
		ImTrackPoint itp = (ImTrackPoint)tr.points.get(pointInds[dataInd]);
		
		ExtractionParameters ep = new ExtractionParameters();
		int nPts = numPts[dataInd];

		int targetArea = 0;
		for (int j=0; j<tr.points.size(); j++){
			targetArea+=tr.points.get(j).area;
		}
		targetArea = targetArea/tr.points.size();
		
		int rethreshVal = CVUtils.findThreshforNumPts(new ImagePlus("",itp.getRawIm().duplicate()), ep, nPts, (int)ep.minSubMaggotArea, (int)ep.maxArea, targetArea, itp.thresh, 255);//117;//139;//
		
		int[] frameSize = {2592,1944}; 
		
		//NOTE: Turn on debugger in DMS.splitPoint
		DistanceMapSplitter.splitPoint(itp, nPts, rethreshVal, targetArea, ep, frameSize, null);
		
		
		IJ.quit();
		
	}
	
	public static void testImDerivs(){
		ImageJ imj = new ImageJ();
		String dir = "E:\\extracted\\optogenetics\\Or42a@Chrimson(X)\\RWN_0.3ohm_BWN_39ohm";
		String filename = "Or42a@Chrimson(X)_RWN_0.3ohm_BWN_39ohm_201504191707.prejav";
		
		Experiment ex = new Experiment(Experiment.fromPath(new File(dir, filename).getPath())); 
		Track t = ex.getTrackFromInd(200);
		
		t.playMovie();
		int i = 6;
		ImTrackPoint itp = (ImTrackPoint)t.getPoint(i);
		ImTrackPoint itp_prev = (ImTrackPoint)t.getPoint(i-1);
		ImTrackPoint itp_next = (ImTrackPoint)t.getPoint(i+1);
		ImTrackPoint itp_prev5 = (ImTrackPoint)t.getPoint(i-5);
		ImTrackPoint itp_next5 = (ImTrackPoint)t.getPoint(i+5);
		
		new ImagePlus("Pt"+(i-5),itp_prev5.getRawIm()).show();
		new ImagePlus("Pt"+(i-1),itp_prev.getRawIm()).show();
		new ImagePlus("**Pt"+(i),itp.getRawIm()).show();
		new ImagePlus("Pt"+(i+1),itp_next.getRawIm()).show();
		new ImagePlus("Pt"+(i+5),itp_next5.getRawIm()).show();
		
		System.out.println("calculating forward deriv...");
		itp.calcImDeriv(itp_prev, itp_next, ExtractionParameters.DERIV_FORWARD);
		new ImagePlus("Forward",itp.imDeriv.duplicate()).show();;
		System.out.println("calculating backward deriv...");
		itp.calcImDeriv(itp_prev, itp_next, ExtractionParameters.DERIV_BACKWARD);
		new ImagePlus("Backward",itp.imDeriv.duplicate()).show();;
		System.out.println("calculating symmetric deriv...");
		itp.calcImDeriv(itp_prev, itp_next, ExtractionParameters.DERIV_SYMMETRIC);
		new ImagePlus("Symmetric",itp.imDeriv.duplicate()).show();;
		
		
		System.out.println("calculating forward deriv...");
		itp.calcImDeriv(itp_prev5, itp_next5, ExtractionParameters.DERIV_FORWARD);
		new ImagePlus("Forward 5",itp.imDeriv.duplicate()).show();
		System.out.println("calculating backward deriv...");
		itp.calcImDeriv(itp_prev5, itp_next5, ExtractionParameters.DERIV_BACKWARD);
		new ImagePlus("Backward 5",itp.imDeriv.duplicate()).show();
		System.out.println("calculating symmetric deriv...");
		itp.calcImDeriv(itp_prev5, itp_next5, ExtractionParameters.DERIV_SYMMETRIC);
		new ImagePlus("Symmetric 5",itp.imDeriv.duplicate()).show();
		
		System.out.println("DONE");
		
		imj.quit();
	}
	
	public static void testDiagIm(){
		
		String dir = "E:\\extracted\\optogenetics\\Or42a@Chrimson(X)\\RWN_0.3ohm_BWN_39ohm";
		String filename = "Or42a@Chrimson(X)_RWN_0.3ohm_BWN_39ohm_201504191707.prejav";
		
		Experiment ex = new Experiment(Experiment.fromPath(new File(dir, filename).getPath())); 
		
		int width = 2048;
		int height = 2048;
		
		ImagePlus diagIm = ex.getDiagnIm(width, height);
		diagIm.show();
		
	}
	
	public static void testParamFromDisk(){
		Experiment_Processor ep = new Experiment_Processor();
		ep.paramFileName = "C:\\Users\\Natalie\\Documents\\Testing\\Parameter_toDisk\\test.txt";
		ep.readParamsFromFile();
		
		System.out.println("blah");
		
	}
	
	public static void testParamToDisk(){
		Experiment_Processor ep = new Experiment_Processor();
		ep.paramFileName = "C:\\Users\\Natalie\\Documents\\Testing\\Parameter_toDisk\\test.txt"; 
		ep.prParams = new ProcessingParameters();
		ep.extrParams = new ExtractionParameters();
		ep.fitParams = new FittingParameters();
		ep.csvPrefs = new CSVPrefs();
		ep.writeParamsToFile();
		
	}
	
	public static void testEx_Proc(){
		
		ImageJ imj = new ImageJ(ImageJ.NO_SHOW);
		
		Experiment_Processor ep = new Experiment_Processor();
		
		
		//Set params from input
		ep.runningFromMain = true;
//		ep.prParams = p;
//		ep.extrParams = e;
//		ep.fitParams = f;
		
		//Set src and dest
		String[] epArgs = new String[3];
		epArgs[0] = "C:\\Users\\Natalie\\Documents\\Testing\\Interface testing\\Berlin@Berlin_2NDs_B_Square_SW_96-160_201411201541.mmf";
		epArgs[1] = "C:\\Users\\Natalie\\Documents\\Testing\\Interface testing\\";
		epArgs[2] = "Berlin@Berlin_2NDs_B_Square_SW_96-160_201411201541";
		
		ep.run(epArgs[0]);
		

		imj.quit();
	}
	
	public static void testExParamaPanel(){
		extrPanel exP = new extrPanel(null);
		JFrame jf = new JFrame();
		jf.add(exP);
		jf.pack();
		jf.setVisible(true);
		
	}
	
	public static void testProcParamPanel(){
		
		ProcPanel prP = new ProcPanel(null);
		JFrame jf = new JFrame();
		jf.add(prP);
		jf.pack();
		jf.setVisible(true);
		
	}
	
	public static void testExractionWindow(){
		
		ExtractorFrame ef = new ExtractorFrame();
		
		ef.run(null);
		
	}
	
	public static void testCSVwriterFrame(){
		
		writerFrame cwf = new writerFrame();
		cwf.run();
		
	}
	
	public static void testToCSV(){
		
		String fname = "C:\\Users\\Natalie\\Documents\\Testing\\CSV writing\\berlin@berlin__LIGHT_RANDOM_WALK_S1_112Hz_201402121807.jav";
		
		Experiment ex = new Experiment(fname);
		
		Experiment.toCSV(ex, fname.replace(".jav", ".csv"));
		
	}
	
	public static void testCSVPanel(){
		
		
		JFrame jf = new JFrame();
		
		csvPrefPanel cpp = new csvPrefPanel();
		
		jf.add(cpp, BorderLayout.CENTER);
		jf.pack();

		jf.setTitle("Test Frame for CSV preferences");
		jf.setVisible(true);
		
	}
	
	public static void testGapDilation(){
		String input = "4 6 ";
		input += "20 20 ";
		input += "36 38 ";
		input += "40 45 ";
		input += "60 65 ";
		input += "62 70 ";

		StringBuilder sb = new StringBuilder();
		
		Vector<Gap> gaps = makeGaps(input);
//		int minValidSegmentLen = 10;
		int startFrame = 1;
		int endFrame = 85;
		boolean dilateToEdges = true;
		
		sb.append("Before Dilation: ("+gaps.size()+")\n"+printGaps(gaps)+"\n");
		BBFPointListGenerator.dilateGaps(gaps, 2, 10, startFrame, endFrame, dilateToEdges);
		sb.append("After Dilation, before Merging: ("+gaps.size()+")\n"+printGaps(gaps)+"\n");
		BBFPointListGenerator.mergeGaps(gaps, 10, null);
		sb.append("After Merging: ("+gaps.size()+")\n"+printGaps(gaps)+"\n");
		
		System.out.println(sb.toString());
	}
	
	public static String printGaps(Vector<Gap> gaps){
		StringBuilder s = new StringBuilder();
		for (Gap g : gaps){
			s.append(g.toString()+". ");
		}
		return s.toString();
	}

	public static Vector<Gap> makeGaps(String input){
		
		Vector<Gap> gaps = new Vector<Gap>();
		Scanner s = new Scanner(input);
		
		while(s.hasNext()){
			gaps.add(new Gap(s.nextInt(), s.nextInt()));
		}
		
		s.close();
		return gaps;
		
	}
	
	public String str(){
		String ret = "";
		for (int i=0; i<fld1; i++){
			ret+=fld2;
		}
		return ret;
	}
	
	public Test(){
		
	}
	
	public Test(int i, String s){
		
		fld1 = i;
		fld2 = s;
		
	}
	
	public void run(String arg0) {
		main(null);
	}

}
