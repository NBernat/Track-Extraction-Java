package TrackExtractionJava;

import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.Serializable;
import java.lang.reflect.Field;
import java.text.NumberFormat;

import javax.swing.JCheckBox;
import javax.swing.JFormattedTextField;
import javax.swing.JLabel;
import javax.swing.JPanel;


public class ExtractionParameters implements Serializable{ 
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	

	public boolean subset = false;
	public int startFrame = 1;
	public int endFrame = 300;
	
	int GCInterval = 500;

	/**
	 * Max number of points stored in a TrackMatch object
	 */
	int numPtsInTrackMatch = 3;
	
	/**
	 * Determines how much collision handling should be done
	 * <p>
	 * 0: When tracks collide, just end them
	 * <p>
	 * 1: Match collisions to nearby points, then try to split image into multiple points
	 * <p>
	 * 2: Level 2, then comb through collisions after tracks are made 
	 */
	public int collisionLevel = 1;
	
	/**
	 *  Distance in pixels which indicates a collision
	 */
	//double collisionDist;
	
	/**
	 * Max distance in pixels for a point match 
	 */
	public double maxMatchDist = 20;
	
	/**
	 * How many frames to move forward when extracting points
	 */
	int increment = 1;
	
	
	///////////////////////////
	// Sample data display
	///////////////////////////
	
	/**
	 * 0=nothing
	 * 1 = play track specified by sampleInd
	 * 2 = 1, plus show ResultsTable & thresholded image of frame specified by sampleInd
	 */
	int showSampleData =0; 
	int sampleInd = 10;
	int trackWindowHeight = 50;
	int trackWindowWidth = 50;
	int trackZoomFac = 10;
	int[] matchSpill = {};//{234,251,356,367};
	boolean flagAbnormalMatches = false;
	boolean dispTrackInfo = false;
	
	///////////////////////////
	// Background Parameters
	///////////////////////////
	/**
	 * 
	 */
	int nBackgroundFrames = 5;
	/**
	 * 
	 */
    int resampleInterval = 100;
    /**
     * 
     */
    double blurSigma = 1;
	/**
	 * 
	 */
    double blurAccuracy = 0.02;//<0.02, lower=better but longer execution
    
    /**
     * Whether or not to globally threshold the image
     */
    boolean useGlobalThresh = true;
    /**
     * The global threshold value
     */
    public int globalThreshValue = 25;
    
    public boolean excludeEdges = true; 
    public boolean clipBoundaries = true;
    public int boundarySize = 10; //Size (in pixels) of the boundary that should be clipped when CLIPBOUNDARIES is set to true 
    /**
     * The fraction from which the area of a maggot can deviate from the target area when splitting a point
     * <p>
     * Between 0 and 1, inclusive 
     */
    double fracChangeForSplitting = .5;
    /**
     * the method for splitting points:
     * 1=extracting points from rethresholded im
     * 2=distributing maggot points 
     */
    int pointSplittingMethod = 1;
    /**
     *  The area change which indicates that a collision has ended, expressed as a fraction of the previous area
     */
    double maxAreaFracForCollisionEnd = .6;
    /**
     * The maximum angle that a contour point can be to be considered for H/T assignment
     */
    double maxContourAngle = Math.PI/2.0;
    /**
     * Number of midline coordinates to extract
     */
    public int numMidCoords = 11;
    
    /**
     * Add-on for the string which specifies the center coordinates of the points
     * <p>
     * Center of Mass=>"M"
     * <p>
     * Centroid=>""
     */
    String centerMethod = "";
    
    /**
     * Minimum blob area
     */
    public double minArea = 20;
    /**
     * Maximum blob area
     */
    public double maxArea = 1000;
    /**
     * Minimum blob area used when rethresholding a maggot
     */
    double minSubMaggotArea = 10;
    
    /**
     * Width (in px) of padding around image used in TrackPoint.getIm()  
     */
    int roiPadding = 0;
    
    /**
     * PLAYMOVIE NOT YET SUPPORTED FOR SAVED FILES WHEN TRACKPOINTTYPE=0
     */
    int trackPointType = 2;
    
    extrPanel epPanel;
    
    /**
	 * Creates a set of Extraction Parameters, with the proper start frame
	 */
	public ExtractionParameters(){
		if (!subset){
			startFrame = 1;
		}
	}
    
