package TrackExtractionJava;

import ij.IJ;
import ij.ImageStack;
import ij.text.TextWindow;

import java.io.PrintWriter;
import java.io.Serializable;
import java.io.StringWriter;
import java.util.ListIterator;
import java.util.Vector;

/**
 * Extracts tracks of moving blobs from a stack of images.  
 * @author Natalie Bernat
 *
 */
public class TrackBuilder implements Serializable{

		
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	////////////////////////////
	// Ongoing track objects 
	////////////////////////////
	/**
	 * Tracks which have not yet been ended
	 */
	Vector<Track> activeTracks;
	/**
	 * Tracks which have been ended
	 */
	Vector<Track> finishedTracks;
	/**
	 * Active collision events  
	 */
	Vector<Integer> activeColIDs;
	/**
	 * Finished collision events  
	 */
	Vector<Integer> finishedColIDs;
	/**
	 * Number of tracks, including active tracks and tracks in collisions 
	 */
	int numTracks;
	
	////////////////////////////
	// Frame-by-frame objects
	////////////////////////////
	/**
	 * TrackPoints which have not yet been added to tracks
	 * <p>
	 * Set when the points are activated
	 */
	Vector<TrackPoint> activePts;
	/**
	 * Matches between tracks and their (nearest/nearby? TBD) points
	 * <p>
	 * Set when the tracks are activated
	 */
	Vector<TrackMatch> matches;
	/**
	 * Index of frame being processed
	 */
	int frameNum;
	
	
	////////////////////////////
	// Auxiliary  objects
	////////////////////////////
	/**
	 * Parameters used for extracting tracks
	 */
	ExtractionParameters ep;
	/**
	* An object which provides points from an image 
	* <p>
	* Because of the point extractor, the TrackBuilder doesn't interact directly with the stack of images 
	*/
	PointExtractor pe;
	/**
	 * Message handler for displaying debug messages
	 */
	Communicator comm;
	/**
	 * Message handler for displaying information about matches in each frame	
	 */
	Vector<Communicator> matchSpills;
	/**
	 * Message handler for displaying information about the tracks
	 */
	Communicator trackMessage;
	
	////////////////////////////
	// Driver and Constructors
	////////////////////////////

	/**
	 * Constructs a TrackBuilder object
	 */
	public TrackBuilder(ImageStack IS, ExtractionParameters ep){
		
		this.ep = ep;
		init(ep.startFrame, IS);
	}
	
	/**
	 * Initialization of objects
	 */
	private void init(int frameNum, ImageStack IS){
		
		//Set Auxillary objects
		comm = new Communicator();
		pe = new PointExtractor(IS, comm, ep);
		
		//Set track-building objects
		activeTracks = new Vector<Track>();
		finishedTracks = new Vector<Track>();
		activeColIDs = new Vector<Integer>();
		finishedColIDs = new Vector<Integer>();
		
		//Set status parameters
		this.frameNum = frameNum;
		
		
		matchSpills = new Vector<Communicator>();
		trackMessage = new Communicator();
		
		
	}
	
	////////////////////////////
	// Track Building methods 
	////////////////////////////
	
	public void run(){
		buildTracks();
	}
	
