package TrackExtractionJava;

import ij.IJ;
import ij.ImageJ;
import ij.io.OpenDialog;
import ij.plugin.PlugIn;
import ij.text.TextWindow;


public class Experiment_Viewer implements PlugIn{

	private Experiment ex;
	private ExperimentFrame exFrame;
	
	
	public static void main(String[] args){
		
        // set the plugins.dir property to make the plugin appear in the Plugins menu
		Class<?> clazz = Experiment_Viewer.class; 
        String url = clazz.getResource("/" + clazz.getName().replace('.', '/') + ".class").toString();
        String pluginsDir = url.substring(5, url.length() - clazz.getName().length() - 6);
        System.setProperty("plugins.dir", pluginsDir);
		/*
        Class<?> clazz2 =  mmf_Reader.class; 
        String url2 = clazz2.getResource("/" + clazz2.getName().replace('.', '/') + ".class").toString();
        String pluginsDir2 = url2.substring(5, url2.length() - clazz2.getName().length() - 6);
        System.setProperty("plugins.dir", pluginsDir2);
        */
         
        // start ImageJ
        new ImageJ();

        // run the plugin
        IJ.runPlugIn(clazz.getName(), "");
		
		
	}
	
	public void run(String arg) {
		
		IJ.showStatus("Getting experiment");
		getExperiment(arg);
		if (ex==null){
			IJ.showStatus("Experiment was not opened");
			return;
		}
		
//		IJ.showStatus("Modifying experiment...");
//		modifyExperiment();
		
		IJ.showStatus("Making experiment frame");
		exFrame = new ExperimentFrame(ex);
		IJ.showStatus("Experiment shown in frame");
		exFrame.run(null);
		
		ex = null;
		exFrame = null;
		
	}
	
	
	private void getExperiment(String arg){
		if (ex==null||ex.getNumTracks()<1){
			//open a browser box]
			String path;
			if(arg!=null && !arg.equals("")){
				path = arg;
			}else {
				OpenDialog od = new OpenDialog("Choose a .jav or .prejav file containing an experiment", null);
				
				String fileName = od.getFileName();
				
				String dir = od.getDirectory();
				if (null == dir) return ; // dialog was canceled
				dir = dir.replace('\\', '/'); // Windows safe
				if (!dir.endsWith("/")) dir += "/";
				
				path = dir + fileName;
			}
			
			
			//try to open file
			try {
				String ext = path.substring(path.length()-3, path.length());
				if (ext.equalsIgnoreCase("ser")){
					IJ.showStatus("Opening the experiment file");
					ex = Experiment.deserialize(path);
				} else if (ext.equalsIgnoreCase("jav")){
					ex =  Experiment.fromPath(path);
				}
				
			} catch (Exception e){
				new TextWindow("Error opening experiment", e.getMessage(), 500, 500);
			}
		}
			
		
	}


}
