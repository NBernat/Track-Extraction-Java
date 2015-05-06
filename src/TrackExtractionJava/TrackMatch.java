package TrackExtractionJava;

import java.io.Serializable;
import java.util.ListIterator;
import java.util.Vector;

/**
 * The TrackMatch class stores a track and a set of points which each might be the next point in the track
 * @author Natalie
 *
 */
public class TrackMatch implements Serializable {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	/**
	 * The TrackBuilder which is using this TrackMatch
	 */
	TrackBuilder TB;
	/**
	 * The track to which points are matched 
	 */
	Track track;
	/**
	 * Non-zero number indicating how many of the closest points should be stored in the match
	 */
	int numStoredMatches;
	/**
	 * The closest points to the end of the track
	 */
	TrackPoint[] matchPts;
	/**
	 * The distance from each matchPt to the end of the track
	 */
	Double[] dist2MatchPts;
	/**
	 * Whether or not each matchPt is valid
	 */
	int[] validMatch;
	
	/**
	 * Constructs a TrackMatch for a track which stores a certain number of point-matches 
	 * @param track Track object to which points are matched  
	 * @param points Points matched to the track
	 * @param numMatches Maximum number of matches stored
	 */
	public TrackMatch(Track track, int numMatches, TrackBuilder TB){
		
		init(track, numMatches, TB);
		
	}
	
	
	/**
	 * Constructor, stores the closest NUMMATCHES points to the last point in the track 
	 * @param track Track object to which points are matched  
	 * @param points Points matched to the track
	 * @param numMatches Maximum number of matches stored
	 */
	public TrackMatch(Track track, Vector<TrackPoint> points, int numMatches, TrackBuilder TB) {
		
		init(track, numMatches, TB);
		
		matchPointsToTrack(points);
		
	}
	
	/**
	 * Constructs a new TrackMatch by copying the info from a given match, except the track changes
	 * @param newTrack The new track 
	 * @param oldMatch The oldmatch that is being copied
	 */
	public TrackMatch(Track newTrack, TrackMatch oldMatch){
		
		this.track = newTrack;
		matchPts = oldMatch.matchPts.clone();
		dist2MatchPts = oldMatch.dist2MatchPts.clone();
		validMatch = oldMatch.validMatch.clone();
		numStoredMatches = oldMatch.numStoredMatches;
		TB = oldMatch.TB;
		track.setMatch(this);
		
	}
	
	public void init(Track track, int numMatches, TrackBuilder TB){
		this.track = track;
		matchPts = new TrackPoint[numMatches];
		dist2MatchPts = new Double[numMatches];
		validMatch = new int[numMatches];
		numStoredMatches = numMatches;
		this.TB = TB;
		track.setMatch(this);
	}
	
	/**
	 * Matches a given list of points to the track by finding the closest points 
	 * <p>
	 * The number of matches, numMatches, is set by an extraction parameter.
	 * <p>
	 * If there are less than numMatches points in POINTS, the match is null and marked as invalid, and the distance to the match point is negative
	 * @param points List of points from which to find matches
	 */
	public void matchPointsToTrack(Vector<TrackPoint> points){
		Vector<TrackPoint> nearestPoints = track.nearestNPts2End(points, numStoredMatches);
		int i=0;
		ListIterator<TrackPoint> iter = nearestPoints.listIterator();
		while (i<numStoredMatches){
			if (iter.hasNext()){
				TrackPoint pt = iter.next();
				if (containsPt(pt.pointID)){
					TB.comm.message("MATCH ALREADY CONTAINS POINT "+pt.pointID, VerbLevel.verb_warning);
				}
				matchPts[i] = pt; //iter.next();
				dist2MatchPts[i] = track.getEnd().dist(matchPts[i]);
				validMatch[i]=1;
			} else {
				matchPts[i] = null;
				dist2MatchPts[i] = -1.0;
				validMatch[i]=-1;
			}
				
			i++;
		}
		//Increment the primary point's match count
		matchPts[0].setNumMatches(matchPts[0].getNumMatches()+1);
			
	}
	
