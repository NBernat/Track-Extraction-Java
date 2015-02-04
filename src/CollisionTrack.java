import java.util.ListIterator;
import java.util.Vector;



public class CollisionTrack extends Track{

	
	//TODO
//	Collision coll;
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	/**
	 * The tracks leading up to the collision
	 */
	Vector<Track> inTracks;	
	/**
	 * The tracks attached to the end of the collision
	 */
	Vector<Track> outTracks;
	
	
	
	public CollisionTrack(TrackBuilder tb) {
		super(tb);
		colInit(null);
		// TODO Auto-generated constructor stub
	}
	
	
	public CollisionTrack(TrackPoint firstPt, TrackBuilder tb) {
		super(firstPt, tb);
		colInit(null);
		// TODO Auto-generated constructor stub
	}

//	public void setCollision(Collision c){
//		coll = c;
//	}
	
	public CollisionTrack(Vector<TrackMatch> collMatches, TrackBuilder tb) {
		super(tb);
		colInit(collMatches);
	}
	
	public void colInit(Vector<TrackMatch> collMatches){
//		setMatch(new TrackMatch(this, collMatches.firstElement()));
		inTracks  = new Vector<Track>();
		ListIterator<TrackMatch> cmIt = collMatches.listIterator();
		while (cmIt.hasNext()){
			inTracks.add(cmIt.next().track);
//			collMatches.get(i).clearAllMatches();
		}
		
		outTracks = new Vector<Track>();
	}
	
	public boolean isCollisionTrack(){
		return true;
	}
	
	/**
	 * Checks if the collision has been ended 
	 * @return Whether or not the collision track has ended 	
	 */
	public boolean hasFinsihed(){
		
		if (outTracks!=null && outTracks.size()>0){
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
	
	
	//TODO
	public Vector<TrackMatch> tryToEndCollision(){
		 
		tb.comm.message("Trying to end collision for track "+trackID, VerbLevel.verb_debug);
		
		//When the area of the maggot in a collision drops significantly 
		if(getMatch()!=null){
			double areaFrac = getMatch().areaChangeFrac(); 
			tb.comm.message("Area Frac, point "+getMatch().getTopMatchPoint().pointID+": "+areaFrac, VerbLevel.verb_debug);
			if (0<areaFrac && areaFrac<tb.ep.maxAreaFracForCollisionEnd){
				//Find a nearby maggot using the most recent match 
				//There's only one trackmatch, so look in just that one for an empty point
				Vector<Integer> emptInds = getMatch().indsOfValidNonPrimaryEmptyMatches();
				if (emptInds.size()>0) {
					//Woo! it was found! Now fix the collision
	//				endCollision();
					Vector<TrackMatch> newMatches = splitColMatchIntoTwo(emptInds.firstElement());
					
					//Set the outTracks
					ListIterator<TrackMatch> tmIt = newMatches.listIterator();
					while (tmIt.hasNext()){
						outTracks.addElement(tmIt.next().track);
					}
					
					return newMatches;
				}
				} else {
					tb.comm.message("No empty points were found nearby", VerbLevel.verb_debug);
	//				return -2;
				}
				
			
		}
		return null; 
//		return -1;
	}
	
	/**
	 * Creates new trackMatches 
	 * @param nonPrimaryMatch
	 * @param nonPrimaryInd
	 * @return
	 */
	public Vector<TrackMatch> splitColMatchIntoTwo(int nonPrimaryInd){
		
		Vector<TrackMatch> newMatches = new Vector<TrackMatch>();
		TrackMatch oldMatch = getMatch();//matches.firstElement();
		
		//Add a copy of this match
		Track track1 = new Track(tb);
		newMatches.add(new TrackMatch(track1, oldMatch));
		
		//Edit the match
		oldMatch.changePrimaryMatch(nonPrimaryInd);
		
		//Add a copy of the edited match
		Track track2 = new Track(tb);
		newMatches.add(new TrackMatch(track2, oldMatch));
		
		//Clear the old match to end the collision track
		oldMatch.clearAllMatches();
		
		return newMatches;
	}
	
	public String infoString(){
		String s = "Collision Track "+trackID+": ";
		if (points.size()>0){
			s += "Frames "+points.firstElement().frameNum+"-"+points.lastElement().frameNum+"; ";
		}
		s += "InTracks";
		for (int i=0; i<inTracks.size(); i++){
			s += " "+inTracks.get(i).trackID;
		}
		s += "; OutTracks";
		for (int i=0; i<outTracks.size(); i++){
			s += " "+outTracks.get(i).trackID;
		}
		
		return s;
	}
	
}
