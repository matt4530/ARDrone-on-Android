//setCommand("AT*CONFIG="+(seq++)+",\"general:navdata_demo\",\"TRUE\""+CR+"AT*FTRIM="+(seq++), false);
//setCommand("AT*CONFIG="+(seq++)+",\"general:navdata_demo\",\"TRUE\""+CR+"AT*FTRIM="+(seq++), false);
//setCommand("AT*CONFIG="+(seq++)+",\"general:navdata_demo\",\"TRUE\""+CR+"AT*FTRIM="+(seq++), false);
//setCommand("AT*CONFIG="+(seq++)+",\"general:navdata_demo\",\"TRUE\""+CR+"AT*FTRIM="+(seq++), false);
//setCommand("AT*CONFIG="+(seq++)+",\"general:navdata_demo\",\"TRUE\""+CR+"AT*FTRIM="+(seq++), false);
//setCommand("AT*CONFIG="+(seq++)+",\"general:navdata_demo\",\"TRUE\""+CR+"AT*FTRIM="+(seq++), false);
//setCommand("AT*CONFIG="+(seq++)+",\"general:navdata_demo\",\"TRUE\""+CR+"AT*FTRIM="+(seq++), false);
//setCommand("AT*CONFIG="+(seq++)+",\"general:navdata_demo\",\"TRUE\""+CR+"AT*FTRIM="+(seq++), false);
//setCommand("AT*CONFIG="+(seq++)+",\"general:navdata_demo\",\"TRUE\""+CR+"AT*FTRIM="+(seq++), false);
//setCommand("AT*CONFIG="+(seq++)+",\"general:navdata_demo\",\"TRUE\""+CR+"AT*FTRIM="+(seq++), false);
//setCommand("AT*CONFIG="+(seq++)+",\"general:navdata_demo\",\"TRUE\""+CR+"AT*FTRIM="+(seq++), false);
//setCommand("AT*CONFIG="+(seq++)+",\"general:navdata_demo\",\"TRUE\""+CR+"AT*FTRIM="+(seq++), false);
//setCommand("AT*CONFIG="+(seq++)+",\"general:navdata_demo\",\"TRUE\""+CR+"AT*FTRIM="+(seq++), false);
//setCommand("AT*CONFIG="+(seq++)+",\"general:navdata_demo\",\"TRUE\""+CR+"AT*FTRIM="+(seq++), false);
//setCommand("AT*CONFIG="+(seq++)+",\"general:navdata_demo\",\"TRUE\""+CR+"AT*FTRIM="+(seq++), false);
//https://github.com/shigeodayo/ARDroneForP5/blob/master/ARDroneForP5/src/ARDroneForP5/src/com/shigeodayo/ardrone/command/CommandManager.java

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

	private static boolean hasWaitedSomeTime = false; //for methods we dont want to do till the app has some time to boot
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
		sensorManager.unregisterListener(this);
        super.onStop();
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

					connectionWhirlProgress.setVisibility(ProgressBar.VISIBLE); // visible = true;
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
		launchButton = (Button)findViewById(R.id.Button01);
		launchButton.setVisibility(Button.INVISIBLE);
		launchButton.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				Log.v("DRONE", "Clicked Launch button");
				if (!isConnected) {
					launchButton.setVisibility(Button.INVISIBLE); //just in case a bug makes it visible when not connected
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
				//try {
					//drone.move(0.0f, 0.0f, 0.0f, 0.1f);
				//} catch (IOException e) {
					//e.printStackTrace();
				//}
				hasWaitedSomeTime = true;
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
		
		//final Bitmap cake = Bitmap.createBitmap(rgbArray, offset, scansize, w, h, Bitmap.Config.RGB_565);
		//runOnUiThread(new Runnable() {
		//	public void run() {
		//		Log.v("Drone Control", "Frame recieved on FusionDrone   rgbArray.length = " + rgbArray.length + "       width = " + videoDisplay.getWidth());// + "       data = " + Arrays.toString(rgbArray));
				/*if(videoDisplay.getDrawingCache() != null)
					videoDisplay.getDrawingCache().setPixels(rgbArray, offset, scansize, startX, startY, w, h);
				else {
					Log.v("Control Tower", "frame was recieved but videoDisplay had null drawing cache");*/
					//videoDisplay.setImageBitmap(Bitmap.createBitmap(rgbArray, offset, scansize, w, h, Bitmap.Config.RGB_565));
				//}
				//videoDisplay.invalidate();
				
		//		(new VideoDisplayer(startX, startY, w, h, rgbArray, offset, scansize)).execute();
				//videoDisplay.setImageBitmap(cake);
				//FusionDrone.queueToShow--;
				//Log.v("Drone Control", "Queue = " + queueToShow);
			//}
		//});
		
		
	}

	@Override
	public void onAccuracyChanged(Sensor sensor, int accuracy) {
		// TODO Auto-generated method stub
		//Log.v("DRONE", "Accuracy changed: " + accuracy);
		
	}

	private float sensorThreshold = 3;
	@Override
	public void onSensorChanged(SensorEvent e) {
		if(!hasWaitedSomeTime) return;
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

		
		
		
		/*if(isFlying)
			try {
				drone.move(0.0f, 0.0f,0.0f, 0.01f*e.values[0]);
			} catch (IOException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
		*/
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
				drone.selectVideoChannel(ARDrone.VideoChannel.VERTICAL_ONLY);
				try {
					drone.sendVideoOnData();
					//drone.enableAutomaticVideoBitrate();
				}
				catch(Exception e) { e.printStackTrace();}
				/*FusionDrone.this.runOnUiThread(new Runnable() { 
					public void run() {
						//FusionDrone.drone.addNavDataListener(FusionDrone.this);
					}
				});*/
				// connectionStartButton.setText("Connected to ARDrone");
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
				launchButton.setVisibility(Button.VISIBLE);
			} else {
				connectionStartButton.setText("Error 1. Retry?");
			}
			isConnected = success.booleanValue();
			connectionStartButton.setEnabled(true);
			connectionWhirlProgress.setVisibility(4);// invisible
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
				launchButton.setVisibility(Button.INVISIBLE);
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
		protected void onPostExecute(Void param) {
			//videoDisplay.setImageBitmap(b);
			videoDisplay.setImageDrawable(new BitmapDrawable(b));
			FusionDrone.queueToShow--;
			Log.v("Drone Control", "Queue = " + FusionDrone.queueToShow);
		}
	}
}