	/**
	 *  Marks as invalid matches that are farther than DISTCUT away from the track 
	 * @param distCut Maximum valid distance between a point and the end of the track
	 * @return Number of points marked as invalid
	 */
	public int cutPointsByDistance(double distCut){
		int numInvalidated = 0;
		
		int i=0;
		while (i<matchPts.length){
			if (dist2MatchPts[i]>distCut){
				if (i==getTopMatchInd()){
					matchPts[i].setNumMatches(matchPts[i].getNumMatches()-1);
				}
				validMatch[i] = 0;
			}
			i++;
		}
		
		return numInvalidated;
	}
	
	/**
	 * Moves point data from one index to another
	 * <p>
	 * Used to keep matches sorted by distance
	 * @param fromIndex Index from which to move data
	 * @param toIndex Index to which to move data
	 */
	public void moveMatchAtoMatchB(int fromIndex, int toIndex){
		if ( fromIndex<0 || toIndex<0 || fromIndex>=numStoredMatches || toIndex>=numStoredMatches) {
			return;
		}
		matchPts[toIndex] = matchPts[fromIndex];
		dist2MatchPts[toIndex] = dist2MatchPts[fromIndex];
		validMatch[toIndex] = validMatch[fromIndex];
		
	}
	
	/**
	 * Returns the closest match
	 * @return An array holding the closest point to the end in the track
	 */
	public TrackPoint[] getPoints(){
		
		if (validMatch[0] == 1){
			TrackPoint[] point = new TrackPoint[]{matchPts[0]};
			return point;
		} else{
			return null;			
		}
		
	}
	
	public TrackPoint getTopMatchPoint(){
		int ind = getTopMatchInd();
		if (ind<0){
			TB.comm.message("Did not find top match point", VerbLevel.verb_debug);
			return null;
		} else {
			return matchPts[ind];
		}
	}
	
	public int getTopMatchInd(){
		for (int i=0; i<matchPts.length; i++){
			if (validMatch[i]>0) {
				return i;
			}
		}
		TB.comm.message("Did not find top match point", VerbLevel.verb_debug);
		return -1;
	}
	
	/**
	 * Tells whether or not the top match is in a collision
	 * @return True if the closest point to the end of the track is the closest point to multiple matches 
	 */
	public int checkTopMatchForCollision(){
		
		TrackPoint topPoint = getTopMatchPoint();
		if (topPoint==null){
			return -1;
		}
		if (topPoint.getNumMatches()>1) {
			return 1;
		}
		return 0;
	}
	
	

	
	/**
	 * Finds the first trackMatch which is colliding with this trackMatch's track 
	 * @param matches List of TrackMatches containing the colliding track
	 * @param startInd First index of matches that is searched for colliding tracks; none of the previous matches are searched 
	 * @return Index of the first TrackMatch whose primary pointMatch is the same as this TrackMatch's primary pointMatch 
	 */
//	public int findCollidingTrackMatch(Vector<TrackMatch> matches, int startInd){
	public int findCollidingTrackMatch(Vector<TrackMatch> matches){
		TB.comm.message("findCollidingTrackMatch called on track "+track.getTrackID(), VerbLevel.verb_debug);
		int ind = -1;
		boolean notFound = true;
		if (matches.isEmpty()){
			TB.comm.message("Match list empty", VerbLevel.verb_debug);
			return -2;
		} else {
//			TB.comm.message("Of "+matches.size()+" matches, we're starting at number "+startInd, VerbLevel.verb_debug);
		}
		
//		if (startInd<matches.size()){
			TB.comm.message("Searching list for collision match...", VerbLevel.verb_debug);
//			ListIterator<TrackMatch> tmIt = matches.listIterator(startInd);
			ListIterator<TrackMatch> tmIt = matches.listIterator();
			while (notFound && tmIt.hasNext()) {
				int curInd = tmIt.nextIndex();
				TrackMatch mCheck = tmIt.next();
				//If this is not the current match and the point is the same, then we found a winner! 
				if (mCheck.getTopMatchPoint()!=null && mCheck.track.getTrackID()!=track.getTrackID() && mCheck.getTopMatchPoint().pointID==getTopMatchPoint().pointID) {
					ind = curInd;
					notFound = false;
				}
			}
//		}
		return ind;
	}
	
