
public class FittingParameters {
	
	int numBBPts = 9;
	
	int[] grains = {8, 4, 1};
	int smallGapMaxLen = 10;//The maximum gap length for which the previous midline will be carried forward (otherwise interpolate)
	int minValidSegmentLen = 10;//The minimum length of segment which is situated between two midline gaps and which is considered valid 
	
	float imageWeight = 1.0f;
	float spineLengthWeight = .4f;
	float spineSmoothWeight = .8f;
	float[] timeLengthWeight = {3.0f, .1f};
	float[] timeSmoothWeight = {1.0f, .1f};
	
	//Head=0, Tail=end
	float[] imageWeights = {1,1,1, 1,1,1, 1,1,1};
	float[] spineLengthWeights = {0,1,1, 1,1,1, 1,1,1};
	float[] spineSmoothWeights = {0,1,1, 1,1,1, 1,1,1};
	float[] timeLengthWeights = {1,1,1, 1,1,1, 1,1,1};
	float[] timeSmoothWeights = {1,1,1, 1,1,1, 1,1,1};
	float[] HTAttractionWeights = {1,0,0, 0,0,0, 0,0,1};
	
	
	
	public FittingParameters(){
		
	}
	
	
}