	/**
	 * Constructs tracks from a series of images
	 */
	protected void buildTracks(){
		
		//Add frames to track objects
		
//		while (pe.nextFrameNum() <= pe.fl.getStackSize()) {
//		while (pe.nextFrameNum() <= pe.endFrameNum && pe.nextFrameNum() <= ep.endFrame) {
		TicToc t = new TicToc();
		t.tic();
		long lastelapsed = 0;
	    long reportEvery = 60;
	    if (!ep.subset){
	    	System.out.println("Building tracks for frames 1-"+pe.fl.getStackSize());
	    } else {
	    	System.out.println("Building tracks for frames "+ep.startFrame+"-"+ep.endFrame);
	    }
		while ( (!ep.subset && (pe.nextFrameNum()<=pe.fl.getStackSize()) ) || 
				(ep.subset && (pe.nextFrameNum()<=pe.endFrameNum && pe.nextFrameNum()<=ep.endFrame)) ){
			frameNum = pe.nextFrameNum();
			if (frameNum%20 == 0){
				IJ.showStatus("Building : Adding Frame "+frameNum+"...");
			}
			long elapsed = t.toc()/1000;
	        if (elapsed - lastelapsed > reportEvery){
	            lastelapsed = elapsed;
	            System.out.println(elapsed+"s: frame "+frameNum);
	        }
			if (addFrame(frameNum)>0) {
				comm.message("Error adding frame "+pe.nextFrameNum(), VerbLevel.verb_error);
				return;
			}
			
		}
		
		//Debug Output
		trackMessage.message("There are "+activeColIDs.size()+"+"+finishedColIDs.size()+" collisions", VerbLevel.verb_message);
		for (int i=0; i<activeTracks.size(); i++){
			trackMessage.message(activeTracks.get(i).infoString(), VerbLevel.verb_message);
		}
		
		//Move all active tracks to finished
		finishedTracks.addAll(activeTracks);
		activeTracks.removeAll(activeTracks);
		

		if (ep.matchSpill.length>0) {
			for (int i=0;i<ep.matchSpill.length;i++){
				
				int ind = ep.matchSpill[i]-ep.startFrame+1;
				if (ind>0){
					new TextWindow("Match Spill for frame "+ep.matchSpill[i], matchSpills.get(ind).outString, 500, 500);
				}
			}
		}
		if (ep.flagAbnormalMatches){
			StringBuilder sb = new StringBuilder();
			for (Communicator c : matchSpills){
				sb.append(c.outString);
			}
			new TextWindow("Abnormal matches", sb.toString(), 600, 500);
		}
		
		
		//resolveCollisions();
		finishedColIDs.addAll(activeColIDs);
		activeColIDs.removeAll(finishedColIDs);

		
	}
	
	
	/**
	 * Adds the points from the specified image frame to the track structures
	 * @param frameNum Index of the frame to be added
	 * @return status: 0 means all is well, >0 means there's an error
	 */
	private int addFrame(int frameNum) {
		
		if (frameNum%ep.GCInterval==0){
			System.gc();
		}
		
		if (loadPoints(frameNum)>0) {
			comm.message("Error loading points in frame "+frameNum, VerbLevel.verb_error);
			return 1;
		}
				
		if (updateTracks()>0) {
			comm.message("Error extending tracks in frame "+frameNum, VerbLevel.verb_error);
			return 1;
		}
		
		return 0;
	}

	
	/**
	 * Loads points from the specified frame into the activePts object
	 * @param frameNum Index of the frame to load
	 * @return status: 0 means all is well, >0 means there's an error
	 */
	private int loadPoints(int frameNum){
		
		if(pe.extractFramePoints(frameNum)>0){
			comm.message("Error extracting points from frame "+frameNum, VerbLevel.verb_error);
			return 1;
		}
		activePts = pe.getPoints();
		if (activePts.size()==0){
			comm.message("No points were extracted from frame "+frameNum, VerbLevel.verb_warning);
		}

		return 0;
	}
	
	
	