	public boolean properPointSize(double area){
		return (area>=minArea && area<=maxArea);
	}
	
	public boolean toDisk(String outputName){
		
		
		try{
			
			File f = new File(outputName);
			
			FileWriter fw = new FileWriter(f.getAbsoluteFile());
			BufferedWriter bw = new BufferedWriter(fw);
			System.out.println("Writing params to disk...");
			Field[] flds = ExtractionParameters.class.getDeclaredFields();//getFields();
			System.out.println(flds.length+" fields");
			for (Field fld: flds){
				bw.write(fld.getName()+":"+fld.get(this)+"\n");
			}
			
			bw.close();
			System.out.println("...finished writing params to disk");
			
		} catch (Exception e){
			System.out.println("Error saving to disk"+e.getMessage());
		}
		
		return true;
	}
	

	
	public extrPanel getPanel(){
		if (epPanel==null){
			epPanel = extrPanel.makePanel(this);
		}
		return epPanel;
	}
	
	public static void main(String[] args) {
		
		ExtractionParameters ep = new ExtractionParameters();
		System.out.println("Saving params to disk...");
		ep.toDisk("C:\\Users\\Natalie\\Documents\\test.txt");
		System.out.println("...done saving params to disk");
	}
	
}


class extrPanel extends JPanel {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	ExtractionParameters exPs;
	
	//Components:
	
	JPanel LeftPanel;
	JPanel RightPanel;
	
	JPanel RangePanel;
	
	JCheckBox subsetBox;
	String subsetName = "Extract from subset of frames";
	
	JPanel frameRangePanel;
	
	JFormattedTextField startFrameField;
	JLabel startFrameLabel;
	JPanel startFramePanel;
	String startFrameName = "Start frame(<=1)";
	
	JFormattedTextField endFrameField;
	JLabel endFrameLabel;
	JPanel endFramePanel;
	String endFrameName = "End frame";
	
	JCheckBox exclEdgeBox;
	String exclEdgeName = "Exclude edge points";
	
	JFormattedTextField glbThreshField;
	JLabel glbThreshLabel;
	JPanel glbThreshPanel;
	String glbThreshName = "Global threshold";
	
	JFormattedTextField maxDistField;
	JLabel maxDistLabel;
	JPanel maxDistPanel;
	String maxDistName = "Max dist between track points (pixels)";

	JFormattedTextField minAreaField;
	JLabel minAreaLabel;
	JPanel minAreaPanel;
	String minAreaName = "Min area of track point (pixels)";

	JFormattedTextField maxAreaField;
	JLabel maxAreaLabel;
	JPanel maxAreaPanel;
	String maxAreaName = "Max area of track point (pixels)";
	
	
	public extrPanel(ExtractionParameters ep){
		if (ep==null){
			exPs = new ExtractionParameters();
		} else {
			exPs = ep;
		}
		buildPanel();
	}
	
	private void buildPanel(){
		//build components
		buildComponents();
		
		//add components to panel
		LeftPanel = new JPanel();
		LeftPanel.setLayout(new GridLayout(3,1));
		LeftPanel.add(subsetBox);
		LeftPanel.add(RangePanel);
		LeftPanel.add(exclEdgeBox);
		
		RightPanel = new JPanel();
		RightPanel.setLayout(new GridLayout(4, 1));
		RightPanel.add(glbThreshPanel);
		RightPanel.add(maxDistPanel);
		RightPanel.add(minAreaPanel);
		RightPanel.add(maxAreaPanel);
		
		add(LeftPanel);
		add(RightPanel);
		
	}
	
