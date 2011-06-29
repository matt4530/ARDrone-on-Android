package com.codeminders.ardrone.examples;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.codeminders.ardrone.ARDrone;

public class ARDrone_JavaDroneActivity extends Activity {
	/** Called when the activity is first created. */

	private static final long CONNECT_TIMEOUT = 4000;
	private static int reconnectAttempts = 0;
	private static final int reconnectAttemptsMax = 4;
	private static int loadingProgress = 0;
	// private static AndroidVideoPanel videoPanel;
	private static ARDrone drone = null;
	
	
	
	
	/*Components*/
	private TextView statusBar;
	private ProgressBar	progressBar;
	/*Components*/
	
	
	
	
	
	
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);
		getUIComponents();
		// videoPanel = new AndroidVideoPanel(this);
		// setContentView(videoPanel);
		
		changeStatus("Created new ARDrone");
		
		
		try {
			drone = new ARDrone();
			setProgress(10);
			
		}
		catch(Exception e)
		{
			
		}
		
		initDrone();
	}

	private void getUIComponents() {
		statusBar = (TextView) findViewById(R.id.statusBar);
		
		
		
		progressBar = (ProgressBar) findViewById(R.id.progressBar);
		Thread progressBarThread = new Thread(new Runnable() {			
			@Override
			public void run() {
				try {
					while(progressBar.getProgress() != 100)
					{
						Thread.sleep(500);
						progressBar.setProgress(ARDrone_JavaDroneActivity.loadingProgress);
					}
				}
				catch(InterruptedException e)
				{
					changeStatus("Error: InterruptedException at progress bar.");
				}
			}
		});
		progressBarThread.start();
		
		
		
	}

	public void initDrone() {
		try {
			// Create ARDrone object,
			// connect to drone and initialize it.
			// Wait until drone is readyW
			try {
				changeStatus("Creation Attempt #" + reconnectAttempts);
				drone.connect();
				drone.clearEmergencySignal();
				drone.waitForReady(CONNECT_TIMEOUT);
			} catch (Exception e) {
				e.printStackTrace();
				if(reconnectAttempts < reconnectAttemptsMax)
				{
					changeProgress(10 + reconnectAttempts*10);
					reconnectAttempts++;
					initDrone();
					return;
				}
				Log.v("DRONE", "Caught exception. Connection time out.");
			}
			// drone.selectVideoChannel(VideoChannel.HORIZONTAL_IN_VERTICAL);
			// drone.enableAutomaticVideoBitrate();
			// videoPanel.setDrone(drone);
		} catch (Throwable e) {
			e.printStackTrace();
			changeStatus("Could not Connect to Drone");
		}
		prepareLaunch();
	}
	public void prepareLaunch()
	{
		try {
			changeProgress(50);
			changeStatus("Connected to Drone");

			// do TRIM operation
			drone.trim();
			drone.playLED(3, 5, 5);
			//drone.takeOff();
			// Log.v("DRONE", "VIDEO PANEL IS NULL = " + (videoPanel._video ==
			// null));
			changeProgress(70);
			sleep(2000);
			// drone.takeOff();
			drone.playLED(4, 5, 5);
			sleep(5000);
			drone.playLED(5, 5, 5);
			// drone.hover();
			sleep(5000);
			drone.playLED(6, 5, 5);
			// drone.land();
			sleep(4000);
			drone.playLED(7, 5, 5);
			// drone.disconnect();
			changeProgress(100);
			// Log.v("DRONE", "VIDEO PANEL IS NULL = " + (videoPanel._video ==
			// null));
			// drone.disconnect();
			// Log.v("DRONE", "VIDEO PANEL IS NULL = " + (videoPanel._video ==
			// null));
			changeStatus("Disconnected from Drone");
			drone.disconnect();

		} catch (Throwable e) {
			e.printStackTrace();
			changeStatus(ALARM_SERVICE);
		}

	}

	public void sleep(long ms) {
		try {
			Thread.sleep(ms);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	public void changeStatus(String st)
	{
		statusBar.setText(st);
	}
	public void changeProgress(int pr)
	{
		loadingProgress = pr;
	}
}