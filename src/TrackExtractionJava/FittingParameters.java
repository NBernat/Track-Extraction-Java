package TrackExtractionJava;

import java.util.Vector;


public class FittingParameters {
	
	int numBBPts = 9;
	
	int[] grains = {16,  1};//{1};//
	int smallGapMaxLen = 10;//The maximum gap length for which the previous midline will be carried forward (otherwise interpolate)
	int minValidSegmentLen = 5;//The minimum segment length (in frames) which is situated between two midline gaps and which is considered valid
	double minFlickerDist = numBBPts;//The minimum distance between spines which indicates an erroneous midline flicker 
	
	float imageWeight = 1.0f;
	float spineLengthWeight = .3f;
	float spineSmoothWeight = 1.0f;
	float[] timeLengthWeight = {3.0f, .1f};
	float[] timeSmoothWeight = {1.0f, .1f};
	
	//Head=0, Tail=end
	float[] imageWeights = {1,1,1, 1,1,1, 1,1,1};
	float[] spineLengthWeights = {.1f,1,1, 1,1,1, 1,1,1};
	float[] spineSmoothWeights = {.1f,1,1, 1,1,1, 1,1,1};
	float[] timeLengthWeights = {1,1,1, 1,1,1, 1,1,1};
	float[] timeSmoothWeights = {1,1,1, 1,1,1, 1,1,1};
	float[] HTAttractionWeights = {1,0,0, 0,0,0, 0,0,1};
	
	
	
	public FittingParameters(){
		
	}
	
	public boolean isFirstPass(int grain){
		return grain==grains[0];
	}
	
	public float timeLengthWeight(int pass){
		if (pass==0) return timeLengthWeight[0]; else return timeLengthWeight[1];
	}
	
	public float timeSmoothWeight(int pass){
		if (pass==0) return timeSmoothWeight[0]; else return timeSmoothWeight[1];
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
