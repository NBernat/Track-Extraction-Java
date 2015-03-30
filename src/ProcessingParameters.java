
public class ProcessingParameters {

	
	
	/**
	 * Min length that the fitter can handle
	 */
	int minTrackLen = 80;//TODO set this when fp is set
	
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
	boolean saveMagEx = false;
	/**
	 * Whether or not to automatically save the fitted tracks of BTPs
	 */
	boolean saveFitEx = false;
	
	
	
	public String[] setLogPath(String srcDir, String srcName){
		String[] logPathParts = {srcDir, "ProcessingLog.txt"};
		
		return logPathParts;
	}
	
	public String[] setMagExPath(String srcDir, String srcName){
		String[] MagExPathParts = {};
		
		return MagExPathParts;
	}
	
	public String[] setFitExPath(String srcDir, String srcName){
		String[] FitExPathParts = {};
		
		return FitExPathParts;
	}
	
}
