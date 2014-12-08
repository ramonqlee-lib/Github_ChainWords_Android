package com.dream2reality.chainwords;

import java.util.List;

import org.json.JSONArray;
import org.json.JSONObject;

import android.app.Activity;
import android.content.Context;
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
import com.idreems.sdk.protocols.ProtocolUtils;
import com.idreems.update.DownLoadManager;
import com.idreems.update.DownLoadManager.OnDownLoadResultListener;
import com.yees.sdk.lightvolley.TaskListener;
import com.yees.sdk.lightvolley.TaskResponse;
import com.yees.sdk.utils.Config;
import com.yees.sdk.utils.Logger;
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

		// 初始化数据
		TextView tView = (TextView) findViewById(R.id.title_textview);
		tView.setText(R.string.vocabulary_title_string);
		String[] data = { getString(R.string.vocabulary_default_item) };
		populateSelectableListView(data);
		// TODO 显示下载列表
		// 同步服务器端数据,并进行数据设置
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

					// 已经下载过了
					if (!TextUtils.isEmpty(getDownloadedFile(
							getApplicationContext(), item.url))) {
						continue;
					}

					data[i] = item.levelString;
				}
				populateSelectableListView(data);
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

	private void populateDownloadedListView(final String[] data) {
		// 初始化数据
		ListView listView = (ListView) findViewById(R.id.download_listview);
		listView.setItemsCanFocus(true);
		listView.setChoiceMode(ListView.CHOICE_MODE_SINGLE);
		// 构造一个数组对象，也就是数据
		listView.setAdapter(new ArrayAdapter<String>(this,
				android.R.layout.simple_list_item_single_choice, data));
		listView.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1,
					int position, long row) {
				// TODO 设定使用的词汇表
			}
		});
	}

	private void populateSelectableListView(final String[] data) {
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
				final DownLoadManager dowApkUpdateManager = new DownLoadManager(
						getApplicationContext(), item.url, "",
						item.levelString, "数据下载中...");
				dowApkUpdateManager
						.setOnDownLoadResultListener(new OnDownLoadResultListener() {

							@Override
							public void onSuccess(String filePath) {
								// TODO 下载完毕，保存到已下载列表中，供选择使用(url 作为key)
								// 并且刷新已经下载的列表
								Logger.d(AppConstants.LOG_TAG, filePath
										+ " 下载完毕");
								Logger.d(AppConstants.LOG_TAG, "Url "
										+ dowApkUpdateManager.getUrl());
								add2DownloadedConfig(getApplicationContext(),
										dowApkUpdateManager.getUrl(), filePath);
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

	/**
	 * 添加到已经下载列表
	 * 
	 * @param url
	 * @param fileName
	 */
	private static void add2DownloadedConfig(Context context, String url,
			String fileName) {
		JSONArray array = getDownloaedFiles(context);
		if (null == array) {
			array = new JSONArray();
		}
		try {
			JSONObject obj = new JSONObject();
			obj.put(url, fileName);
			array.put(obj);
			Config.sharedInstance(context).putString(
					AppConstants.VOCABULARY_DOWNLOADED_ITEM_NAME_KEY,
					array.toString());
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private static String getDownloadedFile(Context context, String url) {
		// 检查文件已经在下载的列表中，并且文件确实有效
		JSONArray array = getDownloaedFiles(context);
		if (TextUtils.isEmpty(url) || null == array) {
			return "";
		}
		try {
			for (int i = 0; i < array.length(); i++) {
				JSONObject obj = array.getJSONObject(i);
				String fileName = ProtocolUtils.getJsonString(obj, url);
				if (!TextUtils.isEmpty(fileName) && Utils.fileExists(fileName)) {
					return fileName;
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return "";
	}

	private static JSONArray getDownloaedFiles(Context context) {
		// TODO 文件下载的列表
		String str = Config.sharedInstance(context).getString(
				AppConstants.VOCABULARY_DOWNLOADED_ITEM_NAME_KEY);
		if (TextUtils.isEmpty(str)) {
			return null;
		}
		try {
			JSONArray ret = new JSONArray(str);
			return ret;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

}
