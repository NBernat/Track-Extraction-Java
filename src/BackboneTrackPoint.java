import ij.ImagePlus;
import ij.gui.PolygonRoi;
import ij.process.FloatPolygon;
import ij.process.ImageProcessor;

import java.awt.Color;
import java.awt.Rectangle;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.PrintWriter;
import java.util.Vector;


/**
 * A TrackPoint containing an image, contour, midline, and backbone
 * @author Natalie
 *
 */
public class BackboneTrackPoint extends MaggotTrackPoint{
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	/**
	 * Identifies the point as a BACKBONETRACKPOINT
	 */
	final int pointType = 3;
	
	/**
	 * The number of pixels in the image considered as part of the maggot
	 */
	private int numPix;
	/**
	 * A list of X Coordinates of points that are considered as part of the maggot 
	 * <p>
	 * Contains numPix valid elements
	 */
	private float[] MagPixX;
	/**
	 * A list of X Coordinates of points that are considered as part of the maggot 
	 * <p>
	 * Contains numPix valid elements  
	 */
	private float[] MagPixY;
	/**
	 * A list of Intensity values (0;255) corresponding to the MagPix points 
	 * <p>
	 * Contains numPix valid elements
	 */
	private int[] MagPixI;
	/**
	 * A list of cluster indices (0;numBBpoints-1) corresponding to the nearest bbOld point to each MagPix point 
	 * <p>
	 * Contains numPix valid elements
	 */
	private int[] clusterInds;
	
	/**
	 * The number of points in the backbone
	 */
	int numBBPts;
	
	/**
	 * The backbone of the maggot
	 */
	private PolygonRoi backbone;
	
	/**
	 * For plotting
	 */
	protected transient FloatPolygon bbInit;
	/**
	 * Temporary backbone used to fit the final backbone
	 */
	protected transient FloatPolygon bbOld;
	/**
	 * Temporary backbone used to fit the final backbone
	 */
	protected transient FloatPolygon bbNew;
	
	transient BackboneFitter bf;
	
	protected boolean artificialMid;
	
	public BackboneTrackPoint(){
		
	}
	
	/**
	 * Constructs a BackboneTrackPoint
	 * @param x
	 * @param y
	 * @param rect
	 * @param area
	 * @param frame
	 * @param thresh
	 */
	public BackboneTrackPoint(double x, double y, Rectangle rect, double area,
			int frame, int thresh) {
		super(x, y, rect, area, frame, thresh);
	}
	
	

	/**
	 * Creates a new BackboneTrackPoint by copying the info in a MaggotTrackPoint
	 * @param mtp The MagggotTrackPoint to be copied
	 * @param numBBPts The number of points in the new Backbone
	 * @return The new BackboneTrackPoint
	 */
	public static BackboneTrackPoint convertMTPtoBTP(MaggotTrackPoint mtp, int numBBPts){
		
		//Copy all old info
		
		BackboneTrackPoint btp = new BackboneTrackPoint(mtp.x, mtp.y, mtp.rect, mtp.area, mtp.frameNum, mtp.thresh);
		mtp.copyInfoIntoBTP(btp);

		//Make new stuff
		btp.numBBPts = numBBPts; 
		
		return btp;
	}
	
	protected void fillInBackboneInfo(PolygonRoi newMidline, float[] prevOrigin){
		artificialMid = true;
		setBackboneInfo(newMidline, prevOrigin);
	}
	
