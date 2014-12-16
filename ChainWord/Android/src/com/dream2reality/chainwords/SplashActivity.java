package com.dream2reality.chainwords;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;

import com.android.volley.toolbox.NetworkImageView;
import com.dream2reality.utils.ChainWordsApp;
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

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.layout_splash_activity);
		populateViews();
	}

	private void populateViews() {
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
				GetDailySentenceResp resp = (GetDailySentenceResp) ret.parsedObject;
				if (!TextUtils.isEmpty(resp.shareImageUrl)) {
					// 显示图片
					NetworkImageView networkImageView = (NetworkImageView) findViewById(R.id.share_imageview);
					networkImageView.setImageUrl(
							resp.shareImageUrl,
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
}