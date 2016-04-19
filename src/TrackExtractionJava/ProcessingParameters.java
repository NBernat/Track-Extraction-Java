package TrackExtractionJava;

import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
//import java.nio.file.Path;
//import java.nio.file.Paths;
import java.text.NumberFormat;

import javax.swing.JCheckBox;
import javax.swing.JFormattedTextField;
import javax.swing.JLabel;
import javax.swing.JPanel;


public class ProcessingParameters {

	
	
	/**
	 * Min length that the fitter can handle
	 */
	public int minTrackLen = 200;
	
	
	
	public boolean doFitting = true;
	
	public boolean loadSingleTrackForFitting = false;
	
	public boolean saveSingleTracksFromFitting = false;
	
	
	
	/**
	 * Closes the MMF window
	 */
	boolean closeMMF = false;
	
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
	
	boolean diagnosticIm = true;
	
	public boolean savetoCSV = false;
	
	boolean testMagFromDisk = false;
	boolean testFitFromDisk = false;
	
	boolean sendDataToExtracted = false;
	
	ProcPanel ppPanel;
	
	
	public static String getOutFromInDir(String inDir){
		
//		Path p = Paths.get(inDir);
		File f = new File(inDir);
		
		StringBuilder out = new StringBuilder(f.getParent().toString());
		
		String dataStr = "data";
		String exStr = "extracted";
		int ind = out.indexOf(dataStr);
		if (ind>0){
			out.delete(ind, ind+dataStr.length());
			out.insert(ind, exStr);
		}		
		
		
		return out.toString();
//		return inDir;
	}
	
	
	public String[] setLogPath(String srcDir, String dstDir){
		String[] logPathParts = {srcDir, "ProcessingLog.txt"};
		if (logPathParts[0]==null || logPathParts[0].equals("")){
			if (dstDir!=null){
				logPathParts[0] = dstDir;
			} 
		}
		return logPathParts;
	}
	public String setExPath(String srcDir, String srcName, String dstDir, String dstName, String ext){
		StringBuilder dir;// = new StringBuilder(srcDir);
		StringBuilder name;// = new StringBuilder(srcName);
		
		//set intital path parts
		if (dstDir!=null && dstDir!=""){
			dir = new StringBuilder(dstDir);
		} else {
			dir = new StringBuilder(srcDir);
		}
		if (sendDataToExtracted){
			dir = new StringBuilder(getOutFromInDir(srcDir));
		}
		
		if (dstName!=null && dstName!=""){
			name = new StringBuilder(dstName);
		} else {
			 name = new StringBuilder(srcName);
		}
		
		//set extension
		if (name.lastIndexOf(".")>name.lastIndexOf(File.separator)){//if there is a file extension in the name
			name.replace(name.lastIndexOf("."), name.length(), ext);
		} else {
			name.append(ext);
		}
		
		return dir+File.separator+name;
	}
	
	public String setMagExPath(String srcDir, String srcName, String dstDir, String dstName){
		return setExPath(srcDir, srcName, dstDir, dstName, ".prejav");
	}
	public String setFitExPath(String srcDir, String srcName, String dstDir, String dstName){
		return setExPath(srcDir, srcName, dstDir, dstName, ".jav");
	}
	
	public ProcPanel getPanel(){
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
	String dofitName = "Do backbone fitting";
	
	JFormattedTextField minTrackLenField;
	JLabel minTrackLenLabel;
	JPanel minTrackLenPanel;
	String minTrackLengthName = "Minimum track length for fitting";
	
	JCheckBox toCSVBox;
	String toCSVName = "Save track data to CSV";
	
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
		JPanel mainPanel = new JPanel();
		mainPanel.setLayout(new GridLayout(3, 1));
		mainPanel.add(dofitBox);
		mainPanel.add(minTrackLenPanel);
		mainPanel.add(toCSVBox);
		
		add(mainPanel);
	}
	
	public void buildComponents(){
		
		dofitBox = new  JCheckBox(dofitName, prPs.doFitting);
		dofitBox.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				prPs.doFitting = dofitBox.isSelected();
				prPs.saveFitEx = dofitBox.isSelected();
			}
		});
		
		
		minTrackLenField = new JFormattedTextField(NumberFormat.getIntegerInstance());
		minTrackLenField.setValue(prPs.minTrackLen); 
		minTrackLenField.addPropertyChangeListener( new PropertyChangeListener() {
			
			@Override
			public void propertyChange(PropertyChangeEvent evt) {
				Integer mtl = ((Number)minTrackLenField.getValue()).intValue();
				prPs.minTrackLen = mtl.intValue();
			}
		}); 
		minTrackLenLabel = new JLabel(minTrackLengthName);
		minTrackLenPanel = new JPanel(new BorderLayout());
		minTrackLenPanel.add(minTrackLenField, BorderLayout.WEST);
		minTrackLenPanel.add(minTrackLenLabel);
		
		toCSVBox = new  JCheckBox(toCSVName, prPs.savetoCSV);
		toCSVBox.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				prPs.savetoCSV = toCSVBox.isSelected();
				
			}
		});
		
	}
	
	public static ProcPanel makePanel(ProcessingParameters pp){
		
		return new ProcPanel(pp);
	}
	
	
}
