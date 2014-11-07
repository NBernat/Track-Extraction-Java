
public class TrackPoint {

	/**
	 *Unique id for the track
	 */
	int trackID;
	/**
	 * X location of the point
	 */
	double x;
	/**
	 * y location of the point
	 */
	double y;
	/**
	 * Index of the frame containing this point 
	 */
	double frameNum;
	/**
	 * Area of the image
	 */
	//double area
	/**
	 * Covariance matrix for the image
	 */
	double[] covariance; 
	
	/**
	 * Used to generate unique IDs for the trackpoints
	 * <p> Incremented each time a new track is made
	 */
	static int lastIDNum;
	
	//TODO Constructors
	
	/**
	 * Helper method for constructors 
	 */
	public void init(double x, double y, double area, double area, double[] cov, int frame, int trackID){
		
	}
	
	//gets&sets (area, cov)
	//Constructors
	//Distance methods
	//VertexAngleMethod?
}
