package TrackExtractionJava;

import ij.gui.PolygonRoi;
import ij.process.FloatPolygon;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ListIterator;
import java.util.Vector;

public class BBFPointListGenerator {

	BackboneFitter bbf;
	
	Track workingTrack;
	int startFrame;
	int endFrame;
	int startInd;
	int endInd;
	boolean subset;
	private Vector<BackboneTrackPoint> BTPs;
	FittingParameters params;
	Communicator comm;
	
	
	protected boolean clipEnds;// = false;
	protected int BTPstartFrame;// = -1;
	protected int BTPendFrame;// = -1;
	protected Vector<TrackPoint> startClippings;
	protected Vector<TrackPoint> endClippings;
	
	
	public BBFPointListGenerator(BackboneFitter bbf, Track track, FittingParameters fp, Communicator comm) {
		this.bbf = bbf;
		workingTrack = track;
		reset();
		turnOffSubsets();
		params = fp;
		this.comm = comm;
		
	}
	
	protected void reset(){
		clipEnds = false;
		BTPstartFrame = -1;
		BTPendFrame = -1;
	}
	
	public void turnOffSubsets(){
		startInd = 0;
		endInd = workingTrack.getNumPoints()-1;
		startFrame = workingTrack.points.firstElement().frameNum;
		endFrame = workingTrack.points.lastElement().frameNum;
		subset = false;
	}
	
	/**
	 * Generates a list of BTPs from the original trackPoint list, with the
	 * proper grain
	 * 
	 * @param grain
	 *            The spacing between points of the original track
	 * @return Flag indicating no error (true) or error (false)
	 */
	public boolean generateBTPList(int pass) {

		BTPs = new Vector<BackboneTrackPoint>();
		
		try {
			
			comm.message("Sampling points", VerbLevel.verb_debug);
			sampleTrackPoints(params.grains[pass]);
			boolean noError = addBackboneInfo(pass, params.grains[pass]);
			
			return noError;

		} catch (Exception e) {
			
			StringWriter sw = new StringWriter();
			PrintWriter pw = new PrintWriter(sw);
			e.printStackTrace(pw);
			comm.message("Problem getting BTPS from the track at grain "+params.grains[pass]+" \n" + sw.toString(), VerbLevel.verb_error);
			return false;
		}
	}
	
	
	/*
	public boolean generateBTPList(int pass, FittingParameters fp){
		
		FittingParameters p = params;
		params = fp;
		boolean b = generateBTPList(pass);
		params = p;
		return b;
	}
	*/
	
	
	public boolean generateFullBTPList(){

		BTPs = new Vector<BackboneTrackPoint>();
		
		int s = startInd;
		int e = endInd;
		startInd = 0;
		endInd = workingTrack.points.size()-1;
		
		try {
			
			comm.message("Sampling points", VerbLevel.verb_debug);
			sampleTrackPoints(1);
			boolean noError = addBackboneInfo(0,1);
			
			return noError;

		} catch (Exception ex) {
			
			StringWriter sw = new StringWriter();
			PrintWriter pw = new PrintWriter(sw);
			ex.printStackTrace(pw);
			comm.message("Problem getting BTPS from the track at grain 1 \n" + sw.toString(), VerbLevel.verb_error);
			return false;
		} finally {
			startInd = s;
			endInd = e;
		}
	}
	
