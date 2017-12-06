//package org.wikijava.sound.playWave;

import java.io.*;
import java.lang.annotation.Target;
import java.util.ArrayList;

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

    public static void main(final String[] args) {
        FileInputStream inputStream = null;
        try{
            inputStream = new FileInputStream("USCVillage.wav");
        } catch(FileNotFoundException e) {
            e.printStackTrace();
        }


        PlaySound ps = new PlaySound(inputStream);
        ps.loudFrames();
    }

    public void run() {
        try {
            this.play();
        } catch (PlayWaveException e) {
            e.printStackTrace();
            return;
        }
    }

    public void loudFrames() {
        AudioInputStream audioInputStream = null;

            try {
                audioInputStream = AudioSystem.getAudioInputStream(this.waveStream);
            } catch (UnsupportedAudioFileException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }

            AudioFormat myaudioFormat = audioInputStream.getFormat();
            audioFormat = myaudioFormat;
            Info info = new Info(SourceDataLine.class, audioFormat);

            SourceDataLine dataLine = null;
            try {
                dataLine = (SourceDataLine) AudioSystem.getLine(info);
                dataLine.open(audioFormat, this.EXTERNAL_BUFFER_SIZE);
            } catch (LineUnavailableException e1) {
                System.err.println("Line unavailablie");
            }


        int bufferSize = 2048;

        int sampleSize = audioFormat.getSampleSizeInBits();
        float sampleRate = audioFormat.getSampleRate();
        int channels =  audioFormat.getChannels();
        boolean isBig = audioFormat.isBigEndian();
        System.out.println("----Audio details---");
        System.out.println("Sample Size: " + sampleSize + ", Sample Rate: " + sampleRate + ", Channels: " + channels + ", Big Endian? " + isBig);

        int readBytes = 0;
        byte[] audioBuffer = new byte[bufferSize];
        float[] samples = new float[bufferSize / 2];



        dataLine.start();

        double afps = this.frameRate()/20;
        ArrayList<Integer> loudFrames = new ArrayList<Integer>();

        try {
                while (readBytes != -1) {
                    readBytes = audioInputStream.read(audioBuffer, 0,
                                                    audioBuffer.length);

                    for(int i=0, s = 0 ; i < readBytes;) {
                        int sample = 0;

                        sample |= audioBuffer[i++] & 0xFF;
                        sample |= audioBuffer[i++] << 8;

                        samples[s++] = sample / 32768f;
//                        samples[s++] = sample;
                    }

                    float rms = 0f;
                    for(float sample: samples) {
                        rms += sample * sample;
                    }

                    rms = (float)Math.sqrt(rms/ samples.length);

                    System.out.println("rms: " + Math.abs(rms));

                    if(Math.abs(rms) > 0.17) {
                        loudFrames.add(dataLine.getFramePosition()/(int)afps);
                        System.out.println("That was loud!");
                    }
                    System.out.println("frame# " + (dataLine.getFramePosition())/afps);

                    if (readBytes >= 0) {
                        dataLine.write(audioBuffer, 0, readBytes);
                    }
                }
        } catch (IOException e1) {
            System.err.println("Play wave exception");
        } finally {
            dataLine.drain();
            dataLine.close();
        }

        //Saving frame selection data
        PrintStream ps = null;

        try {
            ps = new PrintStream("audioFrames.txt");
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        for(int i=0; i < loudFrames.size(); i++) {
            ps.println(loudFrames.get(i));
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
