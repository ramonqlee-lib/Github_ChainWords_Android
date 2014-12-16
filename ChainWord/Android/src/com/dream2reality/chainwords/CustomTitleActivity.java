package com.dream2reality.chainwords;

import android.app.Activity;
import android.view.View;
import android.view.Window;

/**
 * 自定义标题栏的activity
 * 
 * @author ramonqlee
 * 
 */
public class CustomTitleActivity extends Activity {
	
	protected void setContentViewWithCustomTitle(int contentId) {
		requestWindowFeature(Window.FEATURE_CUSTOM_TITLE);
		setContentView(contentId);
		getWindow().setFeatureInt(Window.FEATURE_CUSTOM_TITLE,
				R.layout.layout_custom_titlebar);
	}
	
	public void onBack(View view) {
		onBackPressed();
	}
}