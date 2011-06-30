package com.codeminders.ardrone.examples;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.codeminders.ardrone.ARDrone;
import com.profusiongames.R;

public class ARDrone_JavaDroneActivity extends Activity {
	/** Called when the activity is first created. */

	private static final long CONNECT_TIMEOUT = 4000;
	private static int reconnectAttempts = 0;
	private static final int reconnectAttemptsMax = 4;
	// private static AndroidVideoPanel videoPanel;
	private static ARDrone drone = null;
	
	
	
	
	/*Components*/
	private TextView statusBar;
	private Button connectionStartButton;
	private ProgressBar connectionWhirlProgress;
	/*Components*/
	
	
	
	
	
	
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);
		getUIComponents();
		// videoPanel = new AndroidVideoPanel(this);
		// setContentView(videoPanel);	
		
		
		//try {
		//	drone = new ARDrone();			
		//}
		//catch(Exception e)
		//{	
		//}
		//initDrone();
	}

	private void getUIComponents() {
		statusBar = (TextView) findViewById(R.id.statusBar);
		
		
		connectionStartButton = (Button) findViewById(R.id.button1);
		connectionWhirlProgress = (ProgressBar) findViewById(R.id.progressBar1);
		connectionWhirlProgress.setVisibility(4);
		connectionStartButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
            	if(drone == null)
            	{
            		connectionWhirlProgress.setVisibility(0); //visible = true;
            		connectionStartButton.setEnabled(false);
            		connectionStartButton.setText("Connecting...");
            		connectToDrone();
            	}
            	else
            	{
            		connectionWhirlProgress.setVisibility(0); //visible = true;
            		connectionStartButton.setEnabled(false);
            		connectionStartButton.setText("Disconnecting...");
            	}
            }
        });

	}

	public void connectToDrone() {
		connectionWhirlProgress.setVisibility(4);
		try {
			drone = new ARDrone();
			drone.connect();
			drone.clearEmergencySignal();
			sleep(500);
			drone.waitForReady(CONNECT_TIMEOUT);
			drone.playLED(1,20,5);
			connectionStartButton.setText("Connected to ARDrone");
			Log.v("DRONE", "Connected to ARDrone");
		} catch (Exception e) {
			e.printStackTrace();
			try {
				drone.clearEmergencySignal();
				drone.clearImageListeners();
				drone.clearNavDataListeners();
				drone.clearStatusChangeListeners();
				drone.disconnect();
				drone = null;
			} catch (Exception e1) {e1.printStackTrace();}
			Log.v("DRONE", "Caught exception. Connection time out.");
			connectionStartButton.setText("Error. Retry?");
			connectionStartButton.setEnabled(true);
		}
		
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
			changeStatus("Connected to Drone");

			// do TRIM operation
			drone.trim();
			drone.playLED(3, 5, 5);
			//drone.takeOff();
			// Log.v("DRONE", "VIDEO PANEL IS NULL = " + (videoPanel._video ==
			// null));
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
}