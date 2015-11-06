package TrackExtractionJava;

import javax.swing.BoxLayout;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;



public class csv_writer {

	
	
	
	
	
	
}


class writerFrame extends JFrame {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	FileChooserPanel srcNameChooser;
	csvPrefPanel cpp;
	FileChooserPanel dstNameChooser;
	
	Experiment ex;
	
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
		setSize(550, 600);
		setTitle("Save experiment to CSV...");
		setVisible(true);
	}
	
	
	private void buildFrame(){
		
		buildComponents();
		
		setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
		
		add(srcNameChooser);
		add(cpp);
		add(dstNameChooser);
		
		
	}
	
	
	private void buildComponents(){
		
		//Build experiment chooser/opener panel
		buildExptChooser();
		
		//Create new csv panel
		cpp = new csvPrefPanel();
		
		//create new destination chooser panel
		buildDestChooser();
	}
	
	
	private void buildExptChooser(){
		
		if (ex!=null){
			//Make text display
			
		} else {
			//Make text field
			//Set empty text
			
			//Make button
			//Set listener to:
				//open file chooser
				//then, update text field with name
				//start opening file
				//when file is open, update text display
				//when file is open, update dest chooser
			
		}
		
		
	}
	
	private void buildDestChooser(){
		
		//Make text field
		
		if (ex!=null){
			//make suggestion based on src
		}
		
	}
	
	
}

class FileChooserPanel extends JPanel{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	JLabel label;
	JTextField fileField;
	JFileChooser chooser;
	
	
	public FileChooserPanel(String label) {
		
		buildComponents(label);
		
		buildPanel();
	}
	
	private void buildComponents(String label){
		
		//build label
		
		
		//build file field
		
		//build file browser & button
		
	}
	
	private void buildPanel(){
		
		
		
	}
	
}