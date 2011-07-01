package com.profusiongames;

import java.io.IOException;

import android.app.Activity;
import android.graphics.Bitmap;
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

public class FusionDrone extends Activity implements NavDataListener, DroneVideoListener {

	private static FusionDrone fDrone;
	private static ARDrone drone;
	private static boolean isConnected = false;
	private static boolean isFlying = false;
	private int batteryLife = 0;

	/* Components */
	@SuppressWarnings("unused")
	private TextView statusBar;
	private Button connectionStartButton;
	private ProgressBar connectionWhirlProgress;
	private Button launchButton;
	private ProgressBar batteryDisplay;
	private TextView batteryText;
	private ImageView videoDisplay;
	/* Components */

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);
		fDrone = this;
		getUIComponents();
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
					connectionStartButton.setText("Disconnected");
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
		batteryDisplay = (ProgressBar) findViewById(R.id.batteryBar);
		batteryText = (TextView) findViewById(R.id.batteryStatusText);
		videoDisplay = (ImageView) findViewById(R.id.droneVideoDisplay);
	}

	public static ARDrone getARDrone() {
		return drone;
	}
	public static FusionDrone getFusionDrone() {
		return fDrone;
	}
	

	private class DroneStarter extends AsyncTask<ARDrone, Integer, Boolean> {
		private static final int CONNECTION_TIMEOUT = 4000;

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
				connectionStartButton.setText("Connected to ARDrone. Tap to disconnect.");
				launchButton.setVisibility(Button.VISIBLE);
			} else {
				connectionStartButton.setText("Error Connecting. Retry?");
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
				connectionStartButton.setText("Disconencted to ARDrone. Tap to connect.");
				connectionStartButton.setEnabled(true);
				launchButton.setVisibility(Button.INVISIBLE);
				batteryDisplay.setProgress(0);
				batteryText.setText("Battery Status");
			} else {
				connectionStartButton.setText("Error disconnecting. Retry?");
			}
			isConnected = !success.booleanValue();

			connectionWhirlProgress.setVisibility(ProgressBar.INVISIBLE);// invisible
			
		}
	}

	@Override
	public void navDataReceived(NavData nd) {
		
		batteryLife = nd.getBattery();
		runOnUiThread(new Runnable() {
			public void run() {
				batteryDisplay.setProgress(batteryLife);
				batteryText.setText("Battery Life: " + batteryLife + "%");
			}
		});
	}

	@Override
	public void frameReceived(int startX, int startY, int w, int h, int[] rgbArray, int offset, int scansize) {
		Log.v("FusionDrone", "frameReceived()");
		videoDisplay.setImageBitmap(Bitmap.createBitmap(rgbArray, offset, scansize, w, h, Bitmap.Config.ALPHA_8));
		
	}
}