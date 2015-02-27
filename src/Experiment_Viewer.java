import ij.IJ;
import ij.io.OpenDialog;
import ij.plugin.PlugIn;
import ij.text.TextWindow;


public class Experiment_Viewer implements PlugIn{

	private Experiment ex;
	private ExperimentFrame exFrame;
	
	
	
	
	public void run(String arg) {
		
		IJ.showStatus("Getting experiment");
		getExperiment();
		if (ex==null){
			IJ.showStatus("Experiment was not opened");
			return;
		}
		
		IJ.showStatus("Modifying experiment...");
		modifyExperiment();
		
		IJ.showStatus("Making experiment frame");
		exFrame = new ExperimentFrame(ex);
		IJ.showStatus("Experiment shown in frame");
		exFrame.run(null);
		
	}
	
	
	private void getExperiment(){
		if (ex==null||ex.tracks==null||ex.tracks.size()==0){
			//open a browser box]
			OpenDialog od = new OpenDialog("Choose a .ser file containing an experiment", null);
			
			String fileName = od.getFileName();
			
			String dir = od.getDirectory();
			if (null == dir) return ; // dialog was canceled
			dir = dir.replace('\\', '/'); // Windows safe
			if (!dir.endsWith("/")) dir += "/";
			
			String path = dir + fileName;
			
			//try to open file
			try {
				IJ.showStatus("Opening the experiment file");
				ex = Experiment.open(path); 
			} catch (Exception e){
				new TextWindow("Error opening experiment", e.getMessage(), 500, 500);
			}
		}
			
		
	}
	
	//This is a TEMPORARY method to make life a little easier during development of the backbone fitter
	private void modifyExperiment(){
		IJ.showStatus("Creating Backbone Fitter");
		BackboneFitter bbf = new BackboneFitter();
		IJ.showStatus("Fitting Track");
		bbf.fitTrack(ex.tracks.get(0));
		IJ.showStatus("Track fit!");
		ex.tracks.add(new Track(bbf.BTPs));
		
		
		
	}
	
	
	

}
