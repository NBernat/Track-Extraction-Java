
public class Communicator {
	/**
	* Level determining which messages get displayed 
	*/
	VerbLevel verbosity;
	String outString;
	String lb;
	
	public Communicator(){
		verbosity = VerbLevel.verb_debug;
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
	

}


enum VerbLevel {
		verb_off, verb_error, verb_warning, verb_message, verb_verbose, verb_debug
	}
