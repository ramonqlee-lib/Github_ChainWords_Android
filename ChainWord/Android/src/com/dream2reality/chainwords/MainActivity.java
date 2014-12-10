package com.dream2reality.chainwords;

import java.util.ArrayList;
import java.util.Locale;
import java.util.Random;

import android.app.AlertDialog;
import android.app.ListActivity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.baidu.navisdk.BNaviEngineManager.NaviEngineInitListener;
import com.baidu.navisdk.BaiduNaviManager;
import com.baidu.navisdk.comapi.tts.BNTTSPlayer;
import com.color.speechbubble.AwesomeAdapter;
import com.color.speechbubble.Message;
import com.color.speechbubble.Utility;
import com.dream2reality.constants.AppConstants;
import com.idreems.sdk.common.runners.AddTopListRunner;
import com.idreems.sdk.netmodel.ParsedTaskReponse;
import com.idreems.update.UpdateManager;
import com.yees.sdk.lightvolley.TaskListener;
import com.yees.sdk.lightvolley.TaskResponse;
import com.yees.sdk.utils.Config;
import com.yees.sdk.utils.Constants;
import com.yees.sdk.utils.Logger;
import com.yees.sdk.utils.Utils;

/**
 * 主界面
 * 
 * @author ramonqlee
 * 
 */
public class MainActivity extends ListActivity {
	/** Called when the activity is first created. */
	ArrayList<Message> messages;
	AwesomeAdapter adapter;
	EditText text;
	TextView pointView;
	static Random rand = new Random();
	static String sender;
	private boolean robolicsRunning;
	private String mRobolicWord;

	private boolean mIsEngineInitSuccess = false;
	private NaviEngineInitListener mNaviEngineInitListener = new NaviEngineInitListener() {
		public void engineInitSuccess() {
			// 导航初始化是异步的，需要一小段时间，以这个标志来识别引擎是否初始化成功，为true时候才能发起导航
			mIsEngineInitSuccess = true;
			Logger.i(AppConstants.LOG_TAG, "engineInitSuccess");
		}

		public void engineInitStart() {
		}

		public void engineInitFail() {
		}
	};

	private void saveAndUpdatePointsView(int increasedPoints) {
		int points = Config.sharedInstance(getApplicationContext()).getInt(
				AppConstants.POINTS_KEY);
		if (points < 0) {
			points = 0;
		}
		points += increasedPoints;
		Config.sharedInstance(getApplicationContext()).putInt(
				AppConstants.POINTS_KEY, points);

		pointView.setText(String.valueOf(points));
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		// 初始化导航引擎
		BaiduNaviManager.getInstance().initEngine(this, getSdcardDir(),
				mNaviEngineInitListener, "RqUEl4HtPM7NMbPdNtLdxGHK", null);

		// 初始化TTS
		BNTTSPlayer.initPlayer();
		// 启动activity时不自动弹出软键盘
		getWindow().setSoftInputMode(
				WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);

		setContentView(R.layout.layout_main_activity);
		// global setting
		Constants.setAppContext(getApplicationContext());
		Constants.enableLocalLog(true);

		text = (EditText) this.findViewById(R.id.view_edittext);
		pointView = (TextView) findViewById(R.id.point_textview);
		saveAndUpdatePointsView(0);

		sender = Utility.sender[rand.nextInt(Utility.sender.length - 1)];
		this.setTitle(sender);
		messages = new ArrayList<Message>();

		// 第一次时，在此加入引导用的示例
		if (!Config.sharedInstance(getApplicationContext()).getBoolean(
				AppConstants.CHAIN_WORD_UI_TOUCHED)) {
			Message msg = new Message("expert", false);
			msg.setTailSpan();
			messages.add(msg);

			msg = new Message("terrific", true);
			msg.setHeadSpan();
			messages.add(msg);

			msg = new Message("can", false);
			msg.setTailSpan();
			messages.add(msg);

			msg = new Message("next", true);
			msg.setHeadSpan();
			messages.add(msg);

			msg = new Message("traffic", false);
			msg.setHeadSpan();
			messages.add(msg);

			messages.add(new Message(true, getString(R.string.your_turn)));
		} else {
			// 自动挑选一个单词作为单词接龙的第一个单词
			mRobolicWord = Utility.getNewWord(getApplicationContext());
			Message msg = new Message(mRobolicWord, false);
			msg.setTailSpan();
			messages.add(msg);
			messages.add(new Message(true, getString(R.string.your_turn)));
		}

		adapter = new AwesomeAdapter(this, messages);
		setListAdapter(adapter);

		// FIXME 测试
		// confirmGameOver(this);
	}

