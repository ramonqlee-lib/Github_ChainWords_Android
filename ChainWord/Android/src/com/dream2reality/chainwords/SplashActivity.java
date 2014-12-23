package com.dream2reality.chainwords;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;

import com.android.volley.toolbox.NetworkImageView;
import com.dream2reality.utils.ChainWordsApp;
import com.dream2reality.utils.TTSPlayer;
import com.idreems.sdk.common.runners.GetDailySentenceRunner;
import com.idreems.sdk.netmodel.GetDailySentenceResp;
import com.idreems.sdk.netmodel.ParsedTaskReponse;
import com.yees.sdk.lightvolley.TaskListener;
import com.yees.sdk.lightvolley.TaskResponse;
import com.yees.sdk.utils.Utils;

/**
 * 展示每日一句
 * 
 * @author ramonqlee
 * 
 */
public class SplashActivity extends Activity {
	public static final String HIDE_SKIP_BUTTON_KEY = "HIDE_SKIP_BUTTON_KEY";
	
	private GetDailySentenceResp mGetDailySentenceResp;
	private boolean hideSkipButton;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.layout_splash_activity);
		// 是否隐藏skip button
		Intent intent = getIntent();
		if(null != intent)
		{
			hideSkipButton = intent.getBooleanExtra(HIDE_SKIP_BUTTON_KEY, false);
		}
		populateViews();
	}

	private void populateViews() {
		hideSkipButton();
		
		// 获取金山词霸的每日一句，并展现
		// 目前展现是的分享图片，后续考虑加入自定义展现
		GetDailySentenceRunner runner = new GetDailySentenceRunner(
				getApplicationContext(),
				Utils.getAppVersion(getApplicationContext()));
		runner.setTaskListener(new TaskListener() {

			@Override
			public void onSuccess(TaskResponse response) {
				if (!(response instanceof ParsedTaskReponse)) {
					return;
				}
				ParsedTaskReponse ret = (ParsedTaskReponse) response;
				if (!(ret.parsedObject instanceof GetDailySentenceResp)) {
					return;
				}
				mGetDailySentenceResp = (GetDailySentenceResp) ret.parsedObject;
				if (!TextUtils.isEmpty(mGetDailySentenceResp.shareImageUrl)) {
					// 显示图片
					NetworkImageView networkImageView = (NetworkImageView) findViewById(R.id.share_imageview);
					networkImageView.setImageUrl(
							mGetDailySentenceResp.shareImageUrl,
							ChainWordsApp.sharedInstance().getImageLoader(
									getApplicationContext()));
				}
			}

			@Override
			public void onFailure(int arg0, String arg1) {
				jumpToNextActivity(null);
			}
		});
		runner.run();
	}

	public void jumpToNextActivity(View v) {
		finish();
		Intent intent = new Intent(this, MainActivity.class);
		startActivity(intent);
	}

	public void speak(View v) {
		if (null == mGetDailySentenceResp) {
			return;
		}
		TTSPlayer.sharedInstance(this).speak(mGetDailySentenceResp.content);
	}
	
	private void hideSkipButton()
	{
		if (hideSkipButton) {
			View view = findViewById(R.id.skip_button);
			if (null != view) {
				view.setVisibility(View.GONE);
			}
		}
	}
	
}