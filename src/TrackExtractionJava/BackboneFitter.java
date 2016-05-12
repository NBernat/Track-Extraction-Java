package TrackExtractionJava;

import ij.gui.PolygonRoi;
import ij.process.FloatPolygon;
import ij.text.TextWindow;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Arrays;
import java.util.ListIterator;
import java.util.Vector;

/**
 * Fits backbones to a track of MaggotTrackPoints
 * 
 * @author Natalie Bernat
 *
 */
public class BackboneFitter {

	/**
	 * Fitting parameters
	 */
	protected FittingParameters params;

	/**
	 * Forces which act upon the backbones
	 */
	Vector<Force> Forces;

	/**
	 * The track that is being fit
	 */
	Track track;
	int newTrID=-1;
	
	Track errTrack=null;
	
	/**
	 * A List of (references to) the BTP's in track, worked on during fitting algorithm
	 */
	Vector<BackboneTrackPoint> BTPs;
	protected boolean clipEnds = false;
	protected int BTPstartFrame = -1;
	protected int BTPendFrame = -1;
	private Vector<TrackPoint> startClippings;
	private Vector<TrackPoint> endClippings;
	
	
	
	/**
	 * The amount that each backbone in the track shifted during the last
	 * iteration that acted upon any given point
	 */
	private double[] shifts;

	private BBFUpdateScheme updater;
	
	private int pass;
	
	private boolean diverged = false;
	protected int divergedInd = -1;
	
	Vector<EnergyProfile> energyProfiles;
	
	transient Communicator comm;
	transient Communicator bbcomm;

	/**
	 * Constructs a backbone fitter
	 */
	public BackboneFitter() {
		init(null);
	}
	
	BackboneFitter(FittingParameters fp){
		init(fp);
	}
	
	private void init(FittingParameters fp) {

		if (fp==null){
			params = new FittingParameters();
		} else{
			params = fp;
		}
			
		pass = 0;
		Forces = params.getForces(pass);
		
		if (params.storeEnergies){
			initEnergyProfiles();
		}
	
		comm = new Communicator();
		comm.setVerbosity(VerbLevel.verb_error);
		bbcomm = new Communicator();
		bbcomm.setVerbosity(VerbLevel.verb_off);

	}

	private void initEnergyProfiles(){
		energyProfiles = new Vector<EnergyProfile>();
		for (int i=0; i<Forces.size(); i++){
			energyProfiles.add(new EnergyProfile(Forces.get(i).getName()));
		}
		energyProfiles.add(new EnergyProfile("Total"));
	}
	
	
	private void clearPrev(){
		track = null;
		BTPs = null;
		shifts = null;
		updater = null;
		pass = 0;
		diverged = false;
		BTPs = new Vector<BackboneTrackPoint>();
		clipEnds = false;
		BTPstartFrame=-1;
		BTPendFrame=-1;
		if (params.storeEnergies){
			initEnergyProfiles();
		}
	}
	
	
	/**
	 * Fits backbones to the points in the given track.
	 * <p>
	 * After fitting, the Vector of BackboneTrackPoints can be accessed via
	 * getBackbonePoints()
	 * 
	 * @param tr
	 */
	public void fitTrack(Track tr) {
		
		clearPrev();
		
		if (params.subset){
			int si = (params.startInd>=tr.points.size())? si=tr.points.size()-1 : params.startInd;
			int ei = (params.endInd>=tr.points.size())? tr.points.size()-1 : params.startInd;
			tr = new Track(tr, si, ei);
			if (tr.points.size()<params.minTrackLen){
				System.out.println("Track too short for fitting");
				return;
			}
		}
		
		boolean noError = true;

		// Extract the points, and move on (if successful)
		comm.message("Extracting maggot tracks", VerbLevel.verb_debug);
		
		
		if (noError && convertTrack2BTP(tr)) {//creates the new track
			//If there was no error extraction points, run the different grain passes of the algorithm
			noError = true;
			for(int i=0; (i<params.grains.length && noError); i++){
				noError = doPass(params.grains[i]);
				if (!noError) {
					comm.message("Error on track "+tr.getTrackID()+"("+track.getTrackID()+") pass "+i+"(grain "+params.grains[i]+") \n ---------------------------- \n \n", VerbLevel.verb_error);
					errTrack = track;
					track = null;
					pass++;
					Forces = params.getForces(pass);
				}
				
				
//				if (!comm.outString.equals("")){
//					 new TextWindow("TrackFitter", comm.outString, 500, 500);
//				 }
//				comm = new Communicator();
//				comm.setVerbosity(VerbLevel.verb_error);
			}
			
				 
		} else {
			comm.message("Error converting track points to btp", VerbLevel.verb_error);
		}
		
		System.out.println("Done fitting track");
	}


	
	
