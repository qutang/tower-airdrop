package edu.neu.madcourse.qutang.towerairdrop;

import java.util.ArrayList;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.util.Log;

public class SensorListener implements SensorEventListener {

	private SensorManager sensorManager;
	private Sensor accSensor;
	private Sensor magSensor;
	private Context mContext;
	private float lastX;
	private float lastY;
	private float lastZ;
	private long lastUpdateTime;
	private BaseEventHandler mBaseEventHandler;
	private float[] accelerometerValues;
	private static SensorListener mInstance = null;
	private ArrayList<Double> speeds = new ArrayList<Double>();
	private boolean isRunning;
	
	public static SensorListener getInstance(Context c){
		if(mInstance == null){
			if(c != null){
				mInstance = new SensorListener(c);
			}
		}
		return mInstance;
	}
	
	private SensorListener(Context c) {
		this.mContext = c;
		sensorManager = (SensorManager) mContext
						.getSystemService(Context.SENSOR_SERVICE);
	}

	public void pullEventHandler(BaseEventHandler _baseEventHandler) {
		mBaseEventHandler = _baseEventHandler;
	}

	public void start() {
		if (sensorManager != null) {
			accSensor = sensorManager
					.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
		}
		if (accSensor != null) {
			sensorManager.registerListener(this, accSensor,
					SensorManager.SENSOR_DELAY_GAME);
		}
		lastUpdateTime = System.currentTimeMillis();
		isRunning = true;
	}

	public void stop() {
		sensorManager.unregisterListener(this);
		isRunning = false;
	}
	
	public boolean isRunning(){
		return isRunning;
	}

	@Override
	public void onSensorChanged(SensorEvent event) {
		if(Flags.getFlags().scenarioStatus != Constants.GAME_EVENTS.GAME_RUNNING){
			return;
		}
		if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
			accelerometerValues = event.values;
		}
		switch (Flags.getFlags().sensorScenario) {
		case Constants.SHAKING_SCENARIO:
			handleShakingInstructionScenario(accelerometerValues);
			break;
		case Constants.TILTING_SCENARIO:
			handleTiltingScenario(accelerometerValues);
			break;
		}
	}

	private void handleShakingInstructionScenario(float[] aValues) {
		long currentUpdateTime = System.currentTimeMillis();
		long timeInterval = currentUpdateTime - lastUpdateTime;
		long interval_time = 0L;
		double speedLimition = 0.0;
		if(Flags.getFlags().instructionShakeMode&& FinalPrefs.getInstruction(mContext)){
			interval_time = Constants.INSTRUCTION_INTERVAL_TIME;
			speedLimition = Constants.INSTRUCTION_SPEED_SHRESHOLD;
		}else{
			interval_time = Constants.UPTATE_INTERVAL_TIME;
			speedLimition = Constants.SPEED_SHRESHOLD;
		}
		
		if (timeInterval < interval_time) {
			return;
		}
		lastUpdateTime = currentUpdateTime;
		float x = aValues[0];
		float y = aValues[1];
		float z = aValues[2];
		float deltaX = x - lastX;
		float deltaY = y - lastY;
		float deltaZ = z - lastZ;
		lastX = x;
		lastY = y;
		lastZ = z;
		double speed = Math.sqrt(deltaX * deltaX + deltaY * deltaY + deltaZ
				* deltaZ)
				/ timeInterval * 10000;
		if (speed >= speedLimition) {
			if(Flags.getFlags().instructionShakeMode&& FinalPrefs.getInstruction(mContext)){
				mBaseEventHandler.sendEmptyMessage(Constants.GAME_EVENTS.GOOD_SHAKE.ordinal());
			}
			if(!Flags.getFlags().instructionShakeMode|| !FinalPrefs.getInstruction(mContext)){
				mBaseEventHandler.sendEmptyMessage(Constants.GAME_EVENTS.BEING_SHAKING.ordinal());
			}
			speeds.clear();
		} else {
			if(Flags.getFlags().instructionShakeMode&& FinalPrefs.getInstruction(mContext)){
				mBaseEventHandler.sendEmptyMessage(Constants.GAME_EVENTS.SLOW_SHAKE.ordinal());
			}
			if(!Flags.getFlags().instructionShakeMode || !FinalPrefs.getInstruction(mContext)){
				mBaseEventHandler.sendEmptyMessage(Constants.GAME_EVENTS.PAUSE_SHAKING.ordinal());
			}
			speeds.clear();
		}
	}

	private void handleTiltingScenario(float[] aValue) {
		String direction_flag = "";
		float X_lateral = aValue[0]; 
		if(X_lateral < 1 && X_lateral > -1){
			direction_flag = Constants.TILT_STABLE;
        }else if(X_lateral <= -1){
        	direction_flag = Constants.TILT_RIGHT;
        }else if(X_lateral >= 0.5){
        	direction_flag = Constants.TILT_LEFT;
        }
		if(Flags.getFlags().instructionTitleMode && FinalPrefs.getInstruction(mContext)){
			mBaseEventHandler.sendEmptyMessage(Constants.GAME_EVENTS.TILT_INSTRUCTION.ordinal());
		}else{
			mBaseEventHandler.pullDirectionFlag(direction_flag);
		}
//		mBaseEventHandler.sendEmptyMessage(Constants.GAME_EVENTS.TILT_FLAG.ordinal());
	}

	@Override
	public void onAccuracyChanged(Sensor arg0, int arg1) {

	}
}