	/**
	 * Stores a sampled list of backboneTrackPoints that are sampled at the given grain
	 * @param grain Spacing between trackPoints
	 */
	private void sampleTrackPoints(int grain){
		
//		int numTPs = (endInd-startInd)/grain;
		int numTPs = workingTrack.getNumPoints()/grain;
		try {
			
			for (int i=0; i<numTPs; i++){

				BTPs.add((BackboneTrackPoint)workingTrack.getPoint(i*grain));
//				BTPs.add((BackboneTrackPoint)workingTrack.getPoint(startInd+i*grain));
				
			}
			
		} catch (Exception e){
			StringWriter sw = new StringWriter();
			PrintWriter pw = new PrintWriter(sw);
			e.printStackTrace(pw);
			comm.message("Error sampling trackPoints\n"+sw.toString(), VerbLevel.verb_error);
		}
		
	}

	
	private boolean addBackboneInfo(int pass, int grain){
		
		float[] origin = new float[2];
		try {
			if (pass==0){

				for (int i=0; i<BTPs.size(); i++){
					if (params.leaveFrozenBackbonesAlone && BTPs.get(i).frozen){
						//do nothing
					} else if ((params.leaveBackbonesInPlace && BTPs.get(i).backbone!=null && BTPs.get(i).backbone.getNCoordinates()>0)){
						origin[0]=0;
						origin[1]=0;
						BTPs.get(i).setBackboneInfo(params.clusterMethod, BTPs.get(i).backbone, origin);
					} else {
						origin[0] = BTPs.get(i).rect.x;
						origin[1] = BTPs.get(i).rect.y;
						BTPs.get(i).setBackboneInfo(params.clusterMethod, BTPs.get(i).midline, origin);
					}
					
					comm.message("Adding backbone info to BTP "+i+"(frame "+BTPs.get(i).frameNum+")", VerbLevel.verb_debug);
				}
				
				boolean noError = cleanUpBTPs(findEmptyMids(), params.minFlickerDist*grain);
				return noError;
				
			} else {
				origin[0] = 0;
				origin[1] = 0;
				//The old spines are already in the BTPs from the previous pass; find the empty ones and interpolate
				int prev = 0;//startInd; 
				int count=0;
				int relativeGrain=params.grains[pass-1]/params.grains[pass];
				Vector<FloatPolygon> interpdBBs;
				
				for (int i=(prev+1); i<BTPs.size() ;i++){//i<endInd; i++){
					count++;
					if(count==relativeGrain && BTPs.get(i).backbone!=null){// && !BTPs.get(i).backbone.equals(new PolygonRoi(BTPs.get(i).bbInit, PolygonRoi.POLYLINE))){
						interpdBBs = interpBackbones(i-prev-1, origin, origin, BTPs.get(prev).backbone.getFloatPolygon(), BTPs.get(i).backbone.getFloatPolygon());
						//fill in the midlines
						for (int j=0; j<interpdBBs.size(); j++){
							if (params.leaveFrozenBackbonesAlone && BTPs.get(i).frozen){
//								float[] o = origin;
//								origin[0] = BTPs.get(i).rect.x;
//								origin[1] = BTPs.get(i).rect.y;
//								BTPs.get(prev+1+j).setBackboneInfo(params.clusterMethod, BTPs.get(prev+1+j).backbone, origin);
//								origin = o;
							} else {
								BTPs.get(prev+1+j).setBackboneInfo(params.clusterMethod, new PolygonRoi(interpdBBs.get(j), PolygonRoi.POLYLINE), origin);
							}
						}
						prev = i;
						count = 0;
					}
				}
				
				if (prev!=(BTPs.size()-1)){
					for (int i=(prev+1); i<BTPs.size(); i++){
						origin[0] = BTPs.get(i).rect.x;
						origin[1] = BTPs.get(i).rect.y;
						BTPs.get(i).setBackboneInfo(params.clusterMethod, BTPs.get(i).midline, origin);
					}
					return cleanUpBTPs(findEmptyMids(), params.minFlickerDist*grain);
				}
				workingTrack.setVarianceFromHTdist(2);//TODO make parameter? sets sqrt(variance) to 1/2 distance between backbone points
				
				return true;
			}
			
		} catch (Exception e){
			StringWriter sw = new StringWriter();
			PrintWriter pw = new PrintWriter(sw);
			e.printStackTrace(pw);
			if (pass==0){
				comm.message("Error adding backbone info (getting offsets, prepping backbone-generating data) \n"+sw.toString(), VerbLevel.verb_error);
			} else {
				comm.message("Error adding backbone info (interpolating backbones, prepping backbone-generating data) \n"+sw.toString(), VerbLevel.verb_error);
			}
			return false;
		}
	}
	
	private boolean[] findEmptyMids(){
		
		boolean[] sampledEmptyMids = new boolean[BTPs.size()];
		
		//leaveBackbonesInPlace, leaveFrozenBackbonesAlone
		
		for (int i=0; i<BTPs.size(); i++){
			if (params.leaveBackbonesInPlace || (params.leaveFrozenBackbonesAlone && BTPs.get(i).frozen) ){
				sampledEmptyMids[i] = (BTPs.get(i).backbone==null);// TODO or diverged?
			} else {
				sampledEmptyMids[i] = (BTPs.get(i).midline==null);
			}
		}
		
		return sampledEmptyMids;
		
	}
	
