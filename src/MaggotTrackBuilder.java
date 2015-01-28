import ij.ImageStack;


public class MaggotTrackBuilder extends TrackBuilder {

	public MaggotTrackBuilder(ImageStack IS, ExtractionParameters ep) {
		super(IS, ep);
	}
	
	public void run(){
		
		ep.trackPointType=2;//Make sure the track is loaded as MaggotTrackPoints
		buildTracks();
		orientMaggots();
	}

	private void orientMaggots(){
		for (int i=0; i<finishedTracks.size(); i++){
			orientMaggotTrack(finishedTracks.get(i));  
		}
	} 

	private void orientMaggotTrack(Track track){
//		if (ep.trackPointType!=2){
//			//Load points as MaggotTrack points
//			
//		}
		
		if (track.points!=null && track.points.size()>0 && track.points.get(0).pointType>=2){
			
			MaggotTrackPoint pt;
			MaggotTrackPoint prevPt = (MaggotTrackPoint)track.points.get(0);
//			int lastEndFrameAnalyzed=-1;
//			int lastValidFrame = (prevPt.midline!= null && prevPt.midline.getNCoordinates()!=0) ? 0 : -1;
			
			int AMDSegStart = (prevPt.midline!= null && prevPt.midline.getNCoordinates()!=0) ? 0 : -1;
			int AMDSegEnd = -1;
			
			for (int i=1; i<track.points.size(); i++){
			
				pt = (MaggotTrackPoint)track.points.get(i);
				
				if (pt.midline!= null && pt.midline.getNCoordinates()!=0) { 
					//If a midline exists, align it with the last valid point
					
					pt.chooseOrientation(prevPt);
//					lastValidFrame = i;
					prevPt = pt;
					
					if (AMDSegStart==AMDSegEnd){
						AMDSegStart=i;
					}
					AMDSegEnd=i;
					
					
				} else  { 
					//When the midline doesn't exist, analyze the previous segment of midlines
					//But only if that segment hasn't been analyzed yet, and the segment has at least 2 points

					//Analyze the direction of motion for the segment leading up to this frame, starting with lastEndFrameAnalyzed (or 0)
//					int startFrame = (lastEndFrameAnalyzed>=0) ? lastEndFrameAnalyzed : 0;
					if ( (AMDSegEnd-AMDSegStart)>1 ){
						
						analyzeMaggotDirection(track, AMDSegStart, AMDSegEnd);
						
						AMDSegStart = AMDSegEnd;
					}
				}
				
				//pt.chooseOrientation() can handle a null spine argument, but we might as well align it with the previous VALID point 
				//prevPt = pt;
			}
			
			if (lastEndFrameAnalyzed==-1){
				//Analyze the direction of motion for the whole track
				analyzeMaggotDirection(track, 0, track.points.size()-1);
			}
		}
		
	}
	
	private void analyzeMaggotDirection(Track track, int startFrame, int endFrame){
		
		
		//TODO
		
		
		
	}
	
	
	
	
	
	
}
