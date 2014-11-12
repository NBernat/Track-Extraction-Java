import ij.ImageStack;

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
	Vector<Collision> activeCollisions;
	/**
	 * Finished collision events  
	 */
	Vector<Collision> finishedCollisions;
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
	 * 
	 */
	public TrackBuilder(ImageStack IS, ExtractionParameters ep){
		pe = new PointExtractor(IS);
		this.ep = ep;
		init(0);
	}
	
	/**
	 * Initialization of objects
	 */
	public void init(int frameNum){
		
		activeTracks = new Vector<Track>();
		finishedTracks  = new Vector<Track>();
		activeCollisions  = new Vector<Collision>();
		finishedCollisions  = new Vector<Collision>();
		
		this.frameNum = frameNum;
		
		//TODO load the stack in here?
//		if (pe==null){
//			pe = new PointExtractor(frameNum);
//		}
		comm = new Communicator();
		
		
		//Build the tracks
		buildTracks();
		//Comb out collisions
		resolveCollisions();
		
	}
	
	////////////////////////////
	// Track Building methods 
	////////////////////////////
	
	/**
	 * Constructs tracks from a series of images
	 */
	public void buildTracks(){
		//Add frames to track objects
		while (pe.nextFrame() <= pe.endFrame) {
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
		
		//set activePoints
		//set matches
		//set pointMatchList
		
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
		modifyMatches();
		
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
		
		matchPtsToTracks();
		matchPtsToCollisions();
		
	}
	
	
	
	/**
	 * Builds a {@link #TrackMatch} for each active track and stores it in matches 
	 * @param startMatchInd The first index of {@link #matches} to fill
	 */
	public void matchPtsToTracks(){
		int i;
		for(i=0; i<activeTracks.size(); i++){
			TrackMatch newMatch = new TrackMatch(activeTracks.get(i), activePts, ep.numPtsInTrackMatch);
			matches.add(newMatch);
			//TODO update match table
		}
	}


	/**
	 * Builds a CollisionMatch, which contains TrackMatches for each relevant track, for each collision and stores it in matches 
	 * @param startMatchInd The first index of {@link #matches} to fill
	 */
	public void matchPtsToCollisions(){
		
		int i;
		for(i=0; i<activeCollisions.size(); i++){
			//TODO matches.add(new CollisionMatch(activeCollisions.get(i), activePts, ep.numPtsInTrackMatch));
			TrackMatch newMatch = new CollisionMatch(activeTracks.get(i), activePts, ep.numPtsInTrackMatch); 
			matches.add(newMatch);
			//TODO update match table
			
		}
	}
	
	
	//TODO modifyMatches: error checking
	public void modifyMatches(){
		
		int numCutByDist = cutMatchesByDistance();
		comm.message("Number of matches cut from frame "+frameNum+" by distance: "+numCutByDist, VerbLevel.verb_debug);
		manageCollisions();
		
	}
	
	
	/**
	 * Marks as invalid any TrackMatch that is too far away 
	 * @return Total number of matches  
	 */
	public int cutMatchesByDistance(){
		 
		ListIterator<TrackMatch> it = matches.listIterator();
		int numRemoved = 0;
		while (it.hasNext()) {
			numRemoved += it.next().cutPointsByDistance(ep.maxMatchDist);
		}
		return numRemoved;
	}
	
	//TODO handleCollisions
		//Release disconnected collisions to ActiveTracks
		//Detect new collisions
		//DON'T repair/Flag bad collisions...that comes later after all the tracks are built
	/**
	 * Maintains the collision structures by adding new collisions and ending finished collisions 
	 */
	public void manageCollisions(){
		 
		int numFinishedCollisions = releaseFinishedCollisions();
		comm.message("Number of collisions ended in frame "+frameNum+": "+numFinishedCollisions, VerbLevel.verb_debug);
		
		//Try to maintain number of track in collision
		int netChange = conserveCollisionNums();
		comm.message("Net change in maggot number within collisions at frame "+frameNum+": "+netChange, VerbLevel.verb_debug);
		
		int numNewColl = detectNewCollisions();
		comm.message("Number of new collisions in frame "+frameNum+": "+numNewColl, VerbLevel.verb_debug);
		
		
	}
	
	 
		//Decide which track goes with which?
		//Convert CollisionPairs to CollisionPoints?
		//if one of the tracks has missing...???
	/**
	 * Finds and releases collision events which have finished
	 * @return number of collisions released
	 */
	public int releaseFinishedCollisions(){
		
		Vector<Collision> finished = detectFinishedCollisions();
		int numFinishedCollisions = finished.size();
		ListIterator<Collision> cIter = finished.listIterator();
		while(cIter.hasNext()){
			releaseCollision(cIter.next());
		}
		
		return numFinishedCollisions;
		
	}
	
	
	/**
	 * Checks the active collisions for any finished events
	 * @return Vector of finished collision objects
	 */
	public Vector<Collision> detectFinishedCollisions(){
		Vector<Collision> finished = new Vector<Collision>();
		
		ListIterator<Collision> cIt = activeCollisions.listIterator();
		while(cIt.hasNext()){
			Collision col = cIt.next();
			if (col.hasFinsihed()){
				finished.add(col);
			}
		}
		
		return finished;
	}
	
	/**
	 * Releases a collision by ending the event, adding the outgoing tracks to activeTracks, and storing the collision event for later processing 
	 * @param col The collision to be released
	 */
	public void releaseCollision(Collision col){
		
		//Tell the collision object that this is where to end it
		col.finishCollision(frameNum-pe.increment);
		//Add the newly started tracks to active tracks
		Vector<Track> newTracks = col.getOutTracks();
		activeTracks.addAll(newTracks);
		//Store the collision event for later processing
		finishedCollisions.add(col);
	}
	
	
	
	public int conserveCollisionNums(){
		
		int netNumChange=0;
		//TODO
		return netNumChange;
		
	}
		
	public int detectNewCollisions(){
		
		int numNewColl=0;
		//TODO 
		//Find double matches in points
		//measure distances to second matches
		return numNewColl;
	}
	
	

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

	
	
	//TODO resolveCollisions	
	public int resolveCollisions(){
		
		
		return 0;
		
	}

}
