package com.codeminders.ardrone.examples;

import android.app.Activity;
import android.util.Log;
import android.os.Bundle;

import com.codeminders.ardrone.ARDrone;
import com.codeminders.ardrone.ARDrone.VideoChannel;

public class ARDrone_JavaDroneActivity extends Activity {
	/** Called when the activity is first created. */

	private static final long CONNECT_TIMEOUT = 4000;
	private static AndroidVideoPanel videoPanel;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		videoPanel = new AndroidVideoPanel(this);
		setContentView(videoPanel);
		initDrone();
	}
	public void initDrone()
	{
		ARDrone drone;
		try {
			// Create ARDrone object,
			// connect to drone and initialize it.
			drone = new ARDrone();
			drone.connect();
			drone.clearEmergencySignal();

			// Wait until drone is readyW
			try {
				drone.waitForReady(CONNECT_TIMEOUT);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				drone.clearImageListeners();
				drone.clearNavDataListeners();
				drone.clearEmergencySignal();
				drone.disconnect();
				Log.v("DRONE", "Caught exception");
			}
			drone.selectVideoChannel(VideoChannel.HORIZONTAL_ONLY);
			videoPanel.setDrone(drone);
			Log.v("DRONE", "VIDEO PANEL IS NULL = " + (videoPanel._video == null));

			setTitle(ACTIVITY_SERVICE);

			// do TRIM operation
			drone.trim();
			Log.v("DRONE", "VIDEO PANEL IS NULL = " + (videoPanel._video == null));
			Thread.sleep(4000);
			Log.v("DRONE", "VIDEO PANEL IS NULL = " + (videoPanel._video == null));
			drone.disconnect();
			Log.v("DRONE", "VIDEO PANEL IS NULL = " + (videoPanel._video == null));
			

		} catch (Throwable e) {
			e.printStackTrace();
			setTitle(ALARM_SERVICE);
		}
	}
}