package com.idreems.sdk.common.runners;

import android.content.Context;

import com.idreems.sdk.NetHandler.AnalyticsAgent;
import com.idreems.sdk.NetHandler.AnalyticsAgentImp;
import com.idreems.sdk.netmodel.ParsedTaskReponse;
import com.idreems.sdk.protocols.ProtocolConst;
import com.idreems.sdk.protocols.imp.AddTopListProtocol;
import com.yees.sdk.lightvolley.HttpTaskResponse;
import com.yees.sdk.lightvolley.TaskListener;
import com.yees.sdk.lightvolley.TaskResponse;
import com.yees.sdk.utils.Logger;

/**
 * 加入排行榜的runner
 * 
 * @author ramonqlee
 * 
 */
public class AddTopListRunner implements Runnable {
	Context context;
	private TaskListener listener;
	private String versionString;
	private String playerName;
	private String points;
	
	public AddTopListRunner(Context context, String version,String playerName,String points) {
		this.context = context;
		this.versionString = version;
		this.playerName = playerName;
		this.points = points;
	}

	public void setTaskListener(TaskListener listener) {
		this.listener = listener;
	}

	@Override
	public void run() {
		final AddTopListProtocol protocol = new AddTopListProtocol(
				context, versionString,playerName,points);

		AnalyticsAgent agent = AnalyticsAgentImp.sharedInstance(context,
				protocol.getUrl());
		if (true) {
			String log = "url: " + protocol.getUrl();
			Logger.d(ProtocolConst.NET_LOG_TAG, log);
		}

		agent.postBinary(protocol.getBytes(),new TaskListener() {
			@Override
			public void onSuccess(TaskResponse response) {
				if (response instanceof HttpTaskResponse) {
					byte[] responseBytes = ((HttpTaskResponse) response).data;
					String r = new String(responseBytes);
					if (true) {
						String log = "result: " + r;
						Logger.d(ProtocolConst.NET_LOG_TAG, log);
					}

					Boolean ret = (Boolean) protocol
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
