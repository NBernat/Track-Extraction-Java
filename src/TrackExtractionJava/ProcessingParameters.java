package TrackExtractionJava;


public class ProcessingParameters {

	
	
	/**
	 * Min length that the fitter can handle
	 */
	int minTrackLen = 80;//TODO set this when fp is set
	
	
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
	
	
	
	public String[] setLogPath(String outDir, String srcName){
		String[] logPathParts = {outDir, "ProcessingLog.txt"};
		return logPathParts;
	}
	
	public String[] setMagExPath(String outDir, String srcName){
		
		StringBuilder name = new StringBuilder(srcName);
		name.replace(name.lastIndexOf("."), name.length(), "_MTP.bin");
		String[] MagExPathParts = {outDir, name.toString()};
		return MagExPathParts;
	}
	
	public String[] setFitExPath(String outDir, String srcName){

		StringBuilder name = new StringBuilder(srcName);
		name.replace(name.lastIndexOf("."), name.length(), "_BTP.bin");
		String[] FitExPathParts = {outDir, name.toString()};
		return FitExPathParts;
	}
	
}