	private boolean cleanUpBTPs(boolean[] sampledEmptyMids, double flickerDist){
		if(sampledEmptyMids!=null){
				comm.message("Clearing flickers", VerbLevel.verb_debug);
			clearFlickerMids(sampledEmptyMids, flickerDist);
			
				comm.message("Finding gaps", VerbLevel.verb_debug);
			Vector<Gap> gaps = findGaps(sampledEmptyMids);
				comm.message("Cleaning gaps", VerbLevel.verb_debug);
			sanitizeGaps(gaps);
			MaggotTrackBuilder.orientMaggotTrack(BTPs, comm, workingTrack.getTrackID());
				comm.message("Filling midlines", VerbLevel.verb_debug);
			boolean noError = fillGaps(gaps);
			if (!noError){
				return false;
			}
			if (clipEnds) {
				return clipEnds();
			}
		}
		return true;
	}
	
	private void clearFlickerMids(boolean[] emptyMidlines, double minFlickerDist){
		BackboneTrackPoint prevMag = null;
		BackboneTrackPoint currMag;
		double dist = -1;;
		
		int currInd = 0;
		ListIterator<BackboneTrackPoint> btpIt = BTPs.listIterator();
		while(btpIt.hasNext()){
			
			currMag = btpIt.next();
			if(prevMag!=null) {
				dist = currMag.bbInitDist(prevMag.bbInit);
			} else {
				dist = -1;
			}
			
			if (dist>0 && dist>minFlickerDist) {
				emptyMidlines[currInd] = true;
				String s="";
				if (dist>0) s="too far"; else s="null dist";
				comm.message("Flicker ("+s+") at "+(BTPs.get(currInd).frameNum), VerbLevel.verb_debug);
			}
			
			prevMag = currMag;
			currInd++;
		}
		
		
	}
	
	
	private Vector<Gap> findGaps(boolean[] emptyMidlines){
		
		Vector<Gap> gaps = new Vector<Gap>();
		
		//Build the gap list
		int gapStart = -1;
		int ptr = 0;
		while (ptr < emptyMidlines.length) {

			if (emptyMidlines[ptr] && (!params.leaveFrozenBackbonesAlone || !BTPs.get(ptr).frozen)) {
				comm.message("Gap starting at frame "+(ptr+BTPs.firstElement().frameNum), VerbLevel.verb_debug);
				gapStart = ptr;
				// Find the end of the gap
				do ++ptr; while (ptr < emptyMidlines.length && emptyMidlines[ptr]);
				//Make a new gap
				gaps.add(new Gap(gapStart, ptr-1));
				
			} else {
				++ptr;
			}
		}

		return gaps;
	}
	
	private void sanitizeGaps(Vector<Gap> gaps){
		
				
		if (gaps.size()>1){
			
			comm.message("Merging "+gaps.size()+" gaps", VerbLevel.verb_debug); 
			
			dilateGaps(gaps, params.gapDilation, params.minValidSegmentLen, 0, BTPs.size()-1, params.dilateToEdges);
			//dilateGaps(gaps, params.gapDilation, params.minValidSegmentLen, track.getStart().frameNum, track.getEnd().frameNum, params.dilateToEdges);
			
			if (mergeGaps(gaps, params.minValidSegmentLen, comm)) {
				invalidateGaps(gaps);
//				MaggotTrackBuilder.orientMaggotTrack(BTPs, comm, track.getTrackID());
			}
			
			comm.message("After sanitizing, there are "+gaps.size()+" gaps", VerbLevel.verb_debug); 
		
		} else {
			comm.message("Only one gap, no need to sanitize", VerbLevel.verb_debug);
		}
		
	}
	
	protected static void dilateGaps(Vector<Gap> gaps, int dilation, int minValidSegmentLen, int startFrame, int endFrame, boolean dilateToEdges){
		
		for (Gap g : gaps){
			g.start -= dilation;
			g.end += dilation;
		}
		
		if (gaps.firstElement().start<startFrame || ( dilateToEdges && (gaps.firstElement().start-startFrame)<minValidSegmentLen ) ){
			gaps.firstElement().start=startFrame;
		}
		if (gaps.lastElement().end>endFrame || ( dilateToEdges && (endFrame-gaps.lastElement().end)<minValidSegmentLen ) ){
			gaps.lastElement().end=endFrame;
		}
		
	}
	
