package com.idreems.sdk.protocols;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.json.JSONArray;
import org.json.JSONObject;

import android.content.Context;
import android.text.TextUtils;
import android.util.Log;

import com.yees.sdk.utils.Constants;
import com.yees.sdk.utils.Decrypt;
import com.yees.sdk.utils.Utils;

public class ProtocolUtils {
	public static final int DEFAULT_JSON_INT = -1;
	private static final String HEAD_KEY = "head";
	private static final String CODE_KEY = "code";
	private static final String DESC_KEY = "desc";
	public static final String BODY_KEY = "body";
	public static final String TASK_KEY = "task";
	public static final String USER_KEY = "users";

	private static final String DEBUG_URL_KEY = "DebugUrl";
	private static final String FORMAL_URL_KEY = "FormalUrl";

	// 网络调用返回码
	public static final int HTTP_CODE_OK = 200;// 成功
	public static final int HTTP_CODE_DUPLICATE_REQ = 409;// 重复调用
	public static final int HTTP_CODE_OUT_OF_SERVICE = 416;// 超出运营时间
	public static final int HTTP_CODE_ILLEGAL_PARAM = 417;// 状态符合预期，无法完成当前操作
	public static final int HTTP_CODE_USED_RESOURCE = 422;// 资源被占用
	public static final int HTTP_CODE_UNAVAILABLE_RESOURCE = 503;// 没有车(没有订单)

	public static final String SALT = "FromWikipediathefreeencyclopedia";

	public static String getSign(Map<String, String> map, String macAddress,
			String salt) {
		// ：对数据进行签名，用于完整性和有效性校验
		/**
		 * 验签算法 参与算法的参数：所有post参数（二进制流除外），pad mac地址，盐 算法内容： 1. 先按照key进行升序排列
		 * 2.sign = ver1_name=ver1_valuever2_name=ver2_value... 3. sign += "_" +
		 * pad_mac + “_” + salt 4. sign = sha1(sign)
		 */
		List<String> list = new ArrayList<String>();
		for (String key : map.keySet()) {
			list.add(key);
		}
		Collections.sort(list, new Comparator<String>() {
			@Override
			public int compare(String lhs, String rhs) {
				return (null == lhs) ? 0 : lhs.compareTo(rhs);
			}
		});

		final StringBuilder sBuilder = new StringBuilder();
		for (String key : list) {
			if (TextUtils.isEmpty(key)) {
				continue;
			}
			String value = map.get(key);
			sBuilder.append(String.format(Locale.ENGLISH, "%s=%s", key, value));
		}
		sBuilder.append(String.format(Locale.ENGLISH, "_%s_%s", macAddress,
				salt));

		return Decrypt.SHA1(sBuilder.toString());
	}

	/**
	 * 返回sign签名
	 * 
	 * @param map
	 * @return
	 */
	public static byte[] sign(Map<String, String> map, String macAddress,
			String salt) {
		// ：对数据进行签名，用于完整性和有效性校验
		/**
		 * 验签算法 参与算法的参数：所有post参数（二进制流除外），pad mac地址，盐 算法内容： 1. 先按照key进行升序排列
		 * 2.sign = ver1_name=ver1_valuever2_name=ver2_value... 3. sign += "_" +
		 * pad_mac + “_” + salt 4. sign = sha1(sign)
		 */
		final StringBuilder postStringBuilder = new StringBuilder();
		for (String key : map.keySet()) {
			if (TextUtils.isEmpty(key)) {
				continue;
			}
			postStringBuilder
					.append(String.format("%s=%s&", key, map.get(key)));
		}

		String sha1 = getSign(map, macAddress, salt);
		postStringBuilder.append(String.format(Locale.ENGLISH, "%s=%s", "sign",
				sha1));
		if (true) {
			Log.d(Constants.LOG_TAG, postStringBuilder.toString());
		}
		return postStringBuilder.toString().getBytes();
	}

	/**
	 * 返回协议的url
	 * 
	 * @param context
	 * @param debugMode
	 * @param key
	 * @return
	 */
	public static String getProtocolUrl(Context context, boolean debugMode,
			String subUrl) {
		if (null == context || TextUtils.isEmpty(subUrl)) {
			return "";
		}

		return Utils.getJsonValue(context, debugMode ? DEBUG_URL_KEY
				: FORMAL_URL_KEY)
				+ subUrl;
	}

