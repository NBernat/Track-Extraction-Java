
public class ProcessingParameters {

	private ExtractionParameters ep;
	
	private FittingParameters fp;
	
	int minTrackLen = 80;//TODO set this when fp is set
	
	public void setFittingParameters(FittingParameters fitparams){
		fp = fitparams;
	}
	
}
