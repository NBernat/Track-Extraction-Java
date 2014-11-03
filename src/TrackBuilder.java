import java.util.ArrayList;


public class TrackBuilder {
	
	////////////////////////////
	// Ongoing track objects 
	////////////////////////////
	/**
	 * Tracks which have not yet been ended
	 */
	ArrayList<Track> activeTracks;
	/**
	 * Tracks which have been ended
	 */
	ArrayList<Track> finishedTracks;
	/**
	 * All (active? TBD) collision events  
	 */
	ArrayList<Collision> collisions;
	
	/**
	 * An object which provides points from an image 
	 * <p>
	 * Because of the point extractor, the TrackBuilder doesn't interact directly with the stack of images 
	 */
	PointExtractor pe;
	
	////////////////////////////
	// Frame-by-frame objects
	////////////////////////////
	/**
	 * TrackPoints which have not yet been added to tracks
	 */
	TrackPoint[] activePts;
	/**
	 * Matches between tracks and their (nearest/nearby? TBD) points
	 */
	ArrayList<TrackMatch> matches;
	/**
	 * Cross-referencing list for {@link TrackBuilder.matches}
	 */
	ArrayList<ArrayList<Integer>> pointMatchList;
	/**
	 * Index of frame being processed
	 */
	int frameNum;
	
	
	//TODO constructors
	/**
	 * Default constructor
	 */
	public TrackBuilder(){
		
	}
	
	/**
	 * Another constructor
	 */
	public TrackBuilder(int startFrame){
		
	}
	
	/**
	 * Constructs tracks from a series of images
	 */
	public void buildTracks(){
		//Add frames to track objects
		while (pe.nextFrame() <= pe.endFrameNum) {
			int result = addFrame(pe.nextFrame());
			if (result>0) {
					//TODO switch to messagehandler
				System.out.println("Error adding frame "+pe.nextFrame());
				return;
			}
			
		}
	}
	

	/**
	 * Adds the points from the specified image frame to the track structures
	 * @param frameNum Index of the frame to be added
	 * @return status, 0 means all is well, >0 means there's an error
	 */
	public int addFrame(int frameNum) {
		
		if (loadPoints(frameNum)>0) {
			//TODO switch to messagehandler
			System.out.println("Error loading points in frame "+frameNum);
			return 1;
		}
		
		
		if (extendTracks()>0) {
			//TODO switch to messagehandler
			System.out.println("Error extending tracks in frame "+frameNum);
			return 1;
		}
		
		return 0;
	}

		

	//Load points: LOADPOINTS(BELOW)
		//Extract Points POINTEXTRACTOR
		//Activate Points ACTIVATEPOINTS(BELOW)
	//Extend Tracks: EXTENDTRACKS(BELOW)
		//Activate Tracks: (maintain Lookup table)
			//Build Matches TRACKMATCHER (option to cut off by length?)
			//Modify Matches (length, collision repair)
		//Fuse Tracks
			//Extend 1-1 matches and collisions
			//Start new tracks (unmatched points), move to active
			//End dead tracks (unmatched tracks), move to finished
	
	
	
	//TODO loadPoints
		//rawPts<-- Extract Points POINTEXTRACTOR
		//activePts<--[rawPts,tracks] Activate Points ACTIVATEPOINTS(BELOW)
	public int loadPoints(int frameNum){
		//extract points
		//TODO error check?
		pe.extractFrame(frameNum);
		//activate points
		if (activatePoints(pe.getPoints())>0) {
			//TODO switch to messagehandler
			System.out.println("Error activating points in frame "+frameNum);
			return 1;
		}
		
		return 0;
	}
	
	//TODO activatePoints
	//For each point:
		//Add point to activePts 
		//Count point
		//xxxxx DON'T Match to contending tracks ->trackMatch TRACKMATCH
	//Set lookup table length = number of points
	public int activatePoints(TrackPoint[] points) {
		activePts = points;
		//point match cross reference is 
		//pointMatchList = [points.length];
		
		return 0;
	}
	
	
	
	
	//TODO extendTracks
		//Activate Tracks
		//Fuse Tracks
	public int extendTracks(){
		
		return 0;
	}
	
	
	
	//TODO activateTracks
		//matches<--Build Matches TRACKMATCH (option to cut off by length?; maintain table)
		//matches<--Modify Matches (length, collision repair; maintain table)
	
	//TODO buildMatches
		//For each track,
			//construct a TRACKMATCH (activetracks, collisiontracks)
	
	//TODO modifyMatches
		//Remove matches that are too far CUTMATCHESBYDISTANCE
		//Handle collisions
	
	//TODO cutMatchesByDistance
		//If trackmatch.dist is too great, deleete it
	
	//TODO handleCollisions
		//Detect new collisions
		//Release disconnected collisions to ActiveTracks
		//Repair/Flag bad collisions
	
	//TODO detectNewCollisions
		//Find double matches
		//measure distances
	
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
	
	
	//TODO fuseTrackMatches
	
	//TODO startNewTracks
	
	//TODO endDeadTracks

	

}
