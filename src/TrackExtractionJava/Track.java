package TrackExtractionJava;

import ij.ImagePlus;
import ij.ImageStack;
import ij.gui.Plot;
import ij.process.ColorProcessor;
import ij.process.ImageProcessor;
import ij.text.TextWindow;

import java.awt.Color;
import java.awt.Robot;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.PrintWriter;
import java.io.Serializable;
import java.io.StringWriter;
import java.util.Collections;
import java.util.List;
import java.util.ListIterator;
import java.util.Scanner;
import java.util.Vector;

import javax.swing.JButton;
import javax.swing.JFrame;

import sun.awt.SunToolkit.InfiniteLoop;


public class Track implements Serializable{
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	/**
	 * Used to generate unique IDs for the TrackPoints
	 * <p> 
	 * Incremented each time a new track is made
	 */
	static int nextIDNum=0;
	/**
	 * Constituent TrackPoints
	 */
	protected Vector<TrackPoint> points;
	/**
	 * 
	 */
	protected boolean valid=true;
	
	protected boolean diverged = false;
	/**
	 * Unique identifier for the track 
	 */
	private int trackID;
	
	/**
	 * Maximum ROI height, for playing movies
	 */
	private int maxHeight;
	
	/**
	 * Maximum image height, for playing movies
	 */
	private int maxWidth;
	
	
	Vector<Boolean> isCollision;
	
	
	private transient TrackMatch match;
	
	/**
	 * Access to the TrackBuilder
	 */
	transient TrackBuilder tb;
	/**
	 * Access to the experiment
	 */
	Experiment exp;
	
	transient Communicator comm;
	
	String otherInfo = "";
	
	
	public Track(){
		
	}
	
	public Track(TrackBuilder tb){
		maxHeight=0;
		maxWidth=0;
		
		points = new Vector<TrackPoint>();
		isCollision = new Vector<Boolean>();
		
		trackID = nextIDNum;
		nextIDNum++;
		this.tb = tb;

	}
	
	public Track(List<? extends TrackPoint> pts, int ID){
		maxHeight=0;
		maxWidth=0;
		
		points = new Vector<TrackPoint>();
		points.addAll(pts);
		isCollision = new Vector<Boolean>();
		
		trackID = ID;

	}
	public Track(Vector<? extends TrackPoint> pts, int ID){
		maxHeight=0;
		maxWidth=0;
		
		points = new Vector<TrackPoint>();
		points.addAll(pts);
		isCollision = new Vector<Boolean>();
		
		trackID = ID;

	}
	