	protected void setBackboneInfo(PolygonRoi newMidline, float[] prevOrigin){
		
		if(newMidline!=null){
			
			//Correct the origin of the midline
			FloatPolygon newMid = newMidline.getFloatPolygon();
			float[] xmid = new float[newMid.npoints];
			float[] ymid = new float[newMid.npoints];
			
			//Gather absolute coordinates for the backbones
			for(int i=0; i<newMid.npoints; i++){
				xmid[i] = newMid.xpoints[i]+prevOrigin[0];
				ymid[i] = newMid.ypoints[i]+prevOrigin[1];
			}
			FloatPolygon initBB = new FloatPolygon(xmid, ymid);//midline.getFloatPolygon();
			
			setInitialBB(new PolygonRoi(initBB, PolygonRoi.POLYLINE), numBBPts);
			setMagPix();
			setVoronoiClusters();
			
		}
	}
	
	
	/**
	 * Sets bbOld to the initial backbone guess
	 * @param initBB The initial guess, does not need to contain the correct number of points
	 * @param numPts The correct number of backbone points
	 */
	private void setInitialBB(PolygonRoi initBB, int numPts){
		
		if(bf!=null){
			bf.comm.message("Setting initial Backbone", VerbLevel.verb_debug);
		}
		numBBPts = numPts;
		backbone = initBB;
		if (initBB.getNCoordinates()!=numBBPts){
			if(bf!=null){
				bf.comm.message("initBB has "+initBB.getNCoordinates()+" coords; Interpolating segment", VerbLevel.verb_debug);
			}
			initBB = MaggotTrackPoint.getInterpolatedSegment(initBB, numPts, true);
		}
		if(bf!=null){
			if (initBB!=null){
				bf.comm.message("initBB sucessful", VerbLevel.verb_debug);
			} else{
				bf.comm.message("initBB interpolation failed", VerbLevel.verb_debug);
			}
			
		}
		bbInit = initBB.getFloatPolygon();
		bbOld = initBB.getFloatPolygon();
		
	}
	
	/**
	 * Complies lists of locations and intensity values for the points considered to be part of the maggot
	 */
	private int setMagPix(){
		if (im==null){
			return -1;
		}
		
		if(comm!=null){
			comm.message("Setting MagPix", VerbLevel.verb_debug);
		}
		if(bf!=null){
			bf.comm.message("Setting MagPix", VerbLevel.verb_debug);
		}
		
		
		//TODO MASK THE IMAGE SO IT ONLY SEES THE ONE MAGGOT
		ImageProcessor maskIm = im;
		
		MagPixX = new float[maskIm.getWidth()*maskIm.getHeight()];
		MagPixY = new float[maskIm.getWidth()*maskIm.getHeight()];
		MagPixI = new int[maskIm.getWidth()*maskIm.getHeight()];
		clusterInds = new int[maskIm.getWidth()*maskIm.getHeight()];
		numPix = 0;
		
		
		for(int X=0; X<maskIm.getWidth(); X++){
			for (int Y=0; Y<maskIm.getHeight(); Y++){
				if(maskIm.getPixel(X,Y)>thresh){
					MagPixX[numPix] = 0.5f+X+rect.x;
					MagPixY[numPix] = 0.5f+Y+rect.y;
					MagPixI[numPix] = maskIm.getPixel(X, Y);
					
					numPix++;
				}
			}
		}
		

		if(comm!=null){
			comm.message("Number of MagPix: "+numPix, VerbLevel.verb_debug);
		}
		
		if (numPix==0){
			return 0;
		}
		return 1;
		
	}
		
	/**
	 * Finds the nearest backbone point to each maggot pixel, stores index of bbOld for each pixel in clusterInds
	 */
	private void setVoronoiClusters(){
		
		if(bf!=null){
			bf.comm.message("Setting Voronoi clusters", VerbLevel.verb_debug);
		}
		
		//For each maggot pixel, find the nearest backbone point
		for (int pix=0; pix<numPix; pix++){
			
			double minDistSqr = java.lang.Double.POSITIVE_INFINITY;
			int minInd = -1;
			double distSqr;
			for(int cl=0; cl<numBBPts; cl++){
				distSqr = ((double)MagPixX[pix]-bbOld.xpoints[cl])*((double)MagPixX[pix]-bbOld.xpoints[cl]);
				distSqr+= ((double)MagPixY[pix]-bbOld.ypoints[cl])*((double)MagPixY[pix]-bbOld.ypoints[cl]);
				if(distSqr<minDistSqr){
					minDistSqr = distSqr;
					minInd = cl;
				}
			}
			if (minInd==-1) {
				bf.comm.message("Voronoi clusters went unset in frame "+frameNum, VerbLevel.verb_warning);
				
				
			}
			clusterInds[pix] = minInd;
		}
		
	}


