
public class commtest {

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		
		VerbLevel verb1 = VerbLevel.verb_off;
		VerbLevel verb2 = VerbLevel.verb_debug;
		
		if (verb1.compareTo(verb2)>0){
			System.out.println("off greater than debug");
		} else if (verb1.compareTo(verb2)<0){
			System.out.println("off less than debug");
		}
		
		Communicator cm = new Communicator();
		cm.message("this is a message!", VerbLevel.verb_error);
		cm.message("this is another message!",VerbLevel.verb_verbose);

		int x = 1;
		int y = ++x;
		System.out.println("y: "+y);
		System.out.println("x: "+x);
		
	}

}