	/**
	 * Extend the tracks to include points extracted from the current frame 
	 * @return status: 0 means all is well, >0 means there's an error
	 */
	private int updateTracks(){
		
		//Build matches
		makeMatches();
		comm.message("Frame "+frameNum+" initially has "+matches.size()+" matches", VerbLevel.verb_debug);
		if (matches.size()==0){
			comm.message("No matches were made in frame "+frameNum, VerbLevel.verb_warning);
		}
		
		//Modify matches
		comm.message("Modifying matches", VerbLevel.verb_debug);
		modifyMatches();
		
		//Fuse matches to tracks
		comm.message("Extending/ending tracks", VerbLevel.verb_debug);
		int acTrNum = extendOrEndMatches(); 
		if (acTrNum<0){
			comm.message("Error attaching new points to tracks", VerbLevel.verb_error);
			return 1;
		}
		comm.message("Number of active tracks: "+acTrNum, VerbLevel.verb_debug);
		
		//Start new tracks from remaining activePts
		comm.message("Starting new tracks", VerbLevel.verb_debug);
		int numNew = startNewTracks();
		comm.message("New Tracks: "+numNew, VerbLevel.verb_debug);
		
		return 0;
	}
	
	
	/**
	 * Matches the newly added points to the tracks 
	 */
	private void makeMatches(){
		
		Communicator matchComm = new Communicator();
		
		matches = new Vector<TrackMatch>();
		
		for (int j=0; j<activePts.size(); j++){
			comm.message("Point "+j+": "+activePts.get(j).infoSpill(), VerbLevel.verb_debug);
		}
		
		
		
		//Match points to Tracks
		for(int i=0; i<activeTracks.size(); i++){
			TrackMatch newMatch = new TrackMatch(activeTracks.get(i), activePts, ep.numPtsInTrackMatch, this);
			matches.add(newMatch);
			newMatch.spillInfoToCommunicator(matchComm);
		}
		
		if (ep.matchSpill.length>0){
			matchSpills.addElement(matchComm);
		}
		
		
		
		
	}
	

	
	/**
	 * Corrects initial matching errors
	 */
	private void modifyMatches(){
		
		int numCutByDist = cutMatchesByDistance();
		comm.message("Number of matches cut from frame "+frameNum+" by distance: "+numCutByDist, VerbLevel.verb_debug);
		
		if (ep.flagAbnormalMatches){
			for (TrackMatch m : matches){
				if (m.getTopMatchPoint().numMatches>1){
					Communicator c = new Communicator();
					m.spillInfoToCommunicator(c);
					matchSpills.add(c);
				}
			}
		}
		
		manageCollisions();
		
	}
	
	
	/**
	 * Marks as invalid any TrackMatch that is too far away 
	 * @return Total number of matches  
	 */
	private int cutMatchesByDistance(){
		 
		ListIterator<TrackMatch> it = matches.listIterator();
		int numRemoved = 0;
		while (it.hasNext()) {
			numRemoved += it.next().cutPointsByDistance(ep.maxMatchDist);
		}
		return numRemoved;
	}
	
	
	/**
	 * Maintains the collision structures by adding new collisions, trying basic methods of fixing current collisions, and ending finished collisions 
	 */
	private void manageCollisions(){
		
		if (ep.collisionLevel==0){
			//End all tracks involved in a collision
			
			int numEnded = endNewCollisions();
			comm.message("Tracks ended due to collision: "+numEnded, VerbLevel.verb_debug);
			
		} else if(ep.collisionLevel==1) {
			//Try to resolve collisions via rethresholding, THEN end remaining collisions
			avoidCollisions();
			endNewCollisions();
			
		} else {
			//Try above, then track collision events
			
			//FOR LATER COLLISION TRACKING
			avoidOrCreateCollisions();
			
			//if a point is assigned to more than one track, start a collision event
//			Vector<TrackMatch> newColMatches = detectNewCollisions();
//			comm.message("Number of new collisions in frame "+frameNum+": "+newColMatches.size(), VerbLevel.verb_debug);
//			
//			//Try to maintain number of incoming tracks in each collision by grabbing nearby tracks and splitting points 
//			int endedCols = endCollisions();
//			comm.message("Number of collisions ended in frame "+frameNum+": "+endedCols, VerbLevel.verb_debug);
			
			
			
//			matches.addAll(newColMatches);
			//if number incoming = number outgoing, finish
//			int numFinishedCollisions = releaseFinishedCollisions();
//			comm.message("Number of collisions ended in frame "+frameNum+": "+numFinishedCollisions, VerbLevel.verb_debug);
			
		}
		
	}
	
	
	/**
	 * Ends any tracks that are matched to the same point as another track
	 * @return Number of ended collisions
	 */
	private int endNewCollisions(){
		
		int numEnded = 0;
		ListIterator<TrackMatch> tmIt = matches.listIterator();
		
		while (tmIt.hasNext()){
			
			TrackMatch match = tmIt.next();
			comm.message("Checking Track "+match.track.getTrackID()+" for collisions..", VerbLevel.verb_debug);
			
			if (match.checkTopMatchForCollision()>0) {
				comm.message("Collision found, "+match.getTopMatchPoint().getNumMatches()+" tracks matched to point", VerbLevel.verb_debug);
				
				//Collect the info from the tracks in collisions
				Vector<TrackMatch> colMatches= new Vector<TrackMatch>();
				colMatches.add(match);
				//Find all the additional tracks in the collision
				int numColliding = match.getTopMatchPoint().getNumMatches();
				comm.message("Collision has "+numColliding+" tracks", VerbLevel.verb_debug);
				colMatches.addAll(getCollisionMatches(match));
				
				//TODO deal with incoming/outgoing 
				//End the colliding tracks
				ListIterator<TrackMatch> cmIt = colMatches.listIterator();
				while (cmIt.hasNext()) {
					comm.message("Clearing collsions", VerbLevel.verb_debug);
					numEnded++;
					TrackMatch endMatch = cmIt.next();
					trackMessage.message("Track "+endMatch.track.getTrackID()+" ended at frame "+(frameNum-1)+" for collision in frame "+frameNum, VerbLevel.verb_message);
					endMatch.track.otherInfo += "Ended for collision at frame "+(frameNum-1)+"\nCollision parties: "+colMatches.get(0).track.getTrackID();
					for (int j=1; j<colMatches.size(); j++){
						endMatch.track.otherInfo += ", "+colMatches.get(j).track.getTrackID();
					}
					endMatch.clearAllMatches(); 
				}
				if (numEnded>0) {
					comm.message("Done clearing collisions", VerbLevel.verb_debug);
				}
				
			}	
		}
		comm.message("Ended "+numEnded+" Collision tracks", VerbLevel.verb_debug);
		return numEnded;
	}
	
	
	private void avoidCollisions(){
		
		// 
		Vector<TrackMatch> toRemove = new Vector<TrackMatch>();
		Vector<TrackMatch> toAdd= new Vector<TrackMatch>();
		
		for (TrackMatch tm : matches){
			if (tm.checkTopMatchForCollision()>0){
				rethreshCollision(tm, toRemove, toAdd);
			}
		}
		//Non-Concurrently modify the list of matches
		matches.removeAll(toRemove);
		matches.addAll(toAdd);
		
	}
	
