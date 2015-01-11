package com.dream2reality.chainwords;

import android.app.Activity;
import android.view.MotionEvent;
import android.view.VelocityTracker;
import android.view.View;
import android.view.Window;

import com.dream2reality.utils.AppConstants;
import com.umeng.analytics.MobclickAgent;
import com.yees.sdk.utils.Logger;

/**
 * 自定义标题栏的activity
 * 
 * @author ramonqlee
 * 
 */
public class CustomBaseActivity extends Activity {
	// 手指向右滑动时的最小速度
	private static final int XSPEED_MIN = 200;

	// 手指向右滑动时的最小距离
	private static final int XDISTANCE_MIN = 150;

	// 记录手指按下时的横坐标。
	private float xDown;

	// 记录手指移动时的横坐标。
	private float xMove;

	// 用于计算手指滑动的速度。
	private VelocityTracker mVelocityTracker;

	public void onResume() {
		super.onResume();
		MobclickAgent.onResume(this);
	}

	public void onPause() {
		super.onPause();
		MobclickAgent.onPause(this);
	}

	protected void setContentViewWithCustomTitle(int contentId) {
		requestWindowFeature(Window.FEATURE_CUSTOM_TITLE);
		setContentView(contentId);
		getWindow().setFeatureInt(Window.FEATURE_CUSTOM_TITLE,
				R.layout.layout_custom_titlebar);
	}

	// 返回支持
	public void onBack(View view) {
		onBackPressed();
	}

	@Override
	public boolean onTouchEvent(MotionEvent event) {
		createVelocityTracker(event);
		switch (event.getAction()) {
		case MotionEvent.ACTION_DOWN:
			xDown = event.getRawX();
			break;
		case MotionEvent.ACTION_MOVE:
			xMove = event.getRawX();
			// 活动的距离
			int distanceX = (int) (xMove - xDown);
			// 获取顺时速度
			int xSpeed = getScrollVelocity();
			// 当滑动的距离大于我们设定的最小距离且滑动的瞬间速度大于我们设定的速度时，返回到上一个activity
			Logger.d(AppConstants.LOG_TAG, "distanceX = " + distanceX);
			Logger.d(AppConstants.LOG_TAG, "XDISTANCE_MIN = " + XDISTANCE_MIN);
			Logger.d(AppConstants.LOG_TAG, "xSpeed = " + xSpeed);
			Logger.d(AppConstants.LOG_TAG, "XSPEED_MIN = " + XSPEED_MIN);
			if (distanceX > XDISTANCE_MIN && xSpeed > XSPEED_MIN) {
				finish();
			}
			break;
		case MotionEvent.ACTION_UP:
			recycleVelocityTracker();
			break;
		default:
			break;
		}
		return super.onTouchEvent(event);
	}

	/**
	 * 创建VelocityTracker对象，并将触摸content界面的滑动事件加入到VelocityTracker当中。
	 * 
	 * @param event
	 * 
	 */
	private void createVelocityTracker(MotionEvent event) {
		if (mVelocityTracker == null) {
			mVelocityTracker = VelocityTracker.obtain();
		}
		mVelocityTracker.addMovement(event);
	}

	/**
	 * 回收VelocityTracker对象。
	 */
	private void recycleVelocityTracker() {
		mVelocityTracker.recycle();
		mVelocityTracker = null;
	}

	/**
	 * 获取手指在content界面滑动的速度。
	 * 
	 * @return 滑动速度，以每秒钟移动了多少像素值为单位。
	 */
	private int getScrollVelocity() {
		mVelocityTracker.computeCurrentVelocity(1000);
		int velocity = (int) mVelocityTracker.getXVelocity();
		return Math.abs(velocity);
	}
}