	// 设置按钮点击事件
	public void settingOnClick(View view) {
		Intent intent = new Intent(this, SettingActivity.class);
		startActivity(intent);
	}

	public void topListOnClick(View view) {
		Intent intent = new Intent(this, TopListActivity.class);
		startActivity(intent);
	}

	public void tipClick(View view) {
		// TODO 增加给出一个下一个单词的提示
		Toast.makeText(getApplicationContext(), R.string.empty_pk_string,
				Toast.LENGTH_SHORT).show();
	}

	// 发送消息
	public void sendMessage(View v) {
		String newMessage = text.getText().toString().trim();
		if (TextUtils.isEmpty(newMessage)) {
			Toast.makeText(getApplicationContext(), R.string.empty_pk_string,
					Toast.LENGTH_SHORT).show();
			return;
		}
		if (robolicsRunning) {
			Toast.makeText(getApplicationContext(), R.string.robolics_running,
					Toast.LENGTH_SHORT).show();
			return;
		}

		// 已经看过第一次的说明了，隐藏这个引导
		if (!Config.sharedInstance(getApplicationContext()).getBoolean(
				AppConstants.CHAIN_WORD_UI_TOUCHED)) {
			Config.sharedInstance(getApplicationContext()).putBoolean(
					AppConstants.CHAIN_WORD_UI_TOUCHED, true);
		}

		// FIXME 校验输入单词的合法性
		// 1 确实和上次的单词符合接龙条件
		if (!TextUtils.isEmpty(mRobolicWord)) {
			char lastLetter = mRobolicWord.toLowerCase(Locale.ENGLISH).charAt(
					mRobolicWord.length() - 1);
			char firstLetter = newMessage.toLowerCase(Locale.ENGLISH).charAt(0);
			if (lastLetter != firstLetter) {
				Toast.makeText(getApplicationContext(),
						R.string.fail_to_obey_rule, Toast.LENGTH_LONG).show();
				return;
			}
		}

		if (newMessage.length() > 0) {
			robolicsRunning = true;
			int increasedPoints = newMessage.length();
			saveAndUpdatePointsView(increasedPoints);
			text.setText("");
			Message msg = new Message(newMessage, true);
			msg.setHeadSpan();
			addNewMessage(msg);
			// 在此加入自动回答的单词
			new SendMessage(newMessage).execute();
		}
	}

	/**
	 * 发送消息的任务
	 * 
	 * @author ramonqlee
	 * 
	 */
	private class SendMessage extends AsyncTask<Void, String, String> {
		private String mMessageString;

		public SendMessage(String message) {
			mMessageString = message;
		}

		@Override
		protected String doInBackground(Void... params) {
			try {
				Thread.sleep(2000); // simulate a network call
			} catch (InterruptedException e) {
				e.printStackTrace();
			}

			this.publishProgress(String.format("%s started thinking", sender));
			try {
				Thread.sleep(2000); // simulate a network call
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			this.publishProgress(String.format("%s has found a word", sender));
			try {
				Thread.sleep(2000);// simulate a network call
			} catch (InterruptedException e) {
				e.printStackTrace();
			}

			// 根据mMessageString，自动对应一个单词
			mRobolicWord = Utility.getChainWord(getApplicationContext(),
					mMessageString);
			return mRobolicWord;
		}

		@Override
		public void onProgressUpdate(String... v) {
			if (messages.get(messages.size() - 1).isStatusMessage) {
				messages.get(messages.size() - 1).setMessage(v[0]);
				adapter.notifyDataSetChanged();
				getListView().setSelection(messages.size() - 1);
			} else {
				// add new message, if there is no existing status message
				addNewMessage(new Message(true, v[0]));
			}
		}

		@Override
		protected void onPostExecute(String text) {
			robolicsRunning = false;

			if (messages.get(messages.size() - 1).isStatusMessage) {
				messages.remove(messages.size() - 1);
			}

			// 已经黔驴技穷了
			if (TextUtils.isEmpty(text)) {
				messages.add(new Message(true, getString(R.string.game_over)));
				adapter.notifyDataSetChanged();
				getListView().setSelection(messages.size() - 1);
				// 弹出游戏结束提示，并给出是否上传战绩选项
				confirmGameOver(MainActivity.this);
				return;
			}
			// add the orignal message from server.
			Message msg = new Message(text, false);
			msg.setTailSpan();
			addNewMessage(msg);
		}
	}

