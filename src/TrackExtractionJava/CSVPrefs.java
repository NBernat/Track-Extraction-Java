package TrackExtractionJava;

import java.util.Arrays;
import java.util.Vector;

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
				true,//"pointID",  //Start of Trackpoint data
				false,//"pointType", 
				true,//"trackID",
				true,//"x", 
				true,//"y",
				false,//"rect.x",
				false,//"rect.y",
				false,//"rect.width",
				false,//"rect.height",
				true,//"area",
				false,//"thresh"//End of TrackPointData
				true,//"imOriginX",//Start of ImTrackPoint data
				true,//"imOriginY",//End of ImTrackPoint data
				false,//"prevPointID",//Start of MaggotTrackPoint data
				false,//"nextPointID",
				true,//"htValid",
				false,//"head.x",
				false,//"head.y",
				false,//"mid.x",
				false,//"mid.y",
				false,//"tail.x",
				false,//"tail.y",//End of MaggotTrackPoint
				true//"artificialMid"//Start&End of BackboneTrackPoint data
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
	
	
	
}
