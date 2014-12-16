
public class ExtractionParameters {
	
	/**
	 * Max number of points stored in a TrackMatch object
	 */
	int numPtsInTrackMatch = 3;
	
	/**
	 * Determines how much collision handling should be done
	 * <p>
	 * 0: When tracks collide, just end them
	 * <p>
	 * 1: Match collisions to nearby points, then try to split image into multiple points
	 * <p>
	 * 2: Level 2, then comb through collisions after tracks are made 
	 */
	int collisionLevel = 0;
	
	/**
	 *  Distance in pixels which indicates a collision
	 */
	//double collisionDist;
	
	/**
	 * Max distance in pixels for a point match 
	 */
	double maxMatchDist = 7;
	
	/**
	 * How many frames to move forward when extracting points
	 */
	int increment = 1;
	
	
	///////////////////////////
	// Sample data display
	///////////////////////////
	
	/**
	 * 0=nothing
	 * 1 = play track specified by sampleInd
	 * 2 = 1, plus show ResultsTable & thresholded image of frame specified by sampleInd
	 */
	int showSampleData = 1; 
	int sampleInd = 10;
	//TODO
	int startFrame = 200;
	int endFrame = 400;
	//_frame_normalization_methodT fnm = _frame_normalization_methodT._frame_none;
	//TODO
	int trackWindowHeight = 250;
	int trackWindowWidth = 250;
	int trackZoomFac = 10;
	
	
	int[] matchSpill = {307,383,459};
	boolean dispTrackInfo = true;
	
	///////////////////////////
	// Background Parameters
	///////////////////////////
	/**
	 * 
	 */
	int nBackgroundFrames = 5;
	/**
	 * 
	 */
    int resampleInterval = 100;
    /**
     * 
     */
    double blurSigma = 1;
	/**
	 * 
	 */
    double blurAccuracy = 0.02;//<0.02, lower=better but longer execution
    
    /**
     * Whether or not to globally threshold the image
     */
    boolean useGlobalThresh = true;
    /**
     * The global threshold value
     */
    double globalThreshValue = 20;
    
    /**
     * Add-on for the string which specifies the center coordinates of the points
     * <p>
     * Center of Mass=>"M"
     * <p>
     * Centroid=>""
     */
    String centerMethod = "";
    
    /**
     * Minimum blob area
     */
    double minArea = 50;
    /**
     * Maximum blob area
     */
    double maxArea = 1000;
    //TODO
    int roiPadding = 5;
    
    
	public ExtractionParameters(){
		
	}
	
	public boolean properPointSize(double area){
		return (area>=minArea && area<=maxArea);
	}
	
}
