import ij.IJ;
import ij.process.FloatPolygon;
import ij.text.TextWindow;

import java.util.Arrays;
import java.util.ListIterator;
import java.util.Vector;


/**
 * Fits backbones to a track of MaggotTrackPoints
 * @author Natalie Bernat
 *
 */
public class BackboneFitter {
	
	
	/**
	 * Fitting parameters
	 */
	FittingParameters params;
	
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
	 * The amount that each backbone in the track shifted during the last iteration that acted upon any given point
	 */
	private double[] shifts;
	
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
	public BackboneFitter(){
		 
		params = new FittingParameters();
		
		int pass=0;
		addForces(pass);
		
		comm = new Communicator();
		comm.setVerbosity(VerbLevel.verb_debug);
		
	}
	
	/**
	 * Adds instances of each type of Force to the backbone fitter
	 */
	private void addForces(int pass){
		Forces = new Vector<Force>();
		Forces.add(new ImageForce(params.imageWeights, params.imageWeight));
		Forces.add(new SpineLengthForce(params.spineLengthWeights, params.spineLengthWeight));
		Forces.add(new SpineSmoothForce(params.spineSmoothWeights, params.spineSmoothWeight));
		Forces.add(new TimeLengthForce(params.timeLengthWeights, params.timeLengthWeight[pass]));
		Forces.add(new TimeSmoothForce(params.timeSmoothWeights, params.timeSmoothWeight[pass]));
//		Forces.add(new HTAttractionForce(params.HTAttractionWeights));
	}
	
	/**
	 * Fits backbones to the points in the given track. 
	 * <p>
	 * After fitting, the Vector of BackboneTrackPoints can be accessed via getBackbonePoints() 
	 * @param tr
	 */
	public void fitTrack(Track tr){
		
		track = tr;
		//Extract a list of the TrackPoints and convert them to BackboneTrackPoints to hold the new backbone info 
		BTPs = new Vector<BackboneTrackPoint>();
		try{
			if(track.points.get(0) instanceof MaggotTrackPoint){//track.points.get(0).pointType==2
				for(int i=0; i<track.points.size(); i++){
					comm.message("Getting mtp...", VerbLevel.verb_debug);
					MaggotTrackPoint mtp = (MaggotTrackPoint)track.points.get(i);
					if(mtp==null){
						comm.message("Point "+i+" was not able to be cast", VerbLevel.verb_error);
					} else{
						mtp.comm=comm;
					}
					comm.message("Converting point "+i+" into a BTP", VerbLevel.verb_debug);
					BackboneTrackPoint btp = BackboneTrackPoint.convertMTPtoBTP(mtp, params.numBBPts);
					
					if(btp==null){
						comm.message("convertMTPtoBTP returned null, mtp.midline was null", VerbLevel.verb_debug);
					} else{
						comm.message("convertMTPtoBTP successful", VerbLevel.verb_debug);
					}
					
					btp.bf = this;
					BTPs.add(btp);
				}
				
			} else {
				comm.message("Never made any points", VerbLevel.verb_error);
				//TODO reload the points as BTP
			}
		}catch(Exception e){
			comm.message(e.getMessage(), VerbLevel.verb_debug);
		}
		if (!comm.outString.equals("")){
			new TextWindow("TrackFitter", comm.outString, 500, 500);
		}
		
		//Run the fitting algorithm
		run();
		//Tell the BackboneTrackPoints to store the final backbones
//		finalizeBackbones();
		
	}
	
	/**
	 * Runs the fitting algorithm
	 * <p>
	 * An updating scheme is maintained, which alternates between fitting every point 
	 * and fitting just the points which are still changing significantly
	 */
	protected void run(){
		
		//TODO: Write updating scheme. Maybe write it as a separate BBFUpdater
		
		//KeepRunning-type trackers
		boolean done = false;
		boolean finalIterations = false;
		
		//Indices used to indicate which points should be relaxed
		int[] defaultInds = new int[track.points.size()];
		for(int i=0; i<defaultInds.length; i++) defaultInds[i]=i;
		int[] inds = defaultInds;

		//Run iterative updates of backbones in track
//		while(!done || finalIterations){
			
			//Work on the data
			relaxBackbones(inds);
			
			//calcEnergies();
			
			//Maintain the updating scheme
			//TODO
//			for(int i=0; i<BTPs.size(); i++){
//				BTPs.get(i).setupForNextRelaxationStep();
//			}
			
			
//		}
		
	}
	
	
	/**
	 * Allows the backbones to relax to lower-energy conformations
	 * @param inds The indices of the BTPs which should be relaxed 
	 */
	private void relaxBackbones(int[] inds){
		
		for(int i=0; i<inds.length; i++){
			IJ.showStatus("Relaxing frame "+inds[i]);
			//Relax each backbone
			bbRelaxationStep(inds[i]);
			
		}
		
	}
	
