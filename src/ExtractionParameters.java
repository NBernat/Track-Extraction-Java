
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
	
	
	
	//TODO
	int startFrame = 0;
	int endFrame = 500;
	//_frame_normalization_methodT fnm = _frame_normalization_methodT._frame_none;
	
	
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
    int resampleInterval = 200;
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
    boolean useGlobalThresh;
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
    double minArea = 10;
    /**
     * Maximum blob area
     */
    double maxArea = 500;
    
    
	public ExtractionParameters(){
		
	}
	
	public boolean properPointSize(double area){
		return (area>=minArea && area<=maxArea);
	}
	
}
