package com.dream2reality.chainwords;

import java.util.List;

import android.app.Activity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.dream2reality.constants.AppConstants;
import com.idreems.sdk.common.runners.GetVocabularyRunner;
import com.idreems.sdk.netmodel.GetVocabularyResp;
import com.idreems.sdk.netmodel.ParsedTaskReponse;
import com.idreems.sdk.netmodel.VocabItem;
import com.idreems.update.DownLoadManager;
import com.idreems.update.DownLoadManager.OnDownLoadResultListener;
import com.yees.sdk.lightvolley.TaskListener;
import com.yees.sdk.lightvolley.TaskResponse;
import com.yees.sdk.utils.Config;
import com.yees.sdk.utils.Utils;

/*
 *  词汇表选择界面
 *  1. 进入后自动同步后台数据
 *  2. 同步完成后，单选设置
 */

public class VocabularyActivity extends Activity {
	List<VocabItem> vocabItemList;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_CUSTOM_TITLE);
		setContentView(R.layout.layout_vocabulary_activity);

		getWindow().setFeatureInt(Window.FEATURE_CUSTOM_TITLE,
				R.layout.layout_custom_titlebar);

		String[] data = { getString(R.string.vocabulary_default_item) };
		populateViews(data);

		//  同步服务器端数据,并进行数据设置
		syncServer();
	}

	public void onBack(View view) {
		onBackPressed();
	}

	private void syncServer() {
		GetVocabularyRunner runner = new GetVocabularyRunner(
				getApplicationContext(),
				Utils.getAppVersion(getApplicationContext()));
		runner.setTaskListener(new TaskListener() {

			@Override
			public void onSuccess(TaskResponse response) {
				// 待解析后，在界面上显示
				if (!(response instanceof ParsedTaskReponse)) {
					return;
				}
				ParsedTaskReponse ret = (ParsedTaskReponse) response;
				if (!(ret.parsedObject instanceof GetVocabularyResp)) {
					return;
				}

				GetVocabularyResp resp = (GetVocabularyResp) ret.parsedObject;
				vocabItemList = resp.getItemList();
				if (null == vocabItemList || vocabItemList.isEmpty()) {
					return;
				}
				String[] data = new String[vocabItemList.size()];
				for (int i = 0; i < vocabItemList.size(); ++i) {
					VocabItem item = vocabItemList.get(i);
					if (null == item || TextUtils.isEmpty(item.levelString)
							|| TextUtils.isEmpty(item.url)) {
						continue;
					}
					data[i] = item.levelString;
				}
				populateViews(data);
			}

			@Override
			public void onFailure(int arg0, String arg1) {
				Toast.makeText(getApplicationContext(),
						getString(R.string.fail_to_connect_network),
						Toast.LENGTH_LONG).show();
			}
		});
		runner.run();
	}

	private void populateViews(final String[] data) {
		// 初始化数据
		TextView tView = (TextView) findViewById(R.id.title_textview);
		tView.setText(R.string.vocabulary_title_string);

		// 初始化数据
		ListView listView = (ListView) findViewById(R.id.setting_listview);
		listView.setItemsCanFocus(true);
		listView.setChoiceMode(ListView.CHOICE_MODE_SINGLE);
		// 构造一个数组对象，也就是数据
		listView.setAdapter(new ArrayAdapter<String>(this,
				android.R.layout.simple_list_item_single_choice, data));
		listView.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1,
					int position, long row) {
				VocabItem item = vocabItemList.get(position);
				if (AppConstants.DEBUG_MODE) {
					Toast.makeText(getApplicationContext(),
							"选择了 " + item.levelString, Toast.LENGTH_SHORT)
							.show();
				}

				// 保存词汇表的名字
				Config.sharedInstance(getApplicationContext()).putString(
						AppConstants.VOCABULARY_SELECTED_ITEM_NAME_KEY,
						item.levelString);

				// 开始下载文件
				DownLoadManager dowApkUpdateManager = new DownLoadManager(
						getApplicationContext(), item.url, "",
						item.levelString, "数据下载中...");
				dowApkUpdateManager
						.setOnDownLoadResultListener(new OnDownLoadResultListener() {

							@Override
							public void onSuccess(String filePath) {

							}

							// md5校验失败
							@Override
							public void onFailed(String filePath) {

							}

							// 下载失败
							@Override
							public void onDownloadError(String filePath) {
								Toast.makeText(getApplicationContext(),
										"下载失败，已自动重新下载!", Toast.LENGTH_SHORT)
										.show();
							}
						});
				dowApkUpdateManager.execute();
			}
		});
	}
}
