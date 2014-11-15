import ij.ImagePlus;
import ij.process.ImageProcessor;

import java.util.Vector;


public class ContourFinder {
	
	ImageProcessor imp;
	float[][] pixels;
	int ImgWidth, ImgHeight;
	
	Vector<Contour> contours;
	
	ContourFinder(ImageProcessor image){
		init(image);
	}
	
	void init(ImageProcessor image) {
		
		ImgWidth = image.getWidth();
		ImgHeight = image.getHeight();
		
		pixels = (float[][])image.getPixels();
		
		contours = new Vector<Contour>(); 
	}
	
	
	void findContours(){
		//
		final int DOWN=2, UP=1, RIGHT=2, LEFT=1, X=0, Y=1;
		int Direction;
		final int North=0, NorthEast=1, East=2, SouthEast=3, South=4,
				SouthWest=5, West=6, NorthWest=7, NumDirections=8;
		int Attitude;
		final int Horizontal=0, Vertical=1;
		//
		
		Terrain terrain = new Terrain(ImgWidth, ImgHeight);
		terrain.getTerrain(pixels);	
		
		int nSegments = 0;
		//int[] segmentStart, segmentEnd;
		
		//parameters
		//int MaxNsegments = 100;
		//float xu = .5f, yu = .5f;
		
		//int prevStart;
		
		
	}
    

	
}

//////////////////////////////////////////////////////////////////

class Contour {
	Vector<Double> x;
	Vector<Double> y;
	
	double areaInContour;
	
	boolean complete;
	
	void addContourPoint(double x, double y){
		this.x.add(x);
		this.y.add(y);
	}
	
	Contour(){
		x = new Vector<Double>();
		y = new Vector<Double>();
		complete = false;
	}
	
	
}

//////////////////////////////////////////////////////////////////

class Terrain {   /* shape of data */
	int[][] hori;           /* array: contour passes below? */
	int[][] vert;           /* array: contour passes to the left? */
	float[][] hy;            /* array: offset of contour below */
	float[][] vx;            /* array: offset of contour to the left */
	
	Terrain(int width, int height) {
		hori = new int[width+1][height+1];
		vert = new int[width+1][height+1];
		hy = new float[width+1][height+1];
		vx = new float[width+1][height+1];
		
		
	}
	
	void mapTerrain(){
		
	}
	
}

//////////////////////////////////////////////////////////////////

class Navigator {
	int attitude;         /* Vertical or Horizontal? */
	int direction;        /* direction of motion */
	int ii;               /* X coord of current point on path */
	int jj;               /* Y coord of current point on path */
	Navigator() {}
}


//////////////////////////////////////////////////////////////////

//For multiple contours or threshold levels... 
class ContourLevel{
	int threshold;
	Vector<Contour> contours;
	
	void addContour(Contour ctr){
		contours.add(ctr);
	}
	
	ContourLevel(int thr){
		threshold = thr;
		contours = new Vector<Contour>();
	}
	
}

//////////////////////////////////////////////////////////////////

class SliceContours{
	
	int sliceNum;
	Vector<ContourLevel> cLevels;
	
	SliceContours(int sliceN){
		sliceNum = sliceN;
		cLevels = new Vector<ContourLevel>(); 
	}
	
}