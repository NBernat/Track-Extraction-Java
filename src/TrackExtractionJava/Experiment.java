package TrackExtractionJava;

import ij.IJ;
import ij.ImagePlus;
import ij.process.ByteProcessor;
import ij.process.ColorProcessor;
import ij.process.ImageProcessor;

import java.io.BufferedInputStream;
import java.io.BufferedWriter;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.PrintWriter;
import java.io.Serializable;
import java.io.StringWriter;
import java.io.Writer;
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
	private String fname;
	/**
	 * The extraction parameters used to extract this experiment
	 */
	private ExtractionParameters ep;
	/**
	 * List of IDs that contain CollisionTracks
	 */
//	Vector<Integer> collisionTrackIDs;
	/**
	 * List of tracks contained within the experiment
	 */
	private Vector<Track> tracks;
	/**
	 * 
	 */
	private Vector<Force> Forces;
	
	public Experiment(){
		
	}
	
	public Experiment(String filename){
		try {
			fname = filename;
			ep = new ExtractionParameters();
//			Forces = ;
			
			//TODO use experiment_Processor functions
			DataInputStream dis = new DataInputStream(new BufferedInputStream(new FileInputStream(new File(filename))));
			
			loadFromDisk(dis, null);//new PrintWriter(System.out));
//			System.out.println("Experiment loaded; "+tracks.size()+" tracks"); 
		} catch (Exception e){
			StringWriter sw = new StringWriter();
			PrintWriter prw = new PrintWriter(sw);
			e.printStackTrace(prw);
			System.out.println(sw.toString());
		}
	}
	
	/**
	 * Constructs an Experiment object
	 * @param fname
	 * @param ep
	 * @param collisionTrackIDs
	 * @param tracks
	 */
