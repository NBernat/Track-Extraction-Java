import java.awt.Color;
//import java.awt.Graphics;
import java.awt.Rectangle;
//import java.awt.image.BufferedImage;

import ij.ImagePlus;
import ij.gui.Wand;
//import ij.process.ImageProcessor;
import ij.plugin.PlugIn;
//import ij.process.ImageProcessor;


public class Test_ implements PlugIn{

	
	
	
	
	public static void main(String[] args) {
		ImagePlus rawIm = new ImagePlus("C:/Users/Natalie/Downloads/Gr21a(3)@Chrimson(2)_BLUE_LIGHT_RANDOM_WALK_S2_112Hz_201406121247-1.jpg");
		
		//Get a maggot
		int bX = 977;
		int bY = 887;
		int w = 10;
		int h = 18;
		Rectangle rect = new Rectangle(bX, bY, w, h);
		rawIm.setRoi(rect);
		ImagePlus image = new ImagePlus("croppedIm",rawIm.getProcessor().crop());
		
		//Draw a dot in the middle
		int x = 982-bX;
		int y = 897-bY;
		
		int startX = 978-bX;
		int startY = 887-bY;
		
		image.setColor(Color.WHITE);
		
		image.getProcessor().drawDot(x, y);
		image.getProcessor().drawLine(1, 1, 5, 5);
		
		//Get and draw the contour
		Wand wand = new Wand(image.getProcessor());
		wand.autoOutline(startX, startY);
		
		
		
		image.getProcessor();
		
		image.show();
		
		
		
		
//		BufferedImage bIm = image.getBufferedImage(); 
//		Graphics g = bIm.getGraphics();
//		g.setColor(Color.red);
//		g.drawOval(x-1, y-1, 2, 2);
//		

//		g.drawPolyline(wand.xpoints, wand.ypoints, wand.npoints);
		
//		ImagePlus retIm = new ImagePlus("Center on  "+image.getTitle(), bIm);
//		retIm.show();
		
//		int newWidth = 100;
//		int newHeight = 100;
//		
//		int centerX = 11;
//		int centerY = 13;
//		
//		BufferedImage newIm = new BufferedImage(newWidth, newHeight, image.getBufferedImage().getType());
//		Graphics g = newIm.getGraphics();
//		g.setColor(Color.BLUE);
//		g.fillRect(0,0,newWidth,newHeight);
//		int offsetX = (newWidth/2)+1-centerX;
//		int offsetY = (newHeight/2)+1-centerY;
//		g.drawImage(image.getBufferedImage(), offsetX, offsetY, null);
//		
//		ImagePlus retIm = new ImagePlus("Padded "+image.getTitle(), newIm);
//		
//		retIm.show();
		
		
//		System.out.println("Old image: Size "+image.getWidth()+"x"+image.getHeight());
//		image.setRoi(886, 522, 1070, 720);
//		ImageProcessor cropIm = image.getProcessor().crop();
//		//cropIm.get
//		System.out.println("Cropped image: Size "+cropIm.getWidth()+"x"+cropIm.getHeight());
//		System.out.println("Old image: Size "+image.getWidth()+"x"+image.getHeight());
		
		
	}

//	public void drawContour(ImageProcessor){
//		
//	}
	
	
	@Override
	public void run(String arg0) {
		// TODO Auto-generated method stub
		
		main(null);
	}

}
