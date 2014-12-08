package com.idreems.sdk.netmodel;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONObject;

public class GetVocabularyResp {
	private List<VocabItem> itemList = new ArrayList<VocabItem>();
	
	public GetVocabularyResp(byte[] data)
	{
		if (null == data || 0 == data.length) {
			return;
		}
		
		//  解析单词表数据
		try {
			JSONObject obj = new JSONObject(new String(data));
			if (!obj.has("data")) {
				return;
			}
			JSONArray array = obj.getJSONArray("data");
			for (int i = 0; i < array.length(); i++) {
				JSONObject tmp = array.getJSONObject(i);
				if (null == tmp) {
					continue;
				}
				itemList.add(new VocabItem(tmp));
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public List<VocabItem> getItemList() {
		return itemList;
	}
}
