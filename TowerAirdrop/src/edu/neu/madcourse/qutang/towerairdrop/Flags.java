package edu.neu.madcourse.qutang.towerairdrop;

import org.jbox2d.common.Vec2;

import edu.neu.madcourse.qutang.towerairdrop.R;
import edu.neu.madcourse.qutang.towerairdrop.Constants.EMERGENCY_TYPE;
import edu.neu.madcourse.qutang.towerairdrop.Constants.PAUSE_FROM;
import edu.neu.madcourse.qutang.towerairdrop.Constants.PROGRESS_TYPE;
import edu.neu.madcourse.qutang.towerairdrop.Constants.RESUME_FROM;

/**
 * @author Qu
 *
 */
public class Flags {
	
	/**
	 * Global parameters to be saved
	 */
	//Screen size
	public float screenW = Constants.BASELINE_SCREEN_WIDTH;
	public float screenH = Constants.BASELINE_SCREEN_HEIGHT;
	//Scene
	public int sensorScenario = Constants.INIT_SCENARIO;
	//Scene state, DON'T SAVE
	public Constants.GAME_EVENTS scenarioStatus = Constants.GAME_EVENTS.INIT_SCENE;
	//Current Level
	public int currentLevel = Constants.BASE_LEVEL; //SAVE IT
	//Current screen and camera offset
	public Vec2 screenOffset = new Vec2(0f, 0f);
	public Vec2 cameraCenter = new Vec2(0f, 0f);
	
	/**
	 * Level parameters to be computed, DON'T SAVE EXCEPT FOR SOME
	 */
	//Current level box score
	public int currentScore = Constants.BASE_BOX_SCORE;
	//Total tilt time, will be set up automatically by level
	public long totalTiltTime = Constants.BASE_TILT_TIME; // in sec
	//The drop speed of parachute and box, based on the SIII, now it's fixed
	public float boxDropSpeed = Constants.PARACHUTE_DOWN_SPEED;
	//The init horizontal pos of the tower, randomly generated at each level
	public float initBasePosX = 0.9f; //0~1, SAVE IT!
	//The max offset of tilting, should be greater than 1
	public float max_offset = Constants.BASE_TILT_OFFSET; 
	//The initial shake progress bar position
	public float shakeProgress = Constants.BASE_SHAKE_PROGRESS; // 0~1
	//The total length of shake progress bar
	public int totalMove = 500; // any number, smaller requires shorter time (fixed number)
	//The sensitivity of shaking
	public float shakeSensitivity = Constants.BASE_SHAKE_SENSITIVITY; // will be converted to 1/totalMove + shakeProgress
	//Parachute gone position: counted as how many boxes above
	public float parachuteGonePos = Constants.BASE_PARACHUTE_GONE_POS;
	
	
	/**
	 * 	Used in init scene
	 */
	//Static object, these can all be reused, no need to recreate
	public BackgroundObject bg = new BackgroundObject();
	public ButtonObject btnPause = new ButtonObject();
	public ButtonObject btnQuit = new ButtonObject();
	public TextObject scoreText = new TextObject(R.drawable.coin);
	public TextObject levelText = new TextObject(R.drawable.trophy);
	
	/**
	 * Used in shaking scene
	 */
	//progress bar, no need to recreate
	public ProgressObject progressBar = new ProgressObject(R.drawable.parachute, PROGRESS_TYPE.PARACHUTE);
	//emergency, no need to recreate
	public EmergencyObject currentEmergency = new EmergencyObject(EMERGENCY_TYPE.STUCK);
	//Flag of whether in emergency
	public boolean isInEmergency = false;
	public boolean emergencyInQueue = false;
	public long emergencyTimer = 0;
	public float emergencyInterval = 0; //in milli
	public long shakeCounter = 0;
	public long previousShakeCounter = 0;
	public boolean isGamePaused = false;
	
	
	/**
	 * Used in tilting scene
	 */
	//Static object, no need to recreate
	public ProgressObject heightBar = new ProgressObject(R.drawable.box, PROGRESS_TYPE.HEIGHT);
	public ProgressObject tiltBar = new ProgressObject(R.drawable.box, PROGRESS_TYPE.TILT);
	public DeadlineObject deadline = new DeadlineObject();
	
	//Tilt time that has passed, will update by codes
	public long currentTiltTime = 0; //SAVE AND LOAD
	//Visual shaking effect of the box, based on SIII, now is fixed, but later may used to create interesting visual effect
	public float shakeSpeed = Constants.BOX_SHAKE_SPEED; 
	//Flags for tower creation
	public boolean isTowerReady = false;
	public boolean isTowerCreated = false;
	//Flags for stacking status
	public boolean isStacked = false;
	
	//Number of obstacles in the scene
	public int num_obstacles = 0; 
	
	//Timer of obstacle generator
	//Current time
	public long obstacleTimer = 0; 
	//Interval between now and the next obstacle
	public long obstacleInterval = 0;
	
	/**
	 * Parameters that have to be set at every scene state
	 */
	//Whether to use Smart Camera
	public boolean useSmartCamera = false;
	
	//The timer for landscape generation, which will be used in both shaking and tilting scenes
	//Current time
	public long landscapeTimer = 0;
	//Interval between now and next landscape
	public float landscapeInterval = 0;
	//The moving speed of landscape
	public float landscapeSpeed = Constants.LANDSCAPE_SPEED_SHAKING;

	
	// Instruction Mode
	public boolean instructionShakeMode = true;
	public boolean instructionTitleMode = true;
	public boolean firstlowShakeDialog = true;
	public boolean instructionDialogSelect = false;
	public boolean sensorStart = false;
	public RESUME_FROM resumeFrom = RESUME_FROM.LOCK;
	public PAUSE_FROM pauseFrom = PAUSE_FROM.COVER;


	private static Flags mInstance = null;

	private Flags() {
	}

	public static Flags getFlags() {
		if (mInstance == null) {
			mInstance = new Flags();
			return mInstance;
		} else {
			return mInstance;
		}
	}

	public static Flags resetFlags() {
		mInstance = new Flags();
		return mInstance;
	}
}
