package edu.neu.madcourse.qutang.towerairdrop;

import java.util.Vector;

import org.jbox2d.collision.shapes.PolygonShape;
import org.jbox2d.common.Vec2;
import org.jbox2d.dynamics.Body;
import org.jbox2d.dynamics.BodyDef;
import org.jbox2d.dynamics.BodyType;
import org.jbox2d.dynamics.FixtureDef;
import org.jbox2d.dynamics.World;
import org.jbox2d.dynamics.joints.RevoluteJoint;
import org.jbox2d.dynamics.joints.RevoluteJointDef;

import android.util.Log;

/**
 * @author Qu
 * 
 */
public class Physics {

	// Defined public, should we need to modify them from elsewhere
	public static int velIterations = 6;
	public static int posIterations = 10;

	// This is private, since we need to set it in the physics world, so
	// directly
	// modifying it from outside the class would bypass that. Why not set it
	// in the world directly? The world is in another thread :) It might also
	// stop and start back up again, so we need to have it saved.
	private static Vec2 gravity = new Vec2(0, 1);

	// Threads!
	private static PhysicsThread pThread = null;
	private static BaseEventHandler mBaseEventHandler = null;

	// Our queues. Wonderful? I concur.
	private static Vector<BodyQueue> bodyDestroyQ = new Vector<BodyQueue>();
	private static Vector<BodyQueueDef> bodyCreateQ = new Vector<BodyQueueDef>();

	// We need to keep track of how many bodies exist, so we can stop the thread
	// when none are present, and start it up again when necessary
	private static int bodyCount = 0;

	public static void addCreationQueue(BodyQueueDef bq) {
		// Ship it to our queue
		bodyCreateQ.add(bq);
		bodyCount++;
	}

	public static void startPhysicalThread() {
		if (bodyCount > 0) {
			if (pThread != null) {
				resumePhysicalThread();
			} else {
				pThread = new PhysicsThread();
				pThread.start();
			}
		}
	}

	public static void addDestroyQueue(BodyQueue bodyq) {
		bodyDestroyQ.add(bodyq);
	}

	public static void setGravity(Vec2 grav) {
		if (pThread != null) {
			pThread.setGravity(grav);
		}
		gravity = grav;
	}

	public static Vec2 getGravity() {
		return gravity;
	}

	public static void stopPhysicalThread() {
		bodyCount = 0;
		if(pThread != null){
			pThread.stop = true;
		}
	}
	
	public static void pausePhysicalThread() {
		if(pThread != null){
			pThread.pause = true;
		}
	}
	
	public static void resumePhysicalThread() {
		if(pThread != null){
			pThread.pause = false;
		}
	}

	public static void createParachuteJoint(BoxObject mainBox,
			TriangleObject parachute) {
		pThread.createParachuteJoint(mainBox, parachute);
	}

	public static void destroyParachuteJoint() {
		pThread.destroyParachuteJoint();
	}
	
	public static Body destroyPhysicalBody(Body mBody){
		return pThread.destroyPhysicalBody(mBody);
	}

	public static void pullEventHandler(BaseEventHandler _handler) {
		mBaseEventHandler = _handler;
	}

	public static void onWorldCreated() {
		
	}

	/* This function is for testing purpose */
	public static void testBox2D() {
		// Static Body
		Vec2 gravity = new Vec2(0, -10);
		boolean doSleep = true;
		World world = new World(gravity);
		world.setAllowSleep(doSleep);

		BodyDef groundBodyDef = new BodyDef();
		groundBodyDef.position.set(0, -10);
		Body groundBody = world.createBody(groundBodyDef);
		PolygonShape groundBox = new PolygonShape();
		groundBox.setAsBox(50, 10);
		groundBody.createFixture(groundBox, 0);

		// Dynamic Body
		BodyDef bodyDef = new BodyDef();
		bodyDef.type = BodyType.DYNAMIC;
		bodyDef.position.set(0, 4);
		Body body = world.createBody(bodyDef);
		PolygonShape dynamicBox = new PolygonShape();
		dynamicBox.setAsBox(1, 1);
		FixtureDef fixtureDef = new FixtureDef();
		fixtureDef.shape = dynamicBox;
		fixtureDef.density = 1;
		fixtureDef.friction = 0.3f;
		body.createFixture(fixtureDef);

		// Setup world
		float timeStep = 1.0f / 60.0f;
		int velocityIterations = 6;
		int positionIterations = 2;

		// Run loop
		for (int i = 0; i < 60; ++i) {
			world.step(timeStep, velocityIterations, positionIterations);
			Vec2 position = body.getPosition();
			float angle = body.getAngle();
			System.out.printf("Dynamic body: %4.2f %4.2f %4.2f\n", position.x,
					position.y, angle);
			position = groundBody.getPosition();
			angle = groundBody.getAngle();
			System.out.printf("Static body: %4.2f %4.2f %4.2f\n", position.x,
					position.y, angle);
		}
	}

	// Thread definition, this is where the physics magic happens
	private static class PhysicsThread extends Thread {

		public final String TAG = "PhysicsThread";
		// Setting this to true exits the internal update loop, and ends the
		// thread
		private boolean stop = false;
		private boolean pause = false;
		private int avoidSuspended = 0;

		// We need to know if the thread is still running or not, just in case
		// we try to create it
		// after telling it to stop, but before it can finish.
		private boolean running = false;

