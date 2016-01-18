package TrackExtractionJava;

public class TicToc {
	private long startTime;
	
	public TicToc(){
		
	}
	
	public void tic(){
		startTime = System.currentTimeMillis();
	}
	
	public long toc(){
		return System.currentTimeMillis()-startTime;
	}
	
	public long tocSec(){
		return (toc())/1000;
	}
	
	public long[] tocMinSec(){
		
		long sec = tocSec();
		long[] minSec = new long[2];
		minSec[0] = (long)((int) sec/60);
		sec -= minSec[0]*60;
		minSec[1] = sec;
		
		return minSec;
	}

}
