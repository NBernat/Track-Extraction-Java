import ij.ImagePlus;
import ij.ImageStack;
import ij.gui.Plot;
import ij.process.ImageProcessor;
import ij.text.TextWindow;

import java.awt.Color;
import java.io.Serializable;
import java.util.Collections;
import java.util.ListIterator;
import java.util.Vector;


public class Track implements Serializable{
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	/**
	 * Constituent TrackPoints
	 */
	Vector<TrackPoint> points;
	/**
	 * Unique identifier for the track 
	 */
	int trackID;
	/**
	 * Used to generate unique IDs for the TrackPoints
	 * <p> 
	 * Incremented each time a new track is made
	 */
	static int nextIDNum=0;
	//isActive?
	
	/**
	 * Maximum ROI height, for playing movies
	 */
	int maxHeight;
	
	/**
	 * Maximum image height, for playing movies
	 */
	int maxWidth;
	
	
	Vector<Boolean> isCollision;
	
//	Vector<Collision> collisions;
	
	
	private transient TrackMatch match;
	
	/**
	 * Length of time to pause between frames, ie 1/frameRate, in ms
	 */
	int frameLength = 71;//FrameRate=~14fps
	/**
	 * Access to the TrackBuilder
	 */
	transient TrackBuilder tb;
	/**
	 * Access to the experiment
	 */
	Experiment exp;
	
	transient Communicator comm;
	
	
	///////////////////////////
	// Constructors
	///////////////////////////	
	public Track(TrackBuilder tb){
		maxHeight=0;
		maxWidth=0;
		
		points = new Vector<TrackPoint>();
		isCollision = new Vector<Boolean>();
//		collisions = new Vector<Collision>(); 
		
		trackID = nextIDNum;
		nextIDNum++;
		this.tb = tb;

	}
	
	
	public Track(Vector<BackboneTrackPoint> pts, int ID){
		maxHeight=0;
		maxWidth=0;
		
		points = new Vector<TrackPoint>();
		points.addAll(pts);
		isCollision = new Vector<Boolean>();
//		collisions = new Vector<Collision>(); 
		
		trackID = ID;

	}
	
	public Track(Vector<BackboneTrackPoint> pts, Track tr){
		
		nextIDNum = tr.getNextIDNum();
		maxHeight = tr.maxHeight;
		maxWidth = tr.maxWidth;
		exp = tr.exp;
		isCollision = tr.isCollision;
		
		points = new Vector<TrackPoint>();
		points.addAll(pts);
		for (int i=0; i<points.size(); i++){
			points.get(i).track = this;
		}
		
		
		trackID = nextIDNum;
		nextIDNum++;
		
		
	}
	
	public Track(TrackPoint firstPt, TrackBuilder tb){
		maxHeight=0;
		maxWidth=0;
		
		points = new Vector<TrackPoint>();
		isCollision = new Vector<Boolean>(); 
		
		extendTrack(firstPt);
		
		trackID = nextIDNum;
		nextIDNum++;
		this.tb = tb;
		
	}
	
	///////////////////////////
	// Track building methods
	///////////////////////////	
	
	/**
	 * Adds the given point to the end of the Track
	 * @param pt
	 */
	public void extendTrack(TrackPoint pt){
		points.add(pt);
		pt.track=this;
//		pt.setTrack(this);
		isCollision.addElement(false);
		
		if(pt.rect.height>maxHeight){
			maxHeight = pt.rect.height; 
		}
		
		if(pt.rect.width>maxWidth){
			maxWidth = pt.rect.width; 
		}
		
	}
	
	/**
	 * Reloads the points as MaggotTrackPoints
	 * @return
	 */
//	public int reloadAsMaggotTrack(){
//		
//	}
	
	///////////////////////////
	// Distance methods
	///////////////////////////	
	
	/**
	 * Finds the nearest point in a list to the last point in the track 
	 * @param list List of points to search over
	 * @return Nearest point in the list to the last point in the track
	 */
	public TrackPoint nearestPointinList2End(Vector<TrackPoint> list){
		return points.lastElement().nearestInList2Pt(list);
	}
	
	/**
	 * Finds nearest points in a list to the last point in the track, up to NPTS points
	 * @param list List of points to search over
	 * @param nPts Max number of nearest points to find
	 * @return Nearest point in the list to the last point in the track, up to NPTS
	 */
	public Vector<TrackPoint> nearestNPts2End(Vector<TrackPoint> list, int nPts){
		return points.lastElement().nearestNPts2Pt(list, nPts);
	}
	
	/**
	 * Distance from last point in track to query point
	 * @param pt Query point
	 * @return Distance from last point in track to query point
	 */
	public double distFromEnd(TrackPoint pt){
		if ( (pt!=null) && (!points.isEmpty()) ){
			return pt.dist(points.lastElement());
		}
		return -1;
	}
	
	///////////////////////////
	// Area methods
	///////////////////////////	
	/**
	 * Calculates the mean area of the contours in the track
	 * @return Mean area of the contours in the track
	 */
	public double meanArea(){
		double sum = 0;
		ListIterator<TrackPoint> ptIter = points.listIterator();
		while (ptIter.hasNext()){
			sum += ptIter.next().area;
		}
		return ((double)sum)/points.size();
		
	}
	
	/**
	 * Calculates the median area of the contours in the track
	 * @return Median area of the contours in the track
	 */
	public double medianArea(){
		if (points.isEmpty()) {
	        return 0;
	    }
		Vector<Double> areas = new Vector<Double>();
		ListIterator<TrackPoint> ptIter = points.listIterator();
		while (ptIter.hasNext()){
			areas.add((Double)ptIter.next().area);
		}
		Collections.sort(areas);
		return areas.get(areas.size()/2);
		
	}
	
	
	