	public void buildComponents(){
		//TODO
		
		subsetBox = new  JCheckBox(subsetName, exPs.subset);
		subsetBox.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				exPs.subset = subsetBox.isSelected();
			}
		});
		
		startFrameField = new JFormattedTextField(NumberFormat.getIntegerInstance());
		startFrameField.setValue(exPs.startFrame); 
		startFrameField.addPropertyChangeListener( new PropertyChangeListener() {
			
			@Override
			public void propertyChange(PropertyChangeEvent evt) {
				Integer sf = ((Number)startFrameField.getValue()).intValue();
				exPs.startFrame = sf.intValue();
			}
		}); 
		startFrameLabel = new JLabel(startFrameName);
		startFramePanel = new JPanel(new BorderLayout());
		startFramePanel.add(startFrameField, BorderLayout.WEST);
		startFramePanel.add(startFrameLabel);
		
		endFrameField = new JFormattedTextField(NumberFormat.getIntegerInstance());
		endFrameField.setValue(exPs.endFrame); 
		endFrameField.addPropertyChangeListener( new PropertyChangeListener() {
			
			@Override
			public void propertyChange(PropertyChangeEvent evt) {
				Integer ef = ((Number)endFrameField.getValue()).intValue();
				exPs.endFrame = ef.intValue();
			}
		});
		endFrameLabel = new JLabel(endFrameName);
		endFramePanel = new JPanel(new BorderLayout());
		endFramePanel.add(endFrameField, BorderLayout.WEST);
		endFramePanel.add(endFrameLabel);
		
		RangePanel = new JPanel();
		RangePanel.setLayout(new GridLayout(2,1));
		RangePanel.add(startFramePanel);
		RangePanel.add(endFramePanel);
		
		
		exclEdgeBox = new  JCheckBox(exclEdgeName, exPs.excludeEdges);
		exclEdgeBox.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				exPs.excludeEdges = exclEdgeBox.isSelected();
				
			}
		});
		
		glbThreshField = new JFormattedTextField(NumberFormat.getIntegerInstance());
		glbThreshField.setValue(exPs.globalThreshValue); 
		glbThreshField.addPropertyChangeListener( new PropertyChangeListener() {
			
			@Override
			public void propertyChange(PropertyChangeEvent evt) {
				Integer gt = ((Number)glbThreshField.getValue()).intValue();
				exPs.globalThreshValue = gt.intValue();
			}
		});
		glbThreshLabel = new JLabel(glbThreshName);
		glbThreshPanel = new JPanel(new BorderLayout());
		glbThreshPanel.add(glbThreshField, BorderLayout.WEST);
		glbThreshPanel.add(glbThreshLabel);
		
		maxDistField = new JFormattedTextField(NumberFormat.getIntegerInstance());
		maxDistField.setValue(exPs.maxMatchDist); 
		maxDistField.addPropertyChangeListener( new PropertyChangeListener() {
			
			@Override
			public void propertyChange(PropertyChangeEvent evt) {
				Integer md = ((Number)maxDistField.getValue()).intValue();
				exPs.maxMatchDist = md.intValue();
			}
		});
		maxDistLabel = new JLabel(maxDistName);
		maxDistPanel = new JPanel(new BorderLayout());
		maxDistPanel.add(maxDistField, BorderLayout.WEST);
		maxDistPanel.add(maxDistLabel);
		
		minAreaField = new JFormattedTextField(NumberFormat.getIntegerInstance());
		minAreaField.setValue(exPs.minArea); 
		minAreaField.addPropertyChangeListener( new PropertyChangeListener() {
			
			@Override
			public void propertyChange(PropertyChangeEvent evt) {
				Integer ma = ((Number)minAreaField.getValue()).intValue();
				exPs.minArea = ma.intValue();
			}
		});
		minAreaLabel = new JLabel(minAreaName);
		minAreaPanel = new JPanel(new BorderLayout());
		minAreaPanel.add(minAreaField, BorderLayout.WEST);
		minAreaPanel.add(minAreaLabel);
		
		maxAreaField = new JFormattedTextField(NumberFormat.getIntegerInstance());
		maxAreaField.setValue(exPs.maxArea); 
		maxAreaField.addPropertyChangeListener( new PropertyChangeListener() {
			
			@Override
			public void propertyChange(PropertyChangeEvent evt) {
				Integer ma = ((Number)maxAreaField.getValue()).intValue();
				exPs.maxArea = ma.intValue();
			}
		});
		maxAreaLabel = new JLabel(maxAreaName);
		maxAreaPanel = new JPanel(new BorderLayout());
		maxAreaPanel.add(maxAreaField, BorderLayout.WEST);
		maxAreaPanel.add(maxAreaLabel);
	}
	
	public static extrPanel makePanel(ExtractionParameters ep){
		
		return new extrPanel(ep);
	}
	
	
}