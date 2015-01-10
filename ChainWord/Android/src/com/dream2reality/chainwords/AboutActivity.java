package com.dream2reality.chainwords;

import android.os.Bundle;
import android.widget.TextView;

/**
 * 关于
 * 
 * @author ramonqlee
 * 
 */
public class AboutActivity extends CustomBaseActivity {
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentViewWithCustomTitle(R.layout.layout_about_activity);
		// 初始化数据
		TextView tView = (TextView) findViewById(R.id.title_textview);
		tView.setText(R.string.about);
	}
	
}