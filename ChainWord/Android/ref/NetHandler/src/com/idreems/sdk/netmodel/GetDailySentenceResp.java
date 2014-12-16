package com.idreems.sdk.netmodel;

import org.json.JSONObject;

import com.idreems.sdk.protocols.ProtocolUtils;

public class GetDailySentenceResp {
	public String shareImageUrl;
	
	public GetDailySentenceResp(byte[] data)
	{
		if (null == data || 0 == data.length) {
			return;
		}
		
		//  解析数据
		try {
			JSONObject obj = new JSONObject(new String(data));
			shareImageUrl = ProtocolUtils.getJsonString(obj, "fenxiang_img");
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
