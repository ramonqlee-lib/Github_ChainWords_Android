package com.yees.sdk.widget;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.widget.AutoCompleteTextView;

public class LineEditText extends AutoCompleteTextView {
	private Paint mPaint;

	/**
	 * @param context
	 * @param attrs
	 */
	public LineEditText(Context context, AttributeSet attrs) {
		super(context, attrs);
		if (null == mPaint) {
			mPaint = new Paint();
			mPaint.setStyle(Paint.Style.STROKE);
		}
		mPaint.setColor(Color.BLACK);
	}

	public void setLineColor(int color) {
		mPaint.setColor(color);
	}

	@Override
	public void onDraw(Canvas canvas) {
		super.onDraw(canvas);

		// 画底线
		if (null != mPaint) {
			canvas.drawLine(0, this.getHeight() - 1, this.getWidth() - 1,
					this.getHeight() - 1, mPaint);
		}
	}
}
