package ba.ima.hepek.effects;

import android.app.Activity;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Vibrator;

/**
 * Implements vibrating effect with SOS message
 * 
 * @author ZeKoU - amerzec@gmail.com
 * @author Gondzo - gondzo@gmail.com
 * @author NarDev - valajbeg@gmail.com
 *
 */

public class SOSVibrate extends AsyncTask<Activity, Void, Void> {

	
	@Override
	protected Void doInBackground(Activity... params) {
		
		if(params != null && params[0] != null){
			Vibrator vib = (Vibrator)params[0].getSystemService(Context.VIBRATOR_SERVICE);
			vib.vibrate(SOSPattern.getSOSPattern(), -1); // Vibrate SOS pattern, no repeat
			
		}
		return null;
	}

}
