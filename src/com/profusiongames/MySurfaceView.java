package com.profusiongames;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

public class MySurfaceView extends SurfaceView implements  SurfaceHolder.Callback{
	public Bitmap video;
	public MySurfaceView(Context context) {
		super(context);
		// TODO Auto-generated constructor stub
		video = BitmapFactory.decodeResource(getResources(), R.drawable.icon);
	}
	
	@Override
	public void onDraw(Canvas c)
	{
		
	}
	
	@Override
	public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void surfaceCreated(SurfaceHolder holder) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void surfaceDestroyed(SurfaceHolder holder) {
		// TODO Auto-generated method stub
		
	}

}
