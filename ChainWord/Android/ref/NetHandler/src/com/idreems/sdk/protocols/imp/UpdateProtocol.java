package com.idreems.sdk.protocols.imp;

import java.util.HashMap;
import java.util.Map;

import android.content.Context;

import com.idreems.sdk.netmodel.UpdateModel;
import com.idreems.sdk.protocols.Protocol;
import com.idreems.sdk.protocols.ProtocolUtils;

/**
 * apk更新协议
 * 
 * @author ramonqlee
 * 
 */
public class UpdateProtocol implements Protocol {
	private Context context;
	private String padId;
	private String urlString;
	/**
	 * 
	 * @param padSerial
	 *            pad编号，注册后服务器端分配
	 */
	public UpdateProtocol(Context context,String serverUrl) {
		this.context = context;
		urlString = serverUrl;
	}

	@Override
	public byte[] getBytes() {
		Map<String, String> map = new HashMap<String, String>();
		return ProtocolUtils.sign(map, ProtocolUtils.getMac(context),
				ProtocolUtils.SALT);
	}

	@Override
	public Object handleResponse(byte[] data) {
		return new UpdateModel(data);
	}

	@Override
	public String getUrl() {
		return ProtocolUtils.addExtraHeaders(context, urlString);
	}

}