	///////////////////////////
	// Accessors
	///////////////////////////	
	public TrackPoint getEnd(){
		return points.lastElement();
	}
	

	
	
	public void playMovie() {
		comm = new Communicator();
		playMovie(trackID, null);
		
		if (!comm.outString.equals("")) new TextWindow("PlayMovie Error", comm.outString, 500, 500); 
	}
	
	public void playMovie(MaggotDisplayParameters mdp) {
		playMovie(trackID, mdp);
	}
	
	public void playMovie(int labelInd, MaggotDisplayParameters mdp){
		
//		String trStr = "";
		
		if (tb!=null){
			tb.comm.message("This track has "+points.size()+"points", VerbLevel.verb_message);
		}
		ListIterator<TrackPoint> tpIt = points.listIterator();
		if (tpIt.hasNext()) {
		
			
			TrackPoint point = tpIt.next();
			point.setTrack(this);
			
			//Get the first image
			ImageProcessor firstIm;
			if (mdp!=null) {
				firstIm = point.getIm(mdp);
			} else{
				firstIm = point.getIm();
			}
			
			ImageStack trackStack = new ImageStack(firstIm.getWidth(), firstIm.getHeight());
			
			trackStack.addSlice(firstIm);
			
//			trStr += point.infoSpill();
			
			//Add the rest of the images to the movie
			while(tpIt.hasNext()){
				point = tpIt.next();
				point.setTrack(this);
				
				//Get the next image
				ImageProcessor img;
				if (mdp!=null) {
					img = point.getIm(mdp);
				} else{
					img = point.getIm();
				}
//				img.
				trackStack.addSlice(img);
				
//				trStr += point.infoSpill();
			}
				
			//Show the stack
			ImagePlus trackPlus = new ImagePlus("Track "+trackID+": frames "+points.firstElement().frameNum+"-"+points.lastElement().frameNum ,trackStack);
			
//			trackPlus.flatten();
			trackPlus.show();
//			TextWindow tWin = new TextWindow("Track "+trackID+" info", trStr, 500, 500);
			
		}
	}
	

	
	public String infoString(){
		String info = "";
		
		info += "Track "+trackID+": "+points.size()+" points,";
		if (points.size()!=0){
			info += " frames "+points.firstElement().frameNum+"-"+points.lastElement().frameNum;
		}
		
		for (int i=0; i<points.size(); i++){
			info += "\n Point "+i+": "+points.get(i).infoSpill();
		}
		
		return info;
		
	}
	
	public void setMatch(TrackMatch tm){
		match = tm;
	}
	
	public TrackMatch getMatch(){
		return match;
	}
	
	public int getNextIDNum(){
		return nextIDNum;
	}
	
	public boolean isCollisionTrack(){
		return false;
	}
	
	/**
	 * Pre-Serializes all TrackPoints
	 */
	public void preSerialize(){
		ListIterator<TrackPoint> tpIt = points.listIterator();
		while (tpIt.hasNext()) {
			tpIt.next().preSerialize();
		}
	}
	
	/**
	 * Post-Deserializes all TrackPoints
	 */
	public void postDeserialize(){
		ListIterator<TrackPoint> tpIt = points.listIterator();
		while (tpIt.hasNext()) {
			tpIt.next().postDeserialize();
		}
	}
	
	
	
	protected static String makeDescription(String ID, Vector<TrackPoint> pointList, String addInfo){
		
		String lb = "\n";//System.lineSeparator();
		
		String d = "";
		d += "Track "+ID+lb+lb;
		
		if (pointList!=null){
			d += "Frames: ";
			d += (pointList.size()>0) ? pointList.firstElement().frameNum+"-"+pointList.lastElement().frameNum+lb+lb : "X-X"+lb+lb;
		}
		
		if(!addInfo.equals("")) d += addInfo+lb+lb;
		
		if (pointList!=null){
			d += "Points("+pointList.size()+"):"+lb;
			for (int i=0; i<pointList.size(); i++){
				TrackPoint pt = pointList.get(i);
				d += (i+1)+": "+pt.getTPDescription()+lb;
			}
		}else {
			d += "Frames: "+lb+lb;
			d += "Points(X):"+lb;
			d += "(point list)";
		}
		return d;
	}
	
	public String description(){
		return makeDescription(""+trackID, points, "");
	}
	
	public static String emptyDescription(){
		return makeDescription("X", null, "");
	}
	
	
	public void showEnergyPlot(){
		
		Plot plot = new Plot("Example plot", "Frame", "Energy");
		
		if (exp!=null && exp.Forces!=null){
			
			//Get x coords
			float[] frames = new float[points.size()];
			int startframe = points.firstElement().frameNum;
			for (int i=0; i<frames.length; i++){
				frames[i] = startframe+i;
			}
			
			Vector<float[]> energies = new Vector<float[]>();
			for (int i=0; i<exp.Forces.size(); i++){
				
				float[] energy = new float[points.size()];
				for (int j=0; j<frames.length; j++){
					energy[j] = exp.Forces.get(i).getEnergy(j, points);;
				}
					
				energies.add(energy);
			}
			
			Color[] colors = {Color.WHITE, Color.MAGENTA,Color.GREEN, Color.CYAN, Color.RED};
			for (int i=0; i<exp.Forces.size(); i++){
				if (i<MaggotDisplayParameters.DEFAULTshowForce.length && MaggotDisplayParameters.DEFAULTshowForce[i])
				plot.setColor(colors[i]);
				plot.addPoints(frames, energies.get(i), Plot.LINE); 
				plot.draw();
			}
			
		}
		
		plot.show();
	}
	

	
		
}
