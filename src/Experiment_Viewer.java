import ij.IJ;
import ij.io.OpenDialog;
import ij.plugin.PlugIn;
import ij.text.TextWindow;


public class Experiment_Viewer implements PlugIn{

	private Experiment ex;
	private ExperimentFrame exFrame;
	
	
	
	
	public void run(String arg) {
		
		IJ.showStatus("Getting experiment");
		getExperiment();
		if (ex==null){
			IJ.showStatus("Experiment was not opened");
			return;
		}
		
		IJ.showStatus("Modifying experiment...");
		modifyExperiment();
		
		IJ.showStatus("Making experiment frame");
		exFrame = new ExperimentFrame(ex);
		IJ.showStatus("Experiment shown in frame");
		exFrame.run(null);
		
	}
	
	
	private void getExperiment(){
		if (ex==null||ex.tracks==null||ex.tracks.size()==0){
			//open a browser box]
			OpenDialog od = new OpenDialog("Choose a .ser file containing an experiment", null);
			
			String fileName = od.getFileName();
			
			String dir = od.getDirectory();
			if (null == dir) return ; // dialog was canceled
			dir = dir.replace('\\', '/'); // Windows safe
			if (!dir.endsWith("/")) dir += "/";
			
			String path = dir + fileName;
			
			//try to open file
			try {
				IJ.showStatus("Opening the experiment file");
				ex = Experiment.open(path); 
			} catch (Exception e){
				new TextWindow("Error opening experiment", e.getMessage(), 500, 500);
			}
		}
			
		
	}
	
	//This is a TEMPORARY method to make life a little easier during development of the backbone fitter
	private void modifyExperiment(){
		IJ.showStatus("Creating Backbone Fitter");
		BackboneFitter bbf = new BackboneFitter();
		
		int index = ex.getTrack(4);
		if (index>=0){
			Track tr = ex.tracks.get(index);
//			for (int i=60; i<65; i++){
//				MaggotTrackPoint mtp = (MaggotTrackPoint)tr.points.get(i);  
//				mtp.midline=null;
//			}
			IJ.showStatus("Fitting Track");
			bbf.fitTrack(tr);
			ex.tracks.addElement(tr);
			
			Track bbTrack = new Track(bbf.BTPs);
	//		testInterpolator(bbf);
			
			IJ.showStatus("Track fit!");
			ex.tracks.add(bbTrack);
		}
		
		
	}
	