	public double calcPointShift(){
		//Calculate the change between the old and new backbones
		double shift = 0;
		for(int i=0; i<numBBPts; i++){
			double xs = bbNew.xpoints[i]-bbOld.xpoints[i];
			double ys = bbNew.ypoints[i]-bbOld.ypoints[i];
			shift += (xs*xs)+(ys*ys);
		}
		
		return shift;
	}
	

	/**
	 * Stores a working backbone
	 * @param newBackbone The updated backbone
	 */
	protected void setBBNew(FloatPolygon newBackbone){
		bbNew = newBackbone;
	}

	/**
	 * Preps the working variables for the next iteration of the fitting algorithm
	 */
	protected void setupForNextRelaxationStep(){
		bbOld = bbNew;
		setVoronoiClusters();
	}
	
	/**
	 * Stores the final backbone
	 */
	protected void finalizeBackbone(){
		backbone = new PolygonRoi(bbOld, PolygonRoi.POLYLINE);
		
//		backboneX = new float[bbNew.npoints];
//		backboneY = new float[bbNew.npoints];
//		for (int i=0; i<bbNew.npoints; i++){
//			backboneX[i] = bbNew.xpoints[i];
//			backboneY[i] = bbNew.ypoints[i];
//		}
		
	}
	
	public int getNumPix(){
		return numPix;
	}
	
	public float getMagPixX(int ind){
		return MagPixX[ind];
	}

	public float getMagPixY(int ind){
		return MagPixY[ind];
	}
	
	public int getMagPixI(int ind){
		return MagPixI[ind];
	}
	
	public int getClusterInds(int ind){
		return clusterInds[ind];
	}
	
	public PolygonRoi getBackbone(){
		return backbone;
	}
	
	public ImageProcessor getIm(){

		return getIm(MaggotDisplayParameters.DEFAULTclusters,MaggotDisplayParameters.DEFAULTmid, MaggotDisplayParameters.DEFAULTinitialBB, 	
				MaggotDisplayParameters.DEFAULTcontour, MaggotDisplayParameters.DEFAULTht, MaggotDisplayParameters.DEFAULTforces, MaggotDisplayParameters.DEFAULTbackbone);
		
	}
	
	public ImageProcessor getIm(MaggotDisplayParameters mdp){
		
		if (mdp==null){
			return getIm();
		} else {
			return getIm(mdp.clusters, mdp.mid, mdp.initialBB, 	
				mdp.contour, mdp.ht, mdp.forces, mdp.backbone);
		}
	}
	
	
	public ImageProcessor getIm(boolean clusters, boolean mid, boolean initialBB, boolean contour, boolean ht, boolean forces, boolean bb){

		int expandFac = 10;//TODO MOVE TO PARAMETERS
		
		imOriginX = (int)x-(trackWindowWidth/2)-1;
		imOriginY = (int)y-(trackWindowHeight/2)-1;
		im.snapshot();
		
		ImageProcessor bigIm = im.resize(im.getWidth()*expandFac);
		
		int centerX = (int)(x-rect.x)*(expandFac);
		int centerY = (int)(y-rect.y)*(expandFac);
		ImageProcessor pIm = CVUtils.padAndCenter(new ImagePlus("Point "+pointID, bigIm), expandFac*trackWindowWidth, expandFac*trackWindowHeight, centerX, centerY);
		int offX = trackWindowWidth*(expandFac/2) - ((int)x-rect.x)*expandFac;//rect.x-imOriginX;
		int offY = trackWindowHeight*(expandFac/2) - ((int)y-rect.y)*expandFac;//rect.y-imOriginY;
		
		
		return drawFeatures(pIm, offX, offY, expandFac, clusters, mid, initialBB, contour, ht, forces, bb); 
		
	}
	
