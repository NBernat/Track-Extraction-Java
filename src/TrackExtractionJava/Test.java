package TrackExtractionJava;


import java.util.Scanner;
import java.util.Vector;

public class Test {//extends JFrame

	
	public int fld1;
	public String fld2;
	
	
	/**
	 * 
	 */
//	private static final long serialVersionUID = 1L;
	
	

	public static void main(String[] args) {
		
		String inDir = "C:\\Users\\Natalie\\Documents\\TestJavaMat\\data\\201406122014";
		
		System.out.println(inDir);
		System.out.println(ProcessingParameters.getOutFromInDir(inDir));
		
		
//		ImagePlus imp = new ImagePlus("C:\\Users\\Natalie\\Documents\\TestExProc\\unmaskTest.tif");
//		imp.show();
		
//		testGapDilation();
	}
	
	private static void testGapDilation(){
		String input = "4 6 ";
		input += "20 20 ";
		input += "36 38 ";
		input += "40 45 ";
		input += "60 65 ";
		input += "62 70 ";

		StringBuilder sb = new StringBuilder();
		
		Vector<Gap> gaps = makeGaps(input);
		int minValidSegmentLen = 10;
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
	
	private static String printGaps(Vector<Gap> gaps){
		StringBuilder s = new StringBuilder();
		for (Gap g : gaps){
			s.append(g.toString()+". ");
		}
		return s.toString();
	}

	
	private static Vector<Gap> makeGaps(String input){
		
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
	
	public Test(int i, String s){
		
		fld1 = i;
		fld2 = s;
		
	}
	
	
	
	public void run(String arg0) {
		main(null);
	}

}