	protected static boolean mergeGaps(Vector<Gap> gaps, int minValidSegmentLen, Communicator comm){
		
		if (gaps.size()<2){
			return false;
		}
		
		boolean gapsChanged = false;
		
		Gap prevGap;
		Gap currGap = null;
		
		ListIterator<Gap> gIt = gaps.listIterator();
		prevGap = gIt.next();//First gap
		
		do{
			//Always advance the next gap
			currGap = gIt.next();
			
			if (comm!=null) comm.message("Checking gaps "+prevGap.start+"-"+prevGap.end+" & "+currGap.start+"-"+currGap.end, VerbLevel.verb_debug);
			
			if (prevGap.distTo(currGap)<minValidSegmentLen || currGap.start<=prevGap.end){ 
				if (comm!=null) comm.message("Merging gaps", VerbLevel.verb_debug);
				prevGap.merge2Next(currGap); //Move currGap into prevGap
				gIt.remove(); //Remove currGap from the list
				//Do not advance prevGap, so you compare the next gap to the merged gap
				gapsChanged = true;
			} else {
				//Advance prevGap
				prevGap = currGap;
			}
			
		} while (gIt.hasNext());
		
		return gapsChanged;
	}

	private void invalidateGaps(Vector<Gap> gaps){
		for (int i=0; i<gaps.size(); i++){
			Gap gap = gaps.get(i);
			for (int j=gap.start; j<=gap.end; j++){
				BackboneTrackPoint btp = BTPs.get(j);
				btp.bbvalid = false;
			}
		}
	}
	
	
	/**
	 * Finds and fills all the empty midlines
	 * 
	 * @param emptyMidlines
	 *            A list of booleans indicating whether or not the midline in
	 *            the corresponding BTP is empty
	 */
	private boolean fillGaps(Vector<Gap> gaps) {

		
		ListIterator<Gap> gIt = gaps.listIterator();
		Gap g;
		while(gIt.hasNext()){
			g = gIt.next();
			comm.message("Filling gap at TP "+g.start+"-"+g.end, VerbLevel.verb_debug);
			boolean noError = fillGap(g.start, g.end);
			if(noError){
				comm.message("Filled successfully", VerbLevel.verb_debug);
			} else {
				comm.message("Error filling gap", VerbLevel.verb_debug);
				return false;
			}
		}
		return true;
	}
	
