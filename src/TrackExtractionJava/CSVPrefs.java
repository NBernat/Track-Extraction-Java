package TrackExtractionJava;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Arrays;
import java.util.Vector;

import javax.swing.BoxLayout;
import javax.swing.JCheckBox;
import javax.swing.JPanel;

public class CSVPrefs {
	
//	int[] frames;//TODO
//	int[] trackInds;//TODO
	
	Vector<String> fieldNames = new Vector<String>();
	boolean[] includeValue;
	Vector<String> emptyValue = new Vector<String>();
	
	public CSVPrefs(){
		fieldNames.addAll(Arrays.asList(
				"pointID", //Start of Trackpoint data
				"pointType", 
				"trackID",
				"x", 
				"y",
				"rect.x",
				"rect.y",
				"rect.width",
				"rect.height",
				"area",
				"thresh",//End of TrackPoint data
				"imOriginX",//Start of ImTrackPoint data
				"imOriginY",//End of ImTrackPoint data
				"prevPointID",//Start of MaggotTrackPoint data
				"nextPointID",
				"htValid",
				"head.x",
				"head.y",
				"mid.x",
				"mid.y",
				"tail.x",
				"tail.y",//End of MaggotTrackPoint
				"artificialMid"//Start&End of BackboneTrackPoint data
			));
			
			
			
			boolean[] initVals = {
				false,//"pointID",  //Start of Trackpoint data
				false,//"pointType", 
				false,//"trackID",
				true,//"x", 
				true,//"y",
				false,//"rect.x",
				false,//"rect.y",
				false,//"rect.width",
				false,//"rect.height",
				true,//"area",
				false,//"thresh"//End of TrackPointData
				false,//"imOriginX",//Start of ImTrackPoint data
				false,//"imOriginY",//End of ImTrackPoint data
				false,//"prevPointID",//Start of MaggotTrackPoint data
				false,//"nextPointID",
				true,//"htValid",
				false,//"head.x",
				false,//"head.y",
				false,//"mid.x",
				false,//"mid.y",
				false,//"tail.x",
				false,//"tail.y",//End of MaggotTrackPoint
				false//"artificialMid"//Start&End of BackboneTrackPoint data
			};
			includeValue=initVals;
			
			emptyValue.addAll(Arrays.asList(
					""+(-1),//"pointID",  //Start of Trackpoint data
					""+(-1),//"pointType", 
					""+(-1),//"trackID",
					""+(-1.0),//"x", 
					""+(-1.0),//"y",
					""+(-1),//"rect.x",
					""+(-1),//"rect.y",
					""+(-1),//"rect.width",
					""+(-1),//"rect.height",
					""+(-1.0),//"area",
					""+(-1),//"thresh"//End of TrackPointData
					""+(-1),//"imOriginX",//Start of ImTrackPoint data
					""+(-1),//"imOriginY",//End of ImTrackPoint data
					""+(-1),//"prevPointID",//Start of MaggotTrackPoint data
					""+(-1),//"nextPointID",
					"FALSE",//"htValid",
					""+(-1),//"head.x",
					""+(-1),//"head.y",
					""+(-1),//"mid.x",
					""+(-1),//"mid.y",
					""+(-1),//"tail.x",
					""+(-1),//"tail.y",//End of MaggotTrackPoint
					"FALSE"//"artificialMid"//Start&End of BackboneTrackPoint data
			));
			
	}
	
	public CSVPrefs(Vector<String> names, boolean[] vals){
	}
	
	public static CSVPrefs defaultPrefs(){
		return new CSVPrefs();
	}
	
	public String CSVheaders(){
		
		String headers="";
		
		for (int i=0; i<fieldNames.size(); i++){
			if (includeValue[i]){
				headers+=","+fieldNames.get(i);
			}
		}
		
		return headers;
	}
	
	public static int maxInd(String pointType){
		if (pointType.equalsIgnoreCase("TrackPoint")){
			return 10;
		} else if (pointType.equalsIgnoreCase("ImTrackPoint")){
			return 12;
		} else if (pointType.equalsIgnoreCase("MaggotTrackPoint")){
			return 21;
		} else if (pointType.equalsIgnoreCase("BackboneTrackPoint")||pointType.equalsIgnoreCase("Empty BackboneTrackPoint")){
			return 22;
		} else {
			return 0;
		}
		
	}
	
	public int lastInd(){
		int last = 0;
		for (int i=(includeValue.length-1); (last==0 && i>=0); i--){
			if (includeValue[i]) last = i;
		}
		
		return last;
	}
	
	/**
	 * Returns the number of possible fields (if COUNTTRUE is false) or the number of fields included (if COUNTTRUE is true)
	 * @param countTrue
	 * @return
	 */
	public int numFields(boolean countTrue){
		
		if(countTrue){
			int count=0;
			for (int i=0; i<includeValue.length; i++){
				if (includeValue[i]) count++;
			}
			return count;
		}else {
			return fieldNames.size();
		}
	}
	
	public String getEmptyVal(int ind){
		return (ind>=0 && ind<emptyValue.size())? emptyValue.get(ind) : "";
	}
	
	public void setIncludeVal(String name, boolean val){
		int i = fieldNames.indexOf(name);
		if (i>=0 && i<includeValue.length) includeValue[i]=val;
	}
	
}



class csvPrefPanel extends JPanel{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	
	CSVPrefs prefs;
	Vector<JCheckBox> checkboxes;
	
	
	public csvPrefPanel(){
		init(new CSVPrefs());
	}
	
	public csvPrefPanel(CSVPrefs prefs){
		init(prefs);
	}
	
	private void init(CSVPrefs prefs){
		
		this.prefs = prefs;
		
		checkboxes = new Vector<JCheckBox>();
		
		buildFrame();
	}
	
	
	protected void buildFrame(){
		//set layout
//		setLayout(new GridLayout(11, 2));
		setLayout(new BoxLayout(this, BoxLayout.X_AXIS));
		
		//build components
		buildBoxes();
		
		//add components 
		JPanel left = new JPanel();
		left.setLayout(new BoxLayout(left, BoxLayout.Y_AXIS));
		JPanel mid = new JPanel();
		mid.setLayout(new BoxLayout(mid, BoxLayout.Y_AXIS));
		JPanel right = new JPanel();
		right.setLayout(new BoxLayout(right, BoxLayout.Y_AXIS));
		
		int perCol = prefs.includeValue.length/3;
		if (prefs.includeValue.length%3>0) perCol++;
		for (int i=0; i<checkboxes.size(); i++){
			switch (i/perCol){
				case 0:
					left.add(checkboxes.get(i));
					break;
				case 1:
					mid.add(checkboxes.get(i));
					break;
				case 2:
					right.add(checkboxes.get(i));
					break;
				default:
					break;
			}
		}
		
		add(left);
		add(mid);
		add(right);
		
	}
	
	private void buildBoxes(){
		for (int i=0; i<prefs.fieldNames.size(); i++){
			JCheckBox newBox = new JCheckBox(prefs.fieldNames.get(i));
			newBox.setSelected(prefs.includeValue[i]);
			newBox.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					updatePrefs();
//					JCheckBox box = (JCheckBox)e.getSource();
//					prefs.setIncludeVal(box.getName(), box.isSelected());
				}
			});
			
			checkboxes.add(newBox);
		}
	}
	
	private void updatePrefs(){
		for (int i=0; i<checkboxes.size(); i++){
			prefs.includeValue[i]=checkboxes.get(i).isSelected();
		}
	}
	
	
}