	public Vector<Integer> indsOfValidNonPrimaryEmptyMatches(){
		
		Vector<Integer> inds = new Vector<Integer>();
		
		
		//Mark the good non-primary matches
//		int[] goodMatch = new int[numStoredMatches];
		//Skip the first, bc we want non-primary matches
		for (int i=1; i<numStoredMatches; i++) {
			if (validMatch[i]==1 && matchPts[i].numMatches==0) {
				inds.add(i);
//				goodMatch[i]=1;
			} else {
//				goodMatch[i]=0;
			}
		}
		
		//Determine the length of the return array
//		int sum=0;
//		for (int j : goodMatch) sum+=j;
		
		//Populate the return array
//		int[] inds = new int[sum];
//		//Skip the first, bc, again, we want non-primary matches
//		for (int i=1; i<numStoredMatches; i++) {
//			if (goodMatch[i]==1) {
//				inds[i-1] = i;
//			}
//		}
		
		return inds;
	}
	
	
	public void clearAllMatches(){
		//Set all to invalid, decrement the primary point's point count
		getTopMatchPoint().setNumMatches(getTopMatchPoint().getNumMatches()-1);
		for (int i=0; i<numStoredMatches; i++) {
			validMatch[i]=0;
			
		}
	}
	
	public void replaceMatch(int matchIndex, TrackPoint pt){
		validMatch[matchIndex] = 1;
		matchPts[matchIndex] = pt;
		dist2MatchPts[matchIndex] = track.distFromEnd(pt);
	}
	
	public void spillInfoToCommunicator(Communicator comm){
		VerbLevel oldVerb = comm.verbosity;
		comm.setVerbosity(VerbLevel.verb_debug);
		
		comm.message(" ", VerbLevel.verb_debug);
		comm.message("TrackID: "+track.getTrackID()+" ("+(int)track.getEnd().x+", "+(int)track.getEnd().y+")", VerbLevel.verb_debug);
		for (int i=0; i<numStoredMatches; i++){
			TrackPoint pt = matchPts[i];
			String s = "MatchPt"+i+": point "+pt.pointID+", ("+(int)pt.x+","+(int)pt.y+"), ";
			s += dist2MatchPts[i]+" pix away from track, ";
//			Valid hasn't yet been set, so there's no point incorrectly indicating that it's still valid
//			if (validMatch[i]==0){
//				s += "NOT ";
//			}
//			s += "valid.";
			comm.message(s, VerbLevel.verb_debug);
		}
		
		comm.setVerbosity(oldVerb);
	}
	
	/**
	 * Changes the primary match to the specified index. On error, nothing is changed.
	 * @param ind The index of the new primary match
	 * @return status: -1 for ind out of bounds, 0 for invalid match at ind, 1 for success
	 */
	public int changePrimaryMatch(int ind){
		
		if (ind<0 || ind>validMatch.length){
			return -1;
		}
		
		if (validMatch[ind]==0) {
			return 0;
		}
		
		for(int i=0; i<ind; i++){
			if (i==getTopMatchInd()){
				getTopMatchPoint().setNumMatches(getTopMatchPoint().getNumMatches()-1);
			}
			validMatch[i]=0;
		}
		matchPts[ind].setNumMatches(matchPts[ind].getNumMatches()+1);
		return 1;
		
	}
	
	
	/**
	 * Returns the area of the top match point as a fraction of the last point in the track
	 * @return Area of the top match point expressed as a fraction of the last pointin the track, or -1 if there are not enough points in the track
	 */
	public double areaChangeFrac(){
		if (track.getNumPoints()>0 && getTopMatchPoint()!=null){
			
			return (getTopMatchPoint().area)/(track.getEnd().area);
			
		} else {
			return -1.0;
		}
		
	}
	
	public boolean containsPt(int ptID){
		for (int i=0; i<matchPts.length; i++){
			if (matchPts[i]!=null && matchPts[i].pointID==ptID){
				return true;
			}
		}
		return false;
	}
	
}
