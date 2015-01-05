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
		
//		if (outTracks.size()>0){
		if (collTrack!=null){
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
	
	/**
	 * Housekeeping for when a collision is fixed
	 */
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
			for (int i=0; i<betterInds.length; i++){
				otherMatches.add(match);
				otherMatchInds.add(betterInds[i]);
				otherPointDists.add(match.dist2MatchPts[betterInds[i]]);
			}
		}
		
		
		if (otherMatches.size()>0) { //Empty points were found: edit the matches to avoid/end the collision
		
			
			if (collisionIsEnding()){ //Make new tracks/matches and replace the old matches 
				Vector<TrackMatch> newMatches = new Vector<TrackMatch>();
				
				//Get the NEAREST point
				int ind = otherPointDists.indexOf(Collections.min(otherPointDists));
				TrackMatch matchWithPoint = otherMatches.get(ind);
				//Make a new track&trackMatch for: 1-the old point 2-the NEAREST empty point
				Track track1 = new Track (matches.firstElement().TB);
				newMatches.add(new TrackMatch(track1, matches.firstElement()));//This will copy the trackmatch, and the new track will start with the old point
				Track track2 = new Track(matchWithPoint.TB);
				matchWithPoint.changePrimaryMatch(otherMatchInds.get(ind)); 
				newMatches.add(new TrackMatch(track2, matchWithPoint));//This will copy the trackmatch, and the new track will start with the new empty point
				//Clear the old trackmatches and replace with the new matches 
				matches.firstElement().clearAllMatches();
				matches = newMatches;
				
			} else {
				
				//Find the point that minimizes the total dist
				double minTotalDist = Double.POSITIVE_INFINITY;
				int minInd = -1;
				
				TrackPoint ptA = matches.get(0).track.getEnd();
				TrackPoint ptB = matches.get(1).track.getEnd();
				Vector<TrackPoint> compPts = new Vector<TrackPoint>();
				compPts.add(matches.firstElement().getTopMatchPoint());
				
				for (int i=0; i<otherMatches.size(); i++) {
					//Find the pairing which minimizes the total dist between the points
					compPts.add(otherMatches.get(i).matchPts[otherMatchInds.get(i)]);
					Vector<TrackPoint> orderedPts = matchPtsToNearbyPts(ptA, ptB, compPts);
					double totalDist = distBtwnPts(ptA, orderedPts.get(0))+distBtwnPts(ptB, orderedPts.get(1));
					if (minTotalDist<totalDist){
						minTotalDist = totalDist;
						minInd = i;
					}
					
					//Remove point i from the list
					compPts.remove(1);
				}
				
				//Edit the appropriate match
				otherMatches.get(minInd).changePrimaryMatch(otherMatchInds.get(minInd));
				
			}
			
			//Get the best secondary match
			//Change this choice
//			Object minDist = Collections.min(otherPointDists);
//			int ind = otherPointDists.indexOf(minDist);
//			TrackMatch match2Change = otherMatches.get(ind);
//			
//			
//			Vector<TrackMatch> newMatches = new Vector<TrackMatch>(); 
//			
//			//
//			if (collisionIsEnding()){
//				//Make new tracks; stick the current top match and the new top match into new trackmatches
//				Track track1 = new Track(match2Change.TB);
//				newMatches.add(new TrackMatch(track1, (TrackMatch)match2Change.clone()));
//			}
//			
//			//If this is just an initial correction, simply edit the existing matches
//			//Since the matches are ordered by distance, this will be the first valid match after the
//			//primary match; to change the top match to the new one, simply invalidate the top match
//			int oldInd = match2Change.getTopMatchInd();
//			int newInd = otherMatchInds.get(ind);
//			match2Change.validMatch[oldInd]=0;
//			match2Change.matchPts[oldInd].numMatches--;
//			match2Change.matchPts[newInd].numMatches++;
//					
//			if (collisionIsEnding()) {
//
//				Track track2 = new Track(match2Change.TB);
//				newMatches.add(new TrackMatch(track2, match2Change));
//				
//				//Switch the match to the new match 
//				match2Change.clearAllMatches();
//				matches = newMatches;
//			}
		} else { //No empty points were found
			return false;
		}
		
		return false;
	}
	
	/**
	 * Attempts to resolve the collision by splitting the collision point into multiple points
	 * @return Whether or not the collision point could be split
	 */
	public boolean matchToSplitPts() {
		
		TrackPoint badPt = matches.firstElement().getTopMatchPoint();
		//Try to split the points into the appropriate number of points
		Vector<TrackPoint> splitPts = collTrack.tb.pe.splitPoint(badPt, inTracks.size(), (int) meanAreaOfInTracks());
		
		if (splitPts!=null) {
			
			//(Delete old point in TrackBuilder)
			
			if (collisionIsEnding()){ //Old collision- end the track, start new ones
				
				//End the old track by clearing that match 
				TrackMatch oldMatch = matches.firstElement();
				oldMatch.clearAllMatches();
				matches.clear();
				
				//Make new empty tracks & TrackMatches for each of the split points
				ListIterator<TrackPoint> spIt = splitPts.listIterator();
				while (spIt.hasNext()){
					Track tr = new Track(oldMatch.TB);
					TrackMatch newMatch =new TrackMatch(tr, oldMatch); 
					newMatch.replaceMatch(1, spIt.next());
					matches.add(newMatch);
				}
				
			} else { //New collision- modify the matches
				//Decide which point goes with which track
				TrackPoint ptA = matches.get(1).track.points.lastElement();
				TrackPoint ptB = matches.get(2).track.points.lastElement();
				Vector<TrackPoint> orderedPts = matchPtsToNearbyPts(ptA, ptB, splitPts);
				//Replace the points in the TrackMatches
				orderedPts.get(1).numMatches++;
				matches.get(1).replaceMatch(1, orderedPts.get(1));
				orderedPts.get(2).numMatches++;
				matches.get(2).replaceMatch(1, orderedPts.get(2));
			}
			
			return true;
		} else {
			return false;
		}
	}
	
	/**
	 * Matches each of two points to a point from a list, minimizing the total distance between points 
	 * @param ptA Point matched to first point in returnPoints
	 * @param ptB Point matched to second point in returnPoints
	 * @param nearbyPts Top points to be matched to ptA&ptB
	 * @return List of points matched to ptA and ptB, in order (AMatch, BMatch)
	 */
	public Vector<TrackPoint> matchPtsToNearbyPts(TrackPoint ptA, TrackPoint ptB, Vector<TrackPoint> nearbyPts){
		
		Vector<TrackPoint> bestMatches = new Vector<TrackPoint>();//In order, (A,B)
		double bestTotalDist = Double.POSITIVE_INFINITY;
		
		for (int ptCInd=1; ptCInd<nearbyPts.size(); ptCInd++){
			for(int ptDInd=(ptCInd+1); ptDInd<=nearbyPts.size(); ptDInd++){
				//Try A-C B-D
				double dist1 = distBtwnPts(ptA, nearbyPts.get(ptCInd)) + distBtwnPts(ptB, nearbyPts.get(ptDInd));
				//Try A-D B-C
				double dist2 = distBtwnPts(ptA, nearbyPts.get(ptDInd)) + distBtwnPts(ptB, nearbyPts.get(ptCInd));
				
				if (dist1<bestTotalDist){
					bestMatches.clear();
					if (dist2<dist1){
						bestTotalDist = dist2;
						bestMatches.add(nearbyPts.get(ptDInd));
						bestMatches.add(nearbyPts.get(ptCInd));
					}
					else {
						bestTotalDist = dist1;
						bestMatches.add(nearbyPts.get(ptCInd));
						bestMatches.add(nearbyPts.get(ptDInd));
					}
				}
			}
		}
		
		return bestMatches;
	}
	
	public double distBtwnPts(TrackPoint pt1, TrackPoint pt2){
		return Math.sqrt((pt1.x-pt2.x)*(pt1.x-pt2.x)+(pt1.y-pt2.y)*(pt1.y-pt2.y));
	}
	
	public double meanAreaOfInTracks(){
		
		double totalA = 0;
		int num = 0;
		
		ListIterator<Track> trIt = inTracks.listIterator();
		while (trIt.hasNext()) {
			num++;
			totalA += trIt.next().points.lastElement().area;
		}
		
		return totalA/num; 
	}
	
	public Vector<TrackPoint> getMatchPoints(){
		Vector<TrackPoint> matchPts = new Vector<TrackPoint>();
		for (int i=1; i<matches.size(); i++){
			matchPts.add(matches.get(i).getTopMatchPoint());
		}
		return matchPts;
	}
	
	//TODO change this?
	public boolean collisionIsEnding(){
		int numDesired = inTracks.size();
		int numCurrent = matches.size();
		return numCurrent!=numDesired;
	}
	
	
	
}
