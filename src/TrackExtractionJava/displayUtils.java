package TrackExtractionJava;

import ij.gui.PolygonRoi;
import ij.process.FloatPolygon;
import ij.process.ImageProcessor;

import java.awt.Color;
import java.awt.Rectangle;
import java.util.Vector;


public class displayUtils {

	
	public static void drawSegLines(ImageProcessor im, PolygonRoi seg1, PolygonRoi seg2, int expandFac, int offX, int offY, Color color){
		im.setColor(color);
		for (int i=0; i<seg1.getNCoordinates(); i++){
			int dotX1 = offX + (int)(expandFac*(seg1.getFloatPolygon().xpoints[i]));
			int dotY1 = offY + (int)(expandFac*(seg1.getFloatPolygon().ypoints[i]));
			int dotX2 = offX + (int)(expandFac*(seg2.getFloatPolygon().xpoints[i]));
			int dotY2 = offY + (int)(expandFac*(seg2.getFloatPolygon().ypoints[i]));
			im.drawLine(dotX1, dotY1, dotX2, dotY2);
		}
		
	}
	
	//Drawing methods 
	
	public static void drawClusters(ImageProcessor im, int numPix, float[] MagPixX, float[] MagPixY, int[] clusterInds, int expandFac, int offX, int offY, Rectangle rect){
		
		Color[] colors = {Color.WHITE, Color.PINK, Color.MAGENTA, Color.RED, Color.ORANGE, Color.YELLOW, Color.GREEN, Color.CYAN, Color.BLUE};
		
		for (int i=0; i<numPix; i++){
			im.setColor(colors[(clusterInds[i]+colors.length)%colors.length]);
			int circWid = 2;
			im.drawOval((int)(expandFac*(MagPixX[i]-rect.x)+offX)-(circWid/2), (int)(expandFac*(MagPixY[i]-rect.y)+offY)-(circWid/2), circWid, circWid);
		}
	}
	
	
	public static void drawContour(ImageProcessor im, int[] cX, int[] cY, int expandFac, int offX, int offY, Color color){
		im.setColor(color);
		for (int i=0; i<(cX.length-1); i++){
			im.drawLine(expandFac*cX[i]+offX, expandFac*cY[i]+offY, expandFac*cX[i+1]+offX, expandFac*cY[i+1]+offY);
		}
		im.drawLine(expandFac*cX[cX.length-1]+offX, expandFac*cY[cX.length-1]+offY, expandFac*cX[0]+offX, expandFac*cY[0]+offY);
		
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
//				im.drawDot(dotX, dotY);
				int circWid = 4;
				im.drawOval(dotX-(circWid/2), dotY-(circWid/2), circWid, circWid);

				if (i==0){
					im.drawOval(dotX-(circWid), dotY-(circWid), circWid*2, circWid*2);
				}
			}
		} else {
			int size = 20;
//			im.drawOval(0, 0, size, size);
			
			im.drawLine(0, 0, size, size);
			im.drawLine(size, 0, 0, size);
		}
	}
	
	
	public static void drawMidlines(ImageProcessor im, Vector<PolygonRoi> midlines, int offX, int offY, int expandFac, Vector<Color> colors){
		
		for (int j=0; j<midlines.size(); j++){
			im.setColor(colors.get(j));
			FloatPolygon floatMidline = midlines.get(j).getFloatPolygon();
			for (int i=0; i<midlines.get(j).getNCoordinates(); i++){
				
				int dotX = offX + (int)(expandFac*(floatMidline.xpoints[i]));
				int dotY = offY + (int)(expandFac*(floatMidline.ypoints[i]));
				im.drawDot(dotX, dotY);
				
				int circWid = 2;
				if (i==0){
					im.drawOval(dotX-(circWid), dotY-(circWid), circWid*2, circWid*2);
				}
			}
		}
	}
	
	
	public static void drawBBInit(ImageProcessor im, FloatPolygon bbInit, int offX, int offY, Rectangle rect, int expandFac, Color color){
		im.setColor(color);
		if (bbInit!=null && bbInit.npoints>0){
			for (int i=0; i<bbInit.npoints; i++){
				int x = (int)(expandFac*(bbInit.xpoints[i]-rect.x));
				int y = (int)(expandFac*(bbInit.ypoints[i]-rect.y));
				
				if (!(x==0 && y==0)) {
					int dotX = offX + x;
					int dotY = offY + y;
					int circWid = 6;
					im.drawOval(dotX-(circWid/2), dotY-(circWid/2), circWid, circWid);
	//				im.drawDot(dotX, dotY);
					

					if (i==0){
						im.drawOval(dotX-(circWid), dotY-(circWid), circWid*2, circWid*2);
					}
				} else {
					im.setColor(Color.RED);
					im.drawOval(0, 0, 5, 5);
					im.setColor(color);
				}
				
			}
		} else {
			int ovalSize = 10;
			im.drawOval(10-ovalSize/2, 10-ovalSize/2, ovalSize, ovalSize);
		}
		
	}
	
	public static void drawBackbone(ImageProcessor im, FloatPolygon bbNew, int expandFac, int offX, int offY, Rectangle rect, Color color){
		im.setColor(color);
		if (bbNew!=null && bbNew.npoints>0){
			for (int i=0; i<bbNew.npoints; i++){
				int x = (int)(expandFac*(bbNew.xpoints[i]-rect.x));
				int y = (int)(expandFac*(bbNew.ypoints[i]-rect.y));
				
				if(!(x==0 && y==0)){
					int dotX = offX + x;
					int dotY = offY + y;
					int circWid = 8;
					im.drawOval(dotX-(circWid/2), dotY-(circWid/2), circWid, circWid);
					
					if (i==0){
						im.drawOval(dotX-(circWid), dotY-(circWid), circWid*2, circWid*2);
					}
					
				} else {
					im.setColor(Color.RED);
					im.drawOval(0, 0, 5, 5);
					im.setColor(color);
				}
			}
		}else {
			int ovalSize = 20;
			im.drawOval(0, 0, ovalSize, ovalSize);
		}
	}
	
	
	public static void drawTargets(ImageProcessor im, Vector<FloatPolygon> targetBackbones, int expandFac, int offX, int offY, Rectangle rect){
		
		if(targetBackbones==null){
			return;
		}
		
		Vector<Color> colors = getColorList();
		
		for (int j=0; j<targetBackbones.size(); j++){
			Color color = colors.get(j%colors.size());
			FloatPolygon bbNew = targetBackbones.get(j);
			
			im.setColor(color);
			if (bbNew!=null && bbNew.npoints>0){
				for (int i=0; i<bbNew.npoints; i++){
					int x = (int)(expandFac*(bbNew.xpoints[i]-rect.x));
					int y = (int)(expandFac*(bbNew.ypoints[i]-rect.y));
					
					if(!(x==0 && y==0)){
						int dotX = offX + x;
						int dotY = offY + y;
						int circWid = 4;
						im.drawOval(dotX-(circWid/2), dotY-(circWid/2), circWid, circWid);
						
//						if (i==0){
//							im.drawOval(dotX-(circWid), dotY-(circWid), circWid*2, circWid*2);
//						}
						
					} else {
						im.setColor(Color.RED);
						im.drawOval(1, 1, 6, 6);
						im.setColor(color);
					}
				}
			}else {
				int ovalSize = 20;
				im.drawOval(0, 0, ovalSize, ovalSize);
			}
		}
		
	}
	
	
	private static Vector<Color> getColorList(){
		
		Vector<Color> colors = new Vector<Color>();
		colors.add(Color.RED);//Image
		colors.add(Color.BLUE);//Spine-Length
		colors.add(Color.GREEN);//Spine-Smooth
		colors.add(Color.MAGENTA);//Time-Length 
		colors.add(Color.YELLOW);//Time-Smooth
		
		return colors;
	}
	
	
}