	/**
	 * StartInd and endInd are indices to trackpoints; both inclusive 
	 * @param tr
	 * @param startInd
	 * @param endInd
	 */
	public Track(Track tr, int startInd, int endInd){
		
		nextIDNum = tr.getNextIDNum();
		maxHeight = tr.maxHeight;
		maxWidth = tr.maxWidth;
		exp = tr.exp;
		isCollision = tr.isCollision;
		
		points = new Vector<TrackPoint>();
		for (int i=startInd; i<=endInd; i++){
			points.add(tr.getPoint(i));
			points.get(i-startInd).track = this;
		}
		
		
		trackID = nextIDNum;
		nextIDNum++;
		
		
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
	
	
	public double[] getAreas(){
		if (points==null) return new double[0];
		double[] areas = new double[points.size()];
		for (int i=0;i<points.size(); i++) areas[i]=points.get(i).area;
		return areas;
	}
	
	public double[] getHTdists(){
		if (points==null || points.firstElement().getPointType()<MaggotTrackPoint.pointType) return new double[0];
		double[] HTdist = new double[points.size()];
		for (int i=0;i<points.size(); i++) {
			MaggotTrackPoint mtp = (MaggotTrackPoint)points.get(i);
			if (mtp.htValid){
				HTdist[i]=Math.sqrt((mtp.head.x-mtp.tail.x)*(mtp.head.x-mtp.tail.x) + (mtp.head.y-mtp.tail.y)*(mtp.head.y-mtp.tail.y) );
			} else {
				HTdist[i] = Double.POSITIVE_INFINITY;
			}
		}
		return HTdist;
	}
	
	/**
	 * Calculates trackpoint energies using default fitting parameters
	 * 
	 */
//	public void calcEnergies(){
//		calcEnergies(new FittingParameters());
//	}
	
	/**
	 * Calculates trackpoint energies using 
	 * @param fp
	 */
//	public void calcEnergies(FittingParameters fp){
//		
//		for (int i=0; i<points.size(); i++){
//			points.get(i).calcEnergies(fp);
//		}
//	}
	
	/**
	 * Gathers and returns energies of the given type from trackpoints
	 * 
	 * returns [] if energy type is not available for this type of point
	 * 
	 * @param energyType
	 */
	public double[] getEnergies(String energyType){
		
		if (points==null || points.size()==0){
			System.out.println("No points in track");
			return null;
		}
		
		double[] e = new double[points.size()];
		for (int i=0; i<e.length; i++){
			e[i] = points.get(i).getEnergy(energyType);
		}
		
		return e;
	}
	
	public double[] getEnergyMeanStdDev(String energyType){
		double[] energies = getEnergies(energyType);
		return getEnergyMeanStdDev(energies, energyType);
	}
	
	
	public double[] getEnergyMeanStdDev(double[] energies, String energyType){
		
		double[] meanStdDev = new double[2];
		meanStdDev[0] = MathUtils.mean(energies);
		meanStdDev[1] = MathUtils.stdDev(energies, meanStdDev[0]);
		return meanStdDev;
	}
	
	public Vector<Gap> findBapGaps(String eType, int numStdDevs){
		return findBadGaps(eType, numStdDevs, 1);
	}
	
	public Vector<Gap> findBadGaps(String eType, int numStdDevs, int minValidSegmentLen){
		
		double[] e = getEnergies(eType);
		
		double[] meanStdDev = getEnergyMeanStdDev(e, eType);
		double thresh = meanStdDev[0] + numStdDevs*meanStdDev[1];
		
		boolean[] bad = new boolean[e.length];
		for (int i=0; i<bad.length; i++){
			bad[i] = e[i]>thresh;
		}
		
		Vector<Gap> badGaps = Gap.bools2Segs(bad);
		if (badGaps.size()>1) BBFPointListGenerator.mergeGaps(badGaps, minValidSegmentLen, null);
		
		
 		return badGaps;

	}
	
	
	/**
	 * Clips and returns the list of track points indicated by the startFrame (inclusive) and endFrame (exclusive)
	 * @param startFrame
	 * @param endFrame
	 * @return
	 */
	protected Vector<TrackPoint> clipPoints(int startFrame, int endFrame){
		
		Vector<TrackPoint> clippings = new Vector<TrackPoint>(points.subList(startFrame-points.firstElement().frameNum, endFrame-points.firstElement().frameNum));
		points.subList(startFrame-points.firstElement().frameNum, endFrame-points.firstElement().frameNum).clear();
		return clippings;
	}
	
	///////////////////////////
	// Accessors
	///////////////////////////	
	
	protected void setValid(boolean v){
		valid=v;
	}
	
	public boolean valid(){
		return valid;
	}
	
	protected void setDiverged(boolean v){
		diverged=v;
	}
	
	public boolean diverged(){
		return diverged;
	}
	
	protected void setTrackID(int tid){
		trackID = tid;
	}
	
	public int getTrackID(){
		return trackID;
	}
	
	public int getNumPoints(){
		return points.size();
	}
	
	public Vector<TrackPoint> getPoints(){
		return points;
	}
	
	public TrackPoint getPoint(int index){
		if (index<0|| index>points.size()){
			return null;
		} else {
			return points.get(index);
		}
	}

	public TrackPoint getFramePoint(int frame){
		if (frame<getStart().frameNum || frame>getEnd().frameNum){
			return null;
		} else {
			return points.get(frame-getStart().frameNum);
		}
	}
	
	public TrackPoint getStart(){
		return points.firstElement();
	}
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
	
	public void playBlankMovie(){
		MaggotDisplayParameters mdp = new MaggotDisplayParameters();
		mdp.setAllFalse();
		playMovie(trackID, mdp);

	}
	
	
	public ImagePlus playMovie(int labelInd, MaggotDisplayParameters mdp){
		return getMovieStack(labelInd, mdp, true); 
	}
	
	public ImagePlus getMovieStack(int labelInd, MaggotDisplayParameters mdp, boolean showMovie){
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
				trackStack.addSlice(img);
			}
				
			//Show the stack
			ImagePlus trackPlus = new ImagePlus("Track "+trackID+": frames "+points.firstElement().frameNum+"-"+points.lastElement().frameNum ,trackStack);
			
			if (showMovie) trackPlus.show();
			return trackPlus;
		} else {
			return null;
		}
	}
	
	public void drawTrack(ColorProcessor im){
		
		if (points==null){
			return;
		}
		
		//draw white for valid, red for invalid
		Color c;// = (valid)? new Color(255, 255, 255):new Color(255, 0, 0);
		if (valid){
			if (diverged){
				c = new Color(255, 255, 0);//yellow
			} else{
				c = new Color(255, 255, 255);//white
			}
		} else {
			c = new Color(255, 0, 0);//red
		}
		
		
		for (int i=0; i<points.size(); i++){
			ImTrackPoint itp = (ImTrackPoint)points.get(i);
			itp.drawPoint(im, c);
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

	public int[] getMaxPlateDimensions(){
		int[] dim = {0,0};
		
		for (TrackPoint tp : points){
			int tDim[] = {tp.rect.x+tp.rect.width, tp.rect.y+tp.rect.height};
			if (tDim[0]>dim[0]){
				dim[0]=tDim[0];
			}
			if (tDim[1]>dim[1]){
				dim[1]=tDim[1];
			}
		}
		
		return dim;
		
		
	}
	
	
	public void showFitting(){
		if (points==null || points.size()==0 || points.firstElement().getPointType()!=MaggotTrackPoint.pointType){
			return;
		}
		
		//Make a button
//		JFrame buttonFrame = new JFrame("NextIterationFrame");
////		buttonFrame.setSize(200, 200);
//		
//		JButton nextButton = new JButton("Next Iteration");
//		nextButton.addActionListener(new ActionListener() {
//			@Override
//			public void actionPerformed(ActionEvent e) {
//				notify();
//			}
//		});
//		
//		buttonFrame.add(nextButton);
//		buttonFrame.setVisible(true);
		
		
		BackboneFitter bbf = new BackboneFitter(this);
		bbf.doPause = true;
		bbf.userIn = new Scanner(System.in);
		bbf.userOut = System.out;
		
		// TODO pass in a track fitting scheme and/or Fitting Params
		bbf.fitTrackNewScheme();
		
		
		
	}
	
	public Track fitTrack(FittingParameters fp){
		if (points==null || points.size()==0 || points.firstElement().getPointType()!=MaggotTrackPoint.pointType){
			return null;
		}
		BackboneFitter bbf = new BackboneFitter(this, fp );
		return fitTrack(bbf);
	}
	
	public Track fitTrack(BackboneFitter bbf){
		bbf.fitTrackNewScheme();
		return bbf.workingTrack;
	}
	
	public int toDisk(DataOutputStream dos, PrintWriter pw){
		
		if (pw!=null) pw.println("Writing track "+trackID+"...");
		
		//Write the size in bytes in this track to disk
		try {
			if (pw!=null) pw.println("Getting track size");
			int nBytes = sizeOnDisk(pw);
			if (pw!=null) pw.println("Size on disk: "+nBytes+" bytes");
			if (nBytes>=0){
				if (pw!=null) pw.println("Writing Track size");
				dos.writeInt(nBytes);
				dos.writeInt(trackID);
			} else {
				if (pw!=null) pw.println("...Error getting size of track "+trackID+"; aborting save");
				return 3;
			}
		} catch (Exception e) {
			StringWriter sw = new StringWriter();
			PrintWriter pw2 = new PrintWriter(sw);
			e.printStackTrace(pw2);
			if (pw!=null) pw.println(sw.toString());
			if (pw!=null) pw.println("...Error writing size of track "+trackID+"; aborting save");
			return 3;
		}

		//Write the flags
		try{
			String v;
			if (valid){
				v="true";
			} else{
				v="false";
			}
			if (pw!=null) pw.println("Writing valid flag ("+v+")");
			dos.writeBoolean(valid);
			
			if (diverged){
				v="true";
			} else{
				v="false";
			}
			if (pw!=null) pw.println("Writing diverged flag ("+v+")");
			dos.writeBoolean(diverged);
		} catch(Exception e){
			if (pw!=null) pw.println("...Error writing valid flag in track "+trackID+"; aborting save");
			return 4;
		}
		
		//Write the # of points in this track to disk
		try {
			if (pw!=null) pw.println("Writing #pts ("+points.size()+")");
			if (points.size()>=0){
				dos.writeInt(points.size());
			} else {
				if (pw!=null) pw.println("...Error getting # of points in track "+trackID+"; aborting save");
				return 2;
			}
		} catch (Exception e) {
			if (pw!=null) pw.println("...Error writing # of points in track "+trackID+"; aborting save");
			return 2;
		}
		
		//Write the points to disk
		try {
			if (pw!=null) pw.println("Writing points...");
			for (int i=0; i<points.size(); i++){
				if (points.get(i).toDisk(dos,pw)!=0){
					if (pw!=null) pw.println("...Error writing TrackPoint "+points.get(i).pointID+"; aborting save");
					return 1;
				}
				if (i==(points.size()-1)){
					if (pw!=null) pw.println("Last point in track "+trackID+" written");
				}
			}
			if (pw!=null) pw.println("...done writing points");
		} catch (Exception e) {
			if (pw!=null) pw.println("...Error writing points; aborting save");
			return 1;
		}
		
		if (pw!=null) pw.println("...Track Saved!");
		return 0;
	}

	
	private int sizeOnDisk(PrintWriter pw){
		
		//Add the size of the "# of points" field (32-bit integer)
		int size = Integer.SIZE/Byte.SIZE;
		
		//Add the size of each point
		for (int i=0; i<points.size(); i++){
			size += points.get(i).sizeOnDisk();
		}
		
		return size;
	}
	
	public static Track fromDisk(DataInputStream dis, int pointType, Experiment experiment, PrintWriter pw){
		
		Track tr = new Track();
		
		//load data
		int result=tr.loadFromDisk(dis, pointType, experiment, pw);
		if (result==0){
			return tr;
		} else {
			System.out.println(result);
			return null;
		}
	}
	
	private int loadFromDisk(DataInputStream dis, int pointType, Experiment experiment, PrintWriter pw){
		
		points = new Vector<TrackPoint>();
		exp = experiment;
		trackID=nextIDNum++;
		
		//advance past size on disk
		try {
			int size = dis.readInt();
			if (pw!=null) pw.println(size+" bytes to load...");
			trackID = dis.readInt();
		} catch (Exception e) {
			if (pw!=null) pw.println("ERROR: Unable to advance past field 'size on disk'");
			return 4;
		}

		try {
			valid = dis.readBoolean();
			if (pw!=null) pw.println("Valid track: "+valid);
			diverged = dis.readBoolean();
			if (pw!=null) pw.println("Diverged track: "+diverged);
		} catch (Exception e) {
			if (pw!=null) pw.println("ERROR: Unable to gather 'valid' field");
			return 4;
		}
		
		//Read nPts and then the points
		try {
			int nPts = dis.readInt();
			if (pw!=null) pw.println(nPts+" Points to load...");
			if (pointType==3 && (!valid || diverged)){
				pointType=2;
			}
			
			switch (pointType){
				case 0: 
					TrackPoint tp;
					for (int i=0; i<nPts; i++){
						//Load TrackPoints
						tp = TrackPoint.fromDisk(dis, this, pw);
						if (tp!=null) {
							points.addElement(tp);
						} else {
							if (pw!=null) pw.println("ERROR: null Trackpoint ("+i+"/"+(nPts-1)+")");
							return 3;
						}
					}
					break;
				case 1:
					ImTrackPoint itp;
					for (int i=0; i<nPts; i++){
						//Load ImTrackPoints
						itp = ImTrackPoint.fromDisk(dis, this, pw);
						if (itp!=null) {
							points.addElement(itp);
						} else {
							if (pw!=null) pw.println("ERROR: null ImTrackpoint ("+i+"/"+(nPts-1)+")");
							return 3;
						}
					}
					break;
				case 2:
					MaggotTrackPoint mtp;
					for (int i=0; i<nPts; i++){
						//Load MaggotTrackPoints
						mtp = MaggotTrackPoint.fromDisk(dis, this, pw);
						if (mtp!=null) {
							points.addElement(mtp);
						} else {
							if (pw!=null) pw.println("ERROR: null MaggotTrackpoint ("+i+"/"+(nPts-1)+")");
							return 3;
						}
					}
					break;
				case 3:
					BackboneTrackPoint btp;
					for (int i=0; i<nPts; i++){
						//Load BackboneTrackPoints
						btp = BackboneTrackPoint.fromDisk(dis, this, pw);
						if (btp!=null) {
							points.add(btp);
						} else {
							if (pw!=null) pw.println("ERROR: null BackboneTrackpoint ("+i+"/"+(nPts-1)+")");
							return 3000+i;
						}
					}
					break;
				default:
					//Invalid point type
					if (pw!=null) pw.println("ERROR: Invalid Point type ("+pointType+")");
					return 2;
			}
			
			if (pw!=null) pw.println("...done loading points");
			
		} catch (Exception e){
			StringWriter sw = new StringWriter();
			PrintWriter prw = new PrintWriter(sw);
			e.printStackTrace(prw);
			if (pw!=null) pw.println("ERROR: Unable to load points\n"+sw.toString());
			return 1;
		}
		
		return 0;
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
		
		if (pointList!=null){d += "Points("+pointList.firstElement().getTypeName()+"; "+pointList.size()+"):"+lb;

		d+= String.format("%5s: %5s %8s %13s %10s", "Point", "Frame", "Point ID", "Loc(x,y)[px]", "Area[px]" )+lb;
		for (int i=0; i<pointList.size(); i++){
				TrackPoint pt = pointList.get(i);
				d += pt.getTPDescription(i+1)+lb;//d += (i+1)+": "+pt.getTPDescription(i)+lb;
			}
		}else {
			d += "Frames: "+lb+lb;
			d += "Points(X):"+lb;
			d += "(point list)";
		}
		return d;
	}
	
	public String description(){
		return makeDescription(""+trackID, points, otherInfo);
	}
	
	public static String emptyDescription(){
		return makeDescription("X", null, "");
	}
	
	
	public void showEnergyPlot(){
		
		Plot plot = new Plot("Example plot", "Frame", "Energy");
		
		if (exp!=null && exp.getForces()!=null){
			
			//Get x coords
			float[] frames = new float[points.size()];
			int startframe = points.firstElement().frameNum;
			for (int i=0; i<frames.length; i++){
				frames[i] = startframe+i;
			}
			
			Vector<float[]> energies = new Vector<float[]>();
			for (int i=0; i<exp.getForces().size(); i++){
				
				float[] energy = new float[points.size()];
				for (int j=0; j<frames.length; j++){
					//TODO
//					energy[j] = exp.getForces().get(i).getEnergy(j, points);;
				}
					
				energies.add(energy);
			}
			
			Color[] colors = {Color.WHITE, Color.MAGENTA,Color.GREEN, Color.CYAN, Color.RED};
			for (int i=0; i<exp.getForces().size(); i++){
				if (i<MaggotDisplayParameters.DEFAULTshowForce.length && MaggotDisplayParameters.DEFAULTshowForce[i])
				plot.setColor(colors[i]);
				plot.addPoints(frames, energies.get(i), Plot.LINE); 
				plot.draw();
			}
			
		}
		
		plot.show();
	}
	

	
		
}
