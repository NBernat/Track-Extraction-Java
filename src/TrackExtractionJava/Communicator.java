package TrackExtractionJava;

import java.io.File;
import java.io.PrintWriter;


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
	
	
	public void saveOutput(String dstDir, String fileName){
		if (!outString.equals("")){
			PrintWriter out;
			File f =new File(dstDir+fileName+".txt"); 
			try{
				if (!f.exists()) f.createNewFile();
				out = new PrintWriter(f);
				out.print(outString);
				
			} catch (Exception e){
				e.printStackTrace();
			}
			
		 }
	}
	
	
}


enum VerbLevel {
		verb_off, verb_error, verb_warning, verb_message, verb_verbose, verb_debug
	}
