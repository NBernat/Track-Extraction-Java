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
		
	}
	
	
	public ImageStack getStack(){
		return WindowManager.getCurrentWindow().getImagePlus().getImageStack();
	}
	
	public static void main(String[] args) {
        
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
	JPanel buttonPanel;
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
		
		runButton = new JButton("Run extraction");
		runButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				int statecode = validRunState();
				if (statecode==0){
					runProcessor();
					//TODO close window, show progress
				} else {
					
					String message = "";
					switch(statecode){
					case 2:
						
						break;
					default: 
						message += "Unable to process";
					}
					
					new TextWindow("Processing Message", message, 200, 200);	
				}
			}
		} );
		
		buttonPanel = new JPanel();
		buttonPanel.add(runButton);
		
		//Add them to the MainPanel
		mainPanel = new JPanel(); //new JTabbedPane(tabPlacement);
		mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));
		mainPanel.add("Select input...", input);
		mainPanel.add("Select output...", output);
		mainPanel.add("Set Parameters...", params);
		mainPanel.add("Run Extraction", buttonPanel);
		
		//Add mainPanel to frame
		add(mainPanel);
		
		pack();
	}
	
	
	private void showFrame(){
		setSize(panelSize);
		setTitle(panelName);
		setVisible(true);
	}
	
	private int validRunState(){
		
		
		//TODO
		return 0;
	}
	
	private void runProcessor(){
		
		
		Experiment_Processor ep = new Experiment_Processor();
		
		//Set params from input
		ep.runningFromMain = true;
		ep.prParams = params.procParams;
		ep.extrParams = params.extrParams;
		ep.fitParams = params.fitParams;
		
		//Set src and dest
		String[] epArgs = new String[3];
		epArgs[0] = input.txFld.getText();
		
		
		ep.run("");
		
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
		flCh.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
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
		add(extrParams.getPanel());
		
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