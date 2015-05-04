import ij.gui.PolygonRoi;
import ij.process.FloatPolygon;
import ij.text.TextWindow;
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
	
	/**
	 * The list of empty midlines corresponding to the full list of trackpoints
	 */
//	boolean[] emptyMidlines;
	
	/**
	 * A List of the BbTP's, worked on during fitting algorithm
	 */
	Vector<BackboneTrackPoint> BTPs;

	/**
	 * The amount that each backbone in the track shifted during the last
	 * iteration that acted upon any given point
	 */
	private double[] shifts;

	private BBFUpdateScheme updater;
	
	private int pass;

	/**
	 * The final forces acting on each backbone point
	 */

	/**
	 * The final energy of each backbone point
	 */

	transient Communicator comm;
	transient Communicator bbcomm;

	/**
	 * Constructs a backbone fitter
	 */
	public BackboneFitter() {

		params = new FittingParameters();

		pass = 0;
		Forces = params.getForces(pass);

		comm = new Communicator();
		comm.setVerbosity(VerbLevel.verb_error);
		bbcomm = new Communicator();
		bbcomm.setVerbosity(VerbLevel.verb_off);

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
		
		BTPs = new Vector<BackboneTrackPoint>();

		// Extract the points, and move on (if successful)
		comm.message("Extracting maggot tracks", VerbLevel.verb_debug);
		
		
		if (extractTrackPoints(tr)) {//ExtractTrackPoints creates the new track
			//If there was no error extraction points, run the different grain passes of the algorithm
			boolean noError = true;
			for(int i=0; (i<params.grains.length && noError); i++){
				noError = doPass(params.grains[i]);
				if (!noError) {
					pass++;
					Forces = params.getForces(pass);
					comm.message("Error on track "+tr.getTrackID()+"("+track.getTrackID()+") pass "+i+"(grain "+params.grains[i]+")", VerbLevel.verb_error);
					track = null;
				}
				
				
//				if (!comm.outString.equals("")){
//					 new TextWindow("TrackFitter", comm.outString, 500, 500);
//				 }
//				comm = new Communicator();
//				comm.setVerbosity(VerbLevel.verb_error);
			}
			
				 
		} else {
			comm.message("Error extracting trackPoints from track", VerbLevel.verb_error);
		}
		
		
		
		// }
	}

	
	private void clearPrev(){
		track = null;
		BTPs = null;
		shifts = null;
		updater = null;
		pass = 0;
	}

	/**
	 * Creates a new track full of BTPs out of the points in the track
	 * <p>
	 * 
	 * @return A list of booleans indicating whether or not the midline in the
	 *         corresponding BTP is empty
	 */
	private boolean extractTrackPoints(Track tr) {
		
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
					
					//TODO make emptyMidlines[i] true when it's too far from the previous spine
					
		
					btp.bf = this;
					BTPs.add(btp);
				} 
				
				track = new Track(BTPs, tr);
				BTPs.removeAllElements();
				
			} else {
				comm.message(
						"Points were not maggotTrackPoints; no points were made",
						VerbLevel.verb_error);
				// TODO reload the points as BTP
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
				
			} catch(Exception e){
				
				noError = false;
				
				StringWriter sw = new StringWriter();
				PrintWriter pw = new PrintWriter(sw);
				e.printStackTrace(pw);
				comm.message("Error during BackboneFitter.runFitter() at grain "+grain+"\n"+sw.toString(), VerbLevel.verb_debug);
			} 
			
			
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
			addBackboneInfo();
