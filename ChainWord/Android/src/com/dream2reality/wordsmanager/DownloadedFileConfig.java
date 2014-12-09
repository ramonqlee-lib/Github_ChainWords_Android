package com.dream2reality.wordsmanager;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONObject;

import android.content.Context;
import android.text.TextUtils;

import com.dream2reality.constants.AppConstants;
import com.idreems.sdk.protocols.ProtocolUtils;
import com.yees.sdk.utils.Config;
import com.yees.sdk.utils.Utils;

public class DownloadedFileConfig {
	private static final String URL_KEY = "url";
	private static final String FILE_NAME_KEY = "fileName";
	private static final String DISPLAY_NAME_KEY = "displayName";

	/**
	 * 添加到已经下载列表
	 * 
	 * @param url
	 * @param fileName
	 */
	public static void add(Context context, String url, String fileName,
			String displayName) {
		// 是否已经存在了
		if (!TextUtils.isEmpty(getUrlFromDiplayName(context, displayName))) {
			return;
		}
		JSONArray array = getFilesJsonArray(context);
		if (null == array) {
			array = new JSONArray();
		}
		try {
			JSONObject obj = new JSONObject();
			obj.put(DISPLAY_NAME_KEY, displayName);
			obj.put(FILE_NAME_KEY, fileName);
			obj.put(URL_KEY, url);

			array.put(obj);
			Config.sharedInstance(context).putString(
					AppConstants.VOCABULARY_DOWNLOADED_ITEM_NAME_KEY,
					array.toString());
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * 获取当前设定的文件名
	 * 
	 * @param context
	 * @param url
	 * @return
	 */
	public static String getFileNameFromUrl(Context context, String url) {

		// 检查文件已经在下载的列表中，并且文件确实有效
		JSONArray array = getFilesJsonArray(context);
		if (TextUtils.isEmpty(url) || null == array) {
			return "";
		}
		try {
			for (int i = 0; i < array.length(); i++) {
				JSONObject obj = array.getJSONObject(i);
				if (!TextUtils.equals(url,
						ProtocolUtils.getJsonString(obj, URL_KEY))) {
					continue;
				}

				String fileName = ProtocolUtils.getJsonString(obj,
						FILE_NAME_KEY);
				if (fileExists(fileName)) {
					return fileName;
				}
				return "";
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return "";
	}
	
	public static boolean fileExists(String fullPathName) {
		if (TextUtils.isEmpty(fullPathName)) {
			return false;
		}
		File file = new File(fullPathName);
		return file.exists();
	}

	/**
	 * 获取所有下载文件的jsonarray
	 * 
	 * @param context
	 * @return
	 */
	public static JSONArray getFilesJsonArray(Context context) {
		// 文件下载的列表
		String str = Config.sharedInstance(context).getString(
				AppConstants.VOCABULARY_DOWNLOADED_ITEM_NAME_KEY);
		if (TextUtils.isEmpty(str)) {
			return null;
		}
		try {
			JSONArray ret = new JSONArray(str);
			return ret;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	/**
	 * 获取下载文件的显示名
	 * 
	 * @param context
	 * @return
	 */
	public static String[] getDisplayableNameArray(Context context) {
		JSONArray array = getFilesJsonArray(context);
		if (null == array) {
			return new String[0];
		}
		List<String> ret = new ArrayList<String>();

		try {
			for (int i = 0; i < array.length(); i++) {
				JSONObject obj = array.getJSONObject(i);
				if (null == obj) {
					continue;
				}
				String name = ProtocolUtils
						.getJsonString(obj, DISPLAY_NAME_KEY);
				if (TextUtils.isEmpty(name)) {
					continue;
				}
				ret.add(name);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

		String[] retArray = new String[ret.size()];
		for (int i = 0; i < ret.size(); ++i) {
			retArray[i] = ret.get(i);
		}
		return retArray;
	}

	/**
	 * 根据显示名，获取下载的url
	 * 
	 * @param context
	 * @param displayName
	 * @return
	 */
	public static String getUrlFromDiplayName(Context context,
			String displayName) {
		if (TextUtils.isEmpty(displayName)) {
			return "";
		}
		JSONArray array = getFilesJsonArray(context);
		if (null == array) {
			return "";
		}

		try {
			for (int i = 0; i < array.length(); i++) {
				JSONObject obj = array.getJSONObject(i);
				if (null == obj) {
					continue;
				}
				String name = ProtocolUtils
						.getJsonString(obj, DISPLAY_NAME_KEY);
				if (TextUtils.equals(displayName, name)) {
					return ProtocolUtils.getJsonString(obj, URL_KEY);
				}
			}

		} catch (Exception e) {
			e.printStackTrace();
		}
		return "";
	}
}
