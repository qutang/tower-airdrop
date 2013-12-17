package edu.neu.madcourse.qutang.towerairdrop;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Paint.Align;
import android.graphics.Paint.Style;
import android.graphics.RectF;
import android.graphics.Typeface;

public class TextObject {
	private int left;
	private int top;
	private float size;
	public int color;
	private Bitmap mBitmap;
	private RectF picRectF;
	private Paint textPaint;
	private Paint picPaint;
	public String text;
	public Typeface tf;
	public boolean visible;
	
	public TextObject(int drawable_src){
		tf = Typeface.createFromAsset(GameApplication.getAppContext().getAssets(),
	            "fonts/CloudsAndBlue.ttf");
		tf = Typeface.create(tf, Typeface.BOLD);
		mBitmap = BitmapFactory.decodeResource(GameApplication.getAppContext().getResources(), drawable_src);
		picRectF = new RectF();
		
		textPaint = new Paint();
		textPaint.setTextAlign(Align.LEFT);
		textPaint.setTypeface(tf);
		textPaint.setStyle(Style.FILL);
		
		visible = true;
		
		picPaint = new Paint();
	}
	
	public void setPosition(int _left, int _top, float _size){
		left = _left;
		top = _top;
		size = _size;
		picRectF.left = left;
		picRectF.top = top*0.5f;
		picRectF.right = left + size;
		picRectF.bottom = top*0.5f + size;
		textPaint.setTextSize(size);
	}
	
	public void setColor(int _color){
		color = _color;
		textPaint.setColor(color);
	}
	
	public void draw(Canvas canvas){
		if(!visible || text == null){
			return;
		}
		canvas.drawText(text, picRectF.right + size*0.1f, top, textPaint);
		canvas.drawBitmap(mBitmap, null, picRectF, picPaint);
	}
}
