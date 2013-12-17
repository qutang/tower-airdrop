package edu.neu.madcourse.qutang.towerairdrop;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.concurrent.CopyOnWriteArrayList;

import org.jbox2d.common.Vec2;
import org.jbox2d.dynamics.Body;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.os.Message;
import android.support.v4.view.GestureDetectorCompat;
import android.util.AttributeSet;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import edu.neu.madcourse.qutang.towerairdrop.BaseObject.ObjectType;

public class GameView extends SurfaceView implements SurfaceHolder.Callback {

	private RenderThread mRenderThread;
	private BaseEventHandler mBaseEventHandler;
	private GestureDetectorCompat mDetector;
	public final String TAG = "GameView";

	private static int nextActorId = 0;
	public static CopyOnWriteArrayList<BaseObject> actors = new CopyOnWriteArrayList<BaseObject>();
	public static ArrayList<BaseObject> beforeMainBox = new ArrayList<BaseObject>();
	public static ArrayList<BaseObject> afterMainBox = new ArrayList<BaseObject>();
	public static BoxObject mainBox;
	
	private static float PPM = 128.0f;

	public static Vec2 screenToWorld(Vec2 cords) {
		return new Vec2(cords.x / PPM, cords.y / PPM);
	}

	public static float screenToWorld(float length) {
		return length / PPM;
	}

	public static Vec2 worldToScreen(Vec2 cords) {
		return new Vec2(cords.x * PPM, cords.y * PPM);
	}

	public static float worldToScreen(float length) {
		return length * PPM;
	}

	public static float getPPM() {
		return PPM;
	}

	public static float getMPP() {
		return 1.0f / PPM;
	}

	public static BoxObject getMainBox() {
		for (BaseObject obj : actors) {
			if (obj.objectType == ObjectType.MAIN_BOX) {
				return (BoxObject) obj;
			}
		}
		return null;
	}

	public static TriangleObject getParachute() {
		for (BaseObject obj : actors) {
			if (obj.objectType == ObjectType.PARACHUTE) {
				return (TriangleObject) obj;
			}
		}
		return null;
	}

	public static BaseObject getObjectFromBody(Body body) {
		for (BaseObject obj : actors) {
			try {
				if (obj.mBody.equals(body)) {
					return obj;
				}
			} catch (Exception e) {
				continue;
			}
		}
		return null;
	}
	
	public RenderThread getRenderThread(){
		return mRenderThread;
	}

	private static long spawnDelay = 0;

	public GameView(Context context, AttributeSet attrs) {
		super(context, attrs);
		Log.i(TAG, "GameView constructor");
		getHolder().addCallback(this);
		mRenderThread = new RenderThread(getHolder(), this);
		setFocusable(true);
		setFocusableInTouchMode(true);
	}

	/** zessie's test code **/
	private BoxObject test_obj;

	public BoxObject getTestObject() {
		return test_obj;
	}

//	@Override
//	public boolean onTouchEvent(MotionEvent event) {
//		int touchX = (int) event.getX();
//		int touchY = (int) event.getY();
//		switch(event.getAction()){
//		case MotionEvent.ACTION_DOWN:
//			if (Flags.getFlags().btnPause.posRect.contains(touchX, touchY)) {
//				// if pause button is clicked
//				Flags.getFlags().btnPause.onClick();
//			} else if (Flags.getFlags().btnQuit.posRect.contains(touchX, touchY)) {
//				// if quit button is clicked
//				Flags.getFlags().btnQuit.onClick();
//			}
//			break;
//		}
//		return true;
//	}

	public void pullEventHandler(BaseEventHandler _baseEventHandler) {
		mBaseEventHandler = _baseEventHandler;
	}

	@Override
	public void surfaceChanged(SurfaceHolder holder, int format, int width,
			int height) {
		Log.i(TAG, "SurfaceChanged");
		
		Flags.getFlags().screenW = width;
		Flags.getFlags().screenH = height;

		spawnDelay = System.currentTimeMillis();
		if(mRenderThread.getState() == Thread.State.NEW){
			mRenderThread.setRunning(true);
			mRenderThread.start();
		}
		
		if(mRenderThread.getState() == Thread.State.TERMINATED){
			mRenderThread = new RenderThread(getHolder(), this);
			mRenderThread.setRunning(true);
			mRenderThread.start();
		}


		// initialize world

		Message msg = new Message();
		float[] screen = { width, height};
		msg.obj = screen;
		msg.what = Constants.GAME_EVENTS.INIT_SCENE.ordinal();
		mBaseEventHandler.sendMessage(msg);

		// Testing Box2D, output will be shown in LogCat
		// Physics.testBox2D();
	}

	@Override
	public void surfaceCreated(SurfaceHolder holder) {
		Log.i(TAG, "surfaceCreated");
	}

	@Override
	public void surfaceDestroyed(SurfaceHolder holder) {
		Log.i(TAG, "surfaceDestroyed");
		mRenderThread.setRunning(false);
		Physics.stopPhysicalThread();
		actors.clear();
		nextActorId = 0;
		Flags.resetFlags();
	}

	@Override
	public void draw(Canvas canvas) {
	}

	public void render(Canvas canvas) {
		// Make background white
		canvas.drawColor(Color.WHITE);
		// Draw bitmap background
		Flags.getFlags().bg.drawBackground(canvas);

		// Draw everything
		synchronized (GameView.actors) {
			Iterator<BaseObject> it = actors.iterator();
			while (it.hasNext()) {
				BaseObject obj = it.next();
				if(obj.objectType == ObjectType.MAIN_BOX){
					mainBox = (BoxObject) obj;
				}else if(obj.objectType == ObjectType.BOX){
					beforeMainBox.add(obj);
				}else if(obj.objectType == ObjectType.PARACHUTE){
					afterMainBox.add(obj);
				}else{
					obj.draw(canvas);
				}
			}
		}
		
		synchronized (beforeMainBox) {
			Iterator<BaseObject> b_it = beforeMainBox.iterator();
			while (b_it.hasNext()){
				BaseObject obj = b_it.next();
				obj.draw(canvas);
			}
			beforeMainBox.clear();
		}
		
		if(mainBox != null){
			mainBox.draw(canvas);
		}
		
		
		synchronized (afterMainBox) {
			Iterator<BaseObject> a_it = afterMainBox.iterator();
			while (a_it.hasNext()){
				BaseObject obj = a_it.next();
				obj.draw(canvas);
			}
			afterMainBox.clear();
		}
		
		// Draw deadline
		Flags.getFlags().deadline.draw(canvas);
		
		// Draw emergency
		Flags.getFlags().currentEmergency.draw(canvas);
		
		// Draw header
		Flags.getFlags().bg.drawHeader(canvas);
		
		// Draw buttons
		Flags.getFlags().btnPause.draw(canvas);
		Flags.getFlags().btnQuit.draw(canvas);
		// Draw texts
		Flags.getFlags().scoreText.draw(canvas);
		Flags.getFlags().levelText.draw(canvas);
		// Draw progress bar
		Flags.getFlags().progressBar.draw(canvas);
		Flags.getFlags().heightBar.draw(canvas);
		Flags.getFlags().tiltBar.draw(canvas);
		
		
	}

	public static int getNextId() {
		return nextActorId++;
	}

	public static void rollBackNextId() {
		nextActorId--;
	}

	public static void resetId() {
		nextActorId = 0;
	}

	
	public void pullDetector(GestureDetectorCompat _mDetector) {
		mDetector = _mDetector;
	}
}