	public ImageProcessor getImWithMidline(PolygonRoi mid){
		int expandFac = 10;//TODO MOVE TO PARAMETERS
		
		imOriginX = (int)x-(trackWindowWidth/2)-1;
		imOriginY = (int)y-(trackWindowHeight/2)-1;
		im.snapshot();
		
		ImageProcessor bigIm = im.resize(im.getWidth()*expandFac);
		
		int centerX = (int)(x-rect.x)*(expandFac);
		int centerY = (int)(y-rect.y)*(expandFac);
		ImageProcessor pIm = CVUtils.padAndCenter(new ImagePlus("Point "+pointID, bigIm), expandFac*trackWindowWidth, expandFac*trackWindowHeight, centerX, centerY);
		int offX = trackWindowWidth*(expandFac/2) - ((int)x-rect.x)*expandFac;//rect.x-imOriginX;
		int offY = trackWindowHeight*(expandFac/2) - ((int)y-rect.y)*expandFac;//rect.y-imOriginY;
		
		ImageProcessor im = pIm.convertToRGB();
		Vector<PolygonRoi> mids = new Vector<PolygonRoi>();	mids.add(midline); mids.add(mid);
		Vector<Color> colors = new Vector<Color>(); colors.add(Color.YELLOW); colors.add(Color.RED);
		displayUtils.drawMidlines(im, mids, offX, offY, expandFac, colors);
//		displayUtils.drawMidline(im, mid, offX, offY, expandFac, Color.RED);

		
		return im;
	}
	
	protected ImageProcessor drawFeatures(ImageProcessor grayIm, int offX, int offY, int expandFac, boolean clusters, boolean mid, boolean initialBB, boolean contour, boolean ht, boolean forces, boolean bb){
		
		ImageProcessor im = grayIm.convertToRGB();
		
		
		//PIXEL CLUSTERS
		if (clusters) displayUtils.drawClusters(im, numPix, MagPixX, MagPixY, clusterInds, expandFac, offX, offY, rect);
		
		//MIDLINE
		if (mid) displayUtils.drawMidline(im, midline, offX, offY, expandFac, Color.YELLOW);
		

		//INITIAL SPINE
		if (initialBB) displayUtils.drawBBInit(im, bbInit, offX, offY, rect, expandFac, Color.YELLOW);
		
		//CONTOUR
		if (contour) displayUtils.drawContour(im, cont, expandFac, offX, offY, Color.BLUE);
		
		 
		//HEAD AND TAIL
		if (ht){
			displayUtils.drawPoint(im, head, expandFac, offX, offY, Color.RED);
			displayUtils.drawPoint(im, tail, expandFac, offX, offY, Color.GREEN);
		}
		
		//FORCES
		if (forces) {
			boolean error = false;
			
			String status = "Frame "+frameNum+"\n";
	//		displayUtils.drawForces(im, bbNew, bf.Forces, bf.BTPs, expandFac, expandFac, offX, offY, rect);
			Color[] colors = {Color.WHITE, Color.MAGENTA,Color.GREEN, Color.CYAN, Color.RED};
			for(int f=0; f<(bf.Forces.size()-1); f++){
				
				im.setColor(colors[f]);
				try{
					status += bf.Forces.get(f).getName()+": ";
					
					FloatPolygon targetPts = bf.Forces.get(f).getTargetPoints(frameNum-bf.BTPs.firstElement().frameNum, bf.BTPs);
					
					if (targetPts!=null && targetPts.npoints==bbNew.npoints){
						for (int i=0; i<targetPts.npoints; i++){
							
							int x1 = offX + (int)(expandFac*(bbNew.xpoints[i]-rect.x));
							int y1 = offY + (int)(expandFac*(bbNew.ypoints[i]-rect.y));
							int x2 = (int)(expandFac*(targetPts.xpoints[i]-rect.x)+offX);
							int y2 = (int)(expandFac*(targetPts.ypoints[i]-rect.y)+offY);
							status += "("+(targetPts.xpoints[i]-rect.x)+","+(targetPts.ypoints[i]-rect.y)+") ";
							
							im.drawLine(x1, y1, x2, y2);
							im.drawOval(x2, y2, 2, 2);
		//					im.drawDot((int)(expandFac*(targetPts.xpoints[i]-rect.x)+offX), (int)(expandFac*(targetPts.ypoints[i]-rect.y)+offY));
							
						}
					}
					status+="\n";
				} catch (Exception e ){
					error = true;
//					new TextWindow("Plotting Error: Forces", status, 500, 500);
				}
			}
			
			if (error && track!=null && track.comm!=null) comm.message(status, VerbLevel.verb_error);
			
		}
			
			
		//BACKBONE
		if (bb) displayUtils.drawBackbone(im, backbone.getFloatPolygon(), expandFac, offX, offY, rect, Color.PINK);
		
		return im;
	}
	
	
	public String getTPDescription(){
		String s = super.getTPDescription();
		
		if (midline!=null && htValid) s+= "         ";
				
		if (artificialMid) s+=" M-f";
		return s;
	}
	
