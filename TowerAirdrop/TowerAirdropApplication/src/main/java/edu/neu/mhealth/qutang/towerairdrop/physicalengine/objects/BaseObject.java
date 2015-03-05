package edu.neu.mhealth.qutang.towerairdrop.physicalengine.objects;

import org.jbox2d.collision.shapes.PolygonShape;
import org.jbox2d.common.Vec2;
import org.jbox2d.dynamics.Body;
import org.jbox2d.dynamics.BodyDef;
import org.jbox2d.dynamics.BodyType;
import org.jbox2d.dynamics.FixtureDef;
import org.jbox2d.dynamics.joints.JointDef;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;

import edu.neu.mhealth.qutang.towerairdrop.physicalengine.BaseEventHandler;
import edu.neu.mhealth.qutang.towerairdrop.physicalengine.BodyQueueDef;
import edu.neu.mhealth.qutang.towerairdrop.Constants;
import edu.neu.mhealth.qutang.towerairdrop.Flags;
import edu.neu.mhealth.qutang.towerairdrop.GameApplication;
import edu.neu.mhealth.qutang.towerairdrop.GameView;
import edu.neu.mhealth.qutang.towerairdrop.physicalengine.Physics;

public abstract class BaseObject {

	protected Integer mColor = null;
	protected Bitmap mBitmap = null;
	protected int mAlpha = 255;
	public boolean visible = true;
	public boolean inContact = false;
	public boolean fixOnScreen = false;

	public enum ObjectType {
		MAIN_BOX, BOX, OBSTACLE, LANDSCAPE, PARACHUTE, BASE
	}

	private int id;
	private Vec2 initPos = new Vec2(0f, 0f);
	private float initRotation = 0f;
	protected ObjectType objectType;
	protected Body mBody = null;

	protected float[] mVertices; // in graphic coords
	protected float mDensity;
	protected float mFriction;
	protected float mRestitution;

	abstract boolean addToJoint(JointDef jointDef, Vec2 localScreenPoint);

	public BaseObject(ObjectType objType) {

		this.id = GameView.getNextId();
		GameView.actors.add(this);
		objectType = objType;
	}

    public Body getPhysicalBody(){
        return mBody;
    }

    public ObjectType getObjectType(){
        return objectType;
    }

	public void draw(Canvas canvas) {

		if (!visible) {
			return;
		}

		if (mBitmap == null && mColor == null) {
			return;
		}
	}

	public void setVertices(float[] _vertices) {

		this.mVertices = _vertices;
		if (mBody != null) {
			PolygonShape shape = new PolygonShape();
			Vec2[] verts = new Vec2[mVertices.length / 3];

			int vertIndex = 0;
			for (int i = 0; i < mVertices.length; i += 3) {
				verts[vertIndex] = GameView.screenToWorld(new Vec2(
						mVertices[i], mVertices[i + 1]));
				vertIndex++;
			}

			shape.set(verts, verts.length);
			// Attach fixture
			FixtureDef fd = new FixtureDef();
			fd.shape = shape;
			fd.density = mDensity;
			fd.friction = mFriction;
			fd.restitution = mRestitution;

			// apply collision rules
			switch (objectType) {
			case LANDSCAPE:
				fd.filter.categoryBits = Constants.CATEGORY_LANDSCAPE;
				fd.filter.maskBits = Constants.COLLIDE_MASK_LANSCAPE;
				break;
			case BOX:
				fd.filter.categoryBits = Constants.CATEGORY_BOX;
				fd.filter.maskBits = Constants.COLLIDE_MASK_BOX;
				break;
			case MAIN_BOX:
				fd.filter.categoryBits = Constants.CATEGORY_MAIN_BOX;
				fd.filter.maskBits = Constants.COLLIDE_MASK_MAIN_BOX;
				break;
			case OBSTACLE:
				fd.filter.categoryBits = Constants.CATEGORY_OBSTACLE;
				fd.filter.maskBits = Constants.COLLIDE_MASK_OBSTACLE;
				break;
			case PARACHUTE:
				fd.filter.categoryBits = Constants.CATEGORY_PARACHUTE;
				fd.filter.maskBits = Constants.COLLIDE_MASK_PARACHUTE;
				break;
			case BASE:
				fd.filter.categoryBits = Constants.CATEGORY_BASE;
				fd.filter.maskBits = Constants.COLLIDE_MASK_BASE;
				break;
			}
			mBody.createFixture(fd);
		}
	}

	public void createPhysicsBody(float _density, float _friction,
			float _restitution, Vec2 _velocity) {

		if (mBody != null) {
			return;
		}

		// Create the body
		BodyDef bd = new BodyDef();

		mDensity = _density;
		mFriction = _friction;
		mRestitution = _restitution;

		if (mDensity > 0) {
			bd.type = BodyType.DYNAMIC;
		} else {
			if (objectType != ObjectType.BASE && objectType != ObjectType.BOX) {
				bd.type = BodyType.KINEMATIC;
			} else {
				bd.type = BodyType.STATIC;
			}
		}

		bd.position = GameView.screenToWorld(initPos);
		bd.angle = initRotation * 0.0174532925f;
		if (_velocity != null) {
			bd.linearVelocity = _velocity;
		}
		// Add to physics world body creation queue, will be finalized when
		// possible
		Physics.addCreationQueue(new BodyQueueDef(id, bd));
	}

	// private void destroyPhysicsBody() {
	//
	// if (mBody == null) {
	// return;
	// }
	//
	// mBody = Physics.destroyPhysicalBody(mBody);
	//
	// }

