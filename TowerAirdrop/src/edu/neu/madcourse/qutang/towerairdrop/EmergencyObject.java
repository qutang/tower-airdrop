package edu.neu.madcourse.qutang.towerairdrop;

import java.util.Random;

import org.jbox2d.common.Vec2;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;
import edu.neu.madcourse.qutang.towerairdrop.R;
import edu.neu.madcourse.qutang.towerairdrop.Constants.EMERGENCY_TYPE;

public class EmergencyObject {
	private EMERGENCY_TYPE emergency_type;
	private BoxObject mainBox;
	private Bitmap lock;
	private Bitmap unlock;
	private Bitmap cloud;
	private Bitmap lightning;
	public RectF pos1;
	private Paint paint1;
	public boolean solved = false;
	public int counter = Constants.EMERGENCY_MAX_COUNTER;
	private Random r = new Random();
	
	public EmergencyObject(EMERGENCY_TYPE type){
		emergency_type = type;
		pos1 = new RectF();
		paint1 = new Paint();
		lock = BitmapFactory.decodeResource(GameApplication.getAppContext().getResources(), R.drawable.lock);
		unlock = BitmapFactory.decodeResource(GameApplication.getAppContext().getResources(), R.drawable.unlock);
		cloud = BitmapFactory.decodeResource(GameApplication.getAppContext().getResources(), R.drawable.cloud);
		lightning = BitmapFactory.decodeResource(GameApplication.getAppContext().getResources(), R.drawable.lightning);
	}

	public void setMainBox(BoxObject actor1){
		mainBox = actor1;
	}
	
	public void updateEmergency(EMERGENCY_TYPE type){
		emergency_type = type;
		counter = Constants.EMERGENCY_MAX_COUNTER;
		solved = false;
		paint1.setAlpha(255);
		if(emergency_type == EMERGENCY_TYPE.LIGHTNING){
			updateStatus();
		}
	}
	
	public EMERGENCY_TYPE getType(){
		return emergency_type;
	}
	
	public void updateStatus(){
		pos1.left = (r.nextFloat()*0.6f + 0.2f)*Flags.getFlags().screenW;
		pos1.top = (r.nextFloat()*0.7f + 0.1f)*Flags.getFlags().screenH;
		pos1.right = pos1.left + Flags.getFlags().screenW*0.2f;
		pos1.bottom = pos1.top + Flags.getFlags().screenW*0.2f;
	}
	
	public void draw(Canvas canvas){
		if(mainBox == null || !Flags.getFlags().isInEmergency){
			return;
		}
		
		Vec2 boxPos = mainBox.getPosition();
		switch(emergency_type){
		case STUCK:
			pos1.left = boxPos.x - mainBox.getWidth()*0.4f;
			pos1.right = boxPos.x + mainBox.getWidth()*0.4f;
			pos1.top = boxPos.y - mainBox.getWidth()*0.4f;
			pos1.bottom = boxPos.y + mainBox.getWidth()*0.4f;
			if(!solved){
				canvas.drawBitmap(lock, null, pos1, paint1);
			}else{
				canvas.drawBitmap(unlock, null, pos1, paint1);
			}
			break;
		case BLIND:
			if(!solved){
				paint1.setAlpha(255*counter/Constants.EMERGENCY_MAX_COUNTER);
				pos1.left = boxPos.x - mainBox.getWidth()*1.5f;
				pos1.right = boxPos.x + mainBox.getWidth()*1.5f;
				pos1.top = boxPos.y - mainBox.getWidth()*1.5f;
				pos1.bottom = boxPos.y + mainBox.getWidth()*1.5f;
				canvas.drawBitmap(cloud, null, pos1, paint1);
			}
			break;
		case LIGHTNING:
			if(!solved){
				canvas.drawBitmap(lightning, null, pos1, paint1);
			}
			break;
		}
	}
}