//	public Experiment(String fname, ExtractionParameters ep, Vector<Integer> collisionTrackIDs, Vector<Track> tracks) {
//		init(fname, ep, tracks);
////		init(fname, ep, collisionTrackIDs, tracks);
//	}
	
	public Experiment(TrackBuilder tb){
		init("", tb.ep,tb.finishedTracks);
//		init("", tb.ep, tb.finishedColIDs, tb.finishedTracks);
	}
	
	
	
	
	private void init(String fname, ExtractionParameters ep, Vector<Track> tracks) {
		this.fname = fname;
		this.ep = ep;
//		this.collisionTrackIDs = collisionTrackIDs;
		this.tracks = tracks;
	}
	
	@SuppressWarnings("unchecked")
	public Experiment(Experiment exOld){
		init(exOld.fname, exOld.ep, (Vector<Track>)exOld.tracks.clone());
//		init(exOld.fname, exOld.ep, exOld.collisionTrackIDs, (Vector<Track>)exOld.tracks.clone());
		Forces = exOld.Forces;
	}

	public Experiment(Experiment exOld, Vector<Track> newTracks){
		init(exOld.fname, exOld.ep, newTracks);
//		init(exOld.fname, exOld.ep, exOld.collisionTrackIDs, (Vector<Track>)exOld.tracks.clone());
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
				if (pw!=null) pw.println("Writing type code ("+code+")");
				dos.writeInt(code);
			} else {
				if (pw!=null) pw.println("Invalid experiment code; save aborted");
				return 3;
			}
		} catch (Exception e) {
			if (pw!=null) pw.println("...Error writing experiment type code; save aborted");
			return 3;
		}
		
		//Write the # of tracks
		try {
			if (pw!=null) pw.println("Writing # of tracks ("+tracks.size()+")");
			dos.writeInt(tracks.size());
		} catch (Exception e) {
			if (pw!=null) pw.println("...Error writing # of tracks; save aborted");
			return 2;
		}
		
		//Write each track
		try {
			if (pw!=null) pw.println("Writing Tracks");
			
			for (int j=0; j<tracks.size(); j++){
				Track tr = tracks.get(j);
				if (pw!=null) pw.println("Writing track number "+j+"("+tr.getTrackID()+")");
				if(tr.toDisk(dos,pw)!=0) {
					if (pw!=null) pw.println("...Error writing track "+tr.getTrackID()+"; save aborted");
					return 1; 
				}
			}
			
		} catch (Exception e) {
			if (pw!=null) pw.println("\n...Error writing tracks; save aborted");
			return 1;
		}
		
		try{
			dos.writeInt(0);
		} catch (Exception e){
			if (pw!=null) pw.println("\n...Error writing end of file; save aborted");
			return 1;
		}
		
		if (pw!=null) pw.println("\n...Experiment Saved!");
		return 0;
	}
	
	
	public static Experiment fromDisk(DataInputStream dis, String filename, ExtractionParameters exParam, FittingParameters fp, PrintWriter pw){
		
		
		try {
			Experiment ex = new Experiment();
			if (pw!=null) pw.println("Setting parameters");
			ex.fname = filename;
			ex.ep = exParam;
			ex.Forces = fp.getForces(0);
			if (pw!=null) pw.println("Loading from Disk... ");
			ex.loadFromDisk(dis, pw);
			if (pw!=null) pw.println("...load from disk complete");
			return ex;
		} catch (Exception e){
			StringWriter sw = new StringWriter();
			PrintWriter prw = new PrintWriter(sw);
			e.printStackTrace(prw);
			if (pw!=null) pw.println("...error loading experiment from disk:\n"+sw.toString());
			return null;
		}
		
	}
	
	/**
	 * Opens the experiment, assigns default parameters
	 */
	public static Experiment fromPath(String path){
		
		return fromPath(path, new ExtractionParameters(), new FittingParameters(), new PrintWriter(System.out));
		
	}
	
	public static Experiment fromPath(String path, ExtractionParameters exParam, FittingParameters fp, PrintWriter pw){
		Experiment newEx;
		try{
			 
			DataInputStream dis = new DataInputStream(new BufferedInputStream(new FileInputStream(new File(path))));
			newEx = Experiment.fromDisk(dis, path, exParam, fp, pw);
			dis.close();
			
			
		} catch (Exception e){
			if(pw!=null) pw.println("Error loading experiment");
			return null;
		}
		
		return newEx;
	}
	
	
	public static void toCSV(Experiment ex, String path){
		toCSV(ex, path, null);
	}
	
	public static void toCSV(Experiment ex, String path, CSVPrefs prefs){
		
		//Open the FileWriter
		File f = new File(path);
		if(!f.exists()){
			try {
				f.createNewFile();
			} catch(Exception e) {
				System.out.println("Error creating File for CSV data");
				return;
			}
		}
		
		try{
			BufferedWriter bfw = new BufferedWriter(new FileWriter(f));
			//Check the prefs
			if (prefs==null){
				prefs = CSVPrefs.defaultPrefs();
			}
			//
			ex.toCSV(bfw, prefs);
			
			bfw.close();
		} catch (Exception e){
			System.out.println("Error writing CSV");
		}
	}
	
	public void toCSV(Writer fw, CSVPrefs prefs){
		
		String lb = System.getProperty("line.separator");//"\n";//System.lineSeparator();
		
		try{
			fw.append("Track:");
			int nFields = prefs.numFields(true);
			for (int i=0; i<tracks.size(); i++){
				for (int j=0; j<nFields; j++){
					fw.append(","+tracks.get(i).getTrackID());
				}
			}
			fw.append(lb);
			
			fw.append("frame");
			for (int i=0; i<tracks.size(); i++){
				fw.append(prefs.CSVheaders());
			}
			fw.append(lb);
			
		} catch (Exception e){
			System.out.println("Error writing headers to CSV");
		}
		
		
		int nFrames = getNumFrames();
		int[] startFrames = new int[nFrames];
		for(int i=0; i<tracks.size(); i++){
			startFrames[i]=tracks.get(i).getStart().frameNum;
		}
		int[] currFrame = startFrames;
		
		try{
			//Iterate over each frame and write each track's data for that frame
			for (int f=getStartFrame(); f<nFrames; f++){//TODO change to f:frameinds
				fw.append(f+"");
				for (int t=0; t<tracks.size(); t++){//TODO change to t:trackinds
					String fInfo;
					if (currFrame[t]==f){
						fInfo = tracks.get(t).points.get(currFrame[t]-startFrames[t]).getCSVinfo(prefs, true);
						currFrame[t]++;
					} else {
						fInfo = TrackPoint.getEmptyCSVinfo(prefs);
					}
					fw.append(fInfo);
				}
				fw.append(lb);
			}
		} catch(Exception e){
			StringWriter sw = new StringWriter();
			PrintWriter pw = new PrintWriter(sw);
			e.printStackTrace(pw);
			System.out.println("Error writing info to CSV\n"+sw.toString());
		}
		
	}
	
	
	public int getStartFrame(){

		int startFrame = Integer.MAX_VALUE;
		for(int i=0; i<tracks.size(); i++){
			int s = tracks.get(i).getStart().frameNum;
			if (s<startFrame){
				startFrame = s;
			}
				
		}
		
		return startFrame;
	}
	
	
	public int getNumFrames(){
		
		int numFrames = 0;
		for(int i=0; i<tracks.size(); i++){
			int endFrame = tracks.get(i).getEnd().frameNum;
			if (endFrame>numFrames){
				numFrames = endFrame;
			}
				
		}
		
		return numFrames;
	}
	
	
	public static int getNumTracks(String fname){
		try {
			DataInputStream dis = new DataInputStream(new BufferedInputStream(new FileInputStream(new File(fname))));
			dis.readInt();
			int nTracks = dis.readInt();
			dis.close();
			return nTracks;
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		return -1;
	}
	
	public static int getPointType(String fname){
		
		try {
			DataInputStream dis = new DataInputStream(new BufferedInputStream(new FileInputStream(new File(fname))));
			int tpType = dis.readInt();
			dis.close();
			return tpType;
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		return -1;
	}
	
	public static Track getTrack(int n, String fname){
		return getTrack(n, -1, fname);
	}
	
	public static Track getTrack(int n, int bytes2skip, String fname){
		
		if (n<0) return null;
		
		try {
			DataInputStream dis = new DataInputStream(new BufferedInputStream(new FileInputStream(new File(fname))));
			int tpType = dis.readInt();
			//System.out.println("Type: "+tpType);
			int nTracks = dis.readInt();
			//System.out.println("NTracks: "+nTracks);
			if(n>=nTracks){
				dis.close();
				return null;
			}
			//Skip past all previous tracks
			if (bytes2skip==-1){
				//System.out.println("Skipping to track "+n);
				skip2trackN(n, dis, tpType, null);
			} else {
				dis.skipBytes(bytes2skip);
			}
			
			Track t = Track.fromDisk(dis, tpType, null, null);
			dis.close();
			return t;
		} catch (Exception e) {
			
			e.printStackTrace();
		} 
		
		return null;
		
	}

	private static void skip2trackN(int n, DataInputStream dis, int pointType, PrintWriter pw) throws IOException{
		for(int i=0; i<n;i++){
			Track.fromDisk(dis, pointType, null, null);
//			int skip = dis.readInt();
//			System.out.println("Skipping "+skip+" bytes");
//			dis.skipBytes(skip);
		}
	}
	
	
	private void loadFromDisk(DataInputStream dis, PrintWriter pw){
//		System.out.println("Loading from disk...");
		int progress = -2;
		try{

//			System.out.println("Loading from disk...");
			//Read the Experiment Type
			int tpType = dis.readInt();
			if (pw!=null) pw.println("==> trackpoint type "+tpType);
			progress++;//=-1
			
			//Read the # of tracks
			int numTracks = dis.readInt();
			if (pw!=null) pw.println("==> "+numTracks+" tracks");
			tracks = new Vector<Track>();
			progress++;//=0
			
			//Read each track
			progress = 0;
			Track nextTrack;
			for (int i=0; i<numTracks; i++){

				if (pw!=null) pw.println("==> Track "+i+"/"+(numTracks-1));
				nextTrack = Track.fromDisk(dis, tpType, this, pw);
				if (nextTrack==null) {
					if (pw!=null) pw.println("(null)");
					return;
				}
				tracks.add(nextTrack);
				
				progress++;//= # of tracks loaded
				
				//TODO ask for garbage collection
			}

//			System.out.println("...done loading!");
			
		} catch (Exception e){
			if (pw!=null) pw.println("Error: progress code "+progress);
			System.out.println("...Error loading");
			return;
		}
		
			
	}
	
	
	/**
	 * Saves this Experiment in the specified dir+filename
	 * @param dir The directory in which to save the file(; if empty, saves in current directory?)
	 * @param filename The file name including the extension
	 */
	public void serialize(String dir, String filename){
		
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
	public static Experiment deserialize(String fname) throws Exception{
		
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
	
	public String getFileName(){
		return fname;
	}

	
	
	
	public ImagePlus getDiagnIm(int width, int height){
		
		ColorProcessor dIm = new ColorProcessor(width, height);
		
		for (int i=0; i<tracks.size(); i++){
			tracks.get(i).drawTrack(dIm);
		}
		
		
		return new ImagePlus("Diagnostic Image", dIm);
	}
	
	
	public int getTypeCode(){
		int trackType = -1;
		
		for (int i=0; i<tracks.size(); i++){
			if(tracks.get(i).getNumPoints()>0){
				
				if (tracks.get(i).getStart() instanceof BackboneTrackPoint && trackType<3){
					trackType = 3;
				} else if (tracks.get(i).getStart() instanceof MaggotTrackPoint && trackType<2){
					trackType = 2;
				} else if (tracks.get(i).getStart() instanceof ImTrackPoint && trackType<1){
					trackType = 1;
				} else if (tracks.get(i).getStart() instanceof TrackPoint && trackType<0){
					trackType = 0;
				}
			}
		}
		
		return trackType;
	}
	
	public ExtractionParameters getEP(){
		return ep;
	}
	
	public int getNumTracks(){
		if (tracks==null){
			return -1;
		}
		return tracks.size();
	}
	
	private int getTrackInd(int trackID){
		
		ListIterator<Track> trIt = tracks.listIterator();
		while(trIt.hasNext()){
			if(trIt.next().getTrackID()==trackID) return trIt.previousIndex();
		}
		return -1;
	}
	
	public Track getTrack(int trackID){
		return tracks.get(getTrackInd(trackID));
	}
	
	public Track getTrackFromInd(int i){
		return tracks.get(i);
	}
	
	public void removeTrack(Track t){
		tracks.remove(t);
	}
	
	public Vector<Force> getForces(){
		return Forces;
	}
	
	public void setForces(Vector<Force> Forces){
		this.Forces = Forces;
	}
	
	
	public void replaceTrack(Track newTrack, int ind){
		tracks.setElementAt(newTrack, ind);
	}
	
}
