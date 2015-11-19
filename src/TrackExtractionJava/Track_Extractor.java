package TrackExtractionJava;

import java.awt.Dimension;

import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.JTextField;

import ij.IJ;
import ij.ImageJ;
import ij.ImageStack;
import ij.WindowManager;
import ij.plugin.PlugIn;
import ij.text.TextWindow;


public class Track_Extractor implements PlugIn{
	
	// Set extraction parameters EXTRACTIONPARAMETERS
	ExtractionParameters ep; 
	
	// Load the mmfs into an imagestack
	ImageStack IS;
	
	// Build the tracks TRACKBUILDER
	TrackBuilder tb;
	//ep= new ExtractionParameters()
	ExperimentFrame ef;
	
	public void run(String arg) {
				
		
		ExtractorFrame ef = new ExtractorFrame();
		ef.run(null);
		
		/*
		IJ.showStatus("Getting stack");
		
		IS = getStack();
		if (IS == null) {
			IJ.showMessage("Null ImagePlus");
			return;
		} 

		
		IJ.showStatus("Setting up TrackBuiling");
		ep= new ExtractionParameters();
		IJ.showStatus("Building Tracks");
		
		// 
		if (ep.trackPointType>=2){
			tb = new MaggotTrackBuilder(IS, ep);
		} else {
			tb = new TrackBuilder(IS, ep);
		}
		
		try {
			
			
			tb.run();
			
			
			//////
			IJ.showStatus("Converting to Experiment");
			Experiment exp = tb.toExperiment();
			
			ef = new ExperimentFrame(exp);
			ef.run(null);
			
			new TextWindow("Communicator Output", tb.comm.outString, 500, 500);
		}
		catch (Exception e) {
			
			StackTraceElement[] tr = e.getStackTrace();
			String s = e.toString()+"\n";
			for (int i=0; i<tr.length; i++){
				s += tr[i].toString()+"\n";
			}
			
			if (tb!=null){
				tb.comm.message(s, VerbLevel.verb_error);
				new TextWindow("Communicator Output: Error", tb.comm.outString, 500, 500);
			}
		}
		
		*/
	}
	
	
	public ImageStack getStack(){
		return WindowManager.getCurrentWindow().getImagePlus().getImageStack();
	}
	
	public static void main(String[] args) {
        // set the plugins.dir property to make the plugin appear in the Plugins menu
//        Class<?> clazz = Track_Extractor.class;
//        String url = clazz.getResource("/" + clazz.getName().replace('.', '/') + ".class").toString();
//        String pluginsDir = url.substring(5, url.length() - clazz.getName().length() - 6);
//        System.setProperty("plugins.dir", pluginsDir);

        // start ImageJ
//        new ImageJ();


        // run the plugin
        //IJ.runPlugIn(clazz.getName(), "");
		Track_Extractor te = new Track_Extractor();
		te.run("");
}
	
}



class ExtractorFrame extends JFrame{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	JTabbedPane mainPanel;
	int tabPlacement = JTabbedPane.TOP;
	Dimension panelSize = new Dimension(500,500);
	String panelName = "Experiment Processor"; 
	
	InputPanel input;
	ParamPanel params;
	OutputPanel output;
	
	public void run(String args){

		buildFrame();
		
		showFrame();
		
	}
	
	private void buildFrame(){
		
		//Build components
		input = new InputPanel();
		params = new ParamPanel();
		output = new OutputPanel();
		
		//Add them to the MainPanel
		mainPanel = new JTabbedPane(tabPlacement);
		mainPanel.add("Select input...", input);
		mainPanel.add("Set Parameters...", params);
		mainPanel.add("Select output...", output);
		
		//Add mainPanel to frame
		add(mainPanel);
		
	}
	
	
	private void showFrame(){
		setSize(panelSize);
		setTitle(panelName);
		setVisible(true);
	}
	
	
}


class InputPanel extends JPanel{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	//Internal Elements
	JTextField txFld;
	JButton flChButton;
	JFileChooser flCh;
	
	Experiment ex;
	JPanel descPanel;
	JTextArea desc;
	
	//External Elements
	JTextField outputTxFld;
	
	
	//Constructors
	public InputPanel(){
		
		buildPanel();
	}
	
	//output Setter
	
	//Experiment Getter
	
	
	private void buildPanel(){
		
		buildComponents();
		
		//put components together
		
	}
	
	
	private void buildComponents(){
		
	}
	
	
	
}

class ParamPanel extends JPanel{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	ProcessingParameters procParams;
	ExtractionParameters extrParams;
	FittingParameters fitParams;
	
	
	
	
	
}

class OutputPanel extends JPanel{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	

	JTextField txFld;
	JButton flChButton;
	JFileChooser flCh;
	
	
	
	
}


class ProgressFrame extends JFrame{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
}