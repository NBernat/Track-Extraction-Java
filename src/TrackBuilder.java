import ij.IJ;
import ij.ImageStack;

import java.util.Arrays;
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
	/*
	 * Cross-reference table for point matches, showing how many primary matches contain this point
	 * <p>
	 * each row contains the pointID, and the number of TrackMatches with that point (in that order)
	 */
	//int[][] pointMatchTable;
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
	 * Constructs a TrackBuilder object
	 */
	public TrackBuilder(ImageStack IS, ExtractionParameters ep){
		
		this.ep = ep;
		init(ep.startFrame, IS);
	}
	
	/**
	 * Initialization of objects
	 */
	public void init(int frameNum, ImageStack IS){
		
		//Set Auxillary objects
		comm = new Communicator();
		pe = new PointExtractor(IS, comm, ep);
		
		//Set track-building objects
		activeTracks = new Vector<Track>();
		finishedTracks  = new Vector<Track>();
		activeCollisions  = new Vector<Collision>();
		finishedCollisions  = new Vector<Collision>();
		
		//Set status parameters
		this.frameNum = frameNum;
		
		//TODO Put this here?
		//Build the tracks 
		//buildTracks();
		
		
		
	}
	
	////////////////////////////
	// Track Building methods 
	////////////////////////////
	
	/**
	 * Constructs tracks from a series of images
	 */
	public void buildTracks(){
		
		//Add frames to track objects
		while (pe.nextFrameNum() <= pe.endFrameNum && pe.nextFrameNum() <= ep.endFrame) {
			frameNum = pe.nextFrameNum();
			if (frameNum%20 == 0){
				IJ.showStatus("Building : Adding Frame "+frameNum+"...");
			}
			if (addFrame(frameNum)>0) {
				comm.message("Error adding frame "+pe.nextFrameNum(), VerbLevel.verb_error);
				return;
			}
			
		}
		
		//Comb out collisions
		//TODO resolveCollisions();
		
		//Move all active tracks to finished
		finishedTracks.addAll(activeTracks);
		activeTracks.removeAll(activeTracks);
		finishedCollisions.addAll(activeCollisions);
		activeCollisions.removeAll(finishedCollisions);
	}
	
	
	/**
	 * Adds the points from the specified image frame to the track structures
	 * @param frameNum Index of the frame to be added
	 * @return status: 0 means all is well, >0 means there's an error
	 */
	private int addFrame(int frameNum) {
				
		if (loadPoints(frameNum)>0) {
			comm.message("Error loading points in frame "+frameNum, VerbLevel.verb_error);
			return 1;
		}
				
		if (updateTracks()>0) {
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
	private int loadPoints(int frameNum){
		
		if(pe.extractFramePoints(frameNum)>0){
			comm.message("Error extracting points from frame "+frameNum, VerbLevel.verb_error);
			return 1;
		}
		activePts = pe.getPoints();
		if (activePts.size()==0){
			comm.message("No points were extracted from frame "+frameNum, VerbLevel.verb_warning);
		}

		return 0;
	}
	
	
	
	/**
	 * Extend the tracks to include points extracted from the current frame 
	 * @return status: 0 means all is well, >0 means there's an error
	 */
	private int updateTracks(){
		
		//Build matches
		makeMatches();
		comm.message("Frame "+frameNum+" initially has "+matches.size()+" matches", VerbLevel.verb_debug);
		if (matches.size()==0){
			comm.message("No matches were made in frame "+frameNum, VerbLevel.verb_warning);
		}
		
		//Modify matches
		modifyMatches();
		
		//Fuse matches to tracks
		int acTrNum = extendOrEndTracks(); 
		if (acTrNum<0){
			comm.message("Error attaching new points to tracks", VerbLevel.verb_error);
			return 1;
		}
		comm.message("Number of active tracks: "+acTrNum, VerbLevel.verb_debug);
		
		//Start new tracks from remaining activePts
		int numNew = startNewTracks();
		comm.message("New Tracks: "+numNew, VerbLevel.verb_debug);
		
		return 0;
	}
	
	
	/**
	 * Matches the newly added points to the tracks 
	 */
	private void makeMatches(){
		
		matches = new Vector<TrackMatch>();

		//Match points to Tracks
		for(int i=0; i<activeTracks.size(); i++){
			TrackMatch newMatch = new TrackMatch(activeTracks.get(i), activePts, ep.numPtsInTrackMatch, this);
			matches.add(newMatch);
			//addMatchToPointTable(newMatch);
		}
		//Match points to Collisions
//		for(int j=0; j<activeCollisions.size(); j++){
//			//TODO matches.add(new CollisionMatch(activeCollisions.get(i), activePts, ep.numPtsInTrackMatch));
//			TrackMatch newMatch = new CollisionMatch(activeTracks.get(j), activePts, ep.numPtsInTrackMatch); 
//			matches.add(newMatch);
//			//addMatchToPointTable(newMatch);
//			
//		}
		
	}
	

	
	/**
	 * Corrects initial matching errors
	 */
	private void modifyMatches(){
		
		int numCutByDist = cutMatchesByDistance();
		comm.message("Number of matches cut from frame "+frameNum+" by distance: "+numCutByDist, VerbLevel.verb_debug);
		
		manageCollisions();
		
	}
	
	
	/**
	 * Marks as invalid any TrackMatch that is too far away 
	 * @return Total number of matches  
	 */
	private int cutMatchesByDistance(){
		 
		ListIterator<TrackMatch> it = matches.listIterator();
		int numRemoved = 0;
		while (it.hasNext()) {
			numRemoved += it.next().cutPointsByDistance(ep.maxMatchDist);
		}
		return numRemoved;
	}
	
	
	/**
	 * Maintains the collision structures by adding new collisions, trying basic methods of fixing current colllions, and ending finished collisions 
	 */
	private void manageCollisions(){
		 
		//End Collision tracks, reduce point's number to 0
		if (ep.collisionLevel==0){
			int numEnded = endNewCollisions();
			comm.message("Tracks ended due to collision: "+numEnded, VerbLevel.verb_debug);
		} else {
			//if a point is assigned to more than one track, start a collision event
			int numNewColl = detectNewCollisions();
			comm.message("Number of new collisions in frame "+frameNum+": "+numNewColl, VerbLevel.verb_debug);
			
			//Try to maintain number of incoming tracks in each collision by grabbing nearby tracks and splitting points 
			conserveCollisionNums();
			
			//if number incoming = number outgoing, finish
			int numFinishedCollisions = releaseFinishedCollisions();
			comm.message("Number of collisions ended in frame "+frameNum+": "+numFinishedCollisions, VerbLevel.verb_debug);
			
			//Add new collisions to activeTracks, since the have been dealt with
		}
		
	}
	
	
	//TODO javadoc
	private int endNewCollisions(){
		
		int numEnded = 0;
		ListIterator<TrackMatch> tmIt = matches.listIterator();
		
		while (tmIt.hasNext()){
			
			int i = tmIt.nextIndex();
			TrackMatch match = tmIt.next();
			comm.message("Checking Track "+match.track.trackID+" for collisions..", VerbLevel.verb_debug);
			
			if (match.checkTopMatchForCollision()>0) {
				comm.message("Collision found, "+match.getTopMatchPoint().getNumMatches()+" tracks matched to point", VerbLevel.verb_debug);
				//Collect the info from the tracks in collisions
				Vector<Track> colTracks = new Vector<Track>();
				Vector<TrackMatch> colMatches= new Vector<TrackMatch>();
				
				colMatches.add(match);
				colTracks.add(match.track);
				
				//Find all the additional tracks in the collision
//				int numColliding = match.getTopMatchPoint().getNumMatches();
//				comm.message("Collision has "+numColliding+" tracks", VerbLevel.verb_debug);
//				int startInd = i+1;
//				for (int j=0; j<(numColliding-1); j++) {
//					comm.message("Finding collision track number "+(j+2), VerbLevel.verb_debug);
//					//Find the first TrackMatch in matches that's in the collision
//					int colInd = match.findCollidingTrackMatch(matches, startInd);
//					if (colInd>=0) {
//						comm.message("Matching track found!", VerbLevel.verb_debug);
//						colMatches.add(matches.get(colInd));
//						colTracks.add(matches.get(colInd).track);
//						//If there are more than 2 tracks in the collision, start looking for collision matches at the index following the one that was just found
//						startInd = colInd+1;
//					} else {
//						comm.message("matching collision not found", VerbLevel.verb_debug); 
//					}
//				}
				
				//End the colliding tracks
				ListIterator<TrackMatch> cmIt = colMatches.listIterator();
				while (cmIt.hasNext()) {
					comm.message("Clearing collsions", VerbLevel.verb_debug);
					numEnded++;
					TrackMatch endMatch = cmIt.next();
					endMatch.clearAllMatches();
					
				}
				if (numEnded>0) {
					comm.message("Done clearing collisions", VerbLevel.verb_debug);
				}
				
			}	
		}
		comm.message("Ended "+numEnded+" Collision tracks", VerbLevel.verb_debug);
		return numEnded;
	}
	
	
	//TODO
	private int detectNewCollisions(){
		
		Vector<Collision> newCollisions = new Vector<Collision>();
		//Used to avoid double-checking
		boolean[] matchInNewCollision = new boolean[matches.size()];

		//Check each match for a collision
		//TODO switch to iterator 
		for (int i=0;i<matches.size();i++) {
			
			if (!matchInNewCollision[i] && matches.get(i).checkTopMatchForCollision()>0){

				//Collect the info from the tracks in collisions
				Vector<Track> colTracks = new Vector<Track>();
				Vector<TrackMatch> colMatches= new Vector<TrackMatch>();
				
				matchInNewCollision[i] = true;
				colMatches.add(matches.get(i));
				colTracks.add(matches.get(i).track);

				//Find the colliding track(s)
				int numColliding = matches.get(i).getTopMatchPoint().getNumMatches();
				int startInd = i+1;
				for (int j=0; j<(numColliding-1); j++) {
					
					//Find the first TrackMatch in matches that's in the collision
					int colInd = matches.get(i).findCollidingTrackMatch(matches, startInd);
					if (colInd>=0) {
						matchInNewCollision[colInd] = true;
						colMatches.add(matches.get(colInd));
						colTracks.add(matches.get(colInd).track);
						
						//If there are more than 2 tracks in the collision, start looking for collision matches at the index following the one that was just found
						startInd = colInd+1;
					}
				}
				
				//Create a new collision object
				Collision newCol = new Collision(colTracks, matches.get(i).getTopMatchPoint(), frameNum);
				
				//Try to fix the collision; if unsuccessful, end the tracks and start a new collision
				int fixResult = newCol.fixCollision(colMatches); 
				if (fixResult>0) {
					//If the collision is fixed, then take the collisionPoint's and  
					if (fixResult==2){
						//TODO fix the activePts structure: remove bad point, add new points
					}
					//TODO Then the matches have been modified, and you're done! 
					
				} else {
					//TODO Modify the collision? It's already been created, so I think it should be done for this frame iteration
					activeCollisions.addElement(newCol);
					newCollisions.add(newCol);
				}
				//WHAT TO DO WITH MATCHES? CONVERT TO COLLISIONMATCH?
			}

		}
		return 0;
	}
	
//	private Vector<TrackPoint> findEmptyPoints() {
//		Vector<TrackPoint> emptyPoints = new Vector<TrackPoint>();
//		ListIterator<TrackPoint> tpIt = activePts.listIterator();
//		while (tpIt.hasNext()) {
//			TrackPoint pt = tpIt.next(); 
//			if (pt.getNumMatches()==0) {
//				emptyPoints.add(pt);
//			}
//		}
//		return emptyPoints;
//	}
	
	
	private void conserveCollisionNums(){
		
		
		//TODO
		
		matchCollisionsToEmptyTracks();
		
		splitCollisionPoints();
		
		
	}
	
	//TODO 
	private void matchCollisionsToEmptyTracks(){
		
		//Check each trackMatch to see if it has any validmatches 
		
	}

	//TODO
	private void splitCollisionPoints(){
		
	}
	

	
	/**
	 * Finds and releases collision events which have finished
	 * @return number of collisions released
	 */
	private int releaseFinishedCollisions(){
		
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
	private Vector<Collision> detectFinishedCollisions(){
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
	private void releaseCollision(Collision col){
		
		//Tell the collision object that this is where to end it
		col.finishCollision(frameNum-pe.increment);
		//Add the newly started tracks to active tracks
		Vector<Track> newTracks = col.getOutTracks();
		activeTracks.addAll(newTracks);
		//Store the collision event for later processing
		finishedCollisions.add(col);
	}


	

	//TODO extendOrEndTracks
		//Extend 1-1 matches and good collision matches
		//End dead tracks (unmatched tracks), move to finished
		//Update track number
	private int extendOrEndTracks(){
		
		ListIterator<TrackMatch> mIt = matches.listIterator();
		while (mIt.hasNext()) {
			TrackMatch match = mIt.next();
			
			if (match.getTopMatchPoint()==null) {
				finishedTracks.addElement(match.track);
				activeTracks.remove(match.track);
				
			} else {
				match.track.extendTrack(match.getTopMatchPoint());
				activePts.remove(match.getTopMatchPoint());
			}
		}
		
		numTracks = activeTracks.size() + activeCollisions.size();
		
		return numTracks;
	}
	
	//TODO
	private int startNewTracks(){
		
		int numNew=0;
		ListIterator<TrackPoint> tpIt = activePts.listIterator();
		while (tpIt.hasNext()) {
			TrackPoint pt = tpIt.next();
			if (pt.getNumMatches()==0) {
				activeTracks.addElement(new Track(pt));
				numNew++;
			} else {
				comm.message("TrackPoint "+pt.pointID+" has TrackMatches, but remained active after matches were added to tracks", VerbLevel.verb_warning);
			}
		}
		
		return numNew;
	}
	
	
	
	//TODO resolveCollisions	
//	private int resolveCollisions(){
//		
//		
//		return 0;
//		
//	}

}