	/**
	 * Creates a new track full of BTPs out of the points in the track
	 * <p>
	 * 
	 * @return A list of booleans indicating whether or not the midline in the
	 *         corresponding BTP is empty
	 */
	private boolean convertTrack2BTP(Track tr) {
		
		boolean noerror=true;
		try {
	
			if (tr.getStart() instanceof MaggotTrackPoint) {
				for (int i = 0; i < tr.getNumPoints(); i++) {
		
					comm.message("Getting mtp...", VerbLevel.verb_debug);
					MaggotTrackPoint mtp = (MaggotTrackPoint) tr.getPoint(i);
		
					if (mtp == null) {
						comm.message("Point " + i + " was not able to be cast",
								VerbLevel.verb_error);
					} else {
						mtp.comm = comm;
					}
		
					comm.message("Converting point " + i + " into a BTP",
							VerbLevel.verb_debug);
					BackboneTrackPoint btp = BackboneTrackPoint.convertMTPtoBTP(mtp,params.numBBPts);
		
					if (btp.midline == null) {
						comm.message("mtp.midline was null", VerbLevel.verb_debug);
		
					} else {
						comm.message("convertMTPtoBTP successful", VerbLevel.verb_debug);
					}
					
		
					btp.bf = this;
					BTPs.add(btp);
				} 
				
				track = new Track(BTPs, tr);
				newTrID = track.getTrackID();
				BTPs.removeAllElements();
				
			} else {
				comm.message(
						"Points were not maggotTrackPoints; no backbone points were made",
						VerbLevel.verb_error);
				noerror=false;
			}
				
		} catch (Exception e) {
			noerror=false;
			
			StringWriter sw = new StringWriter();
			PrintWriter pw = new PrintWriter(sw);
			e.printStackTrace(pw);
			comm.message(sw.toString(), VerbLevel.verb_error);
		}

		if (!noerror) track = null;
		return noerror;
	}

	
	private boolean doPass(int grain){
				
		//Set the BTPs for this pass's grain
		comm.message("Generating BTPs at grain "+grain, VerbLevel.verb_debug);
		BTPs.removeAllElements();
		boolean noError = generateBTPList(grain);
		
		if (noError) {
			
			// Set the updater
			updater = new BBFUpdateScheme(BTPs.size());
			shifts = new double[BTPs.size()];

			
			// Run the fitting algorithm
			try {
				
				runFitter();
				if (diverged) {
					comm.message("Track "+track.getTrackID()+" diverged", VerbLevel.verb_error);
					return false;
				}
				
			} catch(Exception e){
				
				noError = false;
				
				StringWriter sw = new StringWriter();
				PrintWriter pw = new PrintWriter(sw);
				e.printStackTrace(pw);
				comm.message("Error during BackboneFitter.runFitter() at grain "+grain+"\n"+sw.toString(), VerbLevel.verb_error);
			} 
			
			
		} else{
			comm.message("Error generating backbones at grain "+grain, VerbLevel.verb_error);
		}
		
		return noError;
	}
	
	
	/**
	 * Generates a list of BTPs from the original trackPoint list, with the
	 * proper grain
	 * 
	 * @param grain
	 *            The spacing between points of the original track
	 * @return Flag indicating no error (true) or error (false)
	 */
	private boolean generateBTPList(int grain) {

		try {
			
			comm.message("Sampling points", VerbLevel.verb_debug);
			sampleTrackPoints(grain);
			boolean noError = addBackboneInfo();
			
			return noError;

		} catch (Exception e) {
			
			StringWriter sw = new StringWriter();
			PrintWriter pw = new PrintWriter(sw);
			e.printStackTrace(pw);
			comm.message("Problem getting BTPS from the track at grain "+grain+" \n" + sw.toString(), VerbLevel.verb_error);
			return false;
		}
	}

	
	/**
	 * Stores a sampled list of backboneTrackPoints that are sampled at the given grain
	 * @param grain Spacing between trackPoints
	 */
	private void sampleTrackPoints(int grain){
		
		
		int numTPs = track.getNumPoints()/grain;
		try {
			
			for (int i=0; i<numTPs; i++){
				
				BTPs.add((BackboneTrackPoint)track.getPoint(i*grain));
				
			}
			
		} catch (Exception e){
			StringWriter sw = new StringWriter();
			PrintWriter pw = new PrintWriter(sw);
			e.printStackTrace(pw);
			comm.message("Error sampling trackPoints\n"+sw.toString(), VerbLevel.verb_error);
		}
		
	}

	
	private boolean addBackboneInfo(){
		
		float[] origin = new float[2];
		try {
			if (pass==0){
				for (int i=0; i<BTPs.size(); i++){
					origin[0] = BTPs.get(i).rect.x;
					origin[1] = BTPs.get(i).rect.y;
					BTPs.get(i).setBackboneInfo(params.clusterMethod, BTPs.get(i).midline, origin);
					comm.message("Adding backbone info to BTP "+i+"(frame "+BTPs.get(i).frameNum+")", VerbLevel.verb_debug);
				}
				boolean noError = cleanUpBTPs(findEmptyMids(), params.minFlickerDist*params.grains[pass]);
				return noError;
			} else {
//				if (clipEnds) clipEnds();
				origin[0] = 0;
				origin[1] = 0;
				//The old spines are already in the BTPs from the previous pass; find the empty ones and interpolate
				int prev = 0; 				
				Vector<FloatPolygon> interpdBBs;
				for (int i=(prev+1); i<BTPs.size(); i++){
					if(BTPs.get(i).backbone!=null){
						interpdBBs = interpBackbones(i-prev-1, origin, origin, BTPs.get(prev).backbone.getFloatPolygon(), BTPs.get(i).backbone.getFloatPolygon());
						//fill in the midlines
						for (int j=prev; j<i; j++){
							BTPs.get(j).setBackboneInfo(params.clusterMethod, new PolygonRoi(interpdBBs.get(j-prev), PolygonRoi.POLYLINE), origin);
						} 
						prev = i;
					}
				}
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
		
		for (int i=0; i<BTPs.size(); i++){
			sampledEmptyMids[i] = (BTPs.get(i).midline==null);
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
			MaggotTrackBuilder.orientMaggotTrack(BTPs, comm, track.getTrackID());
				comm.message("Filling midlines", VerbLevel.verb_debug);
			boolean noError = fillGaps(gaps);
			if (!noError){
				return false;
			}
			if (clipEnds) clipEnds();
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

			if (emptyMidlines[ptr]) {
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
		
		boolean clearGaps = false;
		
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
				clearGaps = true;
			} else {
				//Advance prevGap
				prevGap = currGap;
			}
			
		} while (gIt.hasNext());
		
		return clearGaps;
	}

	private void invalidateGaps(Vector<Gap> gaps){
		for (int i=0; i<gaps.size(); i++){
			invalidateGapMidlines(gaps.get(i));
		}
	}
	
	private void invalidateGapMidlines(Gap gap){
		
		for (int i=gap.start; i<=gap.end; i++){
			BackboneTrackPoint btp = BTPs.get(i);
			btp.htValid = false;
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
					BTPs.get(i).bf = this;
					BTPs.get(i).fillInBackboneInfo(params.clusterMethod, newMid, origin);
				}
				comm.message("Gap filled", VerbLevel.verb_debug);
			} else if (gapStart==0 && gapEnd == (BTPs.size()-1)){
				comm.message("All midlines are invalid in track "+track.getTrackID(), VerbLevel.verb_error);
				System.out.println("All midlines are invalid in track "+track.getTrackID());
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
			comm.message("Error filling gap ("+gapStart+"-"+gapEnd+") in track "+track.getTrackID()+"\n"+sw.toString(), VerbLevel.verb_error);
		}
		return true;
	}

	
	protected Vector<FloatPolygon> interpBackbones(int firstBTP, int endBTP) {
		
		FloatPolygon bbFirst = BTPs.get(firstBTP).midline.getFloatPolygon();
		FloatPolygon bbEnd = BTPs.get(endBTP).midline.getFloatPolygon();

		float[] firstO = {BTPs.get(firstBTP).rect.x, BTPs.get(firstBTP).rect.y};
		float[] endO = {BTPs.get(endBTP).rect.x, BTPs.get(endBTP).rect.y};
		
		return interpBackbones(endBTP - firstBTP - 1, firstO, endO, bbFirst, bbEnd);
	}
	
	
	protected static Vector<FloatPolygon> interpBackbones(int numnewbbs, float[] firstO, float[] endO, FloatPolygon bbFirst, FloatPolygon bbEnd) {
		
		if (numnewbbs<1) return null;
		
		
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


	private boolean clipEnds(){
		
		comm.message("Clipping ends on track "+newTrID+": startFrame="+BTPstartFrame+" endFrame="+BTPendFrame, VerbLevel.verb_message);
		
		if (BTPendFrame>0){//new end frame
			
			//Clip the actual track
			endClippings = track.clipPoints(BTPendFrame+1,track.points.lastElement().frameNum+1);
			
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
			} else {
				comm.message("Error clipping ends in track "+track.getTrackID()+": could not find index of new end frame ("+BTPendFrame+")", VerbLevel.verb_error);
				return false;
			}
		}
		
		if (BTPstartFrame>0){// New Start frame:
			
			startClippings = track.clipPoints(track.points.firstElement().frameNum,BTPstartFrame);
			
			//Find the index of last element to remove
			int i=-1; //i = index 
			while (i<(BTPs.size()-2) && BTPs.get(i+1).getFrameNum()!=BTPstartFrame){
				i++;
			}
			
			if (BTPs.get(i+1).getFrameNum()==BTPstartFrame){
				//Invalidate BTPs
				for (int j=0; j<=i; j++){
					BTPs.get(j).htValid = false;
					BTPs.get(j).finalizeBackbone();
				}
				//Remove elements
				BTPs.subList(0, i+1).clear();
			} else {
				comm.message("Error clipping ends in track "+track.getTrackID()+": could not find index of new start frame ("+BTPstartFrame+")", VerbLevel.verb_error);
				return false;
			}
		}
		
		return true;
	}
	
	
	/**
	 * Runs the fitting algorithm
	 * <p>
	 * An updating scheme is maintained, which alternates between fitting every
	 * point and fitting just the points which are still changing significantly
	 * 
	 * returns convergence status
	 */
	private boolean runFitter() {
		boolean firstPass=true;
		do {
			comm.message("Iteration number " + updater.getIterNum(), VerbLevel.verb_debug);

			// Do a relaxation step
			comm.message("Updating " + updater.inds2Update().length+ " backbones", VerbLevel.verb_debug);
			bbcomm.message("\n\nIteration "+updater.getIterNum(), VerbLevel.verb_debug);
			
			if (params.storeEnergies){
				if (!firstPass){
					for (int i=0; i<energyProfiles.size(); i++){
						energyProfiles.get(i).initEnergyEntryWithPrev();
					}
				} else {
					for (int i=0; i<energyProfiles.size(); i++){
						energyProfiles.get(i).initEnergyEntry(BTPs.size());
					}
					firstPass=false;
				}
			}
			
			relaxBackbones(updater.inds2Update());
			
			if (params.storeEnergies){
				for (int i=0; i<energyProfiles.size(); i++){
					energyProfiles.get(i).storeProfile();
				}
			}

			// Setup for the next step
			for (int i = 0; (i<BTPs.size() && !diverged); i++) {
				shifts[i] = BTPs.get(i).calcPointShift();
				
				//Check for divergence
				if (BTPs.get(i).diverged(params.divergenceConstant)){
					diverged = true;
					divergedInd=i;
					//TODO
//					storeDiverganceInfo(BTPs.get(i));
				} else {
					BTPs.get(i).setupForNextRelaxationStep();
					//TODO
					
					//if (storeTrackDivergance) BTPs.insertElementAt(btps.getNextIter(), i); somewhere.add(BTPs.removeElementAt(i))
				}
			}
			
			// Show the fitting messages, if necessary
			if (!updater.comm.outString.equals("")) {
				new TextWindow("TrackFitting Updater, pass "+pass, updater.comm.outString, 500, 500);
			}
			
		} while (!diverged && updater.keepGoing(shifts));
		
		System.out.println("Number of iterations: "+updater.getIterNum());
		
		if (!diverged) {
			finalizeBackbones();
		} else {
			//TODO
			//storeDiverganceInfo();
		}
		return !diverged;

	}

	/**
	 * Allows the backbones to relax to lower-energy conformations
	 * 
	 * @param inds
	 *            The indices of the BTPs which should be relaxed
	 */
	private void relaxBackbones(int[] inds) {

		
//		if (params.storeEnergies){
//			if (energyProfiles.firstElement().hasPrev()){
//				for (int i=0; i<energyProfiles.size(); i++){
//					energyProfiles.get(i).initEnergyEntryWithPrev();
//				}
//			} else {
//				for (int i=0; i<energyProfiles.size(); i++){
//					energyProfiles.get(i).initEnergyEntry(BTPs.size());
//				}
//			}
//		}
		
		for (int i = 0; i < inds.length; i++) {
			comm.message("Relaxing frame " + inds[i], VerbLevel.verb_debug);
			// Relax each backbone
			bbRelaxationStep(inds[i]);

		}
		
//		if (params.storeEnergies){
//			for (int i=0; i<energyProfiles.size(); i++){
//				energyProfiles.get(i).storeProfile();
//			}
//		}

	}

	/**
	 * Relaxes a backbone to a lower-energy conformation
	 * 
	 * @param btpInd
	 *            Index of the BTP (in BTDs) to be processed
	 */
	private void bbRelaxationStep(int btpInd) {

		// IJ.showStatus("Getting target backbones in frame "+btpInd);
		// Get the lower-energy backbones for each individual force
		Vector<FloatPolygon> targetBackbones = getTargetBackbones(btpInd);
		
		if (params.storeEnergies){
			for (int i=0; i<targetBackbones.size(); i++){
				energyProfiles.get(i).addEnergyEntry(btpInd, Force.getEnergy(targetBackbones.get(i), BTPs.get(btpInd)));
			}
		}
		
		bbcomm.message("Frame "+btpInd+" Components:", VerbLevel.verb_debug);
		
		FloatPolygon newBB = generateNewBackbone(targetBackbones);
		
		if (params.storeEnergies){
			energyProfiles.lastElement().addEnergyEntry(btpInd, Force.getEnergy(newBB, BTPs.get(btpInd))/params.grains[pass]);
		}
		
		BTPs.get(btpInd).setBBNew(newBB);
		
	}

	/**
	 * Allows each force to act on the old backbone of the indicated BTP
	 * 
	 * @param btpInd
	 *            Index of the backbone to relax
	 * @return A vector of all relaxed backbones, in the same order as the list
	 *         of forces
	 */
	private Vector<FloatPolygon> getTargetBackbones(int btpInd) {
		Vector<FloatPolygon> targetBackbones = new Vector<FloatPolygon>();

		// Store the backbones which are relaxed under individual forces
//		ListIterator<Force> fIt = Forces.listIterator(); 
		try{
			for (int i=0; i<Forces.size(); i++){			//while (fIt.hasNext()) {
				
				FloatPolygon tb = Forces.get(i).getTargetPoints(btpInd, BTPs);
//				if (params.storeEnergies){
//					energyProfiles.get(i).addEnergyEntry(btpInd, Force.getEnergy(tb, BTPs.get(btpInd)));
//				}
				targetBackbones.add(tb);;//targetBackbones.add(fIt.next().getTargetPoints(btpInd, BTPs));
			}
		} catch(Exception e){
			StringWriter sw = new StringWriter();
			PrintWriter pw = new PrintWriter(sw);
			e.printStackTrace(pw);
			comm.message("Error getting target backbones: \n"+sw.toString()+"\n", VerbLevel.verb_error);
		}
		
		return targetBackbones;

	}

	/**
	 * Generates the new backbone using the individual-force-shifted spines and
	 * the weighting parameters
	 * 
	 * @param targetSpines
	 */
	private FloatPolygon generateNewBackbone(Vector<FloatPolygon> targetBackbones) {
		
		boolean makeString = (updater.getIterNum()<20);
		StringBuilder st = new StringBuilder("\n");
		
		float[] zeros = new float[params.numBBPts];
		Arrays.fill(zeros, 0);

		float[] newX = new float[params.numBBPts];
		Arrays.fill(newX, 0);
		float[] newY = new float[params.numBBPts];
		Arrays.fill(newY, 0);
		float normFactors[] = new float[params.numBBPts];
		Arrays.fill(normFactors, 0);

		comm.message("Gathering target backbones...", VerbLevel.verb_debug);
		// Add each target backbone to the new backbone and gather the weighting
		// factors for normalization
		for (int tb = 0; tb < targetBackbones.size(); tb++) {

			if (makeString) st.append(Forces.get(tb).name+": ");
			
			float[] targetX = targetBackbones.get(tb).xpoints;
			float[] targetY = targetBackbones.get(tb).ypoints;
			float[] weights = Forces.get(tb).getWeights();

			for (int k = 0; k < params.numBBPts; k++) {
				if (makeString) st.append(k);
				if (weights[k] != 0 && targetX[k]!=0 && targetY[k]!=0) {
					st.append("("+targetX[k]+","+targetY[k]+")");
					newX[k] += targetX[k] * weights[k];
					newY[k] += targetY[k] * weights[k];
					normFactors[k] += weights[k];
				}
			}
			st.append("\n");		

		}
		bbcomm.message(st.toString(), VerbLevel.verb_debug);
		comm.message("Normalizing points", VerbLevel.verb_debug);
		// Normalize each point
		for (int k = 0; k < params.numBBPts; k++) {
			newX[k] = newX[k] / normFactors[k];
			newY[k] = newY[k] / normFactors[k];
		}

		return new FloatPolygon(newX, newY);

	}

	protected void finalizeBackbones() {
		ListIterator<BackboneTrackPoint> btpIt = BTPs.listIterator();
		while (btpIt.hasNext()) {
			btpIt.next().finalizeBackbone();
		}
	}

	protected void storeDiverganceInfo(){
		//TODO
		ListIterator<BackboneTrackPoint> btpIt = BTPs.listIterator();
		while (btpIt.hasNext()) {
			btpIt.next().finalizeBackbone();
		}
	}
	
	
	
	public Vector<BackboneTrackPoint> getBackboneTrackPoints() {
		return BTPs;
	}

	public String getForceName(int ind) {
		return Forces.get(ind).getName();
	}
	
	
	public Track getTrack(){
		return track;
	}

	public void showCommOutput(){
		if (!comm.outString.equals("")){
			 new TextWindow("TrackFitter", comm.outString, 500, 500);
		 }
		if (!bbcomm.outString.equals("")){
			new TextWindow("Backbone Generation", bbcomm.outString, 500, 500);
		}
	}
	
	public void saveCommOutput(String dstDir){
		if (!comm.outString.equals("")){
			PrintWriter out;
			File f =new File(dstDir+"TrackFitter.txt"); 
			try{
				if (!f.exists()) f.createNewFile();
				out = new PrintWriter(f);
				out.print(comm.outString);
				
			} catch (Exception e){
				e.printStackTrace();
			}
			
		 }
		
		if (!bbcomm.outString.equals("")){
			PrintWriter out;
			File f =new File(dstDir+"Backbone Generation.txt"); 
			try{
				if (!f.exists()) f.createNewFile();
				out = new PrintWriter(f);
					out.print(bbcomm.outString);
				
			} catch (Exception e){
				e.printStackTrace();
			}
		}
	}
	
	public void saveEnergyProfiles(String dstDir){
		for (int i=0; i<energyProfiles.size(); i++){
			EnergyProfile.saveProfile(energyProfiles.get(i),dstDir);
		}
	}
	
	
	public boolean diverged(){
		return diverged;
	}
	
	public boolean wasClipped(){
		return clipEnds;
	}
	
	public Vector<TrackPoint> getStartClippings(){
		return startClippings;
	}

	public Vector<TrackPoint> getEndClippings(){
		return endClippings;
	}
	
	
}

class Gap{
	
