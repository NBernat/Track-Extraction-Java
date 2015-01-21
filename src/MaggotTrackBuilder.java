import ij.ImageStack;


public class MaggotTrackBuilder extends TrackBuilder {

	public MaggotTrackBuilder(ImageStack IS, ExtractionParameters ep) {
		super(IS, ep);
	}
	
	public void run(){
		buildTracks();
		orientMaggots();
	}

	private void orientMaggots(){
		for (int i=0; i<finishedTracks.size(); i++){
			orientTrack(finishedTracks.get(i));  
		}
	}

	private void orientTrack(Track track){
		if (ep.trackPointType!=2){//only do this if the correct
			//Load points as mtp
		}
		if (track.points!=null && track.points.size()>0){
			MaggotTrackPoint pt;
			MaggotTrackPoint prevPt = (MaggotTrackPoint)track.points.get(0);
			for (int i=1; i<track.points.size(); i++){
				pt = (MaggotTrackPoint)track.points.get(i);
				pt.chooseOrientation(prevPt);
				
			}
		}
		
	}
}
