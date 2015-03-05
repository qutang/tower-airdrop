package edu.neu.mhealth.qutang.towerairdrop.physicalengine.objects;

import edu.neu.mhealth.qutang.towerairdrop.Constants;
import edu.neu.mhealth.qutang.towerairdrop.Flags;
import edu.neu.mhealth.qutang.towerairdrop.GameApplication;
import edu.neu.mhealth.qutang.towerairdrop.R;
import edu.neu.mhealth.qutang.towerairdrop.Constants.PROGRESS_TYPE;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.graphics.RectF;

public class ProgressObject {
	private RectF posRect;
	private RectF progressRectF;
	private RectF picRectF;
	public float progress;
	private Bitmap mBitmap;
	private PROGRESS_TYPE progressType;
	private Paint outPaint;
	private Paint inPaint;
	private Paint picPaint;
	public boolean visible;
	
	public ProgressObject(int bitmap_id, PROGRESS_TYPE type){
		posRect = new RectF();
		progressRectF = new RectF();
		picRectF = new RectF();
		progress = 0f;
		mBitmap = BitmapFactory.decodeResource(GameApplication.getAppContext().getResources(), bitmap_id);
		progressType = type;
		outPaint = new Paint();
		outPaint.setStyle(Style.STROKE);
		outPaint.setStrokeWidth(5);
		outPaint.setFlags(Paint.ANTI_ALIAS_FLAG);
		
		inPaint = new Paint();
		inPaint.setStyle(Style.FILL);
		
		picPaint = new Paint();
		
		visible = true;
	}
	
	public void setPosition(RectF pos){
		posRect = pos;
		
		if(posRect.isEmpty()){
			return;
		}
		switch(progressType){
		case PARACHUTE:
			progressRectF.set(posRect);
			
			picRectF.left = posRect.right - posRect.height()*0.85f;
			picRectF.top = posRect.top + posRect.height()*0.1f;
			picRectF.bottom = picRectF.top + posRect.height()*0.8f;
			picRectF.right = picRectF.left + posRect.height()*0.8f;
			break;
		case HEIGHT:
			picRectF.left = posRect.left - posRect.width()*0.3f;
			picRectF.right = posRect.right + posRect.width()*0.3f;
			break;
		case TILT:
			picRectF.bottom = posRect.top - posRect.height()*0.1f;
			picRectF.top = picRectF.bottom - posRect.height()*0.5f;
			picRectF.left = posRect.centerX() - picRectF.height()/2;
			picRectF.right = posRect.centerX() + picRectF.height()/2;
			
			progressRectF.top = posRect.top;
			progressRectF.bottom = posRect.bottom;
			break;
		}
	}
	
	public void draw(Canvas canvas){
		if(this.visible == false){
			return;
		}
		float x_corner = 10 * Flags.getFlags().screenW / Constants.BASELINE_SCREEN_WIDTH;
		float y_corner = 10 * Flags.getFlags().screenH / Constants.BASELINE_SCREEN_HEIGHT;
		
		switch(progressType){
		case PARACHUTE:
			progressRectF.right = progressRectF.left + progress * posRect.width();
			
			outPaint.setColor(GameApplication.getAppContext().getResources().getColor(R.color.sunsphere));
			inPaint.setColor(GameApplication.getAppContext().getResources().getColor(R.color.sunsphere));
			
			canvas.drawRoundRect(posRect, x_corner, y_corner, outPaint);
			canvas.drawRoundRect(progressRectF, x_corner, y_corner, inPaint);
			canvas.drawBitmap(mBitmap, null, picRectF, picPaint);
			break;
		case HEIGHT:
			picRectF.top = posRect.top + progress * posRect.height() - picRectF.width()/2;
			picRectF.bottom = posRect.top + progress * posRect.height() + picRectF.width()/2;
			
			outPaint.setColor(GameApplication.getAppContext().getResources().getColor(R.color.valley));
			
			canvas.drawRoundRect(posRect, x_corner, y_corner, outPaint);
			canvas.drawBitmap(mBitmap, null, picRectF, picPaint);
			break;
		case TILT:
			if(progress < 1 && progress > 0){
				progressRectF.left = posRect.left + progress * posRect.width() - posRect.width() * 0.02f;
				progressRectF.right = posRect.left + progress * posRect.width() + posRect.width() * 0.02f;
				if(progress < 0.85 && progress > 0.15){
					outPaint.setColor(GameApplication.getAppContext().getResources().getColor(R.color.valley));
					inPaint.setColor(GameApplication.getAppContext().getResources().getColor(R.color.valley));
				}else{
					outPaint.setColor(Color.RED);
					inPaint.setColor(Color.RED);
				}
				canvas.drawRect(progressRectF, inPaint);
			}else{
				if(progress >= 1){
					progressRectF.left = posRect.left + posRect.width() - posRect.width() * 0.05f;
					progressRectF.right = posRect.left + posRect.width();
				}else if(progress <= 0){
					progressRectF.left = posRect.left;
					progressRectF.right = posRect.left + posRect.width() * 0.05f;
				}
				outPaint.setColor(Color.RED);
				inPaint.setColor(Color.RED);
				canvas.drawRoundRect(progressRectF, x_corner, y_corner, inPaint);
			}
			
			canvas.drawRoundRect(posRect, x_corner, y_corner, outPaint);
			canvas.drawBitmap(mBitmap, null, picRectF, picPaint);
			
			break;
		}
		
	}
}
