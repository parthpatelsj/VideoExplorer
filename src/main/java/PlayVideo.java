
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
import java.awt.image.WritableRaster;
import java.io.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.PriorityQueue;

import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;

class KeyFrameClass{
    BufferedImage image;
    double distance;
    int position;
    boolean inTapestry = false;
}

public class PlayVideo implements Runnable{

    JFrame frame;
    JLabel lbIm1;
    JLabel tapestry;
    BufferedImage img;
    BufferedImage tapestryImg;

    private byte[] bytes;

    private PlaySound playSound;
    private String videoFile;
    private String audioFile;
    private long frameNum;

    private int width = 352;
    private int height = 288;
    private final double fps = 20; //Frames per second
    private final int tapestryFrameCount = 10;
    ArrayList<Integer> selectedFrameNums = new ArrayList<Integer>();
    ArrayList<KeyFrameClass> sceneCuts = new ArrayList<>();
    RandomAccessFile original_video;
    Thread soundThread;

    static private boolean isVideoPlaying = true;
    static private boolean isVideoStopped = false;


    @Override
    public void run() {
        play();
    }


    /* Constructor */
    public PlayVideo(String videoFile, String audioFile, PlaySound pSound, Thread soundThread) {
        this.videoFile = videoFile;
        this.audioFile = audioFile;
        this.playSound = pSound;
        this.soundThread = soundThread;
    }

    public void renderTapestry() {

        img = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        tapestryImg = new BufferedImage((width*10)/4, height/4, BufferedImage.TYPE_INT_RGB);

        File file = new File(videoFile);
        try {
            original_video = new RandomAccessFile(videoFile, "rw");
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        long len = width*height*3;
        long frameCount = file.length()/len;
        bytes = new byte[(int) len];
        loadTapestry(frameCount);
    }

    private void play() {


        frameNum = 0;

        img = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        //tapestryImg = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);

        try {
            File file = new File(videoFile);
            original_video = new RandomAccessFile(videoFile, "rw");

            long len = width*height*3;
            long frameCount = file.length()/len;


            frame = new JFrame();
            GridBagLayout gLayout = new GridBagLayout();
            frame.getContentPane().setLayout(gLayout);

            JLabel lbText1 = new JLabel("Video: " + videoFile);
            lbText1.setHorizontalAlignment(SwingConstants.LEFT);
            JLabel lbText2 = new JLabel("Audio: " + audioFile);
            lbText2.setHorizontalAlignment(SwingConstants.LEFT);
            lbIm1 = new JLabel(new ImageIcon(img));
            tapestry = new JLabel(new ImageIcon(tapestryImg));

            tapestry.addMouseListener(new MouseListener() {
                public void mouseClicked(MouseEvent e) {
                    int x = e.getX();
                    int y = e.getY();
                    System.out.println(x+","+y);
                    int seekPos=0;
                    if (x < 88) { frameNum = selectedFrameNums.get(0);  } else
                        if (x < 88*2) { frameNum = selectedFrameNums.get(1); } else
                            if (x < 88*3) { frameNum = selectedFrameNums.get(2); } else
                                if (x < 88*4) { frameNum = selectedFrameNums.get(3);} else
                                if (x < 88*5) { frameNum = selectedFrameNums.get(4);} else
                                if (x < 88*6) { frameNum = selectedFrameNums.get(5);} else
                                if (x < 88*7) { frameNum = selectedFrameNums.get(6);} else
                                if (x < 88*8) { frameNum = selectedFrameNums.get(7);} else
                                if (x < 88*9) { frameNum = selectedFrameNums.get(8);} else
                                if (x < 88*10) { frameNum = selectedFrameNums.get(9);} else
                    System.out.println(seekPos);


                    double audiofps = playSound.frameRate()/fps;
                    try {
                        seekPos = width*height*3*((int)frameNum);
                        System.out.println("Frame Num: " + frameNum + "Seek Position: " + seekPos);
                        original_video.seek(seekPos);
                        playSound.setSound((int)(audiofps*frameNum));
                        frame.repaint();
                    } catch (IOException e1) {
                        e1.printStackTrace();
                    }

                }

                public void mousePressed(MouseEvent e) {

                }

                public void mouseReleased(MouseEvent e) {

                }

                public void mouseEntered(MouseEvent e) {

                }

                public void mouseExited(MouseEvent e) {

                }
            });

            JButton playButton = new JButton();
            JButton stopButton = new JButton();
            JButton pauseButton = new JButton();

            playButton.addActionListener(new ActionListener() {

                @Override
                public void actionPerformed(ActionEvent e) {
                    // TODO Auto-generated method stub
                    isVideoPlaying = true;
					playSound.resumeSound();
//                    soundThread.resume();
//                    playSound.dataLine.start();
                }
            });

            pauseButton.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    // TODO Auto-generated method stub
                    isVideoPlaying = false;
            		playSound.pauseSound();
//                    soundThread.suspend();
//                    playSound.dataLine.stop();
                }
            });

