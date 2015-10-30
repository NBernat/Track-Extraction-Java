package TrackExtractionJava;

import java.util.Vector;


public class FittingParameters {
	
	int GCInterval = 5;
	
	int numBBPts = 9;
	
	int[] grains = {32,16, 1}; //{32,16,1};//edit 17 {32, 16, 8, 1}, edit18 {32, 16, 4, 1}, edit 19 {32, 8, 1}, edit 20 {32, 16, 2, 1} edit 23, edit 24 {8,4, 1}
	int smallGapMaxLen = 10;//The maximum gap length for which the previous midline will be carried forward (otherwise interpolate)
	int minValidSegmentLen = 5;//The minimum segment length (in frames) which is situated between two midline gaps and which is considered valid
	double minFlickerDist = numBBPts;//The minimum distance between spines which indicates an erroneous midline flicker 
	int gapDilation = 5;
	boolean dilateToEdges = true;
	boolean checkArea= true;
	
	
	
	int divergenceConstant = 1;//
	
	float imageWeight = 1.0f;
	float spineLengthWeight = .5f;//.6f;// edit 21 to .4, edit 22 to .5
	float spineSmoothWeight = 1.0f;//.8f;//edit 1 up to 1.0//edit 15 back to .8 (also timelength), edit 16 back to 1.0(also timelength)
	float[] timeLengthWeight = {3.0f, .3f, .3f};//{3.0f, 0.1f};//edit 2 (swap back, 5)(upped to .5, 6)(back down to .2, 9)(14 up to .3) (15 back to .2, also spinesmooth)(16 back to .3, also spinesmooth)
	float[] timeSmoothWeight = {1.0f, 0.5f, 0.5f};//{.1f, .1f};//edit 7 (1.0,0.1), edit 8(1.0, 0.3), edit 10(upped to (1.0,1.0)), 11 (10., .5) ) 
	
	//Head=0, Tail=end
	float[] imageWeights = {1,1,1, 1,1,1, 1,1,1};
	float[] spineLengthWeights = {.7f,1,1, 1,1,1, 1,1,1};//{.1f,1,1, 1,1,1, 1,1,1};//{0,1,1, 1,1,1, 1,1,1};//edit 3,4
	float[] spineSmoothWeights = {.8f,1,1, 1,1,1, 1,1,1};//{.1f,1,1, 1,1,1, 1,1,1};//edit 12 (head to .5), edit 13 (head to .8)
	float[] timeLengthWeights = {1,1,1, 1,1,1, 1,1,1};
	float[] timeSmoothWeights = {1,1,1, 1,1,1, 1,1,1};
	float[] HTAttractionWeights = {1,0,0, 0,0,0, 0,0,1};
	
	
	
	public FittingParameters(){
		
	}
	
	public boolean isFirstPass(int grain){
		return grain==grains[0];
	}
	
	public float timeLengthWeight(int pass){
		if (pass>=timeLengthWeight.length) {
			return timeLengthWeight[timeLengthWeight.length-1];
		} else {
			return timeLengthWeight[pass]; 
		}
		
//		if (pass==0) return timeLengthWeight[0]; else return timeLengthWeight[1];
	}
	
	public float timeSmoothWeight(int pass){
		if (pass>=timeSmoothWeight.length) {
			return timeSmoothWeight[timeSmoothWeight.length-1];
		} else {
			return timeSmoothWeight[pass]; 
		}
//		if (pass==0) return timeSmoothWeight[0]; else return timeSmoothWeight[1];
	}
	
	
	public Vector<Force> getForces(int pass) {
		
		Vector<Force> Forces = new Vector<Force>();
		Forces.add(new ImageForce(imageWeights, imageWeight));
		Forces.add(new SpineLengthForce(spineLengthWeights,
				spineLengthWeight));
		Forces.add(new SpineSmoothForce(spineSmoothWeights,
				spineSmoothWeight));
		Forces.add(new TimeLengthForce(timeLengthWeights,
				timeLengthWeight(pass)));
		Forces.add(new TimeSmoothForce(timeSmoothWeights,
				timeSmoothWeight(pass)));
		// Forces.add(new HTAttractionForce(params.HTAttractionWeights));
		return Forces;
	}
	
}
