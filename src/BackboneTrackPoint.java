import ij.ImagePlus;
import ij.gui.Line;
import ij.gui.Overlay;
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
	private int[] MagPixX;
	/**
	 * A list of X Coordinates of points that are considered as part of the maggot 
	 * <p>
	 * Contains numPix valid elements  
	 */
	private int[] MagPixY;
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
		if(mtp.midline!=null){
			
			FloatPolygon initBB = mtp.midline.getFloatPolygon();

			for(int i=0; i<initBB.npoints; i++){
				initBB.xpoints[i] += btp.rect.x;
				initBB.ypoints[i] += btp.rect.y;
			}
			
			btp.setInitialBB(new PolygonRoi(initBB, PolygonRoi.POLYLINE), numBBPts);
			
		} else{
			return null;
		}
			
		btp.setMagPix();
		btp.setVoronoiClusters();
		
		return btp;
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
			initBB = MaggotTrackPoint.getInterpolatedSegment(initBB, numPts);
		}
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
		
		
		//TODO MASK THE IMAGE SO IT ONLY SEES THE ONE MAGGOT
		ImageProcessor maskIm = im;
		
		MagPixX = new int[maskIm.getWidth()*maskIm.getHeight()];
		MagPixY = new int[maskIm.getWidth()*maskIm.getHeight()];
		MagPixI = new int[maskIm.getWidth()*maskIm.getHeight()];
		clusterInds = new int[maskIm.getWidth()*maskIm.getHeight()];
		numPix = 0;
		
		
		for(int X=0; X<maskIm.getWidth(); X++){
			for (int Y=0; Y<maskIm.getHeight(); Y++){
				if(maskIm.getPixel(X,Y)>thresh){
					MagPixX[numPix] = X+rect.x;
					MagPixY[numPix] = Y+rect.y;
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
			clusterInds[pix] = minInd;
		}
	}


	public double calcPointShift(){
		//Calculate the change between the old and new backbones
		return -1.0;
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
	
	public int getMagPixX(int ind){
		return MagPixX[ind];
	}

	public int getMagPixY(int ind){
		return MagPixY[ind];
	}
	
	public int getMagPixI(int ind){
		return MagPixI[ind];
	}
	
	public int getClusterInds(int ind){
		return clusterInds[ind];
	}
	
	public ImageProcessor getIm(){

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
		return drawFeatures(pIm, offX, offY, expandFac); 
		
	}
	
	protected ImageProcessor drawFeatures(ImageProcessor grayIm, int offX, int offY, int expandFac){
		ImageProcessor im = grayIm.convertToRGB();
//		Overlay overlay = new Overlay();
//		midline.setColor(Color.YELLOW);
//		overlay.add(midline);
//		overlay.add(new Line(0.0,0.0,5.0,5.0));
		
//		im.drawOverlay(overlay);
//		return im;
		
//		ImagePlus imp = new ImagePlus("", im);
//		imp.setOverlay(overlay);
//		imp.flatten();
//		return imp.getProcessor();
		
		
//		im.drawRoi(midline);
		
//		Color[] colors = {Color.WHITE, Color.PINK, Color.MAGENTA, Color.RED, Color.ORANGE, Color.YELLOW, Color.GREEN, Color.CYAN, Color.BLUE};
//		
//		for (int i=0; i<numPix; i++){
//			im.setColor(colors[clusterInds[i]]);
//			im.drawOval((int)(expandFac*(MagPixX[i]-rect.x)+offX), (int)(expandFac*(MagPixY[i]-rect.y)+offY), 8, 8);
//		}
		
//		FloatPolygon floatMidline = midline.getFloatPolygon();
//		
//		im.setColor(Color.YELLOW);
//		if (midline!=null){
//			for (int i=0; i<midline.getNCoordinates(); i++){
//				int dotX = offX + (int)(expandFac*(floatMidline.xpoints[i]));//(int)((floatMidline.xpoints[i]+offX)*expandFac);
//				int dotY = offY + (int)(expandFac*(floatMidline.ypoints[i]));
////				im.drawOval(dotX-8, dotY-8, 16, 16);
//				im.drawOval(dotX-4, dotY-4, 8, 8);
//			}
//		}
		
		
		im.setColor(Color.YELLOW);
		if (bbOld!=null){
			for (int i=0; i<bbOld.npoints; i++){
				int dotX = offX + (int)(expandFac*(bbOld.xpoints[i]-rect.x));
				int dotY = offY + (int)(expandFac*(bbOld.ypoints[i]-rect.y));
//				im.drawOval(dotX-8, dotY-8, 16, 16);
				im.drawOval(dotX-4, dotY-4, 8, 8);
			}
		}
		
		
		
//		
		
		im.setColor(Color.BLUE);
		for (int i=0; i<(cont.size()-1); i++){
			im.drawLine(expandFac*cont.get(i).x+offX, expandFac*cont.get(i).y+offY, expandFac*cont.get(i+1).x+offX, expandFac*cont.get(i+1).y+offY);
		}
		im.drawLine(expandFac*cont.get(cont.size()-1).x+offX, expandFac*cont.get(cont.size()-1).y+offY, expandFac*cont.get(0).x+offX, expandFac*cont.get(0).y+offY);
		
		
//		int dotX = offX + (int)(expandFac*(floatMidline.xpoints[i]-rect.x));//(int)((floatMidline.xpoints[i]+offX)*expandFac);
//		int dotY = offX + (int)(expandFac*(floatMidline.ypoints[i]-rect.y));
		
		
		
		im.setColor(Color.RED);
		if (head!=null){
			im.drawOval((int)expandFac*head.x+offX, (int)expandFac*head.y+offY, 5, 5);
		}
		im.setColor(Color.GREEN);
		if (tail!=null){
			im.drawOval((int)expandFac*tail.x+offX, (int)expandFac*tail.y+offY, 5, 5);
		}
		
		
		//DRAW THE FORCES
//		im.setColor(Color.MAGENTA);
		
		
		
		Color[] colors = {Color.WHITE, Color.MAGENTA,Color.GREEN, Color.CYAN, Color.RED};
		for(int f=0; f<bf.Forces.size(); f++){
//		int f=4;
			
			im.setColor(colors[f]);
			
			FloatPolygon targetPts = bf.Forces.get(f).getTargetPoints(frameNum-bf.BTPs.firstElement().frameNum, bf.BTPs);
			
			if (targetPts!=null){
				for (int i=0; i<targetPts.npoints; i++){
					
					int x1 = offX + (int)(expandFac*(bbOld.xpoints[i]-rect.x));
					int y1 = offY + (int)(expandFac*(bbOld.ypoints[i]-rect.y));
					int x2 = (int)(expandFac*(targetPts.xpoints[i]-rect.x)+offX);
					int y2 = (int)(expandFac*(targetPts.ypoints[i]-rect.y)+offY);
					
					im.drawLine(x1, y1, x2, y2);
//					im.drawDot((int)(expandFac*(targetPts.xpoints[i]-rect.x)+offX), (int)(expandFac*(targetPts.ypoints[i]-rect.y)+offY));
					
				}
			}
			
		}
		
		im.setColor(Color.PINK);
		if (bbNew!=null){
			for (int i=0; i<bbNew.npoints; i++){
				int dotX = offX + (int)(expandFac*(bbNew.xpoints[i]-rect.x));
				int dotY = offY + (int)(expandFac*(bbNew.ypoints[i]-rect.y));
//				im.drawOval(dotX-8, dotY-8, 16, 16);
				im.drawOval(dotX-4, dotY-4, 8, 8);
			}
		}
		
		
//		im.setColor(Color.CYAN);
//		im.setColor(Color.MAGENTA);
//		im.setColor(Color.ORANGE);
//		im.setColor(Color.WHITE);
		
		
//		
		return im;
	}
	
	private int getPlotXCoord(float xCoord, int offX, int expandFac){
		
		return (int)(offX + expandFac*(xCoord-rect.x));
	}
	
	private int getPlotYCoord(float yCoord, int offY, int expandFac){
		
		return (int)(offY + expandFac*(yCoord-rect.x));
	}
	
	
}
