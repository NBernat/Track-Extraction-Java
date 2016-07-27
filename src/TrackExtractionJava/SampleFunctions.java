package TrackExtractionJava;

import ij.ImageJ;

import java.util.Vector;

public class SampleFunctions {

	
	/**
	 * TODO
	 * @param exptFileName
	 * @param dstFolderName
	 */
	public static void runExperimentProcessorFromCode(String exptFileName, String dstFolderName){
		
		Experiment_Processor ep = new Experiment_Processor();
		ep.runningFromMain = true;

		ProcessingParameters pp = new ProcessingParameters();
//		pp.doFitting = true;
//		pp.fitType=1;
//		pp.showFitEx = true;
		
		ExtractionParameters exP = new ExtractionParameters();
//		exP.subset = true;
//		exP.startFrame = 2000;
//		exP.endFrame = 4000;
		
		FittingParameters fp = new FittingParameters();
//		fp.storeEnergies = false;
		
		ep.prParams = pp;
		ep.extrParams = exP;
		ep.fitParams = fp;//TODO make all the fitTrackNewSchemeParams able to be passed here
		

		String[] args = new String[2];
		args[0] = exptFileName;
		args[1] = dstFolderName;
		ep.run(args);
	}
	
	/**
	 * TODO
	 * @param exptFileName
	 * @param trackID
	 */
	public static void fitTrackFromFile(String exptFileName, int trackID){
		ImageJ ij = new ImageJ();
		
		
		Experiment ex = new Experiment(exptFileName);
		Track t = ex.getTrack(trackID);
		
		BackboneFitter bbf = new BackboneFitter(t);
		bbf.fitTrackNewScheme();
		
		if (bbf.getTrack()!=null){
			Vector<Track> newTracks = new Vector<Track>();
			newTracks.add(bbf.getTrack());
			Experiment newExperiment = new Experiment(ex, newTracks);
			newExperiment.showEx();
		}
	}
	
	/**
	 * TODO
	 * @param exptFileName Should be a PREJAV file
	 * @param trackID
	 */
	public static void showFitting(String exptFileName, int trackID){
		
		ImageJ ij = new ImageJ();
		
		Experiment ex = new Experiment(exptFileName);
		Track t = ex.getTrack(trackID);
		
		t.showFitting();
	}
	
	
}
