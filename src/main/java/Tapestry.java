import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
import java.awt.image.WritableRaster;
import java.io.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

class KeyframeInfo{
    BufferedImage image;
    double distance;
    int position;
    boolean inTapestry = false;
}


public class Tapestry {

    private final int tapestryFrameCount = 10;
    private int fps = 20;

    private String videoFile;
    RandomAccessFile original_video;
    private byte[] bytes;

    BufferedImage tapestryImg;
    private int width;
    private int height;
    private long frameNum;

    ArrayList<BufferedImage> selectedFrames = new ArrayList<>();
    ArrayList<Integer> selectedFrameNums = new ArrayList<>();

    ArrayList<KeyframeInfo> sceneCuts = new ArrayList<>();


    /* Constructor - Tapestry */
    public Tapestry(int frameWidth, int frameHeight, String _videoFile) {
        videoFile = _videoFile;
        width = frameWidth;
        height = frameHeight;

        //@TODO: Change tapestryImg size to be dynamic
        tapestryImg = new BufferedImage(frameWidth, frameHeight, BufferedImage.TYPE_INT_RGB);
    }

    /* Renders the complete tapestry, and returns tapestry image */
    public void renderTapestry() {
        File file = new File(videoFile);

        try {
            original_video = new RandomAccessFile(videoFile, "rw");
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        long len = width*height*3;
        long frameCount = file.length()/len;
        bytes = new byte[(int) len];
        createTapestry(frameCount);

    }


    private void createTapestry(long frameCount) {
        //Will be using scene change detection (in KeyFrameDetection class)
        sceneChangeDetection(frameCount);

        //Force interval-ed frames, instead of just taking first 10 produced from keyframeDetection
        chooseFramesByInterval();


        System.out.println("Total options for scene cuts: " + sceneCuts.size());
        //Going through selected frames, and seeing if they will be loaded into the tapestry
        for(KeyframeInfo keyframe: sceneCuts) {
            if(keyframe.inTapestry) {
                selectedFrames.add(copyImage(keyframe.image));
                selectedFrameNums.add(keyframe.position);
            }
        }

        System.out.println("Total size of tapestry frames = " + selectedFrames.size());


        //Join all of the frames into one image
        BufferedImage joinedFrames = joinFrames(selectedFrames);

        //Scaling the image to fit on the screen
        BufferedImage scaledTapestry = new BufferedImage((width*10)/4, height/4, BufferedImage.TYPE_INT_RGB);
        Graphics g = scaledTapestry.createGraphics();
        g.drawImage(joinedFrames, 0, 0, (width*10)/4, height/4, null);
        g.dispose();

        tapestryImg = scaledTapestry;


        /*DEBUG STUFF */
        //Saving tapestry image to local destination
        try {
            // retrieve image
            File outputfile = new File("saved.png");
            ImageIO.write(joinedFrames, "png", outputfile);

        } catch (IOException e) {
            e.printStackTrace();
        }


        //Saving frame selection data
        PrintStream ps = null;

        try {
            ps = new PrintStream("frames.txt");
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        for(int i=0; i < selectedFrameNums.size(); i++) {
            ps.println(selectedFrameNums.get(i));
        }


    }

    static int counter = 0;
    private void sceneChangeDetection(long frameCount) {
        KeyFrameDetection keyFrameDetection = new KeyFrameDetection();

        BufferedImage previousImage = null;
        for(int i = (int) frameNum; i < frameCount; i++) {
            BufferedImage image = null;
            image = readBytesIntoImage(image);

            if(i > 0) {
                boolean isKeyFrame = keyFrameDetection.getKeyFrames(previousImage, image);
                if(isKeyFrame) {
                    KeyframeInfo keyFrame = new KeyframeInfo();
                    keyFrame.image = copyImage(image);
                    File outputfile = new File(counter + ".jpg");
                    try {
                        ImageIO.write(image, "jpg", outputfile);
                    } catch (IOException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }
                    counter++;

                    keyFrame.distance = keyFrameDetection.distance;
                    keyFrame.position = i;
                    sceneCuts.add(keyFrame);
                }
            }
            previousImage = copyImage(image);
        }

        Collections.sort(sceneCuts, new Comparator<KeyframeInfo>() {
            public int compare(KeyframeInfo class1, KeyframeInfo class2) {
                if (class1.distance > class2.distance) return -1;
                else return 1;
            }
        });


        Collections.sort(sceneCuts, new Comparator<KeyframeInfo>() {
            public int compare(KeyframeInfo class1, KeyframeInfo class2) {
                if (class1.position < class2.position) return -1;
                else return 1;
            }
        });

    }


    /* Need to create a second pass filter that will force our tapestry to at least choose 1 image that best represents every 20 seconds. */
    private void chooseFramesByInterval() {
        //Interval = every (x) seconds, we need to pick a frame to represent in the tapestry
        int interval = (int)(fps*5*60)/tapestryFrameCount;
        for(int i=0; i < tapestryFrameCount; i++) {
            boolean found = false;
            for (KeyframeInfo keyframe : sceneCuts){
                if (i*interval <= keyframe.position && keyframe.position < (i+1)*interval) {
                    System.out.println("Frame: " + keyframe.position + " chosen!");
                    keyframe.inTapestry = true;
                    found = true;
                    break;
                }
            }

            if(found == false) System.out.println("No frame for interval: " + i);
        }
    }



    /* HELPER FUNCTIONS */

    private BufferedImage joinFrames(ArrayList<BufferedImage> selectedFrames) {
        int offset = 0;

        //create a new buffer and draw two image into the new image
        BufferedImage newImage = new BufferedImage(width*10,height, BufferedImage.TYPE_INT_RGB);
        Graphics2D g2 = newImage.createGraphics();

        Color oldColor = g2.getColor();
        //fill background
        g2.setPaint(Color.WHITE);
        g2.fillRect(0, 0, width*10, height);

        g2.setColor(oldColor);

        g2.drawImage(selectedFrames.get(0), null, 0, 0);
        //draw images
        for(int i=1; i< selectedFrames.size(); i++) {
            g2.drawImage(selectedFrames.get(i), null, width*i + offset, 0);
        }
        g2.dispose();
        return newImage;

    }

    private BufferedImage readBytesIntoImage(BufferedImage image) {
        frameNum++;
        image = new BufferedImage(width, height, BufferedImage.TYPE_3BYTE_BGR);
        try {
            int offset = 0;
            int numRead = 0;

            while (offset < bytes.length &&
                    (numRead = original_video.read(bytes, offset, bytes.length - offset)) >= 0) {
                offset += numRead;
            }

            int ind = 0;
            for (int y = 0; y < height; y++) {

                for (int x = 0; x < width; x++) {

                    byte a = 0;
                    byte r = bytes[ind];
                    byte g = bytes[ind + height * width];
                    byte b = bytes[ind + height * width * 2];


                    int pix = 0xff000000 | ((r & 0xff) << 16) | ((g & 0xff) << 8) | (b & 0xff);
                    //int pix = ((a << 24) + (r << 16) + (g << 8) + b);
                    image.setRGB(x, y, pix);
                    ind++;

                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
//        System.out.println(frameNum);
        return image;
    }

    private static BufferedImage copyImage(BufferedImage source) {
        ColorModel cm = source.getColorModel();
        boolean isAlphaPremultiplied = cm.isAlphaPremultiplied();
        WritableRaster raster = source.copyData(null);
        return new BufferedImage(cm, raster, isAlphaPremultiplied, null);
    }


}
