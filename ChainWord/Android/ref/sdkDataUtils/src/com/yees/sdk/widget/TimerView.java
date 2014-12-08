package com.yees.sdk.widget;


import android.content.Context;
import android.graphics.Color;
import android.os.SystemClock;
import android.util.AttributeSet;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.yees.sdk.widget.ChronometerEx.ISecondListener;


public class TimerView extends LinearLayout {
	
	private long time;
	public boolean isNegative = false;
	private float textSize = 0;
	private int textColor = 0xff000000;
	private TextView mCountDownTimerView;
	private ChronometerEx mChronometer;
	private MyCount mc;
	private IFinishListener finishListener;
	private ITimeoutListener timeoutListener;
	private boolean isTimeFormat = false;
	
	public TimerView(Context context) {
		super(context);
	}

	public TimerView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
	}

	public TimerView(Context context, AttributeSet attrs) {
		super(context, attrs);
	}
	
	/**
	 * 是否支持时间格式化  true:到时时间为"0:00" false :到时时间为"00:00:00"
	 * @param isTimeFormat
	 */
	public void setTimeFormat(boolean isTimeFormat) {
		this.isTimeFormat = isTimeFormat;
	}

	/**
	 * 设置倒计时时间
	 * @param time (millis)
	 */
	public void setTime(long time) {
		this.time = time;
	}

	/**
	 * 设置是否支持负数
	 * @param isNegative
	 */
	public void setNegative(boolean isNegative) {
		this.isNegative = isNegative;
	}

	/**
	 * 设置文字大小
	 * @param textSize
	 */
	public void setTextSize(float textSize) {
		this.textSize = textSize;
	}
	public void setTextColor(int textColor) {
		this.textColor = textColor;
	}
	
	public void start() {
		
		if(mChronometer != null){
			mChronometer.stop();
			TimerView.this.removeView(mChronometer);
		}
		if(mc != null){
			mc.cancel();
		}
		mc = new MyCount(time, 1000);
		if(mCountDownTimerView == null){
			mCountDownTimerView = new TextView(getContext());
			mCountDownTimerView.setTextColor(Color.BLACK);
			if(textSize == 0){
				mCountDownTimerView.setTextSize(16);
			}else{
				mCountDownTimerView.setTextSize(textSize);
			}
		}else{
			removeView(mCountDownTimerView);
			mCountDownTimerView = new TextView(getContext());
			mCountDownTimerView.setTextColor(Color.BLACK);
			if(textSize == 0){
				mCountDownTimerView.setTextSize(16);
			}else{
				mCountDownTimerView.setTextSize(textSize);
			}
		}
		mCountDownTimerView.setTextColor(textColor);
		
		addView(mCountDownTimerView);
		mc.start();
	}

	/**
	 * 即时停止
	 */
	public void stop() {
		if(mc != null){
			mc.cancel();
		}
		if(mChronometer != null){
			mChronometer.stop();
		}
	}

	/**
	 * 倒计时类
	 * @author Max
	 */
	private class MyCount extends CountDownTimerEx implements ISecondListener {
		public MyCount(long millisInFuture, long countDownInterval) {
			super(millisInFuture, countDownInterval);
		}
		
		@Override
		public void onFinish() {
			if(isTimeFormat){
				mCountDownTimerView.setText("0:00");
			}else{
				mCountDownTimerView.setText("00:00:00");
			}
			
			if(finishListener != null){
				finishListener.OnFinish();
			}
			
			if(isNegative){ //是否支持负数显示
				TimerView.this.removeView(mCountDownTimerView);
				
				if(mChronometer != null){
					mChronometer.stop();
					TimerView.this.removeView(mChronometer);
				}
				mChronometer = new ChronometerEx(TimerView.this.getContext());
				if(time < 0){
					mChronometer.setBase(SystemClock.elapsedRealtime()+ time);
				}
//				mChronometer.setShowUp(true);
				if(textSize == 0){
					mChronometer.setTextSize(16);
				}else{
					mChronometer.setTextSize(textSize);
				}
				TimerView.this.addView(mChronometer);
				mChronometer.setTextColor(Color.RED);
				mChronometer.setSecondListener(this);
				mChronometer.start();
			}
		}
		@Override
		public void onTick(long millisUntilFinished) {
			mCountDownTimerView.setText(dealTime(millisUntilFinished / 1000));
			if(tickListener != null){
				tickListener.onTick(millisUntilFinished);
			}
		}
		
		@Override
		public void OnSecond(long seconds) {
			if(timeoutListener != null){
				timeoutListener.Timeout(seconds);
			}
		}
	}

	/**
	 * deal time string
	 * 
	 * @param time
	 * @return
	 */
	public static String dealTime(long time) {
		StringBuffer returnString = new StringBuffer();
		long hours = (time % (24 * 60 * 60)) / (60 * 60);
		long minutes = ((time % (24 * 60 * 60)) % (60 * 60)) / 60;
		long second = ((time % (24 * 60 * 60)) % (60 * 60)) % 60;
		String hoursStr = timeStrFormat(String.valueOf(hours));
		String minutesStr = timeStrFormat(String.valueOf(minutes));
		String secondStr = timeStrFormat(String.valueOf(second));
		returnString.append(hoursStr).append(":").append(minutesStr)
				.append(":").append(secondStr);
		return returnString.toString();
	}
	/**
	 * format time
	 * 
	 * @param timeStr
	 * @return
	 */
	private static String timeStrFormat(String timeStr) {
		switch (timeStr.length()) {
			case 1 :
				timeStr = "0" + timeStr;
				break;
		}
		return timeStr;
	}
	
	public void setOnFinishListener(IFinishListener finishListener) {
		this.finishListener = finishListener;
	}
	
	public interface IFinishListener {
		void OnFinish();
	}
	
	public void setOnTimeoutListener(ITimeoutListener timeoutListener) {
		this.timeoutListener = timeoutListener;
	}

	public interface ITimeoutListener {
		void Timeout(long time);
	}
	
	private ITimeTickListener tickListener;
	
	public void setTickListener(ITimeTickListener tickListener) {
		this.tickListener = tickListener;
	}

	public interface ITimeTickListener {
		void onTick(long millisUntilFinished);
	}
	

}
