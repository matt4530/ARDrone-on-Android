package com.profusiongames;

import com.codeminders.ardrone.NavData;
import com.codeminders.ardrone.NavDataListener;

import android.content.Context;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

public class BatteryDisplay extends View implements NavDataListener {
	
	private ProgressBar display = null;
	private TextView text = null;
	public BatteryDisplay(Context context) {
		super(context);
		display = (ProgressBar) findViewById(R.id.batteryBar);
		text = (TextView) findViewById(R.id.batteryStatusText);
	}

	@Override
	public void navDataReceived(NavData nd) {
		// TODO Auto-generated method stub
		try {
			text.setText("Battery Life: " + nd.getBattery() + "%");
			display.setProgress(nd.getBattery());
		}
		catch(Exception e)
		{
			Log.v("Battery Display", "" + (text == null) + "   " + (display == null) + "    " + (nd == null));
			//NavData.printState(nd);
		}
		
	}
	

}
