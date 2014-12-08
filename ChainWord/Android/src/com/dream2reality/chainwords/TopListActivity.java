package com.dream2reality.chainwords;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.app.Activity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.idreems.sdk.common.runners.GetTopListRunner;
import com.idreems.sdk.netmodel.GetToplistResp;
import com.idreems.sdk.netmodel.ParsedTaskReponse;
import com.idreems.sdk.netmodel.TopListItem;
import com.yees.sdk.lightvolley.TaskListener;
import com.yees.sdk.lightvolley.TaskResponse;
import com.yees.sdk.utils.Utils;

/**
 * 排行榜
 * 
 * @author ramonqlee
 * 
 */
public class TopListActivity extends Activity {
	List<TopListItem> mTopListItemList;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_CUSTOM_TITLE);
		setContentView(R.layout.layout_toplist_activity);
		getWindow().setFeatureInt(Window.FEATURE_CUSTOM_TITLE,
				R.layout.layout_custom_titlebar);

		// 同步服务器端数据，并进行设置
		syncServer();
	}

	public void onBack(View view) {
		onBackPressed();
	}

	private void syncServer() {
		GetTopListRunner runner = new GetTopListRunner(getApplicationContext(),
				Utils.getAppVersion(getApplicationContext()));
		runner.setTaskListener(new TaskListener() {

			@Override
			public void onSuccess(TaskResponse response) {
				// 待解析后，在界面上显示
				if (!(response instanceof ParsedTaskReponse)) {
					return;
				}
				ParsedTaskReponse ret = (ParsedTaskReponse) response;
				if (!(ret.parsedObject instanceof GetToplistResp)) {
					return;
				}
				GetToplistResp resp = (GetToplistResp) ret.parsedObject;
				mTopListItemList = (List<TopListItem>) resp.getItemList();
				if (null == mTopListItemList || mTopListItemList.isEmpty()) {
					return;
				}
				List<Map<String, String>> listData = new ArrayList<Map<String, String>>();
				for (TopListItem item : mTopListItemList) {
					Map<String, String> data = new HashMap<String, String>();
					if (null == item || TextUtils.isEmpty(item.playerName)
							|| TextUtils.isEmpty(item.points)) {
						continue;
					}
					data.put("title", item.playerName);
					data.put("description", item.points);
					listData.add(data);
				}
				populateViews(listData);
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

	private void populateViews(List<Map<String, String>> data) {
		// 初始化数据
		TextView tView = (TextView) findViewById(R.id.title_textview);
		tView.setText(R.string.toplist_title_string);

		// 初始化数据
		ListView listView = (ListView) findViewById(R.id.setting_listview);

		SimpleAdapter adapter = new SimpleAdapter(this, data,
				android.R.layout.simple_list_item_2, new String[] { "title",
						"description" }, new int[] { android.R.id.text1,
						android.R.id.text2 });

		// 构造一个数组对象，也就是数据
		listView.setAdapter(adapter);
		listView.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1,
					int position, long row) {
				// TODO 待展示详细的战绩
			}
		});
	}
}
