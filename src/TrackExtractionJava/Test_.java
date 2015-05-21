package TrackExtractionJava;


import ij.IJ;
import ij.ImagePlus;
import ij.io.OpenDialog;
import ij.plugin.PlugIn;


public class Test_ implements PlugIn {//extends JFrame

	
	public int fld1;
	public String fld2;
	

	public static void main(String[] args) {
		
//		OpenDialog od = new OpenDialog("Choose an experiment (.mmf or .ser)", null);
//		String path = od.getPath();
		String path = "C:/Users/Natalie/Documents/TestExProc/unmaskTest.jpg";
		ImagePlus imp = new ImagePlus(path);//IJ.openImage(path);
		imp.show();
		
	}

	
	public Test_(int i, String s){
		
		fld1 = i;
		fld2 = s;
		
	}
	
	public String str(){
		String ret = "";
		for (int i=0; i<fld1; i++){
			ret+=fld2;
		}
		return ret;
	}
	
	@Override
	public void run(String arg0) {
//		main(null);
		OpenDialog od = new OpenDialog("Choose an experiment (.mmf or .ser)", null);
		String path = od.getPath();
//		String path = "C:\\Users\\Natalie\\Documents\\TestExProc\\unmaskTest.jpg";
		ImagePlus imp = IJ.openImage(path);
		imp.show();
		
	}

}
