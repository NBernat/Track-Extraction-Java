
public class ContourPoint implements Comparable<ContourPoint> {
	
	double x;
	double y;
	
	ContourPoint prevPt;
	ContourPoint nextPt;
	
	double angle;
	
	boolean inCvxHull;
	
	public ContourPoint(double x, double y){
		this.x = x;
		this.y = y;
		angle=Double.POSITIVE_INFINITY;
	}
	
	public void setPrev(ContourPoint prevPt){
		this.prevPt = prevPt;
	}

	
	public void setNext(ContourPoint nextPt){
		this.nextPt = nextPt;
	}
	
	
	
	
	
	
	public void measureAngle(){
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
	
	
	
	
	
	
	public int compareTo(ContourPoint otherPt){
		
		if (this.angle==Double.POSITIVE_INFINITY || otherPt.angle==Double.POSITIVE_INFINITY){
			throw new NullPointerException();
		}
		
		if (Math.abs(angle)==Math.abs(otherPt.angle)){
			return 0;
		}
		
		if (Math.abs(angle)-Math.abs(otherPt.angle)>0){
			return 1;
		} else {
			return -1;
		}
			
		
		
		
	}
	
	
}
