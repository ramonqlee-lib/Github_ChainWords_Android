package com.dream2reality.chainwords;

import java.util.List;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.TextView;

import com.android.volley.toolbox.NetworkImageView;
import com.dream2reality.utils.ChainWordsApp;
import com.idreems.sdk.common.runners.GetWordPicsRunner;
import com.idreems.sdk.netmodel.GetWordPicsResp;
import com.idreems.sdk.netmodel.ParsedTaskReponse;
import com.yees.sdk.lightvolley.TaskListener;
import com.yees.sdk.lightvolley.TaskResponse;
import com.yees.sdk.utils.Utils;

/**
 * 单词详细介绍界面
 * 
 * @author ramonqlee
 * 
 */
public class WordActivity extends CustomTitleActivity {
	public static final String EXTRA_KEY = "WordActivity_EXTRA_KEY";
	private CharSequence mWord;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentViewWithCustomTitle(R.layout.layout_word_activity);
		populateViews();
	}

	private void populateViews() {
		Intent intent = getIntent();
		if (null != intent) {
			mWord = intent.getCharSequenceExtra(EXTRA_KEY);
		}
		if (!TextUtils.isEmpty(mWord)) {
			TextView wordView = (TextView) findViewById(R.id.word_textview);
			wordView.setText(mWord.toString());
		}

		GetWordPicsRunner runner = new GetWordPicsRunner(
				getApplicationContext(),
				Utils.getAppVersion(getApplicationContext()), mWord.toString());
		runner.setTaskListener(new TaskListener() {

			@Override
			public void onSuccess(TaskResponse response) {
				if (!(response instanceof ParsedTaskReponse)) {
					return;
				}
				ParsedTaskReponse ret = (ParsedTaskReponse) response;
				if (!(ret.parsedObject instanceof GetWordPicsResp)) {
					return;
				}
				GetWordPicsResp resp = (GetWordPicsResp) ret.parsedObject;
				if(null == resp)
				{
					return;
				}
				List<String> urls = resp.urls;
				if (null != urls && !urls.isEmpty()) {
					String imageUrl = urls.get(0);
					NetworkImageView networkImageView = (NetworkImageView)findViewById(R.id.share_imageview);
					networkImageView.setImageUrl(
							imageUrl,
							ChainWordsApp.sharedInstance().getImageLoader(
									getApplicationContext()));
				}
			}

			@Override
			public void onFailure(int arg0, String arg1) {
				// TODO Auto-generated method stub
			}
		});
		runner.run();

	}
}