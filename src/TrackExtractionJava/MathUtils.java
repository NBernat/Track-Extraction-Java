package TrackExtractionJava;

public class MathUtils {

	
	
	
	
	public static double mean(double[] vals){
		double sum=0;
		for(int i=0; i<vals.length; i++){
			sum += vals[i];
		}
		return sum/vals.length;
	}
	
	public static double stdDev(double[] vals){
		return stdDev(vals, mean(vals));
	}
	
	public static double stdDev(double[] vals, double mean){
		
		double varSum = 0;
		for(int i=0; i<vals.length; i++){
			varSum += (vals[i]-mean)*(vals[i]-mean);
		}
		
		return Math.sqrt(varSum/vals.length);
	}
	
	
}
