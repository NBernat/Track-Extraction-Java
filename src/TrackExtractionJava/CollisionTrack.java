package TrackExtractionJava;

import java.util.ListIterator;
import java.util.Vector;



public class CollisionTrack extends Track{

	
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
	}
	
	
	public CollisionTrack(TrackPoint firstPt, TrackBuilder tb) {
		super(firstPt, tb);
		colInit(null);
	}
	
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
		 
		tb.comm.message("Trying to end collision for track "+getTrackID(), VerbLevel.verb_debug);
		
		 
		if(getMatch()!=null){ //i.e. if this is not the first point in the track
			
			//Check the ratio of areas between the tentative next point and the last point in the collision
			double areaFrac = getMatch().areaChangeFrac(); 
			if(areaFrac>0){
				tb.comm.message("Area Frac, point "+getMatch().getTopMatchPoint().pointID+": "+areaFrac, VerbLevel.verb_debug);
				
				//When the area of the maggot in a collision drops significantly
				if (areaFrac<tb.ep.maxAreaFracForCollisionEnd){
					//Find a nearby maggot using the most recent match 
					//There's only one trackmatch, so look in just that one for an empty point
					Vector<Integer> emptInds = getMatch().indsOfValidNonPrimaryEmptyMatches();
					
					if (emptInds.size()>0) {
						//Woo! it was found! Now fix the collision
						Vector<TrackMatch> newMatches = splitColMatchIntoTwo(emptInds.firstElement());
						//Set the outTracks
						ListIterator<TrackMatch> tmIt = newMatches.listIterator();
						while (tmIt.hasNext()){
							outTracks.addElement(tmIt.next().track);
						}
						return newMatches;
						
						
					} else {
						//The area dropped, but no points were found nearby  
						tb.comm.message("No empty points were found nearby", VerbLevel.verb_debug);
						
						//TODO end the collision track, start new track (i.e. setup for trackBuilder machinery)
						//Create a new track by generating a new match
						Vector<TrackMatch> newMatches = new Vector<TrackMatch>();
						TrackMatch newMatch = new TrackMatch(new Track(tb), getMatch());
						newMatches.add(newMatch);
						
						//Clear the old match 
						outTracks.add(newMatch.track);
						getMatch().clearAllMatches();//End the collision track
						
						return newMatches;
					}
				}
			}
		}
		//If this is the first point, or if the area did not change significantly
		return null;
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
		String s = "Collision Track "+getTrackID()+": ";
		if (getNumPoints()>0){
			s += "Frames "+getStart().frameNum+"-"+getEnd().frameNum+"; ";
		}
		s += "InTracks";
		for (int i=0; i<inTracks.size(); i++){
			s += " "+inTracks.get(i).getTrackID();
		}
		s += "; OutTracks";
		for (int i=0; i<outTracks.size(); i++){
			s += " "+outTracks.get(i).getTrackID();
		}
		
		return s;
	}
	
	public String inAndOutString(){
		String s = "";
		s += "InTracks";
		for (int i=0; i<inTracks.size(); i++){
			s += " "+inTracks.get(i).getTrackID();
		}
		s += "\nOutTracks";
		for (int i=0; i<outTracks.size(); i++){
			s += " "+outTracks.get(i).getTrackID();
		}
		return s;
		
	}
	
	
	public String description(){
		return makeDescription(""+getTrackID(), getPoints(), inAndOutString());
	}
	
}