	// 弹出游戏结束提示，并给出是否上传战绩选项
	private void confirmGameOver(final Context context) {
		// 测试语音播报
		BNTTSPlayer.playTTSText(getString(R.string.you_win_tts_string), -1);

		LayoutInflater factory = LayoutInflater.from(this);
		final View textEntryView = factory.inflate(
				R.layout.alert_dialog_text_entry, null);
		String playerName = Config.sharedInstance(context).getString(
				AppConstants.PLAY_NAME_KEY);
		final EditText nameEdit = (EditText) textEntryView
				.findViewById(R.id.playerName_edittext);
		nameEdit.setText(playerName);

		int tmp = Config.sharedInstance(context)
				.getInt(AppConstants.POINTS_KEY);
		if (tmp < 0) {
			tmp = 0;
		}
		final int points = tmp;
		EditText pointsEditText = (EditText) textEntryView
				.findViewById(R.id.points_edittext);
		pointsEditText.setText(String.valueOf(points));
		new AlertDialog.Builder(context).setTitle(R.string.over)
				.setView(textEntryView).setMessage(R.string.upload_record)
				.setPositiveButton(R.string.ok, new OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						String playerName = nameEdit.getText().toString();
						Config.sharedInstance(context).putString(
								AppConstants.PLAY_NAME_KEY, playerName);
						syncServer(playerName, String.valueOf(points));
					}
				}).setNegativeButton(R.string.cancel, new OnClickListener() {

					@Override
					public void onClick(DialogInterface dialog, int which) {
						restart();
					}
				}).show();
	}

	void addNewMessage(Message m) {
		messages.add(m);
		adapter.notifyDataSetChanged();
		getListView().setSelection(messages.size() - 1);
	}

	private void syncServer(String playerName, String points) {
		if (TextUtils.isEmpty(playerName)) {
			Toast.makeText(getApplicationContext(),
					getString(R.string.empty_player_name), Toast.LENGTH_LONG)
					.show();
			return;
		}
		AddTopListRunner runner = new AddTopListRunner(getApplicationContext(),
				Utils.getAppVersion(getApplicationContext()), playerName,
				points);
		runner.setTaskListener(new TaskListener() {

			@Override
			public void onSuccess(TaskResponse response) {
				restart();
				// TODO 上传结果处理
				if (!(response instanceof ParsedTaskReponse)) {
					return;
				}
				ParsedTaskReponse ret = (ParsedTaskReponse) response;
				if (!(ret.parsedObject instanceof Boolean)) {
					return;
				}
			}

			@Override
			public void onFailure(int arg0, String arg1) {
				Toast.makeText(getApplicationContext(),
						getString(R.string.fail_to_connect_network),
						Toast.LENGTH_LONG).show();
				restart();
			}
		});
		runner.run();
	}

	// 再来一局
	private void restart() {
		new AlertDialog.Builder(this).setTitle(R.string.app_name)
				.setMessage(R.string.renew_game)
				.setNegativeButton(R.string.cancel, new OnClickListener() {

					@Override
					public void onClick(DialogInterface dialog, int which) {
						messages.clear();
						adapter.notifyDataSetChanged();
					}
				}).setPositiveButton(R.string.ok, new OnClickListener() {

					@Override
					public void onClick(DialogInterface dialog, int which) {
						messages.clear();
						mRobolicWord = Utility
								.getNewWord(getApplicationContext());

						// 自动挑选一个单词作为单词接龙的第一个单词
						Message msg = new Message(mRobolicWord, false);
						msg.setTailSpan();
						messages.add(msg);
						messages.add(new Message(true,
								getString(R.string.your_turn)));
						adapter.notifyDataSetChanged();
					}
				}).show();

	}

	private String getSdcardDir() {
		if (Environment.getExternalStorageState().equalsIgnoreCase(
				Environment.MEDIA_MOUNTED)) {
			return Environment.getExternalStorageDirectory().toString();
		}
		return null;
	}

	public void onBackPressed() {
		new AlertDialog.Builder(this)
				.setTitle(R.string.confirm_to_quit_app)
				.setIcon(android.R.drawable.ic_dialog_info)
				.setPositiveButton(R.string.ok,
						new DialogInterface.OnClickListener() {

							@Override
							public void onClick(DialogInterface dialog,
									int which) {
								// 点击“确认”后的操作
								MainActivity.this.finish();
							}
						})
				.setNegativeButton(R.string.cancel,
						new DialogInterface.OnClickListener() {

							@Override
							public void onClick(DialogInterface dialog,
									int which) {
								// 点击“返回”后的操作,这里不设置没有任何操作
							}
						}).show();
	}

	@Override
	protected void onResume() {
		super.onResume();

		// 检查新版本
		UpdateManager.getIntance().checkNewVersion(this,"");
	}
}