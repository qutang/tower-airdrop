package edu.neu.madcourse.qutang.towerairdrop;

import java.io.ObjectStreamField;

import org.jbox2d.common.Vec2;
import org.jbox2d.dynamics.Body;
import org.jbox2d.dynamics.joints.JointDef;
import org.jbox2d.dynamics.joints.RevoluteJointDef;

import android.graphics.BitmapShader;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.graphics.Path.FillType;
import android.graphics.Path;
import android.graphics.RectF;
import android.graphics.Shader.TileMode;
import android.util.Log;

public class TriangleObject extends BaseObject {

  private static final String TAG = "TriangleObject";
private float length;

  public TriangleObject(ObjectType objType, float _length) {
    super(objType); // Just assigns an ID
    // 3 points, 3 coords, 9 elements, 9000 problems
    mVertices = new float[9];
    this.length = _length;
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
	// the equal side triangle is point downward
    mVertices[0] = length * -0.5f; //ul x
    mVertices[1] = -getDistanceToSide(); //ul y
    mVertices[2] = 1.0f;

    mVertices[3] = length * 0.5f;
    mVertices[4] = -getDistanceToSide();
    mVertices[5] = 1.0f;

    mVertices[6] = 0f;
    mVertices[7] = getDistanceToVert();
    mVertices[8] = 1.0f;


    // Update!
    setVertices(mVertices);
  }

  // Rebuild our vertices on modification
  public void setLength(float _length) {
    this.length = _length;
    refreshVertices();
  }

  public float getLength() { return length; }
  
  public float getDistanceToVert() {
	  return (float) (length / 2 / (Math.cos(Math.PI / 6f)));
  }
  
  public float getDistanceToSide() {
	  return (float) (Math.tan(Math.PI / 6f) * length * 0.5f);
  }
  
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
	  Paint trianglePaint = new Paint();
	  trianglePaint.setStyle(Style.FILL);
	  Vec2 pos;
	  float rotation = getRotation();
	  pos = getPosition();
	  canvas.save();
	  canvas.rotate(rotation, pos.x, pos.y);

	  Path triPath = new Path();
	  triPath.setFillType(FillType.EVEN_ODD);
	  triPath.moveTo(pos.x + mVertices[0], pos.y + mVertices[1]);
	  triPath.lineTo(pos.x + mVertices[3], pos.y + mVertices[4]);
	  triPath.lineTo(pos.x + mVertices[6], pos.y + mVertices[7]);
	  triPath.lineTo(pos.x + mVertices[0], pos.y + mVertices[1]);
	  triPath.close();
	  RectF bounds = new RectF();
	  triPath.computeBounds(bounds, false);
	  if(mBitmap == null && mColor != null){
		  trianglePaint.setColor(Color.GRAY);
		  canvas.drawPath(triPath, trianglePaint);
	  }else if(mBitmap != null){
		  canvas.drawBitmap(mBitmap, null, bounds, trianglePaint);
	  }
	  canvas.restore();
  }
}
