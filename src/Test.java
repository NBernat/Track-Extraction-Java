import java.awt.Color;
import java.awt.Graphics;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;

import ij.ImagePlus;
import ij.process.ImageProcessor;


public class Test {

	public static void main(String[] args) {
		ImagePlus rawIm = new ImagePlus("C:/Users/Natalie/Downloads/Gr21a(3)@Chrimson(2)_BLUE_LIGHT_RANDOM_WALK_S2_112Hz_201406121247-1.jpg");
		Rectangle rect = new Rectangle(1426, 922, 19, 24);
		rawIm.setRoi(rect);
		ImagePlus image = new ImagePlus("croppedIm",rawIm.getProcessor().crop());
		image.show();
		
		int newWidth = 100;
		int newHeight = 100;
		
		int centerX = 11;
		int centerY = 13;
		
		BufferedImage newIm = new BufferedImage(newWidth, newHeight, image.getBufferedImage().getType());
		Graphics g = newIm.getGraphics();
		g.setColor(Color.BLUE);
		g.fillRect(0,0,newWidth,newHeight);
		int offsetX = (newWidth/2)+1-centerX;
		int offsetY = (newHeight/2)+1-centerY;
		g.drawImage(image.getBufferedImage(), offsetX, offsetY, null);
		
		ImagePlus retIm = new ImagePlus("Padded "+image.getTitle(), newIm);
		
		retIm.show();
		
		
//		System.out.println("Old image: Size "+image.getWidth()+"x"+image.getHeight());
//		image.setRoi(886, 522, 1070, 720);
//		ImageProcessor cropIm = image.getProcessor().crop();
//		//cropIm.get
//		System.out.println("Cropped image: Size "+cropIm.getWidth()+"x"+cropIm.getHeight());
//		System.out.println("Old image: Size "+image.getWidth()+"x"+image.getHeight());
		
		
	}

}
