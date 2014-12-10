package com.idreems.sdk.netmodel;

import org.json.JSONObject;

import android.content.Context;
import android.text.TextUtils;

import com.idreems.sdk.protocols.ProtocolConst;
import com.idreems.sdk.protocols.ProtocolUtils;
import com.yees.sdk.utils.Logger;
import com.yees.sdk.utils.Utils;

/**
 * apk更新协议返回的报文
 * 
 * @author ramonqlee
 * 
 *         { "app_name": "com.yees.yeesdriver", "latest_ver": "1.0.0.3497",
 *         "update_time": 1416282463, "app_key":
 *         "114df7c1e7f822f66fb710cb891e457d", "download":
 *         "http://www.yees.com.cn/package/driver/YeesDriver_1.0.0.3497.apk" }
 */
public class UpdateModel {
	private String latestVersion;
//	private long updateTime;
	public String fileMd5;
	public String url;

	/**
	 * 是否有新版本
	 * 
	 * @param context
	 * @return
	 */
	public boolean newerVersion(Context context) {
		if (TextUtils.isEmpty(latestVersion)) {
			return false;
		}
		String appVersion = Utils.getAppVersion(context);

		return (latestVersion.compareToIgnoreCase(appVersion) > 0);
	}

	public UpdateModel(byte[] data) {
		// 解析数据，返回响应的对象
		if (null == data || 0 == data.length) {
			return;
		}
		try {
			String r = new String(data);
			int pos = r.indexOf('{');
			if (-1 != pos) {
				Logger.d(ProtocolConst.NET_LOG_TAG, "json start from "+pos);
				r = r.substring(pos, r.length());
			}
			
			JSONObject obj = new JSONObject(r);
			latestVersion = ProtocolUtils.getJsonString(obj, "latest_ver");
//			updateTime = ProtocolUtils.getJsonLong(obj, "update_time");
			fileMd5 = ProtocolUtils.getJsonString(obj, "app_key");
			url = ProtocolUtils.getJsonString(obj, "download");
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
