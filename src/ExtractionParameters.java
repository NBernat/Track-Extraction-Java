
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
	
	//Maggot size limits minArea,maxArea
	
	
	public ExtractionParameters(){
		
	}
	
}
