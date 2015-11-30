package TrackExtractionJava;

import java.awt.BorderLayout;
import java.util.Scanner;
import java.util.Vector;

import javax.swing.JFrame;
import javax.swing.JWindow;



public class Test {//extends JFrame

	
	public int fld1;
	public String fld2;
	
		

	public static void main(String[] args) {
		
		testExParamaPanel();
		
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
