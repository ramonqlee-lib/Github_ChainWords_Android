package com.idreems.sdk.netmodel;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONObject;

public class GetToplistResp {
	private List<TopListItem> itemList = new ArrayList<TopListItem>();
	
	public GetToplistResp(byte[] data)
	{
		if (null == data || 0 == data.length) {
			return;
		}
		
		//  解析数据
		try {
			JSONArray array = new JSONArray(new String(data));
			for (int i = 0; i < array.length(); i++) {
				JSONObject tmp = array.getJSONObject(i);
				if (null == tmp) {
					continue;
				}
				itemList.add(new TopListItem(tmp));
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public List<TopListItem> getItemList() {
		return itemList;
	}
}
