package com.dream2reality.chainwords;

import java.util.ArrayList;
import java.util.List;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.color.speechbubble.Utility;
import com.dream2reality.utils.AppConstants;
import com.dream2reality.wordsmanager.DownloadedFileConfig;
import com.dream2reality.wordsmanager.WordsRepository;
import com.idreems.sdk.common.runners.GetVocabularyRunner;
import com.idreems.sdk.netmodel.GetVocabularyResp;
import com.idreems.sdk.netmodel.ParsedTaskReponse;
import com.idreems.sdk.netmodel.VocabItem;
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

public class VocabularyActivity extends CustomBaseActivity {
	List<VocabItem> vocabItemList;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentViewWithCustomTitle(R.layout.layout_vocabulary_activity);

		// 初始化数据
		TextView tView = (TextView) findViewById(R.id.title_textview);
		tView.setText(R.string.vocabulary_title_string);
		// 显示可选择设置的列表
		populateSelectableListView(DownloadedFileConfig
				.getDisplayableNameArray(getApplicationContext()));
		// 同步服务器端数据,并进行数据设置
		syncServer();
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
				populateDownloadableListView(vocabItemList);
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

	/**
	 * 填充可设置列表
	 * 
	 * @param data
	 */
	private void populateSelectableListView(final String[] data) {
		// 初始化数据
		final ListView listView = (ListView) findViewById(R.id.selectable_listview);
		listView.setItemsCanFocus(true);
		listView.setChoiceMode(ListView.CHOICE_MODE_SINGLE);
		// 构造一个数组对象，也就是数据
		listView.setAdapter(new ArrayAdapter<String>(this,
				android.R.layout.simple_list_item_single_choice, data));

		// 确认当前使用的项目
		runOnUiThread(new Runnable() {
			
			@Override
			public void run() {
				String currentSelectedFileUrl = Config.sharedInstance(
						getApplicationContext()).getString(
								AppConstants.VOCABULARY_SELECTED_ITEM_URL_KEY);
				if (!TextUtils.isEmpty(currentSelectedFileUrl)) {
					String displayName = DownloadedFileConfig.getDisplayNameFromUrl(
							getApplicationContext(), currentSelectedFileUrl);
					for (int i = 0; i < data.length; i++) {
						if (TextUtils.equals(data[i], displayName)) {
							listView.setItemChecked(i, true);
							break;
						}
					}
				}
			}
		});
		listView.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1,
					int position, long row) {
				// 设定使用的词汇表
				String displayName = data[position];
				final String url = DownloadedFileConfig.getUrlFromDiplayName(
						getApplicationContext(), displayName);
				Config.sharedInstance(getApplicationContext()).putString(
						AppConstants.VOCABULARY_SELECTED_ITEM_URL_KEY, url);

				new Thread(new Runnable() {

					@Override
					public void run() {
						String fileName = DownloadedFileConfig
								.getFileNameFromUrl(getApplicationContext(),
										url);
						Config.sharedInstance(getApplicationContext())
								.putString(Utility.FILE_NAME_KEY, fileName);
						// 更新词汇表
						WordsRepository.sharedInstance(getApplicationContext(),
								fileName);
						Logger.d(AppConstants.LOG_TAG, "select and update with fileName: " + fileName);
					}
				}).start();

				Toast.makeText(getApplicationContext(), "选定 " + displayName,
						Toast.LENGTH_SHORT).show();
				Logger.d(AppConstants.LOG_TAG, "selected url: " + url);
			}
		});
	}

	/**
	 * 填充可下载列表
	 * 
	 * @param itemList
	 */
	private void populateDownloadableListView(final List<VocabItem> itemList) {

		if (null == itemList || itemList.isEmpty()) {
			return;
		}
		List<String> selectableList = new ArrayList<String>();
		for (int i = 0; i < itemList.size(); ++i) {
			VocabItem item = itemList.get(i);
			if (null == item || TextUtils.isEmpty(item.levelString)
					|| TextUtils.isEmpty(item.url)) {
				continue;
			}

			// 已经下载过了
			if (!TextUtils.isEmpty(DownloadedFileConfig.getFileNameFromUrl(
					getApplicationContext(), item.url))) {
				continue;
			}

			selectableList.add(item.levelString);
		}
		if (selectableList.isEmpty()) {
			populateDownloadableListView(new String[0]);
			return;
		}

		String[] data = new String[selectableList.size()];
		for (int i = 0; i < selectableList.size(); i++) {
			data[i] = selectableList.get(i);
		}
		populateDownloadableListView(data);
	}

	/**
	 * 填充可下载列表
	 * 
	 * @param data
	 */
	private void populateDownloadableListView(final String[] data) {
		// 初始化数据
		ListView listView = (ListView) findViewById(R.id.downloadable_listview);
		listView.setItemsCanFocus(true);
		listView.setChoiceMode(ListView.CHOICE_MODE_SINGLE);
		// 构造一个数组对象，也就是数据
		listView.setAdapter(new ArrayAdapter<String>(this,
				android.R.layout.simple_list_item_single_choice, data));
		listView.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1,
					int position, long row) {
				String displayName = data[position];
				VocabItem item = null;
				for (int i = 0; i < vocabItemList.size(); i++) {
					VocabItem tmp = vocabItemList.get(i);
					if (null == tmp) {
						continue;
					}
					if (TextUtils.equals(displayName, tmp.levelString)) {
						item = tmp;
						break;
					}
				}

				if (null == item) {
					return;
				}

				// 开始下载文件
				final DownLoadManager dowApkUpdateManager = new DownLoadManager(
						getApplicationContext(), item.url, item.md5,
						item.levelString, "数据下载中...");
				dowApkUpdateManager
						.setOnDownLoadResultListener(new OnDownLoadResultListener() {

							@Override
							public void onSuccess(String filePath) {
								// 下载完毕，保存到已下载列表中，供选择使用(url 作为key)
								// 并且刷新已经下载的列表
								Logger.d(AppConstants.LOG_TAG, filePath
										+ " 下载完毕");
								Logger.d(AppConstants.LOG_TAG, "Url "
										+ dowApkUpdateManager.getUrl());
								for (int i = 0; i < vocabItemList.size(); i++) {
									VocabItem item = vocabItemList.get(i);
									if (null == item) {
										continue;
									}
									if (TextUtils.equals(item.url,
											dowApkUpdateManager.getUrl())) {
										DownloadedFileConfig.add(
												getApplicationContext(),
												dowApkUpdateManager.getUrl(),
												filePath, item.levelString);

										// 刷新已经下载的列表
										populateSelectableListView(DownloadedFileConfig
												.getDisplayableNameArray(getApplicationContext()));
										// 刷新待下载列表
										populateDownloadableListView(vocabItemList);
										return;
									}
								}
							}

							// md5校验失败
							@Override
							public void onFailed(String filePath) {
								Utils.deleteFile(filePath);
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