            stopButton.addActionListener(new ActionListener() {

                @Override
                public void actionPerformed(ActionEvent e) {
                    // TODO Auto-generated method stub
                    isVideoStopped = false;
                    playSound.stopSound();
//                    playSound.dataLine.stop();
                }
            });



            playButton.setText("Play");
            pauseButton.setText("Pause");
            stopButton.setText("Stop");
            JPanel panel = new JPanel();
            panel.add(playButton);
            panel.add(pauseButton);
            panel.add(stopButton);

            GridBagConstraints c = new GridBagConstraints();
            c.fill = GridBagConstraints.HORIZONTAL;
            c.anchor = GridBagConstraints.CENTER;
            c.weightx = 0.5;
            c.gridx = 0;
            c.gridy = 0;
            frame.getContentPane().add(lbText1, c);
            c.fill = GridBagConstraints.HORIZONTAL;
            c.anchor = GridBagConstraints.CENTER;
            c.weightx = 0.5;
            c.gridx = 0;
            c.gridy = 1;
            frame.getContentPane().add(lbText2, c);

            c.fill = GridBagConstraints.HORIZONTAL;
            c.gridx = 0;
            c.gridy = 2;
            frame.getContentPane().add(panel, c);

            c.fill = GridBagConstraints.HORIZONTAL;
//            c.anchor = GridBagConstraints.CENTER;
            //c.weightx = 0.25;
//            c.insets = insets;
            c.gridx = 2;
            c.gridy = 2;
            // frame.getContentPane().add(stopButton, c);

            c.fill = GridBagConstraints.HORIZONTAL;
            c.gridx = 0;
            c.gridy = 3;
            frame.getContentPane().add(lbIm1, c);

            c.fill = GridBagConstraints.HORIZONTAL;
            c.ipady = 144;      //make this component tall
            c.weightx = 0.0;
            c.gridx = 0;
            c.gridy = 4;
            frame.getContentPane().add(tapestry, c);

            frame.pack();
            frame.setVisible(true);


            bytes = new byte[(int) len];

            //Calculate the # of audio samples per frame
            double audioSamplePerFrame = playSound.frameRate()/fps;


            /* In the beginning, need to calculate if video or audio is ahead, and recallibrate it */

            //If video frame  is less than (sound frame / spf) then make video frames catch up
            while (frameNum < Math.round(playSound.position()/audioSamplePerFrame)) {
//                System.out.println("Video: " + frameNum + ", Audio: " + Math.round(playSound.position()/audioSamplePerFrame));
                readBytes();
                frame.repaint();
            }

            //If video frame ahead of (sound frame/spf), do nothing
            while( frameNum > Math.round(playSound.position()/audioSamplePerFrame)) {
//                System.out.println("Video: " + frameNum + ", Audio: " + Math.round(playSound.position()/audioSamplePerFrame));
            }



            /* Begin video loop from most recent synced frame */

