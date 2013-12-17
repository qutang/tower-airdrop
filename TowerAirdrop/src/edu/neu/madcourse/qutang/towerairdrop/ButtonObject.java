package edu.neu.madcourse.qutang.towerairdrop;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;
import edu.neu.madcourse.qutang.towerairdrop.Constants.BUTTON_TYPE;

public class ButtonObject {
	public RectF posRect;
	public BUTTON_TYPE buttonType;
	private BaseEventHandler mBaseEventHandler;
	public boolean visible;
	
	private Bitmap mBitmap;
	
	public ButtonObject(int src, BUTTON_TYPE type, BaseEventHandler handler){
		mBitmap = BitmapFactory.decodeResource(GameApplication.getAppContext().getResources(), src);
		buttonType = type;
		mBaseEventHandler = handler;
		visible = false;
	}
	
	public ButtonObject() {
		
	}

	public void draw(Canvas canvas){
		if(!visible){
			return;
		}
		
		Paint paint = new Paint();
		canvas.drawBitmap(mBitmap, null, posRect, paint);
	}
	
	public boolean isClicked(int x, int y){
		if(x >= posRect.left && x <= posRect.right && y >= posRect.top && y <= posRect.bottom){
			return true;
		}else{
			return false;
		}
	}
	
	public void onClick(){
		switch(buttonType){
		case PLAY:
			mBaseEventHandler.sendEmptyMessage(Constants.GAME_EVENTS.GAME_PAUSE.ordinal());
			break;
		case PAUSE:
			mBaseEventHandler.sendEmptyMessage(Constants.GAME_EVENTS.GAME_RESUME.ordinal());
			break;
		case QUIT:
			mBaseEventHandler.sendEmptyMessage(Constants.GAME_EVENTS.GAME_QUIT.ordinal());
			break;
		default:
			break;
		}
	}
}
