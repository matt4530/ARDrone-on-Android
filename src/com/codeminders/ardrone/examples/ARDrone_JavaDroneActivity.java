package com.codeminders.ardrone.examples;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;

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
				Log.v("DRONE", "Caught exception");
			}
			drone.selectVideoChannel(VideoChannel.HORIZONTAL_ONLY);
			videoPanel.setDrone(drone);

			setTitle(ACTIVITY_SERVICE);

			// do TRIM operation
			drone.trim();
			drone.playLED(1, 5, 5000);
			//drone.takeOff();
			//Log.v("DRONE", "VIDEO PANEL IS NULL = " + (videoPanel._video == null));
			Thread.sleep(4000);
			//drone.land();
			//Log.v("DRONE", "VIDEO PANEL IS NULL = " + (videoPanel._video == null));
			//drone.disconnect();
			//Log.v("DRONE", "VIDEO PANEL IS NULL = " + (videoPanel._video == null));
			 drone.disconnect();

		} catch (Throwable e) {
			e.printStackTrace();
			setTitle(ALARM_SERVICE);
		}
	}
}