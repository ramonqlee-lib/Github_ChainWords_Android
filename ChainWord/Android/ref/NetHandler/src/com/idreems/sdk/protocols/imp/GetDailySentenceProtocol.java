package com.idreems.sdk.protocols.imp;

import android.content.Context;

import com.idreems.sdk.netmodel.GetDailySentenceResp;
import com.idreems.sdk.protocols.Protocol;

/**
 * 获取排行榜
 * 
 * @author ramonqlee
 * 
 */
public class GetDailySentenceProtocol implements Protocol {
	private static final String PROTOCOL_URL = "http://open.iciba.com/dsapi/";
	private Context context;
	private String versionString;

	// token，验签值，任务id
	public GetDailySentenceProtocol(Context context, String version) {
		this.context = context;
		versionString = version;
	}

	@Override
	public byte[] getBytes() {
		return "".getBytes();
	}

	@Override
	public Object handleResponse(byte[] data) {
		return new GetDailySentenceResp(data);
	}

	@Override
	public String getUrl() {
		return PROTOCOL_URL;
	}

}
