import java.awt.List;
import java.io.File;
import java.util.Vector;

import ij.IJ;
import ij.ImageStack;
import ij.io.OpenDialog;
import ij.plugin.PlugIn;
import ij.text.TextWindow;


public class Experiment_Processor implements PlugIn{

	ProcessingParameters prParams; 
	
	private ImageStack mmfStack;
	
	private BackboneFitter bbf;
	
	Experiment ex;
	
	
	
	public void run(String arg0) {
		
		init();
		
		boolean success = loadFile(arg0);
		if(success && ex==null){
			extractTracks();
		}
		fitTracks();
	}
	
	private void init(){
		prParams = new ProcessingParameters();
		bbf = new BackboneFitter();
	}
	
	
	/**
	 * Loads whichever object (experiment or imageStack) is indicated
	 * @param arg0 The complete filename, or null to open a file chooser dialog
	 */
	private boolean loadFile(String arg0){
		String fileName=null;
		String dir=null;
		
		boolean success = false;
		
		if (arg0==null || arg0.equals("")){
			OpenDialog od = new OpenDialog("Choose an experiment (.mmf or .ser)", null);
			dir = od.getDirectory();
			fileName = od.getFileName();
			
		}else {
			IJ.showStatus("Loading file "+arg0);
			StringBuilder sb = new StringBuilder(arg0);
			int sep = sb.lastIndexOf(System.getProperty("path.separator"));
			if (sep>0){
				fileName = sb.substring(sep+1);
				dir = sb.substring(0, sep-1);
			}
		}
		
		
		if (dir!=null){
			IJ.showStatus("Loading file "+fileName);
			if (fileName.substring(fileName.length()-4).equalsIgnoreCase(".mmf")){
				success = openMMF(dir, fileName);
			} else if (fileName.substring(fileName.length()-4).equalsIgnoreCase(".ser")){
				success = openExp(dir, fileName);
				
			} else {
				//Error, not a file 
				IJ.showMessage("File not recognized as a .mmf or a .ser");
			}
		} else {
			IJ.showMessage("Could not load file; null directory");
		}
		
		return success;
	}

	
	private boolean openMMF(String dir, String filename){
//		IJ.runPlugIn(className, arg)
		//CALL MMF OPENER PLUGIN
		return false;
	}
	
	private boolean openExp(String dir, String filename){
		
		String path = new File(dir, filename).getPath(); 
		try {
			IJ.showStatus("Opening the experiment file");
			ex = Experiment.open(path); 
			return true;
		} catch (Exception e){
			new TextWindow("Error opening experiment", e.getMessage(), 500, 500);
			return false;
		}
		
		
//		IJ.runPlugIn("Experiment Viewer", new File(dir, filename).getPath());
		//CALL exp OPENER PLUGIN
	}
	
	
	private void extractTracks(){
		//call extractor
		//save experiment file && store in ex
	}
	
	private void fitTracks(){
		
		
		Track tr;
		Track newTr = null;
		Vector<Track> toRemove = new Vector<Track>();
		for (int i=0; i<ex.tracks.size(); i++){
			tr = ex.tracks.get(i);
			if (tr.points.size()>prParams.minTrackLen) {
				newTr = fitTrack(tr);
				if (newTr!=null){//?? check that this is the case whenever fitTrack works 
					ex.replaceTrack(newTr, i);
					//replace track
				} else {
					toRemove.add(tr);
				}
				
			} else {
				toRemove.add(tr);
				//remove track, make note
				
			}
		}
		
		for(Track t : toRemove){
			ex.tracks.remove(t);
		}
		
		//save modified experiment
		IJ.showStatus("Making experiment frame");
		ExperimentFrame exFrame = new ExperimentFrame(ex);
		IJ.showStatus("Experiment shown in frame");
		exFrame.run(null);
	}
	
	private Track fitTrack(Track tr){
		
		bbf.fitTrack(tr);
		
		return bbf.getTrack();
		
		//make a fitter
//		return null;
	}
	
}