	private boolean rethreshCollision(TrackMatch tm, Vector<TrackMatch> toRemove, Vector<TrackMatch> toAdd){
		
		//try to rethreshold point
		comm.message("Attempting to rethreshold collision in frame "+frameNum, VerbLevel.verb_message);
		int npts = tm.getTopMatchPoint().numMatches;
		
		Vector<TrackPoint> newPts = MaggotTrackPoint.splitPt2NPts((MaggotTrackPoint)tm.getTopMatchPoint(), npts, (int)tm.track.meanArea(), pe, ep, comm);
		
		if (newPts!=null && newPts.size()==npts){
			comm.message("Attempt to rethreshold collision in frame "+frameNum+" sucessful, making appropriate changes in builder", VerbLevel.verb_message);
			try {
				//Find all the track[matche]s in the collision
				Vector<TrackMatch> colMatches= new Vector<TrackMatch>();
				colMatches.add(tm);
				colMatches.addAll(getCollisionMatches(tm));
				
				//Match points to tracks so as to minimize total dist between pairs
				Vector<TrackMatch> newMatches = TrackMatch.matchNPts2NTracks(newPts, TrackMatch.getTracks(colMatches), ep.maxMatchDist, this);
				
				//Update the TrackBuilder structure
				if (newMatches!=null){
					rmActivePt(tm.getTopMatchPoint());
//					activePts.remove(tm.getTopMatchPoint());
					activePts.addAll(newPts);
					toAdd.addAll(newMatches);
					toRemove.addAll(colMatches);
					for (TrackMatch m: colMatches) m.clearAllMatches();//So that the other match (ie the other element of colMatches) doesn't pass the "checkTopMatchForCollision" test
				} else {
					String s = "";
					s+= newPts.size()+" points unable to be matched to "+colMatches.size()+"tracks\n";
					s+="\nCollision tracks:\n";
					for (TrackMatch cm : colMatches){
						s+="Track "+cm.track.getTrackID()+"\n";
					}
					
					s+="\nOldPoint: \n"+tm.getTopMatchPoint().getTPDescription()+"\n";
					s+="\nNew points:\n";
					for (TrackPoint p : newPts){
						s+=p.getTPDescription()+"\n";
					}
					
					new TextWindow("Point splitting error; frame "+newPts.firstElement().frameNum, s, 600, 500);
				}
				
				
				return true;//Successfully avoided collision
				
			} catch (Exception e){
				
				StringWriter sw = new StringWriter();
				PrintWriter pw = new PrintWriter(sw);
				e.printStackTrace(pw);
				new TextWindow("Error extracting tracks", sw.toString(), 500, 500);
				return false;
			}
			
		} else {
			String infoSt = "";
			if (newPts==null){
				infoSt+="no points made";
			} else {
				infoSt+="improper number of points made ("+newPts.size()+"pts, not "+npts+")";
			}
			comm.message("Attempt to rethreshold collision in frame "+frameNum+" unsuccessful; "+infoSt, VerbLevel.verb_message);
			return false;//Did not avoid collision
		}
	}
	
