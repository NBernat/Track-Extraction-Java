package TrackExtractionJava;

import java.util.Vector;

import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.table.AbstractTableModel;


public class FittingParameters {
	
	int GCInterval = 5;
	
	public int numBBPts = 7;
	
	int minTrackLen = 200;//NOTE: SHOULD BE SAME AS PROCESSINGPARAMS.MINTRACKLEN
	boolean subset = false;
	int startInd = 0;
	int endInd = 1000;
	
	int numFinalSingleIterations = 5;
	
	boolean storeEnergies = false;
//	boolean storeCommOutput = false;

	
	/**
	 * Number of points on either side of divergence event to freeze & include when trying to fix a divergence event  
	 */
	int divBufferSize = 50; //~7seconds of buffer
	
	double fracOfStdDevForBentCutoff = 0.5;
	
	String energyTypeForBadGap = "Time-Length";
	int numStdDevForBadGap = 5;
	int edgeSize = 1; //number of frames in the "edge" when fitting by inching inwards
	
	/*
	 * 0= voronoi clusters 
	 * 1= gaussian mixture
	 */
	int clusterMethod = 0;
	
	public int[] grains = {1}; 
	public int smallGapMaxLen = 5;//The maximum gap length for which the previous midline will be carried forward (otherwise interpolate)
	public int minValidSegmentLen = 20;//The minimum segment length (in frames) which is situated between two midline gaps and which is considered valid
	//TODO: distances need to know about distance
	// the typical lengh of a maggot ~15-20 pixels; midline has 11 pts; reversal ~11*15/2 = 80
	public double minFlickerDist = 40; //was numBBPts 7/22 //The minimum distance between spines which indicates an erroneous midline flicker 
	public int gapDilation = 2;
	public boolean dilateToEdges = true;
	
	
	
	public int divergenceConstant = 1;//
	
	public float imageWeight = 1.0f;
	public float spineLengthWeight = 0.4f;
	public float spineSmoothWeight = 0.8f;
	public float[] timeLengthWeight = {0.1f};
	public float[] timeSmoothWeight = {0}; 
	
	//Head=0, Tail=end
	public float[] imageWeights = {0.70f,0.75f,0.8f, 0.85f,1,1, 1};
	public float[] spineLengthWeights = {.5f,1,1, 1,1,1, 1};
	public float[] spineSmoothWeights = {.6f,1,1, 1,1,1, 1};
	public float[][] timeLengthWeights = { {1,1,1, 1,1,1, 1} };
	public float[][] timeSmoothWeights = { {1,1,1, 1,1,1, 1} };
	
	
	/**
	 * Refits the segments of a diverged track surrounding the divergence event
	 */
	boolean refitDiverged = false;
	
	/**
	 * Tries to mend the divergence event 
	 */
	boolean fixDiverged = false;
	
	boolean leaveBackbonesInPlace = false;
	boolean leaveFrozenBackbonesAlone = false;
	
	boolean freezeDiverged = false;
	
	int divergedPatchBuffer = grains[0]*4;
	
	
	
	
	fittingParamTableModel fpTableModel;
	
	JPanel fpPanel;
	
	public FittingParameters(){	}
	

	public static FittingParameters getSinglePassParams(){
		return new FittingParameters();
	}
	
	public static FittingParameters getMultiPassParams(){
		
		FittingParameters fp = new FittingParameters();
			
		
		int[] gs = {32,16, 1}; 
		fp.grains = gs;
		
		float[] tlw = {0.3f, 0.1f, 0.1f};
		float[] tsw = {0.3f, 0.1f, 0.1f}; 
		fp.timeLengthWeight = tlw;
		fp.timeSmoothWeight = tsw;
		
		
		float[][] tlws = { {1,1,1, 1,1,1, 1},
							{1,1,1, 1,1,1, 1},
							{1,1,1, 1,1,1, 1} };
		float[][] tsws = { {1,1,1, 1,1,1, 1},
							{1,1,1, 1,1,1, 1},
							{1,1,1, 1,1,1, 1} };
		fp.timeLengthWeights = tlws;
		fp.timeSmoothWeights = tsws;
		
		
		return fp;
		
		
	}
	