	public void destroySelf() {
		// physically
		for (int i = getId() + 1; i < GameView.actors.size(); i++) {
			GameView.actors.get(i).rollBackId();
		}
		GameView.rollBackNextId();
		BaseEventHandler.objPool.recycleBaseObject(this);
		GameView.actors.remove(getId());
	}

	public void onBodyDestroy() {
		GameView.actors.remove(getId());
	}

	public void onBodyCreation(Body _body) {

		// Threads ftw
		mBody = _body;
		mBody.setUserData(objectType);

		// Body has been created, make fixture and finalize it
		// Physics world waits for completion before continuing

		// Create fixture from vertices
		PolygonShape shape = new PolygonShape();
		Vec2[] verts = new Vec2[mVertices.length / 3];

		int vertIndex = 0;
		for (int i = 0; i < mVertices.length; i += 3) {
			verts[vertIndex] = GameView.screenToWorld(new Vec2(mVertices[i],
					mVertices[i + 1]));
			vertIndex++;
		}

		shape.set(verts, verts.length);

		// Attach fixture
		FixtureDef fd = new FixtureDef();
		fd.shape = shape;
		fd.density = mDensity;
		fd.friction = mFriction;
		fd.restitution = mRestitution;

		// apply collision rules
		switch (objectType) {
		case LANDSCAPE:
			fd.filter.categoryBits = Constants.CATEGORY_LANDSCAPE;
			fd.filter.maskBits = Constants.COLLIDE_MASK_LANSCAPE;
			break;
		case BOX:
			fd.filter.categoryBits = Constants.CATEGORY_BOX;
			fd.filter.maskBits = Constants.COLLIDE_MASK_BOX;
			break;
		case MAIN_BOX:
			fd.filter.categoryBits = Constants.CATEGORY_MAIN_BOX;
			fd.filter.maskBits = Constants.COLLIDE_MASK_MAIN_BOX;
			break;
		case OBSTACLE:
			fd.filter.categoryBits = Constants.CATEGORY_OBSTACLE;
			fd.filter.maskBits = Constants.COLLIDE_MASK_OBSTACLE;
			break;
		case PARACHUTE:
			fd.filter.categoryBits = Constants.CATEGORY_PARACHUTE;
			fd.filter.maskBits = Constants.COLLIDE_MASK_PARACHUTE;
			break;
		case BASE:
			fd.filter.categoryBits = Constants.CATEGORY_BASE;
			fd.filter.maskBits = Constants.COLLIDE_MASK_BASE;
			break;
		}

		mBody.createFixture(fd);
	}

	// Modify the actor or the body, don't call after initialization
	public void setPosition(Vec2 position) {
		position.x -= (Flags.getFlags().screenOffset.x);
		position.y -= (Flags.getFlags().screenOffset.y);
		if (mBody == null) {
			this.initPos = position;
		} else {
			this.mBody.setTransform(GameView.screenToWorld(position), 0f);
		}
	}

	public void setColor(int c) {
		mColor = c;
	}

	// Modify the actor or the body
	public void setRotation(float rotation) {
		if (mBody == null) {
			this.initRotation = rotation;
		}
	}

	public void setBitmap(int resource) {
		mBitmap = BitmapFactory.decodeResource(GameApplication.getAppContext()
				.getResources(), resource);
	}

	public void setAlpha(int i) {
		mAlpha = i;
	}

	// Get from the physics body if avaliable
	public Vec2 getPosition() {
		Vec2 pos = new Vec2();
		if (mBody == null) {
			pos.x = initPos.x + Flags.getFlags().screenOffset.x;
			pos.y = initPos.y + Flags.getFlags().screenOffset.y;
		} else {
			Vec2 noOffsetPos = GameView.worldToScreen(mBody.getPosition());
			pos.x = noOffsetPos.x + Flags.getFlags().screenOffset.x;
			pos.y = noOffsetPos.y + Flags.getFlags().screenOffset.y;
		}

//		pos.x += Flags.getFlags().cameraCenter.x;
//		pos.y += Flags.getFlags().cameraCenter.y;

		return pos;
	}

	public Vec2 getNoOffsetPosition() {
		Vec2 pos = new Vec2();
		if (mBody == null) {
			pos.x = initPos.x;
			pos.y = initPos.y;
		} else {
			Vec2 noOffsetPos = GameView.worldToScreen(mBody.getPosition());
			pos.x = noOffsetPos.x;
			pos.y = noOffsetPos.y;
		}
		return pos;
	}

	public float getRotation() {
		if (mBody == null) {
			return initRotation;
		} else {
			return mBody.getAngle() * 57.2957795786f;
		}
	}

	public int getId() {
		return id;
	}

	public void setId() {
		this.id = GameView.getNextId();
	}

	public void rollBackId() {
		id--;
	}

	public void applyForce(Vec2 force, Vec2 localScreenPoint) {
		if (mBody != null) {
			mBody.applyForce(force, mBody.getWorldPoint(GameView
					.screenToWorld(localScreenPoint)));
		}
	}

	public void applyImpulse(Vec2 impulse, Vec2 localScreenPoint) {
		if (mBody != null) {
			mBody.applyLinearImpulse(impulse, mBody.getWorldPoint(GameView
					.screenToWorld(localScreenPoint)));
		}
	}

	public void setVelocity(Vec2 v) {
		if (mBody != null) {
			mBody.setLinearVelocity(v);
		}
	}

	public boolean isTouched(Vec2 touchPos) {
		return false;
	}
}
