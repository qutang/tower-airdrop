package edu.neu.mhealth.qutang.towerairdrop.activities;

import android.app.Activity;
import android.os.Bundle;
import android.support.v4.view.GestureDetectorCompat;
import android.util.Log;
import android.view.GestureDetector.OnDoubleTapListener;
import android.view.GestureDetector.OnGestureListener;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.MotionEvent;

import edu.neu.mhealth.qutang.towerairdrop.GameMusicPlayer;
import edu.neu.mhealth.qutang.towerairdrop.physicalengine.BaseEventHandler;
import edu.neu.mhealth.qutang.towerairdrop.Constants.DIALOG_FROM;
import edu.neu.mhealth.qutang.towerairdrop.Constants.GAME_EVENTS;
import edu.neu.mhealth.qutang.towerairdrop.Constants.PAUSE_FROM;
import edu.neu.mhealth.qutang.towerairdrop.Flags;
import edu.neu.mhealth.qutang.towerairdrop.GameView;
import edu.neu.mhealth.qutang.towerairdrop.physicalengine.Physics;
import edu.neu.mhealth.qutang.towerairdrop.R;
import edu.neu.mhealth.qutang.towerairdrop.sensors.SensorListener;

public class GameActivity extends Activity implements OnGestureListener,
		OnDoubleTapListener {

	public final String TAG = "GameActivity";
	private BaseEventHandler baseEventHandler;
	private GestureDetectorCompat mDetector;
	private GameView gameView;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		Log.i(TAG, "onCreate");
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_game);
		initiate();
	}

	protected void initiate() {
		gameView = (GameView) findViewById(R.id.gameView);
		GameMusicPlayer.stopSound(this);
		baseEventHandler = new BaseEventHandler(gameView);
		mDetector = new GestureDetectorCompat(this, this);
		mDetector.setOnDoubleTapListener(this);
		SensorListener.getInstance(this).pullEventHandler(baseEventHandler);
		gameView.pullEventHandler(baseEventHandler);
		gameView.pullDetector(mDetector);
		Physics.pullEventHandler(baseEventHandler);
	}



	@Override
	protected void onDestroy() {
		Log.i(TAG, "onDestroy");
		super.onDestroy();
	}

	@Override
	protected void onPause() {
		Log.i(TAG, "onPause");
		GameMusicPlayer.stopBackgroundMusic(this);
		if(Flags.getFlags().pauseFrom == PAUSE_FROM.COVER){
			Flags.getFlags().isGamePaused = true;
			baseEventHandler.pauseGame(false);
			baseEventHandler.okEnablePopDialog("Game is paused, press OK button to resume", Gravity.CENTER, true, true, DIALOG_FROM.PAUSE);
		}
		super.onPause();
	}
	
	@Override
	protected void onStop(){
		Log.i(TAG, "onStop");
		super.onStop();
	}
	
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
	    if ((keyCode == KeyEvent.KEYCODE_BACK)) {
	        baseEventHandler.sendEmptyMessage(GAME_EVENTS.GAME_QUIT.ordinal());
	    }
	    return super.onKeyDown(keyCode, event);
	}

	@Override
	protected void onResume() {
		Log.i(TAG, "onResume");
		super.onResume();
		GameMusicPlayer.playBackgroundMusic(this, R.raw.canon);
		
		boolean load = this.getIntent().getBooleanExtra("CONTINUE", false);
		baseEventHandler.resumeGame(load);
		Flags.getFlags().isGamePaused = false;
		
//		if(Flags.getFlags().resumeFrom == RESUME_FROM.MENU){
//			Flags.getFlags().resumeFrom = RESUME_FROM.LOCK;
//			boolean load = this.getIntent().getBooleanExtra("CONTINUE", false);
//			baseEventHandler.resumeGame(load);
//			Flags.getFlags().isGamePaused = false;
//		}else if(Flags.getFlags().resumeFrom == RESUME_FROM.LOCK){
//			boolean load = this.getIntent().getBooleanExtra("CONTINUE", false);
//			baseEventHandler.resumeGame(load);
//			Flags.getFlags().isGamePaused = false;
//		}
	}

	@Override
	public boolean onTouchEvent(MotionEvent event) {
		this.mDetector.onTouchEvent(event);
		return super.onTouchEvent(event);
	}

	@Override
	public boolean onDoubleTap(MotionEvent e) {
		float x = e.getX();
		float y = e.getY();

		if (Flags.getFlags().isInEmergency) {
			switch (Flags.getFlags().currentEmergency.getType()) {
			case STUCK:
				float distance = (float) Math.sqrt(Math.pow(
						Flags.getFlags().currentEmergency.pos1.centerX() - x,
						2f)
						+ Math.pow(
								Flags.getFlags().currentEmergency.pos1
										.centerY() - y, 2f));
				if (distance <= 150) {
					Flags.getFlags().currentEmergency.counter--;
				}
				if (Flags.getFlags().currentEmergency.counter == 0) {
					Flags.getFlags().currentEmergency.solved = true;
					baseEventHandler.sendEmptyMessage(GAME_EVENTS.EMERGENCY
							.ordinal());
				}
				break;
			default:
				break;
			}
		}

		return false;
	}

	@Override
	public boolean onDoubleTapEvent(MotionEvent e) {
		return false;
	}

	@Override
	public boolean onSingleTapConfirmed(MotionEvent e) {

		return false;
	}

	@Override
	public boolean onDown(MotionEvent event) {
		int touchX = (int) event.getX();
		int touchY = (int) event.getY();

		if (Flags.getFlags().isInEmergency) {
			switch (Flags.getFlags().currentEmergency.getType()) {
			case LIGHTNING:
				float distance = (float) Math.sqrt(Math.pow(
						Flags.getFlags().currentEmergency.pos1.centerX() - touchX,
						2f)
						+ Math.pow(
								Flags.getFlags().currentEmergency.pos1
										.centerY() - touchY, 2f));
				if (distance <= 150) {
					Flags.getFlags().currentEmergency.counter--;
					Flags.getFlags().currentEmergency.updateStatus();
				}
				if (Flags.getFlags().currentEmergency.counter == 0) {
					Flags.getFlags().currentEmergency.solved = true;
					baseEventHandler.sendEmptyMessage(GAME_EVENTS.EMERGENCY
							.ordinal());

				}
				break;
			default:
				break;
			}
		}
		
		
		
//		if (Flags.getFlags().btnPause.posRect.contains(touchX, touchY)) {
//			// if pause button is clicked
//			Flags.getFlags().btnPause.onClick();
//		} 
		float distanceX = touchX - Flags.getFlags().btnQuit.posRect.centerX();
		float distanceY = touchY - Flags.getFlags().btnQuit.posRect.centerY();
		float distance = (float) Math.sqrt(distanceX*distanceX + distanceY*distanceY);
		float t = 0.2f*Flags.getFlags().screenW;
		
		if (Math.sqrt(distanceX*distanceX + distanceY*distanceY) <= 0.2f*Flags.getFlags().screenW) {
			// if quit button is clicked
			Flags.getFlags().btnQuit.onClick();
		}

		return true;
	}

	@Override
	public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX,
			float velocityY) {
		return false;
	}

	@Override
	public void onLongPress(MotionEvent e) {
		return;
	}

	@Override
	public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX,
			float distanceY) {
		float startX = e1.getX();
		float startY = e1.getY();
		float endX = e2.getX();
		float endY = e2.getY();

		if (Flags.getFlags().isInEmergency) {
			switch (Flags.getFlags().currentEmergency.getType()) {
			case BLIND:
				if (Flags.getFlags().currentEmergency.pos1.contains(
						(startX + endX) / 2, (startY + endY) / 2)
						&& Math.sqrt((distanceX * distanceX)
								+ (distanceY * distanceY)) >= Flags.getFlags().screenW * 0.2f) {
					Flags.getFlags().currentEmergency.counter--;
				}
				if (Flags.getFlags().currentEmergency.counter == 0) {
					Flags.getFlags().currentEmergency.solved = true;
					baseEventHandler.sendEmptyMessage(GAME_EVENTS.EMERGENCY
							.ordinal());
				}
				break;
			default:
				break;
			}
		}

		return false;
	}

	@Override
	public void onShowPress(MotionEvent e) {
		return;
	}

	@Override
	public boolean onSingleTapUp(MotionEvent e) {
		return false;
	}

}
