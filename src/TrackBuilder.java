import java.util.ListIterator;
import java.util.Vector;


public class TrackBuilder {

		
	////////////////////////////
	// Ongoing track objects 
	////////////////////////////
	/**
	 * Tracks which have not yet been ended
	 */
	Vector<Track> activeTracks;
	/**
	 * Tracks which have been ended
	 */
	Vector<Track> finishedTracks;
	/**
	 * All (active? TBD) collision events  
	 */
	Vector<Collision> collisions;
	/**
	 * Number of tracks, including active tracks and tracks in collisions 
	 */
	int numTracks;
	
	////////////////////////////
	// Frame-by-frame objects
	////////////////////////////
	/**
	 * TrackPoints which have not yet been added to tracks
	 * <p>
	 * Set when the points are activated
	 */
	Vector<TrackPoint> activePts;
	/**
	 * Matches between tracks and their (nearest/nearby? TBD) points
	 * <p>
	 * Set when the tracks are activated
	 */
	Vector<TrackMatch> matches;
	/**
	 * Cross-reference table for point matches, showing how many primary matches contain this point
	 */
	int[] pointMatchTable;
	/**
	 * Index of frame being processed
	 */
	int frameNum;
	
	
	////////////////////////////
	// Auxiliary  objects
	////////////////////////////
	/**
	 * Parameters used for extracting tracks
	 */
	ExtractionParameters ep;
	/**
	* An object which provides points from an image 
	* <p>
	* Because of the point extractor, the TrackBuilder doesn't interact directly with the stack of images 
	*/
	PointExtractor pe;
	/**
	 * Object for displaying messages
	 */
	Communicator comm;
	
	
	////////////////////////////
	// Driver and Constructors
	////////////////////////////

	/**
	 * Default constructor
	 */
	public TrackBuilder(){
		//TODO Choose file
		init(0);
	}
	
	/**
	 * Another constructor
	 */
	public TrackBuilder(int startFrame){
		//TODO choose frame
		init(startFrame);
	}
	
	/**
	 * Initialization of objects
	 */
	public void init(int frameNum){
		
		//set activeTracks
		//set finishedTracks
		//set collisions
		
		// ?? Maybe set these at the start of each frame...
		//set activePoints
		//set matches
		//set pointMatchList
		this.frameNum = frameNum;
		
		//TODO load the stack in here
		pe = new PointExtractor();
		comm = new Communicator();
		
		
		//Build the tracks
		buildTracks();
		
		//Resolve collisions?
		
	}
	
	////////////////////////////
	// Track Building methods 
	////////////////////////////
	
	/**
	 * Constructs tracks from a series of images
	 */
	public void buildTracks(){
		//Add frames to track objects
		while (pe.nextFrame() <= pe.endFrameNum) {
			frameNum = pe.nextFrame();
			if (addFrame(frameNum)>0) {
				comm.message("Error adding frame "+pe.nextFrame(), VerbLevel.verb_error);
				return;
			}
			
		}
	}
	
	
	/**
	 * Adds the points from the specified image frame to the track structures
	 * @param frameNum Index of the frame to be added
	 * @return status: 0 means all is well, >0 means there's an error
	 */
	public int addFrame(int frameNum) {
		
		if (loadPoints(frameNum)>0) {
			comm.message("Error loading points in frame "+frameNum, VerbLevel.verb_error);
			return 1;
		}
		
		
		if (extendTracks()>0) {
			comm.message("Error extending tracks in frame "+frameNum, VerbLevel.verb_error);
			return 1;
		}
		
		return 0;
	}

	
	/**
	 * Loads points from the specified frame into the activePts object
	 * @param frameNum Index of the frame to load
	 * @return status: 0 means all is well, >0 means there's an error
	 */
	public int loadPoints(int frameNum){
		
		if(pe.extractFrame(frameNum)>0){
			comm.message("Error extracting points from frame "+frameNum, VerbLevel.verb_error);
			return 1;
		}
		activePts = pe.getPoints();
		if (activePts.size()==0){
			comm.message("No points were extracted from frame "+frameNum, VerbLevel.verb_warning);
		}
		pointMatchTable = new int[activePts.size()];
		return 0;
	}
	
