import ij.io.SaveDialog;
import ij.text.TextWindow;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.ListIterator;
import java.util.Vector;














import javax.swing.JButton;
//import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
//import javax.swing.JTextArea;
import javax.swing.ListSelectionModel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;



public class ExperimentFrame extends JFrame{

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
	@SuppressWarnings("rawtypes")
	JList trackList;
	
	/**
	 * A TrackPanel to display info and provide track functions
	 */
	TrackPanel trackPanel;
	
	/**
	 * A panel to show a list of tracks and provide a save button
	 */
	JPanel exPanel;
	
	/**
	 * 
	 */
	public ExperimentFrame(TrackBuilder tb){
		ex = tb.toExperiment();
	}
	
	public ExperimentFrame(Experiment ex){
		this.ex = ex;
	}
	
	/**
	 * @throws Exception 
	 */
	public ExperimentFrame(String fname) throws Exception{
		//TODO check file name
		ex = Experiment.open(fname);
	}
	
	
	
	
	public void run(String args){

		buildFrame();
		
		showFrame();
		
	}
	
	
	protected void buildFrame(){
		
		//Build the trackPanel
		trackPanel = new TrackPanel();
		
		//Build the trackList and set selection handler
		buildExPanel();
		
		//Add components
		add(trackPanel, BorderLayout.CENTER);
		add(exPanel, BorderLayout.WEST);
//		add(new JScrollPane(trackList), BorderLayout.WEST);
		pack();
	}
	
	protected void showFrame(){
		setSize(400, 300);
		setTitle("Experiment "+ex.fname);
		setVisible(true);
	}
	
	
	@SuppressWarnings({ "rawtypes", "unchecked" })
	protected void buildExPanel(){
		
		exPanel = new JPanel();
		exPanel.setLayout(new BorderLayout(5, 5));
		
		//Build the track list 
		trackList = new JList(trackNames());
		trackList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		trackList.addListSelectionListener(new ListSelectionListener() {
			
			@Override
			public void valueChanged(ListSelectionEvent e) {
				
				trackPanel.updateTrack(ex.tracks.get(trackList.getSelectedIndex()));
				
			}
		});
		JScrollPane trackListPanel = new JScrollPane(trackList);
		
		//Build the button
		JButton saveButton = new JButton("Save Experiment");
		saveButton.setSize(150, 40);
		saveButton.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				//Get the file name
				DateFormat df = new SimpleDateFormat("yyyyMMddHHmmss");
				Date date = new Date();
				SaveDialog sd = new SaveDialog("Save Experiment", "ex"+df.format(date), ".ser");
				
				//Save the file 
				try{
					ex.save(sd.getDirectory(), sd.getFileName());
				} catch (Exception exception){
					new TextWindow("Error", "could not save experiment at the given directory\n"+exception.getMessage(), 500, 500);
				}
				
			}
		});
		JPanel buttonPanel = new JPanel();
		buttonPanel.add(saveButton);
		
		
		//Add list and button to panel
		exPanel.add(trackListPanel, BorderLayout.CENTER);
		exPanel.add(buttonPanel, BorderLayout.SOUTH);
		
	}
	
	protected Vector<String> trackNames(){
		
		Vector<String> names = new Vector<String>();
		
		ListIterator<Track> trIt = ex.tracks.listIterator();
		while(trIt.hasNext()){
			String name = "Track "+trIt.next().trackID;
			names.add(name);
		}
		
		return names;
		
	}
	
	
}

class TrackPanel extends JPanel {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	
	Track track;
	JTextArea trackDescription;
	JScrollPane descriptionPanel;
	
	JButton saveButton;
	JPanel buttonPanel;
	
	public TrackPanel(){
		buildTrackPanel();
	}
	
	
	public void buildTrackPanel(){
		
		setLayout(new BorderLayout(5, 5));
		
		
		
		//Build & add the description panel
		trackDescription = new JTextArea(Track.emptyDescription());
		trackDescription.setLineWrap(true);
		descriptionPanel = new JScrollPane(trackDescription);
		//TODO add save button to the 
		
		
		//Build and add the play button 
		JButton playButton = new JButton("Play Track");
		playButton.setSize(100, 40);
		playButton.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				playCurrentTrack();
			}
		});
		buttonPanel = new JPanel();
		buttonPanel.add(playButton);
		
		
		add(descriptionPanel, BorderLayout.CENTER);
		add(buttonPanel, BorderLayout.SOUTH);
		
	}
	
	
	public void updateTrack(Track track){
		
		//Update the track
		this.track = track;
		//Update the message and scroll to the top
//		descriptionPanel.getc
		trackDescription.setText(track.description());
		trackDescription.setCaretPosition(0);
		
	}
	
	public void playCurrentTrack(){
		if (track!=null){
			track.playMovie();
		}
	}
	
}