	public boolean isFirstPass(int grain){
		return grain==grains[0];
	}
	
	public float timeLengthWeight(int pass){
		if (pass>=timeLengthWeight.length) {
			return timeLengthWeight[timeLengthWeight.length-1];
		} else {
			return timeLengthWeight[pass]; 
		}
		
//		if (pass==0) return timeLengthWeight[0]; else return timeLengthWeight[1];
	}
	
	public float timeSmoothWeight(int pass){
		if (pass>=timeSmoothWeight.length) {
			return timeSmoothWeight[timeSmoothWeight.length-1];
		} else {
			return timeSmoothWeight[pass]; 
		}
//		if (pass==0) return timeSmoothWeight[0]; else return timeSmoothWeight[1];
	}
	
	
	public Vector<Force> getForces(int pass) {
		
		Vector<Force> Forces = new Vector<Force>();
		Forces.add(new ImageForce(imageWeights, imageWeight));
		Forces.add(new SpineLengthForce(spineLengthWeights,
				spineLengthWeight));
		Forces.add(new SpineSmoothForce(spineSmoothWeights,
				spineSmoothWeight));
		
		
		float[] tlWeights = new float[numBBPts];
		float[] tsWeights = new float[numBBPts];
		for (int i=0; i<numBBPts; i++){
			tlWeights[i] = timeLengthWeights[pass][i];
			tsWeights[i] = timeSmoothWeights[pass][i];
		}
		
		Forces.add(new TimeLengthForce(tlWeights,
				timeLengthWeight(pass)));
		Forces.add(new TimeSmoothForce(tsWeights,
				timeSmoothWeight(pass)));
		return Forces;
	}
	
	public int getNumWeights(){
		return 3 + 2*grains.length;
	}
	
	public JTable getTable(){
		
		if (fpTableModel!=null || fpTableModel.numGrains!=grains.length){
			fpTableModel = new fittingParamTableModel(this);
		}
		return new JTable(fpTableModel);
	}
	

	private void makePanel(){
		
	}
	
	public JPanel getPanel(){
		if (fpPanel==null){
			makePanel();
		}
		return fpPanel;
	}
	
	
}




class fittingParamTableModel extends AbstractTableModel {
	
	
    /**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	int numGrains;
	FittingParameters fp;
	String[] columnNames;
	String[] rowNames;
	
	public fittingParamTableModel(FittingParameters fp){
		this.fp = fp;
		numGrains = fp.grains.length;
		setRowNames();
		setColumnNames();
	}
	
	private void setRowNames(){
		rowNames = new String[fp.getNumWeights()];
		rowNames[0] = "Image";
		rowNames[1] = "Spine Length";
		rowNames[2] = "Spine Smooth";
		int tlStart = 3;
		int nGrains = numGrains;
		for (int i=0; i<nGrains; i++){
			rowNames[tlStart+i] = "Time Length g"+fp.grains[i];
			rowNames[tlStart+nGrains+i] = "Time Smooth g"+fp.grains[i];
		}
	}
	private void setColumnNames(){
		columnNames = new String[2+fp.numBBPts];
		
		columnNames[0] = "Energy Term";
		columnNames[1] = "Energy Weight";
		for (int i=0; i<fp.numBBPts; i++){
			if (i==0){
				columnNames[2+i] = "Head Weight";
			} else if (i==(fp.numBBPts-1)){
				columnNames[2+i] = "Tail Weight";
			} else {
				columnNames[2+i] = "Backbone Coord "+i+" Weight";
			}
			
		}
		
		
	}
	
	public String getColumnName(int col) {
        return columnNames[col];
    }
    public int getRowCount() { 
    	return rowNames.length; 
	}
    public int getColumnCount() { 
    	return columnNames.length; 
	}
    public Object getValueAt(int row, int col) {
        
    	//TODO
    	
    	return null;
    }
    public boolean isCellEditable(int row, int col){ 
    	//TODO
    	return true; 
    }
    public void setValueAt(Object value, int row, int col) {
    	//TODO
        ///rowData[row][col] = value;
        fireTableCellUpdated(row, col);
    }
}