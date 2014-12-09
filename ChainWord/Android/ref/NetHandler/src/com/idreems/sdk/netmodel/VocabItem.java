package com.idreems.sdk.netmodel;

import org.json.JSONObject;

import com.idreems.sdk.protocols.ProtocolUtils;

public class VocabItem {
	public String levelString;
	public String url;
	public String md5;
	
	public VocabItem(JSONObject value)
	{
		levelString = ProtocolUtils.getJsonString(value, "level");
		url = ProtocolUtils.getJsonString(value, "url");
		md5 = ProtocolUtils.getJsonString(value, "md5");
	}
}
