import org.opencv.core.*;
import org.opencv.highgui.Highgui;
import org.opencv.imgproc.Imgproc;
import org.opencv.objdetect.CascadeClassifier;
import org.opencv.imgcodecs.*;

import java.util.ArrayList;
import java.util.List;

public class FaceDetection {

    public static void main(String[] args) {

        System.load("/usr/local/Cellar/opencv/3.3.1_1/share/OpenCV/java/libopencv_java331.dylib");
        System.out.println("\nRunning FaceDetector");

        CascadeClassifier faceDetector = new CascadeClassifier(FaceDetection.class.getResource("haarcascade_frontalface_alt.xml").getPath());

        Mat image = Imgcodecs.imread(FaceDetection.class.getResource("faces.jpg").getPath());

        System.out.println(image.size());

        MatOfRect faceDetections = new MatOfRect();
        faceDetector.detectMultiScale(image, faceDetections);

        System.out.println(String.format("Detected %s faces", faceDetections.toArray().length));

        for (Rect rect : faceDetections.toArray()) {
            Imgproc.rectangle(image, new Point(rect.x, rect.y), new Point(rect.x + rect.width, rect.y + rect.height),
                    new Scalar(0, 255, 0));
        }

        String filename = "face_output.png";
        System.out.println(String.format("Writing %s", filename));


        Imgcodecs.imwrite(filename, image);
    }
}