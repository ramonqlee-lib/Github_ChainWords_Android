package com.yees.sdk.engine;

import java.util.ArrayList;
import java.util.List;

import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;

import com.yees.sdk.netmodel.Staff;
import com.yees.sdk.utils.Constants;
import com.yees.sdk.utils.Logger;

public class StaffSeacher {
	private static StaffSeacher sStaffSeacher = new StaffSeacher();

	private List<Staff> staffList = new ArrayList<Staff>();
	private boolean running;// 是否在运行中

	private StaffSeacher() {
	}

	public static StaffSeacher newInstance() {
		return new StaffSeacher();
	}

	public static StaffSeacher sharedInstance() {
		return sStaffSeacher;
	}

	// 设置待搜索的数据
	public void setStaff(List<Staff> users) {
		if (null == users || users.isEmpty()) {
			staffList.clear();
			return;
		}
		staffList.addAll(users);
	}

	/**
	 * 请求搜索，并回调(务必在主线程中调用)
	 * 
	 * @param keyWord
	 * @param callback
	 */
	public void request(final String keyWord, final SearcherCallback callback) {
		if (null == callback) {
			Logger.d(Constants.LOG_TAG,
					"StaffSeacher return with null callback !");
			return;
		}

		// callback should be called
		if (TextUtils.isEmpty(keyWord)) {
			Logger.d(Constants.LOG_TAG, "StaffSeacher to call onGetResults !");
			callback.onGetResults(staffList);
			return;
		}

		// 如果上次操作没有完成，则忽略当前操作
		if (running) {
			Logger.d(Constants.LOG_TAG,
					"StaffSeacher is working,ignore current request!");
			return;
		}
		running = true;
		// 从设置的数据中进行搜索
		new Thread(new Runnable() {

			@Override
			public void run() {
				final List<Staff> ret = new ArrayList<Staff>();
				for (Staff user : staffList) {
					if (null == user || !user.onFiltered(keyWord)) {
						continue;
					}
					ret.add(user);
				}

				new Handler(Looper.getMainLooper()).post(new Runnable() {

					@Override
					public void run() {
						Logger.d(Constants.LOG_TAG,
								"StaffSeacher to call onGetResults !");
						callback.onGetResults(ret);
						running = false;
					}
				});
			}
		}).start();
	}
}
