package TrackExtractionJava;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;

import javax.swing.BoxLayout;
import javax.swing.ButtonModel;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSeparator;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;


//import ij.ImageJ;
import ij.ImageStack;
import ij.WindowManager;
import ij.plugin.PlugIn;
import ij.plugin.frame.RoiManager;
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
		
		ImageStack stack = WindowManager.getCurrentWindow().getImagePlus().getImageStack();
//		WindowManager.getCurrentWindow().getImagePlus().setOverlay(null);
//		WindowManager.getCurrentWindow().close();
//		RoiManager.getInstance().move;
//		WindowManager.getCurrentWindow().close();
		return stack;
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
	Dimension panelSize = new Dimension(500,650);
	String panelName = "Experiment Processor"; 
	
	InputPanel input;
	ParamPanel params;
	OutputPanel output;
	JPanel buttonPanel;
	JButton runButton;
	
	public ExtractorFrame(){
		setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
	}
	
	
	public void run(String args){

		buildFrame();
		
		showFrame();
		
	}
	
	private void buildFrame(){
		
		//Build components
		input = new InputPanel();
		output = new OutputPanel();
		params = new ParamPanel();
		input.outputDirFld = output.dirTxFld;
		input.outputNameFld = output.nameTxFld;
		
		runButton = new JButton("Run Extraction");
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
		runButton.getModel().addChangeListener(new ChangeListener() {
			
			@Override
			public void stateChanged(ChangeEvent e) {
				ButtonModel model = (ButtonModel) e.getSource();
				if(model.isPressed()){
					runButton.setText("Running...");
				} else {
					runButton.setText("Run Extraction");
				}
				
			}
		});
		
		buttonPanel = new JPanel();
		buttonPanel.add(runButton);
		
		//Add them to the MainPanel
		mainPanel = new JPanel(); //new JTabbedPane(tabPlacement);
		mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));
		
		mainPanel.add(makeLabelPanel("Source"));
		mainPanel.add("Select input...", input);
		mainPanel.add(new JSeparator(JSeparator.HORIZONTAL));
		mainPanel.add(makeLabelPanel("Parameters"));
		mainPanel.add("Parameters...", params);
		mainPanel.add(new JSeparator(JSeparator.HORIZONTAL));
		mainPanel.add(makeLabelPanel("Destination"));
		mainPanel.add("Select output...", output);
		mainPanel.add(new JSeparator(JSeparator.HORIZONTAL));
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
		

//		ImageJ imj = new ImageJ(ImageJ.NO_SHOW);
		Experiment_Processor ep = new Experiment_Processor();
		
		
		//Set params from input
		ep.runningFromMain = false;
		ep.prParams = params.procParams;
		ep.extrParams = params.extrParams;
		ep.fitParams = params.fitParams;
		ep.csvPrefs = params.cPrefs;
		
		//Set src and dest
		String[] epArgs = new String[3];
		epArgs[0] = input.txFld.getText();
		epArgs[1] = output.dirTxFld.getText();
		epArgs[2] = output.nameTxFld.getText();
		
		ep.run(epArgs);

