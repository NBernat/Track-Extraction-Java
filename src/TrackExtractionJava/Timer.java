package TrackExtractionJava;

import java.io.IOException;
import java.io.Writer;

class Timer {
	private static final TicTocTable tt = new TicTocTable();
	
	public static void tic(String name, boolean notick) {
	    tt.tic(name, notick);
	}

	public static double toc(String name, boolean notock) {
	   return tt.toc(name, notock);
	}


	 public static void generateReport(Writer w) throws IOException {
	    tt.generateReport(w);
	}
	public static String generateReport () {
		return tt.generateReport();
	}
	
	public static TicToc getTicToc (String name) {
		return tt.getTicToc(name);
	}
	
	public static void removeAllTimers() {
		tt.removeAllTimers();
	}
	
	public static void remove(String name) {
		tt.remove(name);
	}
	
	public static void reset(String name) {
		tt.reset(name);
	}
	
	public static void resetAllTimers() {
		tt.resetAllTimers();
	}
	
}
