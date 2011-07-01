package com.codeminders.ardrone.examples;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.util.Log;
import android.view.View;

import com.codeminders.ardrone.ARDrone;
import com.codeminders.ardrone.DroneVideoListener;

public class AndroidVideoPanel extends View implements DroneVideoListener {

	public ARDrone _drone;
	public Bitmap _video;
	public int _x;
	public int _y;

	public AndroidVideoPanel(Context context) {
		super(context);
	}

	public void setDrone(ARDrone drone) {
		drone.addImageListener(this);
		_drone = drone;
	}

	@Override
	public void frameReceived(int startX, int startY, int w, int h, int[] rgbArray, int offset, int scansize) {
		// TODO Auto-generated method stub
		// Color[] colors = new Color[rgbArray.length];
		// for(int i = 0; i < rgbArray.length;i++)
		// {
		// colors[i] = Color.argb(255, red, green, blue)
		// }
		Log.v("DRONE", "Recieved video data");
		if (rgbArray.length == 0)
			return;
		if (_video != null)
			_video.recycle();
		_video = Bitmap.createBitmap(rgbArray, offset, scansize, w, h, Bitmap.Config.ALPHA_8);
		_x = startX;
		_y = startY;
	}

	@Override
	protected void onDraw(Canvas canvas) {
		if (_video != null)
			canvas.drawBitmap(_video, _x, _y, null);
	}
}
