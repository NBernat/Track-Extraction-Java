import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;


public class TrackPanel extends JPanel {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	
	Track track;
	JTextArea trackDescription;
	JScrollPane scroll;
	
	JButton playButton;
	JPanel buttonPanel;
	
	public TrackPanel(){
		buildTrackPanel();
	}
	
	
	public void buildTrackPanel(){
		
		setLayout(new BorderLayout(5, 5));
		
		
		
		//Build & add the description panel
		trackDescription = new JTextArea(Track.emptyDescription());
		trackDescription.setLineWrap(true);
		scroll = new JScrollPane(trackDescription);
		
		
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
		
		
		add(scroll, BorderLayout.CENTER);
		add(buttonPanel, BorderLayout.SOUTH);
		
	}
	
	
	public void updateTrack(Track track){
		
		//Update the track
		this.track = track;
		//Update the message and scroll to the top
		trackDescription.setText(track.description());
		trackDescription.setCaretPosition(0);
		
	}
	
	public void playCurrentTrack(){
		if (track!=null){
			track.playMovie();
		}
	}
	
}
