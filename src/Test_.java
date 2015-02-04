import java.awt.BorderLayout;
import java.awt.Color;
//import java.awt.Dimension;
//import java.awt.LayoutManager;
//import java.awt.Graphics;
import java.awt.Rectangle;
//import java.awt.image.BufferedImage;










//import javax.swing.JFrame;
//import javax.swing.JPanel;

import ij.ImagePlus;
//import ij.gui.GenericDialog;
import ij.gui.ImageWindow;
import ij.gui.Wand;
//import ij.process.ImageProcessor;
import ij.plugin.PlugIn;
import ij.text.TextPanel;
//import ij.text.TextWindow;
//import ij.process.ImageProcessor;


public class Test_  implements PlugIn {//extends JFrame

	
	
	
	
	/**
	 * 
	 */
//	private static final long serialVersionUID = 1L;
	
	

	public static void main(String[] args) {
		
//		Test_ frame = new Test_();
//		
//		frame.setTitle("Test frame!");
//		frame.setSize(1000,1000);
//		frame.setLocationRelativeTo(null);
//		frame.setVisible(true);
		
//		image.getProcessor();
//		ImageWindow imwin = new ImageWindow(image);
//		image.show();
		
		
		/////////////
		// Generate some content
		/////////////
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
	//	image.getProcessor().drawLine(1, 1, 5, 5);
		
		//Get and draw the contour
		Wand wand = new Wand(image.getProcessor());
		wand.autoOutline(startX, startY);
		
		
		String s = "Line 1 \n Line 2 \n Line 3";
		TextPanel txtPanel = new TextPanel("Test text panel");//("Test window", s, 500, 500);
		txtPanel.append(s);
		
		ImageWindow imWin = new ImageWindow(image);
		imWin.setLayout(new BorderLayout());
		imWin.add(txtPanel, BorderLayout.EAST);
		imWin.setSize(800, 800);
		imWin.pack();
		
		
		
		
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
	
	
	public Test_(){
		
//		/////////////
//		// Generate some content
//		/////////////
//		ImagePlus rawIm = new ImagePlus("C:/Users/Natalie/Downloads/Gr21a(3)@Chrimson(2)_BLUE_LIGHT_RANDOM_WALK_S2_112Hz_201406121247-1.jpg");
//		
//		//Get a maggot
//		int bX = 977;
//		int bY = 887;
//		int w = 10;
//		int h = 18;
//		Rectangle rect = new Rectangle(bX, bY, w, h);
//		rawIm.setRoi(rect);
//		ImagePlus image = new ImagePlus("croppedIm",rawIm.getProcessor().crop());
//		
//		//Draw a dot in the middle
//		int x = 982-bX;
//		int y = 897-bY;
//		
//		int startX = 978-bX;
//		int startY = 887-bY;
//		
//		image.setColor(Color.WHITE);
//		
//		image.getProcessor().drawDot(x, y);
////		image.getProcessor().drawLine(1, 1, 5, 5);
//		
//		//Get and draw the contour
//		Wand wand = new Wand(image.getProcessor());
//		wand.autoOutline(startX, startY);
		
		/////////////
		// Do the GUI stuff
		/////////////
		
		//Make an ImageJ text Panel 
		String s = "Line 1 \n Line 2 \n Line 3";
		TextPanel txtPanel = new TextPanel("Test text panel");//("Test window", s, 500, 500);
		txtPanel.append(s);
		
		//Add the Panel(s) to the Frame
//		setLayout(new BorderLayout());
//		add(txtPanel, BorderLayout.CENTER);
		
//		JPanel jpButtons = new JPanel();
		
//		jpButtons.
		
		
		
	}
	
	
	
	@Override
	public void run(String arg0) {
		// TODO Auto-generated method stub
		
		main(null);
	}

}