	//MEANT FOR LATER, DURING COLLISION TRACKING
	private void avoidOrCreateCollisions(){
		
		for (TrackMatch tm : matches){
			if (tm.checkTopMatchForCollision()>0){
				CollisionTrack ct = avoidOrCreateCollision(tm);
				if (ct!=null){
					//add to list of collisions
				}
			}
		}
		
		
	}
	
	private CollisionTrack avoidOrCreateCollision(TrackMatch tm){
		
		return null;
	}
	
	
	
	/**
	 * Finds tracks that collide, tries to resolve them, and if they can't be fixed, creates a new Track and Collision 
	 * @return The number of new collisions
	 */
	/*
	private Vector<TrackMatch> detectNewCollisions(){
		
//		int numNewCollisions = 0;
		Vector<TrackMatch> newColMatches = new Vector<TrackMatch>();

		//Check each match for a collision
		ListIterator<TrackMatch> mIt = matches.listIterator(); 
		while (mIt.hasNext()){
			
			TrackMatch match = mIt.next();
			//CONCURRENT MODIFICATION ERROR: can't add elements until this is over
			
			if (!match.track.isCollision.lastElement() && match.checkTopMatchForCollision()>0){

				//Collect the info from the tracks in collisions
				
				Vector<TrackMatch> colMatches= new Vector<TrackMatch>();
				
				colMatches.add(match);
				colMatches.addAll(getCollisionMatches(match));
				
				if(colMatches.size()==1){
					comm.message("Collision at point "+match.getTopMatchPoint().pointID+" in track "+match.track.getTrackID()+" has no accompanying trackmatch!", VerbLevel.verb_error);
					match.getTopMatchPoint().setNumMatches(match.getTopMatchPoint().getNumMatches()-1);
					trackMessage.message("Track "+match.track.getTrackID()+" ended at frame "+(frameNum-1)+" for rogue collision in frame "+frameNum, VerbLevel.verb_message);
					match.clearAllMatches();
				} else {
					
					TrackMatch colMatch = avoidOrCreateCollision(colMatches);
					
					if (colMatch!=null){
						newColMatches.add(colMatch);
					}
				}
				
			}
		}
		
//		matches.addAll(newColMatches);
		
		return newColMatches;
	}
	*/

	/**
	 * Finds the TrackMatches which are colliding with the given TrackMatch
	 * @param match The query TrackMatch
	 * @return A Vector of TrackMatches sharing the same match point
	 */
	public Vector<TrackMatch> getCollisionMatches(TrackMatch match){
		
//		Vector<TrackMatch> colMatches= new Vector<TrackMatch>();
			
		//Find the TrackMatches that are in the collision
		Vector<TrackMatch> ctm = match.findCollidingTrackMatches(matches);
		if (ctm!=null && ctm.size()>0) {
//			colMatches.addAll(ctm);
			comm.message("Match(es) found", VerbLevel.verb_debug);
		} else{
			ctm=null;
			comm.message("No matches found", VerbLevel.verb_debug);
		}
		
		return ctm;

		
	}
	