	//For each point:
		//Add point to activePts 
		//Count point
	//Set lookup table length = number of points
//	public int activatePoints(TrackPoint[] points) {
//		activePts = points;
//		
//		return 0;
//	}
	
	
	
	
	/**
	 * Extend the tracks to include points extracted from the current frame
	 * @return status: 0 means all is well, >0 means there's an error
	 */
	public int extendTracks(){
		
		//Build matches
		buildMatches();
		if (matches.size()==0){
			comm.message("No matches were made in frame "+frameNum, VerbLevel.verb_warning);
			return 1;
		}
		
		//Modify matches
		if (modifyMatches()>0){
			comm.message("Error modifying matches", VerbLevel.verb_error);
			return 1;
		}
		
		//Fuse matches to tracks
		if (fuseMatches()>0){
			comm.message("Error attaching new points to tracks", VerbLevel.verb_error);
			return 1;
		}
		
		return 0;
	}
	
	
	/**
	 * Matches the newly added points to the tracks 
	 */
	public void buildMatches(){
		
		matches = new Vector<TrackMatch>();
		
		matchPtsToActiveTracks();
		matchPtsToCollisions();
		
	}
	
	
	
	/**
	 * Builds a {@link TrackMatch} for each active track and stores it in matches 
	 * @param startMatchInd The first index of {@link TrackBuilder.matches} to fill
	 */
	public void matchPtsToActiveTracks(){
		int i;
		for(i=0; i<activeTracks.size(); i++){
			matches.add(new TrackMatch(activeTracks.get(i), activePts, ep.numPtsInTrackMatch));
			//TODO update match table
		}
	}


	/**
	 * Builds a CollisionMatch, which contains TrackMatches for each relevant track, for each collision and stores it in matches 
	 * @param startMatchInd The first index of {@link TrackBuilder.matches} to fill
	 */
	public void matchPtsToCollisions(){
		
		int i;
		for(i=0; i<collisions.size(); i++){
			matches.add(new CollisionMatch(activeTracks.get(i), activePts, ep.numPtsInTrackMatch));
			//TODO update match table
		}
	}
	
	
	//TODO modifyMatches: error checking
	public int modifyMatches(){
		
		int numCutByDist = cutMatchesByDistance();
		comm.message("Number of matches cut from frame "+frameNum+" by distance: "+numCutByDist, VerbLevel.verb_debug);
		
		handleCollisions();
		
		return 0;
	}
	
	
	/**
	 * Marks as invalid any TrackMatch that is too far away 
	 * @return Total number of matches  
	 */
	public int cutMatchesByDistance(){
		 
		ListIterator<TrackMatch> it = matches.listIterator();
		int numRemoved = 0;
		while (it.hasNext()) {
			numRemoved += it.next().cutPointsByDistance(ep.collisionDist);
		}
		return numRemoved;
	}
	
	//TODO handleCollisions
		//Detect new collisions
		//Release disconnected collisions to ActiveTracks
		//Repair/Flag bad collisions
	public int handleCollisions(){
		
		detectNewCollisions();
		
		return 0;
	}
	
	//TODO detectNewCollisions
		//Find double matches in points
		//measure distances
	public int detectNewCollisions(){
		
		
		
		return 0;
	}
	
	//TODO releaseCollisions
		//Decide which track goes with which
		//Convert CollisionPairs to CollisionPoints
		//if one of the tracks has missing 
	
	//TODO repairBadCollisions
		//Detect bad collisions (no 2-2 matches) 
		//Try to match to empty points nearby
		//Try to split the one image to two
		//Mark bad collisions (to widen range of points nearby)
	
	//TODO detectBadCollisions
	
	//TODO matchCollisionWithEmptyPoint
	
	//TODO splitImagetoMultiplePoints
	
	
	//TODO fuseTracks
		//Extend 1-1 matches and good collision matches
		//Start new tracks (unmatched points), move to active
		//End dead tracks (unmatched tracks), move to finished
		//Update track number
	public int fuseMatches(){
		
		return 0;
	}
	
	
	//TODO fuseTrackMatches
	
	//TODO startNewTracks
	
	//TODO endDeadTracks

	

}
