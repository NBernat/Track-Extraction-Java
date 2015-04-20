import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
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
	private String outputDir;
	
	private ImagePlus mmfStack;
	private BackboneFitter bbf;
	private Experiment ex;
	
	private TicToc runTime;
	private int indentLevel;
	
	//TODO command line invocation
	/**
	public void main(String[] args){
		
		
		String arg0=null;
		outputDir = null;
		int i=0;
		String s;		
		while (i<args.length){
			
			s=args[i];
			
			if (s.equalsIgnoreCase("input")){
				i++;
				String[] out = findArgUnit(args, i);
				if (out!=null){
					arg0 = out[0];
					i = Integer.parseInt(out[1]);
				} else {
					System.out.println("Invalid input directory name");
				}
			} else if (s.equalsIgnoreCase("outputdir")){
				i++;
				String[] out = findArgUnit(args, i);
				if (out!=null){
					outputDir = out[0];
					i = Integer.parseInt(out[1]);
				} else {
					System.out.println("Invalid output directory name");
				}
				
			}
			
			i++;
		}
		
		//Open IJ
		run(arg0);
		
		
		
		
	}
	
	private String[] findArgUnit(String[] args, int startInd){
		
		if (args.length<=startInd){
			return null;
		} else {
			String[] out = new String[2];
			
			//Check beginning
			if (args[startInd].charAt(0) != '['){
				return null;
			}
			
			//Find end
			String s="";
			int i = startInd;
			boolean endFound = false;
			while (!endFound && i<args.length){
				s += args[i];
				if (s.charAt(s.length()-1)==']'){
					endFound=true;
				} else {
					
					i++;
				}
			}
			
			if (endFound){
				return out;
			} else {
				return null;
			}
		}
	}
	
	*/
	
	/**
	 * Opens a file (via argument or dialog); extracts if needed, and fits tracks; saves extracted and fit tracks
	 */
	public void run(String arg0) {
		indentLevel=0;
		runTime = new TicToc();
//		runTime.tic();
				
				
//		log("Initiating processor");
		init();
		
//		log("Loading File...");
		boolean success = (loadFile(arg0) );
		try {
			runTime.tic();
			setupLog();
			log("Initiated log entry");
			if(success){
				if (ex==null){
					
					log("Loaded mmf; Extracting tracks...");
					extractTracks();
					log("...done extracting tracks");
					if (prParams.saveMagEx) {
						log("Saving Maggot Tracks...");
						saveOldTracks();
						log("Done saving Maggot Tracks");
					}
					
					//TODO release memory to OS? System.gc
				}
				
				log("Fiting "+ex.tracks.size()+" Tracks...");
				fitTracks();
				log("...done fitting tracks");
				if (prParams.saveFitEx) {
					log("Saving backbone tracks...");
					if (saveNewTracks()){
						log("Done saving backbone tracks");
					} else {
						log("Error saving tracks");
					}
				}
				//TODO release memory to OS? System.gc
			} else {
				log("...no success");
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
		indentLevel++;
		
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
		
		indentLevel--;
		return success;
	}
	/**
	 * Loads an imageStack into the processor from a .mmf file
	 * @param dir
	 * @param filename
	 * @return Status of the file opening: true=successful
	 */
	private boolean openMMF(String dir, String filename){
		indentLevel++;
		try{
			IJ.showStatus("Opening MMF...");		
			IJ.run("Import MMF", "path=["+new File(dir, filename).getPath()+"]");
			mmfStack = WindowManager.getCurrentWindow().getImagePlus();
//			if (prParams.closeMMF) WindowManager.getCurrentWindow().close();
			IJ.showStatus("MMF open");
			return true;
		} catch (Exception e){
			new TextWindow("Error opening experiment", e.getMessage(), 500, 500);
			return false;
		} finally{
			indentLevel--;
		}
	}
	/**
	 * Loads an experiment into the processor from a .ser file
	 * @param dir
	 * @param filename
	 * @return Status of the file opening: true=successful
	 */
	private boolean openExp(String dir, String filename){
		indentLevel++;
		try {
			IJ.showStatus("Opening Experiment...");
			ex = new Experiment(Experiment.open(new File(dir, filename).getPath())); 
			IJ.showStatus("Experiment open");
			return true;
		} catch (Exception e){
			new TextWindow("Error opening experiment", e.getMessage(), 500, 500);
			return false;
		} finally{
			indentLevel--;
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
		indentLevel++;
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
		} finally{
			indentLevel--;
		}
		
		
	}
	
	
	/**
	 * Runs track extraction on the imageStack
	 */
	private void extractTracks(){
		indentLevel++;
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
		} finally{
			indentLevel--;
		}
		
	}
	/**
	 * Replaces each track in the experiment with a backbone-fitted track
	 */
	private void fitTracks(){
		indentLevel++;
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
		indentLevel--;
	}
	/**
	 * Fits backbones to a Track of MaggotTrackPoints
	 * @param tr The track of MaggotTrackPoints to be fit
	 * @return A track fit by the BackboneFitter
	 */
	private Track fitTrack(Track tr){
		indentLevel++;
		bbf.fitTrack(tr);
		indentLevel--;
		return bbf.getTrack();
		
	}

	
	/**
	 * Saves the maggotTracks according to the naming convention defined in the Processing Parameters
	 */
	private void saveOldTracks(){
		indentLevel++;
		String[] pathParts = prParams.setMagExPath(srcDir, srcName);
		ex.save(pathParts[0], pathParts[1]); 
		indentLevel--;
	}
	/**
	 * Saves the backboneTracks according to the naming convention defined in the Processing Parameters
	 */
	private boolean saveNewTracks(){
		indentLevel++;
		
//		String[] pathParts = prParams.setFitExPath(srcDir, srcName);
//		File f = new File(pathParts[0]+File.separator+pathParts[1]);
//		
//		boolean status;
//		try{
//			ex.toDisk(new DataOutputStream(new FileOutputStream(f)), processLog);
//			status=true;
//		} catch(Exception e){
//			status=false;
//		}
		String[] pathParts = prParams.setFitExPath(srcDir, srcName);
		ex.save(pathParts[0], pathParts[1]);
		
		indentLevel--;
		return true;
//		return status;
	}
	
	private void log(String message){
		String indent = "";
		for (int i=0;i<indentLevel; i++) indent+="----";
		processLog.println(runTime.tocSec()+indent+" "+message);
	}
	
}




class TicToc{
	
	private long startTime;
	
	public TicToc(){
		
	}
	
	public void tic(){
		startTime = System.currentTimeMillis();
	}
	
	public long toc(){
		return System.currentTimeMillis()-startTime;
	}
	
	public long tocSec(){
		return (toc())/1000;
	}
	
	
}

