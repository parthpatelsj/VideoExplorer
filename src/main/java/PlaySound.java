//package org.wikijava.sound.playWave;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;

import javax.sound.sampled.*;
import javax.sound.sampled.DataLine.Info;

/**
 * <Replace this with a short description of the class.>
 *
 * @author Giulio
 */
public class PlaySound implements Runnable {

    private InputStream waveStream;
    private AudioFormat audioFormat;
    public SourceDataLine dataLine;
    Clip clip;
    private final int EXTERNAL_BUFFER_SIZE = 524288; // 128Kb
    private boolean isAudioPlaying = true;
    private boolean isAudioStopped = false;

    /**
     * CONSTRUCTOR
     */
    public PlaySound(InputStream waveStream) {
        //this.waveStream = waveStream;
        this.waveStream = new BufferedInputStream(waveStream);
    }

    public void run() {
        try {
            this.play();
        } catch (PlayWaveException e) {
            e.printStackTrace();
            return;
        }
    }

    public void play() throws PlayWaveException {
        AudioInputStream audioInputStream = null;

        try {
            audioInputStream = AudioSystem.getAudioInputStream(this.waveStream);
            clip = AudioSystem.getClip();

        } catch (UnsupportedAudioFileException e1) {
            throw new PlayWaveException(e1);
        } catch (IOException e1) {
            throw new PlayWaveException(e1); }
         catch (LineUnavailableException e) {
            e.printStackTrace();
        }


            // Obtain the information about the AudioInputStream
            audioFormat = audioInputStream.getFormat();
//        Info info = new Info(SourceDataLine.class, audioFormat);

            // opens the audio channel
//             dataLine = null;

//            try {
//                dataLine = (SourceDataLine) AudioSystem.getLine(info);
//                dataLine.open(audioFormat, this.EXTERNAL_BUFFER_SIZE);
//            } catch (LineUnavailableException e1) {
//                throw new PlayWaveException(e1);
//            }

            //opens the audio channel (using clips)
        try {
//            dataLine = (SourceDataLine) AudioSystem.getLine(info);
//            dataLine.open(audioFormat, this.EXTERNAL_BUFFER_SIZE);
            clip.open(audioInputStream);
        } catch (LineUnavailableException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

            // Starts the music :P
           //   dataLine.start();
              clip.start();

//            int readBytes = 0;
//            byte[] audioBuffer = new byte[this.EXTERNAL_BUFFER_SIZE];
//            int x = 1;
//
//            try {
//                while (readBytes != -1) {
//                    readBytes = audioInputStream.read(audioBuffer, 0,
//                            audioBuffer.length);
//                    if (isAudioStopped)
//                        break;
//                    while (!isAudioPlaying) {
//                    }
//
//
//                    if (readBytes >= 0) {
//                        dataLine.write(audioBuffer, 0, readBytes);
//                    }
//                }
//            } catch (IOException e1) {
//                throw new PlayWaveException(e1);
//            } finally {
//                // plays what's left and and closes the audioChannel
//                dataLine.drain();
//                dataLine.close();
//            }
    }


    public long position() {
        return clip.getLongFramePosition();
    }

    public float frameRate() {
        return audioFormat.getFrameRate();
    }

    public void pauseSound() {
        isAudioPlaying = false;
//        dataLine.stop();
        clip.stop();
    }

    public void resumeSound() {
        isAudioPlaying = true;
//        dataLine.start();
        clip.start();
    }

    public void stopSound() {
        isAudioStopped = true;
    }

    public void setSound(int frames) {
        clip.stop();
        clip.setFramePosition(frames);
        clip.start();

    }
}