	/**
	 * Tries to edit colliding track matches, otherwise ends the tracks, creates a collision track, and keeps a record of the event in the form of a Collision object
	 * @param colMatches The TrackMatches that collide to the same point 
	 * @return The number of new collisions 
	 */
	/*
	private TrackMatch avoidOrCreateCollision(Vector<TrackMatch> colMatches){
		
		TrackMatch retMatch = null;
		
		//Grab the point. to be deleted if it's split into multiple points
		TrackPoint colPt = colMatches.firstElement().getTopMatchPoint();
		int ptID = colPt.pointID;
		
		
		
		//Try to fix the collision
		int colFix = avoidCollision(colMatches);//newCol.avoidCollision();//
		
		if (colFix==0){ //The Collision-fixing machinery did not fix the matches
		//The old matches still exist, and they will be used later to end tracks (ie don't remove them)
		//Create a new collision object from the collision
		CollisionTrack newCol = new CollisionTrack(colMatches, this);
		fixCollisionMatches(newCol, colMatches);
		activeColIDs.add(newCol.getTrackID());
		//add this trackID to list
		retMatch = newCol.getMatch();//newCol.matches.firstElement();//

		}
		
		else if (colFix==1) {
			comm.message("Collision avoided at point "+ptID+" by matching to nearby points", VerbLevel.verb_debug);
		} else if (colFix==2) {
			comm.message("Collision avoided at point "+ptID+" by splitting the collision point", VerbLevel.verb_debug);
		}
		
		return retMatch;
	}
	
	*/
	
	/*
	private int avoidCollision(Vector<TrackMatch> colMatches){
		
		if (matchCollisionToEmptyPts(colMatches)){
			return 1;
		}  
		if (matchCollisionToSplitPts(colMatches)) {
			return 2;
		}
		
		return 0;
	}
	*/
	
	/*
	private boolean matchCollisionToEmptyPts(Vector<TrackMatch> colMatches){
		
		//Set up the data needed to adjust the matches
		Vector<TrackMatch> otherMatches = new Vector<TrackMatch>();//for locating the match to modify
		Vector<Integer> otherMatchInds = new Vector<Integer>(); //for locating the point within the match 
		Vector<Double> otherPointDists = new Vector<Double>();//for finding best new match
		
		ListIterator<TrackMatch> mIt = colMatches.listIterator();
		while (mIt.hasNext()) {
			TrackMatch match = mIt.next();
			Vector<Integer> betterInds = match.indsOfValidNonPrimaryEmptyMatches();
			for (int i=0; i<betterInds.size(); i++){
				otherMatches.add(match);
				otherMatchInds.add(betterInds.get(i));
				otherPointDists.add(match.dist2MatchPts[betterInds.get(i)]);
			}
		}
		
		if (otherMatches.size()>0) { 
			//Find the point that minimizes the total dist
			double minTotalDist = Double.POSITIVE_INFINITY;
			int minInd = -1;
			
			TrackPoint ptA = colMatches.get(0).track.getEnd();
			TrackPoint ptB = colMatches.get(1).track.getEnd();
			Vector<TrackPoint> compPts = new Vector<TrackPoint>();
			compPts.add(colMatches.firstElement().getTopMatchPoint());
			
			for (int i=0; i<otherMatches.size(); i++) {
				//Find the pairing which minimizes the total dist between the points
				compPts.add(otherMatches.get(i).matchPts[otherMatchInds.get(i)]);
				Vector<TrackPoint> orderedPts = matchPtsToNearbyPts(ptA, ptB, compPts);
				double totalDist = distBtwnPts(ptA, orderedPts.get(0))+distBtwnPts(ptB, orderedPts.get(1));
				if (minTotalDist<totalDist){
					minTotalDist = totalDist;
					minInd = i;
				}
				
				//Remove point i (=compPts[1]) from the list
				compPts.remove(1);
			}
			
			//Edit the appropriate match
			otherMatches.get(minInd).changePrimaryMatch(otherMatchInds.get(minInd));
			
			return true;
		} else {
			return false;
		}
	}
	
	*/
	