	/**
	 * 获取处理状态，是否成功
	 * 
	 * @param data
	 * @return
	 */
	public static boolean handleResponseState(byte[] data) {
		return (HTTP_CODE_OK == handleResponseCode(data));
	}

	/**
	 * 返回状态码
	 * 
	 * @param data
	 * @return
	 */
	public static int handleResponseCode(byte[] data) {
		int r = DEFAULT_JSON_INT;
		if (null == data || 0 == data.length) {
			return r;
		}
		/**
		 * { "head": { "code": "200", "desc": "OK" }, "body":
		 */
		String jsonString = new String(data);
		if (TextUtils.isEmpty(jsonString)) {
			return r;
		}
		try {
			JSONObject obj = new JSONObject(jsonString);
			if (obj.has(HEAD_KEY)) {
				obj = (JSONObject) obj.get(HEAD_KEY);
				if (obj.has(CODE_KEY)) {
					return obj.getInt(CODE_KEY);
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return r;
	}

	public static JSONObject handleResponseBody(JSONObject obj) {
		JSONObject r = new JSONObject();
		if (null == obj) {
			return r;
		}
		try {
			if (obj.has(BODY_KEY) && (obj.get(BODY_KEY) instanceof JSONObject)) {
				return obj.getJSONObject(BODY_KEY);
			}
			
		} catch (Exception e) {
			e.printStackTrace();
		}
		return r;
	}

	public static String handleResponseDescription(JSONObject obj) {
		String r = "";
		if (null == obj) {
			return r;
		}
		try {
			if (obj.has(HEAD_KEY)) {
				obj = (JSONObject) obj.get(HEAD_KEY);
				if (obj.has(DESC_KEY)) {
					return obj.getString(DESC_KEY);
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return r;
	}

	public static int handleResponseCode(JSONObject obj) {
		int r = DEFAULT_JSON_INT;
		if (null == obj) {
			return r;
		}
		try {
			if (obj.has(HEAD_KEY)) {
				obj = (JSONObject) obj.get(HEAD_KEY);
				if (obj.has(CODE_KEY)) {
					return obj.getInt(CODE_KEY);
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return r;
	}

	/**
	 * 获取处理状态的描述字符串
	 * 
	 * @param data
	 * @return
	 */
	public static String handleResponseDesc(byte[] data) {
		String r = "";
		if (null == data || 0 == data.length) {
			return r;
		}
		/**
		 * { "head": { "code": "200", "desc": "OK" }, "body":
		 */
		String jsonString = new String(data);
		if (TextUtils.isEmpty(jsonString)) {
			return r;
		}
		try {
			JSONObject obj = new JSONObject(jsonString);
			if (obj.has(HEAD_KEY)) {
				obj = (JSONObject) obj.get(HEAD_KEY);
				if (obj.has(DESC_KEY)) {
					return obj.getString(DESC_KEY);
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return r;
	}

	/**
	 * 获取服务器返回的json对象中得body对象
	 * 
	 * @param data
	 * @return
	 */
	public static JSONObject handleResponseBody(byte[] data) {
		JSONObject r = new JSONObject();
		if (null == data || 0 == data.length) {
			return r;
		}
		/**
		 * { "head": { "code": "200", "desc": "OK" }, "body":xx
		 */
		String jsonString = new String(data);
		if (TextUtils.isEmpty(jsonString)) {
			return r;
		}
		try {
			JSONObject obj = new JSONObject(jsonString);
			if (obj.has(BODY_KEY) && (obj.get(BODY_KEY) instanceof JSONObject)) {
				return obj.getJSONObject(BODY_KEY);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return r;
	}

	public static Object handleResponseBodyInJsonArray(byte[] data) {
		JSONArray r = new JSONArray();
		if (null == data || 0 == data.length) {
			return r;
		}
		/**
		 * { "head": { "code": "200", "desc": "OK" }, "body":xx
		 */
		String jsonString = new String(data);
		if (TextUtils.isEmpty(jsonString)) {
			return r;
		}
		try {
			JSONObject obj = new JSONObject(jsonString);
			if (obj.has(BODY_KEY)) {
				return obj.getJSONArray(BODY_KEY);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return r;
	}

	public static String getJsonString(JSONObject obj, String key) {
		if (null == obj || TextUtils.isEmpty(key)) {
			return "";
		}
		try {
			if (obj.has(key)) {
				return obj.getString(key);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return "";
	}

	public static int getJsonInt(JSONObject obj, String key) {
		if (null == obj || TextUtils.isEmpty(key)) {
			return DEFAULT_JSON_INT;
		}
		try {
			if (obj.has(key)) {
				return obj.getInt(key);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return DEFAULT_JSON_INT;
	}

	public static double getJsonDouble(JSONObject obj, String key) {
		if (null == obj || TextUtils.isEmpty(key)) {
			return 0;
		}
		try {
			if (obj.has(key)) {
				return obj.getDouble(key);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return 0;
	}

	public static long getJsonLong(JSONObject obj, String key) {
		if (null == obj || TextUtils.isEmpty(key)) {
			return 0;
		}
		try {
			if (obj.has(key)) {
				return obj.getLong(key);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return 0;
	}

	public static boolean getJsonBoolean(JSONObject obj, String key) {
		if (null == obj || TextUtils.isEmpty(key)) {
			return false;
		}
		try {
			if (obj.has(key)) {
				return obj.getBoolean(key);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return false;
	}

	public static JSONObject getJsonObject(JSONObject obj, String key) {
		if (null == obj || TextUtils.isEmpty(key)) {
			return null;
		}
		try {
			if (!obj.has(key)) {
				return null;
			}
			if (!(obj.get(key) instanceof JSONObject)) {
				return null;
			}
			return obj.getJSONObject(key);
		} catch (Exception e) {
			e.printStackTrace();
		}

		return null;
	}
	
	public static Object getObject(JSONObject obj, String key) {
		if (null == obj || TextUtils.isEmpty(key)) {
			return null;
		}
		try {
			if (!obj.has(key)) {
				return null;
			}
			return obj.get(key);
		} catch (Exception e) {
			e.printStackTrace();
		}

		return null;
	}

	/**
	 * 毫秒转秒
	 * 
	 * @param val
	 * @return
	 */
	public static long millis2sec(long systemTimeInSecs) {
		// 转换为秒数(服务器端要求)
		final int SEC2MIL = 1000;
		systemTimeInSecs = (systemTimeInSecs - systemTimeInSecs % SEC2MIL);
		return systemTimeInSecs / SEC2MIL;
	}

	public static String getMac(Context context) {
		return Utils.getLocalWiFiMac(context);
	}
	
	/**
	 * 获取联网时，上传的参数
	 * @param context
	 * @return
	 */
	private static Map<String, String> getNetCommonHeaders(Context context)
	{
		Map<String,String> map = new HashMap<String, String>();
		
		//  增加mac，imei等公共参数
		map.put("versionName", Utils.getAppVersion(context));
		map.put("imei", Utils.getIMEI(context));
		map.put("imsi", Utils.getIMSI(context));
		
		return map;
	}
	
	/**
	 * 追加参数
	 * @param context
	 * @param url
	 * @return
	 */
	public static String addExtraHeaders(Context context,String url)
	{
		StringBuilder retBuilder = new StringBuilder(url);
		
		// add first separator
		if (-1 == url.indexOf("?")) {//no parameter yet
			retBuilder.append("?");
		}
		else {
			retBuilder.append("&");
		}
		Map<String, String> map = ProtocolUtils.getNetCommonHeaders(context);
		Iterator<String> iter = map.keySet().iterator();
		while (iter.hasNext()) {
		    String key = iter.next();
		    String value = map.get(key);
		    if (TextUtils.isEmpty(key) || TextUtils.isEmpty(value)) {
				continue;
			}
		    retBuilder.append(String.format(Locale.CHINA, "%s=%s&",key,value));
		}
		retBuilder.replace(retBuilder.length()-1, retBuilder.length(), "");// remove last &
		return retBuilder.toString();
	}
}
