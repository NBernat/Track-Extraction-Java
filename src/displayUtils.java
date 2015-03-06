import ij.gui.PolygonRoi;
import ij.process.FloatPolygon;
import ij.process.ImageProcessor;

import java.awt.Color;
import java.awt.Rectangle;
import java.util.Vector;


public class displayUtils {

	
	
	
	//Drawing methods 
	
	public static void drawClusters(ImageProcessor im, int numPix, float[] MagPixX, float[] MagPixY, int[] clusterInds, int expandFac, int offX, int offY, Rectangle rect){
		
		Color[] colors = {Color.WHITE, Color.PINK, Color.MAGENTA, Color.RED, Color.ORANGE, Color.YELLOW, Color.GREEN, Color.CYAN, Color.BLUE};
		
		for (int i=0; i<numPix; i++){
			im.setColor(colors[(clusterInds[i]+colors.length)%colors.length]);
			int circWid = 2;
			im.drawOval((int)(expandFac*(MagPixX[i]-rect.x)+offX)-(circWid/2), (int)(expandFac*(MagPixY[i]-rect.y)+offY)-(circWid/2), circWid, circWid);
		}
	}
	
//	public static void drawForces(ImageProcessor im, FloatPolygon bbNew, Vector<Force> forces, Vector<BackboneTrackPoint> BTPs, int frameNum, int expandFac, int offX, int offY, Rectangle rect){
//		Color[] colors = {Color.WHITE, Color.MAGENTA,Color.GREEN, Color.CYAN, Color.RED};
//		for(int f=0; f<forces.size(); f++){
//			
//			im.setColor(colors[f]);
//			
//			FloatPolygon targetPts = forces.get(f).getTargetPoints(frameNum-BTPs.firstElement().frameNum, BTPs);
//			
//			if (targetPts!=null){
//				for (int i=0; i<targetPts.npoints; i++){
//					
//					int x1 = offX + (int)(expandFac*(bbNew.xpoints[i]-rect.x));
//					int y1 = offY + (int)(expandFac*(bbNew.ypoints[i]-rect.y));
//					int x2 = (int)(expandFac*(targetPts.xpoints[i]-rect.x)+offX);
//					int y2 = (int)(expandFac*(targetPts.ypoints[i]-rect.y)+offY);
//					
//					im.drawLine(x1, y1, x2, y2);
////					im.drawDot((int)(expandFac*(targetPts.xpoints[i]-rect.x)+offX), (int)(expandFac*(targetPts.ypoints[i]-rect.y)+offY));
//					
//				}
//			}
//			
//		}
//	}
	
	public static void drawContour(ImageProcessor im, Vector<ContourPoint> cont, int expandFac, int offX, int offY, Color color){
		im.setColor(color);
		for (int i=0; i<(cont.size()-1); i++){
			im.drawLine(expandFac*cont.get(i).x+offX, expandFac*cont.get(i).y+offY, expandFac*cont.get(i+1).x+offX, expandFac*cont.get(i+1).y+offY);
		}
		im.drawLine(expandFac*cont.get(cont.size()-1).x+offX, expandFac*cont.get(cont.size()-1).y+offY, expandFac*cont.get(0).x+offX, expandFac*cont.get(0).y+offY);
		
	}
	
	public static void drawPoint(ImageProcessor im, ContourPoint point, int expandFac, int offX, int offY, Color color){
		im.setColor(color);
		if (point!=null){
			im.drawOval((int)expandFac*point.x+offX, (int)expandFac*point.y+offY, 5, 5);
		}
	}
	
	
	public static void drawMidline(ImageProcessor im, PolygonRoi midline, int offX, int offY, int expandFac, Color color){
		im.setColor(color);
		if (midline!=null){
			FloatPolygon floatMidline = midline.getFloatPolygon();
			for (int i=0; i<midline.getNCoordinates(); i++){
				int dotX = offX + (int)(expandFac*(floatMidline.xpoints[i]));
				int dotY = offY + (int)(expandFac*(floatMidline.ypoints[i]));
				im.drawDot(dotX, dotY);
			}
		}
	}
	
	
	public static void drawBBInit(ImageProcessor im, FloatPolygon bbInit, int offX, int offY, Rectangle rect, int expandFac, Color color){
		im.setColor(color);
		if (bbInit!=null){
			for (int i=0; i<bbInit.npoints; i++){
				int dotX = offX + (int)(expandFac*(bbInit.xpoints[i]-rect.x));
				int dotY = offY + (int)(expandFac*(bbInit.ypoints[i]-rect.y));
				int circWid = 2;
				im.drawOval(dotX-(circWid/2), dotY-(circWid/2), circWid, circWid);
//				im.drawDot(dotX, dotY);
			}
		}
	}
	
	public static void drawBackbone(ImageProcessor im, FloatPolygon bbNew, int expandFac, int offX, int offY, Rectangle rect, Color color){
		im.setColor(color);
		if (bbNew!=null){
			for (int i=0; i<bbNew.npoints; i++){
				int dotX = offX + (int)(expandFac*(bbNew.xpoints[i]-rect.x));
				int dotY = offY + (int)(expandFac*(bbNew.ypoints[i]-rect.y));
				int circWid = 8;
				im.drawOval(dotX-(circWid/2), dotY-(circWid/2), circWid, circWid);
			}
		}
	}
	
}
