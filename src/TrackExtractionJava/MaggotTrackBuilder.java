package TrackExtractionJava;

import java.util.Vector;

import ij.ImageStack;
import ij.text.TextWindow;


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
		if (ep.calcDerivs){
			calcImDerivs();
		}
	}

	/**
	 * Orients all the tracks so that all maggots have their head in the direction of motion
	 */
	protected void orientMaggots(){

		Communicator c = new Communicator();
		c.setVerbosity(VerbLevel.verb_off);
		for (int i=0; i<finishedTracks.size(); i++){
			orientMaggotTrack(finishedTracks.get(i), c);  
		}
		if (!c.outString.equals("")) new TextWindow("Orientation debugging output", c.outString, 500, 500);
	}

	/**
	 * Orients the maggots in a track in the direction of their motion
	 * @param track Track to be oriented
	 */
	protected static void orientMaggotTrack(Track track, Communicator c){
		
		omt(track, c);
//		orientMaggotTrack(track.getPoints(), comm, track.getTrackID());
	}
		
	
	protected static void omt(Track track){
		omt(track, null);
	}
	
	protected static void omt(Track track, Communicator c){
		
		if (c!=null) c.message("Track "+track.getTrackID(), VerbLevel.verb_debug);

		boolean debug = false; // (track.trackID<10 || track.points.size()<150);
		
		Vector<? extends TrackPoint> points = track.getPoints();
		
		Vector<Segment> segList = findSegments(points);
		if (segList==null){
			return;//Points are not MTPs 
		}
		if (debug){
			String s = "Track "+track.getTrackID()+"\n";
			for (int i=0; i<segList.size(); i++){
				s += segList.get(i).start+"-"+segList.get(i).end+"\n";
			}
			if (segList.size()==0){
				s+="Emtpy list";
			} else {
				
			}
			new TextWindow("Track "+track.getTrackID(), s, 500, 500);
			
		}
		
		for(Segment seg: segList){
			alignSegment(points, seg);
		}
		for(Segment seg: segList){
//			orientSegment(points, seg);
			if (c!=null) c.message("Orienting segment #"+segList.indexOf(seg)+"/"+segList.indexOf(segList.lastElement())+",  ("+seg.length()+"pts)", VerbLevel.verb_debug);
			orientSegment(points, seg, c);
		}
		
//		int i=0;
//		while (i<segList.size()){
//			orientSegment(points, segList.get(i));
//			i++;
//		}
//		
	}
	
	
	protected static Vector<Segment> findSegments(Vector<? extends TrackPoint> points){
		
		Vector<Segment> segList = null; 
		
		if (points!=null && points.size()>0 && (points.get(0) instanceof MaggotTrackPoint)){
			
			segList = new Vector<Segment>();
			
			MaggotTrackPoint pt;
			int i=0;
			while (i<points.size()){
				
				pt = (MaggotTrackPoint)points.get(i);
				
				if (pt.midline!=null && pt.htValid){//Find the segment starting here && pt.midline.getNCoordinates()!=0
					
					int segStart = i;
					boolean notFound = true;
					while (notFound && i<points.size()){
						i++;
						//pt2 = (MaggotTrackPoint)points.get(i);
						if (i==points.size() || ((MaggotTrackPoint)points.get(i)).midline==null){// || pt2.midline.getNCoordinates()==0 || !pt2.htValid
							//END SEGMENT
							notFound = false;
							Segment newSeg = new Segment(segStart, i-1);
							if (segList.size()!=0){
								newSeg.prevSeg = segList.lastElement();
								segList.lastElement().nextSeg = newSeg;
							}
							segList.add(newSeg);
						}
					}
				}//leave here with i->null midline (or after end)
				
				i++;//increment past null midline
			}
			
		}
		return segList;
	}
	
	protected static void alignSegment(Vector<? extends TrackPoint> points, Segment seg){
		
		MaggotTrackPoint prevPt = (MaggotTrackPoint) points.get(seg.start);
		MaggotTrackPoint pt;
		for (int i=(seg.start+1); i<=seg.end; i++){
			pt = (MaggotTrackPoint) points.get(i);
			pt.orientMTP(prevPt);
			prevPt = pt;
		} 
	}
	
	
	protected static int orientSegment(Vector<? extends TrackPoint> points, Segment seg){
		return orientSegment(points, seg, null);
	}

	
	protected static int orientSegment(Vector<? extends TrackPoint> points, Segment seg, Communicator c){

		if(seg.length()>=2){
			//Orient the segment to the direction of motion
			double dpSum=0;
			MaggotTrackPoint prevPt = (MaggotTrackPoint) points.get(seg.start);
			MaggotTrackPoint pt;
			for (int i=(seg.start+1); i<=seg.end; i++){
				pt = (MaggotTrackPoint) points.get(i);
				dpSum += pt.MaggotDotProduct(prevPt);
				prevPt = pt;
			}
			
			if (dpSum<0){
				if (c!=null) c.message("Long segment, flipped", VerbLevel.verb_debug);
				flipSeg(points, seg.start, seg.end);
			} else {
				if (c!=null) c.message("Long segment, unflipped", VerbLevel.verb_debug);
			}
			
			return 1;

		} else {
			
			//1-point-long segment: try to orient it to the surrounding points
			if (seg.prevSeg!=null){
				if (c!=null) c.message("Short segment, aligned to prev", VerbLevel.verb_debug);
				//Align this point to the last point in the previous segment
				((MaggotTrackPoint)points.get(seg.start)).orientMTP((MaggotTrackPoint)points.get(seg.prevSeg.end));
				return 1;
			} else if(seg.nextSeg!=null){
				if (c!=null) c.message("Short segment, aligning to next...", VerbLevel.verb_debug);
				//Orient the next segment, then align this point to that segment
				int num = orientSegment(points,seg.nextSeg);
				int flip = ((MaggotTrackPoint)points.get(seg.start)).chooseOrientation((MaggotTrackPoint)points.get(seg.nextSeg.end),false);
				if (flip==1){
					if (c!=null) c.message("...FLIPPED", VerbLevel.verb_debug);
					((MaggotTrackPoint)points.get(seg.start)).invertMaggot();
				} else{
					if (c!=null) c.message("...UNFLIPPED", VerbLevel.verb_debug);
				}
				return 1+num;
			} else {
				if (c!=null) c.message("Short segment, UNORIENTED", VerbLevel.verb_debug);
				//Could not orient...must be a short weird track. Advance past this segment
				return 1;
			}
			
		}
	}
	
	
	
	
	protected static void orientMaggotTrack(Vector<? extends TrackPoint> points, Communicator comm, int trackID){
		
		
		if (points!=null && points.size()>0 && (points.get(0) instanceof MaggotTrackPoint)){//&& track.points.get(0).pointType>=2
			
			MaggotTrackPoint prevPt = (MaggotTrackPoint)points.get(0);
			MaggotTrackPoint pt;
			
			int AMDSegStart = (prevPt.midline!= null && prevPt.midline.getNCoordinates()!=0 && prevPt.htValid) ? 0 : -1;
			int AMDSegEnd = -1;
			
			for (int i=1; i<points.size(); i++){
			
				pt = (MaggotTrackPoint)points.get(i);
				
				if (pt.midline!= null && pt.midline.getNCoordinates()!=0 && pt.htValid) {
					//If a valid midline exists, align it with the last valid point
					
					int orStat = pt.orientMTP(prevPt);
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
			if (comm!=null) comm.message("Direction Analyisis Error: Track has "+points.size()+" points, startInd="+startInd+", endInd="+endInd, VerbLevel.verb_message);
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
	
	
	protected void calcImDerivs(){
		for (Track t : finishedTracks){
			
			for (int i=0; i<ep.imDerivWidth; i++){
				if (ep.imDerivType==ExtractionParameters.DERIV_FORWARD && (i+ep.imDerivWidth)<t.getNumPoints()){
					((ImTrackPoint)t.getPoint(i)).calcImDeriv(null, (ImTrackPoint)t.getPoint(i+ep.imDerivWidth), ep.imDerivType);
				} else {
					//For now, do nothing
				}
			}
			
			for (int i=ep.imDerivWidth; i<(t.getNumPoints()-ep.imDerivWidth); i++){
				//Calculate the deriv of the point's im 
				((ImTrackPoint)t.getPoint(i)).calcImDeriv((ImTrackPoint)t.getPoint(i-ep.imDerivWidth), (ImTrackPoint)t.getPoint(i+ep.imDerivWidth), ep.imDerivType);
			}
			
			
			for (int i=(t.getNumPoints()-ep.imDerivWidth); i<t.getNumPoints(); i++){
				if (ep.imDerivType==ExtractionParameters.DERIV_BACKWARD && (i-ep.imDerivWidth)>=0){
					((ImTrackPoint)t.getPoint(i)).calcImDeriv((ImTrackPoint)t.getPoint(i-ep.imDerivWidth), null, ep.imDerivType);
				} else {
					//For now, do nothing
				}
			}
			
			
		}
	}
		
	
}

class Segment{
	
	int start;
	int end;
	
	Segment prevSeg;
	Segment nextSeg;
	
	public Segment(int s, int e){
		start = s;
		end = e;
	}
	
	public int length(){
		return end-start+1;
	}
}
