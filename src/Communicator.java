
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
