import java.awt.BorderLayout;

//import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JList;
import javax.swing.JScrollPane;
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
	 * 
	 */
	public ExperimentFrame(TrackBuilder tb){
		
		ex = tb.toExperiment();
	}
	
	
	public ExperimentFrame(Experiment ex){
		
		this.ex = ex;
		
	}
	
	
	/**
	 * 
	 */
	public ExperimentFrame(String fname){
		//TODO check file name
		
		try {
			ex = Experiment.open(fname);
		} catch (Exception e) {
			e.printStackTrace();
			
		} 
		
		
		
	}
	
	
	public void run(String args){

		buildFrame();
		
		showFrame();
		
	}
	
	
	@SuppressWarnings({ "rawtypes", "unchecked" })
	protected void buildFrame(){
		
		//Build the trackPanel
		trackPanel = new TrackPanel();
		
		//Build the trackList and set selection handler
		trackList = new JList(ex.tracks);
		trackList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		trackList.addListSelectionListener(new ListSelectionListener() {
			
			@Override
			public void valueChanged(ListSelectionEvent e) {
				
				trackPanel.updateTrack((Track) trackList.getSelectedValue());
				
			}
		});
		
		//Add components
		add(trackPanel, BorderLayout.CENTER);
		add(new JScrollPane(trackList), BorderLayout.WEST);
		pack();
	}
	
	protected void showFrame(){
		setSize(500, 300);
		setTitle("Experiment "+ex.fname);
		setVisible(true);
	}
	
}
