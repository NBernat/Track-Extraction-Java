//import ij.IJ;
import javax.print.attribute.standard.Finishings;

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
		
		//GUItest.main();
		
		IJ.showStatus("Getting stack");
		//IS = WindowManager.getCurrentImage();
		IS = WindowManager.getCurrentWindow().getImagePlus().getImageStack();
		if (IS == null) {
			IJ.showMessage("Null ImagePlus");
			return;
		} 
//		IJ.showMessage("Slices: "+IS.getNSlices()+"; Frames: "+IS.getNFrames());
//		if (IS.getProcessor() != null) {
//			IJ.showMessage("Is an ImageProcessor");
//		} 
//		if (IS.getProcessor(0)==null) {
//			IJ.showMessage("Zero Null ");
//		} 
//		if (IS.getProcessor(1)==null) {
//			IJ.showMessage("One Null ");
//		}
		
		IJ.showStatus("Setting up TrackBuiling");
		ep= new ExtractionParameters();
		IJ.showStatus("Building Tracks");
		
		// 
		tb = new TrackBuilder(IS, ep);
		
		try {
			
			tb.buildTracks();
			
			
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
			
			while (!gd.wasCanceled()){
				gd.showDialog();
				//EXECUTE THIS ON "OKAY" PRESS
				int num = (int)gd.getNextNumber();
				
				if (num>=0){
					if (num<=tb.finishedTracks.size() && !gd.wasCanceled()){
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
	//					TextWindow tw = new TextWindow("Match Spill for frame ", tb.matchSpills.get(track.points.lastElement().frameNum).outString, 500, 500);
					}
				} else{
//					num = -1-num;//this makes it an index of collision tracks
//					//TODO Search through the finishedCollisions for the specified collision
//					if (num<=tb.finishedTracks.size() && !gd.wasCanceled()){
//						int trackInd = tb.findIndOfTrack(num, tb.finishedTracks);
//						try {
//							Track track = tb.finishedTracks.get(trackInd);
//							track.playMovie();
//						} catch (Exception e) {
//							
//							StackTraceElement[] tr = e.getStackTrace();
//							String s = e.toString()+"\n";
//							for (int i=0; i<tr.length; i++){
//								s += tr[i].toString()+"\n";
//							}
//							
//							new TextWindow("Error", "Error playing trackID number "+num+"\n"+s, 500, 500);
//						}
////						TextWindow tw = new TextWindow("Match Spill for frame ", tb.matchSpills.get(track.points.lastElement().frameNum).outString, 500, 500);
//					}
				}
				
			}
			
			new TextWindow("Communicator Output", tb.comm.outString, 500, 500);
			
//			if (ep.showSampleData>=1){
//				int trackInd = ep.sampleInd;
//				tb.comm.message("Number of Finished Tracks: "+tb.finishedTracks.size(), VerbLevel.verb_message);
//				IJ.showStatus("Playing Track "+trackInd);
//				tb.comm.message("Playing TrackID "+tb.finishedTracks.get(trackInd).trackID, VerbLevel.verb_message);
//				tb.finishedTracks.get(trackInd).playMovie(trackInd);
//	
//				tb.comm.message("Completed successfully!", VerbLevel.verb_message);
//				if (ep.showSampleData>=2){
//					TextWindow tw = new TextWindow("Communicator Output", tb.comm.outString, 500, 500);
//				}
//			}
//			
			
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
