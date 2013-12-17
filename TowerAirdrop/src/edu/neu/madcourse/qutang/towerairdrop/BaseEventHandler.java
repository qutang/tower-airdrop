package edu.neu.madcourse.qutang.towerairdrop;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;

import org.jbox2d.callbacks.ContactImpulse;
import org.jbox2d.callbacks.ContactListener;
import org.jbox2d.collision.Manifold;
import org.jbox2d.common.Vec2;
import org.jbox2d.dynamics.Body;
import org.jbox2d.dynamics.contacts.Contact;

import android.app.Activity;
import android.app.Dialog;
import android.app.Service;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.graphics.Color;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Typeface;
import android.os.Handler;
import android.os.Message;
import android.os.Vibrator;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import edu.neu.madcourse.qutang.towerairdrop.R;
import edu.neu.madcourse.qutang.towerairdrop.BaseObject.ObjectType;
import edu.neu.madcourse.qutang.towerairdrop.Constants.BUTTON_TYPE;
import edu.neu.madcourse.qutang.towerairdrop.Constants.DIALOG_FROM;
import edu.neu.madcourse.qutang.towerairdrop.Constants.EMERGENCY_TYPE;
import edu.neu.madcourse.qutang.towerairdrop.Constants.GAME_EVENTS;
import edu.neu.madcourse.qutang.towerairdrop.Constants.PAUSE_FROM;

