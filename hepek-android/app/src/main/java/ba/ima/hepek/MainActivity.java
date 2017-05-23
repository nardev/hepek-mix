package ba.ima.hepek;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import android.app.Activity;
import android.content.SharedPreferences;
import android.hardware.Camera;
import android.location.Location;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.Toast;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationServices;
import com.google.gson.Gson;

import org.eclipse.paho.android.service.MqttAndroidClient;
import org.eclipse.paho.client.mqttv3.DisconnectedBufferOptions;
import org.eclipse.paho.client.mqttv3.IMqttActionListener;
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.IMqttToken;
import org.eclipse.paho.client.mqttv3.MqttCallbackExtended;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;

import ba.ima.hepek.effects.SOSFlash;
import ba.ima.hepek.effects.SOSVibrate;
import ba.ima.hepek.utils.SoundThread;

import static java.security.AccessController.getContext;

/**
 * Main application activity with all UI
 * 
 * @author ZeKoU - amerzec@gmail.com
 * @author Gondzo - gondzo@gmail.com
 * @author NarDev - valajbeg@gmail.com
 */

public class MainActivity extends Activity implements SurfaceHolder.Callback {

	/* Used as logging ID */
	private static final String ACTIVITY = MainActivity.class.getSimpleName();
    private final String pending_messages = "PENDING_MESSAGES";

    /* UI elements */
	private Button hepekButton;

	private Button ledWhite;

	private Button ledBlue;

	private Button ledGreen;

	private Button ledRed;

	// FlashThread flashThread = null;
	private Camera mCamera;
	public static SurfaceView preview;
	public static SurfaceHolder mHolder;
	private MqttAndroidClient mqttAndroidClient;
    private GoogleApiClient mGoogleApiClient;

    AtomicBoolean sendInProgress = new AtomicBoolean(false);

