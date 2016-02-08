package TrackExtractionJava;

import java.util.Vector;

import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.table.AbstractTableModel;


public class FittingParameters {
	
	int GCInterval = 5;
	
	public int numBBPts = 9;
	
	public int[] grains = {32,16, 1}; //{32,16,1};//edit 17 {32, 16, 8, 1}, edit18 {32, 16, 4, 1}, edit 19 {32, 8, 1}, edit 20 {32, 16, 2, 1} edit 23, edit 24 {8,4, 1}
	public int smallGapMaxLen = 10;//The maximum gap length for which the previous midline will be carried forward (otherwise interpolate)
	public int minValidSegmentLen = 5;//The minimum segment length (in frames) which is situated between two midline gaps and which is considered valid
	public double minFlickerDist = numBBPts;//The minimum distance between spines which indicates an erroneous midline flicker 
	public int gapDilation = 5;
	public boolean dilateToEdges = true;
	
	
	
	public int divergenceConstant = 1;//
	
	public float imageWeight = 1.0f;
	public float spineLengthWeight = .5f;//.6f;// edit 21 to .4, edit 22 to .5
	public float spineSmoothWeight = 1.0f;//.8f;//edit 1 up to 1.0//edit 15 back to .8 (also timelength), edit 16 back to 1.0(also timelength)
	public float[] timeLengthWeight = {3.0f, .3f, .3f};//{3.0f, 0.1f};//edit 2 (swap back, 5)(upped to .5, 6)(back down to .2, 9)(14 up to .3) (15 back to .2, also spinesmooth)(16 back to .3, also spinesmooth)
	public float[] timeSmoothWeight = {1.0f, 0.5f, 0.5f};//{.1f, .1f};//edit 7 (1.0,0.1), edit 8(1.0, 0.3), edit 10(upped to (1.0,1.0)), 11 (10., .5) ) 
	
	//Head=0, Tail=end
	public float[] imageWeights = {1,1,1, 1,1,1, 1,1,1};
	public float[] spineLengthWeights = {.7f,1,1, 1,1,1, 1,1,1};//{.1f,1,1, 1,1,1, 1,1,1};//{0,1,1, 1,1,1, 1,1,1};//edit 3,4
	public float[] spineSmoothWeights = {.8f,1,1, 1,1,1, 1,1,1};//{.1f,1,1, 1,1,1, 1,1,1};//edit 12 (head to .5), edit 13 (head to .8)
	public float[][] timeLengthWeights = { {1,1,1, 1,1,1, 1,1,1},
									{1,1,1, 1,1,1, 1,1,1},
									{1,1,1, 1,1,1, 1,1,1}	};
	public float[][] timeSmoothWeights = { {1,1,1, 1,1,1, 1,1,1},
									{1,1,1, 1,1,1, 1,1,1},
									{1,1,1, 1,1,1, 1,1,1}	};
	
	fittingParamTableModel fpTableModel;
	
	JPanel fpPanel;
	
	public FittingParameters(){
		//TODO
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