public class BaseEventHandler extends Handler implements OnClickListener,
		ContactListener {

	private static final String TAG = "BaseEventHandler";
	private GameView mGameView;
	private String direction_flag;
	private Vec2 actor1Pos = null; // very important init pos for actor1 to
									// appear, vary for each scenario
	private Vec2 actor2Pos = null; // very important init pos for actor2 to
									// appear, vary for each scenario
	private Vec2 actor3Pos = null; // very important init pos for actor3 to
									// appear, vary for each scenario
	private int actor1Direction = 0; // very important init direction for actor1
										// to move, default is vertical, for
										// dynamic body, don't need to set
	private int actor2Direction = 0;
	private int actor3Direction = 0;
	private BaseObject actor1 = null; // normally this is the main box
	private BaseObject actor2 = null; // second important object, could be plane
										// or parachute
	private BaseObject actor3 = null; // third important object

	private ArrayList<BoxObject> tower = new ArrayList<BoxObject>();
	private Random r = new Random();

	private float screenW;
	private float screenH;

	private long boxShakeTimer;

	public static BaseObjectPool objPool = new BaseObjectPool();

	private BoxObject testLand;

	private GameApplication gApplication;

	private Timer timer;

	private Dialog dialog;
	
	private Dialog quiteDialog;

	private TimerTask task;
	private float initBoxX;
	private float offsetBoxX;
	private float towerPosY;
	private float towerPosX;

	// private AudioManager audioMgr;
	//
	// private int maxVolumn = 50;
	// private int curVolume = 50;
	// private int minVolumn = 0;

	/** this is only a test for orientation sensor **/
	// private TextView mFlagView;

	public BaseEventHandler(GameView _gameView) {
		super();
		this.mGameView = _gameView;
		Flags.getFlags().firstlowShakeDialog = true;
		Flags.getFlags().sensorStart = false;
		gApplication = (GameApplication) mGameView.getContext()
				.getApplicationContext();
		/** this is only a test for orientation sensor **/
		// this.mFlagView = _flatView;
	}

	@Override
	public void dispatchMessage(Message msg) {
		Constants.GAME_EVENTS event = Constants.GAME_EVENTS.values()[msg.what];
		switch (event) {
		case HANDLER_TEST:
			break;
		case BEING_SHAKING:
			// update progress bar
			if(Flags.getFlags().isGamePaused){
				return;
			}
			if (Flags.getFlags().shakeProgress * Flags.getFlags().totalMove < Flags
					.getFlags().totalMove) {
				Flags.getFlags().shakeProgress += Flags.getFlags().shakeSensitivity
						/ Flags.getFlags().totalMove;
				Music.stopSound(mGameView.getContext());
			}
			if (Flags.getFlags().previousShakeCounter
					- Flags.getFlags().shakeCounter > Constants.EMERGENCY_REST_INTERVAL
					&& Flags.getFlags().previousShakeCounter <= Constants.EMERGENCY_SHAKING_INTERVAL) {
				Flags.getFlags().shakeCounter = Flags.getFlags().previousShakeCounter;
			} else {
				Flags.getFlags().previousShakeCounter = Flags.getFlags().shakeCounter;
			}
			Flags.getFlags().shakeCounter++;
			break;
		case PAUSE_SHAKING:
			if(Flags.getFlags().isGamePaused){
				return;
			}
			if (Flags.getFlags().shakeProgress * Flags.getFlags().totalMove > 0) {
				Flags.getFlags().shakeProgress -= Flags.getFlags().shakeSensitivity
						/ Flags.getFlags().totalMove;
				Music.playSound(mGameView.getContext(), R.raw.alarm);
			}
			Flags.getFlags().shakeCounter--;
			break;
		case INIT_SCENE:
			if (msg.obj != null) {
				screenW = ((float[]) msg.obj)[0];
				screenH = ((float[]) msg.obj)[1];
				Flags.getFlags().screenW = screenW;
				Flags.getFlags().screenH = screenH;
			}
			(new Runnable() {
				@Override
				public void run() {
					createWorld();
				}
			}).run();
			
			break;
		case SCENE_CREATED:
			onWorldCreated();
			break;
		case ACTION_READY:
			(new Runnable() {
				@Override
				public void run() {
					onActionReady();
				}
			}).run();
			break;
		case GAME_RUNNING:
			(new Runnable() {
				@Override
				public void run() {
					onGameRunning();
				}
			}).run();
			break;
		case SCENE_ENDING:
			onSceneEnding();
			break;
		case NEXT_SCENE:
			onNextScene();
			break;
		case SHOW_TOAST:
			showToast((String) msg.obj);
			break;
		case BOX_COLLIDE:
			Music.playSound(mGameView.getContext(), R.raw.collision);
			offerVibration();
			// update score
			Flags.getFlags().currentScore -= 10;
			if (Flags.getFlags().currentScore <= 0) {
				dialog.dismiss();
				Flags.getFlags().currentScore = 0;
				Flags.getFlags().scoreText.text = String.format("%d", 0);
				Flags.getFlags().levelText.text = String.format("%d",Flags.getFlags().currentLevel);
				caculateScore();
				onGameOver(false);
				gApplication.setScore(0);
			}
			Flags.getFlags().scoreText.text = String.format("%d",Flags.getFlags().currentScore);
			Toast.makeText(mGameView.getContext(), "-10", Toast.LENGTH_SHORT)
					.show();
			break;
		case GOOD_SHAKE:
			offerVibration(500);
			StringBuilder b = new StringBuilder();
			b.append("You have done a very nice job in the practice!\n\n")
				.append("Now prepare yourself to play in the real game.\n\n")
				.append("Your box has a parachute with it but it is not open yet. Keep shaking continuously to trigger the parachute!");
//			popDialog(b.toString(), Gravity.TOP, 3000);
			okEnablePopDialog(b.toString(), Gravity.TOP, false, true, DIALOG_FROM.PAUSE);
			
			break;
		case SLOW_SHAKE:
			if (Flags.getFlags().firstlowShakeDialog) {
				Flags.getFlags().firstlowShakeDialog = false;
			} else {
				offerVibration(500);
				SensorListener.getInstance(null).stop();
				Flags.getFlags().firstlowShakeDialog = true;
				okEnablePopDialog("Let us shake harder, you can do it!", Gravity.TOP, false, true, DIALOG_FROM.PAUSE);
			}
			break;
		case TILT_INSTRUCTION:
			SensorListener.getInstance(null).stop();
			instructionTwoPopDialog(Gravity.CENTER, 7000);
			break;
		case EMERGENCY:
			handleEmergency();
			break;
		case GAME_PAUSE:
			popDialog("Game is paused.", 3000);
			break;
		case GAME_RESUME:
			popDialog("Game is resumed.", 3000);
			break;
		case GAME_QUIT:
			b = new StringBuilder();
			b.append("Do you really want to go back to the main menu?");
			Physics.pausePhysicalThread();
			SensorListener.getInstance(null).stop();
			okEnablePopDialog(b.toString(), Gravity.CENTER, true, false, DIALOG_FROM.PAUSE);
			break;
		case GAME_OVER:
			onGameOver(false);
			break;
		default:
			break;
		}
	}

	private void onGameOver(boolean stop) {
		if(stop){
			Physics.stopPhysicalThread();
		}
		StringBuilder b = new StringBuilder();
		b.append("I am sorry but the game is over!\n")
		.append("You total score is: ")
		.append(gApplication.getScore())
		.append("\n\n")
		.append("Click OK to restart from level 1.");
		gameOVerPopDialog(b.toString(), Gravity.CENTER);
	}

	private void handleEmergency() {
		// stop sensor listener
		if (!Flags.getFlags().currentEmergency.solved) {
			SensorListener.getInstance(null).stop();
			offerVibration(500);
			StringBuilder b = new StringBuilder();

			switch (Flags.getFlags().currentEmergency.getType()) {
			case STUCK:
				b.append("Emergency! The box got locked!\n\n")
					.append("You have to keep tapping on the box to unlock it before it's too late!\n");
				okEnablePopDialog(b.toString(),
						Gravity.TOP, false, true, DIALOG_FROM.EMERGENCY);
//				popDialog(
//						"Emergency! The box got locked, tap continuously to unlock it before it's too late!",
//						Gravity.TOP, 3000);
				break;
			case BLIND:
				b.append("Emergency! The box got hidden by the cloud in the center of the screen!\n\n")
					.append("You have to keep swiping over the cloud in the center to get your box back!");
				okEnablePopDialog(b.toString(),
						Gravity.TOP, false, true, DIALOG_FROM.EMERGENCY);
//				popDialog(
//						"Emergency! The box got stuck by clouds, swipe over the clouds to get your box back!",
//						Gravity.TOP, 3000);
				break;
			case LIGHTNING:
				b.append("Emergency! The box encountered the weird lightning\n\n")
					.append("The lightening will run away when you tap it\n\n")
					.append("You have to tap on and catch it for several times to dismiss the lightning!");
				okEnablePopDialog(b.toString(),
						Gravity.TOP, false, true, DIALOG_FROM.EMERGENCY);
//				popDialog(
//						"Emergency! The box encountered the lightning, tap to dismiss the lightning before it damaged the box!",
//						Gravity.TOP, 3000);
				break;
			}
		} else {
			popDialog(
					"The emergency has been solved, keep shaking continuously to get the parachute.",
					Gravity.TOP, 3000);
		}
	}

	private void onSceneEnding() {
		// get the screen offset
		updateScreenOffset();
		switch (Flags.getFlags().sensorScenario) {
		case Constants.INIT_SCENARIO:
			Flags.getFlags().scenarioStatus = Constants.GAME_EVENTS.NEXT_SCENE;
			break;
		case Constants.SHAKING_SCENARIO:
			Flags.getFlags().scenarioStatus = Constants.GAME_EVENTS.NEXT_SCENE;
			break;
		case Constants.TILTING_SCENARIO:
			SensorListener.getInstance(null).stop();
			// wait until the box is static
			if (actor1.mBody.getLinearVelocity().y == 0
					&& Flags.getFlags().scenarioStatus != GAME_EVENTS.NEXT_SCENE
					&& !dialog.isShowing()) {
				if (actor3.inContact == true) {
					// not stack successfully
					Flags.getFlags().isStacked = false;
					okEnablePopDialog("You lost a box, but it's OK, try a new box",Gravity.CENTER, true, true, DIALOG_FROM.PAUSE);
					
				} else {
					// stack successfully
					Flags.getFlags().isStacked = true;
					okEnablePopDialog("You stacked the box, next level is coming...",Gravity.CENTER, true, true, DIALOG_FROM.PAUSE);
					Flags.getFlags().currentLevel++;
					gApplication.setScore(gApplication.getScore() + Integer.valueOf(Flags.getFlags().currentScore)* Integer.valueOf(Flags.getFlags().currentLevel));
					int totalScore = Integer.valueOf(Constants.SHAREDPREFERENCES.getString("TotalScore", "0"));
					int totalLevel = Integer.valueOf(Constants.SHAREDPREFERENCES.getString("totalLevel", "0"));
					Editor editor = Constants.SHAREDPREFERENCES.edit();
					editor.putString("TotalScore",String.valueOf(Flags.getFlags().currentScore * Flags.getFlags().currentLevel+ totalScore));
					editor.putString("totalLevel",String.valueOf(1 + totalLevel));
					editor.commit();
				}
			}
			break;
		}
		Physics.resumePhysicalThread();
		
	}

	private void onNextScene() {
		switch (Flags.getFlags().sensorScenario) {
		case Constants.INIT_SCENARIO:
			Flags.getFlags().sensorScenario = Constants.SHAKING_SCENARIO;
			break;
		case Constants.SHAKING_SCENARIO:
			popDialog("Yeah! The parachute is triggered.", Gravity.CENTER, 3000);
			Flags.getFlags().instructionTitleMode = true;
			offerVibration(500);
			SensorListener.getInstance(null).stop();
			Flags.getFlags().sensorScenario = Constants.TILTING_SCENARIO;
			break;
		case Constants.TILTING_SCENARIO:
			if (Flags.getFlags().isStacked) {
//				int totalScore = Integer.valueOf(Constants.SHAREDPREFERENCES.getString("TotalScore", "0"));
//				int totalLevel = Integer.valueOf(Constants.SHAREDPREFERENCES.getString("totalLevel", "0"));
//				Editor editor = Constants.SHAREDPREFERENCES.edit();
//				editor.putString("TotalScore",String.valueOf(Flags.getFlags().currentScore * Flags.getFlags().currentLevel+ totalScore));
//				editor.putString("totalLevel",String.valueOf(1 + totalLevel));
//				editor.commit();
//				Flags.getFlags().currentLevel++;
			}

			Flags.getFlags().sensorScenario = Constants.INIT_SCENARIO;
			// save game
			saveGame();
			break;
		}
		Physics.stopPhysicalThread(); // this comes first
		Physics.resumePhysicalThread();
	}

	private void clearUpScene() {
		// clear up graphs
		synchronized (GameView.actors) {
			GameView.actors.clear();
		}
		
		GameView.resetId();
		GameView.mainBox = null;
		
		synchronized (GameView.afterMainBox) {
			GameView.afterMainBox.clear();
		}
		synchronized (GameView.beforeMainBox) {
			GameView.beforeMainBox.clear();
		}
		actor1 = null;
		actor2 = null;
		actor3 = null;
		actor1Pos = null;
		actor2Pos = null;
		actor3Pos = null;
		actor1Direction = 0;
		actor2Direction = 0;
		actor3Direction = 0;
	}

	private void onGameRunning() {
		updateScreenOffset();
		switch (Flags.getFlags().sensorScenario) {
		case Constants.INIT_SCENARIO:
			// scene ends, go to next scene
			if (actor1.getNoOffsetPosition().y >= screenH) {
				Flags.getFlags().scenarioStatus = Constants.GAME_EVENTS.NEXT_SCENE;
			}
			break;
		case Constants.SHAKING_SCENARIO:
			// make box shaking
			makeMainBoxShaking();
			// generate next landscape
			if (Flags.getFlags().landscapeTimer == 0) { // init landscape timer
				Flags.getFlags().landscapeTimer = System.currentTimeMillis();
				Flags.getFlags().landscapeInterval = r.nextFloat()
						* Constants.LANDSCAPE_RANGE_INTERVAL_SHAKING
						+ Constants.LANDSCAPE_MIN_INTERVAL_SHAKING;
			}
			if (System.currentTimeMillis() - Flags.getFlags().landscapeTimer >= Flags
					.getFlags().landscapeInterval * 1000) {
				synchronized (GameView.actors) {
					generateNextLandscape(false,
							Flags.getFlags().landscapeSpeed);
				}
				Flags.getFlags().landscapeTimer = System.currentTimeMillis();
				Flags.getFlags().landscapeInterval = r.nextFloat()
						* Constants.LANDSCAPE_RANGE_INTERVAL_SHAKING
						+ Constants.LANDSCAPE_MIN_INTERVAL_SHAKING;
			}
			// if (testLand != null) {
			// Log.v(TAG, testLand.getPosition().toString());
			// }

			// generate emergency
			if (!Flags.getFlags().instructionShakeMode || !FinalPrefs.getInstruction(mGameView.getContext())) {
				generateNextEmergency();
			}

			// update progress bar
			Flags.getFlags().progressBar.progress = Flags.getFlags().shakeProgress;

			// When progress Bar is full or empty
			if (Flags.getFlags().shakeProgress >= 1) {
				Flags.getFlags().scenarioStatus = Constants.GAME_EVENTS.NEXT_SCENE;
			} else if (Flags.getFlags().shakeProgress <= 0) {
				caculateScore();
				sendEmptyMessage(GAME_EVENTS.GAME_OVER.ordinal());
			}

			// destroy landscape if it's out of screen
			destroyObjects();

			break;
		case Constants.TILTING_SCENARIO:
			if (!Flags.getFlags().instructionTitleMode || !FinalPrefs.getInstruction(mGameView.getContext())) {
				// init the start tilt time
				if (Flags.getFlags().currentTiltTime == 0) {
					Flags.getFlags().currentTiltTime = System
							.currentTimeMillis();
				}

				// update height progress bar
				Flags.getFlags().heightBar.progress = (System
						.currentTimeMillis() - Flags.getFlags().currentTiltTime)
						/ (float) Flags.getFlags().totalTiltTime / 1000;

				// update horizontal tilt bar
				offsetBoxX = (actor1.getNoOffsetPosition().x - initBoxX)
						/ (screenW * Flags.getFlags().max_offset);
				Flags.getFlags().tiltBar.progress = Flags.getFlags().initBasePosX
						- offsetBoxX;

				updateTowerStatus();

				updateParachuteStatus();

				// tower is ready to be stacked
				if (Flags.getFlags().isTowerReady == true) {
					if (actor2 != null) {
						// before parachute is gone, move up camera
						Flags.getFlags().cameraCenter.y += scaleSpeedOrForces(
								Flags.getFlags().boxDropSpeed, false);
						// at certain time point, parachute gone
						if (actor2 != null
								&& actor1.getPosition().y >= Flags.getFlags().screenH
										- (Flags.getFlags().currentLevel - 1)
										* Constants.BOX_SIZE
										* Flags.getFlags().screenW
										* 0.8f
										- Constants.BOX_SIZE
										* Flags.getFlags().screenW
										- (Flags.getFlags().parachuteGonePos)
										* Constants.BOX_SIZE
										* Flags.getFlags().screenW) {
							Physics.destroyParachuteJoint();
							actor2.destroySelf();
							actor2 = null;
						} else {
							// show the parachute gone line
							Flags.getFlags().deadline.setPosition(Flags
									.getFlags().screenH
									- (Flags.getFlags().currentLevel - 1)
									* Constants.BOX_SIZE
									* Flags.getFlags().screenW
									* 0.8f
									- Constants.BOX_SIZE
									* Flags.getFlags().screenW
									/ 2
									- (Flags.getFlags().parachuteGonePos)
									* Constants.BOX_SIZE
									* Flags.getFlags().screenW);
							Flags.getFlags().deadline.visible = true;
						}
					} else {
						// after parachute is gone
						Flags.getFlags().deadline.visible = false;
						Flags.getFlags().cameraCenter.y += scaleSpeedOrForces(
								Constants.BOX_STACK_SPEED, false);

					}
					fixTowerAndGround(true);
				}
				// tower is not ready to be stacked
				else {
					// generate next landscape every 2~4s
					if (Flags.getFlags().landscapeTimer == 0) {
						Flags.getFlags().landscapeTimer = System
								.currentTimeMillis();
						Flags.getFlags().landscapeInterval = (r.nextFloat()
								* Constants.LANDSCAPE_RANGE_INTERVAL_TILT + Constants.LANDSCAPE_MIN_INTERVAL_TILT) * 1000;
					}

					if (System.currentTimeMillis()
							- Flags.getFlags().landscapeTimer >= Flags
								.getFlags().landscapeInterval) {
						generateNextLandscape(false, 0.0f);
						Flags.getFlags().landscapeTimer = System
								.currentTimeMillis();
						Flags.getFlags().landscapeInterval = (r.nextFloat()
								* Constants.LANDSCAPE_RANGE_INTERVAL_TILT + Constants.LANDSCAPE_MIN_INTERVAL_TILT) * 1000;
					}

					// generate next obstacle every 0.5-1.5s
					if (Flags.getFlags().obstacleTimer == 0) {
						Flags.getFlags().obstacleTimer = System
								.currentTimeMillis();
						Flags.getFlags().obstacleInterval = (long) ((r
								.nextFloat()
								* Constants.OBSTACLE_RANGE_INTERVAL + Constants.OBSTACLE_MIN_INTERVAL) * 1000);
					}
					if (System.currentTimeMillis()
							- Flags.getFlags().obstacleTimer >= Flags
								.getFlags().obstacleInterval) {
						generateNextObstacle(true);
						Flags.getFlags().obstacleTimer = System
								.currentTimeMillis();
						Flags.getFlags().obstacleInterval = (long) ((r
								.nextFloat()
								* Constants.OBSTACLE_RANGE_INTERVAL + Constants.OBSTACLE_MIN_INTERVAL) * 1000);
					}
				}
				// destroy object if it's out of screen
				destroyObjects();
			}
			// During instruction
			else {
				// only generate next landscape every 2~4s
				if (Flags.getFlags().landscapeTimer == 0) {
					Flags.getFlags().landscapeTimer = System
							.currentTimeMillis();
					Flags.getFlags().landscapeInterval = (r.nextFloat()
							* Constants.LANDSCAPE_RANGE_INTERVAL_TILT + Constants.LANDSCAPE_MIN_INTERVAL_TILT) * 1000;
				}

				if (System.currentTimeMillis()
						- Flags.getFlags().landscapeTimer >= Flags.getFlags().landscapeInterval) {
					generateNextLandscape(false, 0.0f);
					Flags.getFlags().landscapeTimer = System
							.currentTimeMillis();
					Flags.getFlags().landscapeInterval = (r.nextFloat()
							* Constants.LANDSCAPE_RANGE_INTERVAL_TILT + Constants.LANDSCAPE_MIN_INTERVAL_TILT) * 1000;
				}
			}
			break;
		}
		Physics.resumePhysicalThread();
	}

	private void initInitInterface() {
		// set graphics
		// set background
		BackgroundObject background = Flags.getFlags().bg;
		background.setPosition(new Rect(0, 0, (int) screenW, (int) screenH));

		background.setBackground(R.drawable.day_sky);

		// set pause and quit buttons
		Flags.getFlags().btnPause = new ButtonObject(R.drawable.pause,
				BUTTON_TYPE.PLAY, this);
		Flags.getFlags().btnPause.posRect = new RectF();
		Flags.getFlags().btnPause.posRect.set(screenW * Constants.BUTTON_LEFT,
				screenH * Constants.BUTTON_TOP, screenW
						* (Constants.BUTTON_LEFT + Constants.BUTTON_SIZE),
				screenH * Constants.BUTTON_TOP + screenW
						* Constants.BUTTON_SIZE);

		Flags.getFlags().btnQuit = new ButtonObject(R.drawable.main,
				BUTTON_TYPE.QUIT, this);
		Flags.getFlags().btnQuit.visible = true;
		Flags.getFlags().btnQuit.posRect = new RectF();
		Flags.getFlags().btnQuit.posRect
				.set(screenW
						* (Constants.BUTTON_LEFT + Constants.BUTTON_SIZE + Constants.BUTTON_GAP),
						screenH * Constants.BUTTON_TOP,
						screenW
								* (Constants.BUTTON_LEFT + Constants.BUTTON_GAP + 2 * Constants.BUTTON_SIZE),
						screenH * Constants.BUTTON_TOP + screenW
								* Constants.BUTTON_SIZE);

		// set score and level texts
		Flags.getFlags().scoreText.setColor(Color.BLACK);
		Flags.getFlags().scoreText.setPosition(
				(int) (screenW * Constants.SCORE_LEFT),
				(int) (screenH * Constants.SCORE_TOP), screenW
						* Constants.SCORE_SIZE);
		Flags.getFlags().scoreText.text = String.format("%d",
				Flags.getFlags().currentScore);

		Flags.getFlags().levelText.setColor(Color.BLACK);
		Flags.getFlags().levelText.setPosition(
				(int) (screenW * Constants.LEVEL_LEFT),
				(int) (screenH * Constants.LEVEL_TOP), screenW
						* Constants.LEVEL_SIZE);
		Flags.getFlags().levelText.text = String.format("%d",
				Flags.getFlags().currentLevel);

		// set visibility of staffs
		Flags.getFlags().progressBar.visible = false;
		Flags.getFlags().heightBar.visible = false;
		Flags.getFlags().tiltBar.visible = false;
		Flags.getFlags().deadline.visible = false;
	}

	private void initShakeInterface() {
		// show progress bar
		ProgressObject progressBar = Flags.getFlags().progressBar;
		progressBar
				.setPosition(new RectF(
						Constants.PARACHUTE_PROGRESS_LEFT * screenW,
						Constants.PARACHUTE_PROGRESS_TOP * screenH,
						screenW
								* (Constants.PARACHUTE_PROGRESS_LEFT + Constants.PARACHUTE_PROGRESS_WIDTH),
						screenH
								* (Constants.PARACHUTE_PROGRESS_TOP + Constants.PARACHUTE_PROGRESS_HEIGHT)));
		progressBar.progress = Flags.getFlags().shakeProgress;

		// set visibility of staffs
		Flags.getFlags().progressBar.visible = true;
		Flags.getFlags().heightBar.visible = false;
		Flags.getFlags().tiltBar.visible = false;
		Flags.getFlags().deadline.visible = false;
	}

	private void initTiltInterface() {
		// reset current tilt time
		Flags.getFlags().currentTiltTime = 0;

		// show height progress bar
		ProgressObject heightBar = Flags.getFlags().heightBar;
		heightBar
				.setPosition(new RectF(
						Constants.HEIGHT_PROGRESS_LEFT * screenW,
						Constants.HEIGHT_PROGRESS_TOP * screenH,
						screenW
								* (Constants.HEIGHT_PROGRESS_LEFT + Constants.HEIGHT_PROGRESS_WIDTH),
						screenH
								* (Constants.HEIGHT_PROGRESS_TOP + Constants.HEIGHT_PROGRESS_HEIGHT)));
		heightBar.progress = (System.currentTimeMillis() - Flags.getFlags().currentTiltTime)
				/ (float) Flags.getFlags().totalTiltTime / 1000;

		// show horizontal radar bar
		ProgressObject tiltBar = Flags.getFlags().tiltBar;
		tiltBar.setPosition(new RectF(
				Constants.TILT_PROGRESS_LEFT * screenW,
				Constants.TILT_PROGRESS_TOP * screenH,
				screenW
						* (Constants.TILT_PROGRESS_LEFT + Constants.TILT_PROGRESS_WIDTH),
				screenH
						* (Constants.TILT_PROGRESS_TOP + Constants.TILT_PROGRESS_HEIGHT)));
		tiltBar.progress = Flags.getFlags().initBasePosX;

		// create and hide deadline, but location not set up yet
		Flags.getFlags().deadline
				.setText("Parachute will disappear after this line");

		// set visibility of staffs
		Flags.getFlags().progressBar.visible = false;
		Flags.getFlags().heightBar.visible = true;
		Flags.getFlags().tiltBar.visible = true;
		Flags.getFlags().deadline.visible = false;
	}

	// This should be called before init interface
	private void initLevel() {
		Flags.getFlags().currentScore = (int) (Constants.BASE_BOX_SCORE + Constants.INCREASED_BOX_SCORE
				* Math.floor(Flags.getFlags().currentLevel
						/ Constants.SCORE_INCREASE_INTERVAL)); // increase 5
																// points every
		// 5 levels
		// set tilt time
		Flags.getFlags().totalTiltTime = (long) (Constants.BASE_TILT_TIME + Constants.INCREASED_TILT_TIME
				* Math.floor(Flags.getFlags().currentLevel
						/ Constants.TILT_TIME_INCREASE_INTERVAL)); // increase 2
																	// sec every
		// 10 levels
		// set parachute down speed
		Flags.getFlags().boxDropSpeed = Constants.PARACHUTE_DOWN_SPEED;

		// set shake sensitivity
		Flags.getFlags().shakeSensitivity = (float) (Constants.BASE_SHAKE_SENSITIVITY + Constants.DECREASED_SHAKE_SENSITIVITY
				* Math.floor(Flags.getFlags().currentLevel
						/ Constants.SHAKE_SENSITIVITY_CHANGE_INTERVAL));
		if (Flags.getFlags().shakeSensitivity < Constants.MIN_SHAKE_SENSITIVITY) {
			Flags.getFlags().shakeSensitivity = Constants.MIN_SHAKE_SENSITIVITY;
		}

		// set initial shake progress position
		Flags.getFlags().shakeProgress = (float) (Constants.BASE_SHAKE_PROGRESS + Constants.DECREASED_SHAKE_PROGRESS
				* Math.floor(Flags.getFlags().currentLevel
						/ Constants.SHAKE_PROGRESS_CHANGE_INTERVAL));
		if (Flags.getFlags().shakeProgress < Constants.MIN_SHAKE_PROGRESS) {
			Flags.getFlags().shakeProgress = Constants.MIN_SHAKE_PROGRESS;
		}

		// set max offset of tilting
		Flags.getFlags().max_offset = (float) (Constants.BASE_TILT_OFFSET + Constants.INCREASED_TILT_OFFSET
				* Math.floor(Flags.getFlags().currentLevel
						/ Constants.TILT_OFFSET_INCREASE_INTERVAL)); // increase
																		// 0.5
																		// every
		// 10 levels

		// set the position when parachute is gone
		Flags.getFlags().parachuteGonePos = (float) (Constants.BASE_PARACHUTE_GONE_POS + Constants.INCREASED_PARACHUTE_GONE_POS
				* Math.floor(Flags.getFlags().currentLevel
						/ Constants.PARACHUTE_GONE_POS_INCREASE_INTERVAL));
		if (Flags.getFlags().parachuteGonePos > Constants.MAX_PARACHUTE_GONE_POS) {
			Flags.getFlags().parachuteGonePos = Constants.MAX_PARACHUTE_GONE_POS;
		}

		// generate random base position
		Flags.getFlags().initBasePosX = r.nextFloat();
		
		// set up instruction
//		if(Flags.getFlags().currentLevel <= 3){
//			Flags.getFlags().instructionShakeMode = true;
//			Flags.getFlags().instructionTitleMode = true;
//			Flags.getFlags().firstlowShakeDialog = true;
//			Flags.getFlags().instructionDialogSelect = false;
//			Flags.getFlags().sensorStart = false;
//		}else{
//			Flags.getFlags().instructionShakeMode = true;
//			Flags.getFlags().instructionTitleMode = false;
//			Flags.getFlags().firstlowShakeDialog = false;
//			Flags.getFlags().instructionDialogSelect = false;
//			Flags.getFlags().sensorStart = false;
//		}
	}

	private void createBoxCarrier() {
		// generate main Pos
		actor1Pos = generateMainPos(true);
		// create static carrier
		actor2 = new BoxObject(ObjectType.LANDSCAPE, Constants.CARRIER_SIZE
				* Flags.getFlags().screenW, Constants.CARRIER_SIZE
				* Flags.getFlags().screenW);

		actor2Direction = generateDirection(true);
		if (actor2Direction == 1) {
			actor2Pos = new Vec2(-Flags.getFlags().screenW * 0.15f, actor1Pos.y);
		} else {
			actor2Pos = new Vec2(actor1Pos.x, -Flags.getFlags().screenH * 0.05f);
		}
		actor2.setPosition(actor2Pos);
		actor2.setColor(Color.TRANSPARENT);
		actor2.setBitmap(R.drawable.airplane);
		actor2.createPhysicsBody(Constants.LANDSCAPE_DENSITY,
				Constants.BOX_FRICTION_DEFAULT,
				Constants.BOX_RESTITUTION_DEFAULT, null);
	}

	private void createMainBox() {
		actor1 = new BoxObject(ObjectType.MAIN_BOX, Constants.BOX_SIZE
				* screenW, Constants.BOX_SIZE * screenW);

		actor1.setColor(Color.TRANSPARENT);
		actor1.setBitmap(R.drawable.box);
		actor1.setPosition(actor1Pos);
		actor1.createPhysicsBody(Constants.BOX_DENSITY_DEFAULT,
				Constants.BOX_FRICTION_DEFAULT,
				Constants.BOX_RESTITUTION_DEFAULT, null);
	}

	private void createParachute() {
		actor2 = new TriangleObject(ObjectType.PARACHUTE,
				Constants.PARACHUTE_SIZE * screenW);
		actor2Pos = new Vec2(actor1Pos.x, actor1Pos.y
				- ((BoxObject) actor1).getHeight() / 3
				- ((TriangleObject) actor2).getDistanceToVert());
		actor2.setPosition(actor2Pos);
		actor2.setColor(Color.GRAY);
		actor2.setBitmap(R.drawable.parachute);

		actor2.createPhysicsBody(Constants.PARACHUTE_DENSITY_DEFAULT,
				Constants.PARACHUTE_FRICTION_DEFAULT,
				Constants.PARACHUTE_RESTITUTION_DEFAULT, null);
	}

	private void createStackTower(boolean ready) {
		if (!ready) {
			tower.clear();
			towerPosY = 0;
			actor3 = new BoxObject(ObjectType.BASE, screenW, screenH * 0.01f);

			actor3.setPosition(new Vec2(-100f, -100f));
			actor3.setColor(GameApplication.getAppContext().getResources()
					.getColor(R.color.gray));
			actor3.createPhysicsBody(0.0f, 1.0f,
					Constants.BOX_RESTITUTION_DEFAULT, null);

			BoxObject box;
			for (int i = 0; i < Flags.getFlags().currentLevel; i++) {

				if (i == Flags.getFlags().currentLevel - 1) {
					// the top one
					box = new BoxObject(ObjectType.BOX, Constants.BOX_SIZE
							* screenW, Constants.BOX_SIZE * screenW, 0.6f);
				} else {
					box = new BoxObject(ObjectType.BOX, Constants.BOX_SIZE
							* screenW, Constants.BOX_SIZE * screenW);
				}

				box.setPosition(new Vec2(-100f, -100f));
				box.setColor(Color.TRANSPARENT);
				box.setBitmap(R.drawable.box);
				box.createPhysicsBody(0.0f, Constants.BOX_FRICTION_DEFAULT,
						Constants.BOX_RESTITUTION_DEFAULT, null);
				tower.add(box);
			}
		} else {
			float groundPosX = screenW / 2;

			float groundPosY = screenH + (Flags.getFlags().currentLevel - 1)
					* Constants.BOX_SIZE * screenW * 0.8f + Constants.BOX_SIZE
					* screenW / 2;

			actor3.setPosition(new Vec2(groundPosX, groundPosY));

			float towerOffset = Flags.getFlags().initBasePosX - offsetBoxX
					- 0.5f;
			towerPosX = initBoxX + towerOffset * screenW
					* Flags.getFlags().max_offset;
			towerPosY = groundPosY - Constants.BOX_SIZE * screenW / 2;
			for (BoxObject box : tower) {
				box.setPosition(new Vec2(towerPosX, towerPosY));
				towerPosY -= Constants.BOX_SIZE * screenW * 0.8f;
			}
			Flags.getFlags().isTowerCreated = true;
		}
	}

	/* To initialize the world (physical and graphical) for each scenario */
	private void createWorld() {
		// reset screenoffset and camera center
		Flags.getFlags().screenOffset = new Vec2(0f, 0f);
		Flags.getFlags().cameraCenter = new Vec2(0f, 0f);

		// set up level parameters
		initLevel();

		clearUpScene();

		initInitInterface();

		switch (Flags.getFlags().sensorScenario) {
		case Constants.INIT_SCENARIO:

			// set gravity
			Vec2 gravity = scaleSpeedOrForces(Constants.GRAVITY_FORCE_DEFAULT);
			Physics.setGravity(gravity);

			// set camera
			Flags.getFlags().useSmartCamera = false;

			createBoxCarrier();

			// set scene status
			Flags.getFlags().scenarioStatus = Constants.GAME_EVENTS.SCENE_CREATED;
			break;
		case Constants.SHAKING_SCENARIO:
			// set up graphics
			initShakeInterface();

			// set Gravity to be zero
			Physics.setGravity(Constants.NO_GRAVITY);

			// set smart camera off
			Flags.getFlags().useSmartCamera = false;

			// set main pos, center of the screen before call createMainBox
			actor1Pos = new Vec2(screenW / 2, screenH / 2);
			createMainBox();

			// set main box for emergency object
			Flags.getFlags().currentEmergency.setMainBox((BoxObject) actor1);

			// reset the status of emergency
			Flags.getFlags().isInEmergency = false;
			Flags.getFlags().emergencyInQueue = false;
			Flags.getFlags().emergencyTimer = 0;
			Flags.getFlags().emergencyInterval = 0;
			Flags.getFlags().shakeCounter = 0;
			Flags.getFlags().previousShakeCounter = 0;

			// reset landscape generation timer
			Flags.getFlags().landscapeTimer = 0;
			Flags.getFlags().landscapeInterval = 0;
			Flags.getFlags().landscapeSpeed = Constants.LANDSCAPE_SPEED_SHAKING;

			Flags.getFlags().scenarioStatus = Constants.GAME_EVENTS.SCENE_CREATED;
			break;
		case Constants.TILTING_SCENARIO:

			// set up graphics
			initTiltInterface();

			// set Gravity to be zero
			Physics.setGravity(Constants.NO_GRAVITY);

			// set smart camera on
			Flags.getFlags().useSmartCamera = true;

			// set camera center
			Flags.getFlags().cameraCenter = new Vec2(screenW / 2, screenH / 2);

			// set main pos
			actor1Pos = new Vec2(screenW / 2, screenH / 2);
			createMainBox();
			createParachute();

			// Just create, not really show
			createStackTower(false);
			// reset tower status
			Flags.getFlags().isTowerCreated = false;
			Flags.getFlags().isTowerReady = false;
			Flags.getFlags().isStacked = false;

			// reset object generation
			Flags.getFlags().num_obstacles = 0;
			Flags.getFlags().obstacleTimer = 0;
			Flags.getFlags().obstacleInterval = 0;

			// reset landscape generation
			Flags.getFlags().landscapeTimer = 0;
			Flags.getFlags().landscapeInterval = 0;
			// Attention: here we have a transition in speed lower down, so the
			// landscape speed should keep as the same as shake at first
			Flags.getFlags().landscapeSpeed = Constants.LANDSCAPE_SPEED_SHAKING;

			Flags.getFlags().scenarioStatus = Constants.GAME_EVENTS.SCENE_CREATED;
			break;
		}
		Physics.startPhysicalThread();
	}

	private void onWorldCreated() {
		// get the screen offset
		updateScreenOffset();
		switch (Flags.getFlags().sensorScenario) {
		case Constants.INIT_SCENARIO:

			// set velocity for carrier
			Vec2 carrier_speed;
			if (actor2Direction == 1) {
				carrier_speed = scaleSpeedOrForces(new Vec2(
						Constants.CARRIER_SPEED, 0.0f));
				actor2.setVelocity(carrier_speed);
			} else {
				carrier_speed = scaleSpeedOrForces(new Vec2(0.0f,
						Constants.CARRIER_SPEED));
				actor2.setVelocity(carrier_speed);
			}

			// check if the position of actor2 is in the ready position
			Log.i(TAG, "current: " + actor2.getNoOffsetPosition().toString()
					+ ", destination: " + actor1Pos.toString());
			if (Math.abs(actor2.getNoOffsetPosition().x - actor1Pos.x) <= scaleSpeedOrForces(
					Constants.CARRIER_SPEED, true) * 2
					&& Math.abs(actor2.getNoOffsetPosition().y - actor1Pos.y) <= scaleSpeedOrForces(
							Constants.CARRIER_SPEED, false) * 2) { // ready
				// now
				actor2.setVelocity(new Vec2(0f, 0f));

				Flags.getFlags().scenarioStatus = Constants.GAME_EVENTS.ACTION_READY;
			}
			break;
		case Constants.SHAKING_SCENARIO:
			// do nothing, just dispatch to action ready
			Flags.getFlags().scenarioStatus = Constants.GAME_EVENTS.ACTION_READY;
			break;
		case Constants.TILTING_SCENARIO:
			// set up parachute joint
			Physics.createParachuteJoint((BoxObject) actor1,
					(TriangleObject) actor2);

			Flags.getFlags().scenarioStatus = Constants.GAME_EVENTS.ACTION_READY;
			break;
		}
		Physics.resumePhysicalThread();
	}

	private void onActionReady() {
		// get the screen offset
		updateScreenOffset();
		switch (Flags.getFlags().sensorScenario) {
		case Constants.INIT_SCENARIO:
			synchronized (GameView.actors) {
				createMainBox();
			}
			Flags.getFlags().scenarioStatus = Constants.GAME_EVENTS.GAME_RUNNING;
			break;
		case Constants.SHAKING_SCENARIO:
			// dispatch to game running
			if (!FinalPrefs.getInstruction(mGameView.getContext())) {
				popDialog("Your box has a parachute. Shake hard to get it...",
						Gravity.TOP, 3000);
			} else {
				instructionOnePopDialog(Gravity.TOP, 10000);
			}
			Flags.getFlags().scenarioStatus = Constants.GAME_EVENTS.GAME_RUNNING;
			break;
		case Constants.TILTING_SCENARIO:
			// get the init horiziontal pos of box
			initBoxX = actor1.getNoOffsetPosition().x;

			// move the center of the camera up, simulate after parachute open,
			// the speed of the box decreases
			Flags.getFlags().cameraCenter.y -= scaleSpeedOrForces(
					Constants.PARACHUTE_UP_SPEED, false);

			// generate next landscape with speed decreasing
			if (Flags.getFlags().landscapeTimer == 0) {
				Flags.getFlags().landscapeTimer = System.currentTimeMillis();
				Flags.getFlags().landscapeInterval = (r.nextFloat()
						* Constants.LANDSCAPE_RANGE_INTERVAL_TILT + Constants.LANDSCAPE_MIN_INTERVAL_TILT) * 1000;
			}
			if (System.currentTimeMillis() - Flags.getFlags().landscapeTimer >= Flags
					.getFlags().landscapeInterval) {
				synchronized (GameView.actors) {
					generateNextLandscape(false,
							Flags.getFlags().landscapeSpeed);
				}
				Flags.getFlags().landscapeSpeed += Constants.LANDSCAPE_UP_ACCEL;
				Flags.getFlags().landscapeTimer = System.currentTimeMillis();
				Flags.getFlags().landscapeInterval = (r.nextFloat()
						* Constants.LANDSCAPE_RANGE_INTERVAL_TILT + Constants.LANDSCAPE_MIN_INTERVAL_TILT) * 1000;
			}

			// generate next obstacle
			if (!Flags.getFlags().instructionTitleMode || !FinalPrefs.getInstruction(mGameView.getContext())) {
				if (Flags.getFlags().obstacleTimer == 0) {
					Flags.getFlags().obstacleTimer = System.currentTimeMillis();
					Flags.getFlags().obstacleInterval = (long) ((r.nextFloat()
							* Constants.OBSTACLE_RANGE_INTERVAL + Constants.OBSTACLE_MIN_INTERVAL) * 1000);
				}
				if (System.currentTimeMillis() - Flags.getFlags().obstacleTimer >= Flags
						.getFlags().obstacleInterval) {
					generateNextObstacle(true);
					Flags.getFlags().obstacleTimer = System.currentTimeMillis();
					Flags.getFlags().obstacleInterval = (long) ((r.nextFloat()
							* Constants.OBSTACLE_RANGE_INTERVAL + Constants.OBSTACLE_MIN_INTERVAL) * 1000);
				}
			}

			// finish speed reduce, parachute now at the upper side of the
			// screen
			if (actor2.getPosition().y <= Constants.BOX_SLOW_DOWN_POS * screenH) {
				// set up velocity of box
				Vec2 box_speed = scaleSpeedOrForces(new Vec2(0,
						Flags.getFlags().boxDropSpeed));
				actor1.setVelocity(box_speed);
				actor2.setVelocity(box_speed);

				Flags.getFlags().scenarioStatus = Constants.GAME_EVENTS.GAME_RUNNING;
			}

			// destroy object if it's out of screen
			synchronized (GameView.actors) {
				destroyObjects();
			}
			break;
		}
		Physics.resumePhysicalThread();
	}

	public void pullDirectionFlag(String direction_flag) {
		this.direction_flag = direction_flag;
	}

	public void handleTilting(String direction_flag) {
		Log.i("Orientation Test", direction_flag);
	}

	public void handleFeedback() {

	}

	public void offerVibration(int length) {
		if (FinalPrefs.getVibration(mGameView.getContext())) {
			Vibrator v = (Vibrator) mGameView.getContext().getSystemService(
					Service.VIBRATOR_SERVICE);
			v.vibrate(length);
		}
	}

	public void offerVibration() {
		if (FinalPrefs.getVibration(mGameView.getContext())) {
			offerVibration(50);
		}
	}

	// public void popDialog(String msg) {
	// if (mDialog == null || !mDialog.isShowing()) {
	// Music.stopSound(mGameView.getContext());
	// AlertDialog.Builder alert_dialog = new Builder(
	// mGameView.getContext());
	// alert_dialog.setMessage(msg);
	// alert_dialog.setPositiveButton("OK", this);
	// alert_dialog.setCancelable(false);
	// mDialog = alert_dialog.show();
	// }
	// }

	public void popDialog(String msg, long closeTime) {
		int position = Gravity.CENTER;
		popDialog(msg, position, closeTime);
	}

	public void popDialog(String msg, int position, long closeTime) {// position:
																		// Gravity.Bottom
		if (dialog == null || !dialog.isShowing()) {
			// Timer close Dialog
			startTimer();
			timer.schedule(task, closeTime);
			Music.stopSound(mGameView.getContext());
			dialog = new Dialog(mGameView.getContext(), R.style.DialogBox);
			dialog.setCancelable(false);
			Window window = dialog.getWindow();
			WindowManager.LayoutParams wlp = window.getAttributes();
			wlp.gravity = position;
			if (wlp.gravity == Gravity.TOP) {
				wlp.verticalMargin = 0.15f;
			}
			wlp.flags &= ~WindowManager.LayoutParams.FLAG_DIM_BEHIND;
			window.setAttributes(wlp);
			dialog.setContentView(R.layout.custom);
			TextView text = (TextView) dialog.findViewById(R.id.text);
			Typeface face = Typeface.createFromAsset(GameApplication
					.getAppContext().getAssets(), "fonts/Comic.ttf");
			text.setTypeface(face);
			text.setText(msg);
			dialog.show();
		}
	}

	public void gameOVerPopDialog(String msg, int position) {// position:
		// Gravity.Bottom
		if (quiteDialog == null || !quiteDialog.isShowing()) {
			Music.stopSound(mGameView.getContext());
			quiteDialog = new Dialog(mGameView.getContext(), R.style.DialogBox);
			quiteDialog.setCancelable(false);
			Window window = quiteDialog.getWindow();
			WindowManager.LayoutParams wlp = window.getAttributes();
			wlp.gravity = position;
			if (wlp.gravity == Gravity.TOP) {
				wlp.verticalMargin = 0.15f;
			}
			wlp.flags &= ~WindowManager.LayoutParams.FLAG_DIM_BEHIND;
			window.setAttributes(wlp);
			
			quiteDialog.setContentView(R.layout.close);
			Button ok_btn = (Button) quiteDialog.findViewById(R.id.ok_btn);
			Button quit_btn = (Button) quiteDialog.findViewById(R.id.quit_btn);
			
			ok_btn.setOnClickListener(new View.OnClickListener() {
				public void onClick(View v) {
					quiteDialog.dismiss();
					Flags.getFlags().isGamePaused = false;
					Flags.getFlags().currentLevel = Constants.BASE_LEVEL;
					Flags.getFlags().scenarioStatus = Constants.GAME_EVENTS.INIT_SCENE;
					Flags.getFlags().sensorScenario = Constants.INIT_SCENARIO;
					sendEmptyMessage(Constants.GAME_EVENTS.INIT_SCENE.ordinal());
				}
			});
			
			quit_btn.setOnClickListener(new View.OnClickListener() {
				public void onClick(View v) {
					Flags.getFlags().pauseFrom = PAUSE_FROM.DIALOG;
					Flags.getFlags().isGamePaused = false;
					quiteDialog.dismiss();
					Flags.getFlags().currentLevel = Constants.BASE_LEVEL;
					Flags.getFlags().scenarioStatus = Constants.GAME_EVENTS.INIT_SCENE;
					Flags.getFlags().sensorScenario = Constants.INIT_SCENARIO;
					pauseGame(true);
					((Activity) mGameView.getContext()).finish();
				}
			});
			TextView text = (TextView) quiteDialog.findViewById(R.id.text);
			Typeface face = Typeface.createFromAsset(GameApplication
					.getAppContext().getAssets(), "fonts/Comic.ttf");
			text.setTypeface(face);
			text.setText(msg);
			quiteDialog.show();
			Flags.getFlags().isGamePaused = true;
		}
	}

	public void okEnablePopDialog(String msg, int position, boolean quit, boolean ok, DIALOG_FROM from) {// position:
		// Gravity.Bottom
		if (dialog == null || !dialog.isShowing()) {

			Music.stopSound(mGameView.getContext());
			
			dialog = new Dialog(mGameView.getContext(), R.style.DialogBox);
			dialog.setCancelable(false);
			dialog.setContentView(R.layout.close);
			TextView text = (TextView) dialog.findViewById(R.id.text);
			Typeface face = Typeface.createFromAsset(GameApplication
					.getAppContext().getAssets(), "fonts/Comic.ttf");
			text.setTypeface(face);
			text.setText(msg);
			
			Window window = dialog.getWindow();
			if(window != null){
				WindowManager.LayoutParams wlp = window.getAttributes();
	
				wlp.gravity = position;
				if (wlp.gravity == Gravity.TOP) {
					wlp.verticalMargin = 0.15f;
				}
				wlp.flags &= ~WindowManager.LayoutParams.FLAG_DIM_BEHIND;
				window.setAttributes(wlp);
			}
			
			final Button ok_btn = (Button) dialog.findViewById(R.id.ok_btn);
			if(!ok){
				ok_btn.setText("Cancel");
			}
			ok_btn.setOnClickListener(new View.OnClickListener() {
				public void onClick(View v) {
					if(Flags.getFlags().instructionShakeMode != false){
						Flags.getFlags().instructionShakeMode = false;
					}
					if(ok_btn.getText() == "Cancel"){
						Physics.resumePhysicalThread();
					}
					closeDialogBackLogic();
					Flags.getFlags().isGamePaused = false;
				}
			});
			Button quit_btn = (Button) dialog.findViewById(R.id.quit_btn);
			if(!quit){
				quit_btn.setVisibility(View.GONE);
			}
			
			quit_btn.setOnClickListener(new View.OnClickListener() {
				public void onClick(View v) {
					Flags.getFlags().pauseFrom = PAUSE_FROM.DIALOG;
					dialog.dismiss();
					Flags.getFlags().isGamePaused = false;
					pauseGame(true);
					if(Flags.getFlags().isStacked){
//						Flags.getFlags().currentLevel++;
						Flags.getFlags().sensorScenario = Constants.INIT_SCENARIO;
						saveGame();
					}
					((Activity) mGameView.getContext()).finish();
				}
			});
			dialog.show();
			if(from != DIALOG_FROM.EMERGENCY){
				Flags.getFlags().isGamePaused = true;
			}
		}
	}

	public void instructionOnePopDialog(int position, long closeTime) {// position:
		// Gravity.Bottom
		if (dialog == null || !dialog.isShowing()) {
			
			Music.stopSound(mGameView.getContext());
			dialog = new Dialog(mGameView.getContext(), R.style.DialogBox);
			dialog.setCancelable(false);
			dialog.setContentView(R.layout.instruction_1);
			
			Window window = dialog.getWindow();
			WindowManager.LayoutParams wlp = window.getAttributes();
			wlp.gravity = position;
			if (wlp.gravity == Gravity.TOP) {
				wlp.verticalMargin = 0.15f;
			}
			wlp.flags &= ~WindowManager.LayoutParams.FLAG_DIM_BEHIND;
			window.setAttributes(wlp);
			
			Button ok_btn = (Button) dialog.findViewById(R.id.ok_btn);
			ok_btn.setOnClickListener(new View.OnClickListener() {
				public void onClick(View v) {
					closeDialogBackLogic();
				}
			});
			dialog.show();
		}
	}

	public void instructionTwoPopDialog(int position, long closeTime) {// position:
		// Gravity.Bottom
		if (dialog == null || !dialog.isShowing()) {
			// Timer close Dialog
			// startTimer();
			// timer.schedule(task, closeTime);
			Music.stopSound(mGameView.getContext());
			dialog = new Dialog(mGameView.getContext(), R.style.DialogBox);
			dialog.setCancelable(false);
			
			Window window = dialog.getWindow();
			WindowManager.LayoutParams wlp = window.getAttributes();
			wlp.gravity = position;
			if (wlp.gravity == Gravity.TOP) {
				wlp.verticalMargin = 0.15f;
			}
			wlp.flags &= ~WindowManager.LayoutParams.FLAG_DIM_BEHIND;
			window.setAttributes(wlp);
			
			dialog.setContentView(R.layout.instruction_2);
			
			Button ok_btn = (Button) dialog.findViewById(R.id.ok_btn);
			ok_btn.setOnClickListener(new View.OnClickListener() {
				public void onClick(View v) {
					dialog.dismiss();
					Flags.getFlags().sensorStart = false;
					SensorListener.getInstance(null).start();
					Flags.getFlags().instructionTitleMode = false;
					if(Constants.SHAREDPREFERENCES.getBoolean("changeInstructionSet", true)){
						Editor edit = Constants.SHAREDPREFERENCES.edit();
						edit.putBoolean("changeInstructionSet", false);
						edit.commit();
						FinalPrefs.disableInstruction(mGameView.getContext());
					}
				}
			});
			dialog.show();
		}
	}

	private void showToast(String text) {
		Toast.makeText(mGameView.getContext(), text, Toast.LENGTH_SHORT).show();
	}

	private Vec2 generateMainPos(boolean random) {
		if (random) {
			float y = 0.125f + r.nextFloat() * 0.375f;
			float x = 0.25f + r.nextFloat() * 0.5f;
			return new Vec2(Flags.getFlags().screenW * x,
					Flags.getFlags().screenH * y);
		} else {
			return new Vec2(Flags.getFlags().screenW / 2,
					Flags.getFlags().screenH / 8);
		}
	}

	private int generateDirection(boolean random) {
		if (random) {
			return r.nextInt(2); // 0:vertical, 1:horizontal
		} else {
			return 1;
		}
	}

	private BoxObject generateNextLandscape(boolean randomVelocity,
			Float customSpeed) {
		// randomly generate landscape
		Random r = new Random();
		// randomly generate landscape size
		float size = r.nextFloat() * screenW * Constants.LANDSCAPE_SIZE_RANGE
				+ screenW * Constants.LANDSCAPE_MIN_SIZE;
		BoxObject landscape = (BoxObject) objPool.obtainBaseObject(
				ObjectType.LANDSCAPE, size, size);
		// randomly generate initial position (from below to top)
		float x = r.nextFloat() * screenW;
		float y = screenH + r.nextFloat() * screenH * 0.01f;
		landscape.setPosition(new Vec2(x, y));
		landscape.setColor(Color.LTGRAY);
		landscape.setBitmap(R.drawable.cloud);
		landscape.setAlpha(r.nextInt(155) + 100);
		// randomly generate velocity
		float speed;
		if (randomVelocity) {
			speed = -r.nextFloat() * 5 - 1;
		} else {
			if (customSpeed != null) {
				speed = customSpeed;
			} else {
				speed = 0f;
			}
		}
		speed = scaleSpeedOrForces(speed, false);
		if (landscape.mBody == null) {
			landscape.createPhysicsBody(Constants.BOX_DENSITY_DEFAULT,
					Constants.BOX_FRICTION_DEFAULT,
					Constants.BOX_RESTITUTION_DEFAULT, new Vec2(0, speed));
		} else {
			landscape.setVelocity(new Vec2(0, speed));
		}
		return landscape;
	}

	private void generateNextObstacle(boolean randomVelocity) {
		int[] obstacles = { R.drawable.evil, R.drawable.bomb,
				R.drawable.monster, R.drawable.obstacle, R.drawable.pumpkin,
				R.drawable.reddy };
		if (Flags.getFlags().num_obstacles > Constants.MAX_OBSTACLES) {
			return;
		}
		Flags.getFlags().num_obstacles++;
		// randomly generate landscape
		// randomly generate landscape size
		float size = r.nextFloat() * screenW * Constants.OBSTACLE_SIZE_RANGE
				+ screenW * Constants.OBSTACLE_SIZE_MIN;
		BoxObject obstacle = (BoxObject) objPool.obtainBaseObject(
				ObjectType.OBSTACLE, size, size);
		// randomly generate initial position (from below to top)
		float x = r.nextFloat() * screenW * 1.1f - screenW * 0.1f; // from -50
																	// to
																	// screenW +
																	// 50
		float y;
		if (x > 0 && x < screenW) { // if x is within screen, y should be out
			y = screenH + r.nextFloat() * screenH * 0.05f;
		} else { // if x is out of screen, y doesn't matter
			y = r.nextFloat() * (screenH * 0.1f) + screenH; //
		}
		obstacle.setPosition(new Vec2(x, y));
		obstacle.setColor(Color.GREEN);
		int i_obs = r.nextInt(obstacles.length);
		obstacle.setBitmap(obstacles[i_obs]);
		// randomly generate velocity
		float x_speed;
		float y_speed;
		if (randomVelocity) {
			x_speed = -r.nextFloat() * Constants.OBSTACLE_X_RANGE_SPEED
					+ Constants.OBSTACLE_X_MIN_SPEED;
			y_speed = -r.nextFloat() * Constants.OBSTACLE_Y_RANGE_SPEED
					+ Constants.OBSTACLE_Y_MIN_SPEED;
		} else {
			x_speed = 0f;
			y_speed = 0f;
		}
		Vec2 speed = scaleSpeedOrForces(new Vec2(x_speed, y_speed));

		if (obstacle.mBody == null) {
			obstacle.createPhysicsBody(0f, Constants.BOX_FRICTION_DEFAULT,
					Constants.BOX_RESTITUTION_DEFAULT, speed);
		} else {
			obstacle.setVelocity(speed);
		}

	}

	private void generateNextEmergency() {
		if(Flags.getFlags().isGamePaused == true){
			Flags.getFlags().emergencyTimer = 0;
			return;
		}
		
		if (Flags.getFlags().emergencyTimer == 0) {
			Flags.getFlags().emergencyTimer = System.currentTimeMillis();
			Flags.getFlags().emergencyInterval = (r.nextFloat()
					* Constants.EMERGENCY_RANGE_INTERVAL + Constants.EMERGENCY_MIN_INTERVAL) * 1000;
		}
		if (Flags.getFlags().isInEmergency) {
			// reset timer
			Flags.getFlags().emergencyTimer = System.currentTimeMillis();
			// reset shake counter
			Flags.getFlags().shakeCounter = 0;
			Flags.getFlags().previousShakeCounter = 0;

			// send message
			if (!Flags.getFlags().emergencyInQueue) {
				Flags.getFlags().emergencyInQueue = sendEmptyMessage(GAME_EVENTS.EMERGENCY
						.ordinal());
			}
			return;
		} else {
			// Calculate timing
			// Condition 1: 30s after the last emergency
			// Condition 2: keepShaking for 10s then start reduce
			Log.v(TAG,
					String.format("%d", Flags.getFlags().previousShakeCounter
							- Flags.getFlags().shakeCounter));
			Log.v(TAG,
					String.format("%d", Flags.getFlags().previousShakeCounter));
			if (Flags.getFlags().previousShakeCounter
					- Flags.getFlags().shakeCounter > Constants.EMERGENCY_REST_INTERVAL
					&& Flags.getFlags().previousShakeCounter > Constants.EMERGENCY_SHAKING_INTERVAL) { // trigger
				Flags.getFlags().isInEmergency = true;
				Flags.getFlags().currentEmergency.solved = false;
				Flags.getFlags().currentEmergency
						.updateEmergency(EMERGENCY_TYPE.values()[r.nextInt(3)]);
			} else if (System.currentTimeMillis()
					- Flags.getFlags().emergencyTimer > Flags.getFlags().emergencyInterval) {// trigger
				Flags.getFlags().isInEmergency = true;
				Flags.getFlags().currentEmergency.solved = false;
				Flags.getFlags().emergencyInterval = (r.nextFloat()
						* Constants.EMERGENCY_RANGE_INTERVAL + Constants.EMERGENCY_MIN_INTERVAL) * 1000;
				Flags.getFlags().currentEmergency
						.updateEmergency(EMERGENCY_TYPE.values()[r.nextInt(3)]);
			}
		}
	}

	private void makeMainBoxShaking() {
		Flags.getFlags().shakeSpeed = Constants.BOX_SHAKE_SPEED;
		Vec2 shake_speed = new Vec2(Flags.getFlags().shakeSpeed, 0);
		if (actor1.mBody.m_linearVelocity.x == 0) {
			shake_speed.x = -shake_speed.x;
			actor1.setVelocity(shake_speed);
			boxShakeTimer = System.currentTimeMillis();
		} else if (actor1.mBody.m_linearVelocity.x < 0
				&& System.currentTimeMillis() - boxShakeTimer >= 10) {
			actor1.setVelocity(shake_speed);
			boxShakeTimer = System.currentTimeMillis();
		} else if (actor1.mBody.m_linearVelocity.x > 0
				&& System.currentTimeMillis() - boxShakeTimer >= 10) {
			shake_speed.x = -shake_speed.x;
			actor1.setVelocity(shake_speed);
			boxShakeTimer = System.currentTimeMillis();
		}
	}

	private void preventParachuteOverTurn() {
		if (Math.abs(actor2.getRotation()) > Constants.MAX_TILT_ANGLE) {
			actor1.mBody.setAngularVelocity(0.0f);
			actor2.mBody.setAngularVelocity(0.0f);
		}
	}

	private void preventTiltingTooFast() {
		// prvent parachute moving too fast (left or right)
		float max_speed = scaleSpeedOrForces(Constants.MAX_TILT_SPEED, true);
		Vec2 box_force = scaleSpeedOrForces(new Vec2(Constants.BOX_FORCE_TILT,
				0));
		Vec2 parachute_force = scaleSpeedOrForces(new Vec2(
				Constants.PARACHUTE_FORCE_TILT, 0));
		if (direction_flag == Constants.TILT_LEFT) {
			if (actor2.mBody.getLinearVelocity().x > -max_speed
					|| actor2.getRotation() > -Constants.MAX_TILT_ANGLE) {

				box_force.x = -box_force.x;
				parachute_force.x = -parachute_force.x;

				actor1.applyForce(box_force,
						new Vec2(((BoxObject) actor1).getWidth() / 2,
								-((BoxObject) actor1).getHeight() / 2));
				actor2.applyForce(parachute_force, new Vec2(
						-((TriangleObject) actor2).getLength() / 2,
						-((TriangleObject) actor2).getDistanceToSide()));
			}
		} else if (direction_flag == Constants.TILT_RIGHT) {
			if (actor2.mBody.getLinearVelocity().x < max_speed
					|| actor2.getRotation() < Constants.MAX_TILT_ANGLE) {

				actor1.applyForce(box_force,
						new Vec2(-((BoxObject) actor1).getWidth() / 2,
								-((BoxObject) actor1).getHeight() / 2));
				actor2.applyForce(parachute_force, new Vec2(
						((TriangleObject) actor2).getLength() / 2,
						-((TriangleObject) actor2).getDistanceToSide()));
			}
		}
	}

	private void updateTowerStatus() {
		// after certain time, create tower
		if (System.currentTimeMillis() - Flags.getFlags().currentTiltTime >= Flags
				.getFlags().totalTiltTime * 1000
				&& Flags.getFlags().isTowerCreated == false) {
			popDialog("Tower is appearing...", Gravity.CENTER, 3000);
			// hide height bar
			Flags.getFlags().heightBar.visible = false;
			// update position of the stack tower
			createStackTower(true);
		}
		// gradually show tower
		else if (Flags.getFlags().isTowerCreated == true
				&& actor3.getPosition().y <= screenH) {
			Flags.getFlags().isTowerReady = true;
		} else if (Flags.getFlags().isTowerCreated == true) {
			// fix only horizontal but not vertical
			fixTowerAndGround(false);
		}
	}

	private void updateParachuteStatus() {
		if (actor2 != null) {
			// prevent parachute over turn
			preventParachuteOverTurn();

			// prevent tilting too fast
			preventTiltingTooFast();
		}
	}

	private void destroyObjects() {
		Iterator<BaseObject> it = GameView.actors.iterator();
		while (it.hasNext()) {
			BaseObject obj = it.next();
			if (obj.objectType == ObjectType.LANDSCAPE) {
				if (obj.getPosition().y < 0) {
					obj.destroySelf();
				}
			} else if (obj.objectType == ObjectType.OBSTACLE) {
				if (obj.inContact) {
					Flags.getFlags().num_obstacles--;
					obj.destroySelf();
				}
				if (obj.getPosition().y < 0) {
					Flags.getFlags().num_obstacles--;
					obj.destroySelf();
				}
			} else if (obj.objectType == ObjectType.PARACHUTE) {

			}
		}
	}

	@Override
	public void onClick(DialogInterface dialog, int which) {
		// switch (Flags.getFlags().sensorScenario) {
		// case Constants.SHAKING_SCENARIO:
		// SensorListener.getInstance(null).start();
		// break;
		// case Constants.TILTING_SCENARIO:
		// if (Flags.getFlags().scenarioStatus != GAME_EVENTS.SCENE_ENDING) {
		// SensorListener.getInstance(null).start();
		// } else { // end activity
		// Flags.getFlags().scenarioStatus = GAME_EVENTS.NEXT_SCENE;
		// }
		// break;
		// }
	}

	@Override
	public void beginContact(Contact contact) { // two object begin to collide
		Body bodyA = contact.getFixtureA().getBody();
		Body bodyB = contact.getFixtureB().getBody();
		if (bodyA.getUserData() == ObjectType.MAIN_BOX) {
			
			if (bodyB.getUserData() == ObjectType.BOX) { // stacked onto tower
				if(actor3 != null && actor3.inContact == true){
					return;
				}
				Physics.setGravity(scaleSpeedOrForces(Constants.GRAVITY_FORCE_DEFAULT));
				Flags.getFlags().useSmartCamera = false;
				Flags.getFlags().scenarioStatus = GAME_EVENTS.SCENE_ENDING;
				GameView.getObjectFromBody(bodyB).inContact = true;
				if (!Music.isPlayingSound()) {
					Music.stopSound(mGameView.getContext());
					Music.playSound(mGameView.getContext(), R.raw.box_down);
				}
			}

			if (bodyB.getUserData() == ObjectType.BASE) { // fail to stack onto
				// tower but on the
				// base
				Physics.setGravity(scaleSpeedOrForces(Constants.GRAVITY_FORCE_DEFAULT));
				Flags.getFlags().useSmartCamera = false;
				Flags.getFlags().scenarioStatus = GAME_EVENTS.SCENE_ENDING;
				if(actor3 != null){
					actor3.inContact = true;
				}
				if (!Music.isPlayingSound()) {
					Music.stopSound(mGameView.getContext());
					Music.playSound(mGameView.getContext(), R.raw.box_down);
				}
			}

			if (bodyB.getUserData() == ObjectType.OBSTACLE
					&& (Flags.getFlags().instructionTitleMode == false || !FinalPrefs.getInstruction(mGameView.getContext()))
					&& Flags.getFlags().currentScore >= 0) {
				BoxObject obstacle = (BoxObject) GameView.getObjectFromBody(bodyB);
				if (obstacle != null) {
					obstacle.inContact = true;
					sendEmptyMessage(Constants.GAME_EVENTS.BOX_COLLIDE.ordinal());
				}
			}
		}
	}

	@Override
	public void endContact(Contact contact) { // two object end collide
		Body bodyA = contact.getFixtureA().getBody();
		Vec2 box_speed = new Vec2(0, Flags.getFlags().boxDropSpeed);

		box_speed = scaleSpeedOrForces(box_speed);

		if (bodyA.getUserData() == ObjectType.MAIN_BOX
				|| bodyA.getUserData() == ObjectType.PARACHUTE) {
			actor1.setVelocity(box_speed);
			if (actor2 != null) {
				actor2.setVelocity(box_speed);
			}
		}
	}

	@Override
	public void postSolve(Contact arg0, ContactImpulse arg1) {

	}

	@Override
	public void preSolve(Contact arg0, Manifold arg1) {

	}

	// only used when set up speed, for read out, don't use it
	private Vec2 scaleSpeedOrForces(Vec2 input) {
		Vec2 output = input.clone();

		output.x = output.x * screenW / Constants.BASELINE_SCREEN_WIDTH;
		output.y = output.y * screenH / Constants.BASELINE_SCREEN_HEIGHT;

		return (output);
	}

	// only used when set up speed, for read out, don't use it
	private float scaleSpeedOrForces(float speed, boolean width) {
		if (width) {
			return (speed * screenW / Constants.BASELINE_SCREEN_WIDTH);
		} else {
			return (speed * screenH / Constants.BASELINE_SCREEN_HEIGHT);
		}
	}

	private void updateScreenOffset() {
		if (Flags.getFlags().useSmartCamera) {
			Vec2 pos = actor1.getNoOffsetPosition();
			Vec2 diff = new Vec2(-pos.x + Flags.getFlags().cameraCenter.x,
					-pos.y + Flags.getFlags().cameraCenter.y);
			Flags.getFlags().screenOffset = diff;
		}
	}

	private void fixTowerAndGround(boolean v) {
		float groundPosX = screenW / 2;
		float groundPosY = screenH;

		Vec2 currentPos = actor3.getPosition();
		currentPos.x = groundPosX;
		if (v) {
			currentPos.y = groundPosY;
		}
		actor3.setPosition(currentPos);

		// for tower, only fix y
		float towerPosY = screenH - Constants.BOX_SIZE * screenW / 2;

		if (v) {
			Vec2 boxPos;
			for (BoxObject box : tower) {
				boxPos = box.getPosition();
				boxPos.y = towerPosY;
				box.setPosition(boxPos);
				towerPosY -= Constants.BOX_SIZE * screenW * 0.8f;
			}
		}
	}

	public void caculateScore() {
		int oldScore = Integer.valueOf(Constants.SHAREDPREFERENCES.getString("HightestScore", "0"));
		int SingleLevel = Integer.valueOf(Constants.SHAREDPREFERENCES.getString("SingleLevel", "0"));
		Editor editor = Constants.SHAREDPREFERENCES.edit();
		if (gApplication.getScore() > oldScore) {
			editor.putString("HightestScore",
					String.valueOf(gApplication.getScore()));
		}
		if (Flags.getFlags().currentLevel > SingleLevel) {
			editor.putString("SingleLevel",
					String.valueOf(Flags.getFlags().currentLevel - 1));
		}
		editor.commit();
	}

	public void saveGame() {
		Editor editor = Constants.SHAREDPREFERENCES.edit();
		editor.putInt("CURRENT_LEVEL", Flags.getFlags().currentLevel);
		editor.putInt("CURRENT_SCENE", Flags.getFlags().sensorScenario);
		editor.commit();
	}

	public void loadGame() {
		SharedPreferences pref = Constants.SHAREDPREFERENCES;
		Flags.getFlags().currentLevel = pref.getInt("CURRENT_LEVEL",
				Constants.BASE_LEVEL);
		Flags.getFlags().sensorScenario = pref.getInt("CURRENT_SCENE",
				Constants.INIT_SCENARIO);
	}

	public void pauseGame(boolean stop) {
		if(stop){
			Physics.stopPhysicalThread();
		}else{
			// pause physical engine
//			Physics.stopPhysicalThread();
			Physics.pausePhysicalThread();
		}
		// stop sensing
		if (SensorListener.getInstance(null).isRunning()) {
			SensorListener.getInstance(null).stop();
		}
		
		Flags.getFlags().emergencyTimer = 0;
		
		// save game status
		saveGame();
	}

	public void resumeGame(boolean load) {
		if (load) {
			loadGame();
			Flags.getFlags().scenarioStatus = Constants.GAME_EVENTS.INIT_SCENE;
		}
		Physics.startPhysicalThread();
//		mGameView.getRenderThread().start();
		if (Flags.getFlags().sensorScenario == Constants.SHAKING_SCENARIO || Flags.getFlags().sensorScenario == Constants.TILTING_SCENARIO
				&& !SensorListener.getInstance(null).isRunning()) {
			SensorListener.getInstance(null).start();
		}
		if(Flags.getFlags().isInEmergency){
			SensorListener.getInstance(null).stop();
		}
	}

	public void startTimer() {
		timer = new Timer();
		task = new TimerTask() {
			public void run() {
				closeDialogBackLogic();

				timer.cancel();
			}
		};
	}

	public void closeDialogBackLogic() {
		dialog.dismiss();
		switch (Flags.getFlags().sensorScenario) {
		case Constants.SHAKING_SCENARIO:
			if (!Flags.getFlags().isInEmergency) {
				SensorListener.getInstance(null).start();
			} else if (Flags.getFlags().currentEmergency.solved) {
				SensorListener.getInstance(null).start();
				Flags.getFlags().isInEmergency = false;
				Flags.getFlags().emergencyInQueue = false;
			}
			break;
		case Constants.TILTING_SCENARIO:
			if (Flags.getFlags().currentScore <= 0) {
				Flags.getFlags().currentLevel = Constants.BASE_LEVEL;
				Flags.getFlags().sensorScenario = Constants.INIT_SCENARIO;
			}
			if (Flags.getFlags().scenarioStatus != GAME_EVENTS.SCENE_ENDING) {
				SensorListener.getInstance(null).start();
			} else { // end activity
				Flags.getFlags().scenarioStatus = GAME_EVENTS.NEXT_SCENE;
			}
			break;
		}
	}
}
