import java.util.Vector;

import ij.ImageStack;


public class MaggotTrackBuilder extends TrackBuilder {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public MaggotTrackBuilder(ImageStack IS, ExtractionParameters ep) {
		super(IS, ep);
	}
	
	/**
	 * Builds the tracks with MaggotTrackPoints 
	 */
	public void run(){
		
		ep.trackPointType=2;//Make sure the track is loaded as MaggotTrackPoints
		buildTracks();
		orientMaggots();
	}

	/**
	 * Orients all the tracks so that all maggots have their head in the direction of motion
	 */
	protected void orientMaggots(){
		for (int i=0; i<finishedTracks.size(); i++){
			orientMaggotTrack(finishedTracks.get(i), comm);  
		}
	} 

	/**
	 * Orients the maggots in a track in the direction of their motion
	 * @param track Track to be oriented
	 */
	protected static void orientMaggotTrack(Track track, Communicator comm){
		orientMaggotTrack(track.points, comm, track.trackID);
	}
		
		
	protected static void orientMaggotTrack(Vector<? extends TrackPoint> points, Communicator comm, int trackID){
		
		
//		if (ep.trackPointType!=2){
//			//Load points as MaggotTrack points
//			
//		}
		
		if (points!=null && points.size()>0 && (points.get(0) instanceof MaggotTrackPoint)){//&& track.points.get(0).pointType>=2
			
			MaggotTrackPoint pt;
			MaggotTrackPoint prevPt = (MaggotTrackPoint)points.get(0);
			
			int AMDSegStart = (prevPt.midline!= null && prevPt.midline.getNCoordinates()!=0 && prevPt.htValid) ? 0 : -1;
			int AMDSegEnd = -1;
			
			for (int i=1; i<points.size(); i++){
			
				pt = (MaggotTrackPoint)points.get(i);
				
				if (pt.midline!= null && pt.midline.getNCoordinates()!=0 && pt.htValid) {
					//If a valid midline exists, align it with the last valid point
					
					int orStat = pt.chooseOrientation(prevPt);
					if (orStat<0){
						if (comm!=null) comm.message("Orientation Failed, Track"+trackID+" frames "+(i+points.firstElement().frameNum), VerbLevel.verb_error);
					}
					prevPt = pt;
					
					//Set the new start frame when the last segment has just been analyzed
					//	->Because of initialization, this will happen on the first valid spine in the track
					if (AMDSegStart<0){//AMDSegEnd==lastEndFrameAnalyzed && AMDSegStart<0){//if (AMDSegStart==AMDSegEnd){
						AMDSegStart=i;
					}
					//Always set this as the last frame of the ending segment
					AMDSegEnd=i;
					
					
				} else  { 
					//When the midline doesn't exist, analyze the previous segment of midlines
					//But only if that segment has at least 2 points
					
					if (comm!=null) comm.message("Midline invalid, frame "+(i+points.firstElement().frameNum), VerbLevel.verb_message);
					
					//Analyze the direction of motion for the segment leading up to this frame, starting with lastEndFrameAnalyzed (or 0)
					if ( AMDSegStart!=-1 && (AMDSegEnd-AMDSegStart)>1 ){ //TODO && (AMDSegEnd-AMDSegStart)<AMDSegEnd (?)
						analyzeMaggotDirection(points, AMDSegStart, AMDSegEnd, comm, trackID);
					}
					
					//Regardless, acknowledge the empty spine so that the next valid segment is analyzed correctly 
					AMDSegStart = -1;
					
				}
			}
			
			//Catch the case when there were no gaps, so the direction was never analyzed
//			if (lastEndFrameAnalyzed==-1){
				//Analyze the direction of motion for the whole track
				analyzeMaggotDirection(points, AMDSegStart, AMDSegEnd, comm, trackID);
//			}
			
			
		} else {
			if (comm!=null) comm.message("Track was not oriented", VerbLevel.verb_error);
			if (comm!=null && !(points.get(0) instanceof MaggotTrackPoint)) comm.message("TrackPoints not MTPs", VerbLevel.verb_error);
		}
		
		
		
	}
	
	/**
	 * Checks if the segment of MaggotTrackPoints is oriented in the direction of motion
	 * @param track Track to be oriented
	 * @param startInd Starting INDEX (not frame) to be oriented
	 * @param endInd Ending  INDEX (not frame) to be oriented
	 */
	protected static void analyzeMaggotDirection(Vector<? extends TrackPoint> points, int startInd, int endInd, Communicator comm, int trackID){
		
		if (points.isEmpty() || startInd<0 || endInd<0 || startInd>=endInd){
			comm.message("Direction Analyisis Error: Track has "+points.size()+" points, startInd="+startInd+", endInd="+endInd, VerbLevel.verb_message);
			return;
		}
		
		if (comm!=null) comm.message("Analyzing midline direction: Track "+trackID+" "+(startInd+points.firstElement().frameNum)+"-"+(endInd+points.firstElement().frameNum), VerbLevel.verb_debug);
		
		double dpSum=0;
		MaggotTrackPoint pt;
		MaggotTrackPoint prevPt = (MaggotTrackPoint) points.get(startInd);
		for (int i=startInd+1; i<=endInd; i++){
			pt = (MaggotTrackPoint) points.get(i);
			dpSum += pt.MaggotDotProduct(prevPt);
			prevPt = pt;
		} 
		
		
		if (dpSum<0){
			flipSeg(points, startInd, endInd);
		}
		
		
	}
	
	/**
	 * Flips the orientation of every maggot head/tail/midline in the segment
	 * @param track
	 * @param startInd
	 * @param endInd
	 */
	protected static void flipSeg(Vector<? extends TrackPoint> points, int startInd, int endInd) {
		for (int i=startInd; i<=endInd; i++){
			MaggotTrackPoint pt = (MaggotTrackPoint) points.get(i);
			pt.invertMaggot();
		}
	}
	
	
	
}
