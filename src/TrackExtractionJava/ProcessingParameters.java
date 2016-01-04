package TrackExtractionJava;

import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
//import java.nio.file.Path;
//import java.nio.file.Paths;
import java.text.NumberFormat;

import javax.swing.JCheckBox;
import javax.swing.JFormattedTextField;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;


public class ProcessingParameters {

	
	
	/**
	 * Min length that the fitter can handle
	 */
	int minTrackLen = 500;//TODO set this when fp is set
	
	
	
	boolean doFitting = true;
	
	/**
	 * Closes the MMF window
	 */
	boolean closeMMF = false;//TODO set/use this 
	
	/**
	 * Displays an ExperimentFrame after extracting tracks 
	 */
	boolean showMagEx = false;
	/**
	 * Displays an ExperimentFrame after fitting tracks 
	 */
	boolean showFitEx = false;
	
	/**
	 * Whether or not to automatically save the extracted tracks of MTPs
	 */
	boolean saveMagEx = true;
	/**
	 * Whether or not to automatically save the fitted tracks of BTPs
	 */
	boolean saveFitEx = true;
	
	boolean saveErrors = true;
	
	boolean testMagFromDisk = false;
	boolean testFitFromDisk = false;
	
	boolean sendDataToExtracted = true;
	
	ProcPanel ppPanel;
	
	
	public static String getOutFromInDir(String inDir){
		
//		Path p = Paths.get(inDir);
//		
//		StringBuilder out = new StringBuilder(p.getParent().toString());
//		
//		String dataStr = "data";
//		String exStr = "extracted";
//		int ind = out.indexOf(dataStr);
//		out.delete(ind, ind+dataStr.length());
//		out.insert(ind, exStr);
//				
//		return out.toString();
		return inDir;
	}
	
	
	public String[] setLogPath(String srcDir, String srcName){
		String[] logPathParts = {srcDir, "ProcessingLog.txt"};
		return logPathParts;
	}
	
	public String[] setMagExPath(String srcDir, String srcName){
		StringBuilder path = new StringBuilder(srcDir);
		StringBuilder name = new StringBuilder(srcName);
		if (sendDataToExtracted){
			path = new StringBuilder(getOutFromInDir(srcDir));
		}
		name.replace(name.lastIndexOf("."), name.length(), ".prejav");
		String[] MagExPathParts = {path.toString(), name.toString()};
		return MagExPathParts;
	}
	
	public String[] setFitExPath(String srcDir, String srcName){
		//Clean up use of stringbuilder vs string 
		StringBuilder path = new StringBuilder(srcDir);
		StringBuilder name = new StringBuilder(srcName);
		if (sendDataToExtracted){
			path = new StringBuilder(getOutFromInDir(srcDir));
		}
		name.replace(name.lastIndexOf("."), name.length(), ".jav");
//		int mtpInd = name.indexOf("MTP");
//		if (mtpInd>=0){
//			name.replace(mtpInd, 3, "BTP");
//		}
		String[] FitExPathParts = {path.toString(), name.toString()};
		return FitExPathParts;
	}
	
	
	public JPanel getPanel(){
		if (ppPanel==null){
			ppPanel = ProcPanel.makePanel(this);
		}
		return ppPanel;
	}
	
	
	
}

class ProcPanel extends JPanel {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	ProcessingParameters prPs;
	
	JCheckBox dofitBox;
	String dofitName = "Do fitting";
	
	JFormattedTextField minTrackLenField;
	JLabel minTrackLenLabel;
	JPanel minTrackLenPanel;
	String minTrackLengthName = "Minimum track length for fitting";
	
	JCheckBox viewExBox;
	String viewExName = "View experiment after processing";
	
	public ProcPanel(ProcessingParameters pp){
		if (pp==null){
			prPs = new ProcessingParameters();
		} else {
			prPs = pp;
		}
		buildPanel();
	}
	
	private void buildPanel(){
		//build components
		buildComponents();
		
		//add components to panel
		setLayout(new GridLayout(3, 1));
		add(dofitBox);
		add(minTrackLenPanel);
		add(viewExBox);
		
		
	}
	
	public void buildComponents(){
		
		dofitBox = new  JCheckBox(dofitName, prPs.doFitting);
		dofitBox.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				prPs.doFitting = dofitBox.isSelected();
				
			}
		});
		
		
		minTrackLenField = new JFormattedTextField(NumberFormat.getIntegerInstance());
		minTrackLenField.setValue(prPs.minTrackLen); 
		minTrackLenField.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				
				prPs.minTrackLen = (Integer)minTrackLenField.getValue();
				if (minTrackLenField.isEditValid()){
					prPs.minTrackLen = (Integer)minTrackLenField.getValue();
				} else {
					minTrackLenField.setValue(minTrackLenField.getValue());
				}
			}
		});
		minTrackLenLabel = new JLabel(minTrackLengthName);
		minTrackLenPanel = new JPanel(new BorderLayout());
		minTrackLenPanel.add(minTrackLenField, BorderLayout.WEST);
		minTrackLenPanel.add(minTrackLenLabel);
		
		viewExBox = new  JCheckBox(viewExName, (prPs.doFitting)?prPs.showFitEx:prPs.showMagEx);
		viewExBox.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				//Both parameters are changed; if viewEx is selected, the appropriate
				//param is DEselected based on doFitting, so that only one is shown
				prPs.showFitEx = viewExBox.isSelected();
				prPs.showMagEx = viewExBox.isSelected();
				
			}
		});
		
	}
	
	public static ProcPanel makePanel(ProcessingParameters pp){
		
		return new ProcPanel(pp);
	}
	
	
}
