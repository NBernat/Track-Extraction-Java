package TrackExtractionJava;

import ij.IJ;
import ij.io.SaveDialog;
import ij.text.TextWindow;

import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Vector;





import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFrame;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
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
	 * Display parameters for showing track movies
	 */
	MaggotDisplayParameters mdp;
	
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
	 * A panel to choose display options for the track.playMovie
	 */
	JPanel playPanel;
	
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
		ex = Experiment.deserialize(fname);
	}
	
	
	
	
	public void run(String args){

		buildFrame();
		
		showFrame();
		
	}
	
	
	protected void buildFrame(){
		
		mdp = new MaggotDisplayParameters();
		
		//Build the trackPanel
		trackPanel = new TrackPanel(mdp);
		
		//Build the trackList 
		buildExPanel();
		
		//Build the display option panel
		buildOpPanel();
		
		
		//Add components
		add(trackPanel, BorderLayout.CENTER);
		add(exPanel, BorderLayout.WEST);
		add(playPanel, BorderLayout.EAST);
//		add(new JScrollPane(trackList), BorderLayout.WEST);
		pack();
		
		addWindowListener(new java.awt.event.WindowAdapter() {
		    @Override
		    public void windowClosing(java.awt.event.WindowEvent windowEvent) {
		        ex = null;
		        mdp=null;
		        trackList = null;//.removeAll();
		        trackPanel = null;//.removeAll();
		        exPanel = null;//.removeAll();
		        playPanel = null;//.removeAll();
		        
		    }
		});
	}
	
	protected void showFrame(){
		setSize(550, 600);
		setTitle("("+ex.getNumTracks()+" tracks) Experiment "+ex.getFileName());
//		setTitle("("+Experiment.getNumTracks(ex.getFileName())+" tracks) Experiment "+ex.getFileName());
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
				
				trackPanel.updateTrack(getCurrentTrack());//;ex.tracks.get(trackList.getSelectedIndex()));
				//TODO change to get the track id 
				
			}
		});
		trackList.addMouseListener(new MouseAdapter(){
		    @Override
		    public void mouseClicked(MouseEvent e){
		        if(e.getClickCount()==2){
		            trackPanel.updateTrack(getCurrentTrack());
		            trackPanel.playCurrentTrack();
		        }
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
				
				
				if (new File(sd.getDirectory(), sd.getFileName()).exists()) {
					new TextWindow("Message", "That file name already exists", 500, 500);
				} else {
					//TODO enforce that the file doesn't already exist 
					//Save the file 
					try{
						IJ.showStatus("Saving file...");
						ex.serialize(sd.getDirectory(), sd.getFileName());
						IJ.showStatus("File saved!");
					} catch (Exception exception){
						new TextWindow("Error", "could not save experiment at the given directory\n"+exception.getMessage(), 500, 500);
					}
				}
			}
		});
		JPanel buttonPanel = new JPanel();
		buttonPanel.add(saveButton);
		
		
		//Add list and button to panel
		exPanel.add(trackListPanel, BorderLayout.CENTER);
		exPanel.add(buttonPanel, BorderLayout.SOUTH);
		
	}
	
	protected void buildOpPanel(){
		playPanel = new JPanel();
		
		playPanel.add(new DisplayOpPanel(mdp));
		
	}
	
	protected Vector<String> trackNames(){
		
		Vector<String> names = new Vector<String>();
		
//		ListIterator<Track> trIt = ex.tracks.listIterator();
//		while(trIt.hasNext()){
		for (int i=0; i<ex.getNumTracks(); i++){
					
			Track t = ex.getTrackFromInd(i);//trIt.next();
			String name = "Track "+t.getTrackID()+" ("+t.getNumPoints()+")";
			if (t instanceof CollisionTrack) name+="*";
			names.add(name);
		}
		
		return names;
		
	}
	
	protected int getCurrentTrackID(){
		String name = (String) trackList.getSelectedValue();
		int beforeInd = name.indexOf(" ");
		int afterInd = name.indexOf(" ", beforeInd+1);
		return Integer.valueOf(name.substring(beforeInd+1, afterInd-1));
	}
	
	protected Track getCurrentTrack(){
//		return ex.getTrack(getCurrentTrackID());
		return ex.getTrackFromInd(trackList.getSelectedIndex());
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
	
	MaggotDisplayParameters mdp;
	
	JButton saveButton;
	JPanel buttonPanel;
	
	public TrackPanel(MaggotDisplayParameters mdp){
		this.mdp = mdp;
		buildTrackPanel();
	}
	
	
	public void buildTrackPanel(){
		
		setLayout(new BorderLayout(5, 5));
		
		
		
		//Build & add the description panel
		trackDescription = new JTextArea(Track.emptyDescription());
		trackDescription.setLineWrap(false);
		descriptionPanel = new JScrollPane(trackDescription);
		
		
		//Build and add the play button 
		JButton playButton = new JButton("Play Track");
		playButton.setSize(100, 40);
		playButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				playCurrentTrack();
			}
		});
		
