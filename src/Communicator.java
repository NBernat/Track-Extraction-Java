
public class Communicator {
	/**
	* Level determining which messages get displayed 
	*/
	VerbLevel verbosity;
	
	public Communicator(){
		verbosity = VerbLevel.verb_message;
	}
	
	public void message(String message, VerbLevel messVerb){
		if (messVerb==null){
			return;
		} else if (messVerb.compareTo(verbosity) <=0 ){
			System.out.println(messVerb.toString()+": "+message);
		}
	}
	

}


enum VerbLevel {
		verb_off, verb_error, verb_warning, verb_message, verb_verbose, verb_debug
	}
