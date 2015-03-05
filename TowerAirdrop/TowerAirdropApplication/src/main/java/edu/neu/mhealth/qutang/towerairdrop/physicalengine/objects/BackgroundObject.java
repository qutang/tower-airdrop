package edu.neu.mhealth.qutang.towerairdrop.physicalengine.objects;

import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.drawable.BitmapDrawable;

import edu.neu.mhealth.qutang.towerairdrop.Constants;
import edu.neu.mhealth.qutang.towerairdrop.GameApplication;

public class BackgroundObject {
	public Rect posRect;
	private RectF headRectF = new RectF();;
	private BitmapDrawable mBg;
	private Paint headPaint;

	public BackgroundObject() {
		headPaint = new Paint();
		headPaint.setColor(Color.WHITE);
		headPaint.setStyle(Style.FILL);
	}

	public BackgroundObject(Rect pos) {
		posRect = pos;
		headRectF.left = posRect.width() * Constants.HEADER_LEFT;
		headRectF.top = posRect.height() * Constants.HEADER_TOP;
		headRectF.right = posRect.width() * Constants.HEADER_RIGHT;
		headRectF.bottom = posRect.top + posRect.height()
				* Constants.HEADER_HEIGHT;

		headPaint = new Paint();
		headPaint.setColor(Color.WHITE);
		headPaint.setStyle(Style.FILL);
	}

	public void setPosition(Rect pos) {
		posRect = pos;
		headRectF.left = posRect.width() * Constants.HEADER_LEFT;
		headRectF.top = posRect.height() * Constants.HEADER_TOP;
		headRectF.right = posRect.width() * Constants.HEADER_RIGHT;
		headRectF.bottom = posRect.top + posRect.height()
				* Constants.HEADER_HEIGHT;
	}

	public void setBackground(int source) {
		Bitmap mbitmap = BitmapFactory.decodeResource(GameApplication.getAppContext().getResources(), source);
		mBg = new BitmapDrawable(mbitmap);
//		mBg = new GradientDrawable();
//		mBg = (GradientDrawable) GameApplication.getAppContext().getResources()
//				.getDrawable(source);
	}

	public void drawBackground(Canvas canvas) {
		if (mBg == null) {
			return;
		}
		mBg.setBounds(posRect);
		mBg.draw(canvas);
	}

	public void drawHeader(Canvas canvas) {
		if (!headRectF.isEmpty()) {
			canvas.drawRoundRect(headRectF, 20, 20, headPaint);
		}
	}

	public int calculateInSampleSize(BitmapFactory.Options options,
			int reqWidth, int reqHeight) {
		// Raw height and width of image
		final int height = options.outHeight;
		final int width = options.outWidth;
		int inSampleSize = 1;

		if (height > reqHeight || width > reqWidth) {

			final int halfHeight = height / 2;
			final int halfWidth = width / 2;

			// Calculate the largest inSampleSize value that is a power of 2 and
			// keeps both
			// height and width larger than the requested height and width.
			while ((halfHeight / inSampleSize) > reqHeight
					&& (halfWidth / inSampleSize) > reqWidth) {
				inSampleSize *= 2;
			}
		}
		return inSampleSize;
	}

	public Bitmap decodeSampledBitmapFromResource(Resources res, int resId,
			int reqWidth, int reqHeight) {

		// First decode with inJustDecodeBounds=true to check dimensions
		final BitmapFactory.Options options = new BitmapFactory.Options();
		options.inJustDecodeBounds = true;
		BitmapFactory.decodeResource(res, resId, options);

		// Calculate inSampleSize
		options.inSampleSize = this.calculateInSampleSize(options, reqWidth,
				reqHeight);

		// Decode bitmap with inSampleSize set
		options.inJustDecodeBounds = false;
		return BitmapFactory.decodeResource(res, resId, options);
	}
}
