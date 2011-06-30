package com.profusiongames;

import com.codeminders.ardrone.ARDrone;

import android.app.Activity;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;

public class FusionDrone extends Activity {

	private static ARDrone drone;
	private static boolean isConnected = false;

	/* Components */
	@SuppressWarnings("unused")
	private TextView statusBar;
	private Button connectionStartButton;
	private ProgressBar connectionWhirlProgress;

	/* Components */

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);
		getUIComponents();
	}

	private void getUIComponents() {
		statusBar = (TextView) findViewById(R.id.statusBar);

		connectionStartButton = (Button) findViewById(R.id.button1);
		connectionWhirlProgress = (ProgressBar) findViewById(R.id.progressBar1);
		connectionWhirlProgress.setVisibility(4);
		connectionStartButton.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				android.util.Log.v("DRONE", "Clicked Connection button");
				if (!isConnected) {

					connectionWhirlProgress.setVisibility(0); // visible = true;
					connectionStartButton.setEnabled(false);
					connectionStartButton.setText("Connecting...");
					(new DroneStarter()).execute(FusionDrone.drone);
				} else {
					connectionStartButton.setEnabled(false);
					connectionStartButton.setText("Disconnected");
					(new DroneEnder()).execute(FusionDrone.drone);
				}
			}
		});

	}

	public static ARDrone getARDrone() {
		return drone;
	}

	private class DroneStarter extends AsyncTask<ARDrone, Integer, Boolean> {
		private static final int CONNECTION_TIMEOUT = 4000;

		@Override
		protected Boolean doInBackground(ARDrone... drones) {
			ARDrone drone = drones[0];
			try {
				drone = new ARDrone();
				FusionDrone.drone = drone; //passing in null objects will not pass object refs
				drone.connect();
				drone.clearEmergencySignal();
				drone.waitForReady(CONNECTION_TIMEOUT);
				drone.playLED(1, 10, 4);
				// connectionStartButton.setText("Connected to ARDrone");
				Log.v("DRONE", "Connected to ARDrone" +  FusionDrone.drone);
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
				Log.v("DRONE", "Caught exception. Connection time out." +  FusionDrone.drone);
			}
			return false;
		}

		protected void onPostExecute(Boolean success) {
			if (success.booleanValue()) {
				connectionStartButton.setText("Connected to ARDrone. Tap to disconnect.");
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
				Log.v("DRONE", "Disconnected to ARDrone" +  FusionDrone.drone);
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
			} else {
				connectionStartButton.setText("Error disconnecting. Retry?");
			}
			isConnected = !success.booleanValue();
			
			connectionWhirlProgress.setVisibility(4);// invisible
		}
	}
}