import java.util.ListIterator;
import java.util.Vector;


public class TrackMatch {
	
	Track track;
	int numMatches;
	TrackPoint[] matchPts;
	Double[] dist2MatchPts;
	int[] validMatch;
	
	/**
	 * Constructor 
	 * @param track Track object to which points are matched  
	 * @param points Points matched to the track
	 * @param numMatches Maximum number of matches stored
	 */
	public TrackMatch(Track track, int numMatches){
		this.track = track;
		matchPts = new TrackPoint[numMatches];
		dist2MatchPts = new Double[numMatches];
		validMatch = new int[numMatches];
	}
	
	
	/**
	 * Constructor, stores the closest NUMMATCHES points to the last point in the track 
	 * @param track Track object to which points are matched  
	 * @param points Points matched to the track
	 * @param numMatches Maximum number of matches stored
	 */
	public TrackMatch(Track track, Vector<TrackPoint> points, int numMatches) {
		this.track = track;
		matchPts = new TrackPoint[numMatches];
		dist2MatchPts = new Double[numMatches];
		validMatch = new int[numMatches];
		
		matchPointsToTrack(points);
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
		Vector<TrackPoint> nearestPoints = track.nearestNPts2End(points, numMatches);
		int i=0;
		ListIterator<TrackPoint> iter = nearestPoints.listIterator();
		while (i<numMatches){
			if (iter.hasNext()){
				matchPts[i] = iter.next();
				dist2MatchPts[i] = track.getEnd().dist(matchPts[i]);
				validMatch[i]=1;
			} else {
				matchPts[i] = null;
				dist2MatchPts[i] = -1.0;
				validMatch[i]=-1;
			}
				
			i++;
		}
			
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
		if ( fromIndex<0 || toIndex<0 || fromIndex>=numMatches || toIndex>=numMatches) {
			return;
		}
		matchPts[toIndex] = matchPts[fromIndex];
		dist2MatchPts[toIndex] = dist2MatchPts[fromIndex];
		validMatch[toIndex] = validMatch[fromIndex];
		
	}
	
	
}