	/*
	private boolean matchCollisionToSplitPts(Vector<TrackMatch> colMatches){
		 
		TrackPoint badPt = colMatches.firstElement().getTopMatchPoint();
		//Try to split the points into the appropriate number of points
		Vector<Track> trList = new Vector<Track>();
		for(int i=0; i<colMatches.size(); i++){
			trList.add(colMatches.get(i).track);
		}
//		Vector<TrackPoint> splitPts = pe.splitPoint(badPt, colMatches.size(), (int) meanAreaOfTracks(trList));
		
//		if (splitPts.size()>0) {
//			//Decide which point goes with which track
//			TrackPoint ptA = colMatches.get(0).track.getEnd();
//			TrackPoint ptB = colMatches.get(1).track.getEnd();
//			Vector<TrackPoint> orderedPts = matchPtsToNearbyPts(ptA, ptB, splitPts);
//			//Replace the points in the TrackMatches
//			orderedPts.get(0).setNumMatches(orderedPts.get(0).getNumMatches()+1);
//			colMatches.get(0).replaceMatch(1, orderedPts.get(0));
//			orderedPts.get(1).setNumMatches(orderedPts.get(1).getNumMatches()+1);
//			colMatches.get(1).replaceMatch(1, orderedPts.get(1));
//			
//			//Manage the points in the trackbuilder objects
//			activePts.remove(badPt);
//			activePts.addAll(orderedPts);
//			
//			return true;
//		}
		
		
		return false;
	}
	*/

	/*
	private void fixCollisionMatches(CollisionTrack colTrack, Vector<TrackMatch> initMatches){
		
		colTrack.setMatch(new TrackMatch(colTrack, initMatches.firstElement()));
		
		for (int i=0; i<initMatches.size(); i++){
			initMatches.get(i).clearAllMatches();
		}
		
	}
	
	*/
	
	/**
	 * Tries to fix each ongoing collision
	 */
	/*
	private int endCollisions(){
		
		int endedCols = 0;
	
		ListIterator<Integer> colIt = activeColIDs.listIterator();
		Vector<Integer> toRemove = new Vector<Integer>();
		while (colIt.hasNext()) {
			
			Integer colID = colIt.next();
			comm.message("Looking for collisionTrack "+colID, VerbLevel.verb_debug);
			int ind = findIndOfTrack(colID, activeTracks);
			if (ind>=0) {//otherwise, it's a new collision and doesn't need to be checked
				comm.message("Index of activeTracks: "+ind, VerbLevel.verb_debug);
				CollisionTrack col = (CollisionTrack) activeTracks.get(ind);
				
				
				Vector<TrackMatch> newMatches = col.tryToEndCollision(); 
				
				if (newMatches!=null && newMatches.size()>0){ //The collision was fixed! (or simply ended)
					
					finishedColIDs.add(colID);
					//activeColIDs.remove(colID);
					toRemove.add(colID);
					matches.addAll(newMatches);
					endedCols++;
					
				
				}	
			}
			
			
			
		}
		for (int i=0; i<toRemove.size(); i++){
			activeColIDs.remove(toRemove.get(i));
		}
		
		
		return endedCols;
		
	}
	*/



