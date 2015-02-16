import ij.process.FloatPolygon;
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
	 * A List of the BbTP's, worked on during  
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
	
	
	/**
	 * Constructs a backbone fitter
	 */
	public BackboneFitter(){
		 
		params = new FittingParameters();
		
		addForces();
		
	}
	
	/**
	 * Adds instances of each type of Force to the backbone fitter
	 */
	private void addForces(){
		Forces = new Vector<Force>();
		Forces.add(new ImageForce());
		Forces.add(new SpineLengthForce());
		Forces.add(new SpineSmoothForce());
		Forces.add(new TimeLengthForce());
		Forces.add(new TimeSmoothForce());
		Forces.add(new HTAttractionForce());
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
		if(track.points.get(0).pointType==2){
			for(int i=0; i<track.points.size(); i++){
				BackboneTrackPoint btp = BackboneTrackPoint.convertMTPtoBTP((MaggotTrackPoint)track.points.get(i), params.numBBPts);
				BTPs.add(btp);
			}
		} else {
			//TODO reload the points as BTP
		}
		
		//Run the fitting algorithm
		run();
		//Tell the BackboneTrackPoints to store the final backbones
		finalizeBackbones();
		
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
		while(!done || finalIterations){
			
			//Work on the data
			relaxBackbones(inds);
			
			//calcEnergies();
			
			//Maintain the updating scheme
			//TODO
			
		}
		
	}
	
	
	/**
	 * Allows the backbones to relax to lower-energy conformations
	 * @param inds The indices of the BTPs which should be relaxed 
	 */
	private void relaxBackbones(int[] inds){
		
		for(int i=0; i<inds.length; i++){
			
			//Relax each backbone
			bbRelaxationStep(inds[i]);
			
		}
		
	}
	
	/**
	 * Relaxes a backbone to a lower-energy conformation
	 * @param btpInd Index of the BTP (in BTDs) to be processed
	 */
	private void bbRelaxationStep(int btpInd){
		
		//Get the lower-energy backbones for each individual force
		Vector<FloatPolygon> targetSpines = getTargetBackbones(btpInd);
		//Combine the single-force backbones into one
		generateNewBackbone(targetSpines);
		//Get the shift (and energy?) for use in updating scheme/display
		shifts[btpInd] = BTPs.get(btpInd).calcPointShift();
		
	}
	
	/**
	 * Allows each force to act on the old backbone of the indicated BTP
	 * @param btpInd Index of the backbone to relax
	 * @return A vector of all relaxed backbones, in the same order as the list of forces 
	 */
	private Vector<FloatPolygon> getTargetBackbones(int btpInd){
		Vector<FloatPolygon> targetSpines = new Vector<FloatPolygon>();
		
		//Store the backbones which are relaxed under individual forces
		ListIterator<Force> fIt = Forces.listIterator();
		while(fIt.hasNext()){
			targetSpines.add(fIt.next().getTargetPoints(btpInd, BTPs));
		}
		
		return targetSpines;
		
	}
	
	/**
	 * Generates the new backbone using the individual-force-shifted spines and the weighting parameters
	 * @param targetSpines
	 */
	private void generateNewBackbone(Vector<FloatPolygon> targetSpines){
		
	}
	

	
	private void finalizeBackbones(){
		ListIterator<BackboneTrackPoint> btpIt = BTPs.listIterator();
		while(btpIt.hasNext()){
			btpIt.next().finalizeBackbone();
		}
	}
	
	
	
	public Vector<BackboneTrackPoint> getBackbonePoints(){
		return BTPs;
	}
	
	public String getForceName(int ind){
		return Forces.get(ind).getName();
	}
	
	
}
