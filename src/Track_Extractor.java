import ij.IJ;
import ij.ImageJ;
import ij.ImageStack;
import ij.WindowManager;
import ij.plugin.PlugIn;
import ij.text.TextWindow;


public class Track_Extractor implements PlugIn{
	
	// Set extraction parameters EXTRACTIONPARAMETERS
	ExtractionParameters ep; 
	
	// Load the mmfs into an imagestack
	ImageStack IS;
	
	// Build the tracks TRACKBUILDER
	TrackBuilder tb;
	//ep= new ExtractionParameters()
	ExperimentFrame ef;
	
	public void run(String arg) {
				
		
		IJ.showStatus("Getting stack");
		IS = WindowManager.getCurrentWindow().getImagePlus().getImageStack();
		if (IS == null) {
			IJ.showMessage("Null ImagePlus");
			return;
		} 

		
		IJ.showStatus("Setting up TrackBuiling");
		ep= new ExtractionParameters();
		IJ.showStatus("Building Tracks");
		
		// 
		if (ep.trackPointType>=2){
			tb = new MaggotTrackBuilder(IS, ep);
		} else {
			tb = new TrackBuilder(IS, ep);
		}
		
		try {
			
			
			tb.run();
			
			
			//////
			IJ.showStatus("Converting to Experiment");
			Experiment exp = tb.toExperiment();
			
			//TODO put this in experiment frame vv
			//Save it to file...
//			IJ.showStatus("Saving file");
//			exp.save("C:\\Users\\Natalie\\Documents", "testSer");
//			String fname = exp.fname;
			
			ef = new ExperimentFrame(exp);
			ef.run(null);
			
			
		}
		catch (Exception e) {
			
			StackTraceElement[] tr = e.getStackTrace();
			String s = e.toString()+"\n";
			for (int i=0; i<tr.length; i++){
				s += tr[i].toString()+"\n";
			}
			
			if (tb!=null){
				tb.comm.message(s, VerbLevel.verb_error);
				new TextWindow("Communicator Output: Error", tb.comm.outString, 500, 500);
			}
		}
		
		
	}
	
	public static void main(String[] args) {
        // set the plugins.dir property to make the plugin appear in the Plugins menu
        Class<?> clazz = Track_Extractor.class;
        String url = clazz.getResource("/" + clazz.getName().replace('.', '/') + ".class").toString();
        String pluginsDir = url.substring(5, url.length() - clazz.getName().length() - 6);
        System.setProperty("plugins.dir", pluginsDir);

        // start ImageJ
        new ImageJ();


        // run the plugin
        //IJ.runPlugIn(clazz.getName(), "");
}
	
}
