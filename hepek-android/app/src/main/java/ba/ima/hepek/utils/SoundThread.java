package ba.ima.hepek.utils;

import android.app.Activity;
import android.content.Context;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.util.Log;

/**
 * Playing sounds
 * 
 * @author ZeKoU - amerzec@gmail.com
 * @author Gondzo - gondzo@gmail.com
 * @author NarDev - valajbeg@gmail.com
 * 
 */
public class SoundThread extends Thread {

	private static final String ACTIVITY = SoundThread.class.getSimpleName();
	
	private MediaPlayer mp;
	
	/* Time to be delayed before playing */
	private long playDelayTime = 0L;
	
	private Activity app;

	public SoundThread(final Context context, final int soundResource, final Activity app) {

		//this.soundResource = soundResource;
		//this.context = context;
		this.mp = MediaPlayer.create(context, soundResource);
		this.app = app;
	}
	
	
	@Override
	public void run() {
		try {
			sleep(playDelayTime);
			if(!mp.isPlaying()){
				
				//Adjust volume to max
				AudioManager audioManager = (AudioManager)app.getSystemService(Context.AUDIO_SERVICE);
				
				mp.setVolume(audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC), audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC));
				
				mp.start();
				//FIXME: Still not able to control single playback at a time
				while(mp.isPlaying()){
					continue;
				}
				mp.release();
			}
						
		} catch (InterruptedException e) {
			Log.e(ACTIVITY, "Error playing sound" + e.getMessage());
			e.printStackTrace();
		} catch (IllegalStateException e) {
			Log.e(ACTIVITY, "Error playing sound" + e.getMessage());
			e.printStackTrace();
		} 

	}
	
	public void setPlayDelayTime(long playDelayTime) {
		this.playDelayTime = playDelayTime;
	}
}