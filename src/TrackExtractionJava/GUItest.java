package TrackExtractionJava;

import ij.gui.GenericDialog;


public class GUItest {

//	GenericDialog gd;
	
	public static void main(){
		GenericDialog gd = new GenericDialog("Test gui");
		
		
		String title="Example";
		int width=512,height=512;
		gd.addStringField("Title: ", title);
	    gd.addNumericField("Width: ", width, 0);
	    gd.addNumericField("Height: ", height, 0);
	    gd.showDialog();
	    if (gd.wasCanceled()) return;
	}
	
}
