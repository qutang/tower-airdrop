package edu.neu.mhealth.qutang.towerairdrop;

import org.jbox2d.common.Vec2;

import android.content.SharedPreferences;

public class Constants {
	// Messages or scene status
	public enum GAME_EVENTS {
		SHOW_TOAST, HANDLER_TEST, BEING_SHAKING, PAUSE_SHAKING, INIT_SCENE, SCENE_CREATED, ACTION_READY, GAME_RUNNING, SCENE_ENDING, NEXT_SCENE, GAME_OVER, GAME_PAUSE, GAME_RESUME, GAME_SAVE, GAME_LOAD, GAME_QUIT, STACK, STUCK, EMERGENCY, TILT_FLAG, BOX_COLLIDE, FINISH_STACK, GOOD_SHAKE, SLOW_SHAKE, TILT_INSTRUCTION
	}

	public enum BUTTON_TYPE {
		SETTING, PAUSE, PLAY, QUIT
	}

	public enum PROGRESS_TYPE {
		HEIGHT, PARACHUTE, TILT
	}
	
	public enum EMERGENCY_TYPE {
		STUCK, BLIND, LIGHTNING
	}
	
	public enum RESUME_FROM {
		MENU, LOCK
	}
	
	public enum PAUSE_FROM {
		DIALOG, COVER
	}
	
	public enum DIALOG_FROM {
		EMERGENCY, PAUSE
	}

	public static final String TILT_STABLE = "stable";
	public static final String TILT_LEFT = "left";
	public static final String TILT_RIGHT = "right";

	// Scenarios
	public static final int INIT_SCENARIO = 0x102;
	public static final int SHAKING_SCENARIO = 0x103;
	public static final int TILTING_SCENARIO = 0x104;
	public static final int GAME_OVER_SCENARIO = 0x105;

	// sensor constants
	public static final int SPEED_SHRESHOLD = 1500;
	public static final int UPTATE_INTERVAL_TIME = 70;
	public static final int INSTRUCTION_SPEED_SHRESHOLD = 35;
	public static final int INSTRUCTION_INTERVAL_TIME = 2000;

	// for different screen size
	public static final float BASELINE_SCREEN_WIDTH = 720f;
	public static final float BASELINE_SCREEN_HEIGHT = 1280f;

	// Level up mechanism
	public static final int BASE_BOX_SCORE = 20;
	public static final int INCREASED_BOX_SCORE = 5;
	public static final int SCORE_INCREASE_INTERVAL = 5;
	
	public static final int BASE_TILT_TIME = 6;
	public static final int INCREASED_TILT_TIME = 2;
	public static final int TILT_TIME_INCREASE_INTERVAL = 10;
	
	public static final float BASE_TILT_OFFSET = 1;
	public static final float INCREASED_TILT_OFFSET = 0.5f;
	public static final float TILT_OFFSET_INCREASE_INTERVAL = 10;
	
	public static final float BASE_SHAKE_SENSITIVITY = 5f;
	public static final float DECREASED_SHAKE_SENSITIVITY = 0.2f;
	public static final float SHAKE_SENSITIVITY_CHANGE_INTERVAL = 5;
	public static final float MIN_SHAKE_SENSITIVITY = 1f;
	
	public static final float BASE_SHAKE_PROGRESS = 0.8f;
	public static final float DECREASED_SHAKE_PROGRESS = 0.05f;
	public static final float SHAKE_PROGRESS_CHANGE_INTERVAL = 10;
	public static final float MIN_SHAKE_PROGRESS = 0.2f;
	
	public static final float BASE_PARACHUTE_GONE_POS = 1f;
	public static final float INCREASED_PARACHUTE_GONE_POS = 0.2f;
	public static final float PARACHUTE_GONE_POS_INCREASE_INTERVAL = 10f;
	public static final float MAX_PARACHUTE_GONE_POS = 3f;

	public static final int BASE_LEVEL = 1;
	
	// Physical world
	public static final float PARACHUTE_SIZE = 0.2f; // proportion to the screen
														// width

	public static final float BOX_SIZE = 0.1f; // proportion to the screen width

	public static final float CARRIER_SIZE = 0.4f;

	public static final float LANDSCAPE_MIN_SIZE = 1 / 6f;
	public static final float LANDSCAPE_SIZE_RANGE = 1 / 5f;

	public static final float OBSTACLE_SIZE_RANGE = 0.1f;
	public static final float OBSTACLE_SIZE_MIN = 0.1f;

	public static final float BOX_DENSITY_DEFAULT = 1.0f;
	public static final float BOX_FRICTION_DEFAULT = 0.3f;
	public static final float BOX_RESTITUTION_DEFAULT = 0.0f;

	public static final float PARACHUTE_DENSITY_DEFAULT = 0.5f;
	public static final float PARACHUTE_FRICTION_DEFAULT = 0.3f;
	public static final float PARACHUTE_RESTITUTION_DEFAULT = 0.0f;
	public static final float LANDSCAPE_DENSITY = 0.0f;
	

	// landscape timing
	public static float LANDSCAPE_RANGE_INTERVAL_SHAKING = 1f; // in sec
	public static float LANDSCAPE_MIN_INTERVAL_SHAKING = 0.5f; // in sec
	public static float LANDSCAPE_RANGE_INTERVAL_TILT = 2f;
	public static float LANDSCAPE_MIN_INTERVAL_TILT = 2f;

