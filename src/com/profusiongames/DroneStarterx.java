package com.profusiongames;

import android.os.AsyncTask;
import android.util.Log;

import com.codeminders.ardrone.ARDrone;

public class DroneStarterx extends AsyncTask<ARDrone, Integer, Boolean> {
	private static final int CONNECTION_TIMEOUT = 4000;

	@Override
	protected Boolean doInBackground(ARDrone... drones) {
		ARDrone drone = drones[0];
		try {
			drone = new ARDrone();
			drone.connect();
			drone.clearEmergencySignal();
			drone.waitForReady(CONNECTION_TIMEOUT);
			drone.playLED(1, 20, 5);
			// connectionStartButton.setText("Connected to ARDrone");
			Log.v("DRONE", "Connected to ARDrone");
			return true;
		} catch (Exception e) {
			e.printStackTrace();
			try {
				drone.clearEmergencySignal();
				drone.clearImageListeners();
				drone.clearNavDataListeners();
				drone.clearStatusChangeListeners();
				drone.disconnect();
				drone = null;
			} catch (Exception e1) {
				e1.printStackTrace();
			}
			Log.v("DRONE", "Caught exception. Connection time out.");
			// connectionStartButton.setText("Error. Retry?");
			// connectionStartButton.setEnabled(true);
		}
		return false;
	}

	protected void onPostExecute(boolean success) {
		if (success) {
			// showDialog("Was a success");
		}

	}

}
