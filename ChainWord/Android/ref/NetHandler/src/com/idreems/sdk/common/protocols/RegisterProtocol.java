/**
 * 
 */
package com.idreems.sdk.common.protocols;

import java.util.HashMap;
import java.util.Map;

import org.json.JSONObject;

import android.content.Context;

import com.idreems.sdk.protocols.Protocol;
import com.idreems.sdk.protocols.ProtocolUtils;
import com.yees.sdk.utils.Constants;

/**
 * pad注册协议
 */
public class RegisterProtocol implements Protocol {
	private static final String PROTOCOL_URL = "/send_pad";

	private static final String PAD_ID_KEY = "pad_id";
	private static final int INVALID_PAD_ID = -1;
	private String macAddress;
	private int clientType;
	private Context context;

	/**
	 * 
	 * @param macAddress
	 *            本机mac地址
	 * @param clientType
	 *            当前客户端的类型
	 */
	public RegisterProtocol(Context context, String macAddress, int clientType) {
		this.context = context;
		this.macAddress = macAddress;
		this.clientType = clientType;
	}

	@Override
	public byte[] getBytes() {
		// post: mac=12:34:3d:2a:3d:5c&appType=1
		// String r = String.format(Locale.ENGLISH, "mac=%s&appType=%d",
		// this.macAddress, this.clientType);
		Map<String, String> map = new HashMap<String, String>();
		map.put("mac", this.macAddress);
		map.put("app_type", String.valueOf(this.clientType));
		return ProtocolUtils.sign(map, this.macAddress, ProtocolUtils.SALT);
	}

	/**
	 * 返回注册后，服务器为pad分配的编号
	 * 
	 * @ret : 非空字符串的话，则为pad的编号；否则注册失败
	 */
	@Override
	public Object handleResponse(byte[] data) {
		// 解析数据，返回响应的对象
		try {
			Object ret = ProtocolUtils.handleResponseBody(data);
			if (ret instanceof JSONObject) {
				JSONObject bodyJsonObject = (JSONObject) ret;
				if (bodyJsonObject.has(PAD_ID_KEY)) {
					return bodyJsonObject.getInt(PAD_ID_KEY);
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return INVALID_PAD_ID;
	}

	@Override
	public String getUrl() {
		if (null == this.context) {
			return "";
		}

		String url = ProtocolUtils.getProtocolUrl(context, Constants.isServerDebugEnv(),
				PROTOCOL_URL);
		return ProtocolUtils.addExtraHeaders(context, url);
	}
}
