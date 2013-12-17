package edu.neu.madcourse.qutang.towerairdrop;

import org.jbox2d.common.Vec2;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;

public class DeadlineObject {
	private Vec2 startPos;
	private Vec2 endPos;
	private String text;
	private Paint linePaint;
	private Paint textPaint;
	public boolean visible;
	
	public DeadlineObject(){
		startPos = new Vec2();
		endPos = new Vec2();
		linePaint = new Paint();
		linePaint.setColor(Color.YELLOW);
		linePaint.setStrokeWidth(2);
		
		textPaint = new Paint();
		textPaint.setColor(Color.GRAY);
		
		
		visible = false;
	}
	
	public void setPosition(float posY){
		startPos.x = 0;
		startPos.y = posY;
		endPos.x = Flags.getFlags().screenW;
		endPos.y = posY;
	}
	
	public void setText(String _text){
		text = _text;
		textPaint.setTextSize(0.02f*Flags.getFlags().screenH);
	}
	
	public void draw(Canvas canvas){
		if(!visible){
			return;
		}
		
		canvas.drawLine(startPos.x, startPos.y, endPos.x, endPos.y, linePaint);
		canvas.drawText(text, startPos.x + 0.4f * Flags.getFlags().screenW, startPos.y - 0.015f*Flags.getFlags().screenH, textPaint);
	}
}
