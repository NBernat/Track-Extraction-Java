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
	
	//TODO addPair method
	
	//TODO
	public boolean hasFinsihed(){
		
		//check numin vs numcurrent(aka length of matches)
		
		return false;
	}
	
	public Vector<Track> getOutTracks(){
		return outTracks;
	}
	

	public void finishCollision(int endFrame){
		 
		//Vector<Track> newTracks = new Vector<Track>();
		
		//convert final points to tracks
		//store as outTracks
		//update endFrame
		
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
		
		return 0;
	}
	
	
	//TODO Implement this for collision track points 
	public boolean matchToEmptyPts() {
		
		
		//Iterate through the matches
			//find the best match
		Vector<TrackMatch> otherMatches = new Vector<TrackMatch>();//for locating the match to modify
		Vector<Integer> otherMatchInds = new Vector<Integer>(); //for locating the point within the match 
		Vector<Double> otherPointDists = new Vector<Double>();//for finding best new match
		
		//Find the available emptypoints
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
		
		if (numCurrent==numDesired){
			//Try to match the new pointmatches to current trackmatches
			if (otherMatches.size()>0) {
				Object minDist = Collections.min(otherPointDists);
				int ind = otherPointDists.indexOf(minDist);
				//Change the primary point to this one by setting all matches before the new match to be invalid
				for (int i=0; i<otherMatchInds.get(ind); i++) {
					otherMatches.get(ind).validMatch[i]=0;
				}
				
			} else {
				return false;
			}
			
		} else {
			//create new pointmatches
			
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
