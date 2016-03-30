package TrackExtractionJava;

import ij.ImageJ;
import ij.ImagePlus;
import ij.Prefs;

import java.awt.BorderLayout;
import java.io.File;
import java.util.Scanner;
import java.util.Vector;

import javax.swing.JFrame;



public class Test {//extends JFrame

	
	public int fld1;
	public String fld2;
	
		

	public static void main(String[] args) {
		
		
		
		testDistanceMapSplitter();
		
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
		
		Vector<TrackPoint> splitPts =  DistanceMapSplitter.splitPoint(itp, nPts, rethreshVal, targetArea, ep, frameSize, null);
		
		for (int i=0; i<splitPts.size(); i++){
			new ImagePlus("split point "+i, ((ImTrackPoint)splitPts.get(i)).getRawIm()).show();
		}
		
		
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
		BackboneFitter.dilateGaps(gaps, 2, 10, startFrame, endFrame, dilateToEdges);
		sb.append("After Dilation, before Merging: ("+gaps.size()+")\n"+printGaps(gaps)+"\n");
		BackboneFitter.mergeGaps(gaps, 10, null);
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
