package com.idreems.sdk.netmodel;

import org.json.JSONObject;

import com.idreems.sdk.protocols.ProtocolUtils;

public class TopListItem {
	public String playerName;
	public String points;
	
	public TopListItem(JSONObject value)
	{
		playerName = ProtocolUtils.getJsonString(value, "playerName");
		points = ProtocolUtils.getJsonString(value, "points");
	}
}
