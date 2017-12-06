
import java.io.FileInputStream;
import java.io.FileNotFoundException;




public class AVPlayer {


	public static void main(final String[] args) {
		FileInputStream inputStream = null;

		if (args.length < 2) {
			System.err.println("usage: java -jar AVPlayer.jar [RGB file] [WAV file] render");
			return;
		}
		String videoFile = args[0];
		String audioFile = args[1];
		String render = args[2];

		Boolean renderTapestry = false;
		if(render.equals("import")) renderTapestry = true;

		try{
			inputStream = new FileInputStream(audioFile);
		} catch(FileNotFoundException e) {
			e.printStackTrace();
		}

		if(!renderTapestry){
			PlaySound playSound = new PlaySound(inputStream);
			Thread soundThread = new Thread(playSound);
			PlayVideo playVideo = new PlayVideo(videoFile, audioFile, playSound, soundThread);
			playVideo.renderTapestry();

			Thread videoThread = new Thread(playVideo);
			soundThread.start();
			videoThread.start();
		}
		else {
			PlaySound playSound = new PlaySound(inputStream);
			Thread soundThread  = new Thread(playSound);
			PlayVideo playVideo = new PlayVideo(videoFile, audioFile, playSound, soundThread);

			playVideo.importTapestry("seamedTapestry.png", "frames.txt");

			Thread videoThread = new Thread(playVideo);
			soundThread.start();
			videoThread.start();

		}

	}

}