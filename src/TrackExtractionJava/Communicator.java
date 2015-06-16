package TrackExtractionJava;


public class Communicator {
	/**
	* Level determining which messages get displayed 
	*/
	VerbLevel verbosity;
	String outString;
	String lb;
	
	public Communicator(){
		verbosity = VerbLevel.verb_off;
		outString = "";
		lb = System.getProperty("line.separator");
	}
	
	public void message(String message, VerbLevel messVerb){
		if (messVerb==null){
			return;
		} else if (messVerb.compareTo(verbosity) <=0 ){
			outString += messVerb.toString()+": "+message+lb;
			//System.out.println(messVerb.toString()+": "+message);
		}
	}
	
	public void setVerbosity(VerbLevel verbosity){
		this.verbosity = verbosity;
	}
	
	public String toString(){
		
		return "Communicator at verbosity level "+verbosity.name()+lb+outString;
	}
	

}


enum VerbLevel {
		verb_off, verb_error, verb_warning, verb_message, verb_verbose, verb_debug
	}