	// obstacle timing

	public static final float OBSTACLE_RANGE_INTERVAL = 1f;
	public static final float OBSTACLE_MIN_INTERVAL = 0.5f;
	
	// emergency timing
	public static final float EMERGENCY_RANGE_INTERVAL = 5f;
	public static final float EMERGENCY_MIN_INTERVAL = 5f;
	public static final long EMERGENCY_SHAKING_INTERVAL = 150;
	public static final long EMERGENCY_REST_INTERVAL = 2;
	public static final int EMERGENCY_MAX_COUNTER = 5;

	// Speeds: all designed based on baseline
	public static float CARRIER_SPEED = 2.0f;
	public static float LANDSCAPE_SPEED_TILTING = -2.0f;
	public static float LANDSCAPE_SPEED_SHAKING = -10.0f;
	public static float LANDSCAPE_UP_ACCEL = 1.5f;

	public static float MAX_TILT_SPEED = 1.5f; // left and right speed
	public static float MAX_TILT_ANGLE = 5f; // Degree

	public static float PARACHUTE_UP_SPEED = 3.0f;

	public static float PARACHUTE_DOWN_SPEED = 1.0f;
	public static float BOX_STACK_SPEED = 10f;

	public static float OBSTACLE_X_MIN_SPEED = 3f;
	public static float OBSTACLE_X_RANGE_SPEED = 6f;
	public static float OBSTACLE_Y_MIN_SPEED = 0f;
	public static float OBSTACLE_Y_RANGE_SPEED = 3f;
	
	public static float BOX_SHAKE_SPEED = 3f;

	// Forces: all designed based on baseline
	public static final Vec2 GRAVITY_FORCE_DEFAULT = new Vec2(0, 10.0f);
	public static final Vec2 NO_GRAVITY = new Vec2(0f, 0f);
	public static float PARACHUTE_FORCE_TILT = 0.8f;
	public static float BOX_FORCE_TILT = 0.5f;

	// Smart Camera
	public static final float SMART_CAMERA_THRESHOLD = 5;

	// Collision rules
	public static final short CATEGORY_BOX = 0x0001;
	public static final short CATEGORY_OBSTACLE = 0x0002;
	public static final short CATEGORY_LANDSCAPE = 0x0003;
	public static final short CATEGORY_PARACHUTE = 0x0004;
	public static final short CATEGORY_MAIN_BOX = 0x0005;
	public static final short CATEGORY_BASE = 0x0006;
	public static final short COLLIDE_MASK_BOX = CATEGORY_MAIN_BOX;
	public static final short COLLIDE_MASK_MAIN_BOX = CATEGORY_BOX
			| CATEGORY_OBSTACLE;
	public static final short COLLIDE_MASK_OBSTACLE = CATEGORY_PARACHUTE
			| CATEGORY_MAIN_BOX;
	public static final short COLLIDE_MASK_LANSCAPE = 0;
	public static final short COLLIDE_MASK_PARACHUTE = CATEGORY_OBSTACLE;
	public static final short COLLIDE_MASK_BASE = CATEGORY_MAIN_BOX;

	// Limits
	public static final int MAX_OBSTACLES = 5;

	// Positions
	public static final float BOX_SLOW_DOWN_POS = 1 / 7f; // adaptive to screen
															// height

	// Button pos
	public static final float BUTTON_SIZE = 0.08f;
	public static final float BUTTON_LEFT = 0.76f;
	public static final float BUTTON_TOP = 0.92f;
	public static final float BUTTON_GAP = 0.02f;

	// Text pos
	public static final float SCORE_LEFT = 0.7f;
	public static final float SCORE_TOP = 0.06f;
	public static final float SCORE_SIZE = 0.06f;

	public static final float LEVEL_LEFT = 0.15f;
	public static final float LEVEL_TOP = 0.06f;
	public static final float LEVEL_SIZE = 0.06f;

	// Header pos
	public static final float HEADER_HEIGHT = 0.08f;
	public static final float HEADER_LEFT = 0.02f;
	public static final float HEADER_RIGHT = 0.98f;
	public static final float HEADER_TOP = 0.01f;

	// Progress Bar pos
	public static final float PARACHUTE_PROGRESS_HEIGHT = 0.05f;
	public static final float PARACHUTE_PROGRESS_LEFT = 0.06f;
	public static final float PARACHUTE_PROGRESS_TOP = 0.92f;
	public static final float PARACHUTE_PROGRESS_WIDTH = 0.6f;

	public static final float HEIGHT_PROGRESS_LEFT = 0.05f;
	public static final float HEIGHT_PROGRESS_TOP = 0.6f;
	public static final float HEIGHT_PROGRESS_WIDTH = 0.06f;
	public static final float HEIGHT_PROGRESS_HEIGHT = 0.25f;

	public static final float TILT_PROGRESS_LEFT = 0.06f;
	public static final float TILT_PROGRESS_TOP = 0.92f;
	public static final float TILT_PROGRESS_WIDTH = 0.3f;
	public static final float TILT_PROGRESS_HEIGHT = 0.04f;


	// activity
	public static final String GAME_ACTIVITY = "edu.neu.mhealth.qutang.towerairdrop.activities.GameActivity";
	
	//Score
	public static SharedPreferences SHAREDPREFERENCES = null;

}
