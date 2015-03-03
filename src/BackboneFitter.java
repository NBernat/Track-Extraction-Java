import ij.gui.PolygonRoi;
import ij.process.FloatPolygon;
import ij.text.TextWindow;

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
	private FittingParameters params;

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

	/**
	 * Constructs a backbone fitter
	 */
	public BackboneFitter() {

		params = new FittingParameters();

		int pass = 0;
		addForces(pass);

		comm = new Communicator();
		comm.setVerbosity(VerbLevel.verb_off);

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

		track = tr;
		BTPs = new Vector<BackboneTrackPoint>();

		// Extract the points, and move on (if successful)
		boolean noError = generateBTPs(1);
		// boolean noError = true;
		// TODO

		// for(int i=0; (i<params.grains.length && noError); i++){
		// boolean nextGrain = params.grains[i];
		// noError = generateBTPs(nextGrain);
		if (noError) {
			// Set the updater
			updater = new BBFUpdateScheme(BTPs.size());
			shifts = new double[BTPs.size()];

			// Run the fitting algorithm
			run();

			// Show the fitting messages, if necessary
			// if (!comm.outString.equals("")){
			// new TextWindow("TrackFitter", comm.outString, 500, 500);
			// }
			if (!updater.comm.outString.equals("")) {
				new TextWindow("TrackFitting Updater", updater.comm.outString,
						500, 500);
			}
		}
		// }
	}

	/**
	 * Generates a list of BTPs from the original trackPoint list, with the
	 * proper grain
	 * 
	 * @param grain
	 *            The spacing between points of the original track
	 * @return Flag indicating no error (true) or error (false)
	 */
	private boolean generateBTPs(int grain) {

		// Extract a list of the TrackPoints and convert them to
		// BackboneTrackPoints to hold the new backbone info
		try {
			if (track.points.get(0) instanceof MaggotTrackPoint) {// track.points.get(0).pointType==2

				boolean[] emptyMidlines = extractTracks();
				fillEmptyMidlines(emptyMidlines);
				return true;

			} else {
				comm.message(
						"Points were not maggotTrackPoints; no points were made",
						VerbLevel.verb_error);
				// TODO reload the points as BTP
				return false;
			}
		} catch (Exception e) {
			comm.message(
					"Problem getting BTPS from the track \n" + e.getMessage(),
					VerbLevel.verb_debug);
			return false;
		}
	}

	/**
	 * Creates BTPs out of the points in the track
	 * 
	 * @return A list of booleans indicating whether or not the midline in the
	 *         corresponding BTP is empty
	 * @throws Exception
	 */
	private boolean[] extractTracks() throws Exception {
		boolean[] emptyMidlines = new boolean[track.points.size()];

		for (int i = 0; i < track.points.size(); i++) {

			comm.message("Getting mtp...", VerbLevel.verb_debug);
			MaggotTrackPoint mtp = (MaggotTrackPoint) track.points.get(i);

			if (mtp == null) {
				comm.message("Point " + i + " was not able to be cast",
						VerbLevel.verb_error);
			} else {
				mtp.comm = comm;
			}

			comm.message("Converting point " + i + " into a BTP",
					VerbLevel.verb_debug);
			BackboneTrackPoint btp = BackboneTrackPoint.convertMTPtoBTP(mtp,
					params.numBBPts);

			if (btp.midline == null) {
				emptyMidlines[i] = true;
				comm.message("mtp.midline was null", VerbLevel.verb_debug);

			} else {
				comm.message("convertMTPtoBTP successful", VerbLevel.verb_debug);
			}

			btp.bf = this;
			BTPs.add(btp);
		}

		return emptyMidlines;
	}

	/**
	 * Finds and fills all the empty midlines
	 * 
	 * @param emptyMidlines
	 *            A list of booleans indicating whether or not the midline in
	 *            the corresponding BTP is empty
	 */
	private void fillEmptyMidlines(boolean[] emptyMidlines) {

		int gapStart = -1;

		int ptr = 0;
		while (ptr < emptyMidlines.length) {

			if (emptyMidlines[ptr]) {
				gapStart = ptr;
				// Find the end of the gap
				do
					++ptr;
				while (emptyMidlines[ptr] && ptr < emptyMidlines.length);
				// Fill the gap
				boolean noError = fillGap(gapStart, ptr - 1);

			} else {
				++ptr;
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

		if (gapLen < params.smallGapMaxLen) {
			// Set the
			PolygonRoi fillerMidline;
			if (gapStart != 0) {
				fillerMidline = BTPs.get(gapStart - 1).midline;
			} else if (gapEnd != (BTPs.size() - 1)) {
				fillerMidline = BTPs.get(gapEnd + 1).midline;
			} else {
				return false;
			}

			for (int i = gapStart; i <= gapEnd; i++) {
				BTPs.get(i).fillInMidline(fillerMidline);
			}

		} else if (gapStart != 0 && gapEnd != (BTPs.size() - 1)) {

			interpBackbones(gapStart - 1, gapEnd + 1);

		} else {
			return false;
		}

		return true;
	}

	private void interpBackbones(int firstBTP, int endBTP) {

		int totalNum = endBTP - firstBTP + 1;
		FloatPolygon bbFirst = BTPs.get(firstBTP).midline.getFloatPolygon();
		FloatPolygon bbEnd = BTPs.get(endBTP).midline.getFloatPolygon();
		float[] xbbfirst= new float[bbFirst.npoints];
		float[] ybbfirst= new float[bbFirst.npoints];
		float[] xbbend= new float[bbEnd.npoints];
		float[] ybbend= new float[bbEnd.npoints];
		for (int i=0; i<bbFirst.npoints; i++){
			xbbfirst[i] = bbFirst.xpoints[i]+BTPs.get(firstBTP).rect.x;
			ybbfirst[i] = bbFirst.ypoints[i]+BTPs.get(firstBTP).rect.y;
			xbbend[i] = bbEnd.xpoints[i]+BTPs.get(endBTP).rect.x;
			ybbend[i] = bbEnd.ypoints[i]+BTPs.get(endBTP).rect.y;
		}
		
		float[] xorigins = new float[totalNum-2];
		float[] yorigins = new float[totalNum-2];
		float[] angles = new float[totalNum-2];
		
		// Find the initial shifts, aka origins of rotation (Interpolate the
		// origins from the tail points); shift bbFirst/End
		
		
		// Find the angles of rotations; rotate the bbFirst/End 
		
		// Find the initial coords 

		// Perform the back rotations
		
		// Perform the back shifts
		

	}

	/**
	 * Runs the fitting algorithm
	 * <p>
	 * An updating scheme is maintained, which alternates between fitting every
	 * point and fitting just the points which are still changing significantly
	 */
	protected void run() {
		int iterCount = 0;
		do {

			comm.message("Iteration number " + iterCount, VerbLevel.verb_debug);
			iterCount++;

			// Do a relaxation step
			comm.message("Updating " + updater.inds2Update().length
					+ " backbones", VerbLevel.verb_debug);
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
	private FloatPolygon generateNewBackbone(
			Vector<FloatPolygon> targetBackbones) {

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

			float[] targetX = targetBackbones.get(tb).xpoints;
			float[] targetY = targetBackbones.get(tb).ypoints;
			float[] weights = Forces.get(tb).getWeights();

			for (int k = 0; k < params.numBBPts; k++) {
				if (weights[k] != 0) {
					newX[k] += targetX[k] * weights[k];
					newY[k] += targetY[k] * weights[k];
					normFactors[k] += weights[k];
				}
			}

		}
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