	/**
	 * Processes each TrackMatch, either extending the track with the match or ending tracks with no matches.
	 * <p>
	 * Special care is taken to maintain bookkeeping for collisions
	 * @return Current number of active tracks 
	 */
	private int extendOrEndMatches(){
		
		ListIterator<TrackMatch> mIt = matches.listIterator();
		while (mIt.hasNext()) {
			TrackMatch match = mIt.next();
			
			if (match.getTopMatchPoint()==null) {
				//End the match/track
				finishedTracks.addElement(match.track);
				trackMessage.message(match.track.infoString(), VerbLevel.verb_message);
				activeTracks.remove(match.track);
				
			} else if (match.track.getNumPoints()==0) {
				//THIS IS IMPORTANT!
				//if there's a new track created by a collision starting/ending, add it to activeTracks & remove the pt from points 
				match.track.extendTrack(match.getTopMatchPoint());
//				match.track.markCollision(frameNum, null);
				
				activeTracks.add(match.track);
				rmActivePt(match.track.getEnd());
//				activePts.remove(match.track.getEnd());
				
			} else {
				match.track.extendTrack(match.getTopMatchPoint());
				//If the track is in a collision, mark the new point 
				//THIS MAY BE MODIFIED vvvv
//				if (match.track.isCollision.get(match.track.isCollision.size()-1)){
//					match.track.markCollision(frameNum, null);
//				}
				
				rmActivePt(match.track.getEnd());
//				activePts.remove(match.track.getEnd());
						
				//activePts.remove(match.getTopMatchPoint());
			}
		}
		 
		numTracks = activeTracks.size();
		
		return numTracks;
	}
	
	/**
	 * Creates a new Track for any points which were not matched to any tracks
	 * @return The number of new tracks
	 */
	private int startNewTracks(){
		
		int numNew=0;
		ListIterator<TrackPoint> tpIt = activePts.listIterator();
		while (tpIt.hasNext()) {
			TrackPoint pt = tpIt.next();
			comm.message("Getting num of matches for point "+pt.pointID, VerbLevel.verb_debug);
			
				comm.message("Adding a new track", VerbLevel.verb_debug);
				activeTracks.addElement(new Track(pt, this));
				numNew++;
			if (pt.getNumMatches()!=0) {
				comm.message("TrackPoint "+pt.pointID+" has TrackMatches, but remained active after matches were added to tracks", VerbLevel.verb_warning);
			}
		}
		
		return numNew;
	}
	
	protected void rmActivePt(TrackPoint pt){
		pt.strip();
		activePts.remove(pt);
	}
	
	/**
	 * Finds the index in the given Track list of the specified track ID
	 * @param trackID The track to be found
	 * @param trackList The list to search through 
	 * @return The index of list that can be used to access the query track (-1 if it's not found)
	 */
	public static int findIndOfTrack(int trackID, Vector<Track> trackList){
		
		for (int i=0;i<trackList.size();i++){
			if (trackList.get(i).getTrackID()==trackID){
				return i;
			}
		}
		return -1;
		
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
	
	public double meanAreaOfTracks(Vector<Track> tr){
		
		double totalA = 0;
		int num = 0;
		
		ListIterator<Track> trIt = tr.listIterator();
		while (trIt.hasNext()) {
			num++;
			totalA += trIt.next().getEnd().area;
		}
		
		return totalA/num; 
	}
	
	
	
	public Experiment toExperiment(){
		//Clean up the TrackBuilder
		if (!activeTracks.isEmpty()){
			finishedTracks.addAll(activeTracks);
			activeTracks.removeAll(activeTracks);
		}
		if (!activeColIDs.isEmpty()){
			finishedColIDs.addAll(activeColIDs);
			activeColIDs.removeAll(finishedColIDs);
		}
		
		//Create the Experiment
		Experiment exp = new Experiment(this);
		
		//Attach the experiment to all the tracks 
//		ListIterator<Track> trIt = exp.tracks.listIterator();
//		while(trIt.hasNext()) {
		for (int i=0; i<exp.getNumTracks(); i++){
			exp.getTrackFromInd(i).exp = exp;
//			trIt.next().exp = exp;
		}
		
		
		return exp;
	}
	
	public void showCommOutput(){
		if (ep.dispTrackInfo){
			if (!trackMessage.outString.equals("")){
				new TextWindow("Track info", trackMessage.outString, 500, 500);
			}
			if (!comm.outString.equals("")){
				new TextWindow("Builder info", comm.outString, 500, 500);
			}
				
		}
		
	}
	

}