//		JButton plotButton = new JButton("Plot Track Energies");
//		plotButton.setSize(150, 40);
//		plotButton.addActionListener(new ActionListener() {
//			@Override
//			public void actionPerformed(ActionEvent e) {
//				plotCurrentTrackE();
//			}
//		});
		//Build and add the play button 
		buttonPanel = new JPanel();
		buttonPanel.add(playButton);
//		buttonPanel.add(plotButton);
		
		
		add(descriptionPanel, BorderLayout.CENTER);
		add(buttonPanel, BorderLayout.SOUTH);
		
	}
	
	
	public void updateTrack(Track track){
		
		//Update the track
		this.track = track;
		//Update the message and scroll to the top
		trackDescription.setText(track.description());
		trackDescription.setCaretPosition(0);
		//TODO Set ePlotPaneltrack
	}
	
	public void playCurrentTrack(){
		try{
			if (track!=null){
				track.playMovie(mdp);
			}
		} catch(Exception e){
			StringWriter sw = new StringWriter();
			PrintWriter prw = new PrintWriter(sw);
			e.printStackTrace(prw);
			new TextWindow("PlayMovie Error", "Could not play track "+track.getTrackID()+" movie\n"+sw.toString()+"\n", 500, 500);
		}
	}
	
	
	public void plotCurrentTrackE(){
		
	}
	
}


class DisplayOpPanel extends JPanel{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	private MaggotDisplayParameters mdp;
	HashMap<JCheckBox, String> paramNames;
	private JCheckBox clusterBox;
	private JCheckBox midBox;
	private JCheckBox initialBBBox;
	private JCheckBox contourBox;
	private JCheckBox htBox;
	private JCheckBox forcesBox;
	private JCheckBox backboneBox;
	
	/**
	 * Constructs a Display option panel with the given display parameters
	 * @param mdp
	 */
	public DisplayOpPanel(MaggotDisplayParameters mdp){
		this.mdp = mdp;//new MaggotDisplayParameters();
		buildDisplayOpPanel();
	}
	
	private void buildDisplayOpPanel(){
		setLayout(new GridLayout(7, 1));
		
		buildCheckBoxes();
		add(clusterBox);
		add(midBox);
		add(initialBBBox);
		add(contourBox);
		add(htBox);
		add(forcesBox);
		add(backboneBox);
	}
	
	private void buildCheckBoxes(){
//		paramNames.put(clusterBox, "clusters");
//		buildCheckBox(clusterBox, "Clusters");
		clusterBox = new JCheckBox("Clusters");
		clusterBox.setSelected(mdp.getParam("clusters"));
		clusterBox.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				mdp.setParam("clusters", clusterBox.isSelected());
			}
		});
		
//		paramNames.put(midBox, "mid");
//		buildCheckBox(midBox, "Midline");
		midBox = new JCheckBox("Midline");
		midBox.setSelected(mdp.getParam("mid"));
		midBox.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				mdp.setParam("mid", midBox.isSelected());
			}
		});
		
//		paramNames.put(initialBBBox, "initialBB");
//		buildCheckBox(initialBBBox, "Initial Backbone Guess");
		initialBBBox = new JCheckBox("Initial Backbone Guess");
		initialBBBox.setSelected(mdp.getParam("initialBB"));
		initialBBBox.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				mdp.setParam("initialBB", initialBBBox.isSelected());
			}
		});
		
//		paramNames.put(contourBox, "contour");
//		buildCheckBox(clusterBox, "Contour");clusterBox = new JCheckBox("Clusters");
		contourBox = new JCheckBox("Contour");
		contourBox.setSelected(mdp.getParam("contour"));
		contourBox.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				mdp.setParam("contour", contourBox.isSelected());
			}
		});

		
//		paramNames.put(htBox, "ht");
//		buildCheckBox(htBox, "Head & Tail");
		htBox = new JCheckBox("Head & Tail");
		htBox.setSelected(mdp.getParam("ht"));
		htBox.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				mdp.setParam("ht", htBox.isSelected());
			}
		});

		
//		paramNames.put(forcesBox, "forces");
//		buildCheckBox(forcesBox, "Forces");
		forcesBox = new JCheckBox("Forces");
		forcesBox.setSelected(mdp.getParam("forces"));
		forcesBox.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				mdp.setParam("forces", forcesBox.isSelected());
			}
		});

		
//		paramNames.put(backboneBox, "backbone");
//		buildCheckBox(backboneBox, "Backbone");
		backboneBox = new JCheckBox("Backbone");
		backboneBox.setSelected(mdp.getParam("backbone"));
		backboneBox.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				mdp.setParam("backbone", backboneBox.isSelected());
			}
		});

	}
	
//	private void buildCheckBox(JCheckBox box, String title){
//		
//		box = new JCheckBox(title);
//		box.setSelected(mdp.getParam(paramNames.get(box)));
//		box.addActionListener(new ActionListener() {
//			
//			@Override
//			public void actionPerformed(ActionEvent e) {
//				mdp.setParam(paramNames.get(box), box.isSelected());
//			}
//		});
//		
//		
//	}
	
	
}


