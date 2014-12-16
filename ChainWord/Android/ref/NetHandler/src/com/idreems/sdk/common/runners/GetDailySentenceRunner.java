package com.idreems.sdk.common.runners;

import android.content.Context;

import com.idreems.sdk.NetHandler.AnalyticsAgent;
import com.idreems.sdk.NetHandler.AnalyticsAgentImp;
import com.idreems.sdk.netmodel.GetDailySentenceResp;
import com.idreems.sdk.netmodel.ParsedTaskReponse;
import com.idreems.sdk.protocols.ProtocolConst;
import com.idreems.sdk.protocols.imp.GetDailySentenceProtocol;
import com.yees.sdk.lightvolley.HttpTaskResponse;
import com.yees.sdk.lightvolley.TaskListener;
import com.yees.sdk.lightvolley.TaskResponse;
import com.yees.sdk.utils.Logger;

/**
 * 获取排行榜的runner
 * 
 * @author ramonqlee
 * 
 */
public class GetDailySentenceRunner implements Runnable {
	Context context;
	private TaskListener listener;
	private String versionString;

	public GetDailySentenceRunner(Context context, String version) {
		this.context = context;
		versionString = version;
	}

	public void setTaskListener(TaskListener listener) {
		this.listener = listener;
	}

	@Override
	public void run() {
		final GetDailySentenceProtocol protocol = new GetDailySentenceProtocol(
				context, versionString);

		AnalyticsAgent agent = AnalyticsAgentImp.sharedInstance(context,
				protocol.getUrl());
		if (true) {
			String log = "url: " + protocol.getUrl();
			Logger.d(ProtocolConst.NET_LOG_TAG, log);
		}

		agent.getBinary(new TaskListener() {
			@Override
			public void onSuccess(TaskResponse response) {
				if (response instanceof HttpTaskResponse) {
					byte[] responseBytes = ((HttpTaskResponse) response).data;
					String r = new String(responseBytes);
					if (true) {
						String log = "result: " + r;
						Logger.d(ProtocolConst.NET_LOG_TAG, log);
					}
					GetDailySentenceResp ret = (GetDailySentenceResp) protocol
							.handleResponse(responseBytes);
					if (null != listener) {
						listener.onSuccess(new ParsedTaskReponse(ret));
					}
				}
			}

			@Override
			public void onFailure(int errorCode, String message) {
				String log = message + "errorCode " + errorCode;
				Logger.d(ProtocolConst.NET_LOG_TAG, log);

				if (null != listener) {
					listener.onFailure(errorCode, message);
				}
			}
		});
	}

}
