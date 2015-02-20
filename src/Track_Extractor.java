import java.io.File;

import ij.IJ;
import ij.ImageJ;
import ij.ImageStack;
//import ij.gui.GenericDialog;
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
	
	public void run(String arg) {
				
		/**
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
		**/
		try {
			
			/**
			tb.run();
			
			
			//////
			IJ.showStatus("Converting to Experiment");
			Experiment exp = tb.toExperiment();
			
			//Save it to file...
			IJ.showStatus("Saving file");
			exp.save("C:\\Users\\Natalie\\Documents", "testSer");
			String fname = exp.fname;
			**/
			
			
			String fname = "C:\\Users\\Natalie\\Documents"+File.separator+"testSer"+".ser";
			IJ.showStatus("Opening File");
			Experiment ex = Experiment.open(fname); 
			IJ.showStatus("Experiment Opened");
			////// 
			
			IJ.showStatus("Creating Backbone Fitter");
			BackboneFitter bbf = new BackboneFitter();
			IJ.showStatus("Fitting Track");
			bbf.fitTrack(ex.tracks.get(0));
			IJ.showStatus("Track fit!");
			ex.tracks.add(new Track(bbf.BTPs));
			ExperimentFrame exFrame = new ExperimentFrame(ex);
			exFrame.run(null);
			
			/*
			///////////////////////////////////////////////////////////////////////
			//OLD GUI
			GenericDialog gd = new GenericDialog("Track chooser");
			gd.addMessage("Choose a track: (0-"+(ex.tracks.size()-1)+")");
			if (ex.collisionTrackIDs.size()>0){
				String st = "CollisionTracks:";
				for (int i=0; i<ex.collisionTrackIDs.size(); i++) {
					st += " "+ex.collisionTrackIDs.get(i);
				}
				gd.addMessage(st);
			}
			gd.addMessage("Then press enter");
			gd.addMessage("To close, X out of this box");
			gd.addNumericField("Track", 1, 0);
			
			if (tb!=null && tb.comm.verbosity!=VerbLevel.verb_off && !tb.comm.outString.equals("")){
				new TextWindow("Communicator Output", tb.comm.outString, 500, 500); 
			}
		
			
			while (!gd.wasCanceled()){
				gd.showDialog();
				//EXECUTE THIS ON "OKAY" PRESS
				
				int num = (int)gd.getNextNumber();
				
//				if (num>=0 && num<=tb.finishedTracks.size() && !gd.wasCanceled()){
				if (num>=0 && num<=ex.tracks.size() && !gd.wasCanceled()){
					
					int trackInd = TrackBuilder.findIndOfTrack(num, ex.tracks);
					try {
						Track track = ex.tracks.get(trackInd);
//						Track track = tb.finishedTracks.get(trackInd);
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
			//END OLD GUI
			///////////////////////////////////////////////////////////////////////
			
			*/
			///////////////////////////////////////////////////////////////////////
			//NEW GUI
//			Experiment exp = tb.toExperiment();
			//Feed this into the new GUI
			
			
			
			
			//END NEW GUI
			///////////////////////////////////////////////////////////////////////
			
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
