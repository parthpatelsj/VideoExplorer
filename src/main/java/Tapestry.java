import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
import java.awt.image.WritableRaster;
import java.io.*;
import java.util.*;


class KeyframeInfo{
    BufferedImage image;
    double distance;
    int position;
    boolean inTapestry = false;
    boolean botTapestry = false;
    int energy;
}


public class Tapestry {

    private final int tapestryFrameCount = 10;
    private int fps = 20;

    private String videoFile;
    RandomAccessFile original_video;
    private byte[] bytes;

    BufferedImage tapestryImg;
    BufferedImage bot_tapestryImg;
    private int width;
    private int height;

    ArrayList<BufferedImage> selectedFrames = new ArrayList<>();
    ArrayList<Integer> selectedFrameNums = new ArrayList<>();

    ArrayList<BufferedImage> bottom_selectedFrames = new ArrayList<>();
    ArrayList<Integer> bottom_selectedFrameNums = new ArrayList<>();

    ArrayList<KeyframeInfo> topTapestry = new ArrayList<>();
    ArrayList<KeyframeInfo> bottomTapestry = new ArrayList<>();

    ArrayList<ArrayList<KeyframeInfo>> newSelectedFrames = new ArrayList<ArrayList<KeyframeInfo>>();



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
        seamCarveTapestry();

    }

    public void seamCarveTapestry() {
//        ImageRetarget ir = new ImageRetarget(new GradientMagnitudeFunction());
//        BufferedImage result = ir.retarget(tapestryImg, 880, 160);

        SeamCarver seamCarve = new SeamCarver();
        BufferedImage carved = seamCarve.runSeamCarve(tapestryImg);



        //        Scaling the image to fit on the screen
        BufferedImage scaledTapestry = new BufferedImage((int)(1710/1.2), (int)(128/1.024), BufferedImage.TYPE_INT_RGB);
        Graphics g = scaledTapestry.createGraphics();
        g.drawImage(carved, 0, 0, (int)(1710/1.2), (int)(128/1.024), null);
        g.dispose();

//        WidthSeamCarver wseam = new WidthSeamCarver();
//        BufferedImage finalResult = wseam.widthSeamCarve(carved, 285);

        tapestryImg = scaledTapestry;

        //Debugging
        try {
            ImageIO.write(scaledTapestry, "png", new File("seamedTapestry.png"));
        } catch (Exception e) {
            System.out.println("Failed to write output image!");
        }
    }


    private void createTapestry(long frameCount) {
        //Will be using scene change detection (in KeyFrameDetection class)
        sceneChangeByInterval(frameCount);

        //Force interval-ed frames, instead of just taking first 10 produced from keyframeDetection
        newChooseFramesByInterval();



        for(KeyframeInfo f: topTapestry) {
            selectedFrames.add(copyImage(f.image));
            selectedFrameNums.add(f.position);
        }

        for(KeyframeInfo f: bottomTapestry) {
            bottom_selectedFrames.add(copyImage(f.image));
            bottom_selectedFrameNums.add(f.position);
        }



        System.out.println("Total size of top tapestry frames = " + selectedFrames.size());
        System.out.println("Total size of bottom tapestry frames = " + bottom_selectedFrames.size());



        //Join all of the frames into one image
        BufferedImage joinedFrames = joinFrames(selectedFrames);
        joinedFrames = joinedFrames.getSubimage(0, 45, width*10, height-45);
        BufferedImage bot_joinedFrames = joinFrames(bottom_selectedFrames);
        bot_joinedFrames = bot_joinedFrames.getSubimage(0, 45, width*10, height-45);

        BufferedImage combined_tapestry = mergeTapestry(joinedFrames, bot_joinedFrames);

//        Scaling the image to fit on the screen
        BufferedImage scaledTapestry = new BufferedImage((width*10)/2, ((height-90)*2)/2, BufferedImage.TYPE_INT_RGB);
        Graphics g = scaledTapestry.createGraphics();
        g.drawImage(combined_tapestry, 0, 0, (width*10)/2, ((height-90)*2)/2, null);
        g.dispose();

        tapestryImg = scaledTapestry;



        /*DEBUG STUFF */
        //Saving tapestry image to local destination
        try {
            // retrieve image
            File outputfile = new File("saved.png");
            ImageIO.write(tapestryImg, "png", outputfile);

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
            ps.println(bottom_selectedFrameNums.get(i));
        }


    }

    static int counter = 0;

    private void sceneChangeByInterval(long frameCount) {
        KeyFrameDetection keyFrameDetection = new KeyFrameDetection();
        BufferedImage prevImg = null;
        Set<Point> emptySet = new HashSet<>();

        /* Going through frames in intervals of 600. */
        for(int col=0; col < 10; col++) {

            ArrayList<KeyframeInfo> intervalFrames = new ArrayList<KeyframeInfo>();

            for (int frame = col*600; frame < (col+1)*600; frame++) {

                BufferedImage currImg = null;
                currImg = readBytesIntoImage(currImg);

                if(frame > 0) {
                    WidthSeamCarver energyGen = new WidthSeamCarver();
                    boolean isKeyFrame = keyFrameDetection.getKeyFrames(prevImg, currImg);

                    //Select the frame if its a scene change, or 200th, or 500th frame of its interval.
                    // This ensure there is at least 2 frames
                    if(isKeyFrame || (frame == (col*600)+200) || frame == (col*600)+500) {
                        KeyframeInfo keyFrame = new KeyframeInfo();
                        keyFrame.image = copyImage(currImg);


                        //Save scene cuts for debug purposes
                        File outputfile = new File("scene-cuts/" + col + "_" + counter + ".jpg");
                        try {
                            ImageIO.write(currImg, "jpg", outputfile);
                        } catch (IOException e) {
                            // TODO Auto-generated catch block
                            e.printStackTrace();
                        }


                        counter++;

                        keyFrame.distance = keyFrameDetection.distance;
                        keyFrame.position = frame;

                        keyFrame.energy = energyGen.getMaxVariance(emptySet, keyFrame.image);

                        //New - interval-ed way
                        intervalFrames.add(keyFrame);

                    }
                }
                prevImg = copyImage(currImg);
            }

            Collections.sort(intervalFrames, new Comparator<KeyframeInfo>() {
                public int compare(KeyframeInfo class1, KeyframeInfo class2) {
                    if (class1.distance > class2.distance) return -1;
                    else return 1;
                }
            });


            Collections.sort(intervalFrames, new Comparator<KeyframeInfo>() {
                public int compare(KeyframeInfo class1, KeyframeInfo class2) {
                    if (class1.position < class2.position) return -1;
                    else return 1;
                }
            });

            newSelectedFrames.add(intervalFrames);
        }
    }


    private void newChooseFramesByInterval() {
        for(ArrayList<KeyframeInfo> x: newSelectedFrames){

//            KeyframeInfo maxKey = Collections.max(x, new Comparator<KeyframeInfo>() {
//                public int compare(KeyframeInfo class1, KeyframeInfo class2) {
//                    if(class1.energy < class2.energy) return -1;
//                    if(class1.energy == class2.energy) return 0;
//                    else return 1;
//                }
//            });

            Collections.sort(x, new Comparator<KeyframeInfo>() {
                public int compare(KeyframeInfo class1, KeyframeInfo class2) {
                    if(class1.energy > class2.energy) return -1;
                    else return 1;
                }
            });

            topTapestry.add(x.get(0));
            bottomTapestry.add(x.get(x.size()-1));

            x.get(0).inTapestry = true;
            x.get(x.size()-1).botTapestry = true;
            System.out.println("Frame #: " + x.get(0).position + "chosen, with energy: " + x.get(0).energy);
        }
    }



    /* HELPER FUNCTIONS */

    private BufferedImage mergeTapestry(BufferedImage top, BufferedImage bottom) {

        BufferedImage newImage = new BufferedImage(width*10, (height-90)*2, BufferedImage.TYPE_INT_RGB);
        Graphics2D g2 = newImage.createGraphics();

        Color oldColor = g2.getColor();
        //fill background
        g2.setPaint(Color.WHITE);
        g2.fillRect(0, 0, width*10, (height-90)*2);

        g2.setColor(oldColor);

        g2.drawImage(top, null, 0, 0);
        g2.drawImage(bottom, null, 0, height-90);

        g2.dispose();
        return newImage;
    }

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
