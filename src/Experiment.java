import ij.IJ;

import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
//import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.PrintWriter;
import java.io.Serializable;
import java.io.StringWriter;
import java.util.ListIterator;
import java.util.Vector;


public class Experiment implements Serializable{

	/**
	 * Serialization ID
	 */
	private static final long serialVersionUID = 1L;
	
	/**
	 * Name of the original file? or the .ser file? 
	 */
	String fname;
	/**
	 * The extraction parameters used to extract this experiment
	 */
	ExtractionParameters ep;
	/**
	 * List of IDs that contain CollisionTracks
	 */
	Vector<Integer> collisionTrackIDs;
	/**
	 * List of tracks contained within the experiment
	 */
	Vector<Track> tracks;
	/**
	 * 
	 */
	Vector<Force> Forces;
	
	/**
	 * Constructs an Experiment object
	 * @param fname
	 * @param ep
	 * @param collisionTrackIDs
	 * @param tracks
	 */
	public Experiment(String fname, ExtractionParameters ep, Vector<Integer> collisionTrackIDs, Vector<Track> tracks) {
		init(fname, ep, collisionTrackIDs, tracks);
	}
	
	public Experiment(TrackBuilder tb){
		init("", tb.ep, tb.finishedColIDs, tb.finishedTracks);
	}
	
	
	public void init(String fname, ExtractionParameters ep, Vector<Integer> collisionTrackIDs, Vector<Track> tracks) {
		this.fname = fname;
		this.ep = ep;
		this.collisionTrackIDs = collisionTrackIDs;
		this.tracks = tracks;
	}
	
	@SuppressWarnings("unchecked")
	public Experiment(Experiment exOld){
		init(exOld.fname, exOld.ep, exOld.collisionTrackIDs, (Vector<Track>)exOld.tracks.clone());
		Forces = exOld.Forces;
	}

	
	public int toDisk(DataOutputStream dos, PrintWriter pw){
		
		if (tracks.size()==0){
			if (pw!=null) pw.println("No tracks in experiment; save aborted"); 
			return 4;
		}
		
		if (pw!=null) pw.println("Saving experiment to disk...");
		
		//Write the Experiment Type
		try {
			int code = getTypeCode();
			if (code>=0){
				dos.write(code);
			} else {
				if (pw!=null) pw.println("Invalid experiment code; save aborted");
				return 3;
			}
		} catch (Exception e) {
			if (pw!=null) pw.println("Error writing experiment type code; save aborted");
			return 3;
		}
		
		//Write the # of tracks
		try {
			dos.write(tracks.size());
		} catch (Exception e) {
			if (pw!=null) pw.println("Error writing # of tracks; save aborted");
			return 2;
		}
		
		//Write each track
		try {
			if (pw!=null) pw.println("Writing Tracks...");
			for (Track tr : tracks){
				if(tr.toDisk(dos,pw)!=0) {
					if (pw!=null) pw.println("Error writing track "+tr.trackID+"; save aborted");
					return 1; 
				}
			}
		} catch (Exception e) {
			if (pw!=null) pw.println("Error writing tracks; save aborted");
			return 1;
		}
		
		if (pw!=null) pw.println("Experiment Saved!");
		return 0;
	}
	
	private int getTypeCode(){
		int trackType = -1;
		
		for (int i=0; (trackType<0 && i<tracks.size()); i++){
			if(tracks.get(i).points.size()>0){
				trackType = tracks.get(i).points.firstElement().pointType;
			}
		}
		
		if (trackType>=0){
			trackType = (trackType<<8) + 0x01;
		}
		
		return trackType;
	}
	
	/**
	 * Saves this Experiment in the specified dir+filename
	 * @param dir The directory in which to save the file(; if empty, saves in current directory?)
	 * @param filename The file name including the extension
	 */
	public void save(String dir, String filename){
		
		//TODO CHECK THE DIR/FILENAME
		fname = dir+File.separator+filename;
		File f = new File(fname);
		
		//Pre-serialize the tracks 
		IJ.showStatus("PreSerializing...");
		ListIterator<Track> trIt = tracks.listIterator();
		while (trIt.hasNext()){
			trIt.next().preSerialize();
		}
		
		//Serialize the Experiment
		IJ.showStatus("Writing objects to file");
		try {
			
			FileOutputStream fo = new FileOutputStream(f);
			ObjectOutputStream oo = new ObjectOutputStream(fo);
			
			oo.writeObject(this);
			
			oo.close();
			fo.close();
			IJ.showStatus("Done writing objects to file");
			
		} catch (Exception e){
			IJ.showStatus("Error writing objects to file");
			StringWriter sw = new StringWriter();
			PrintWriter pw = new PrintWriter(sw);
			e.printStackTrace(pw);
			IJ.showMessage("Error saving experiment:\n"+sw.toString());
			return;
		}
		
		
		
	}
	
	
	/**
	 * Opens a serialized Experiment 
	 * @param fname
	 * @return
	 * @throws Exception 
	 */
	public static Experiment open(String fname) throws Exception{
		
		Experiment ex;
		
		//TODO Check the extension
		File f = new File(fname);
			
		//Deserialize the experiment
		FileInputStream fi = new FileInputStream(f);
		ObjectInputStream oi = new ObjectInputStream(fi);
		ex = (Experiment) oi.readObject();
		oi.close();
		fi.close();
		
		//PostDeserialize the tracks
		ListIterator<Track> trIt = ex.tracks.listIterator();
		while (trIt.hasNext()){
			trIt.next().postDeserialize();
		}
		
		return ex;
	}
	
	public int getTrack(int trackNum){
		
		ListIterator<Track> trIt = tracks.listIterator();
		while(trIt.hasNext()){
			if(trIt.next().trackID==trackNum) return trIt.previousIndex();
		}
		return -1;
	}
	
	public void setForces(Vector<Force> Forces){
		this.Forces = Forces;
	}
	
	
	public void replaceTrack(Track newTrack, int ind){
		tracks.setElementAt(newTrack, ind);
	}
	
}
