package com.idreems.sdk.netmodel;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONObject;

import com.idreems.sdk.protocols.ProtocolConst;
import com.yees.sdk.utils.Logger;

public class GetVocabularyResp {
	private List<VocabItem> itemList = new ArrayList<VocabItem>();
	
	public GetVocabularyResp(byte[] data)
	{
		if (null == data || 0 == data.length) {
			return;
		}
		
		//  解析单词表数据
		try {
			String r = new String(data);
			int pos = r.indexOf('{');
			if (-1 != pos) {
				Logger.d(ProtocolConst.NET_LOG_TAG, "json start from "+pos);
				r = r.substring(pos, r.length());
			}
			
			JSONObject obj = new JSONObject(r);
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
