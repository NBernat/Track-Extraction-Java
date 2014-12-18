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
	
	Vector<TrackMatch> matches;

	
	//TODO
	public Collision(Vector<TrackMatch> initMatches, int frameNum){
		//this.inTracks = inTracks;
		//collisionPairs.add(new CollisionPair(point));
		
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
	 * 
	 * @return Whether or not the collision has diverged into separate tracks
	 */
	public boolean hasFinsihed(){
		
		if (outTracks.size()>0){
			return true;
		}
		
		return false;
	}
	
	public Vector<Track> getOutTracks(){
		return outTracks;
	}
	


	
	//Try to fix the collision at the current frame
	//TODO try to fix the collision point using the stored matches
	public int fixCollision() {		
		
		if (matchToEmptyPts()){
			return 1;
		}
		if (matchToSplitPts()) {
			return 2;
		}
		
		//This is a legit collision: 
		//make a new track and trackmatch 
		//end the old track matches (invalidate all) 
		
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
		
		if (otherMatches.size()>0) { //Empty points were found
			//Edit the matches to avoid/end the collision
		
			//Get the best secondary match
			Object minDist = Collections.min(otherPointDists);
			int ind = otherPointDists.indexOf(minDist);
			TrackMatch match2Change = otherMatches.get(ind);
			
			if (numCurrent==numDesired){//This is an initial correction, just edit the existing matches 
					
				//Since the matches are ordered by distance, this will be the first valid match after the
				//primary match; to change the top match to the new one, simply invalidate the top match
				int oldInd = match2Change.getTopMatchInd();
				int newInd = otherMatchInds.get(ind);
				match2Change.validMatch[oldInd]=0;
				match2Change.matchPts[oldInd].numMatches--;
				match2Change.matchPts[newInd].numMatches++;
					
			} else {//This is the end of a collision, create new matches (which will be converted to new tracks)
				//TODO handle the end of collisions
				
				
				
			}
		} else { //No empty points were found
			return false;
		}
		
		//find empty points from matches
		//if (single or multiple) exactly one has a good one
			//take that one
		//if multiple and both have a good one
			//compare, take best
		
		//fix matches, fix pointMatchNums 
		
		//if no good ones,
		return false;
	}
	
//	public 
	
	//TODO 
	public boolean matchToSplitPts() {
		//Vector<TrackPoint> splitPts = splitPoint(colPoint);
		//if (splitPts!=null) {
			//fix matches, fix pointMatchNums, 
		//}
		
		return false;
	}
	
	
	
	
	
	
	
}
