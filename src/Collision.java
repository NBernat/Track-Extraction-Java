import java.util.Vector;

/**
 * An object that stores all the information about a collision event
 * @author Natalie
 *
 */
public class Collision {
	
	//TODO
	Vector<Track> inTracks;	
	//TODO
	Vector<Track> outTracks;
	//TODO
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

	
	public Collision(Vector<Track> inTracks, TrackPoint point, int frameNum){
		this.inTracks = inTracks;
		//collisionPairs.add(new CollisionPair(point));
		startFrame = frameNum;
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
	
	
	
	//TODO
	public void finishCollision(int endFrame){
		 
		//Vector<Track> newTracks = new Vector<Track>();
		
		//TODO 
		//convert final points to tracks
		//store as outTracks
		//update endFrame
		
	}
	
	//Try to fix the collision at the current frame
	//TODO try to fix the collision point using the specified matches
	public int fixCollision(Vector<TrackMatch> colMatches) {		
		
		if (matchToEmptyPts(colMatches)){
			return 1;
		}
		if (matchToSplitPts(colMatches)) {
			return 2;
		}
		
		return 0;
	}
	
	
	//TODO  
	public boolean matchToEmptyPts(Vector<TrackMatch> colMatches) {
		//find empty points from matches
		//if (single or multiple) exactly one has a good one
			//take that one
		//if multiple and both have a good one
			//compare, take best
		
		//fix matches, fix pointMatchNums 
		
		//if no good ones,
		return false;
	}
	
	//TODO 
	public boolean matchToSplitPts(Vector<TrackMatch> colMatches) {
		//Vector<TrackPoint> splitPts = splitPoint(colPoint);
		//if (splitPts!=null) {
			//fix matches, fix pointMatchNums, 
		//}
		
		return false;
	}
	
	
	
	
	
	
	
}
