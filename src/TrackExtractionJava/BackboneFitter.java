package TrackExtractionJava;

import ij.process.FloatPolygon;
import ij.text.TextWindow;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
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
	
	
	
	/**
	 * The amount that each backbone in the track shifted during the last
	 * iteration that acted upon any given point
	 */
	private double[] shifts;

	private BBFUpdateScheme updater;
	
	private int pass;
	
	
	private boolean subsets = false;
	
	private boolean diverged = false;
	protected int divergedInd = -1;
	
	Vector<EnergyProfile> energyProfiles;
	
	transient Communicator comm;
	transient Communicator bbcomm;

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
		
		
	}

	private void initEnergyProfiles(){
		energyProfiles = new Vector<EnergyProfile>();
		for (int i=0; i<Forces.size(); i++){
			energyProfiles.add(new EnergyProfile(Forces.get(i).getName()));
		}
		energyProfiles.add(new EnergyProfile("Total"));
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
		BTPs = null;
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
	 * Fits backbones to the points in the given track.
	 * <p>
	 * After fitting, the Vector of BackboneTrackPoints can be accessed via
	 * getBackbonePoints()
	 * 
	 */
	public void fitTrack() {
		
//		clearPrev();
		
		boolean noError = true;

		// Extract the points, and move on (if successful)
		comm.message("Extracting maggot tracks", VerbLevel.verb_debug);
		
		noError = true;
		for(int i=0; (i<params.grains.length && noError); i++){
			noError = doPass(params.grains[i]);
			if (!noError) {
				comm.message("Error on track ("+workingTrack.getTrackID()+") pass "+i+"(grain "+params.grains[i]+") \n ---------------------------- \n \n", VerbLevel.verb_error);
				errTrack = workingTrack;
				workingTrack = null;
			}
			pass++;
			if (pass<params.grains.length) Forces = params.getForces(pass);
		}
		

		//run divergence fixer 
//		if (diverged && params.divFix){
//			System.out.println("Track diverged, attempting to fix");
//			fixDiverged();
//		}
		
		System.out.println("Done fitting track");
	}


	public void fitTrackNewScheme(){
		
		//Run one iteration of fitter on entire track to get energies 
		boolean noError = bplg.generateFullBTPList();
		BTPs = bplg.getBTPs();
		if (noError){
			updater = new BBFUpdateScheme(BTPs.size());
			shifts = new double[BTPs.size()];
			
			relaxBackbones(updater.inds2Update());
			calcShifts();
			setupForNextRelaxationStep();
		} else {
			System.out.println("Error generating btplist");
		}
		
		//Find high energy areas
		Vector<Gap> lowEnergyGaps = findLowEnergyGaps();
		Vector<Gap> highEnergyGaps = findHighFromLowGaps(lowEnergyGaps);
		
		fullTrack = workingTrack;
		//Run fitter on low energy energy gaps
		for (Gap leGap : lowEnergyGaps){
			fitTrackSubset(leGap.start, leGap.end, false);
		}
		
		for (Gap heGap : highEnergyGaps){
			fitTrackSubsetWithBuffer(heGap.start, heGap.end, false);
		}
		
		
		//Fit entire track?
	}
	
	
	private Vector<Gap> findLowEnergyGaps(){
		
		// TODO
		
		return null;
	}
	
	private Vector<Gap> findHighFromLowGaps(Vector<Gap> heGaps){
		
		// TODO
		
		return null;
	}
	
	
	public void fitTrackSubset(int startInd, int endInd){
		fitTrackSubset(startInd, endInd, true);
	}
	
	/**
	 * Fits a subset of the working track. the full track is stored in fullTrack
	 */
	private void fitTrackSubset(int startInd, int endInd, boolean saveFullTrack){
		subsets = true;
		if (saveFullTrack) fullTrack = workingTrack;
		workingTrack = new Track(fullTrack, startInd, endInd);
		bplg.workingTrack = workingTrack;
		fitTrack();
	}
	
	
	private void fitTrackSubsetWithBuffer(int startInd, int endInd, boolean saveFullTrack){
		subsets = true;
		if (saveFullTrack) fullTrack = workingTrack;
		workingTrack = new Track(fullTrack, startInd, endInd);
		bplg.workingTrack = workingTrack;
		//Set updater properties
		// TODO
		fitTrack();
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
		boolean noError = bplg.generateBTPList(pass);
		BTPs = bplg.getBTPs();
		
		if (noError){
			updater = new BBFUpdateScheme(BTPs.size());
			shifts = new double[BTPs.size()];
		} else{
			comm.message("Error generating backbones at pass "+pass, VerbLevel.verb_error);
		}
		
		return noError;
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
			if (BTPs.get(i).diverged(params.divergenceConstant)){
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
		ListIterator<BackboneTrackPoint> btpIt = BTPs.listIterator();
		while (btpIt.hasNext()) {
			btpIt.next().finalizeBackbone();
		}
	}
	
	
	
//	private Vector<Track> fixDiverged(){
//		
//		
//		//Find easy subsets
//	
//		Gap divGap = findDivergedGap();
//		
//		//Use the pass to 
//		int[] startInds = {0, divGap.end+1};
//		int[] endInds = {divGap.start-1, workingTrack.points.size()-1};
//		
//		
//		//MAKE TWO NEW BBFS? 
//		//Run fitter on easy subsets 
//		BackboneFitter leftSegment = new BackboneFitter(new Track(oldTrack, startInds[0], endInds[0]), params);
//		BackboneFitter rightSegment = new BackboneFitter(new Track(oldTrack, startInds[1], endInds[1]), params);
//		
//		
//		
//		
//		
//		fullTrack = workingTrack;
//		Vector<Track> fitTracks = new Vector<Track>();
//		boolean allFit = true;
//		for (int i=0; i<startInds.length; i++){
//			fitTrackSubset(startInds[i], endInds[i], false);
//			//deal with energy profile
//			//deal with end clippings
//			if (diverged) {
//				allFit = false;
//			} else {
//				fitTracks.add(workingTrack);
//			}
//		}
//		
//		
//		if (allFit){
//			//Try to fix diverged areas? If it works, replace fitTracks with fullTrack
//			//	fill in unfit pts with btp info
//			//	set updater defaultInds to these points
//			//	make updater mode that only works on defaultInds, followed by wholeTrack if they converge
//		} else {
//			//Mark unfit invalid and return fullTrack 
//		}
//		
//		
//		
//		
//		return fitTracks;
//	}
//	
//	private Gap findDivergedGap(){
//		//
//		
//		//shifts, pass, divergedInd
//		//use shifts to find list of inds
//		
//		//use inds to find frames
//		return null;
//		
//	}
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
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
		//JK gonna load them up in matlab
	}
	
	
}