            for(int i = (int)frameNum; i < frameCount; i++) {

                if(isVideoStopped) {
                    img = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
                    frame.repaint();
                    break;
                }
                ///If video frame ahead of (sound frame/spf), do nothing
                while(frameNum > Math.round(playSound.position()/audioSamplePerFrame) || !isVideoPlaying) {
//                    System.out.println("Video: " + frameNum + ", Audio: " + Math.round(playSound.position()/audioSamplePerFrame));

                }

                //If video frame is less than (sound frame/spf) make video frame catch up
                while(frameNum < Math.round(playSound.position()/audioSamplePerFrame)) {
//                    System.out.println("Video: " + frameNum + ", Audio: " + Math.round(playSound.position()/audioSamplePerFrame));
                    readBytes();
                    frame.repaint();
                }

                readBytes();
                frame.repaint();
                // System.out.println("frame Count: " + frameNum);
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    static int counter = 0;
    private void loadTapestry(long frameCount) {
        ArrayList<BufferedImage> selectedFrames = new ArrayList<>();
        KeyFrameDetection keyFrameDetection = new KeyFrameDetection();
        BufferedImage previousImage = null;
        selectedFrameNums = new ArrayList<Integer>();
        //Copying every 600th frame
        for (int i = (int) frameNum; i < frameCount; i++) {
            BufferedImage image = null;
            image = readBytesIntoImage(image);
            if(i > 0) {
                boolean isKeyFrame = keyFrameDetection.getKeyFrames(previousImage, image);
                if(isKeyFrame) {
                    KeyFrameClass keyFrame = new KeyFrameClass();
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
        Collections.sort(sceneCuts, new Comparator<KeyFrameClass>() {
            public int compare(KeyFrameClass class1, KeyFrameClass class2) {
                if (class1.distance > class2.distance) return -1;
                else return 1;
            }
        });

//        if(sceneCuts.size() >= 10) {
//            sceneCuts.subList(0, 10).clear();;
//        }

        Collections.sort(sceneCuts, new Comparator<KeyFrameClass>() {
            public int compare(KeyFrameClass class1, KeyFrameClass class2) {
                if (class1.position < class2.position) return -1;
                else return 1;
            }
        });

        //Force interval-ed frames, instead of just taking first 10 produced from keyframeDetection
        chooseFramesByInterval();

        for(KeyFrameClass keyframe: sceneCuts) {
            if(keyframe.inTapestry) {
                selectedFrames.add(copyImage(keyframe.image));
                selectedFrameNums.add(keyframe.position);
            }
        }
//        for(int i = 0; i < sceneCuts.size() && i < 10; i++) {
//            selectedFrames.add(copyImage(sceneCuts.get(i).image));
//            selectedFrameNums.add(sceneCuts.get(i).position);
//        }


        BufferedImage joinedFrames = joinFrames(selectedFrames);

        //Scaling the image
        BufferedImage scaledTapestry = new BufferedImage((width*10)/4, height/4, BufferedImage.TYPE_INT_RGB);
        Graphics g = scaledTapestry.createGraphics();
        g.drawImage(joinedFrames, 0, 0, (width*10)/4, height/4, null);
        g.dispose();

        tapestryImg = scaledTapestry;


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

    /* Need to create a second pass filter that will force our tapestry to at least choose 1 image that best represents every 20 seconds. */
    private void chooseFramesByInterval() {
        //Interval = every (x) seconds, we need to pick a frame to represent in the tapestry
        int interval = (int)(fps*5*60)/tapestryFrameCount;
        for(int i=0; i < tapestryFrameCount; i++) {

            for (KeyFrameClass keyframe : sceneCuts){
                if (i*interval <= keyframe.position && keyframe.position < (i+1)*interval) {
                    keyframe.inTapestry = true;
                    break;
                }
            }
        }


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

    private void readBytes() {
        frameNum++;

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
                    img.setRGB(x, y, pix);
                    ind++;

                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
//        System.out.println(frameNum);

    }



    private static BufferedImage copyImage(BufferedImage source) {
        ColorModel cm = source.getColorModel();
        boolean isAlphaPremultiplied = cm.isAlphaPremultiplied();
        WritableRaster raster = source.copyData(null);
        return new BufferedImage(cm, raster, isAlphaPremultiplied, null);
    }


}

