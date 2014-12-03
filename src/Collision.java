import java.util.ArrayList;
import java.util.Vector;


public class Collision {
	
	//TODO
	Vector<Track> inTracks;
	//TODO
	ArrayList<CollisionPair> collisionPairs; //OR COLLISIONPOINTS??	
	//TODO
	Vector<Track> outTracks;
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
		collisionPairs.add(new CollisionPair(point));
		startFrame = frameNum;
	}
	
	//TODO addPair method
	
	//TODO
	public boolean hasFinsihed(){
		
		//check numin vs numcurrent 
		//Check distace
		
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
	public int fixCollision(Vector<TrackMatch> matches ) {
		//TODO try to fix the collision point using the specified matches 
		return 0;
	}
	
	
}
