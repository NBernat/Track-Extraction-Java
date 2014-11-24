import ij.ImagePlus;
import ij.measure.ResultsTable;
import ij.plugin.filter.GaussianBlur;
import ij.plugin.filter.ParticleAnalyzer;
import ij.process.ImageProcessor;


public class CVUtils {

	//rethreshold to a specified number of regions
	
	//TODO
	//http://docs.opencv.org/modules/imgproc/doc/miscellaneous_transformations.html
	static ImagePlus thresholdImtoZero(ImagePlus image, double globalThreshValue) {
		//clone image
		ImagePlus cloneIm = (ImagePlus) image.clone();
		//threshold clone 
		int max = 255;
		
		//return clone
		return cloneIm;
	}
	
	//TODO
	//http://docs.opencv.org/modules/core/doc/old_basic_structures.html
	static ImagePlus maskCopy(ImagePlus image, ImagePlus mask){
		
		ImagePlus maskdIm = (ImagePlus) image.clone();
		
		return maskdIm;
	}
	
	
	//TODO
	static ImageProcessor blurIm(ImageProcessor image, double sigma){
		
		ImageProcessor cloneIm = (ImageProcessor) image.clone();
		GaussianBlur GB = new GaussianBlur();
        GB.blurGaussian(image, sigma, sigma, .02);
        return cloneIm;
	}
	
	//TODO
	static ImagePlus blurIm(ImagePlus image, double sigma) {
		ImageProcessor cloneIm = ((ImagePlus)image.clone()).getProcessor();
		return new ImagePlus(image.getTitle(), blurIm(cloneIm,sigma));
	}
	
	
	//TODO
	//http://docs.opencv.org/modules/core/doc/operations_on_arrays.html
	static ImagePlus compGE(ImagePlus threshIm, ImagePlus threshCompIm){
		ImagePlus compdIm = null;
		
		return compdIm;
		
	}
	
	//TODO 
	//http://rsb.info.nih.gov/ij/developer/api/ij/measure/ResultsTable.html#ResultsTable()
	//http://rsb.info.nih.gov/ij/developer/api/index.html?ij/plugin/filter/ParticleAnalyzer.html
	//get:
//	double x = rt.getValue("X", row);
//	double y = rt.getValue("Y", row);
//	double boundX = rt.getValue("BX", row);
//	double boundY = rt.getValue("BY", row);
//	double width = rt.getValue("Width", row);
//	double height = rt.getValue("Height", row);
//	double area = rt.getValue("Area", row);
	static ResultsTable findPoints(ImagePlus image, ExtractionParameters ep) {
		
		ResultsTable rt = new ResultsTable();
		
		ParticleAnalyzer pa = new ParticleAnalyzer(); 
		
		return rt;
	}
	
}
