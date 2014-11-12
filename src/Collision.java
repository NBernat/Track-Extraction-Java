import java.util.ArrayList;
import java.util.Vector;


public class Collision {
	
	Vector<Track> inTracks;
	ArrayList<CollisionPair> collisionPairs;
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
	
	
	public Collision(Vector<Track> inTracks, Vector<Track> outTracks, TrackPoint[] points){
		this.inTracks = inTracks;
		this.outTracks = outTracks;
		collisionPairs.add(new CollisionPair(points));
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
	
	
	
	
	public void finishCollision(int endFrame){
		 
		Vector<Track> newTracks = new Vector<Track>();
		
		//TODO 
		//convert final points to tracks
		//store as outTracks
		//update endFrame
		
	}
	
	
}
