package com.dream2reality.chainwords;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

import com.dream2reality.utils.AppConstants;
import com.idreems.update.UpdateManager;
import com.umeng.fb.FeedbackAgent;
import com.yees.sdk.utils.Config;

public class SettingActivity extends CustomBaseActivity {
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentViewWithCustomTitle(R.layout.layout_setting_activity);
		populateViews();
	}

	private void populateViews() {
		// 初始化数据
		TextView tView = (TextView) findViewById(R.id.title_textview);
		tView.setText(R.string.setting_title_string);

		ListView listView = (ListView) findViewById(R.id.setting_listview);
		String[] data = { getString(R.string.setting_list_item_vocabulary),
				getString(R.string.setting_list_item_version),
				getString(R.string.setting_list_item_check_new_version),
				getString(R.string.setting_list_item_daily_sentence),
				getString(R.string.setting_list_item_set_player_name),
				getString(R.string.setting_list_item_feed_back)};

		// 构造一个数组对象，也就是数据
		listView.setAdapter(new ArrayAdapter<String>(this,
				android.R.layout.simple_list_item_1, data));
		listView.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1,
					int position, long row) {
				switch (position) {
				case 0: {
					Intent intent = new Intent(SettingActivity.this,
							VocabularyActivity.class);
					startActivity(intent);
				}
					break;
				case 1:
				// TODO 版本说明
				{
					Intent intent = new Intent(SettingActivity.this,
							AboutActivity.class);
					startActivity(intent);
				}
					break;
				case 2:
					// 检查新版本
					UpdateManager.getIntance().checkNewVersion(
							SettingActivity.this,
							getString(R.string.newest_version_already));
					break;
				case 3: {
					Intent intent = new Intent(SettingActivity.this,
							SplashActivity.class);
					intent.putExtra(SplashActivity.HIDE_SKIP_BUTTON_KEY, true);
					startActivity(intent);
				}
					break;
				case 4:
					// 设置昵称
					popSetPlayerNameDialog(SettingActivity.this);
					break;
				case 5:
					// 用户反馈
					new FeedbackAgent(SettingActivity.this).startFeedbackActivity();
					break;
				default:
					break;
				}
			}
		});
	}

	// 弹出游戏结束提示，并给出是否上传战绩选项
	private void popSetPlayerNameDialog(final Context context) {
		LayoutInflater factory = LayoutInflater.from(this);
		final View textEntryView = factory.inflate(
				R.layout.alert_dialog_set_playername, null);
		String playerName = Config.sharedInstance(context).getString(
				AppConstants.PLAY_NAME_KEY);
		final EditText nameEdit = (EditText) textEntryView
				.findViewById(R.id.playerName_edittext);
		nameEdit.setText(playerName);

		new AlertDialog.Builder(context)
				.setTitle(R.string.setting_list_item_set_player_name)
				.setView(textEntryView)
				.setPositiveButton(R.string.ok, new OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						String playerName = nameEdit.getText().toString();
						Config.sharedInstance(context).putString(
								AppConstants.PLAY_NAME_KEY, playerName);
					}
				}).setNegativeButton(R.string.cancel, null).show();
	}
}
