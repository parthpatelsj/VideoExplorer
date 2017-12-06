
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.image.BufferedImage;
import java.io.*;
import java.util.ArrayList;
import java.util.Scanner;
import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;


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
    ArrayList<Integer> selectedFrameNums = new ArrayList<Integer>();
    ArrayList<Integer> bottomSelectedFrameNums = new ArrayList<Integer>();
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


        Tapestry generateTapestry = new Tapestry(width, height, videoFile);
        generateTapestry.renderTapestry();

        tapestryImg = generateTapestry.tapestryImg;
        selectedFrameNums = generateTapestry.selectedFrameNums;
        bottomSelectedFrameNums = generateTapestry.bottom_selectedFrameNums;

    }

    public void importTapestry(String tapestry, String filepath) {

        //Setting tapestry
        BufferedImage image = null;
        try {
            image = ImageIO.read(new File(tapestry));
        } catch (IOException e) {
            System.err.println("Can't open");
        }

        //Reading frame Numbers
        Scanner s = null;
        try {
            s = new Scanner(new File(filepath));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        ArrayList<Integer> topFrames = new ArrayList<Integer>();
        ArrayList<Integer> bottomFrames = new ArrayList<Integer>();
        while(s.hasNext()) {
            topFrames.add(Integer.parseInt(s.next()));
            bottomFrames.add(Integer.parseInt(s.next()));
        }
        s.close();

        tapestryImg = image;
        selectedFrameNums = topFrames;
        bottomSelectedFrameNums = bottomFrames;

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


            final int fPP = 142;

            //The bottom frame
            final int bottom = 130;
            tapestry.addMouseListener(new MouseListener() {
                public void mouseClicked(MouseEvent e) {
                    int x = e.getX();
                    int y = e.getY();
                    System.out.println(x+","+y);
                    int seekPos=0;
                    if (x < fPP) {
                        if (y > bottom) {
                            frameNum = bottomSelectedFrameNums.get(0);
                        }
                        else frameNum = selectedFrameNums.get(0);

                    } else if (x < fPP*2) {
                        if (y > bottom) {
                            frameNum = bottomSelectedFrameNums.get(1);
                        }
                        else frameNum = selectedFrameNums.get(1);
                    } else if (x < fPP*3) {
                        if (y > bottom) {
                            frameNum = bottomSelectedFrameNums.get(2);
                        }
                        else frameNum = selectedFrameNums.get(2);
                    } else if (x < fPP*4) {
                        if (y > bottom) {
                            frameNum = bottomSelectedFrameNums.get(3);
                        }
                        else frameNum = selectedFrameNums.get(3);
                    } else if (x < fPP*5) {
                        if (y > bottom) {
                            frameNum = bottomSelectedFrameNums.get(4);
                        }
                        else frameNum = selectedFrameNums.get(4);
                    } else if (x < fPP*6) {
                        if (y > bottom) {
                            frameNum = bottomSelectedFrameNums.get(5);
                        }
                        else frameNum = selectedFrameNums.get(5);
                    } else if (x < fPP*7) {
                        if (y > bottom) {
                            frameNum = bottomSelectedFrameNums.get(6);
                        }
                        else frameNum = selectedFrameNums.get(6);
                    } else if (x < fPP*8) {
                        if (y > bottom) {
                            frameNum = bottomSelectedFrameNums.get(7);
                        }
                        else frameNum = selectedFrameNums.get(7);
                    } else if (x < fPP*9) {
                        if (y > bottom) {
                            frameNum = bottomSelectedFrameNums.get(8);
                        }
                        else frameNum = selectedFrameNums.get(8);
                    }
                    else if (x < fPP*10) {
                        if (y > bottom) {
                            frameNum = bottomSelectedFrameNums.get(9);
                        }
                        else frameNum = selectedFrameNums.get(9);
                    } else System.err.println("No frame for position you clicked on!");


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
//            JButton stopButton = new JButton();
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

//            stopButton.addActionListener(new ActionListener() {
//
//                @Override
//                public void actionPerformed(ActionEvent e) {
//                    // TODO Auto-generated method stub
//                    isVideoStopped = false;
//                    playSound.stopSound();
////                    playSound.dataLine.stop();
//                }
//            });



            playButton.setText("Play");
            pauseButton.setText("Pause");
//            stopButton.setText("Stop");
            JPanel panel = new JPanel();
            panel.add(playButton);
            panel.add(pauseButton);
//            panel.add(stopButton);

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

//            c.fill = GridBagConstraints.HORIZONTAL;
////            c.anchor = GridBagConstraints.CENTER;
//            //c.weightx = 0.25;
////            c.insets = insets;
//            c.gridx = 2;
//            c.gridy = 2;
//             frame.getContentPane().add(stopButton, c);

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




}

