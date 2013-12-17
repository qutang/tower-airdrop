package edu.neu.madcourse.qutang.towerairdrop;

import android.graphics.Canvas;
import android.util.Log;
import android.view.SurfaceHolder;

public class RenderThread extends Thread {
	public final String TAG = "RenderThread";
	private SurfaceHolder mHolder;
	private GameView mView;
	private boolean isRunning;
	
	@Override
	public void run() {
		Canvas canvas;
		Log.d(TAG, "Starting game loop");
		while (isRunning) {
			canvas = null;
			try{
				canvas = mHolder.lockCanvas();
				if(canvas != null){
					synchronized (mHolder){
						// render state to the screen
						mView.render(canvas);
					}
				}
			}
			finally{
				if(canvas != null){
					mHolder.unlockCanvasAndPost(canvas);
				}
			}
		}
		Log.i(TAG,"End game loop");
	}

	public RenderThread(SurfaceHolder holder, GameView view) {
		super();
		Log.i(TAG,"Constructor");
		mHolder = holder;
		mView = view;
	}

	public void setRunning(boolean r){
		isRunning = r;
	}
	
	public boolean isRunning(){
		return isRunning;
	}
}