//			if (sampledEmptyMids!=null) cleanUpBTPs(sampledEmptyMids);//TODO move this to the first pass
			
			return true;

		} catch (Exception e) {
			
			StringWriter sw = new StringWriter();
			PrintWriter pw = new PrintWriter(sw);
			e.printStackTrace(pw);
			comm.message("Problem getting BTPS from the track at grain "+grain+" \n" + sw.toString(), VerbLevel.verb_debug);
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

	
	private void addBackboneInfo(){
		
		float[] origin = new float[2];
		
		if (pass==0){
			for (int i=0; i<BTPs.size(); i++){
				origin[0] = BTPs.get(i).rect.x;
				origin[1] = BTPs.get(i).rect.y;
				BTPs.get(i).setBackboneInfo(BTPs.get(i).midline, origin);
				comm.message("Adding backbone info to BTP "+i+"(frame "+BTPs.get(i).frameNum+")", VerbLevel.verb_debug);
			}
			
			cleanUpBTPs(findEmptyMids(), params.minFlickerDist*params.grains[pass]);
			
			
		} else {
			
			origin[0] = 0;
			origin[1] = 0;
			//The old spines are already in the BTPs from the previous pass; find the empty ones and interpolate

			int prev = 0;//TODO find the first spine!! If this does not =0, carry spines backwards 
			
			Vector<FloatPolygon> interpdBBs;
			for (int i=(prev+1); i<BTPs.size(); i++){
				if(BTPs.get(i).getBackbone()!=null){
					interpdBBs = interpBackbones(i-prev-1, origin, origin, BTPs.get(prev).getBackbone().getFloatPolygon(), BTPs.get(i).getBackbone().getFloatPolygon());
					//fill in the midlines
					for (int j=prev; j<i; j++){
						BTPs.get(j).setBackboneInfo(new PolygonRoi(interpdBBs.get(j-prev), PolygonRoi.POLYLINE), origin);
					} 
					prev = i;
				}
			}
			
			
			
		}
	}
	
	private boolean[] findEmptyMids(){
		
		boolean[] sampledEmptyMids = new boolean[BTPs.size()];
		
		for (int i=0; i<BTPs.size(); i++){
			sampledEmptyMids[i] = (BTPs.get(i).midline==null);
		}
		
		return sampledEmptyMids;
		
	}
	
	private void cleanUpBTPs(boolean[] sampledEmptyMids, double flickerDist){
		if(sampledEmptyMids!=null){
			comm.message("Clearing flickers", VerbLevel.verb_debug);
			clearFlickerMids(sampledEmptyMids, flickerDist);
			
			comm.message("Finding gaps", VerbLevel.verb_debug);
			Vector<Gap> gaps = findGaps(sampledEmptyMids);
			comm.message("Cleaning gaps", VerbLevel.verb_debug);
			sanitizeGaps(gaps);
			
			
			comm.message("Filling midlines", VerbLevel.verb_debug);
			fillGaps(gaps);
		}
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
			
			if (mergeGaps(gaps)) {
				clearGaps(gaps);
				MaggotTrackBuilder.orientMaggotTrack(BTPs, comm, track.getTrackID());//TODO redo this as a method of a list of TP's
			}
			
			comm.message("After sanitizing, there are "+gaps.size()+" gaps", VerbLevel.verb_debug); 
		
		} else {
			comm.message("Only one gap, no need to sanitize", VerbLevel.verb_debug);
		}
		
	}
	
	private boolean mergeGaps(Vector<Gap> gaps){
		
		boolean clearGaps = false;
		
		Gap prevGap;
		Gap currGap = null;
		
		ListIterator<Gap> gIt = gaps.listIterator();
		prevGap = gIt.next();//First gap
		
		do{
			//Always advance the next gap
			currGap = gIt.next();
			
			comm.message("Checking gaps "+prevGap.start+"-"+prevGap.end+" & "+currGap.start+"-"+currGap.end, VerbLevel.verb_debug);
			
			if (prevGap.distTo(currGap)<params.minValidSegmentLen){ 
				comm.message("Merging gaps", VerbLevel.verb_debug);
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

	private void clearGaps(Vector<Gap> gaps){
		for (int i=0; i<gaps.size(); i++){
			clearGapMidlines(gaps.get(i));
		}
	}
	
	private void clearGapMidlines(Gap gap){
		
		for (int i=gap.start; i<=gap.end; i++){
//			MaggotTrackPoint mtp = (MaggotTrackPoint) track.points.get(i);
//			mtp.htValid = false;
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
	private void fillGaps(Vector<Gap> gaps) {

		
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
			}
		}
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
				BTPs.get(i).fillInBackboneInfo(fillerMidline, origin);
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
				BTPs.get(i).fillInBackboneInfo(newMid, origin);
			}
			comm.message("Gap filled", VerbLevel.verb_debug);
		} else {
			//TODO CLIP THE TRACK
			return false;
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
	
	
	protected Vector<FloatPolygon> interpBackbones(int numnewbbs, float[] firstO, float[] endO, FloatPolygon bbFirst, FloatPolygon bbEnd) {
		
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

	/**
	 * Runs the fitting algorithm
	 * <p>
	 * An updating scheme is maintained, which alternates between fitting every
	 * point and fitting just the points which are still changing significantly
	 */
	private void runFitter() {
		do {
//			if (updater.getIterNum()>20) comm.setVerbosity(VerbLevel.verb_error);
//			if (updater.getIterNum()>20) bbcomm.setVerbosity(VerbLevel.verb_off);
			comm.message("Iteration number " + updater.getIterNum(), VerbLevel.verb_debug);

			// Do a relaxation step
			comm.message("Updating " + updater.inds2Update().length
					+ " backbones", VerbLevel.verb_debug);
			
			bbcomm.message("\n\nIteration "+updater.getIterNum(), VerbLevel.verb_debug);
			relaxBackbones(updater.inds2Update());

			// Setup for the next step
			for (int i = 0; i < BTPs.size(); i++) {
				shifts[i] = BTPs.get(i).calcPointShift();
				BTPs.get(i).setupForNextRelaxationStep();
			}
			
			// Show the fitting messages, if necessary
			if (!updater.comm.outString.equals("")) {
				new TextWindow("TrackFitting Updater, pass "+pass, updater.comm.outString, 500, 500);
			}
		} while (updater.keepGoing(shifts));
		
		finalizeBackbones();

	}

	/**
	 * Allows the backbones to relax to lower-energy conformations
	 * 
	 * @param inds
	 *            The indices of the BTPs which should be relaxed
	 */
	private void relaxBackbones(int[] inds) {

		for (int i = 0; i < inds.length; i++) {
			comm.message("Relaxing frame " + inds[i], VerbLevel.verb_debug);
			// Relax each backbone
			bbRelaxationStep(inds[i]);

		}

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
		// Combine the single-force backbones into one, and set it as the new
		// backbone
		// IJ.showStatus("Setting bbNew in frame "+btpInd);
		bbcomm.message("Frame "+btpInd+" Components:", VerbLevel.verb_debug);
		BTPs.get(btpInd).setBBNew(generateNewBackbone(targetBackbones));
		// Get the shift (and energy?) for use in updating scheme/display
		// shifts[btpInd] = BTPs.get(btpInd).calcPointShift();

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
		ListIterator<Force> fIt = Forces.listIterator();
		while (fIt.hasNext()) {
			targetBackbones.add(fIt.next().getTargetPoints(btpInd, BTPs));
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
	
}
