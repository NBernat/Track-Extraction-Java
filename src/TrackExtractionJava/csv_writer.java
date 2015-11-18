package TrackExtractionJava;


import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;


import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextArea;
import javax.swing.JTextField;



public class csv_writer {

	
	
	
	
	
	
}


class writerFrame extends JFrame {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	

	Experiment ex;
	
	JPanel srcNamePanel;
	JFileChooser srcNameChooser;
	JTextField srcName;
	JTextArea description;
	Dimension srcDim;
	
	JPanel csvPrefPanel;
	csvPrefPanel cpp;
	
	JPanel dstNamePanel;
	JFileChooser dstNameChooser;
	JTextField dstName;
	Dimension dstDim;
	
	JPanel savePanel;
	
	
	public writerFrame() {
	}
	
	public writerFrame(Experiment ex) {
		this.ex = ex;
	}
	
	
	public void run(){
		
		buildFrame();
		showFrame();
		
	}
	
	private void showFrame(){
		
		setTitle("Save experiment to CSV...");
		
		setSize(600,600);
//		pack();
		setVisible(true);
	}
	
	
	private void buildFrame(){
		
		buildComponents();
		
		setLayout(new BorderLayout());
		
		add(srcNamePanel, BorderLayout.NORTH);
		add(csvPrefPanel, BorderLayout.CENTER);
		
		JPanel southPanel = new JPanel();
		southPanel.setLayout(new BoxLayout(southPanel, BoxLayout.Y_AXIS));
		southPanel.add(dstNamePanel);
		southPanel.add(savePanel);
		
		add(southPanel, BorderLayout.SOUTH);
	}
	
	
	private void buildComponents(){
		
		//Build experiment chooser/opener panel
		buildExptChooser();
		
		//Create new csv panel
		buildCPPPanel();
		
		//create new destination chooser panel
		buildDestChooser();
		
		buildSavePanel();
	}
	
	
	private void buildExptChooser(){
		
		srcDim = new Dimension(500,100);
		
		
		if (ex!=null){
			//Make text display
			
			String exptDisplay = ex.getFileName()+"\n"
					+"("+ex.getNumTracks()+" tracks)";
			
			description = new JTextArea(exptDisplay);
			srcNamePanel = new JPanel();
			srcNamePanel.add(description);
			
		} else {
			
			//Make a name field
			JPanel namePanel = new JPanel();
			namePanel.setSize(getPreferredSize()); 
			String exptDisplay = "Choose an experiment (.jav)...";
			srcName = new JTextField(exptDisplay);
			srcName.setMaximumSize(srcDim);
			namePanel.add(srcName);
			
			description = new JTextArea("Experiment...");
			description.setSize(200, 50);
			srcName.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					//Get the file name
					openExpt(srcName.getText());
				}
			});
//			namePanel.add(description);
			
			//Make the button to browse files
			JButton browseSrcButton = new JButton("Browse...");
			srcNameChooser = new JFileChooser();
			browseSrcButton.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					int result = srcNameChooser.showOpenDialog(srcNamePanel);
					
					if (result==JFileChooser.APPROVE_OPTION){
						srcName.setText(srcNameChooser.getSelectedFile().getPath());
						
						openExpt(srcNameChooser.getSelectedFile().getPath());
						
						//If no destination exists, make a suggestion 
						if (srcName.getText().equals("")){
							if (srcName.getText().contains(".jav")){
								dstName.setText(srcName.getText().replace(".jav", ".csv"));
							} else if (srcName.getText().contains(".prejav")){
								dstName.setText(srcName.getText().replace(".prejav", ".csv"));
							}
						}
						
						
					}
				}
			});
			
			
			JPanel srcChooserPanel = new JPanel();
			srcChooserPanel.setLayout(new BoxLayout(srcChooserPanel, BoxLayout.X_AXIS));
			srcChooserPanel.add(namePanel);
			srcChooserPanel.add(browseSrcButton);
			
			
			srcNamePanel = new JPanel();
			srcNamePanel.setLayout(new BoxLayout(srcNamePanel, BoxLayout.Y_AXIS));
//			srcNamePanel.setSize(500, 100);
//			srcNamePanel.add(namePanel, BorderLayout.CENTER);
//			srcNamePanel.add(browseSrcButton, BorderLayout.EAST);
			
			
//			srcName.setMinimumSize(new Dimension((int)(srcDim.width*.8), (int)(srcDim.height*.8)));
//			srcName.setMaximumSize(new Dimension((int)(srcDim.width*.8), (int)(srcDim.height*.8)));
			srcNamePanel.add(srcChooserPanel);
			srcNamePanel.add(description);
			
		}

		srcNamePanel.setMinimumSize(srcDim);
		srcNamePanel.setMaximumSize(srcDim);
		
	}
	
	
	private void buildCPPPanel(){

		csvPrefPanel = new JPanel(new BorderLayout());
		cpp = new csvPrefPanel();
		
		csvPrefPanel.add(cpp, BorderLayout.CENTER);
		
	}
	
	private void buildDestChooser(){
		
		dstNamePanel = new JPanel();
		dstDim = new Dimension(500, 200);
		
		//Make text field
		dstName = new JTextField("");
		if (ex!=null){
			//make suggestion based on src
			if (srcName.getText().contains(".jav")){
				dstName.setText(srcName.getText().replace(".jav", ".csv"));
			} else if (srcName.getText().contains(".prejav")){
				dstName.setText(srcName.getText().replace(".prejav", ".csv"));
			}
		}
		
		//Make button that opens file chooser
		JButton browseDstButton = new JButton("Browse...");
		dstNameChooser = new JFileChooser();
		browseDstButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				int result = dstNameChooser.showSaveDialog(dstNamePanel);
				
				if (result==JFileChooser.APPROVE_OPTION){
					dstName.setText(dstNameChooser.getSelectedFile().getPath());
				}
			}
		});
		
		
		//Add components to panel
		dstNamePanel.add(dstName);
		dstNamePanel.add(browseDstButton);
		
	}
	
	private void buildSavePanel(){
		savePanel = new JPanel(new BorderLayout());
		
		JButton saveToCSV = new JButton("Save to CSV");
		saveToCSV.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				int res = checkData();
				if (res==0){
					Experiment.toCSV(ex, dstName.getText(), cpp.prefs);
				}
			}
		}); 
		
		savePanel.add(saveToCSV, BorderLayout.CENTER);
		
	}
	
	
	private void openExpt(String path){
		//Try to open experiment
		description.setText("Opening experiment...");
		ex = Experiment.fromPath(path);
		
		if (ex!=null){
			description.setText("Experiment: "+ex.getNumTracks()+" tracks");
		} else{
			description.setText("Could not open file");
		}
	}
	
	
	private int checkData(){
		if (ex==null){
			JOptionPane.showMessageDialog(new JFrame(), "Load an experiment");
			return 1;
		}
		if (new File(dstName.getText()).exists()){
			JOptionPane.showMessageDialog(new JFrame(), "Save file already exists");
			return 2;
		}
		
		return 0;
	}
}
