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
	 * A List of the BbTP's, worked on during fitting algorithm
	 */
	Vector<BackboneTrackPoint> BTPs;

	/**
	 * The amount that each backbone in the track shifted during the last
	 * iteration that acted upon any given point
	 */
	private double[] shifts;

	private BBFUpdateScheme updater;

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

		int pass = 0;
		addForces(pass);

		comm = new Communicator();
		comm.setVerbosity(VerbLevel.verb_off);
		bbcomm = new Communicator();
		bbcomm.setVerbosity(VerbLevel.verb_off);

	}

	/**
	 * Adds instances of each type of Force to the backbone fitter
	 */
	private void addForces(int pass) {
		Forces = new Vector<Force>();
		Forces.add(new ImageForce(params.imageWeights, params.imageWeight));
		Forces.add(new SpineLengthForce(params.spineLengthWeights,
				params.spineLengthWeight));
		Forces.add(new SpineSmoothForce(params.spineSmoothWeights,
				params.spineSmoothWeight));
		Forces.add(new TimeLengthForce(params.timeLengthWeights,
				params.timeLengthWeight[pass]));
		Forces.add(new TimeSmoothForce(params.timeSmoothWeights,
				params.timeSmoothWeight[pass]));
		// Forces.add(new HTAttractionForce(params.HTAttractionWeights));
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

		BTPs = new Vector<BackboneTrackPoint>();

		// Extract the points, and move on (if successful)
		comm.message("Extracting maggot tracks", VerbLevel.verb_debug);
		boolean[] emptyMidlines = extractTrackPoints(tr);
		track = new Track(BTPs, tr);
		
		
		if (emptyMidlines!=null) {
			comm.message("Generating BTPs", VerbLevel.verb_debug);
			
			// for(int i=0; (i<params.grains.length && noError); i++){
			// boolean nextGrain = params.grains[i];
			// noError = generateBTPs(nextGrain);
			boolean noError = generateBTPList(1, emptyMidlines);
			
			if (noError) {
				
				// Set the updater
				updater = new BBFUpdateScheme(BTPs.size());
				shifts = new double[BTPs.size()];
	
				
				// Run the fitting algorithm
				try {
					run();
				} catch(Exception e){
					
					StringWriter sw = new StringWriter();
					PrintWriter pw = new PrintWriter(sw);
					e.printStackTrace(pw);
					
					comm.message("Error during BackboneFitter.run()\n"+sw.toString(), VerbLevel.verb_debug);
				} 
				
				
				
				
				// Show the fitting messages, if necessary
				if (!updater.comm.outString.equals("")) {
					new TextWindow("TrackFitting Updater", updater.comm.outString,
							500, 500);
				}
			}
			
			

		} else {
			
			
			comm.message("Error extracking trackPoints from track", VerbLevel.verb_error);
		}
		
		
		if (!comm.outString.equals("")){
			 new TextWindow("TrackFitter", comm.outString, 500, 500);
		 }
		if (!bbcomm.outString.equals("")){
			new TextWindow("Backbone Generation", bbcomm.outString, 500, 500);
		}
		// }
	}


	/**
	 * Creates BTPs out of the points in the track
	 * 
	 * @return A list of booleans indicating whether or not the midline in the
	 *         corresponding BTP is empty
	 * @throws Exception
	 */
	private boolean[] extractTrackPoints(Track tr) {
		boolean[] emptyMidlines;
		
		try {
			emptyMidlines = new boolean[tr.points.size()];
	
			if (tr.points.get(0) instanceof MaggotTrackPoint) {
				for (int i = 0; i < tr.points.size(); i++) {
		
					comm.message("Getting mtp...", VerbLevel.verb_debug);
					MaggotTrackPoint mtp = (MaggotTrackPoint) tr.points.get(i);
		
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
						emptyMidlines[i] = true;
						comm.message("mtp.midline was null", VerbLevel.verb_debug);
		
					} else {
						comm.message("convertMTPtoBTP successful", VerbLevel.verb_debug);
					}
					
					//TODO make emptyMidlines[i] true when it's too far from the previous spine
					
		
					btp.bf = this;
					BTPs.add(btp);
				} 
			
			} else {
				comm.message(
						"Points were not maggotTrackPoints; no points were made",
						VerbLevel.verb_error);
				// TODO reload the points as BTP
				emptyMidlines = null;
			}
				
		} catch (Exception e) {
			emptyMidlines = null;
			
			StringWriter sw = new StringWriter();
			PrintWriter pw = new PrintWriter(sw);
			e.printStackTrace(pw);
			comm.message(sw.toString(), VerbLevel.verb_error);
		}

		return emptyMidlines;
	}
	

	/**
	 * Generates a list of BTPs from the original trackPoint list, with the
	 * proper grain
	 * 
	 * @param grain
	 *            The spacing between points of the original track
	 * @return Flag indicating no error (true) or error (false)
	 */
	private boolean generateBTPList(int grain, boolean[] emptyMidlines) {

		// Extract a list of the TrackPoints and convert them to
		// BackboneTrackPoints to hold the new backbone info
		try {
				
			comm.message("Clearing flickers", VerbLevel.verb_debug);
			clearFlickerMids(emptyMidlines);
			
			comm.message("Finding gaps", VerbLevel.verb_debug);
			Vector<Gap> gaps = findGaps(emptyMidlines);
			comm.message("Cleaning gaps", VerbLevel.verb_debug);
			sanitizeGaps(gaps);
			
			
			comm.message("Filling midlines", VerbLevel.verb_debug);
			fillGaps(gaps);
			
			return true;

		} catch (Exception e) {
			
			StringWriter sw = new StringWriter();
			PrintWriter pw = new PrintWriter(sw);
			e.printStackTrace(pw);
			comm.message("Problem getting BTPS from the track \n" + sw.toString(), VerbLevel.verb_debug);
			return false;
		}
	}

	
	private void clearFlickerMids(boolean[] emptyMidlines){
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
			
			if (dist>0 && dist>params.minFlickerDist) {
				emptyMidlines[currInd] = true;
				comm.message("Flicker at "+(currInd+BTPs.firstElement().frameNum), VerbLevel.verb_debug);
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
				do ++ptr; while (emptyMidlines[ptr] && ptr < emptyMidlines.length);
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
				MaggotTrackBuilder.orientMaggotTrack(track, comm);
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
			MaggotTrackPoint mtp = (MaggotTrackPoint) track.points.get(i);
			mtp.midline = null;
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
				BTPs.get(i).fillInMidline(fillerMidline, origin);
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
				BTPs.get(i).fillInMidline(newMid, origin);
			}
			comm.message("Gap filled", VerbLevel.verb_debug);
		} else {
			//TODO CLIP THE TRACK
			return false;
		}

		return true;
	}

	protected Vector<FloatPolygon> interpBackbones(int firstBTP, int endBTP) {
		
		//Copy the coordinates of the surrounding BTPs so that they can be manipulated
		FloatPolygon bbFirst = BTPs.get(firstBTP).midline.getFloatPolygon();
		FloatPolygon bbEnd = BTPs.get(endBTP).midline.getFloatPolygon();
		int numbbpts = bbFirst.npoints;
		float[] xbbfirst= new float[numbbpts];
		float[] ybbfirst= new float[numbbpts];
		float[] xbbend= new float[numbbpts];
		float[] ybbend= new float[numbbpts];
		for (int i=0; i<numbbpts; i++){//Get the absolute coordinates
			xbbfirst[i] = bbFirst.xpoints[i]+BTPs.get(firstBTP).rect.x;
			ybbfirst[i] = bbFirst.ypoints[i]+BTPs.get(firstBTP).rect.y;
			xbbend[i] = bbEnd.xpoints[i]+BTPs.get(endBTP).rect.x;
			ybbend[i] = bbEnd.ypoints[i]+BTPs.get(endBTP).rect.y;
		}
		
		int numnewbbs = endBTP - firstBTP - 1;
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
	protected void run() {
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

		} while (updater.keepGoing(shifts));

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
