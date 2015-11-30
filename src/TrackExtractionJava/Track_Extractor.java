package TrackExtractionJava;

import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.BoxLayout;
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
	
	JPanel mainPanel;
	//int tabPlacement = JTabbedPane.TOP;
	Dimension panelSize = new Dimension(500,500);
	String panelName = "Experiment Processor"; 
	
	InputPanel input;
	ParamPanel params;
	OutputPanel output;
	JButton runButton;
	
	public void run(String args){

		buildFrame();
		
		showFrame();
		
	}
	
	private void buildFrame(){
		
		//Build components
		input = new InputPanel();
		output = new OutputPanel();
		params = new ParamPanel();
		input.outputTxFld = output.txFld;
		
		//TODO make button
		
		//Add them to the MainPanel
		mainPanel = new JPanel(); //new JTabbedPane(tabPlacement);
		mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));
		mainPanel.add("Select input...", input);
		mainPanel.add("Select output...", output);
		mainPanel.add("Set Parameters...", params);
		
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
	
	//Builders
	private void buildPanel(){
		
		buildComponents();
		
		//put components together
		JPanel srcChooserBox = new JPanel();
		srcChooserBox.add(txFld);
		srcChooserBox.add(flChButton);
		
		JPanel descBox = new JPanel();
		descBox.setSize(30, 5);
		descBox.add(desc);
		
		add(srcChooserBox);
		add(descBox);
		
		
	}
	
	private void buildComponents(){

		//build the experiment description
		desc = new JTextArea("Experiment...",2, 20);
		
		
		//build the source name text field
		String txFldDisplay = "Choose an experiment (.jav)...";
		int txFldNColumns = 20;
		txFld = new JTextField(txFldDisplay,txFldNColumns);
		txFld.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				//Get the file name
				openExpt(txFld.getText());
			}
		});

		//build the file choosing button & file chooser
		
		flCh = new JFileChooser();
		flChButton = new JButton("Browse...");
		
		flChButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				int result = flCh.showOpenDialog(txFld);
				
				if (result==JFileChooser.APPROVE_OPTION){
					
					txFld.setText(flCh.getSelectedFile().getPath());
					openExpt(flCh.getSelectedFile().getPath());
					
					setOutput();
					
					
				}
			}
		});
		
		
		
	}
	
	
	//Auxiliary functions
	private void setOutput(){
		
		//If no destination exists, make a suggestion 
		if (outputTxFld.getText().equals("Save as...")){
			if (txFld.getText().contains(".jav")){
				outputTxFld.setText(txFld.getText().replace(".jav", ".csv"));
			} else if (txFld.getText().contains(".prejav")){
				outputTxFld.setText(txFld.getText().replace(".prejav", "_pre.csv"));
			}
		}
	}
	
	private void openExpt(String path){
		//Try to open experiment
		desc.setText("Opening experiment...");
		ex = Experiment.fromPath(path);
		
		if (ex!=null){
			desc.setText("Experiment: "+ex.getNumTracks()+" tracks");
		} else{
			desc.setText("Could not open file");
		}
	}
	
}



class OutputPanel extends JPanel{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	

	JTextField txFld;
	JButton flChButton;
	JFileChooser flCh;
	
	public OutputPanel(){
		buildPanel();
	}
	
	private void buildPanel(){
		
		//Build components
		buildComponents();
		
		//Put them together
		JPanel dstChooserBox = new JPanel();
		dstChooserBox.add(txFld);
		dstChooserBox.add(flChButton);
		
		add(dstChooserBox);
		
	}
	
	private void buildComponents(){
				
		//Build the dest name text field
		String txFldDisplay = "Save as...";
		int txFldNColumns = 20;
		txFld = new JTextField(txFldDisplay,txFldNColumns);
		
		//build the dest chooser and button
		flCh = new JFileChooser();
		flChButton = new JButton("Browse...");
		
		flChButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				int result = flCh.showSaveDialog(txFld);
				if (result==JFileChooser.APPROVE_OPTION){
					txFld.setText(flCh.getSelectedFile().getPath());
				}
			}
		});
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
	
	public ParamPanel(){
		init(null, null, null);
		buildPanel();
	}
	
	public ParamPanel(ProcessingParameters pp, ExtractionParameters ep, FittingParameters fp){
		init(pp, ep, fp);
		buildPanel();
	}
	
	private void init(ProcessingParameters pp, ExtractionParameters ep, FittingParameters fp){
		
		if (pp==null){
			procParams = new ProcessingParameters();
		} else {
			procParams = pp;
		}
		
		if (ep==null){
			extrParams = new ExtractionParameters();
		} else {
			extrParams = ep;
		}
		
		if (fp==null){
			fitParams = new FittingParameters();
		} else {
			fitParams = fp;
		}
		
	}
	
	private void buildPanel(){
		//Build the components
		buildComponents();
		
		//Add components to the panel
		setLayout(new GridLayout(1, 3));
		add(procParams.getPanel());
		
	}
	
	private void buildComponents(){
		
		//TODO
		
	}
	
	
}

class ProgressFrame extends JFrame{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
}