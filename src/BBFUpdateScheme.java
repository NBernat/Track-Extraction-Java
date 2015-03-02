
/**
 * Provides a BackboneFitter with methods to implement the following updating scheme:
 * <p>
 * While (!converged && below iteration cap)  <br>
 * { alternate( (Update all)xALLFRAMESITERATIONS, (Update top)xTOPONLYITERATIONS)}<br>
 * Then: (Update all)xFINALITERATIONS
 * 
 * @author Natalie
 *
 */
public class BBFUpdateScheme {

	/**
	 * Parameters
	 */
	private final double convThres = .005;
	private final int maxIterations = 200;
	private final int allFramesIterations = 10;
	private final int topOnlyIterations = 10;
	private final int finalIterations = 10;
	
	/**
	 * Counters
	 */
	private int currentSchemeCount;
	private int totalCount;
	
	/**
	 * Flags
	 */
	private boolean done;
	private boolean updateAll;
	private boolean finalIters;
	
	/**
	 * Index lists
	 */
	private int[] defaultInds;
	private int[] inds2Update;
	
	/**
	 * Debugging
	 */
	transient Communicator comm;
	
	
	/**
	 * Constructs a Backbone Fitting algorithm updater and sets initial conditions
	 * @param numPts the number of Backbone Points in the track
	 */
	public BBFUpdateScheme(int numPts){
		
		defaultInds = new int[numPts];
		for(int i=0; i<defaultInds.length; i++) defaultInds[i]=i;
		
		done = false;
		finalIters = false;
		updateAll = true;
		
		totalCount = 0;
		currentSchemeCount = 0;
		
		inds2Update = defaultInds;
		
		
		comm = new Communicator();
		comm.setVerbosity(VerbLevel.verb_debug);
	}
	
	
	/**
	 * Indicates whether or not to keep running the BBF algorithm, under the assumption that a relaxation step just occurred 
	 * @return true to keep going, or false to end the algorithm
	 */
	public boolean keepGoing(double[] shifts){
		
		totalCount++;
		currentSchemeCount++;
		
		comm.message("Checking keepGoing after iteration "+totalCount, VerbLevel.verb_debug);
		
		if(!finalIters){ //Update one of the Non-Final schemes
			comm.message("Nonfinal", VerbLevel.verb_debug);
			updateNonFinal(shifts);
		} else{ //Update the Final scheme
			comm.message("Final", VerbLevel.verb_debug);
			comm.message("(shifts:"+shifts[0]+","+shifts[1]+","+shifts[2]+",...)", VerbLevel.verb_debug);
			updateFinal();
		}
		
		return !done;
	}
	
	
	private void updateNonFinal(double[] shifts){

		
		//Check if it should switch to "Final" scheme
		if( converged(shifts) || (totalCount>=maxIterations) ){ 
			//Set up for final iterations
			finalIters=true;
			currentSchemeCount = 0;
			inds2Update = defaultInds;
			if(totalCount>=maxIterations){
				comm.message("Passed maxIterations", VerbLevel.verb_debug);
			} else{
				comm.message("Converged (shifts:"+shifts[0]+","+shifts[1]+","+shifts[2]+",...)", VerbLevel.verb_debug);
			}
			
			
		} else if(updateAll){ 
			//Update the "All" scheme
			comm.message("--All (shifts:"+shifts[0]+","+shifts[1]+","+shifts[2]+",...)", VerbLevel.verb_debug);
			updateAll();
			
		} else { 
			//Update the "Top only" scheme
			comm.message("--Top "+inds2Update.length+" (shifts:"+shifts[0]+","+shifts[1]+","+shifts[2]+",...)", VerbLevel.verb_debug);
			updateTop(shifts);
			
		}
	}
	
	private boolean converged(double[] shifts){

		int i=0;
		while(i<shifts.length){
			if(shifts[i]>convThres){
				return false;
			}
			i++;
		}
		return true;
	}
	
	private void updateAll(){
		//Check if its time to switch to "Top only"
		if(currentSchemeCount>=allFramesIterations){
			updateAll=false;
			currentSchemeCount=0;
		}
		//Nothing else needs to be updated
	}
	
	private void updateTop(double[] shifts){
		//Check if its time to switch to "All"
		if(currentSchemeCount>=topOnlyIterations){
			updateAll=true;
			currentSchemeCount=0;
			inds2Update = defaultInds;
			
		} else {
			//Update the top inds
			setInds(shifts);
		}
	}	
	
	private void setInds(double[] shifts){
		
		//Count how many to put in the list
		int numAboveThresh = 0;;
		for (int i=0; i<shifts.length; i++) {
			if(shifts[i]>=convThres){
				numAboveThresh++;
			}
		}
		
		//Build the list
		inds2Update = new int[numAboveThresh];
		int j=0;
		for (int i=0; i<shifts.length; i++) {
			if(shifts[i]>=convThres){
				inds2Update[j] = i;
				j++;
			}
		}
		
		
	}
	
	private void updateFinal(){
		//Check if it's done
		if(currentSchemeCount>=finalIterations){
			done = true;
		}
		//Nothing else needs to be updated
	}

	
	
	public int[] inds2Update(){
		return inds2Update;
	}
	
}
