import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
import java.awt.image.DataBufferInt;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.imageio.ImageIO;
import javax.swing.plaf.basic.BasicInternalFrameTitlePane.MaximizeAction;

import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfFloat;
import org.opencv.core.MatOfInt;
import org.opencv.core.Point;
import org.opencv.core.Scalar;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;

public class KeyFrameDetection {
    public double distance = 0;
    public KeyFrameDetection() {
        /* NOTE: MAKE SURE LIBRARIES ARE INSTALLED */
        System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
//        System.load("/usr/local/Cellar/opencv/3.3.1_1/share/OpenCV/java/libopencv_java331.dylib");
    }

    /**
     * Converting BufferedImage type provided by Java library to the mat image type which contains matrix of pixels of OpenCV
     * @param bufImg
     * @return
     * @throws IOException
     */
    private Mat bufferedImageToMat(BufferedImage bufImg) throws IOException {
        BufferedImage bi = new BufferedImage(bufImg.getWidth(), bufImg.getHeight(), BufferedImage.TYPE_3BYTE_BGR);
        bi.getGraphics().drawImage(bufImg, 0, 0, null);
        Mat mat = new Mat(bi.getHeight(), bi.getWidth(), CvType.CV_8UC3);
        byte[] data = ((DataBufferByte) bi.getRaster().getDataBuffer()).getData();
        mat.put(0, 0, data);
        mat.convertTo(mat, CvType.CV_32FC3);
        return mat;
    }

    /**
     * Gets the color histogram of the given mat image
     * @param image
     * @return
     */
    @SuppressWarnings("unchecked")
    private Mat getHistogram(Mat image) {
//		Imgproc.cvtColor(image, image, Imgproc.COLOR_BGR2HSV);
        List<Mat> rgb = new ArrayList<>();
        Core.split(image, rgb); //Splits the image matrix into the three pixel value channels and stores them in rgb

        MatOfInt histSize = new MatOfInt(256);

        final MatOfFloat histRange = new MatOfFloat(0f, 256f);

        boolean accumulate = false;

        Mat r_hist = new Mat();
        Mat g_hist = new Mat();
        Mat b_hist = new Mat();

        List<Mat> rplane = new ArrayList<Mat>();
        List<Mat> gplane = new ArrayList<Mat>();
        List<Mat> bplane = new ArrayList<Mat>();

        rplane.add(rgb.get(0));
        gplane.add(rgb.get(1));
        bplane.add(rgb.get(2));

        Imgproc.calcHist(rplane, new MatOfInt(), new Mat(), r_hist, histSize, histRange, accumulate);
        Imgproc.calcHist(gplane, new MatOfInt(), new Mat(), g_hist, histSize, histRange, accumulate);
        Imgproc.calcHist(bplane, new MatOfInt(), new Mat(), b_hist, histSize, histRange, accumulate);

        int hist_w = 512;
        int hist_h = 400;
        long bin_w = Math.round((double) hist_w / 256);

        Mat histImage = new Mat(hist_h, hist_w, CvType.CV_32FC3);
        Core.normalize(r_hist, r_hist, 3, histImage.rows(), Core.NORM_MINMAX, -1, new Mat());
        Core.normalize(g_hist, g_hist, 3, histImage.rows(), Core.NORM_MINMAX, -1, new Mat());
        Core.normalize(b_hist, b_hist, 3, histImage.rows(), Core.NORM_MINMAX, -1, new Mat());


        for (int i = 1; i < 256; i++) {
        	/*Creating a curve from the list of points we obtain from the histogram function, consider
        	 * every two adjacent points and draw a line between them
        	 */
            Point p1 = new Point(bin_w * (i - 1), hist_h - Math.round(r_hist.get(i - 1, 0)[0]));
            Point p2 = new Point(bin_w * (i), hist_h - Math.round(r_hist.get(i, 0)[0]));
            Imgproc.line(histImage, p1, p2, new Scalar(255, 0, 0), 2, 8, 0);

            Point p3 = new Point(bin_w * (i - 1), hist_h - Math.round(g_hist.get(i - 1, 0)[0]));
            Point p4 = new Point(bin_w * (i), hist_h - Math.round(g_hist.get(i, 0)[0]));
            Imgproc.line(histImage, p3, p4, new Scalar(0, 255, 0), 2, 8, 0);

            Point p5 = new Point(bin_w * (i - 1), hist_h - Math.round(b_hist.get(i - 1, 0)[0]));
            Point p6 = new Point(bin_w * (i), hist_h - Math.round(b_hist.get(i, 0)[0]));
            Imgproc.line(histImage, p5, p6, new Scalar(0, 0, 255), 2, 8, 0);

        }
        return histImage;
    }
    /**
     * Identifies scenes transitions, scene changes by comparing the histograms
     * @param prevFrame
     * @param currFrame
     * @return
     */
    private boolean identifySceneTransitions(BufferedImage prevFrame, BufferedImage currFrame){
        ArrayList<BufferedImage> keyFrames = new ArrayList<>();
        Mat prevImage = null, currImage = null;
        try {
            prevImage = getHistogram(bufferedImageToMat(prevFrame));
            currImage = getHistogram(bufferedImageToMat(currFrame));
        } catch (IOException e) {
            e.printStackTrace();
        }
        distance = Imgproc.compareHist(prevImage, currImage, Imgproc.CV_COMP_BHATTACHARYYA);
        System.out.println(distance);
        if(distance > 0.70) {
            return true;
        }
        return false;
    }
    /**
     * public method to be called to get the key frames
     * @param prevFrame
     * @param currFrame
     * @return
     */
    public boolean getKeyFrames(BufferedImage prevFrame, BufferedImage currFrame){
        return identifySceneTransitions(prevFrame, currFrame);
    }

}
