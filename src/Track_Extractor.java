import ij.IJ;
import ij.ImageJ;
import ij.ImageStack;
//import ij.ImageStack;
import ij.WindowManager;
import ij.gui.GenericDialog;
import ij.plugin.PlugIn;
import ij.text.TextWindow;


public class Track_Extractor implements PlugIn{
	
	//TODO Set extraction parameters EXTRACTIONPARAMETERS
	ExtractionParameters ep; 
	
	//TODO Load the mmfs into an imagestack
	ImageStack IS;
	
	//TODO Build the tracks TRACKBUILDER
	TrackBuilder tb;
	//ep= new ExtractionParameters()
	
	public void run(String arg) {
				
		IJ.showStatus("Getting stack");
		//IS = WindowManager.getCurrentImage();
		IS = WindowManager.getCurrentWindow().getImagePlus().getImageStack();
		//TODO Enforce the video type
		if (IS == null) {
			IJ.showMessage("Null ImagePlus");
			return;
		} 

		
		IJ.showStatus("Setting up TrackBuiling");
		ep= new ExtractionParameters();
		IJ.showStatus("Building Tracks");
		
		// 
		tb = new MaggotTrackBuilder(IS, ep);
		
		try {
			
//			tb.buildTracks();
			tb.run();
			
			
			GenericDialog gd = new GenericDialog("Track chooser");
			gd.addMessage("Choose a track: (0-"+(tb.finishedTracks.size()-1)+")");
			String st = "CollisionTracks:";
			for (int i=0; i<tb.finishedColIDs.size(); i++) {
				st += " "+tb.finishedColIDs.get(i);
			}
			gd.addMessage(st);
			gd.addMessage("Then press enter");
			gd.addMessage("To close, X out of this box");
			gd.addNumericField("Track", 1, 0);
			
			if (tb.comm.verbosity!=VerbLevel.verb_off && !tb.comm.outString.equals("")){
				new TextWindow("Communicator Output", tb.comm.outString, 500, 500); 
			}
			
			while (!gd.wasCanceled()){
				gd.showDialog();
				//EXECUTE THIS ON "OKAY" PRESS
				
				int num = (int)gd.getNextNumber();
				
				if (num>=0 && num<=tb.finishedTracks.size() && !gd.wasCanceled()){
					
					int trackInd = tb.findIndOfTrack(num, tb.finishedTracks);
					try {
						Track track = tb.finishedTracks.get(trackInd);
						track.playMovie();
					} catch (Exception e) {
						
						StackTraceElement[] tr = e.getStackTrace();
						String s = e.toString()+"\n";
						for (int i=0; i<tr.length; i++){
							s += tr[i].toString()+"\n";
						}
						
						new TextWindow("Error", "Error playing trackID number "+num+"\n"+s, 500, 500);
					}

				}
				
			}
			

			
		}
		catch (Exception e) {
			
			StackTraceElement[] tr = e.getStackTrace();
			String s = e.toString()+"\n";
			for (int i=0; i<tr.length; i++){
				s += tr[i].toString()+"\n";
			}
			
			tb.comm.message(s, VerbLevel.verb_error);
			new TextWindow("Communicator Output: Error", tb.comm.outString, 500, 500);
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
