package ba.ima.hepek.effects;

import android.content.Context;
import android.content.pm.PackageManager;
import android.hardware.Camera;
import android.hardware.Camera.Parameters;
import android.os.AsyncTask;
import android.util.Log;

/**
 * Implements flashing effect with SOS message
 * 
 * @author ZeKoU - amerzec@gmail.com
 * @author Gondzo - gondzo@gmail.com
 * @author NarDev - valajbeg@gmail.com
 *
 */

public class SOSFlash extends AsyncTask<Context, Void, Void>{

	public static final String ACTIVITY = SOSFlash.class.getSimpleName();
	
	private Camera cam;
	
	public SOSFlash(final Camera camera) {
		super();
		this.cam = camera;
	}

	@Override
	protected Void doInBackground(Context... context) {
		if(context[0] != null){
			boolean flashCapable = context[0].getPackageManager().hasSystemFeature(
					PackageManager.FEATURE_CAMERA_FLASH);
			Log.d(ACTIVITY, "Has flash?: " + flashCapable);
			
			if(flashCapable){
				Parameters params = null;
				// Simulate a slow network
				try {

					params = cam.getParameters();
					params.setFlashMode(Parameters.FLASH_MODE_TORCH);
					cam.setParameters(params);

					Log.d("FLASH", "Begin flashing...");

					boolean on = true;
					for (long l : SOSPattern.getSOSPattern()) {
						if (on) {
							Log.d("FLASH", "ON for: " + l);
							params.setFlashMode(Parameters.FLASH_MODE_TORCH);
							cam.setParameters(params);

							on = false;
						} else {
							Log.d("FLASH", "OFF for:" + l);
							params.setFlashMode(Parameters.FLASH_MODE_OFF);
							cam.setParameters(params);
							on = true;
						}
						Thread.sleep(l);//Tricky for AsyncTask
					}
							
				} catch (InterruptedException e) {

					Log.e(ACTIVITY, "Failed flashing:" + e.getMessage());
					e.printStackTrace();
					
				} finally {

					try {
						params.setFlashMode(Parameters.FLASH_MODE_OFF);
						cam.setParameters(params);
						
					} catch (Exception e) {
						Log.e(ACTIVITY, "Failed stopping flash!");
						
					}

				}
			}
			
		}
		return null;
		
		
	}
}
