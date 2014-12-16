package com.idreems.sdk.netmodel;

import org.json.JSONObject;

import com.idreems.sdk.protocols.ProtocolUtils;

public class GetDailySentenceResp {
	public String shareImageUrl;
	public String content;
	public String note;
	
	public GetDailySentenceResp(byte[] data)
	{
		if (null == data || 0 == data.length) {
			return;
		}
		
		//  解析数据
		try {
			JSONObject obj = new JSONObject(new String(data));
			shareImageUrl = ProtocolUtils.getJsonString(obj, "fenxiang_img");
			content = ProtocolUtils.getJsonString(obj, "content");
			note = ProtocolUtils.getJsonString(obj, "note");
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	/**
	 * {
    "sid": "1141",
    "tts": "http://news.iciba.com/admin/tts/2014-12-17.mp3",
    "content": "There are two primary choices in life: to accept conditions as they exist, or accept the responsibility for changing them.",
    "note": "人生两大选择：要么接受现状，要么接受改变现状的责任。(Denis Waitley)",
    "love": "257",
    "translation": "词霸小编：体胖还须勤锻炼，人丑就要多读书！没有王子公主的命就别有王子公主的病，不把时间花在改变现状，就只能认命你的屌丝人生。共勉！",
    "picture": "http://cdn.iciba.com/news/word/2014-12-17.jpg",
    "picture2": "http://cdn.iciba.com/news/word/big_2014-12-17b.jpg",
    "caption": "词霸每日一句",
    "dateline": "2014-12-17",
    "s_pv": "731",
    "sp_pv": "79",
    "tags": [
        {
            "id": "10",
            "name": "正能量"
        },
        {
            "id": "16",
            "name": "治愈系"
        }
    ],
    "fenxiang_img": "http://cdn.iciba.com/web/news/longweibo/imag/2014-12-17.jpg"
}
	 */
}
