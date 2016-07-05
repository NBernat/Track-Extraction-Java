package TrackExtractionJava;

import ij.ImagePlus;
import ij.gui.PolygonRoi;
import ij.process.FloatPolygon;
import ij.text.TextWindow;
import ij.util.ArrayUtil;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Arrays;
import java.util.HashMap;
import java.util.ListIterator;
import java.util.Scanner;
import java.util.Vector;

import com.sun.xml.internal.bind.v2.runtime.unmarshaller.XsiNilLoader.Array;

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
	 * Place to store the full track when working on segments of tracks
	 */
	Track fullTrack = null;
	
	Track oldTrack = null;

	/**
	 * The track that is being fit
	 */
	Track workingTrack;
	int newTrID=-1;
	
	Track errTrack=null;
	
	/**
	 * A List of (references to) the BTP's in track, worked on during fitting algorithm
	 */
	Vector<BackboneTrackPoint> BTPs;
	
	private BBFPointListGenerator bplg;
	
	protected boolean clipEnds = false;
	protected int BTPstartFrame = -1;
	protected int BTPendFrame = -1;
	private Vector<TrackPoint> startClippings;
	private Vector<TrackPoint> endClippings;
	
	Vector<Gap> bentLarvae;
	Vector<Gap> straightLarvae;
	
	/**
	 * The amount that each backbone in the track shifted during the last
	 * iteration that acted upon any given point
	 */
	private double[] shifts;

	private BBFUpdateScheme updater;
	
	private int pass;
	
	private boolean hidePoints = false;
	
	private boolean useScaleFactors = true;
	
	private boolean diverged = false;
	protected int divergedInd = -1;
	Vector<Gap> divergedGaps;
	
	Vector<Gap> invalidGaps;
	
	Vector<Integer> numIters;
	Vector<EnergyProfile> energyProfiles;
	
	transient Communicator comm;
	transient Communicator bbcomm;

	
	boolean doPause = false;
	Scanner userIn = null;
	PrintStream userOut = null;
	ImagePlus pauseStack = null;
	MaggotDisplayParameters pauseDisplayParams = null;
	
	
	
	/**
	 * Constructs a backbone fitter
	 */
	public BackboneFitter(Track t) {
		init(t, null);
	}
	
	/*
	 * For backwards compatibility
	 */
	public BackboneFitter() {
		init(null, null);
	}
	
	BackboneFitter(Track t, FittingParameters fp){
		init(t, fp);
	}
	
	/*
	 * For backwards compatibility
	 */
	BackboneFitter(FittingParameters fp){
		init(null, fp);
	}
	
	private void init(Track t, FittingParameters fp) {

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
		
		initTrack(t);

		bplg = new BBFPointListGenerator(this, workingTrack, params, comm);
		
		invalidGaps = new Vector<Gap>();
		divergedGaps = new Vector<Gap>();
	}

	private void initEnergyProfiles(){
		energyProfiles = new Vector<EnergyProfile>();
		for (int i=0; i<Forces.size(); i++){
			energyProfiles.add(new EnergyProfile(Forces.get(i).getName()));
		}
		energyProfiles.add(new EnergyProfile("Total"));
		numIters = new Vector<Integer>();
	}
	
	private void initTrack(Track tr){
		
		if (tr==null){
			return;
		}
		oldTrack = tr;
		
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
		
		if (!generateFullWorkingTrack(tr)) {//creates the new track
			comm.message("Error converting track points to btp", VerbLevel.verb_error);
		}
		 
		
	}
	
	
	private void clearPrev(){
		workingTrack = null;
		hidePoints = false;
		shifts = null;
		updater = null;
		pass = 0;
		Forces = params.getForces(pass);
		diverged = false;
		BTPs = new Vector<BackboneTrackPoint>();
		clipEnds = false;
		BTPstartFrame=-1;
		BTPendFrame=-1;
		if (params.storeEnergies){
			initEnergyProfiles();
		}
	}
	
	protected void resetFitter(){
		shifts = null;
		updater = null;
		pass = 0;
		Forces = params.getForces(pass);
		diverged = false;
		BTPs = new Vector<BackboneTrackPoint>();
		clipEnds = false;
		BTPstartFrame=-1;
		BTPendFrame=-1;
		if (params.storeEnergies){
			initEnergyProfiles();
		}
	}
	
	protected void resetParams(FittingParameters fp){
		params = fp;
		bplg.params = fp;
		resetForNextExectution();
	}
	
	protected void resetForNextExectution(){
		pass = 0;
		Forces = params.getForces(pass);
	}
	
	/**
	 * For backwards compatibility
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
		
		
		if (noError && generateFullWorkingTrack(tr)) {//creates the new track
			//If there was no error extraction points, run the different grain passes of the algorithm
			noError = true;
			for(int i=0; (i<params.grains.length && noError); i++){
				noError = doPass(params.grains[i]);
				if (!noError) {
					comm.message("Error on track "+tr.getTrackID()+"("+workingTrack.getTrackID()+") pass "+i+"(grain "+params.grains[i]+") \n ---------------------------- \n \n", VerbLevel.verb_error);
					errTrack = workingTrack;
					workingTrack = null;
				}
				pass++;
				Forces = params.getForces(pass);
				
				
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
	 * Fits backbones to the points in the given track.
	 * <p>
	 * After fitting, the Vector of BackboneTrackPoints can be accessed via
	 * getBackbonePoints()
	 * 
	 */
	public void fitTrack() {
		
//		clearPrev();
		
		if (workingTrack.points.size()<params.minTrackLen){
			errTrack = workingTrack; 
			System.out.println("Track length below fitparams.mintracklen");
			workingTrack = null;
			return;
		}
		
		boolean noError = true;

		// Extract the points, and move on (if successful)
		comm.message("Extracting maggot tracks", VerbLevel.verb_debug);
		
		noError = true;
		for(int i=0; (i<params.grains.length && noError); i++){
			noError = doPass(params.grains[i]);
			if (!noError) {
				comm.message("Error on track ("+workingTrack.getTrackID()+") pass "+i+"(grain "+params.grains[i]+") \n ---------------------------- \n \n", VerbLevel.verb_error);
				errTrack = workingTrack;
//				workingTrack = null;
			}
			pass++;
			if (pass<params.grains.length) Forces = params.getForces(pass);
		}
		

		//run divergence fixer 
		if (!noError){
			if (diverged && params.refitDiverged){
				System.out.println("Track diverged, attempting to fix");
				handleDivergence();
			} else {
				workingTrack = null;
			}
			
		}
		
		System.out.println("Done fitting track");
	}

	protected void fitTrackSubset_IncludeSurrounding_old(int startInd, int endInd){
		
		useScaleFactors = true;
		bplg.startInd = startInd;
		bplg.endInd = endInd;
		
		for (int i=0; i<workingTrack.points.size(); i++){
			if (i<startInd || i>endInd){
				((BackboneTrackPoint)bplg.workingTrack.points.get(i)).scaleFactor = 0;
			}
		}
		
		
		fitTrack();
	}
	
	
	
	
	public void fitTrackNewScheme(){
		
		if (workingTrack.points.size()<params.minTrackLen){
			errTrack = workingTrack; 
			System.out.println("Track length below fitparams.mintracklen");
			workingTrack = null;
			return;
		}
		
		//Segment the track 
		bentLarvae = findBentGaps(workingTrack.getHTdists());
		straightLarvae = findStraightGaps(bentLarvae);
		
		//Fit the straight larvae
		// TODO turn this block into its own function
		FittingParameters spp = FittingParameters.getSinglePassParams();
		spp.freezeDiverged = true;
		resetParams(spp);
		boolean hideGapPoints = true;
		if (userOut!=null) userOut.println("Fitting Straight Subsets: "+straightLarvae.toString());
		fitSubsets(straightLarvae, hideGapPoints);
		if (workingTrack==null){
			System.out.println("Error fitting straight larvae");
			return;
		}
		
		
		//Fit the bent larvae
		// TODO turn this block into its own function
		hideGapPoints = false;
		spp.leaveFrozenBackbonesAlone = true;//This tells the plg not to re-initialize the frozen bb's
		spp.freezeDiverged = true;
		resetParams(spp);
		if (userOut!=null) userOut.println("Fitting Bent Subsets: "+bentLarvae.toString());
		fitSubsets(bentLarvae, hideGapPoints);
		if (workingTrack==null){
			System.out.println("Error fitting bent larvae");
			return;
		}
		

		//Patch diverged sections
		// TODO turn this block into its own function
		FittingParameters patchParams = new FittingParameters();
		patchParams.leaveFrozenBackbonesAlone = true;//This tells the plg not to re-initialize the frozen bb's
		patchParams.freezeDiverged = true;
		patchParams.leaveBackbonesInPlace = true;
		resetParams(patchParams); 
		for (Gap divG : divergedGaps){
//			patchTrackSubset(divG, params.divergedPatchBuffer, patchParams);
			boolean doPrev =  divG.start != 0;
			boolean doNext =  divG.end != (workingTrack.getNumPoints()-1);
			if (userOut!=null) userOut.println("Patching Diverged Subset: "+divG.toString());
			patchGap_InchInwards(divG, params.edgeSize, doPrev, doNext);
			
		}
		if (workingTrack==null){
			System.out.println("Error patching diverged frames");
			return;
		}
		
		//Inch inwards on the remaining bad gaps
		Vector<Gap> badGaps = findBadGaps();
		FittingParameters edgeParams = FittingParameters.getSinglePassParams();
		edgeParams.leaveFrozenBackbonesAlone = true;//This tells the plg not to re-initialize the frozen bb's
		edgeParams.freezeDiverged = true;
		edgeParams.leaveBackbonesInPlace = true;
		resetParams(edgeParams); 
//		int count = 0;
//		int maxCount = 10;
//		while (badGaps.size()>0 && count<maxCount){
			for (Gap badG : badGaps){
				boolean doPrev =  badG.start != 0;
				boolean doNext =  badG.end != (workingTrack.getNumPoints()-1);
				if (userOut!=null) userOut.println("Patching Bad Subset: "+badG.toString());
				patchGap_InchInwards(badG, params.edgeSize, doPrev, doNext);
			}
			badGaps = findBadGaps();
//			count++;
//		}
		
		//Do final run on the whole track for continuity
		spp = FittingParameters.getSinglePassParams();
		spp.leaveFrozenBackbonesAlone = true;//This tells the plg not to re-initialize the frozen bb's
		spp.freezeDiverged = true;
		spp.leaveBackbonesInPlace = true;
		resetParams(spp);
		fitTrack();
		
	}	
		
		
		
	public void runSingleIteration(){
			
		//Run one iteration of fitter on entire track to get energies 
		boolean noError = bplg.generateFullBTPList();
		BTPs = bplg.getBTPs();
		
		if (params.storeEnergies){
			for (int i=0; i<energyProfiles.size(); i++){
				energyProfiles.get(i).initEnergyEntry(BTPs.size());
			}
		}
		
		if (noError){
			updater = new BBFUpdateScheme(BTPs.size());
			shifts = new double[BTPs.size()];
			
			relaxBackbones(updater.inds2Update());
			
			if (params.storeEnergies){
				for (int i=0; i<energyProfiles.size(); i++){
					energyProfiles.get(i).storeProfile();
				}
			}
			
			calcShifts();
			setupForNextRelaxationStep();
		} else {
			System.out.println("Error generating btplist");
		}
		
		//Find high energy areas
//		Vector<Gap> straightGaps = findStraightGaps();
//		Vector<Gap> bentGaps = findBentGaps();
		
//		fullTrack = workingTrack;
		//Run fitter on low energy energy gaps
//		for (Gap sGap : straightGaps){
//			
//			//fitTrackSubset_IgnoreSurrounding(leGap.start, leGap.end);
//		}
//		
//		for (Gap bGap : bentGaps){
//			//fitTrackSubset_IncludeSurrounding(heGap.start, heGap.end);//, params.divBufferSize, false);
//		}
//		
		
		//Fit entire track?
	}
	
	
	private Vector<Gap> findStraightGaps(double[] htDists, double mean, double stdDev){
			
		return findStraightGaps(findBentGaps(htDists, mean, stdDev));
	}
	
	private Vector<Gap> findStraightGaps(Vector<Gap> bent){
		
		return complementarySegs(bent, 0, workingTrack.points.size()-1);
	}
	
	private Vector<Gap> findBentGaps(double[] htDists){
		double mean = MathUtils.mean(htDists);
		return findBentGaps(htDists, mean, MathUtils.stdDev(htDists, mean));
	}
	private Vector<Gap> findBentGaps(double[] htDists, double mean, double stdDev){
		
		
		boolean[] bent = new boolean[htDists.length];
		for (int i=0; i<bent.length; i++){
			bent[i] = htDists[i]<(mean-stdDev*params.fracOfStdDevForBentCutoff);
		}
		Vector<Gap> bentGaps = Gap.bools2Segs(bent);
		if (bentGaps.size()>1) BBFPointListGenerator.mergeGaps(bentGaps, params.minValidSegmentLen, null);
		
		
		return bentGaps;
	}
	
	
	/**
	 * Returns the list of segments within start:end (inclusive)) that are complementary to the given segments
	 * @param segs
	 * @param start
	 * @param end
	 * @return
	 */
	public static Vector<Gap> complementarySegs(Vector<Gap> segs, int start, int end){
		
		Vector<Gap> comps = new Vector<Gap>();
		int s = start;
		for (Gap seg : segs){
			int e = seg.start-1;
			if (e-s>0 && start<=s && e<=end){
				comps.add(new Gap(s, e));
			}
			s = seg.end+1; 
		}
		if (end-s>0){
			comps.add(new Gap(s, end));
		}
		
		return comps;
	}
	
	public void fitSubsets(Vector<Gap> subsets, boolean hideGapPoints, FittingParameters fp){
		FittingParameters oldFP = params;
		resetParams(fp);
		
		fitSubsets(subsets, hideGapPoints); 
		
		resetParams(oldFP);
	}
	
	public void fitSubsets(Vector<Gap> subsets, boolean hideGapPoints){

		if (subsets==null || subsets.size()==0){
			return;
		}
		
		boolean usfOld = useScaleFactors;
		
		useScaleFactors = true;
		
		setFrozenGapsInTrack(subsets, true);
		if (hideGapPoints) setHiddenGapsInTrack(subsets, true); 
		
		fitTrack();
		
		setFrozenGapsInTrack(subsets, false);
		if (hideGapPoints) setHiddenGapsInTrack(subsets, false); 
		
		useScaleFactors = usfOld;
	}
	
	/**
	 * Runs the fitter on 2*EDGESIZE points: the first EDGESIZE and last EDGESIZE points in the given subset
	 * @param subset
	 * @param edgeSize
	 */
