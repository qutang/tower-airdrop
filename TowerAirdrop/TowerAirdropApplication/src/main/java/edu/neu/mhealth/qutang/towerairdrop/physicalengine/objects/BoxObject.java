package edu.neu.mhealth.qutang.towerairdrop.physicalengine.objects;

import org.jbox2d.common.Vec2;
import org.jbox2d.dynamics.Body;
import org.jbox2d.dynamics.joints.JointDef;
import org.jbox2d.dynamics.joints.RevoluteJointDef;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;

import edu.neu.mhealth.qutang.towerairdrop.GameView;

public class BoxObject extends BaseObject {

  private float width;
  private float height;
  private float factor = 1f;

  public BoxObject(ObjectType objType, float _width, float _height) {
    super(objType); // Just assigns an ID
    // 4 points, 3 coords, 12 elements, 9000 problems
    mVertices = new float[12];

    this.width = _width;
    this.height = _height;

    refreshVertices();
  }
  
  public BoxObject(ObjectType objType, float _width, float _height, float _factor) {
	    super(objType); // Just assigns an ID
	    // 4 points, 3 coords, 12 elements, 9000 problems
	    mVertices = new float[12];

	    this.width = _width;
	    this.height = _height;
	    this.factor = _factor;
	    refreshVertices();
	  }

  @Override
public void onBodyCreation(Body _body) {
	super.onBodyCreation(_body);
}

private void refreshVertices() {

    // Modify our own vertex array, and pass it to setVertices
    // We'll define our box centered around the origin
    // The z cord could potentially be used to specify a layer to render on. Food for thought.
    mVertices[0] = width * -0.5f;
    mVertices[1] = height * factor * -0.5f;
    mVertices[2] = 1.0f;

    mVertices[3] = width * -0.5f;
    mVertices[4] = height * factor * 0.5f;
    mVertices[5] = 1.0f;

    mVertices[6] = width * 0.5f;
    mVertices[7] = height * factor * -0.5f;
    mVertices[8] = 1.0f;

    mVertices[9] = width * 0.5f;
    mVertices[10] = height * factor * 0.5f;
    mVertices[11] = 1.0f;

    // Update!
    setVertices(mVertices);
  }

  // Rebuild our vertices on modification
  public void setSize(float _width, float _height) {  
    this.width = _width;
    this.height = _height;
    refreshVertices();
  }

  public float getWidth() { return width; }
  public float getHeight() { return height; }
  
  public boolean addToJoint(JointDef jointDef, Vec2 localScreenPoint){
	  switch(jointDef.type){
	  case REVOLUTE:
		  if(jointDef.bodyA != null){
			  if(jointDef.bodyB != null){
				  return false;
			  }else{
				  jointDef.bodyB = mBody;
				  ((RevoluteJointDef) jointDef).localAnchorB.set(GameView.screenToWorld(localScreenPoint));
				  jointDef.userData = 1;
			  }
		  }else{
			  jointDef.bodyA = mBody;
			  ((RevoluteJointDef) jointDef).localAnchorA.set(GameView.screenToWorld(localScreenPoint));
		  }
		  break;
	  default:
		  break;
	  }
	  return true;
  }
  
  @Override
  public void draw(Canvas canvas){
	  super.draw(canvas);
	  Paint boxPaint = new Paint();
	  boxPaint.setAlpha(mAlpha);
	  canvas.save();
	  Vec2 pos;
	  float rotation = getRotation();
	  pos = getPosition();
	  canvas.rotate(rotation, pos.x, pos.y);
	  if(mBitmap == null && mColor != null){
		  boxPaint.setColor(mColor);
		  canvas.drawRect(pos.x - width/2, pos.y - height/2, pos.x + width/2, pos.y + height/2, boxPaint);
	  }else if(mBitmap != null){
		  RectF r = new RectF(pos.x - width/2, pos.y - height/2, pos.x + width/2, pos.y + height/2);
		  canvas.drawBitmap(mBitmap, null, r, boxPaint);
	  }
	  canvas.restore();
  }
  
  @Override
  public boolean isTouched(Vec2 touchPos){
	  Vec2 center;
	  center = getPosition();
	  if(Math.abs(center.x - touchPos.x) <= width && Math.abs(center.y - touchPos.y) <= height){
		  return true;
	  }else{
		  return false;
	  }
  }
}
