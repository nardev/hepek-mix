package ba.ima.hepek;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;

import com.google.analytics.tracking.android.EasyTracker;

/**
 * Splash screen activity
 * Starting activity for application.
 * 
 * @author ZeKoU - amerzec@gmail.com
 * @author Gondzo - gondzo@gmail.com
 * @author NarDev - valajbeg@gmail.com
 */
public class SplashScreen extends Activity {

	
	/* Used as logging ID */
	private static final String ACTIVITY = SplashScreen.class.getSimpleName();
	
	/* Main application activity intent*/
	private Intent mainActivity;

	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.splash_screen);
		
	}

	@Override
	protected void onPause() {
		super.onPause();
		Log.d(ACTIVITY, "Pausing app..");
	}

	@Override
	protected void onStop() {
		super.onStop();
		//GA simple tracking
		EasyTracker.getInstance().activityStop(this);
	}

	@Override
	protected void onStart() {
		super.onStart();
		Log.d(ACTIVITY, "Starting app...");
		// GA simple tracking
		//TODO: Explore advanced tracking goals
		EasyTracker.getInstance().activityStart(this);
	}

	@Override
	protected void onResume() {
		super.onResume();
		Log.d(ACTIVITY, "Resuming app..");
		mainActivity = new Intent(SplashScreen.this, MainActivity.class);

		/* Splash show time in milliseconds */
		int timeToWaitSplash = 3000;
		
		
		Handler handler = new Handler();
		// run a thread and wait few seconds to show main screen
		handler.postDelayed(new Runnable() {

			@Override
			public void run() {

				// make sure we close the splash screen so the user won't come back when it presses back key
				finish();
				// start the main activity
				SplashScreen.this.startActivity(mainActivity);

			}

		}, timeToWaitSplash); 
		
	}

}