package TrackExtractionJava;

import java.awt.Color;
import ij.gui.Plot;
import ij.gui.PlotWindow;
import ij.plugin.PlugIn;
import ij.text.TextPanel;


public class Test_ implements PlugIn {//extends JFrame

	
	public int fld1;
	public String fld2;
	
	
	/**
	 * 
	 */
//	private static final long serialVersionUID = 1L;
	
	

	public static void main(String[] args) {
		
//		float[] x = new float[20];
//		for(int i=0; i<x.length; i++) x[i]=i;
//		float[] y = new float[20];
//		for(int i=0; i<y.length; i++) y[i]=i;
//		Plot plot = new Plot("The window title", "labels on the x-axis", "labels on the y-axis", x, y);
//		plot.setLimits(0, 20, 0, 40);
//		plot.setColor(Color.RED);
//		plot.draw();
//		float[] y2 = new float[20];
//		for(int i=0; i<y2.length; i++) y2[i]=2*y[i];
//		plot.addPoints(x,y2,Plot.LINE);
//		plot.setColor(Color.yellow);
//		plot.draw();
//		plot.show();
		
		
		// Some data to show
        double[] x = { 1, 3, 4, 5, 6, 7, 8, 9, 11 };
        double[] y = { 20, 5, -2, 3, 10, 12, 8, 3, 0 };
        double[] y2 = { 18, 10, 3, 1, 7, 11, 11, 5, 2 };
        double[] x3 = { 2, 10 };
        double[] y3 = { 13, 3 };

        // Initialize the plot with x/y
        Plot plot = new Plot("Example plot", "x", "y", x, y);

        // make some margin (xMin, xMax, yMin, yMax)
        plot.setLimits(0, 12, -3, 21);

        // Add x/y2 in blue; need to draw the previous data first
        plot.draw();
        plot.setColor(Color.BLUE);
        plot.addPoints(x, y2, Plot.LINE);

        // Add x3/y3 as circles instead of connected lines
        plot.draw();
        plot.setColor(Color.BLACK);
        plot.addPoints(x3, y3, Plot.CIRCLE);

        // Finally show it, but remember the window
        PlotWindow window = plot.show();

        // Wait 5 seconds
        try { Thread.sleep(5000); } catch (InterruptedException e) {}

        // Make a new plot and update the window
        plot = new Plot("Example plot2", "x", "y", x, y2);
        plot.setLimits(0, 12, -3, 21);
        plot.draw();
        plot.setColor(Color.GREEN);
        plot.addPoints(x, y, Plot.CROSS);
        plot.draw();
        plot.setColor(Color.RED);
        plot.addPoints(x3, y3, Plot.LINE);
        window.drawPlot(plot);
		
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
		/**
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
		**/
		
		
		
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
	
	
	public Test_(int i, String s){
		
		fld1 = i;
		fld2 = s;
		
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
//		String s = "Line 1 \n Line 2 \n Line 3";
//		TextPanel txtPanel = new TextPanel("Test text panel");//("Test window", s, 500, 500);
//		txtPanel.append(s);
		
		//Add the Panel(s) to the Frame
//		setLayout(new BorderLayout());
//		add(txtPanel, BorderLayout.CENTER);
		
//		JPanel jpButtons = new JPanel();
		
//		jpButtons.
		
		/*
		 * COMM TEST:
		 */
//		VerbLevel verb1 = VerbLevel.verb_off;
//		VerbLevel verb2 = VerbLevel.verb_debug;
//		
//		if (verb1.compareTo(verb2)>0){
//			System.out.println("off greater than debug");
//		} else if (verb1.compareTo(verb2)<0){
//			System.out.println("off less than debug");
//		}
//		
//		Communicator cm = new Communicator();
//		cm.message("this is a message!", VerbLevel.verb_error);
//		cm.message("this is another message!",VerbLevel.verb_verbose);
//
//		int x = 1;
//		int y = ++x;
//		System.out.println("y: "+y);
//		System.out.println("x: "+x);
		
		
	}
	
	public String str(){
		String ret = "";
		for (int i=0; i<fld1; i++){
			ret+=fld2;
		}
		return ret;
	}
	
	@Override
	public void run(String arg0) {
		// TODO Auto-generated method stub
		
		main(null);
	}

}