	int start;
	int end;
	
	public Gap(int s, int e){
		start = s;
		end = e;
	}
	
	public int len(){
		return end-start+1;
	}
	
	public int distTo(Gap g2){
		
		if(g2.start<end){
			return start-g2.end-1;//g2.distTo(this);
		} else {
			return g2.start-end-1;
		}
	}
	
	public void merge2Next(Gap gNext){
		end = gNext.end;
	}
	
	public String toString(){
		return start+"-"+end;
	}
	
}


class EnergyProfile implements java.io.Serializable{
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	public String name;
	//energies.get(i)[j] = energy of j'th trackpoint at i'th iteration of bbf algorithm
	public Vector<Float[]> energies;
	transient Float[] energyIter;
	
	public EnergyProfile(String name){
		this.name = name;
		energies = new Vector<Float[]>();
	}
	
	
//	public void generateProfile(Vector<BackboneTrackPoint> BTPs){
//		Float[] en = new Float[BTPs.size()];
//		for (int i=0; i<en.length; i++){
//			en[i] = f.getEnergy(i, BTPs);
//		}
//		energies.add(en);
//	}

	public void storeProfile(){
		energies.addElement(energyIter);
	}
	
	public void initEnergyEntry(int btpLen){
		energyIter = new Float[btpLen];
	}
	
	public void initEnergyEntryWithPrev(){
		energyIter = energies.lastElement().clone();
	}
	
	public boolean hasPrev(){
		return energies.size()>0;
	}
	
	public void addEnergyEntry(int ind, float entry){
		energyIter[ind] = entry;
	}
	
	public static void saveProfile(EnergyProfile enPr, String dstDir){
		File f = new File(dstDir+"\\"+enPr.name+".ser");
		try {
			ObjectOutputStream out = new ObjectOutputStream(new BufferedOutputStream(new FileOutputStream(f)));
			out.writeObject(enPr);
			out.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public static EnergyProfile loadEnergyProfile(String fname){
		File f = new File(fname);
		EnergyProfile enPr = null;
		try {
			ObjectInputStream in = new ObjectInputStream(new BufferedInputStream(new FileInputStream(f)));
			enPr = (EnergyProfile) in.readObject();
			in.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return enPr;
	}
	
	public void printEnergies(){
		//TODO
		//JK gonna load them up in matlab
	}
	
	
}
