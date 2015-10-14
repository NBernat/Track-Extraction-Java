package TrackExtractionJava;


public class ProcessingParameters {

	
	
	/**
	 * Min length that the fitter can handle
	 */
	int minTrackLen = 500;//TODO set this when fp is set
	
	
	
	boolean doFitting = false;
	
	/**
	 * Closes the MMF window
	 */
	boolean closeMMF = false;//TODO set/use this 
	
	/**
	 * Displays an ExperimentFrame after extracting tracks 
	 */
	boolean showMagEx = true;
	/**
	 * Displays an ExperimentFrame after fitting tracks 
	 */
	boolean showFitEx = false;
	
	/**
	 * Whether or not to automatically save the extracted tracks of MTPs
	 */
	boolean saveMagEx = true;
	/**
	 * Whether or not to automatically save the fitted tracks of BTPs
	 */
	boolean saveFitEx = true;
	
	boolean saveErrors = true;
	
	boolean testMagFromDisk = false;
	boolean testFitFromDisk = false;
	
	boolean sendDataToExtracted = false;
	
	
	public static String getOutFromInDir(String inDir){
		
//		Path p = Paths.get(inDir);
//		
//		StringBuilder out = new StringBuilder(p.getParent().toString());
//		
//		String dataStr = "data";
//		String exStr = "extracted";
//		int ind = out.indexOf(dataStr);
//		out.delete(ind, ind+dataStr.length());
//		out.insert(ind, exStr);
//				
//		return out.toString();
		return inDir;
	}
	
	
	public String[] setLogPath(String srcDir, String srcName){
		String[] logPathParts = {srcDir, "ProcessingLog.txt"};
		return logPathParts;
	}
	
	public String[] setMagExPath(String srcDir, String srcName){
		StringBuilder path = new StringBuilder(srcDir);
		StringBuilder name = new StringBuilder(srcName);
		if (sendDataToExtracted){
			path = new StringBuilder(getOutFromInDir(srcDir));
		}
		name.replace(name.lastIndexOf("."), name.length(), ".prejav");
		String[] MagExPathParts = {path.toString(), name.toString()};
		return MagExPathParts;
	}
	
	public String[] setFitExPath(String srcDir, String srcName){
		//Clean up use of stringbuilder vs string 
		StringBuilder path = new StringBuilder(srcDir);
		StringBuilder name = new StringBuilder(srcName);
		if (sendDataToExtracted){
			path = new StringBuilder(getOutFromInDir(srcDir));
		}
		name.replace(name.lastIndexOf("."), name.length(), ".jav");
//		int mtpInd = name.indexOf("MTP");
//		if (mtpInd>=0){
//			name.replace(mtpInd, 3, "BTP");
//		}
		String[] FitExPathParts = {path.toString(), name.toString()};
		return FitExPathParts;
	}
	
}