	public double bbInitDist(FloatPolygon otherbb){
		double totalDistSqr = 0;
		
		if (bbInit!=null && otherbb!=null && bbInit.npoints!=0 && otherbb.npoints!=0 && bbInit.npoints==otherbb.npoints){
			
			for (int i=0; i<bbInit.npoints; i++){
				totalDistSqr+= (bbInit.xpoints[i]-otherbb.xpoints[i])*(bbInit.xpoints[i]-otherbb.xpoints[i]);
				totalDistSqr+= (bbInit.ypoints[i]-otherbb.ypoints[i])*(bbInit.ypoints[i]-otherbb.ypoints[i]);
			}
			
		} else {
			return -1.0;	
		}
		
		return Math.sqrt(totalDistSqr);
	}
	
	
	public int toDisk(DataOutputStream dos, PrintWriter pw){
		
		//Write all ImTrackPoint data
		super.toDisk(dos, pw);
		
		try {
			//Write # of backbone points
			dos.writeShort(backbone.getNCoordinates());
			//Write backbone points
			FloatPolygon bfp = backbone.getFloatPolygon();
			for (int i=0; i<bfp.npoints; i++){
				dos.writeFloat(bfp.xpoints[i]);
				dos.writeFloat(bfp.ypoints[i]);
			}
			//Write artificial mid
			dos.writeByte(artificialMid ? 1:0);
		} catch (Exception e) {
			if (pw!=null) pw.println("Error writing BackboneTrackPoint image for point "+pointID+"; aborting save");
			return 1;
		}
		
		return 0;
	}
	
	public int sizeOnDisk(){
		
		int size = super.sizeOnDisk();
		size += Short.SIZE/Byte.SIZE + (2*backbone.getNCoordinates())*java.lang.Float.SIZE/Byte.SIZE+ Byte.SIZE/Byte.SIZE;
		
		return size;
	}

	public static BackboneTrackPoint fromDisk(DataInputStream dis, Track t){
		
		BackboneTrackPoint btp = new BackboneTrackPoint();
		if (btp.loadFromDisk(dis,t)==0){
			return btp;
		} else {
			return null;
		}
	}
	
	protected int loadFromDisk(DataInputStream dis, Track t){
		
		//Load all superclass info
		if (super.loadFromDisk(dis, t)!=0){
			return 1;
		}
		
		//read new data
		try {
			//#bbPts
			numBBPts = dis.readShort();
			
			//backbone
			float[] bbX = new float[numBBPts];
			float[] bbY = new float[numBBPts];
			for (int i=0; i<numBBPts; i++){
				bbX[i] = dis.readFloat();
				bbY[i] = dis.readFloat();
			}
			backbone = new PolygonRoi(bbX,  bbY, PolygonRoi.POLYLINE);
			
			//artificialMid
			artificialMid = (dis.readByte()==1);
			
		} catch (Exception e) {
			//if (pw!=null) pw.println("Error writing TrackPoint Info for point "+pointID+"; aborting save");
			return 2;
		}
		
		return 0;
	}
	
}
