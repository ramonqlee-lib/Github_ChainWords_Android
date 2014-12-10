package com.idreems.sdk.common.runners;

import android.content.Context;

import com.idreems.sdk.NetHandler.AnalyticsAgent;
import com.idreems.sdk.NetHandler.AnalyticsAgentImp;
import com.idreems.sdk.netmodel.ParsedTaskReponse;
import com.idreems.sdk.netmodel.UpdateModel;
import com.idreems.sdk.protocols.ProtocolConst;
import com.idreems.sdk.protocols.imp.UpdateProtocol;
import com.yees.sdk.lightvolley.HttpTaskResponse;
import com.yees.sdk.lightvolley.TaskListener;
import com.yees.sdk.lightvolley.TaskResponse;
import com.yees.sdk.utils.Logger;

public class CheckUpdateRunner extends BaseRunner implements Runnable {
	private static final String TAG = ProtocolConst.NET_LOG_TAG;

	String urlString;
	Context context;

	public CheckUpdateRunner(Context context, String url) {
		this.context = context;
		urlString = url;
	}

	@Override
	public void run() {
		final UpdateProtocol protocol = new UpdateProtocol(context, urlString);

		AnalyticsAgent agent = AnalyticsAgentImp.sharedInstance(context,
				protocol.getUrl());
		if (true) {
			String log = "url: " + protocol.getUrl();
			Logger.d(TAG, log);
			Logger.p(log);
		}

		agent.getBinary(new TaskListener() {
			@Override
			public void onSuccess(TaskResponse response) {
				if (response instanceof HttpTaskResponse) {
					byte[] responseBytes = ((HttpTaskResponse) response).data;
					String r = new String(responseBytes);
					if (true) {
						String log = "result: " + r;
						Logger.d(TAG, log);
						Logger.p(log);
					}
					UpdateModel tmp = (UpdateModel) protocol
							.handleResponse(responseBytes);
					if (null != listener) {
						listener.onSuccess(new ParsedTaskReponse(tmp));
					}
				}
			}

			@Override
			public void onFailure(int errorCode, String message) {
				if (null != listener) {
					listener.onFailure(errorCode, message);
				}
				if (true) {
					String log = message + "errorCode " + errorCode;
					Logger.d(TAG, log);
					Logger.p(log);
				}
			}
		});
	}
}
