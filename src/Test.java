import ij.ImagePlus;
import ij.process.ImageProcessor;


public class Test {

	public static void main(String[] args) {
		ImagePlus image = new ImagePlus("C:/Users/Natalie/Downloads/FullSizeRender.jpg");
		
		System.out.println("Old image: Size "+image.getWidth()+"x"+image.getHeight());
		image.setRoi(886, 522, 1070, 720);
		ImageProcessor cropIm = image.getProcessor().crop();
		//cropIm.get
		System.out.println("Cropped image: Size "+cropIm.getWidth()+"x"+cropIm.getHeight());
		System.out.println("Old image: Size "+image.getWidth()+"x"+image.getHeight());
		
		
	}

}
