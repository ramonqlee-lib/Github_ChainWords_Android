package com.idreems.sdk.netmodel;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONObject;

import com.idreems.sdk.protocols.ProtocolUtils;

public class GetWordMeaningResp {
	
	public static class OrgTrans {
		public String origString;
		public String transString;

		public OrgTrans(JSONObject object) {
			origString = ProtocolUtils.getJsonString(object, "orig");
			transString = ProtocolUtils.getJsonString(object, "trans");
		}
	}

	public String wordString;
	public String acceptation;
	public List<OrgTrans> sentenceList = new ArrayList<OrgTrans>();

	public GetWordMeaningResp(byte[] data) {
		if (null == data || 0 == data.length) {
			return;
		}

		//  解析数据
		try {
			JSONObject obj = new JSONObject(new String(data));
			wordString = ProtocolUtils.getJsonString(obj, "key");
			// json or json array
			acceptation = getAcceptation(obj);
			sentenceList = getSentenceList(obj);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private String getAcceptation(JSONObject object) {
		if (null == object) {
			return "";
		}
		try {
			Object ret = object.get("acceptation");
			if (ret instanceof JSONObject) {
				// TODO
			}
			if (ret instanceof JSONArray) {
				JSONArray array = (JSONArray) ret;
				StringBuilder stringBuilder = new StringBuilder();
				for (int i = 0; i < array.length(); i++) {
					stringBuilder.append(array.getString(i));
				}
				return stringBuilder.toString();
			}

		} catch (Exception e) {
			e.printStackTrace();
		}
		return "";
	}

	private List<OrgTrans> getSentenceList(JSONObject object) {
		ArrayList<OrgTrans> ret = new ArrayList<OrgTrans>();
		if (null == object) {
			return ret;
		}

		try {
			JSONArray array = object.getJSONArray("sent");
			for (int i = 0; i < array.length(); i++) {
				JSONObject tmp = array.getJSONObject(i);
				OrgTrans orgTrans = new OrgTrans(tmp);
				ret.add(orgTrans);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

		return ret;
	}
}