//		imj.quit();
	}
	
	public JPanel makeLabelPanel(String labelText){
		
		JPanel labelPanel = new JPanel();
		JLabel label = new JLabel(labelText);
		label.setFont(new Font(label.getFont().getName(), Font.BOLD, label.getFont().getSize()*2));
		labelPanel.add(label);
		
		return labelPanel;
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
	JTextField outputDirFld;
	JTextField outputNameFld;
	

	static String txFldDisplay = "Choose a file... (or type 'current' )";
	int txFldNColumns = 20;
	
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
//		add(descBox);
		
		
	}
	
	private void buildComponents(){

		//build the experiment description
		desc = new JTextArea("Experiment...",2, 20);
		
		
		//build the source name text field
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
		
		File f = new File(txFld.getText());
		String dir = f.getParent();
		String name = f.getName();
		
		//If no destination exists, make a suggestion 
		if(outputDirFld.getText().equals(OutputPanel.dirTxFldDisplay)){
			outputDirFld.setText(dir);
		}
		
		if (outputNameFld.getText().equals(OutputPanel.nameTxFldDisplay)){
			int i = name.lastIndexOf(".");
			outputNameFld.setText(name.substring(0, i));
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
	

	JTextField dirTxFld;
	JButton flChButton;
	JFileChooser flCh;
	
	JTextField nameTxFld;
	
	
	static String dirTxFldDisplay = "Choose save directory...";
	int dirTxFldNColumns = 20;
	static String nameTxFldDisplay = "Choose save name...";
	int nameTxFldNColumns = 20;
	
	public OutputPanel(){
		buildPanel();
	}
	
	private void buildPanel(){
		
		//Build components
		buildComponents();
		
		setLayout(new BoxLayout(this, BoxLayout.Y_AXIS)); 
		
		//Put them together
		JPanel dirChooserPanel = new JPanel();
		dirChooserPanel.add(dirTxFld);
		dirChooserPanel.add(flChButton);
		JPanel namePanel = new JPanel();
		namePanel.add(nameTxFld);
		
		add(dirChooserPanel);
		add(namePanel);
		
	}
	
	private void buildComponents(){
		
		
		
		//Build the dir  text field
		dirTxFld = new JTextField(dirTxFldDisplay,dirTxFldNColumns);
		
		//build the dir chooser and button
		flCh = new JFileChooser();
		flCh.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
		flChButton = new JButton("Browse...");
		
		flChButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				int result = flCh.showSaveDialog(dirTxFld);
				if (result==JFileChooser.APPROVE_OPTION){
					dirTxFld.setText(flCh.getSelectedFile().getPath());
				}
			}
		});
		
		//build the name text field
		nameTxFld = new JTextField(nameTxFldDisplay,nameTxFldNColumns);
	}
	
	
}


class ParamPanel extends JPanel{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	ProcessingParameters procParams;
	ProcPanel pp;
	ExtractionParameters extrParams;
	extrPanel ep;
	FittingParameters fitParams;
	CSVPrefs cPrefs;
	JButton cPrefButton;
	JFrame cPrefFrame;
	JPanel cPrefPanel;
	
	public ParamPanel(){
		init(null, null, null, null);
		buildPanel();
	}
	
	public ParamPanel(ProcessingParameters pp, ExtractionParameters ep, FittingParameters fp, CSVPrefs cp){
		init(pp, ep, fp, cp);
		buildPanel();
	}
	
	private void init(ProcessingParameters pp, ExtractionParameters ep, FittingParameters fp, CSVPrefs cp){
		
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
		
		if (cp==null){
			cPrefs = new CSVPrefs();
		} else {
			cPrefs = cp;
		}
		
	}
	
	private void buildPanel(){
		//Build the components
		buildComponents();
		
		//Add components to the panel
		setLayout(new BoxLayout(this,BoxLayout.Y_AXIS));
		
		add(pp);
		add(cPrefPanel);
		add(ep);
		
	}
	
	private void buildComponents(){
		
		pp = procParams.getPanel();
		pp.setAlignmentX(Component.CENTER_ALIGNMENT);
		ep = extrParams.getPanel();
		ep.setAlignmentX(Component.CENTER_ALIGNMENT);
		
		cPrefButton = new JButton("Set CSV Saving Preferences");
		cPrefButton.addActionListener(new ActionListener() {
			
			public void actionPerformed(ActionEvent e) {
				setCSVPrefs();
			}
		});
		cPrefPanel = new JPanel();
		cPrefPanel.add(cPrefButton);
		
	}
	
	private void setCSVPrefs(){
		
		cPrefFrame = new JFrame();
		
		//Build components
		csvPrefPanel cpp = new csvPrefPanel(cPrefs);
		JButton okButton = new JButton("OK");
		okButton.addActionListener(new ActionListener() {
			
			public void actionPerformed(ActionEvent e) {
				procParams.savetoCSV = true;
				pp.toCSVBox.setSelected(true);
				cPrefFrame.dispose();
			}
		});
		
		//Display components in frame
		cPrefFrame.setLayout(new BorderLayout());
		cPrefFrame.add(cpp, BorderLayout.CENTER);
		cPrefFrame.add(okButton, BorderLayout.SOUTH);
		
		cPrefFrame.pack();

		cPrefFrame.setTitle("Test Frame for CSV preferences");
		cPrefFrame.setVisible(true);
	}
	
	
}

class ProgressFrame extends JFrame{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	JLabel progLabel;
	
	public ProgressFrame() {
		setTitle("Progress");
	}
	
	
	
	
	public static void updateProgress(ProgressFrame pf, String statusUpdate){
		if (pf!=null){
			
		}
	}
}