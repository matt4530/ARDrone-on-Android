package com.profusiongames;

import java.io.IOException;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.codeminders.ardrone.ARDrone;
import com.codeminders.ardrone.DroneVideoListener;
import com.codeminders.ardrone.NavData;
import com.codeminders.ardrone.NavDataListener;

public class FusionDrone extends Activity implements NavDataListener, DroneVideoListener, SensorEventListener {

	private static FusionDrone fDrone;
	private static ARDrone drone;
	private static SensorManager sensorManager;

	private static boolean isConnected = false;
	private static boolean isFlying = false;
	private int batteryLife = 0;
	public static int queueToShow = 0;
	
	private double startX = -1f;
	private double startY = -1f;
	private double startZ = -1f;

	/* Components */
	@SuppressWarnings("unused")
	private TextView statusBar;
	private Button connectionStartButton;
	private ProgressBar connectionWhirlProgress;
	private Button launchButton;
	private TextView batteryText;
	private ImageView videoDisplay;
	private Button animateButton;
	/* Components */

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.main);
		fDrone = this;
		sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
		getUIComponents();
		
	}
	
	
	

	
	
	
	
	
	
	
	
	@Override
	protected void onResume() {
        super.onResume();
        sensorManager.registerListener(this, sensorManager.getDefaultSensor(Sensor.TYPE_ORIENTATION), 3);
    }
	@Override
    protected void onPause() {
        super.onPause();
        sensorManager.unregisterListener(this);
    }
	@Override
    protected void onStop() {
		super.onStop();
		sensorManager.unregisterListener(this);
		try {
			if(drone != null)
			{
				drone.clearImageListeners();
				drone.clearNavDataListeners();
				drone.clearStatusChangeListeners();
				drone.clearStatusChangeListeners();
				drone.disconnect();
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}   
    }
	
	
	private void getUIComponents() {
		statusBar = (TextView) findViewById(R.id.statusBar);

		connectionStartButton = (Button) findViewById(R.id.connectButton);
		connectionWhirlProgress = (ProgressBar) findViewById(R.id.progressBar1);
		connectionWhirlProgress.setVisibility(ProgressBar.INVISIBLE);
		connectionStartButton.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				Log.v("DRONE", "Clicked Connection button");
				if (!isConnected) {

					connectionWhirlProgress.setVisibility(ProgressBar.VISIBLE); 
					connectionStartButton.setEnabled(false);
					connectionStartButton.setText("Connecting...");
					(new DroneStarter()).execute(FusionDrone.drone);
				} else {
					if(isFlying) { try { drone.land(); Thread.sleep(400);} catch (Exception e) {e.printStackTrace();}} //if going to disconnect, but still flying, attempt to tell drone to land
					connectionStartButton.setEnabled(false);
					connectionStartButton.setText("Disconnecting...");
					(new DroneEnder()).execute(FusionDrone.drone);
				}
			}
		});
		launchButton = (Button)findViewById(R.id.launchButton);
		//launchButton.setVisibility(Button.INVISIBLE);
		launchButton.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				Log.v("DRONE", "Clicked Launch button");
				if (!isConnected) {
					//do nothing
				} else if(isFlying) {
					try { drone.land(); launchButton.setText("Takeoff"); isFlying = false;} 
					catch (IOException e) {e.printStackTrace();}
				} else	{
					try { drone.takeOff(); launchButton.setText("Land"); isFlying = true;}
					catch (IOException e) {e.printStackTrace();}
				}
			}
		});
		batteryText = (TextView) findViewById(R.id.batteryStatusText);
		videoDisplay = (ImageView) findViewById(R.id.droneVideoDisplay);
		animateButton = (Button) findViewById(R.id.animateButton);
		animateButton.setOnClickListener(new View.OnClickListener() {
		public void onClick(View v) {
			try {
				if(drone != null)
					drone.sendVideoOnData();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		});
	}

	public static ARDrone getARDrone() {
		return drone;
	}
	public static FusionDrone getFusionDrone() {
		return fDrone;
	}
	
	@Override
	public void navDataReceived(NavData nd) {
		//NavData.printState(nd);
		//Log.v("DRONE", nd.getVisionTags().toString());
		if(nd.getVisionTags() != null)
		{
			Log.v("DRONE", nd.getVisionTags().toString());
		}
		batteryLife = nd.getBattery();
		runOnUiThread(new Runnable() {
			public void run() {
				batteryText.setText("Battery Life: " + batteryLife + "%");
			}
		});
	}

	@Override
	public void frameReceived(final int startX, final int startY, final int w, final int h, final int[] rgbArray, final int offset, final int scansize) 
	{
		(new VideoDisplayer(startX, startY, w, h, rgbArray, offset, scansize)).execute();
		Log.v("Drone Control", "Frame recieved on FusionDrone   rgbArray.length = " + rgbArray.length + "       width = " + w + " height = " + h);
		try {
			drone.playLED(4, 20, 1);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		
	}


	
	@Override
	public void onAccuracyChanged(Sensor sensor, int accuracy) {
		// TODO Auto-generated method stub
		//Log.v("DRONE", "Accuracy changed: " + accuracy);
		
	}

	private float sensorThreshold = 3;
	@Override
	public void onSensorChanged(SensorEvent e) {
		if(Math.random() < 1) return;
		Log.v("DRONE", "sensor: " + e.sensor + ", x: " + MathUtil.trunk(e.values[0]) + ", y: " + MathUtil.trunk(e.values[1]) + ", z: " + MathUtil.trunk(e.values[2]));
		if(startX == -1f) 
		{
			startX = e.values[0];
			startY = e.values[1];
			startZ = e.values[2];
		}
		float shortX = MathUtil.trunk(MathUtil.getShortestAngle(e.values[0],(float) startX));
		float shortY = MathUtil.trunk(MathUtil.getShortestAngle(e.values[1],(float) startY));
		float shortZ = MathUtil.trunk(MathUtil.getShortestAngle(e.values[2],(float) startZ));
		if( MathUtil.abs(shortX) <  sensorThreshold) shortX = 0;// do nothing
		if( MathUtil.abs(shortY) <  sensorThreshold) shortY = 0;// do nothing
		if( MathUtil.abs(shortZ) <  sensorThreshold) shortZ = 0;// do nothing
		Log.v("DRONE", "sensor difference: x: " + shortX + ", y: " + shortY + ", z: " + shortZ);
	} 
	


    
    
    
    
    
    
    
	private class DroneStarter extends AsyncTask<ARDrone, Integer, Boolean> {
		private static final int CONNECTION_TIMEOUT = 3000;

		@Override
		protected Boolean doInBackground(ARDrone... drones) {
			ARDrone drone = drones[0];
			try {
				drone = new ARDrone();
				FusionDrone.drone = drone; // passing in null objects will not pass object refs
				drone.connect();
				drone.clearEmergencySignal();
				drone.waitForReady(CONNECTION_TIMEOUT);
				drone.playLED(1, 10, 4);
				drone.addNavDataListener(FusionDrone.fDrone);
				drone.addImageListener(FusionDrone.fDrone);
				drone.selectVideoChannel(ARDrone.VideoChannel.HORIZONTAL_ONLY);
				try {
					//drone.sendTagDetectionOnData();
					drone.sendVideoOnData();
					//drone.enableAutomaticVideoBitrate();
				}
				catch(Exception e) { e.printStackTrace();}
				Log.v("DRONE", "Connected to ARDrone" + FusionDrone.drone);
				return true;
			} catch (Exception e) {
				e.printStackTrace();
				try {
					drone.clearEmergencySignal();
					drone.clearImageListeners();
					drone.clearNavDataListeners();
					drone.clearStatusChangeListeners();
					drone.disconnect();
				} catch (Exception e1) {
					e1.printStackTrace();
				}
				Log.v("DRONE", "Caught exception. Connection time out." + FusionDrone.drone);
			}
			return false;
		}

		protected void onPostExecute(Boolean success) {
			if (success.booleanValue()) {
				connectionStartButton.setText("Disconnect...");
			} else {
				connectionStartButton.setText("Error 1. Retry?");
			}
			isConnected = success.booleanValue();
			connectionStartButton.setEnabled(true);
			connectionWhirlProgress.setVisibility(Button.INVISIBLE);
		}
	}
	
	
	
	
	
	
	
	
	

	private class DroneEnder extends AsyncTask<ARDrone, Integer, Boolean> {
		@Override
		protected Boolean doInBackground(ARDrone... drones) {
			ARDrone drone = drones[0];
			try {
				drone.playLED(2, 10, 4);
				drone.clearEmergencySignal();
				drone.clearImageListeners();
				drone.clearNavDataListeners();
				drone.clearStatusChangeListeners();
				drone.disconnect();
				Log.v("DRONE", "Disconnected to ARDrone" + FusionDrone.drone);
				return true;
			} catch (Exception e) {
				e.printStackTrace();
				try {
					drone.clearEmergencySignal();
					drone.clearImageListeners();
					drone.clearNavDataListeners();
					drone.clearStatusChangeListeners();
					drone.disconnect();
				} catch (Exception e1) {
					e1.printStackTrace();
				}
				Log.v("DRONE", "Caught exception. Disconnection error.");
			}
			return false;
		}

		protected void onPostExecute(Boolean success) {
			if (success.booleanValue()) {
				connectionStartButton.setText("Connect...");
				connectionStartButton.setEnabled(true);
				//launchButton.setVisibility(Button.INVISIBLE);
				batteryText.setText("Battery Status");
			} else {
				connectionStartButton.setText("Error 2. Retry?");
			}
			isConnected = !success.booleanValue();

			connectionWhirlProgress.setVisibility(ProgressBar.INVISIBLE);// invisible
			
		}
	}

	
	
	
	
	
	
	
	
	
	private class VideoDisplayer extends AsyncTask<Void, Integer, Void> {
		
		public Bitmap b;
		public int[]rgbArray;
		public int offset;
		public int scansize;
		public int w;
		public int h;
		public VideoDisplayer(int x, int y, int width, int height, int[] arr, int off, int scan) {
	        super();
	        // do stuff
	        rgbArray = arr;
	        offset = off;
	        scansize = scan;
	        w = width;
	        h = height;
	    }

		
		@Override
		protected Void doInBackground(Void... params) {
			b =  Bitmap.createBitmap(rgbArray, offset, scansize, w, h, Bitmap.Config.RGB_565);
			b.setDensity(100);
			return null;
		}
		@Override
		protected void onPostExecute(Void param) {;
			Log.v("Drone Control", "THe system memory is : " + Runtime.getRuntime().freeMemory());
			((BitmapDrawable)videoDisplay.getDrawable()).getBitmap().recycle(); 
			videoDisplay.setImageDrawable(new BitmapDrawable(b));
			FusionDrone.queueToShow--;
			Log.v("Drone Control", "Queue = " + FusionDrone.queueToShow);
		}
	}
}