	/**
	 * Fills the specified gap in midlines. If the gap is small enough
	 * (according to FittingParameters), the previous (or following midline) is
	 * carried through the gap. Otherwise, the midlines are interpolated from
	 * the surrounding midlines
	 * <p>
	 * False is returned when both the midlines surrounding a small gap, or one
	 * of the midlines surrounding a large gap, is at the beginning or end of
	 * the track
	 * 
	 * @param gapStart
	 *            Index of the BTP at the start of the gap
	 * @param gapEnd
	 *            Index of the BTP at the end of the gap
	 * @return false when a filling error occurs, otherwise true
	 */
	private boolean fillGap(int gapStart, int gapEnd) {
		try { 
			int gapLen = gapEnd - gapStart + 1;
			comm.message("Filling gap of size "+gapLen, VerbLevel.verb_debug);
			if (gapLen < params.smallGapMaxLen) {
				// Set the
				
				PolygonRoi fillerMidline;
				float[] origin;
				if (gapStart != 0) {
					fillerMidline = BTPs.get(gapStart - 1).midline;
					float[] o = {BTPs.get(gapStart - 1).rect.x, BTPs.get(gapStart - 1).rect.y};
					origin = o;
				} else if (gapEnd != (BTPs.size() - 1)) {
					fillerMidline = BTPs.get(gapEnd + 1).midline;
					float[] o = {BTPs.get(gapEnd + 1).rect.x, BTPs.get(gapEnd + 1).rect.y};
					origin = o;
				} else {
					return false;
				}
	
				for (int i = gapStart; i <= gapEnd; i++) {
					BTPs.get(i).fillInBackboneInfo(params.clusterMethod, fillerMidline, origin);
				}
	
			} else if (gapStart != 0 && gapEnd != (BTPs.size() - 1)) {
				comm.message("Filling large gap", VerbLevel.verb_debug);
				
				Vector<FloatPolygon> newMids = interpBackbones(gapStart - 1, gapEnd + 1);
				
				//Debug messages
				comm.message("Interpolation complete; Midlines:", VerbLevel.verb_debug);
				for (int i=0; i<newMids.size(); i++){
					FloatPolygon mid = newMids.get(i);
					String s = "";
					for (int j=0; j<mid.npoints; j++){
						float xmid = mid.xpoints[j];
						float ymid = mid.ypoints[j];
						s+= "("+xmid+","+ymid+") ";
					}
					comm.message(s, VerbLevel.verb_debug);
				}
				
				for (int i = gapStart; i <= gapEnd; i++) {
					float[] origin = {0.0f,0.0f};
					PolygonRoi newMid = new PolygonRoi(newMids.get(i-gapStart), PolygonRoi.POLYLINE);
					comm.message("Filling in midline "+i+"; new midline has "+newMid.getNCoordinates()+" pts", VerbLevel.verb_debug);
					BTPs.get(i).bf = bbf;
					BTPs.get(i).fillInBackboneInfo(params.clusterMethod, newMid, origin);
				}
				
				comm.message("Gap filled", VerbLevel.verb_debug);
			
			} else if (gapStart==0 && gapEnd == (BTPs.size()-1)){
				comm.message("All midlines are invalid in track "+workingTrack.getTrackID(), VerbLevel.verb_error);
				System.out.println("All midlines are invalid in track "+workingTrack.getTrackID());
				return false;
				
			} else {
				clipEnds=true;
				if (gapStart == 0) {
					BTPstartFrame=BTPs.get(gapEnd+1).frameNum;
				} else if (gapEnd == (BTPs.size()-1)){
					BTPendFrame=BTPs.get(gapStart-1).frameNum;
				}
			}
		}catch(Exception e){
			StringWriter sw = new StringWriter();
			PrintWriter pw = new PrintWriter(sw);
			e.printStackTrace(pw);
			comm.message("Error filling gap ("+gapStart+"-"+gapEnd+") in track "+workingTrack.getTrackID()+"\n"+sw.toString(), VerbLevel.verb_error);
		}
		return true;
	}


