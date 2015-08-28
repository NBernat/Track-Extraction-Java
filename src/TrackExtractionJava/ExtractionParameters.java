package TrackExtractionJava;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.Serializable;
import java.lang.reflect.Field;


public class ExtractionParameters implements Serializable{
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	
	
	
	int GCInterval = 500;

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
	int collisionLevel = 1;
	
	/**
	 *  Distance in pixels which indicates a collision
	 */
	//double collisionDist;
	
	/**
	 * Max distance in pixels for a point match 
	 */
	double maxMatchDist = 20;
	
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
	int showSampleData =0; 
	int sampleInd = 10;
	//TODO
	boolean subset = false;
	int startFrame = 1;
	int endFrame = 500;
	//_frame_normalization_methodT fnm = _frame_normalization_methodT._frame_none;
	//TODO
	int trackWindowHeight = 50;
	int trackWindowWidth = 50;
	int trackZoomFac = 10;
	
	
	int[] matchSpill = {};//{234,251,356,367};
	boolean flagAbnormalMatches = false;
	boolean dispTrackInfo = false;
	
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
    
    boolean excludeEdges = true; 
    boolean clipBoundaries = true;
    int boundarySize = 10; //Size (in pixels) of the boundary that should be clipped when CLIPBOUNDARIES is set to true 
    /**
     * The fraction from which the area of a maggot can deviate from the target area when splitting a point
     * <p>
     * Between 0 and 1, inclusive 
     */
    double fracChangeForSplitting = .5;
    /**
     *  The area change which indicates that a collision has ended, expressed as a fraction of the previous area
     */
    double maxAreaFracForCollisionEnd = .6;
    /**
     * The maximum angle that a contour point can be to be considered for H/T assignment
     */
    double maxContourAngle = Math.PI/2.0;
    /**
     * Number of midline coordinates to extract
     */
    int numMidCoords = 11;
    
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
    double minArea = 35;
    /**
     * Maximum blob area
     */
    double maxArea = 1000;
    //TODO
    int roiPadding = 0;
    
    /**
     * PLAYMOVIE NOT YET SUPPORTED FOR SAVED FILES WHEN TRACKPOINTTYPE=0
     */
    int trackPointType = 2;
    
    /**
	 * Creates a set of Extraction Parameters, with the proper start frame
	 */
	public ExtractionParameters(){
		if (!subset){
			startFrame = 1;
		}
	}
    
	public boolean properPointSize(double area){
		return (area>=minArea && area<=maxArea);
	}
	
	public boolean toDisk(String outputName){
		
		
		try{
			
			File f = new File(outputName);
			
			FileWriter fw = new FileWriter(f.getAbsoluteFile());
			BufferedWriter bw = new BufferedWriter(fw);
			System.out.println("Writing params to disk...");
			Field[] flds = ExtractionParameters.class.getDeclaredFields();//getFields();
			System.out.println(flds.length+" fields");
			for (Field fld: flds){
				bw.write(fld.getName()+":"+fld.get(this)+"\n");
			}
			
			bw.close();
			System.out.println("...finished writing params to disk");
			
		} catch (Exception e){
			System.out.println("Error saving to disk"+e.getMessage());
		}
		
		return true;
	}
	
	
	public static void main(String[] args) {
		
		ExtractionParameters ep = new ExtractionParameters();
		System.out.println("Saving params to disk...");
		ep.toDisk("C:\\Users\\Natalie\\Documents\\test.txt");
		System.out.println("...done saving params to disk");
	}
	
}
