
public class TrackBuilder {
	//activeTracks
	//finishedTracks
	//collisions
	
	//activePts
	//trackMatch
	//pointMatchList
	
	
	
	//TODO Build Tracks:
	//For each frame:
		//Load points: LOADPOINTS(BELOW)
			//Extract Points POINTEXTRACTOR
			//Activate Points ACTIVATEPOINTS(BELOW)
		//Extend Tracks: TRACKEXTENDER(BELOW)
			//Activate Tracks: (maintain Lookup table)
				//Build Matches TRACKMATCHER (option to cut off by length?)
				//Modify Matches (length, collision repair)
			//Fuse Tracks
				//Extend 1-1 matches and collisions
				//Start new tracks (unmatched points), move to active
				//End dead tracks (unmatched tracks), move to finished
		
	//TODO loadPoints
		//rawPts<-- Extract Points POINTEXTRACTOR
		//activePts<--[rawPts,tracks] Activate Points ACTIVATEPOINTS(BELOW)
	
	//TODO activatePoints
	//For each point:
		//Add point to activePts 
		//Count point
		//xxxxx DON'T Match to contending tracks ->trackMatch TRACKMATCH
	//Set lookup table length = number of points
	
	
	
	
	//TODO trackExtender
		//Activate Tracks
		//Fuse Tracks
	
	
	
	//TODO activateTracks
		//matches<--Build Matches TRACKMATCH (option to cut off by length?; maintain table)
		//matches<--Modify Matches (length, collision repair; maintain table)
	
	//TODO buildMatches
		//For each track,
			//construct a TRACKMATCH (activetracks, collisiontracks)
	
	//TODO modifyMatches
		//Remove matches that are too far CUTMATCHESBYDISTANCE
		//Handle collisions
	
	//TODO cutMatchesByDistance
		//If trackmatch.dist is too great, deleete it
	
	//TODO handleCollisions
		//Detect new collisions
		//Release disconnected collisions to ActiveTracks
		//Repair/Flag bad collisions
	
	//TODO detectNewCollisions
	
	//TODO releaseCollisions
	
	//TODO repairBadCollisions
		//Detect bad collisions (no 2-2 matches) 
		//Try to match to empty points nearby
		//Try to split the one image to two
		//Mark bad collisions (to widen range of points nearby)
	
	//TODO detectBadCollisions
	
	//TODO matchCollisionWithEmptyPoint
	
	//TODO splitImagetoMultiplePoints
	
	
	//TODO fuseTracks
		//Extend 1-1 matches and good collision matches
		//Start new tracks (unmatched points), move to active
		//End dead tracks (unmatched tracks), move to finished
	
	
	//TODO fuseTrackMatches
	
	//TODO startNewTracks
	
	//TODO endDeadTracks

	

}