		// The world itself
		private World physicsWorld = null;
		private RevoluteJoint mainJoint = null; // Joint for box and parachute

		public boolean isRunning() {
			return running;
		}
		
		public boolean isPaused() {
			return pause;
		}

		public void setGravity(Vec2 grav) {
			if (physicsWorld != null) {
				physicsWorld.setGravity(grav);
			}
		}

		public Vec2 getGravity() {
			if (physicsWorld != null) {
				return physicsWorld.getGravity();
			} else {
				return null;
			}
		}
		
		public void update(){
			// pause first
			pausePhysicalThread();
			switch(Flags.getFlags().scenarioStatus){
			case SCENE_CREATED:
				mBaseEventHandler.sendEmptyMessage(Constants.GAME_EVENTS.SCENE_CREATED.ordinal());
				break;
			case ACTION_READY:
				mBaseEventHandler.sendEmptyMessage(Constants.GAME_EVENTS.ACTION_READY.ordinal());
				break;
			case GAME_OVER:
				mBaseEventHandler.sendEmptyMessage(Constants.GAME_EVENTS.GAME_OVER.ordinal());
				break;
			case NEXT_SCENE:
				mBaseEventHandler.sendEmptyMessage(Constants.GAME_EVENTS.NEXT_SCENE.ordinal());
				break;
			case GAME_RUNNING:
				mBaseEventHandler.sendEmptyMessage(Constants.GAME_EVENTS.GAME_RUNNING.ordinal());
				break;
			case SCENE_ENDING:
				mBaseEventHandler.sendEmptyMessage(Constants.GAME_EVENTS.SCENE_ENDING.ordinal());
				break;
			default:
				resumePhysicalThread();
				break;
			}
		}
		
		public void createParachuteJoint(BoxObject mainBox,
				TriangleObject parachute) {
			if (mainJoint != null)
				return;
			RevoluteJointDef mainJointDef = new RevoluteJointDef();
			mainBox.addToJoint(mainJointDef, new Vec2(0f,
					-mainBox.getHeight() / 3));
			parachute.addToJoint(mainJointDef,
					new Vec2(0f, parachute.getDistanceToVert()));
			mainJointDef.collideConnected = true;
			mainJointDef.referenceAngle = 0;
			mainJointDef.enableLimit = true;
			mainJointDef.lowerAngle = -15 * 0.0174532925f;
			mainJointDef.upperAngle = 15 * 0.0174532925f;
			mainJoint = (RevoluteJoint) physicsWorld.createJoint(mainJointDef);
		}

		public void destroyParachuteJoint() {
			if (mainJoint != null) {
				physicsWorld.destroyJoint(mainJoint);
				mainJoint = null;
			}
		}
		
		public Body destroyPhysicalBody(Body mBody){
			if(mBody != null){
				physicsWorld.destroyBody(mBody);
				mBody = null;
			}
			return null;
		}

		@Override
		public void run() {
			Log.i(TAG, "Physics Thread start");
			running = true;

			// Create world with saved gravity
			physicsWorld = new World(gravity);
			physicsWorld.setContactListener(mBaseEventHandler);
			physicsWorld.setAllowSleep(true);

			// Step!
			while (!stop) {

				// Record the start time, so we know how long it took to sim
				// everything
				long startTime = System.currentTimeMillis();

//				if (bodyDestroyQ.size() > 0) {
//					synchronized (bodyDestroyQ) {
//
//						for (BodyQueue bodyq : bodyDestroyQ) {
//							physicsWorld.destroyBody(bodyq.getBody());
//							bodyCount--;
//							BaseObject obj = GameView.actors.get(bodyq.getActorID());
//							obj.onBodyDestroy();
//						}
//						bodyDestroyQ.clear();
//					}
//				}

				if (bodyCreateQ.size() > 0) {
					synchronized (bodyCreateQ) {
						// Handle creations
						for (BodyQueueDef bq : bodyCreateQ) {
							BaseObject obj;
							try{
								obj = GameView.actors.get(bq
										.getActorID());
							}catch(ArrayIndexOutOfBoundsException e){
								continue;
							}
							obj.onBodyCreation(physicsWorld.createBody(bq
									.getBd()));
						}
						bodyCreateQ.clear();
					}
				}

				// update physical status according to scene status
				update();
				
				while(pause || Flags.getFlags().isGamePaused){
					if(avoidSuspended == 0){
						Log.i(TAG, "Physics Thread is paused");
					}
					avoidSuspended++;
					if(avoidSuspended == 1000){
						avoidSuspended = 1;
					}
				}

				// Perform step, calculate elapsed time and divide by 1000 to
				// get it
				// in seconds
				physicsWorld.step(0.016666666f, velIterations, posIterations);

				if (bodyCount == 0) {
					stop = true;
				}

				long simTime = System.currentTimeMillis() - startTime;

				if (simTime < 16) {
					try {
						Thread.sleep(16 - simTime);
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			}
			Log.i(TAG, "Physics Thread stop");
			running = false;
			pThread = null;
			
			// init the next scene
			if(Flags.getFlags().scenarioStatus == Constants.GAME_EVENTS.NEXT_SCENE){
				Flags.getFlags().scenarioStatus = Constants.GAME_EVENTS.INIT_SCENE;
				mBaseEventHandler.sendEmptyMessage(Constants.GAME_EVENTS.INIT_SCENE.ordinal());
			}
		}
	}
}