import java.util.Collections;
import java.util.ListIterator;
import java.util.Vector;

/**
 * An object that stores all the information about a collision event
 * @author Natalie
 *
 */
public class Collision {
	
	/**
	 * The tracks leading up to the collision
	 */
	Vector<Track> inTracks;	
	/**
	 * The tracks attached to the end of the collision
	 */
	Vector<Track> outTracks;
	/**
	 * The track consisting of maggots stuck in a collision
	 */
	Track collTrack; 
	/**
	 * First frame where the maggots collide
	 */
	int startFrame;
	/**
	 * Current frame, used to build collisions 
	 */
	int currentFrame;
	/**
	 * LastFrame with colliding maggots
	 */
	int endFrame;
	/**
	 * Any matches to the track/tracks involved in the collision
	 */
	Vector<TrackMatch> matches;

	
	/**
	 * Constructs a Collision object from the tracks in the given trackmatches
	 * @param initMatches The matches that collide to a single point
	 * @param frameNum The frame at which they collide
	 */
	public Collision(Vector<TrackMatch> initMatches, int frameNum){
		
		matches.addAll(initMatches);
		
		inTracks = new Vector<Track>();
		for (int i=0; i<initMatches.size(); i++){
			inTracks.add(initMatches.get(i).track);
		}
		outTracks = new Vector<Track>();
		startFrame = frameNum;
		currentFrame = frameNum;
		endFrame=-1;
	}
	
	/**
	 * Checks if the collision has been ended 
	 * @return Whether or not the collision track has ended 	
	 */
	public boolean hasFinsihed(){
		
		if (outTracks.size()>0){
			return true;
		}
		
		return false;
	}
	
	/**
	 * Accessor for the tracks leaving the collisions 
	 * @return The outgoing tracks from this collision
	 */
	public Vector<Track> getOutTracks(){
		return outTracks;
	}
	

	/**
	 * Starts the collision's track and adjusts the matches
	 */
	public void startCollision(){
		
		//Make a new track and match
		TrackMatch match = matches.firstElement();
		collTrack = new Track(match.TB);
		TrackMatch newMatch = new TrackMatch(collTrack, match);
		ListIterator<TrackMatch> mIt = matches.listIterator();
		
		//Replace the old matches with new one
		while (mIt.hasNext()) {
			mIt.next().clearAllMatches();
		}
		matches.removeAllElements();
		matches.add(newMatch);
		
	}
	
	//TODO
	public void endCollision() {
		
		ListIterator<TrackMatch> mIt = matches.listIterator();
		while (mIt.hasNext()){
			outTracks.add(mIt.next().track);
			
		}
		
		endFrame = outTracks.firstElement().points.firstElement().frameNum;
		
	}
	
	/**
	 * Try to fix the collision 
	 * @return Status int: 0 = unfixed; 1 = fixed by matching to nearby points; 2 = fixed by splitting the points apart
	 */
	public int fixCollision() {		
		
		if (matchToEmptyPts()){

			return 1;
		}
		if (matchToSplitPts()) {

			return 2;
		}
		
		
		return 0;
	}
	
	
	/**
	 * Finds unmatched points near the collision and uses them to end/avoid a collision  
	 * <p>
	 * Empty points are found from point matches; the number of points in a match/ distance from original points are set in Extraction Parameters
	 * @return Whether or not empty points were used to fix the collision 
	 */
	public boolean matchToEmptyPts() {
		
		//Set up the data needed to adjust the matches
		Vector<TrackMatch> otherMatches = new Vector<TrackMatch>();//for locating the match to modify
		Vector<Integer> otherMatchInds = new Vector<Integer>(); //for locating the point within the match 
		Vector<Double> otherPointDists = new Vector<Double>();//for finding best new match
		
		//Find the available empty points and store info about them
		ListIterator<TrackMatch> mIt = matches.listIterator();
		while (mIt.hasNext()) {
			TrackMatch match = mIt.next();
			int[] betterInds = match.indsOfValidNonPrimaryEmptyMatches();
			if (betterInds.length>0) {
				for (int i=0; i<betterInds.length; i++){
					otherMatches.add(match);
					otherMatchInds.add(betterInds[i]);
					otherPointDists.add(match.dist2MatchPts[betterInds[i]]);
				}
			}
		}

		
		int numDesired = inTracks.size();
		int numCurrent = matches.size();
		boolean collisionIsEnding = (numCurrent!=numDesired);
		
		
		if (otherMatches.size()>0) { //Empty points were found, edit the matches to avoid/end the collision
		
			//Get the best secondary match
			Object minDist = Collections.min(otherPointDists);
			int ind = otherPointDists.indexOf(minDist);
			TrackMatch match2Change = otherMatches.get(ind);
			
			
			Vector<TrackMatch> newMatches = new Vector<TrackMatch>(); 
			
			if (collisionIsEnding){
				//Make new tracks; stick the current top match and the new top match into new trackmatches
				Track track1 = new Track(match2Change.TB);
				newMatches.add(new TrackMatch(track1, match2Change));
			}
			
			//If this is just an initial correction, simply edit the existing matches
			//Since the matches are ordered by distance, this will be the first valid match after the
			//primary match; to change the top match to the new one, simply invalidate the top match
			int oldInd = match2Change.getTopMatchInd();
			int newInd = otherMatchInds.get(ind);
			match2Change.validMatch[oldInd]=0;
			match2Change.matchPts[oldInd].numMatches--;
			match2Change.matchPts[newInd].numMatches++;
					
			if (collisionIsEnding) {

				Track track2 = new Track(match2Change.TB);
				newMatches.add(new TrackMatch(track2, match2Change));
				
				//Switch the match to the new match 
				match2Change.clearAllMatches();
				matches = newMatches;
			}
		} else { //No empty points were found
			return false;
		}
		
		return false;
	}
	
	
	//TODO 
	public boolean matchToSplitPts() {
		//Vector<TrackPoint> splitPts = splitPoint(colPoint);
		//if (splitPts!=null) {
			//fix matches, fix pointMatchNums, 
		//}
		
		return false;
	}
	
	
	
	
	
	
	
}