	/**
	 * Relaxes a backbone to a lower-energy conformation
	 * @param btpInd Index of the BTP (in BTDs) to be processed
	 */
	private void bbRelaxationStep(int btpInd){
		
		IJ.showStatus("Getting target backbones in frame "+btpInd);
		//Get the lower-energy backbones for each individual force
		Vector<FloatPolygon> targetBackbones = getTargetBackbones(btpInd);
		//Combine the single-force backbones into one, and set it as the new backbone
		IJ.showStatus("Setting bbNew in frame "+btpInd);
		BTPs.get(btpInd).setBBNew(generateNewBackbone(targetBackbones));
		//Get the shift (and energy?) for use in updating scheme/display
//		shifts[btpInd] = BTPs.get(btpInd).calcPointShift();
		
	}
	
	/**
	 * Allows each force to act on the old backbone of the indicated BTP
	 * @param btpInd Index of the backbone to relax
	 * @return A vector of all relaxed backbones, in the same order as the list of forces 
	 */
	private Vector<FloatPolygon> getTargetBackbones(int btpInd){
		Vector<FloatPolygon> targetBackbones = new Vector<FloatPolygon>();
		
		//Store the backbones which are relaxed under individual forces
		ListIterator<Force> fIt = Forces.listIterator();
		while(fIt.hasNext()){
			targetBackbones.add(fIt.next().getTargetPoints(btpInd, BTPs));
		}
		
		return targetBackbones;
		
	}
	
	/**
	 * Generates the new backbone using the individual-force-shifted spines and the weighting parameters
	 * @param targetSpines
	 */
	private FloatPolygon generateNewBackbone(Vector<FloatPolygon> targetBackbones){
		
		float[] zeros = new float[params.numBBPts];
		Arrays.fill(zeros, 0);
		
		float[] newX = new float[params.numBBPts];
		Arrays.fill(newX, 0);
		float[] newY = new float[params.numBBPts];
		Arrays.fill(newY,  0);
		float normFactors[] = new float[params.numBBPts];
		Arrays.fill(normFactors, 0);
		
		IJ.showStatus("Gathering target backbones...");
		//Add each target backbone to the new backbone and gather the weighting factors for normalization
		for (int tb=0; tb<targetBackbones.size(); tb++){
			
			float[] targetX = targetBackbones.get(tb).xpoints;
			float[] targetY = targetBackbones.get(tb).ypoints;
			float[] weights = Forces.get(tb).getWeights();
			
			for (int k=0; k<params.numBBPts; k++){
				if (weights[k]!=0){
					newX[k] += targetX[k]*weights[k];
					newY[k] += targetY[k]*weights[k];
					normFactors[k] += weights[k];
				}
			}
			
		}
		IJ.showStatus("Normalizing points");
		//Normalize each point
		for(int k=0; k<params.numBBPts; k++){
			newX[k] = newX[k]/normFactors[k];
			newY[k] = newY[k]/normFactors[k];
		}
		
		return new FloatPolygon(newX, newY);
		
	}
	
	
	
	
	
	private void finalizeBackbones(){
		ListIterator<BackboneTrackPoint> btpIt = BTPs.listIterator();
		while(btpIt.hasNext()){
			btpIt.next().finalizeBackbone();
		}
	}
	
	
	
	public Vector<BackboneTrackPoint> getBackboneTrackPoints(){
		return BTPs;
	}
	
	public String getForceName(int ind){
		return Forces.get(ind).getName();
	}
	
	
}
