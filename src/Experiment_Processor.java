import java.io.File;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Vector;

import ij.IJ;
import ij.ImagePlus;
import ij.WindowManager;
import ij.io.OpenDialog;
import ij.plugin.PlugIn;
import ij.text.TextWindow;


public class Experiment_Processor implements PlugIn{

	ProcessingParameters prParams; 
	private String srcDir;
	private String srcName;
	private PrintWriter processLog;
	
	
	private ImagePlus mmfStack;
	private BackboneFitter bbf;
	private Experiment ex;
	
	
	/**
	 * Opens a file (via argument or dialog); extracts if needed, and fits tracks; saves extracted and fit tracks
	 */
	public void run(String arg0) {
		
		init();
		
		boolean success = (loadFile(arg0) && setupLog());
		try {
			if(success){
				
				if (ex==null){
					extractTracks();
					if (prParams.saveMagEx) saveOldTracks();
				}
				
				fitTracks();
				if (prParams.saveFitEx) saveNewTracks();
				
			}
		} catch (Exception e){
			
		} finally {
			if (processLog!=null) processLog.close();
		}
		
		IJ.showStatus("Done Processing");
		
	}
	
	
	/**
	 * Set ups the processing objects
	 */
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
		
		boolean success;
		
		//Get the path name...
		if (arg0==null || arg0.equals("")){ //...from a dialog
			OpenDialog od = new OpenDialog("Choose an experiment (.mmf or .ser)", null);
			dir = od.getDirectory();
			fileName = od.getFileName();
			
		} else {//...from the passed argument
			IJ.showStatus("Loading file "+arg0);
			StringBuilder sb = new StringBuilder(arg0);
			int sep = sb.lastIndexOf(System.getProperty("path.separator"));
			if (sep>0){
				fileName = sb.substring(sep+1);
				dir = sb.substring(0, sep-1);
			}
		}
		
		//Open the file with the appropriate method
		if (dir!=null){
			
			srcDir = dir;
			srcName = fileName;
			//TODO logPath = 
			
			IJ.showStatus("Loading file "+fileName);
			
			if (fileName.substring(fileName.length()-4).equalsIgnoreCase(".mmf")){
				success = openMMF(dir, fileName);
				
			} else if (fileName.substring(fileName.length()-4).equalsIgnoreCase(".ser")){
				success = openExp(dir, fileName);
				
			} else {
				IJ.showMessage("File not recognized as a .mmf or a .ser");
				success = false;
			}
		} else {
			IJ.showMessage("Could not load file; null directory");
			success = false;
		}
		
		return success;
	}
	/**
	 * Loads an imageStack into the processor from a .mmf file
	 * @param dir
	 * @param filename
	 * @return Status of the file opening: true=successful
	 */
	private boolean openMMF(String dir, String filename){
		try{
			IJ.showStatus("Opening MMF...");		
			IJ.run("Import MMF", "path=["+new File(dir, filename).getPath()+"]");
			mmfStack = WindowManager.getCurrentWindow().getImagePlus();
			IJ.showStatus("MMF open");
			return true;
		} catch (Exception e){
			new TextWindow("Error opening experiment", e.getMessage(), 500, 500);
			return false;
		}
		
	}
	/**
	 * Loads an experiment into the processor from a .ser file
	 * @param dir
	 * @param filename
	 * @return Status of the file opening: true=successful
	 */
	private boolean openExp(String dir, String filename){
		
		try {
			IJ.showStatus("Opening Experiment...");
			ex = new Experiment(Experiment.open(new File(dir, filename).getPath())); 
			IJ.showStatus("Experiment open");
			return true;
		} catch (Exception e){
			new TextWindow("Error opening experiment", e.getMessage(), 500, 500);
			return false;
		}
		
	}
	
	
	/**
	 * Sets up the Processing Log (A text file containing output RE processing events)
	 * <p>
	 * Log is saved in a txt file named according to the naming convention defined in the Processing Parameters. If the 
	 * file already exists, this log is appended to the end of that file
	 * @return Success of the creation of the log 
	 */
	private boolean setupLog(){
		
		try{
			String[] logPathParts = prParams.setLogPath(srcDir, srcName);
			processLog = new PrintWriter(new FileWriter(new File(logPathParts[0], logPathParts[1]).getPath(), true));
			
			processLog.println();
			processLog.println("----------------------------------------------");
			DateFormat df = new SimpleDateFormat("MM-dd-yyyy hh:mm z");
			processLog.println("Log entry at "+df.format(new Date()));
			processLog.println("----------------------------------------------");
			processLog.println();
			
			
			return true;
		} catch (Exception e){
			new TextWindow("Log error", "Unable to create Processing Log file '"+srcName+"' in '"+srcDir+"'", 500, 500);
			return false;
		}
	}
	
	
	/**
	 * Runs track extraction on the imageStack
	 */
	private void extractTracks(){
		
		try {
			//Extract the tracks
			IJ.showStatus("Extracting tracks");
			MaggotTrackBuilder tb = new MaggotTrackBuilder(mmfStack.getImageStack(), new ExtractionParameters());
			tb.run();
			ex = new Experiment(tb.toExperiment());
			tb.showCommOutput();
			
			//Show the extracted tracks
			if(prParams.showMagEx){
				IJ.showStatus("Showing Experiment");
				ExperimentFrame exFrame = new ExperimentFrame(ex);
				exFrame.run(null);
			}
			
		} catch  (Exception e){
			new TextWindow("Error opening experiment", e.getMessage(), 500, 500);
		}
		
	}
	/**
	 * Replaces each track in the experiment with a backbone-fitted track
	 */
	private void fitTracks(){
		
		IJ.showStatus("Fitting Tracks...");
		
		Track tr;
		Track newTr = null;
		Vector<Track> toRemove = new Vector<Track>();

		//Fit each track that is long enough for the course passes
		for (int i=0; i<ex.tracks.size(); i++){
			tr = ex.tracks.get(i);
			if (tr.points.size()>prParams.minTrackLen) {//Check track length
				newTr = fitTrack(tr);
				if (newTr!=null){
					ex.replaceTrack(newTr, i);
				} else {
					toRemove.add(tr);
				}
			} else {
				toRemove.add(tr);
			}
		}
		
		bbf.showCommOutput();
		
		//Remove the tracks that couldn't be fit
		for(Track t : toRemove){
			ex.tracks.remove(t);
		}
		
		IJ.showStatus("Done fitting tracks");
		
		//Show the fitted tracks
		if (prParams.showFitEx){
			IJ.showStatus("Making experiment frame");
			ExperimentFrame exFrame = new ExperimentFrame(ex);
			IJ.showStatus("Experiment shown in frame");
			exFrame.run(null);
		} 
		
	}
	/**
	 * Fits backbones to a Track of MaggotTrackPoints
	 * @param tr The track of MaggotTrackPoints to be fit
	 * @return A track fit by the BackboneFitter
	 */
	private Track fitTrack(Track tr){
		bbf.fitTrack(tr);
		return bbf.getTrack();
	}

	
	/**
	 * Saves the maggotTracks according to the naming convention defined in the Processing Parameters
	 */
	private void saveOldTracks(){
		String[] pathParts = prParams.setMagExPath(srcDir, srcName);
		ex.save(pathParts[0], pathParts[1]); 
	}
	/**
	 * Saves the backboneTracks according to the naming convention defined in the Processing Parameters
	 */
	private void saveNewTracks(){
		String[] pathParts = prParams.setFitExPath(srcDir, srcName);
		ex.save(pathParts[0], pathParts[1]); 
	}
}






