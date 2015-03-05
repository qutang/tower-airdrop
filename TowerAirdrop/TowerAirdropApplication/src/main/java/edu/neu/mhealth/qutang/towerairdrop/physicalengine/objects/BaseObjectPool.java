package edu.neu.mhealth.qutang.towerairdrop.physicalengine.objects;

import java.util.Stack;

import org.jbox2d.dynamics.Body;

import edu.neu.mhealth.qutang.towerairdrop.physicalengine.objects.BaseObject.ObjectType;
import edu.neu.mhealth.qutang.towerairdrop.GameView;
import edu.neu.mhealth.qutang.towerairdrop.physicalengine.Physics;

public class BaseObjectPool {
	
	private final Stack<BoxObject> mAvailableObstacles;
	private final Stack<Body> mAvailableObstacleBodies;
	private final Stack<BoxObject> mAvailableLandscapes;
	private final Stack<Body> mAvailableLandscapeBodies;
	
	public BaseObjectPool() {
		this.mAvailableObstacles = new Stack<BoxObject>();
		this.mAvailableLandscapes = new Stack<BoxObject>();
		this.mAvailableObstacleBodies = new Stack<Body>();
		this.mAvailableLandscapeBodies = new Stack<Body>();
	}
	
	public BaseObject obtainBaseObject(ObjectType objType, float width, float height) {
		switch(objType){
		case OBSTACLE:
			if (this.mAvailableObstacles.size() > 0) {
				final BoxObject obstacle = this.mAvailableObstacles.pop();
//				obstacle.mBody = this.mAvailableObstacleBodies.pop();
				obstacle.inContact = false;
				obstacle.setId();
				obstacle.setSize(width, height);
				GameView.actors.add(obstacle);
				return obstacle;
			}
			else {
				return new BoxObject(ObjectType.OBSTACLE, width, height);
			}
		case LANDSCAPE:
			if (this.mAvailableLandscapes.size() > 0) {
				final BoxObject landscape = this.mAvailableLandscapes.pop();
//				landscape.mBody = this.mAvailableLandscapeBodies.pop();
				landscape.setId();
				landscape.setSize(width, height);
				GameView.actors.add(landscape);
				return landscape;
			}
			else {
				return new BoxObject(ObjectType.LANDSCAPE, width, height);
			}
		default:
			return null;
		}
	}
	
	public void recycleBaseObject(final BaseObject obj) {
		switch(obj.objectType){
		case OBSTACLE:
			this.mAvailableObstacles.push((BoxObject) obj);
			Physics.destroyPhysicalBody(obj.mBody);
			this.mAvailableObstacleBodies.push(obj.mBody);
			
			obj.mBody = null;
			break;
		case LANDSCAPE:
			this.mAvailableLandscapes.push((BoxObject) obj);
			Physics.destroyPhysicalBody(obj.mBody);
			
			this.mAvailableLandscapeBodies.push(obj.mBody);
			
			obj.mBody = null;
			break;
		}
		
	}
}