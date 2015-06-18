package TrackExtractionJava;


public class ProcessingParameters {

	
	
	/**
	 * Min length that the fitter can handle
	 */
	int minTrackLen = 80;//TODO set this when fp is set
	
	
	
	boolean doFitting = true;
	
	/**
	 * Closes the MMF window
	 */
	boolean closeMMF = true;//TODO set/use this 
	
	/**
	 * Displays an ExperimentFrame after extracting tracks 
	 */
	boolean showMagEx = true;
	/**
	 * Displays an ExperimentFrame after fitting tracks 
	 */
	boolean showFitEx = true;
	
	/**
	 * Whether or not to automatically save the extracted tracks of MTPs
	 */
	boolean saveMagEx = true;
	/**
	 * Whether or not to automatically save the fitted tracks of BTPs
	 */
	boolean saveFitEx = true;
	
	public static String getOutFromInDir(String inDir){
		
		StringBuilder out = new StringBuilder(inDir);
		
		String dataStr = "data";
		String exStr = "extracted";
		int ind = out.lastIndexOf(dataStr);
		out.replace(ind, ind+dataStr.length(), exStr);
				
		return out.toString();
	}
	
	public String[] setLogPath(String outDir, String srcName){
		String[] logPathParts = {outDir, "ProcessingLog.txt"};
		return logPathParts;
	}
	
	public String[] setMagExPath(String outDir, String srcName){
		
		StringBuilder name = new StringBuilder(srcName);
		name.replace(name.lastIndexOf("."), name.length(), "_MTP.jav");
		String[] MagExPathParts = {outDir, name.toString()};
		return MagExPathParts;
	}
	
	public String[] setFitExPath(String outDir, String srcName){

		StringBuilder name = new StringBuilder(srcName);
		name.replace(name.lastIndexOf("."), name.length(), ".jav");
		int mtpInd = name.indexOf("MTP");
		if (mtpInd>=0){
			name.replace(mtpInd, 3, "BTP");
		}
		String[] FitExPathParts = {outDir, name.toString()};
		return FitExPathParts;
	}
	
}
