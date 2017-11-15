
import java.io.FileInputStream;
import java.io.FileNotFoundException;




public class AVPlayer {



	public static void main(final String[] args) {
		FileInputStream inputStream = null;

		if (args.length < 2) {
			System.err.println("usage: java -jar AVPlayer.jar [RGB file] [WAV file]");
			return;
		}
		String videoFile = args[0];
		String audioFile = args[1];

		try{
			inputStream = new FileInputStream(audioFile);
		} catch(FileNotFoundException e) {
			e.printStackTrace();
		}

		PlaySound playSound = new PlaySound(inputStream);
		PlayVideo playVideo = new PlayVideo(videoFile, audioFile, playSound);

		Thread soundThread = new Thread(playSound);
		Thread videoThread = new Thread(playVideo);
		soundThread.start();
		videoThread.start();

	}

}