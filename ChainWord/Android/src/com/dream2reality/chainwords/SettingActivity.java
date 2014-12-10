package com.dream2reality.chainwords;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

import com.dream2reality.constants.AppConstants;
import com.idreems.update.UpdateManager;
import com.yees.sdk.utils.Config;

public class SettingActivity extends Activity {
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_CUSTOM_TITLE);
		setContentView(R.layout.layout_setting_activity);
		getWindow().setFeatureInt(Window.FEATURE_CUSTOM_TITLE,
				R.layout.layout_custom_titlebar);
		populateViews();
	}

	public void onBack(View view) {
		onBackPressed();
	}

	private void populateViews() {
		// 初始化数据
		TextView tView = (TextView) findViewById(R.id.title_textview);
		tView.setText(R.string.setting_title_string);

		ListView listView = (ListView) findViewById(R.id.setting_listview);
		String[] data = { getString(R.string.setting_list_item_vocabulary),
				getString(R.string.setting_list_item_version),
				getString(R.string.setting_list_item_check_new_version),
				getString(R.string.setting_list_item_recommend),
				getString(R.string.setting_list_item_set_player_name) };

		// 构造一个数组对象，也就是数据
		listView.setAdapter(new ArrayAdapter<String>(this,
				android.R.layout.simple_list_item_1, data));
		listView.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1,
					int position, long row) {
				// TODO 事件处理
				switch (position) {
				case 0:
					// TODO 词汇表选择
					Intent intent = new Intent(SettingActivity.this,
							VocabularyActivity.class);
					startActivity(intent);
					break;
				case 1:
					// TODO 版本说明
					break;
				case 2:
					// TODO 新版本检测
					// 检查新版本
					UpdateManager.getIntance().checkNewVersion(SettingActivity.this,getString(R.string.newest_version_already));
					break;
				case 3:
					// TODO 推荐给好友
					break;
				case 4:
					// 设置昵称
					popSetPlayerNameDialog(SettingActivity.this);
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
		
		new AlertDialog.Builder(context).setTitle(R.string.setting_list_item_set_player_name)
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
