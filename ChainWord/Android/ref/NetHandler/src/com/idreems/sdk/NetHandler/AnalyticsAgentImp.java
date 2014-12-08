package com.idreems.sdk.NetHandler;

import java.util.HashMap;
import java.util.Map;

import android.content.Context;
import android.util.Log;

import com.yees.sdk.lightvolley.EasyX509TrustManager;
import com.yees.sdk.lightvolley.HttpGetDataSourceImp;
import com.yees.sdk.lightvolley.HttpPostDataSource;
import com.yees.sdk.lightvolley.HttpPostDataSourceImp;
import com.yees.sdk.lightvolley.HttpTask;
import com.yees.sdk.lightvolley.LightVolley;
import com.yees.sdk.lightvolley.TaskListener;
import com.yees.sdk.lightvolley.TaskQueue;
import com.yees.sdk.lightvolley.TaskResponse;
import com.yees.sdk.utils.Constants;

public class AnalyticsAgentImp implements AnalyticsAgent {
	private String _urlString;
	private static Context sAppContext;
	private static AnalyticsAgentImp sAgent = new AnalyticsAgentImp();
	private static TaskQueue sTaskQueue = LightVolley.newTaskQueue();

	public static AnalyticsAgent sharedInstance(Context context, String url) {
		if (context != null) {
			sAppContext = context.getApplicationContext();
			EasyX509TrustManager.setHttpsBKS(sAppContext, getBksFileName(),getPassword());
		}
		sAgent._urlString = url;
		return sAgent;
	}

	/**
	 * 根据当前环境，获取证书文件名
	 * 
	 * @return
	 */
	private static String getBksFileName() {
		return Constants.isServerDebugEnv() ? "tclient_d.bks" : "tclient.bks";
	}

	/**
	 * 根据当前环境，获取证书文件密码
	 * 
	 * @return
	 */
	private static String getPassword() {
		return Constants.isServerDebugEnv() ? "123456789" : "123456";
	}

	@Override
	public void startSession(Context context) {
		// 后续支持

	}

	@Override
	public void endSession(Context context) {
		// 后续支持

	}

	public void postBinary(byte[] binary, TaskListener listener) {
		post(binary, listener);
	}

	@Override
	public void logEvent(String key, String value) {
		Map<String, String> logs = new HashMap<String, String>();
		logs.put(key, value);
		logEvent(logs);
	}

	@Override
	public void logEvent(Map<String, String> events) {
		// 上传数据到服务器
		// ::整理成服务器所需的格式，上传
		// 整理成json格式
		// zip+encrypt
		String body = "";
		post(body.getBytes(), null);
	}

	public void post(HttpPostDataSource dataSource, final TaskListener listener) {
		sTaskQueue.add(new HttpTask(sAppContext, dataSource,
				new TaskListener() {
					@Override
					public void onSuccess(TaskResponse reponse) {
						releaseTaskQueue();
						if (listener != null) {
							listener.onSuccess(reponse);
						}
					}

					@Override
					public void onFailure(int errorCode, String message) {
						// ::失败后，每隔一定时间重试一下，共计重试设定的次数
						// 失败时，考虑重试或者缓存到本地，待下次一起上传（暂不支持）
						releaseTaskQueue();
						if (listener != null) {
							listener.onFailure(errorCode, message);
						}
					}
				}));
		if (true) {
			Log.d(Constants.LOG_TAG,
					"TaskQueue on post size = " + sTaskQueue.size());
		}
	}

	public void getBinary(final TaskListener listener) {
		HttpGetDataSourceImp dataSource = new HttpGetDataSourceImp(
				_urlString);
		sTaskQueue.add(new HttpTask(sAppContext, dataSource,
				new TaskListener() {
					@Override
					public void onSuccess(TaskResponse reponse) {
						releaseTaskQueue();
						if (listener != null) {
							listener.onSuccess(reponse);
						}
					}

					@Override
					public void onFailure(int errorCode, String message) {
						// ::失败后，每隔一定时间重试一下，共计重试设定的次数
						// 失败时，考虑重试或者缓存到本地，待下次一起上传（暂不支持）
						releaseTaskQueue();
						if (listener != null) {
							listener.onFailure(errorCode, message);
						}
					}
				}));
		if (true) {
			Log.d(Constants.LOG_TAG,
					"TaskQueue on post size = " + sTaskQueue.size());
		}
	}

	private void post(byte[] body, final TaskListener listener) {
		// 上传数据到服务器
		HttpPostDataSourceImp dataSourceAgent = new HttpPostDataSourceImp(
				_urlString);
		dataSourceAgent.setBody(body);

		post(dataSourceAgent, listener);
	}

	private void releaseTaskQueue() {
		if (true) {
			Log.d(Constants.LOG_TAG, "TaskQueue on releaseTaskQueue size = "
					+ sTaskQueue.size());
		}
		if (sTaskQueue != null && sTaskQueue.size() == 0) {
			sTaskQueue.stop();
		}
	}

}
