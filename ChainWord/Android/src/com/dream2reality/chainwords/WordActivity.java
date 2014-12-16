package com.dream2reality.chainwords;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.TextView;

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
	}
}