	private boolean clipEnds(){
		
		comm.message("Clipping ends on track "+workingTrack.getTrackID()+": startFrame="+BTPstartFrame+" endFrame="+BTPendFrame, VerbLevel.verb_message);
		
		int nFrames = (BTPendFrame>0)? BTPendFrame : workingTrack.points.lastElement().frameNum;
		nFrames -= (BTPstartFrame>0)? BTPstartFrame : workingTrack.points.firstElement().frameNum;
		if (nFrames<params.minTrackLen) {
			System.out.println("After clipping, track is too short");
			return false;
		}
		
		
		if (BTPendFrame>0){//new end frame
			
			//Clip the actual track
			endClippings = workingTrack.clipPoints(BTPendFrame+1,workingTrack.points.lastElement().frameNum+1);
			
			//Find the index of the endFrame BTP
			int i=BTPs.size();//i = index of first element to remove
			while (i>1 && BTPs.get(i-1).getFrameNum()!=BTPendFrame){
				i--;
			}
			
			if (BTPs.get(i-1).getFrameNum()==BTPendFrame){
				
				//invalidate BTPs
				for (int j=i; j<BTPs.size(); j++){
					BTPs.get(j).htValid = false;
					BTPs.get(j).finalizeBackbone();
				}
				//Remove elements
				BTPs.subList(i, BTPs.size()).clear();
				
				
				//TODO edit the subset in bbf
				
			} else {
				comm.message("Error clipping ends in track "+workingTrack.getTrackID()+": could not find index of new end frame ("+BTPendFrame+")", VerbLevel.verb_error);
				return false;
			}
			
			
		}
		
		if (BTPstartFrame>0){// New Start frame:
			
			startClippings = workingTrack.clipPoints(workingTrack.points.firstElement().frameNum,BTPstartFrame);
			
			//Find the index of last element to remove
			int i=-1; //i = index 
			while (i<(BTPs.size()-2) && BTPs.get(i+1).getFrameNum()!=BTPstartFrame){
				i++;
			}
			
			if (BTPs.get(i+1).getFrameNum()==BTPstartFrame && i>0){
				
				int frames2Remove = BTPs.get(i).frameNum-BTPs.firstElement().frameNum;
				//Invalidate BTPs
				for (int j=0; j<=i; j++){
					BTPs.get(j).htValid = false;
					BTPs.get(j).finalizeBackbone();
				}
				//Remove elements
				BTPs.subList(0, i+1).clear();
				
				if (bbf.bentLarvae!=null && bbf.bentLarvae.size()>0){
					Vector<Gap> gapsToRemove = new Vector<Gap>();
					for (Gap g : bbf.bentLarvae){
						g.subtract(frames2Remove);
						if (g.start<0){
							g.start=0;
						}
						if (g.end<0){
							gapsToRemove.add(g);
						}
						
					}
					bbf.bentLarvae.remove(gapsToRemove);
				}
				
				if (bbf.straightLarvae!=null && bbf.straightLarvae.size()>0){
					Vector<Gap> gapsToRemove = new Vector<Gap>();
					for (Gap g : bbf.straightLarvae){
						g.subtract(frames2Remove);
						if (g.start<0){
							g.start=0;
						}
						if (g.end<0){
							gapsToRemove.add(g);
						}
						
					}
					bbf.straightLarvae.remove(gapsToRemove);
				}
				
			} else {
				comm.message("Error clipping ends in track "+workingTrack.getTrackID()+": could not find index of new start frame ("+BTPstartFrame+")", VerbLevel.verb_error);
				return false;
			}
		}
		
		return true;
	}

	
	protected Vector<FloatPolygon> interpBackbones(int firstBTP, int endBTP) {
		
		BackboneTrackPoint fbtp = BTPs.get(firstBTP);
		BackboneTrackPoint ebtp = BTPs.get(endBTP);
		
		FloatPolygon bbFirst;// = BTPs.get(firstBTP).midline.getFloatPolygon();
		FloatPolygon bbEnd;// = BTPs.get(endBTP).midline.getFloatPolygon();

		float[] firstO;
		float[] endO;
		
		if (params.leaveBackbonesInPlace || (params.leaveFrozenBackbonesAlone && fbtp.frozen) ){
			float[] fO = {0,0};
			firstO = fO;
			bbFirst = fbtp.backbone.getFloatPolygon();
		} else {
			float[] fO = {fbtp.rect.x, fbtp.rect.y};
			firstO = fO;
			bbFirst = fbtp.midline.getFloatPolygon();
		}
			
		if (params.leaveBackbonesInPlace || (params.leaveFrozenBackbonesAlone && ebtp.frozen) ){	
			float[] eO = {0,0};
			endO = eO;
			bbEnd = ebtp.backbone.getFloatPolygon();
		} else {
			float[] eO = {ebtp.rect.x, ebtp.rect.y};
			endO = eO;
			bbEnd = ebtp.midline.getFloatPolygon();
		}
		
		return interpBackbones(endBTP - firstBTP - 1, firstO, endO, bbFirst, bbEnd);
	}
	
	
	protected static Vector<FloatPolygon> interpBackbones(int numnewbbs, float[] firstO, float[] endO, FloatPolygon bbFirst, FloatPolygon bbEnd) {
		
		if (numnewbbs<1) return null;
		
		int fnp = bbFirst.npoints;
		int enp = bbEnd.npoints;
		if (fnp!=enp){
			if (fnp>0 && enp>0){//arbitrarily chose to interpolate the first bb to the # bbPts of the second bb
				bbFirst = MaggotTrackPoint.getInterpolatedSegment(new PolygonRoi(bbFirst, PolygonRoi.POLYLINE), enp, true).getFloatPolygon();
			} else {
				return null;
			}
		}
		
		
		//Copy the coordinates of the surrounding BTPs so that they can be manipulated
		int numbbpts = bbFirst.npoints;
		float[] xbbfirst= new float[numbbpts];
		float[] ybbfirst= new float[numbbpts];
		float[] xbbend= new float[numbbpts];
		float[] ybbend= new float[numbbpts];
		for (int i=0; i<numbbpts; i++){//Get the absolute coordinates
			xbbfirst[i] = bbFirst.xpoints[i]+firstO[0];
			ybbfirst[i] = bbFirst.ypoints[i]+firstO[1];
			xbbend[i] = bbEnd.xpoints[i]+endO[0];
			ybbend[i] = bbEnd.ypoints[i]+endO[1];
		}
		
		float start;
		float end;
		
		// Find the initial shifts, aka origins of rotation
		float[] xorigins = new float[numnewbbs];
		float[] yorigins = new float[numnewbbs];
		start = xbbfirst[numbbpts-1];//tail of the first maggot
		end = xbbend[numbbpts-1];//tail of the second maggot
		xorigins = CVUtils.interp1D(start, end, xorigins.length);
		start = ybbfirst[numbbpts-1];//tail of the first maggot
		end = ybbend[numbbpts-1];//tail of the second maggot
		yorigins = CVUtils.interp1D(start, end, yorigins.length);
		
		//Shift both maggots so that their tails are at the origin
		for (int i=0; i<numbbpts; i++){
			xbbfirst[i] = xbbfirst[i]-xbbfirst[numbbpts-1];
			ybbfirst[i] = ybbfirst[i]-ybbfirst[numbbpts-1];

			xbbend[i] = xbbend[i]-xbbend[numbbpts-1];
			ybbend[i] = ybbend[i]-ybbend[numbbpts-1];
		}
		
		
		// Find the angles of rotations; rotate the bbFirst/End 
		float[] angles = new float[numnewbbs];
		start = (float)Math.atan2(ybbfirst[0], xbbfirst[0]);
		end = (float)Math.atan2(ybbend[0], xbbend[0]);
		float dif = (float)((end-start+2*Math.PI)%(2*Math.PI));
		if( ((start-end+2*Math.PI)%(2*Math.PI))<dif ) dif = -(float)((start-end+2*Math.PI)%(2*Math.PI));
		dif = dif/(numnewbbs+1);
		for(int j=0; j<angles.length; j++) angles[j]=start+dif*(j+1);
		
		//Rotate both maggots so that their heads are on the x axis
		float[] newCoord;
		for(int i=0; i<numbbpts; i++){
			newCoord = CVUtils.rotateCoord(xbbfirst[i], ybbfirst[i], -start);
			xbbfirst[i] = newCoord[0];
			ybbfirst[i] = newCoord[1];
			
			newCoord = CVUtils.rotateCoord(xbbend[i], ybbend[i], -end);
			xbbend[i] = newCoord[0];
			ybbend[i] = newCoord[1];
		}
		
		
		//Generate the new midline coords
		Vector<float[]> xnewbbs = new Vector<float[]>();
		Vector<float[]> ynewbbs = new Vector<float[]>();
		for (int j=0; j<numnewbbs; j++){
			xnewbbs.add(new float[numbbpts]);
			ynewbbs.add(new float[numbbpts]);
		}
		
		
		// Find the initial coords by interpolating between the shifted, rotated, initial backbones
		for (int i=0; i<numbbpts; i++){
			float[] xsubi = CVUtils.interp1D(xbbfirst[i], xbbend[i], numnewbbs);
			float[] ysubi = CVUtils.interp1D(ybbfirst[i], ybbend[i], numnewbbs);
			for (int j=0; j<numnewbbs;j++){//for each j'th backbone, fill in the i'th coordinate
				xnewbbs.get(j)[i] = xsubi[j];
				ynewbbs.get(j)[i] = ysubi[j];
			}
		}
		
		
		// Perform the back rotations and shifts
		for (int j=0; j<numnewbbs; j++){
			for (int i=0; i<numbbpts; i++){
				float[] newCrds = CVUtils.rotateCoord(xnewbbs.get(j)[i], ynewbbs.get(j)[i], angles[j]);
				xnewbbs.get(j)[i] = newCrds[0]+xorigins[j];
				ynewbbs.get(j)[i] = newCrds[1]+yorigins[j];
			}
		}
		
		//Generate the return objects
		Vector<FloatPolygon> newMids = new Vector<FloatPolygon>();
		for (int j=0; j<numnewbbs; j++){
			newMids.add(new FloatPolygon(xnewbbs.get(j), ynewbbs.get(j)));
		}
		
		return newMids;
	}

	
	
	
	public Vector<BackboneTrackPoint> getBTPs(){
		return BTPs;
	}
	
}
