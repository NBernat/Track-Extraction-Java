import java.awt.Point;


public class ContourPoint extends Point implements Comparable<ContourPoint> {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	
	int x;
	int y;
	
	ContourPoint prevPt;
	ContourPoint nextPt;
	
	double angle;
	
	boolean htCand;
	
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
	
	
}
