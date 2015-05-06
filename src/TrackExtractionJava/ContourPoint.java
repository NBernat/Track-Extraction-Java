package TrackExtractionJava;

import java.awt.Point;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.PrintWriter;
import java.io.StringWriter;


public class ContourPoint extends Point implements Comparable<ContourPoint> {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	
//	int x;
//	int y;
	
	public ContourPoint prevPt;
	public ContourPoint nextPt;
	
	public double angle;
	
	public boolean htCand;
	
	public ContourPoint(){
		
	}
	
	public ContourPoint(int x, int y){
		this.x = x;
		this.y = y;
		angle=java.lang.Double.POSITIVE_INFINITY;
	}

	public ContourPoint(double  x, double y){
		this.x = (int)x;
		this.y = (int)y;
		angle=java.lang.Double.POSITIVE_INFINITY;
	}
		
	public void measureAngle(ContourPoint prevPt, ContourPoint nextPt){
		if (prevPt==null || nextPt==null){
			return;
		}
		
		angle = Math.acos(((x - prevPt.x)*(x - nextPt.x) + (y - prevPt.y)*(y - nextPt.y))/(dist(prevPt)*dist(nextPt)));
		
	}
	
	public double distSquared(ContourPoint pt2){
		return (pt2.x-x)*(pt2.x-x) + (pt2.y-y)*(pt2.y-y);
	}
	
	
	public double dist(ContourPoint pt2){
		return Math.sqrt(distSquared(pt2));
	}
	
	
	
	
	public boolean equals(ContourPoint cp){
		
		return (x==cp.x && y==cp.y);
		
	}
	
	public int compareTo(ContourPoint otherPt){
		
		if (this.angle==java.lang.Double.POSITIVE_INFINITY || otherPt.angle==java.lang.Double.POSITIVE_INFINITY){
			throw new NullPointerException();
		}
		
		if (Math.abs(angle)==Math.abs(otherPt.angle) ){
			return 0;
		}
		
		if (Math.abs(angle)-Math.abs(otherPt.angle)>0){
			return 1;
		} else {
			return -1;
		}
		
	}
	
	
	public void setPrev(ContourPoint prevPt){
		this.prevPt = prevPt;
	}

	public void setNext(ContourPoint nextPt){
		this.nextPt = nextPt;
	}
	
	public void sethtCand(boolean cand){
		htCand = cand;
	}
	
	public int toDisk(DataOutputStream dos, PrintWriter pw){
		
		//Write info
		try {
			dos.writeInt(x);
			dos.writeInt(y);
		} catch (Exception e) {
			StringWriter sw = new StringWriter();
			PrintWriter prw = new PrintWriter(sw);
			e.printStackTrace(prw);
			if (pw!=null) pw.println("Error writing ContourPoint Info:\n"+sw.toString());
			return 1;
		}
		
		return 0;
	}
	
	public static int sizeOnDisk(){
		return 2*Integer.SIZE/Byte.SIZE;
	}
	
	public static ContourPoint fromDisk(DataInputStream dis){
		ContourPoint cp = new ContourPoint();
		if (cp.loadFromDisk(dis)==0){
			return cp;
		} else {
			return null;
		}
	}
	public static ContourPoint fromDisk(DataInputStream dis, PrintWriter pw){
		
		ContourPoint cp = new ContourPoint();
		if (cp.loadFromDisk(dis, pw)==0){
			return cp;
		} else {
			return null;
		}
	}
	
	protected int loadFromDisk(DataInputStream dis){
		
		//read new data: image
		try {
			x = dis.readInt();
			y = dis.readInt();
			angle=java.lang.Double.POSITIVE_INFINITY;
			
		} catch (Exception e) {
			return 1;
		}
		
		return 0;
	}
	

	protected int loadFromDisk(DataInputStream dis, PrintWriter pw){
		
		//read new data: image
		try {
			x = dis.readInt();
		} catch (Exception e) {
			StringWriter sw = new StringWriter();
			PrintWriter prw = new PrintWriter(sw);
			e.printStackTrace(prw);
			pw.println("X coord unreadable: "+sw.toString());
			return 1;
		}
		try{
			y = dis.readInt();
		} catch (Exception e) {
			StringWriter sw = new StringWriter();
			PrintWriter prw = new PrintWriter(sw);
			e.printStackTrace(prw);
			pw.println("Y coord unreadable: "+sw.toString());
			return 1;
		}
			angle=java.lang.Double.POSITIVE_INFINITY;
			
		
		
		return 0;
	}
	
}
