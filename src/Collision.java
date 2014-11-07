import java.util.ArrayList;


public class Collision {
	
	Track[] inTracks;
	ArrayList<CollisionPair> collisionPairs;
	Track[] outTracks;
	
	public Collision(Track[] inTracks, Track[] outTracks, TrackPoint[] points){
		this.inTracks = inTracks;
		this.outTracks = outTracks;
		collisionPairs.add(new CollisionPair(points));
	}
	
	//TODO add pair method
}