    @Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.activity_main);

		init();

	}

	/* Load UI components */
	private void init() {

		preview = (SurfaceView) findViewById(R.id.camSurface);
		mHolder = preview.getHolder();
		mHolder.addCallback(this);

		this.hepekButton = (Button) this.findViewById(R.id.hepekBtn);
		this.hepekButton.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View arg0) {

				// Play sound
				Log.d(ACTIVITY, "Hepek sound effect invoked...");
				new SoundThread(MainActivity.this, R.raw.hepek_sound_effect,
						MainActivity.this).start();

				// Vibrate
				Log.d(ACTIVITY, "Hepek vibrate effect invoked...");
				new SOSVibrate().execute(new Activity[] { MainActivity.this });

				// Flash SOS
				Log.d(ACTIVITY, "Hepek flash effect invoked...");
				new SOSFlash(mCamera).execute(getApplicationContext());
                addMessage();

			}

		});

		// Create an instance of GoogleAPIClient.
		if (mGoogleApiClient == null) {
			mGoogleApiClient = new GoogleApiClient.Builder(this)
					.addApi(LocationServices.API)
					.build();
		}

	}

	@Override
	protected void onStart() {

		mGoogleApiClient.connect();
        doConnect();
		super.onStart();
	}

	@Override
	protected void onPause() {
		super.onPause();
		Log.d("MAIN", "onPause()");
	}

	@Override
	protected void onStop() {
		super.onStop();

		mGoogleApiClient.disconnect();

        try{
            mqttAndroidClient.disconnect();
        }catch (MqttException ex){

        }


		if (mCamera != null) {
			mCamera.stopPreview();
			mCamera.release();
		}

	}

	@Override
	protected void onResume() {
		super.onResume();
		try {
			Log.d("FLASH", "Starting camera preview");
			mCamera = Camera.open();
			mCamera.startPreview();
			mCamera.setPreviewDisplay(mHolder);
		} catch (IOException e) {
			Log.e("FLASH", "Error starting camera preview!");
			e.printStackTrace();
		}

	}

	@Override
	public void surfaceChanged(SurfaceHolder arg0, int arg1, int arg2, int arg3) {
		// Not interested in this because we hide cam preview surface anyways.
	}

	@Override
	public void surfaceCreated(SurfaceHolder holder) {
		mHolder = holder;
		try {
			mCamera.setPreviewDisplay(mHolder);
		} catch (IOException e) {
			e.printStackTrace();
		}

	}

	@Override
	public void surfaceDestroyed(SurfaceHolder arg0) {
		// Not interested in this because we hide cam preview surface anyways.
	}

	private void doConnect(){


        String username=BuildConfig.mqttUsername;
        String password= BuildConfig.mqttPassword;
        String serverUri = "tcp://hepek.ba:1883";

        String clientId = "clientId";
        clientId = clientId + System.currentTimeMillis();
        mqttAndroidClient = new MqttAndroidClient(getApplicationContext(), serverUri, clientId);
        mqttAndroidClient.setCallback(new MqttCallbackExtended() {
            @Override
            public void connectComplete(boolean reconnect, String serverURI) {

                doPublish();
            }

            @Override
            public void connectionLost(Throwable cause) {
            }

            @Override
            public void messageArrived(String topic, MqttMessage message) throws Exception {
            }

            @Override
            public void deliveryComplete(IMqttDeliveryToken token) {
                try {
                    SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(MainActivity.this);
                    String pm = sharedPref.getString("PENDING_MESSAGES","[]");
                    List<Message> messages = new Gson().fromJson(pm,new ArrayList<Message>().getClass());
                    messages.remove(0);
                    sharedPref.edit().putString(pending_messages,new Gson().toJson(messages)).commit();
                    sendInProgress.set(false);
                    doPublish();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });

        MqttConnectOptions mqttConnectOptions = new MqttConnectOptions();
        mqttConnectOptions.setAutomaticReconnect(true);
        mqttConnectOptions.setCleanSession(false);
        if (username.length()>0)
            mqttConnectOptions.setUserName(username);
        if (password.length()>0)
            mqttConnectOptions.setPassword(password.toCharArray());

        try {
            mqttAndroidClient.connect(mqttConnectOptions, null, new IMqttActionListener() {
                @Override
                public void onSuccess(IMqttToken asyncActionToken) {
                    //Toast.makeText(MainActivity.this,"Connected to mqtt server.",Toast.LENGTH_SHORT).show();
                    DisconnectedBufferOptions disconnectedBufferOptions = new DisconnectedBufferOptions();
                    disconnectedBufferOptions.setBufferEnabled(true);
                    disconnectedBufferOptions.setBufferSize(100);
                    disconnectedBufferOptions.setPersistBuffer(false);
                    disconnectedBufferOptions.setDeleteOldestMessages(false);
                    mqttAndroidClient.setBufferOpts(disconnectedBufferOptions);
                }

                @Override
                public void onFailure(IMqttToken asyncActionToken, Throwable exception) {
                    Log.d("","Failed to connect");
                }
            });


        } catch (MqttException ex){
            ex.printStackTrace();
        }
    }

    private void addMessage(){
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(this);
        String pm = sharedPref.getString(pending_messages,"[]");
        List<Message> messages = new Gson().fromJson(pm,new ArrayList<Message>().getClass());

        Location mLastLocation = LocationServices.FusedLocationApi.getLastLocation(
                mGoogleApiClient);
        String android_id = Settings.Secure.getString(this.getContentResolver(),
                Settings.Secure.ANDROID_ID);
        Message msg = new Message();
        if (mLastLocation!=null) {
            msg.timestamp = mLastLocation.getTime();
            msg.lat = mLastLocation.getLatitude();
            msg.lon = mLastLocation.getLongitude();
            msg.speed = mLastLocation.getSpeed();
            msg.accuracy = mLastLocation.getAccuracy();
            msg.altitude = mLastLocation.getAltitude();
            msg.bearing = mLastLocation.getBearing();
            msg.provider = mLastLocation.getProvider();
        }
        msg.deviceID = android_id;

        messages.add(msg);
        sharedPref.edit().putString(pending_messages,new Gson().toJson(messages)).commit();
        doPublish();
    }

	private void doPublish() {

        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(this);
        String pm = sharedPref.getString("PENDING_MESSAGES","[]");
        List<Message> messages = new Gson().fromJson(pm,new ArrayList<Message>().getClass());
        if(messages.size()==0 || sendInProgress.get()) return;
        if (!mqttAndroidClient.isConnected()) return;
        final String publishTopic = "hepek/hepekiraj";
        final String publishMessage=new Gson().toJson(messages.get(0));

        try {
            MqttMessage message = new MqttMessage();
            message.setPayload(publishMessage.getBytes());
            sendInProgress.set(true);
            mqttAndroidClient.publish(publishTopic, message);
        } catch (MqttException e) {
            Log.d("",("Error Publishing: " + e.getMessage()));
            sendInProgress.set(false);
        }
	}


}
