import ij.ImagePlus;
import ij.gui.PolygonRoi;
import ij.process.FloatPolygon;
import ij.process.ImageProcessor;

import java.awt.Color;
import java.awt.Rectangle;
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
		if(mtp.midline!=null){
			
			FloatPolygon initBB = mtp.midline.getFloatPolygon();

			for(int i=0; i<initBB.npoints; i++){
				initBB.xpoints[i] += btp.rect.x;
				initBB.ypoints[i] += btp.rect.y;
			}
			
			btp.setInitialBB(new PolygonRoi(initBB, PolygonRoi.POLYLINE), numBBPts);
			btp.setMagPix();
			btp.setVoronoiClusters();
		}
		
		return btp;
	}
	
	protected void fillInMidline(PolygonRoi newMidline, float[] prevOrigin){
		
		if(newMidline!=null){
			
			//Correct the origin of the midline
			FloatPolygon newMid = newMidline.getFloatPolygon();
			float[] xmid = new float[newMid.npoints];
			float[] ymid = new float[newMid.npoints];
			float offX = prevOrigin[0]-rect.x;
			float offY = prevOrigin[1]-rect.y;
			for(int i=0; i<newMid.npoints; i++){
				xmid[i] = newMid.xpoints[i]+offX;
				ymid[i] = newMid.ypoints[i]+offY;
			}
			midline = new PolygonRoi(new FloatPolygon(xmid, ymid), PolygonRoi.POLYLINE);

			for(int i=0; i<newMid.npoints; i++){
				xmid[i] += rect.x;
				ymid[i] += rect.y;
			}
			FloatPolygon initBB = new FloatPolygon(xmid, ymid);//midline.getFloatPolygon();
//			for(int i=0; i<initBB.npoints; i++){
//				initBB.xpoints[i] += rect.x;
//				initBB.ypoints[i] += rect.y;
//			}
			
			setInitialBB(new PolygonRoi(initBB, PolygonRoi.POLYLINE), numBBPts);
			setMagPix();
			boolean anyUnset = setVoronoiClusters();
			
			if (bf!=null){
				 if (anyUnset) bf.comm.message("Voronoi clusters went unset in frame "+frameNum, VerbLevel.verb_warning);
			}
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
	private boolean setVoronoiClusters(){
		boolean anyUnset = false;
		
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
			if (minInd==-1) anyUnset=true;
			clusterInds[pix] = minInd;
		}
		
		return anyUnset;
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
		backbone = new PolygonRoi(bbNew, PolygonRoi.POLYLINE);
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

		boolean clusters = false;
		boolean mid = true;
		boolean initialBB = true; 
		boolean contour = false;
		boolean ht = false;
		boolean forces = false;
		boolean backbone = true;
		
		return getIm(clusters, mid, initialBB, contour, ht, forces, backbone);
		
	}
	
	
	public ImageProcessor getIm(boolean clusters, boolean mid, boolean initialBB, boolean contour, boolean ht, boolean forces, boolean backbone){

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
		
		
		return drawFeatures(pIm, offX, offY, expandFac, clusters, mid, initialBB, contour, ht, forces, backbone); 
		
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
	
	protected ImageProcessor drawFeatures(ImageProcessor grayIm, int offX, int offY, int expandFac, boolean clusters, boolean mid, boolean initialBB, boolean contour, boolean ht, boolean forces, boolean backbone){
		
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
	//		displayUtils.drawForces(im, bbNew, bf.Forces, bf.BTPs, expandFac, expandFac, offX, offY, rect);
			Color[] colors = {Color.WHITE, Color.MAGENTA,Color.GREEN, Color.CYAN, Color.RED};
			for(int f=0; f<bf.Forces.size(); f++){
				
				im.setColor(colors[f]);
				
				FloatPolygon targetPts = bf.Forces.get(f).getTargetPoints(frameNum-bf.BTPs.firstElement().frameNum, bf.BTPs);
				
				if (targetPts!=null){
					for (int i=0; i<targetPts.npoints; i++){
						
						int x1 = offX + (int)(expandFac*(bbNew.xpoints[i]-rect.x));
						int y1 = offY + (int)(expandFac*(bbNew.ypoints[i]-rect.y));
						int x2 = (int)(expandFac*(targetPts.xpoints[i]-rect.x)+offX);
						int y2 = (int)(expandFac*(targetPts.ypoints[i]-rect.y)+offY);
						
						im.drawLine(x1, y1, x2, y2);
	//					im.drawDot((int)(expandFac*(targetPts.xpoints[i]-rect.x)+offX), (int)(expandFac*(targetPts.ypoints[i]-rect.y)+offY));
						
					}
				}
				
			}
		}
			
			
		//BACKBONE
		if (backbone) displayUtils.drawBackbone(im, bbNew, expandFac, offX, offY, rect, Color.PINK);
		
		return im;
	}
	
//	private int getPlotXCoord(float xCoord, int offX, int expandFac){
//		
//		return (int)(offX + expandFac*(xCoord-rect.x));
//	}
//	
//	private int getPlotYCoord(float yCoord, int offY, int expandFac){
//		
//		return (int)(offY + expandFac*(yCoord-rect.x));
//	}
	
	
}
