
public class ExtractionParameters {
	
	/**
	 * Max number of points stored in a TrackMatch object
	 */
	int numPtsInTrackMatch = 3;
	
	/**
	 *  Distance in pixels(?) which indicates a collision
	 */
	double collisionDist;
	
	/**
	 * Max distance in pixels(?) for a point match
	 * <p>
	 * Determined by body length? 
	 */
	double maxMatchDist;
	
	/**
	 * How many frames to move forward when extracting points
	 */
	int increment = 1;
	
	//Maggot size limits: minArea,maxArea
	
	/**
	 * 
	 */
	double[][] analysisRect = {{0,0},{-1,-1}};
	
	
	///////////////////////////
	// Background Parameters
	///////////////////////////
	/**
	 * 
	 */
	int nBackgroundFrames;
	/**
	 * 
	 */
    int resampleInterval;
    /**
     * 
     */
    double blurSigma;
	/**
	 * 
	 */
    double blurAccuracy;//<0.02, lower=better but longer execution
    
    /**
     * Whether or not to globally threshold the image
     */
    boolean useGlobalThresh;
    /**
     * The global threshold value
     */
    double globalThreshValue;
    
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
    double minArea;
    /**
     * Maximum blob area
     */
    double maxArea;
    
    
	public ExtractionParameters(){
		
	}
	
	public boolean properPointSize(double area){
		return (area>=minArea && area<=maxArea);
	}
	
}
