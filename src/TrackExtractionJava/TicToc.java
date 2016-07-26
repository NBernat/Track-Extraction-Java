package TrackExtractionJava;

import java.io.IOException;
import java.io.Writer;
import java.lang.*;
import java.util.Hashtable;

public class TicToc {

	/* 
	 * File:   tictoc.cpp
	 * Author: Marc
	 * 
	 * Created on December 3, 2009, 12:07 PM
	 * (C) Marc Gershow; licensed under the Creative Commons Attribution Share Alike 3.0 United States License.
	 * To view a copy of this license, visit http://creativecommons.org/licenses/by-sa/3.0/us/ or send a letter to
	 * Creative Commons, 171 Second Street, Suite 300, San Francisco, California, 94105, USA.
	 */
	
	public static final int NOT_TICKED = -1;
	
	private int ncalls;
	private double starttime;
	private double totaltime;
	private double maxtime;
	private double mintime;
	private boolean ticked;
	private int numblowntics;
	private String name;
   
	public TicToc() {
    	this("anonymous");
    }
    public TicToc(String name) {
        this.name = name;
	    reset();
	}
    public void reset() {
    	ncalls = 0;
 	    starttime = 0;
 	    totaltime = 0;
 	    maxtime = 0;
 	    mintime = Integer.MAX_VALUE;
 	    ticked = false;
 	    numblowntics = 0;
    }
    
    public void tic() {
    	starttime = System.currentTimeMillis();
 	    if (ticked) {
 	        ++numblowntics;
 	    }
 	    ticked = true;
    }
    public double toc() {  	
	    if (!ticked) {
	        return NOT_TICKED; //toc called without tic
	    }
	    ++ncalls;
	    ticked = false;
	    double et = System.currentTimeMillis() - starttime;
	    maxtime = et > maxtime ? et : maxtime;
	    mintime = et < mintime ? et : mintime;
	    totaltime += et;
	    return et;
    }
	public void writeInfo (Writer w) throws IOException {
		w.append("name: " + name + "\n");
		w.append(" ncalls: " + ncalls + "\n");
		w.append(" totaltime: " + totaltime + "\n");
		w.append(" maxtime: " + maxtime + "\n");
		w.append(" mintime: " + mintime + "\n");
		w.append(" avg time: " + totaltime/ncalls + "\n");
		w.append(" num blown tics: " + numblowntics + "\n");
	}
	public int getNcalls() {
		return ncalls;
	}
	public double getTotaltime() {
		return totaltime;
	}
	public double getMaxtime() {
		return maxtime;
	}
	public double getMintime() {
		return mintime;
	}
	public int getNumblowntics() {
		return numblowntics;
	}
	public String getName() {
		return name;
	}
    
};

