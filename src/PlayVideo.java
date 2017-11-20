import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;

public class PlayVideo implements Runnable{

    JFrame frame;
    JLabel lbIm1;
    BufferedImage img;

    private byte[] bytes;

    private PlaySound playSound;
    private String videoFile;
    private String audioFile;
    private long frameNum;

    private int width = 352;
    private int height = 288;
    private final double fps = 20; //Frames per second
    private InputStream is;
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

    private void play() {

        frameNum = 0;

        img = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);

        try {
            File file = new File(videoFile);
            is = new FileInputStream(file);

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

            JButton playButton = new JButton();
            JButton stopButton = new JButton();
            JButton pauseButton = new JButton();

            playButton.addActionListener(new ActionListener() {

                @Override
                public void actionPerformed(ActionEvent e) {
                    // TODO Auto-generated method stub
                    isVideoPlaying = true;
//					playSound.resumeSound();
                    soundThread.resume();
                    playSound.dataLine.start();
                }
            });

            pauseButton.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    // TODO Auto-generated method stub
                    isVideoPlaying = false;
//            		playSound.pauseSound();
                    soundThread.suspend();
                    playSound.dataLine.stop();
                }
            });

            stopButton.addActionListener(new ActionListener() {

                @Override
                public void actionPerformed(ActionEvent e) {
                    // TODO Auto-generated method stub
                    isVideoStopped = false;
                    playSound.stopSound();
                    playSound.dataLine.stop();
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

            frame.pack();
            frame.setVisible(true);


            bytes = new byte[(int) len];

            //Calculate the # of audio samples per frame
            double audioSamplePerFrame = playSound.frameRate()/fps;


            /* In the beginning, need to calculate if video or audio is ahead, and recallibrate it */

            //If video frame  is less than (sound frame / spf) then make video frames catch up
            while (frameNum < Math.round(playSound.position()/audioSamplePerFrame)) {
                readBytes();
                frame.repaint();
            }

            //If video frame ahead of (sound frame/spf), do nothing
            while( frameNum > Math.round(playSound.position()/audioSamplePerFrame)) {
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
                }

                //If video frame is less than (sound frame/spf) make video frame catch up
                while(frameNum < Math.round(playSound.position()/audioSamplePerFrame)) {
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
                    (numRead = is.read(bytes, offset, bytes.length - offset)) >= 0) {
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
    }
}
