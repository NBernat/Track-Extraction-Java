import java.io.File;
import java.io.FileInputStream;
//import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
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
	
	/**
	 * Constructs an experiment from a "*.ser" file
	 * @param fname
	 */

	
	
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
		ListIterator<Track> trIt = tracks.listIterator();
		while (trIt.hasNext()){
			trIt.next().preSerialize();
		}
		
		//Serialize the Experiment
		try {
			
			FileOutputStream fo = new FileOutputStream(f);
			ObjectOutputStream oo = new ObjectOutputStream(fo);
			
			oo.writeObject(this);
			
			oo.close();
			fo.close();
			
			
		} catch (Exception e){
			//TODO 
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
		
		//TODO Check the extension
		File f = new File(fname);
		
		//Deserialize the experiment
		Experiment ex;
			
		FileInputStream fi = new FileInputStream(f);
		ObjectInputStream oi = new ObjectInputStream(fi);
		
		ex = (Experiment) oi.readObject();
		
		oi.close();
		fi.close();
		
//			init(fname, ex.ep, ex.collisionTrackIDs, ex.tracks);
			
			
		
		
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
	
}