//	public boolean fitSubsetEdges(Gap subset, int edgeSize){
//		return fitSubsetEdges(subset, edgeSize, true);
//	}
	
	
	public boolean fitSubsetEdges(Gap subset, int edgeSize, boolean freezeHideInner, boolean doPrev, boolean doNext){
		
		int freezeHideS = (doPrev)? (subset.start+edgeSize) : subset.start;
		int freexeHideE = (doNext)? (subset.end-edgeSize) : subset.end;
		
		
		Track tempTrack = workingTrack;
		replaceBBInfoInSubsetEdges(subset, edgeSize, doPrev, doNext);
		
		//Hide/Freeze inner points manually
		if (freezeHideInner){
			setFrozen(freezeHideS, freexeHideE, true);
			setHidden(freezeHideS, freexeHideE, true);
		}
		
		Vector<Gap> subs = new Vector<Gap>();
		subs.add(subset);
		fitSubsets(subs, false);
		
		//Unhide/Unfreeze inner points manually
		if (freezeHideInner){
			setFrozen(freezeHideS, freexeHideE, false);
			setHidden(freezeHideS, freexeHideE, false);
		}

		if (workingTrack == null){
			workingTrack = tempTrack;
			return false;
		}
		
		return true;
		
	}
	
	protected void replaceBBInfoInSubsetEdges(Gap subset, int edgeSize){
		replaceBBInfoInSubsetEdges(subset, edgeSize, true, true);
	}
	
	/**
	 * Fills in the subset edges with backbone info from surrounding points
	 * @param subset
	 * @param edgeSize
	 */
	protected void replaceBBInfoInSubsetEdges(Gap subset, int edgeSize, boolean doPrev, boolean doNext){
		
		float[] prevO = {0,0};
		float[] nextO = {0,0};
		PolygonRoi prevBB = null;
		PolygonRoi nextBB = null;
		if (doPrev) prevBB = ((BackboneTrackPoint)workingTrack.points.get(subset.start-1)).backbone;
		if (doNext) nextBB = ((BackboneTrackPoint)workingTrack.points.get(subset.end+1)).backbone;
		for (int i=0; i<edgeSize; i++){
			if (doPrev) ((BackboneTrackPoint)workingTrack.points.get(subset.start+i)).fillInBackboneInfo(params.clusterMethod, prevBB, prevO);
			if (doNext) ((BackboneTrackPoint)workingTrack.points.get(subset.end-i)).fillInBackboneInfo(params.clusterMethod, nextBB, nextO);
		}
		
	}
	
	/**
	 * Freezes all the points OTHER THAN the points contained within the "unfrozen" subsets
	 * @param unfrozen
	 * @param frozen
	 */
	private void setFrozenGapsInTrack(Vector<Gap> unfrozen, boolean frozen){
		if (unfrozen.firstElement().start>0){//gap at beginning
			setFrozen(0, unfrozen.firstElement().start-1, frozen);
		}
		
		for (int i=0;i<(unfrozen.size()-1);i++){//gap in middle
			setFrozen(unfrozen.get(i).end+1, unfrozen.get(i+1).start-1, frozen);
		}
		
		if (unfrozen.lastElement().end<(bplg.workingTrack.points.size()-1)){//gap at end
			setFrozen(unfrozen.lastElement().end+1, bplg.workingTrack.points.size()-1, frozen);
		}
	}

	/**
	 * Hides all the points OTHER THAN the points contained within the "hidden" subsets
	 * @param unhidden
	 * @param hidden
	 */
	private void setHiddenGapsInTrack(Vector<Gap> unhidden, boolean hidden){
		if (unhidden.firstElement().start>0){//gap at beginning
			setHidden(0, unhidden.firstElement().start-1, hidden);
		}
		
		for (int i=0;i<(unhidden.size()-1);i++){//gap in middle
			setHidden(unhidden.get(i).end+1, unhidden.get(i+1).start-1, hidden);
		}
		
		if (unhidden.lastElement().end<(bplg.workingTrack.points.size()-1)){//gap at end
			setHidden(unhidden.lastElement().end+1, bplg.workingTrack.points.size()-1, hidden);
		}
	}
	
	protected boolean patchTrackSubset(Gap segment, int bufferSize){
		FittingParameters patchParams = new FittingParameters();
		patchParams.leaveFrozenBackbonesAlone = true;
		patchParams.storeEnergies = true;
		
		return patchTrackSubset(segment, bufferSize, patchParams); 
	}
	
	protected boolean patchTrackSubset(Gap segment, int bufferSize, FittingParameters patchParams){
		
		FittingParameters oldParams = params;
		boolean usfOld = useScaleFactors;
		
		Vector<Gap> unbuffered = new Vector<Gap>();
		unbuffered.add(segment);
		Vector<Gap> buffered = new Vector<Gap>();
		buffered.add(new Gap(segment.start-bufferSize, segment.end+bufferSize));
		

		resetParams(patchParams);
		useScaleFactors = true;
		
		setFrozenGapsInTrack(unbuffered, true);
		setHiddenGapsInTrack(buffered, true);
		
		boolean success = true;
		Track prefit = workingTrack;
		fitTrack();
		if (workingTrack==null){
			success=false;
			workingTrack = prefit;
		}
		
		setFrozenGapsInTrack(unbuffered, false);
		setHiddenGapsInTrack(buffered, false);
		
		resetParams(oldParams);
		useScaleFactors = usfOld;
		
		return success;
	}
	
	protected Vector<Gap> findBadGaps(){
		
		double[] meanStdDev = workingTrack.getEnergyMeanStdDev(params.energyTypeForBadGap);
		double thresh = meanStdDev[0] + params.numStdDevForBadGap*meanStdDev[1];
		
		double[] e = workingTrack.getEnergies(params.energyTypeForBadGap);
		boolean[] bad = new boolean[e.length];
		for (int i=0; i<bad.length; i++){
			bad[i] = e[i]>thresh;
		}
		
		Vector<Gap> badGaps = Gap.bools2Segs(bad);
		if (badGaps.size()>1) BBFPointListGenerator.mergeGaps(badGaps, params.minValidSegmentLen, null);
		
		
 		return badGaps;
	}
	
	/**
	 * Sequentially fits points on the edges of the gaps until all are fit. 
	 * 
	 * NOTE: Boundary cases need to be updated for case when edgeSize!=1
	 * 
	 * @param badG
	 * @param edgeSize
	 * @return
	 */
	protected boolean patchGap_InchInwards(Gap badG, int edgeSize, boolean doPrev, boolean doNext){
		
		if (!doPrev && !doNext) return false;
		
//		float oldIWt = params.imageWeight*2;
		float[] oldwts = params.timeLengthWeight;
		for (int i=0; i<params.timeLengthWeight.length; i++) {
			params.timeLengthWeight[i]=params.timeLengthWeight[i]*5;
		}
		
		Gap unfit = new Gap(badG.start, badG.end);
		boolean success = true;
		int numPtsCutoff = (doPrev && doNext)? 2 : 1;   
		while (unfit.size()>numPtsCutoff && success){
			resetForNextExectution();
			success = fitSubsetEdges(unfit, edgeSize, true, doPrev, doNext); 
			
			if (success){
				if (doPrev) unfit.start++;
				if (doNext) unfit.end--;
				
				if (unfit.size()<=numPtsCutoff){
					resetForNextExectution();
					success = fitSubsetEdges(unfit, 1, false, doPrev, doNext);
				}
			}
			
		}
		
		
		resetForNextExectution();
		Vector<Gap> bads = new Vector<Gap>();
		bads.add(badG);
		fitSubsets(bads, false);
//		patchTrackSubset(badG, params.divBufferSize);
		
//		params.imageWeight = oldIWt;
		params.timeLengthWeight = oldwts;
		return success;
	}
	
	
	
	public void fitTrackSubsets_IgnoreSurrounding(Vector<Gap> subsets){
		fitSubsets(subsets, true);
	}
	
	public void fitTrackSubsets_IncludeSurrounding(Vector<Gap> subsets){
		fitSubsets(subsets, false);
	}
	
	public void fitTrackSubset_IgnoreSurrounding_old(int startInd, int endInd){
		fitTrackSubset_IgnoreSurrounding_old(startInd, endInd, true);
	}
	
	/**
	 * Fits a subset of the working track. the full track is stored in fullTrack
	 */
	private void fitTrackSubset_IgnoreSurrounding_old(int startInd, int endInd, boolean saveFullTrack){
//		subsets = true;
		if (saveFullTrack) fullTrack = workingTrack;
		workingTrack = new Track(fullTrack, startInd, endInd);
		bplg.workingTrack = workingTrack;
		fitTrack();
	}
	
	
	/**
	 * Marks points so that they are not changed at update time (bbnew=bbold, shift=0)
	 * @param startInd index of workingTrack 
	 * @param endInd index of workingTrack 
	 */
	protected void setFrozen(int startInd, int endInd, boolean freeze){

		for (int i=startInd; i<=endInd; i++){
			if (i>=0 && i<bplg.workingTrack.points.size()) {
				((BackboneTrackPoint)bplg.workingTrack.points.get(i)).setFrozen(freeze);
			}
		}
	
	}

	/**
	 * Marks points so that they are not used during calculation
	 * @param startInd
	 * @param endInd
	 */
	protected void setHidden(int startInd, int endInd, boolean hide){
		
		for (int i=startInd; i<=endInd; i++){
			if (i>=0 && i<bplg.workingTrack.points.size()) {
				((BackboneTrackPoint)bplg.workingTrack.points.get(i)).setHidden(hide);
			}
		}
//		updater.hidePoints(getHiddenBTPs());
		
	}
	
	/**
	 * Creates a new track full of BTPs out of the points in the track
	 * <p>
	 * 
	 * @return A list of booleans indicating whether or not the midline in the
	 *         corresponding BTP is empty
	 */
	private boolean generateFullWorkingTrack(Track tr) {
		
		boolean noerror=true;
		try {

			BTPs = new Vector<BackboneTrackPoint>();
			
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
				
				workingTrack = new Track(BTPs, tr);
				workingTrack.setTrackID(tr.getTrackID());
				newTrID = workingTrack.getTrackID();
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

		if (!noerror) workingTrack = null;
		return noerror;
	}

	
	private boolean doPass(int grain){
				
		//Set the BTPs for this pass
		if (!setupForPass()) {
			return false;
		}
		
		//Run the actual fitter
		try {
			runFitter();
			if (diverged) {
				comm.message("Track "+workingTrack.getTrackID()+" diverged", VerbLevel.verb_error);
				return false;
			}
			if (params.storeEnergies){
				numIters.add(updater.getIterNum());
			}
		} catch(Exception e){
			StringWriter sw = new StringWriter();
			PrintWriter pw = new PrintWriter(sw);
			e.printStackTrace(pw);
			comm.message("Error during BackboneFitter.runFitter() at grain "+grain+"\n"+sw.toString(), VerbLevel.verb_error);
			return false;
		} 
		
		return true;
	}
	
	private boolean setupForPass(){
		BTPs.removeAllElements();
		bplg.reset();
		boolean noError = bplg.generateBTPList(pass);
		BTPs = bplg.getBTPs();
		
		if (noError){
			updater = new BBFUpdateScheme(BTPs.size());
			if (hidePoints) updater.hidePoints(getHiddenBTPs());
			
			shifts = new double[BTPs.size()];
		} else{
			comm.message("Error generating backbones at pass "+pass, VerbLevel.verb_error);
		}
		
		return noError;
	}
	
	
	private boolean[] getFrozenBTPs(){
		
		boolean[] frozen = new boolean[BTPs.size()];
		for (int i=0; i<BTPs.size(); i++) frozen[i] = BTPs.get(i).frozen;
		return frozen;
	}
	
	private boolean[] getHiddenBTPs(){
	
	boolean[] hidden = new boolean[BTPs.size()];
	for (int i=0; i<BTPs.size(); i++) hidden[i] = BTPs.get(i).hidden;
	return hidden;
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
			calcShifts();
			if (diverged && params.freezeDiverged){
				Gap div = findDivergedGap();
				int trackIndStart = BTPs.get(div.start).frameNum-bplg.workingTrack.points.firstElement().frameNum;
				int trackIndEnd = BTPs.get(div.end).frameNum-bplg.workingTrack.points.firstElement().frameNum;
				setFrozen(trackIndStart, trackIndEnd, true); 
				setHidden(trackIndStart, trackIndEnd, true);
				divergedGaps.add(new Gap(trackIndStart, trackIndEnd));
				diverged =false;
			}
			
			
			if (doPause){
				pause();
			}
			
			
			setupForNextRelaxationStep();
			
			// Show the fitting messages, if necessary
			if (!updater.comm.outString.equals("")) {
				new TextWindow("TrackFitting Updater, pass "+pass, updater.comm.outString, 500, 500);
			}
			
		} while (!diverged && updater.keepGoing(shifts));
		
		System.out.println("Number of iterations: "+updater.getIterNum());
		
		if (!diverged) {
			finalizeBackbones();
		}
		return !diverged;

	}

	/**
	 * Allows the backbones to relax to lower-energy conformations
	 * 
	 * @param inds
	 *            The indices of the BTPs which should be relaxed
	 */
	private void relaxBackbones(boolean[] inds) {
		
		for (int i = 0; i < inds.length; i++) {
			comm.message("Relaxing frame " + i, VerbLevel.verb_debug);
			// Relax each backbone

			if (inds[i]) {
				if (useScaleFactors){
					double sf = (BTPs.get(i).frozen) ? 0 : BTPs.get(i).scaleFactor; 
					bbRelaxationStep(i, sf);
				} else {
					bbRelaxationStep(i);
				}
			} else if (params.storeEnergies){
				for (int j=0; j<energyProfiles.size(); j++){
					energyProfiles.get(j).addEnergyEntry(i, -1);
				}
				
			}
		}
		
	}

	/**
	 * Relaxes a backbone to a lower-energy conformation
	 * 
	 * @param btpInd
	 *            Index of the BTP (in BTDs) to be processed
	 */
	private void bbRelaxationStep(int btpInd) {
		bbRelaxationStep(btpInd, 1);
	}
	private void bbRelaxationStep(int btpInd, double scaleFactor) {
		//scaleFactor is the amount by which the step is multiplied; 1 is standard, 0 does not update

		// IJ.showStatus("Getting target backbones in frame "+btpInd);
		// Get the lower-energy backbones for each individual force
		if (scaleFactor > 0) {
			Vector<FloatPolygon> targetBackbones = getTargetBackbones(btpInd);
			
			if (params.storeEnergies){
				for (int i=0; i<targetBackbones.size(); i++){
					energyProfiles.get(i).addEnergyEntry(btpInd, Force.getEnergy(targetBackbones.get(i), BTPs.get(btpInd)));
				}
			}
			
			bbcomm.message("Frame "+btpInd+" Components:", VerbLevel.verb_debug);
			
			FloatPolygon newBB = CVUtils.fPolyAdd(CVUtils.fPolyMult(generateNewBackbone(targetBackbones), scaleFactor), CVUtils.fPolyMult(BTPs.get(btpInd).bbOld, 1-scaleFactor));//scaleFactor * generateNewBackbone(targetBackbones) + (1-scaleFactor) * BTPs.get(btpInd).bbOld;
			
			if (params.storeEnergies){
				energyProfiles.lastElement().addEnergyEntry(btpInd, Force.getEnergy(newBB, BTPs.get(btpInd))/params.grains[pass]);
			}
			
			BTPs.get(btpInd).setBBNew(newBB);
		} else {
			BTPs.get(btpInd).setBBNew(BTPs.get(btpInd).bbOld);
			if (params.storeEnergies){
				for (int j=0; j<energyProfiles.size(); j++){
					energyProfiles.get(j).addEnergyEntry(btpInd, -1);
				}
			}
		}
		
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
		try{
			for (int i=0; i<Forces.size(); i++){
				
				FloatPolygon tb = Forces.get(i).getTargetPoints(btpInd, BTPs);
				targetBackbones.add(tb);
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

	
	private void calcShifts(){
		for (int i = 0; i<BTPs.size(); i++) {//for (int i = 0; (i<BTPs.size() && !diverged); i++) {
			shifts[i] = BTPs.get(i).calcPointShift();
			
			//Check for divergence
			if (!BTPs.get(i).frozen && BTPs.get(i).diverged(params.divergenceConstant)){
				diverged = true;
				divergedInd=i;
			} else {
				//BTPs.get(i).setupForNextRelaxationStep();
			}
		}
	}
	
	private void setupForNextRelaxationStep(){
		
		for (int i = 0; i<BTPs.size(); i++) {
			BTPs.get(i).setupForNextRelaxationStep();
		}
	}
	
	
	protected void finalizeBackbones() {
		
		for (int j=0; j<BTPs.size(); j++) {
			HashMap<String, Double> energies = new HashMap<String, Double>();
			for (int i=0; i<Forces.size(); i++){
				energies.put(Forces.get(i).name, new Double(Forces.get(i).getEnergy(j, BTPs)));
			}
			energies.put("Total", new Double(Force.getEnergy(generateNewBackbone(getTargetBackbones(j)), BTPs.get(j))));
			BTPs.get(j).storeEnergies(energies);
			BTPs.get(j).finalizeBackbone();
		}
	}
	
	
	
	private void handleDivergence(){
		
		fullTrack = workingTrack;
		//Find easy subsets
		Gap divGap = findDivergedGap();
		
		int[] startInds = {0, divGap.end+1};
		int[] endInds = {divGap.start-1, workingTrack.points.size()-1};
		
		if (endInds[0]>0 && startInds[1]<(workingTrack.points.size()-1)){//The divergence is in the middle
			//Run fitter on each subset 
			BackboneFitter leftSegment = new BackboneFitter(new Track(oldTrack, startInds[0], endInds[0]), params);
			BackboneFitter rightSegment = new BackboneFitter(new Track(oldTrack, startInds[1], endInds[1]), params);
			leftSegment.fitTrack();
			rightSegment.fitTrack();
			
			if ( leftSegment.fixed() && rightSegment.fixed() ){
				diverged = false;
				//Merge the tracks
				mergeTrack(leftSegment.getTrack());
				mergeTrack(rightSegment.getTrack());
				
				if (params.fixDiverged){
					fixDivergedGap(divGap);
					if (fixed()){
						//Wahoo! Everything worked.
						topOffFullTrack();
					} else {
						markGapInvalid(divGap);
					}
				} else {
					markGapInvalid(divGap);
				}
				
	
				addInvalidGapsFromFitter(leftSegment);
				addInvalidGapsFromFitter(rightSegment);
				
	
				workingTrack = fullTrack;
				
			} else {
				
				if (leftSegment.fixed()) {
					
					// TODO check rightSegment for good pieces (in workingTrack)
					
					System.out.println("Replacing track "+workingTrack.getTrackID()+"("+workingTrack.getNumPoints()+"pts) with non-diverged subset track "+leftSegment.getTrack().getTrackID()+" ("+leftSegment.getTrack().getNumPoints()+"pts)");
					errTrack = rightSegment.getTrack();
					workingTrack = leftSegment.getTrack();
					diverged = false;
	//				mergeTrack(leftSegment.getTrack());
	//				addInvalidGapsFromFitter(leftSegment);
	//				diverged = false;
	//				s = divGap.start;
	//				e = endInds[1];
				}else if (rightSegment.fixed()){
					
					// TODO check leftSegment for good pieces (in workingTrack)
					
					System.out.println("Replacing track "+workingTrack.getTrackID()+"("+workingTrack.getNumPoints()+"pts) with non-diverged subset track "+rightSegment.getTrack().getTrackID()+" ("+rightSegment.getTrack().getNumPoints()+"pts)");
					errTrack = leftSegment.getTrack();
					workingTrack = rightSegment.getTrack();
	//				mergeTrack(rightSegment.getTrack());
	//				addInvalidGapsFromFitter(rightSegment);
	//				diverged = false;
	//				s = startInds[0];
	//				e = divGap.end;
				} else {
	//				Gap invalidGap = new Gap(startInds[0], endInds[1]); 
	//				markGapInvalid(invalidGap);
					diverged = true;
					errTrack = workingTrack;
					workingTrack = null;
				}
				
			}
			
			
		} else{//The divergence IS on an end
			// TODO clip the ends 
			
		}
		
		
	}
	
	
	private Gap findDivergedGap(){
		
		//shifts, pass, divergedInd
		//use shifts to find list of inds
		
		//find median of shifts
		double[] sorted = Arrays.copyOf(shifts, shifts.length);
		Arrays.sort(sorted);
		double median = sorted[sorted.length/2];
		
		//Find the segment surrounding the divergance event
		int s = -1; 
		int e = -1;
		for (int i=divergedInd; (e==-1 && i<shifts.length); i++) if (shifts[i]<=median) e=i;
		for (int i=divergedInd; (s==-1 && i>=0); i--) if (shifts[i]<=median) s=i; 
		if (s==-1) s=0;
		if (e==-1) e=(shifts.length-1);
		
		return (s!=-1 && e!=-1) ? new Gap(s, e) : null;
		
	}
	
	
	private void addInvalidGapsFromFitter(BackboneFitter bbf){
		for (Gap g : bbf.invalidGaps){
			
			int indOffset = bbf.fullTrack.points.firstElement().frameNum - fullTrack.points.firstElement().frameNum;
			invalidGaps.addElement(new Gap(g.start+indOffset, g.end+indOffset));
		}
	}
	
	/**
	 * Merges the points of track t into fullTrack
	 * 
	 * If t is not a subset of fullTrack, fullTrack is not changed 
	 * 
	 * @param t A track whose list of points is a (duplicated) subset of fullTrack 
	 */
	private void mergeTrack(Track t){
		
		//find point in fullTrack that corresponds to t.points.get(0);
		int ftZeroInd = t.points.firstElement().frameNum-fullTrack.points.firstElement().frameNum;
		
		if (ftZeroInd<0 || ftZeroInd>=fullTrack.points.size() || (ftZeroInd+t.points.size()-1)>=fullTrack.points.size()){
			//t is not a subset of fullTrack
			return;
		}
		
		//replace points
		for (int i=0; i<t.points.size(); i++){
			fullTrack.points.setElementAt(t.points.get(i), ftZeroInd+i); 
		}
		
		
		
	}
	
	
	private void fixDivergedGap(Gap g){
		//TODO
		//fitTrackSubsetWithBuffer(g.start, g.end, params.divBufferSize, true);
		
	}
	
	private void topOffFullTrack(){
		// TODO
		//Make sure to properly handle the fact that some points in the fixed track segments 
		//may be invalid
	}
	
	private void markGapInvalid(Gap g){
		
		for (int i=g.start; i<g.end; i++){
			BackboneTrackPoint btp = (BackboneTrackPoint)fullTrack.points.get(i);
			btp.bbvalid = false;
			btp.bbOld = btp.bbInit;
			btp.finalizeBackbone();
		}
		
		invalidGaps.addElement(g);
	}
	
	private void pause() {
		
		outputData();

		if (userOut!=null) userOut.println("Enter any character to continue...");
		userIn.next();
		if (userOut!=null) userOut.println("...Continuing");
		
	}
	
	
	private void outputData(){
		
		if (userOut!=null) userOut.println("Iteration "+updater.getIterNum());
		
		if (pauseStack!=null){
			//Update stack
			pauseDisplayParams.contour = !pauseDisplayParams.contour;
			pauseStack.setStack(bplg.workingTrack.getMovieStack(bplg.workingTrack.getTrackID(), pauseDisplayParams, false).getImageStack());
			
		} else {
			//Set parameters and show first stack
			pauseDisplayParams = new MaggotDisplayParameters();
			pauseDisplayParams.setAllFalse();
			pauseDisplayParams.newBB = true;
			pauseDisplayParams.initialBB = true;
			pauseDisplayParams.contour = true;
			pauseDisplayParams.mid = true;
			
			pauseStack = bplg.workingTrack.playMovie(bplg.workingTrack.getTrackID(), pauseDisplayParams);
		}
		
	}
	
	
	
	
	
	
	
	
	
	
	
	
	public Vector<BackboneTrackPoint> getBackboneTrackPoints() {
		return BTPs;
	}

	public String getForceName(int ind) {
		return Forces.get(ind).getName();
	}
	
	
	public Track getTrack(){
//		return (subsets)? fullTrack : workingTrack;
		return workingTrack;
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
	
	public boolean fixed(){
		return (!diverged && errTrack==null);
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
	
	public int size(){
		return end-start;
	}
	
	public void add(int i){
		start+=i;
		end+=i;
	}
	
	public void subtract(int i){
		add(-i);
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
	
	/**
	 * Finds segments of TRUE values. 
	 * @param data
	 * @return
	 */
	public static Vector<Gap> bools2Segs(boolean[] data){
		
		Vector<Gap> segs = new Vector<Gap>();
		
		//Build the gap list
		int segStart = -1;
		int ptr = 0;
		while (ptr < data.length) {

			if (data[ptr]) {
				segStart = ptr;
				// Find the end of the gap
				do ++ptr; while (ptr < data.length && data[ptr]);
				//Make a new gap
				segs.add(new Gap(segStart, ptr-1));
				
			} else {
				++ptr;
			}
		}

		return segs;
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
		//JK gonna load them up in matlab
	}
	
	public Float[] getLastEnergies(){
		Float[] last = energies.lastElement();
		Float[] f = new Float[last.length];
		boolean[] empty = new boolean[f.length];
		int numEmpty = 0;
		for (int i=0; i<f.length; i++){
			if (last[i]!=null){
				f[i]=last[i];
				empty[i]=false;
			} else {
				empty[i] = true;
				numEmpty++;
			}
		}
		
		int lastInd = energies.size()-2;
		last = energies.get(lastInd);
		while (numEmpty>0){
			if (last.length==f.length){
				for (int j=0; j<f.length; j++){
					if (empty[j] && last[j]!=null){
						f[j]=last[j];
						empty[j]=false;
						numEmpty--;
					}
				}
				last = energies.get(--lastInd);
			}
		}
		
		
		return f;
	}
}
