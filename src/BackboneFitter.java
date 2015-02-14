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
	 * Parameters
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
	 * The final forces acting on each backbone point
	 */
	
	/**
	 * The final energy of each backbone point 
	 */
	
	
	
	public BackboneFitter(){
		 
		params = new FittingParameters();
		
		addForces();
		
		
	}
	
	private void addForces(){
		Forces = new Vector<Force>();
		Forces.add(new ImageForce());
		Forces.add(new SpineLengthForce());
		Forces.add(new SpineSmoothForce());
		Forces.add(new TimeLengthForce());
		Forces.add(new TimeSmoothForce());
		Forces.add(new HTAttractionForce());
	}
	
	public void fitTrack(Track tr){
		track = tr;
		BTPs = new Vector<BackboneTrackPoint>(); 
		if(track.points.get(0).pointType>=2){//TODO this whole type checking is a little clumsy, FIX IT
			if(track.points.get(0).pointType==2){
				for(int i=0; i<track.points.size(); i++){
					//track.points.setElementAt(BackboneTrackPoint.convertMTPtoBTP((MaggotTrackPoint)track.points.get(i), params.numBBPts), i);
					BackboneTrackPoint btp = BackboneTrackPoint.convertMTPtoBTP((MaggotTrackPoint)track.points.get(i), params.numBBPts);
					BTPs.add(btp);
				}
			}
			run();
		}
		else{
			//convert the points to mtp?
			
		}
	}
	
	protected void run(){
		
		
		//TODO Set update scheme variables
		boolean done = false;
		boolean finalIterations = false;
		
		int[] defaultInds = new int[track.points.size()];
		for(int i=0; i<defaultInds.length; i++) defaultInds[i]=i;
		int[] inds = defaultInds;

		//Run iterative updates of backbones in track
		while(!done || finalIterations){
			
			//Work on the data
			double shift = relaxBackbones(inds);
			
			//Maintain the updating scheme
			
		}
		
		calcEnergies();//Store energies
		finalizeBackbones();
		
		
	}
	
	
	//returns the total point shift from this relaxation step
	private double relaxBackbones(int[] inds){
		
		double shiftSum=0;
		
		//Loop through each backbone in the list
		for(int i=0; i<inds.length; i++){
			
			int ind = inds[i];
//			BackboneTrackPoint btp = BTPs.get(ind);
			
//			setSurroundingPointLists();
//			btp.bbRelaxationStep(params, beforePts, afterPts);
			bbRelaxationStep(ind);
			
//			shiftSum+=btp.calcPointShift();
			
		}
		
		return shiftSum;//TODO this might need to be a vector
	}
	
	private void bbRelaxationStep(int btpInd){
		
		Vector<FloatPolygon> targetSpines = getTargetSpines(btpInd);
		calcNewBackbone(targetSpines);
		
	}
	
	
	private Vector<FloatPolygon> getTargetSpines(int btpInd){
		Vector<FloatPolygon> targetSpines = new Vector<FloatPolygon>();
		
		//Iterate over Forces
		ListIterator<Force> fIt = Forces.listIterator();
		while(fIt.hasNext()){
			Force force = fIt.next();
			FloatPolygon targSpine = force.getTargetPoints(btpInd, BTPs);
			targetSpines.add(targSpine);
		}
		
		return targetSpines;
		
	}
	
	private void calcNewBackbone(Vector<FloatPolygon> targetSpines){
		
	}
	
//	private void setSurroundingPointLists(){
//		//Retrieve the lists of points before and after this one;
//		beforePts.removeAllElements();
//		//TODO ADD THEM ACCORDING TO THE PARAMETERS
//		afterPts.removeAllElements();
//		//TODO ADD THEM ACCORDING TO THE PARAMETERS
//	}
	
	
	private void calcEnergies(){
		
	}
	
	private void finalizeBackbones(){
		ListIterator<BackboneTrackPoint> btpIt = BTPs.listIterator();
		while(btpIt.hasNext()){
			btpIt.next().finalizeBackbone();
		}
	}
	
	
}