	/*
	private void testInterpolator(BackboneFitter bbf){
		
		int firstInd = 46;
		int lastInd = 55;
		
		
		int firstBTP = firstInd;
		int endBTP = lastInd;
		Vector<BackboneTrackPoint> BTPs = bbf.BTPs;
		
		FloatPolygon bbFirst = BTPs.get(firstBTP).midline.getFloatPolygon();
		FloatPolygon bbEnd = BTPs.get(endBTP).midline.getFloatPolygon();
		int numbbpts = bbFirst.npoints;
		float[] xbbfirst= new float[numbbpts];
		float[] ybbfirst= new float[numbbpts];
		float[] xbbend= new float[numbbpts];
		float[] ybbend= new float[numbbpts];
		for (int i=0; i<numbbpts; i++){//Get the absolute coordinates
			xbbfirst[i] = bbFirst.xpoints[i]+BTPs.get(firstBTP).rect.x;
			ybbfirst[i] = bbFirst.ypoints[i]+BTPs.get(firstBTP).rect.y;
			xbbend[i] = bbEnd.xpoints[i]+BTPs.get(endBTP).rect.x;
			ybbend[i] = bbEnd.ypoints[i]+BTPs.get(endBTP).rect.y;
		}
		
		int numnewbbs = endBTP - firstBTP - 1;
		float start;
		float end;
		
		// Find the initial shifts, aka origins of rotation
		float[] xorigins = new float[numnewbbs];
		float[] yorigins = new float[numnewbbs];
		start = xbbfirst[numbbpts-1];//tail of the first maggot
		end = xbbend[numbbpts-1];//tail of the second maggot
		xorigins = CVUtils.interp1D(start, end, xorigins.length);
		start = ybbfirst[numbbpts-1];//tail of the first maggot
		end = ybbend[numbbpts-1];//tail of the second maggot
		yorigins = CVUtils.interp1D(start, end, yorigins.length);
		
		//Shift both maggots so that their tails are at the origin
		for (int i=0; i<numbbpts; i++){
			xbbfirst[i] = xbbfirst[i]-xbbfirst[numbbpts-1];
			ybbfirst[i] = ybbfirst[i]-ybbfirst[numbbpts-1];

			xbbend[i] = xbbend[i]-xbbend[numbbpts-1];
			ybbend[i] = ybbend[i]-ybbend[numbbpts-1];
		}
		
		ImageProcessor plot = CVUtils.plot(null, xbbfirst, ybbfirst, Color.RED);
		plot = CVUtils.plot(plot, xbbend, ybbend, Color.GREEN);
		
		
		// Find the angles of rotations; rotate the bbFirst/End 
		float[] angles = new float[numnewbbs];
		start = (float)Math.atan2(ybbfirst[0], xbbfirst[0]);
		end = (float)Math.atan2(ybbend[0], xbbend[0]);
		float dif = (float)((end-start+2*Math.PI)%(2*Math.PI));
		if( ((start-end+2*Math.PI)%(2*Math.PI))<dif ) dif = (float)((start-end+2*Math.PI)%(2*Math.PI));
		dif = dif/(numnewbbs+1);
		for(int j=0; j<angles.length; j++) angles[j]=start+dif*(j+1);
		
		//Rotate both maggots so that their heads are on the x axis
		float[] newCoord;
		for(int i=0; i<numbbpts; i++){
			newCoord = CVUtils.rotateCoord(xbbfirst[i], ybbfirst[i], -start);
			xbbfirst[i] = newCoord[0];
			ybbfirst[i] = newCoord[1];
			
			newCoord = CVUtils.rotateCoord(xbbend[i], ybbend[i], -end);
			xbbend[i] = newCoord[0];
			ybbend[i] = newCoord[1];
		}
		
		
		plot = CVUtils.plot(plot, xbbfirst, ybbfirst, Color.PINK);
		plot = CVUtils.plot(plot, xbbend, ybbend, Color.CYAN);
		
		
		
		//Generate the new midline coords
		Vector<float[]> xnewbbs = new Vector<float[]>();
		Vector<float[]> ynewbbs = new Vector<float[]>();
		for (int j=0; j<numnewbbs; j++){
			xnewbbs.add(new float[numbbpts]);
			ynewbbs.add(new float[numbbpts]);
		}
		
		
		// Find the initial coords by interpolating between the shifted, rotated, initial backbones
		for (int i=0; i<numbbpts; i++){
			float[] xsubi = CVUtils.interp1D(xbbfirst[i], xbbend[i], numnewbbs);
			float[] ysubi = CVUtils.interp1D(ybbfirst[i], ybbend[i], numnewbbs);
			for (int j=0; j<numnewbbs;j++){//for each j'th backbone, fill in the i'th coordinate
				xnewbbs.get(j)[i] = xsubi[j];
				ynewbbs.get(j)[i] = ysubi[j];
			}
		}
		
		
		for (int i=0; i<xnewbbs.size(); i++){
			plot = CVUtils.plot(plot, xnewbbs.get(i), ynewbbs.get(i), Color.YELLOW);
		}
		
		
		
		
		// Perform the back rotations and shifts
		for (int j=0; j<numnewbbs; j++){
			for (int i=0; i<numbbpts; i++){
				float[] newCrds = CVUtils.rotateCoord(xnewbbs.get(j)[i], ynewbbs.get(j)[i], angles[j]);
				xnewbbs.get(j)[i] = newCrds[0]+xorigins[j]-760;
				ynewbbs.get(j)[i] = newCrds[1]+yorigins[j]-910;
			}
		}
		
		
		String q = "FIRST: ";for (int j=0; j<bbFirst.npoints; j++) q+="("+(bbFirst.xpoints[j]+BTPs.get(firstBTP).rect.x)+","+(bbFirst.ypoints[j]+BTPs.get(firstBTP).rect.y)+")";q+="\n";
		for (int i=0; i<xnewbbs.size(); i++){
			plot = CVUtils.plot(plot, xnewbbs.get(i), ynewbbs.get(i), Color.BLUE);
			for (int j=0; j<xnewbbs.get(i).length; j++) q+="("+(xnewbbs.get(i)[j]+760)+","+(ynewbbs.get(i)[j]+910)+")";
			q+="\n";
		}
		q += "LAST: ";for (int j=0; j<bbEnd.npoints; j++) q+="("+(bbEnd.xpoints[j]+BTPs.get(endBTP).rect.x)+","+(bbEnd.ypoints[j]+BTPs.get(endBTP).rect.y)+")";q+="\n";
		new TextWindow("Manually Interpolated backbones", q, 500, 500);
		ImagePlus imp = new ImagePlus("Interp ", plot);
//		imp.show();
		
		
		
		
		
		
		
		
		
		
		
		

		Vector<FloatPolygon> mids = bbf.interpBackbones(firstInd, lastInd);
		float[] prevOrigin = {0.0f, 0.0f};
		Vector<PolygonRoi> newMidlines = new Vector<PolygonRoi>();
		
		String s = "";
		for (int i=0; i<mids.size(); i++){
			FloatPolygon newMid = mids.get(i);
			FloatPolygon oldMid = bbf.BTPs.get(i+1).midline.getFloatPolygon();
			float[] xmid = new float[newMid.npoints];
			float[] ymid = new float[newMid.npoints];
			float offX = prevOrigin[0]-bbf.BTPs.get(i+firstBTP+1).rect.x;
			float offY = prevOrigin[1]-bbf.BTPs.get(i+endBTP+1).rect.y;
			
			s+=i+" Offset=("+offX+","+offY+")\n";
			s+="New: ";
			
			for(int j=0; j<newMid.npoints; j++){
				xmid[j] = newMid.xpoints[j]+offX;
				ymid[j] = newMid.ypoints[j]+offY;
				s+= "("+(xmid[j]-offX)+","+(ymid[j]-offY)+") ";
			}
			newMidlines.add(new PolygonRoi(new FloatPolygon(xmid, ymid), PolygonRoi.POLYLINE));
			s+="\n Old:";
			for (int j=0; j<oldMid.npoints; j++){
				xmid[j] = oldMid.xpoints[j]+bbf.BTPs.get(i+firstBTP+1).rect.x;
				ymid[j] = oldMid.ypoints[j]+bbf.BTPs.get(i+firstBTP+1).rect.y;
				s+= "("+xmid[j]+","+ymid[j]+") ";
			}
			s+="\n";
		}
		new TextWindow("Interpolated backbones", s, 500, 500);
		
		boolean clusters = false;
		boolean mid = true;
		boolean initialBB = false; 
		boolean contour = false;
		boolean ht = false;
		boolean forces = false;
		boolean backbone = false;
		
		ImageProcessor firstIm = bbf.BTPs.get(firstInd).getIm(clusters, mid, initialBB, contour, ht, forces, backbone);
		ImageStack trackStack = new ImageStack(firstIm.getWidth(), firstIm.getHeight());
		trackStack.addSlice(firstIm);
		for (int i=(firstInd+1); i<=lastInd; i++){ 
			ImageProcessor im = bbf.BTPs.get(i).getIm(clusters, mid, initialBB, contour, ht, forces, backbone);
//			float[] o = {0.0f, 0.0f};
//			int expandFac = 10;
//			int offX = bbf.BTPs.get(i).trackWindowWidth*(expandFac/2) - ((int)bbf.BTPs.get(i).x-bbf.BTPs.get(i).rect.x)*expandFac;//rect.x-imOriginX;
//			int offY = bbf.BTPs.get(i).trackWindowHeight*(expandFac/2) - ((int)bbf.BTPs.get(i).y-bbf.BTPs.get(i).rect.y)*expandFac;//rect.y-imOriginY;
//			if (i<lastInd) displayUtils.drawMidline(im, new PolygonRoi(mids.get(i-1), Roi.POLYLINE), offX, offY, 10, Color.RED);
			
			
			if (i<lastInd) im = bbf.BTPs.get(i).getImWithMidline(newMidlines.get(i-firstInd-1));
		
			trackStack.addSlice(im);
		}
		ImagePlus trackPlus = new ImagePlus("BTPs",trackStack);
//		trackPlus.show();
		
		//getIm(boolean clusters, boolean mid, boolean initialBB, boolean contour, boolean ht, boolean forces, boolean backbone){
		
		
	}
	*/
	

}
