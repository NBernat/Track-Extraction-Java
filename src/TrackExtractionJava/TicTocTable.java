package TrackExtractionJava;

import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;
import java.lang.*;
import java.util.Collections;
import java.util.Comparator;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Vector;

public class TicTocTable {

	/* 
	 * File:   tictoc.cpp
	 * Author: Marc
	 * 
	 * Created on December 3, 2009, 12:07 PM
	 * (C) Marc Gershow; licensed under the Creative Commons Attribution Share Alike 3.0 United States License.
	 * To view a copy of this license, visit http://creativecommons.org/licenses/by-sa/3.0/us/ or send a letter to
	 * Creative Commons, 171 Second Street, Suite 300, San Francisco, California, 94105, USA.
	 */
	
	public static final int NOT_FOUND = -10;
	
	private boolean enabled = true;
	private Hashtable<String, TicToc> timers;
	
	public TicTocTable() {
		timers = new Hashtable<String, TicToc>();
	}
	
	public void tic(String name) {
		tic(name, false);
	}
	
	public void tic(String name, boolean notick) {
	    if (!enabled || notick)
	        return;
	    
	    TicToc td = timers.get(name);
	    if (td == null) {
	    	td = new TicToc(name);
	    	timers.put(name, td);
	    }
	    td.tic();
	}

	public double toc(String name) {
		return toc(name, false);
	}
	
	public double toc(String name, boolean notock) {
	    if (!enabled || notock) {
	        return 0;
	    } TicToc td = timers.get(name);
	    if (td == null) {
	        return NOT_FOUND; //key not found
	    }
	    return td.toc();
	}
	 public double getElapsedTime (String name) {
		TicToc td = timers.get(name);
	    if (td == null) {
	        return NOT_FOUND; //key not found
	    }
	    return td.getElapsedTime();
	 }

	 public void generateReport(Writer w) throws IOException {
	    Vector<TicToc> v = new Vector<TicToc>();
		for (Enumeration<TicToc> e = timers.elements(); e.hasMoreElements();){
			v.add(e.nextElement());
		}
		Collections.sort(v,new alphaSorter());
		for (TicToc t : v) {
			w.append("--\n");
			t.writeInfo(w);
		}

	}
	public String generateReport () {
		StringWriter sw = new StringWriter();
		try {
			generateReport(sw);
		} catch (IOException e) {
			return e.toString();
		}
		return sw.toString();
	}
	
	public TicToc getTicToc (String name) {
		return timers.get(name);
	}
	
	public void removeAllTimers() {
		timers.clear();
	}
	
	public void remove(String name) {
		timers.remove(name);
	}
	
	public void reset(String name) {
		TicToc td = timers.get(name);
	    if (td == null) {
	    	td = new TicToc(name);
	    	timers.put(name, td);
	    }
	    td.reset();
	}
	
	public void resetAllTimers() {
		for (Enumeration<TicToc> e = timers.elements(); e.hasMoreElements();){
			e.nextElement().reset();
		}
	}
	
	public void enable() {
		enabled = true;
	}
	public void disable() {
		enabled = false;
	}

}

class alphaSorter implements Comparator<TicToc> {

	@Override
	public int compare(TicToc o1, TicToc o2) {
		return o1.getName().compareToIgnoreCase(o2.getName());
	}
	
}

class totalTimeSorter implements Comparator<TicToc> {

	@Override
	public int compare(TicToc o1, TicToc o2) {
		return Double.compare(o1.getTotaltime(), o2.getTotaltime());
	}
	
}
    


