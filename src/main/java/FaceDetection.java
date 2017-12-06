import org.opencv.core.*;
import org.opencv.imgproc.Imgproc;
import org.opencv.objdetect.CascadeClassifier;
import org.opencv.imgcodecs.*;

import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;


public class FaceDetection {

    public FaceDetection() {
        System.load("/usr/local/Cellar/opencv/3.3.1_1/share/OpenCV/java/libopencv_java331.dylib");
    }


    public boolean hasFaces(BufferedImage in) {
        Set<java.awt.Point> allFaces = getFacePoints(in);
        if(!allFaces.isEmpty()) return true;
        else return false;
    }


    public Set<java.awt.Point> getFacePoints(BufferedImage img) {
        System.out.println("\nRunning FaceDetector");

        String frontalface_cascade = "haarcascade_frontalface_alt.xml";
        String profileface = "haarcascade_profileface.xml";
        String frontalface_v2 = "haarcascade_frontalface_alt2.xml";
        String fullbody = "haarcascade_fullbody.xml";

        CascadeClassifier faceDetector = new CascadeClassifier(FaceDetection.class.getResource(frontalface_v2).getPath());
        CascadeClassifier faceDetector1 = new CascadeClassifier(FaceDetection.class.getResource(profileface).getPath());
        CascadeClassifier faceDetector2 = new CascadeClassifier(FaceDetection.class.getResource(frontalface_cascade).getPath());
        CascadeClassifier bodyDetector = new CascadeClassifier(FaceDetection.class.getResource(fullbody).getPath());




        Mat image = img2Mat(img);

        System.out.println(image.size());

        MatOfRect faceDetections = new MatOfRect();
        MatOfRect faceDetections1 = new MatOfRect();
        MatOfRect faceDetections2 = new MatOfRect();
        MatOfRect bodyDetections = new MatOfRect();


        faceDetector.detectMultiScale(image, faceDetections);
        faceDetector1.detectMultiScale(image, faceDetections1);
        faceDetector2.detectMultiScale(image, faceDetections2);
        bodyDetector.detectMultiScale(image, bodyDetections);

        System.out.println(String.format("Detected %s faces", faceDetections.toArray().length));

        ArrayList<Integer> facePoints = new ArrayList<>();

        Set<java.awt.Point> allFaces = new HashSet<>();

        for (Rect rect : faceDetections.toArray()) {
            Imgproc.rectangle(image, new Point(rect.x, rect.y), new Point(rect.x + rect.width, rect.y + rect.height),
                    new Scalar(0, 255, 0));

            for(int i = rect.x; i <= rect.x + rect.width; i++) {
                for (int j = rect.y; j <= rect.y + rect.height; j++) {
                    allFaces.add(new java.awt.Point(i, j));
                }
            }

        }

        for (Rect rect : faceDetections1.toArray()) {
            Imgproc.rectangle(image, new Point(rect.x, rect.y), new Point(rect.x + rect.width, rect.y + rect.height),
                    new Scalar(0, 255, 0));

            for(int i = rect.x; i <= rect.x + rect.width; i++) {
                for (int j = rect.y; j <= rect.y + rect.height; j++) {
                    allFaces.add(new java.awt.Point(i, j));
                }
            }

        }

        for (Rect rect : faceDetections2.toArray()) {
            Imgproc.rectangle(image, new Point(rect.x, rect.y), new Point(rect.x + rect.width, rect.y + rect.height),
                    new Scalar(0, 255, 0));

            for(int i = rect.x; i <= rect.x + rect.width; i++) {
                for (int j = rect.y; j <= rect.y + rect.height; j++) {
                    allFaces.add(new java.awt.Point(i, j));
                }
            }

        }

        for (Rect rect : bodyDetections.toArray()) {
            Imgproc.rectangle(image, new Point(rect.x, rect.y), new Point(rect.x + rect.width, rect.y + rect.height),
                    new Scalar(0, 255, 0));

            for(int i = rect.x; i <= rect.x + rect.width; i++) {
                for (int j = rect.y; j <= rect.y + rect.height; j++) {
                    allFaces.add(new java.awt.Point(i, j));
                }
            }

        }

        System.out.println("Size of points for faces: " + allFaces.size());



        String filename = "face_output.png";
        System.out.println(String.format("Writing %s", filename));


        Imgcodecs.imwrite(filename, image);

        return allFaces;
    }

    private static Mat img2Mat(BufferedImage in) {
        Mat out = new Mat(in.getHeight(), in.getWidth(), CvType.CV_8UC3);
        byte[] data = new byte[in.getWidth() * in.getHeight() * (int) out.elemSize()];
        int[] dataBuff = in.getRGB(0, 0, in.getWidth(), in.getHeight(), null, 0, in.getWidth());
        for (int i = 0; i < dataBuff.length; i++) {
            data[i * 3] = (byte) ((dataBuff[i]));
            data[i * 3 + 1] = (byte) ((dataBuff[i]));
            data[i * 3 + 2] = (byte) ((dataBuff[i]));
        }
        out.put(0, 0, data);
        return out;
    }
}