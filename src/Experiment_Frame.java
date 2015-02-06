import java.awt.BorderLayout;

import javax.swing.JFrame;
import javax.swing.JList;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.ListSelectionModel;


public class Experiment_Frame extends JFrame{

	/**
	 * Serialization ID
	 */
	private static final long serialVersionUID = 1L;
	
	/**
	 * The experiment containing the tracks
	 */
	private Experiment ex;
	
	/**
	 * A list of tracks
	 */
	JList<Track> trackList;
	
	/**
	 * A description box
	 */
	JTextArea trackDescription;
	
	

	/**
	 * 
	 */
	public Experiment_Frame(TrackBuilder tb){
		
		ex = tb.toExperiment();
	}
	
	/**
	 * 
	 */
	public Experiment_Frame(String fname){
		//TODO check file name
		
		try {
			ex = Experiment.open(fname);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			
		} 
		
		
		
	}
	
	public void run(String args){

		
		
		////////////////////
		// Create Panels 
		////////////////////

		//LIST:
		//Make the track list
		trackList = new JList<Track>(ex.tracks);
		trackList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		//Add the trackList in a scroll pane 
		add(new JScrollPane(trackList), BorderLayout.WEST);
		
		//TODO DESCRIPTION:
		//set default textwindow value 
		trackDescription = new JTextArea(Track.emptyDescription());
		trackDescription.setLineWrap(true);
		
		
		
		
		//TODO BUTTONS:
		//play movie button
		
		//OPTIONS:
		//(do this later)
		//(write code for track to accept options)
		
		
		////////////////////
		// Setup Handlers  
		////////////////////
		
		//TODO Define List Action Handler 
		//(get index, generate description, update description)
		
		//TODO Define Play Movie handler 
		//(get index, get options, track.playMovie)
		
		
		
		produceFrame();
		
	}
	
	protected void produceFrame(){
		setSize(1000, 500);
		setTitle("Experiment "+ex.fname);
		setVisible(true);
	}
	
}
