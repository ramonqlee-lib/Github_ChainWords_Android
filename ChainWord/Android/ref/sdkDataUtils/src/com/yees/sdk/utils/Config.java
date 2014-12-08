package com.yees.sdk.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.text.TextUtils;

public class Config {
	private static Config sConfig;
	private Context mContext;

	public synchronized static Config sharedInstance(Context context) {
		if (null == sConfig) {
			sConfig = new Config(context);
		}

		return sConfig;
	}
	
	private Config(Context context) {
		if (null == context) {
			return;
		}
		mContext = context.getApplicationContext();
	}

	private SharedPreferences getSharedPreferences()
	{
		if (null == mContext) {
			return null;
		}
		return mContext.getSharedPreferences("yeesSdkSharedConfig", 0);
	}
	
	private Editor getEditor()
	{
		SharedPreferences preferences= getSharedPreferences();
		if (null == preferences) {
			return null;
		}
		return preferences.edit();
	}
	
	public boolean putString(String key, String value) {
		Editor editor = getEditor();
		if (null == editor || TextUtils.isEmpty(key)) {
			return false;
		}
		editor.putString(key, value);
		return editor.commit();
	}

	public String getString(String key) {
		SharedPreferences preferences = getSharedPreferences();
		if (null == preferences || TextUtils.isEmpty(key)) {
			return "";
		}
		return preferences.getString(key, "");
	}

	public boolean putBoolean(String key, boolean value) {
		Editor editor = getEditor();
		if (null == editor || TextUtils.isEmpty(key)) {
			return false;
		}
		editor.putBoolean(key, value);
		return editor.commit();
	}

	public boolean getBoolean(String key) {
		SharedPreferences preferences = getSharedPreferences();
		if (null == preferences || TextUtils.isEmpty(key)) {
			return false;
		}
		return preferences.getBoolean(key, false);
	}
	
	public boolean putInt(String key, int value)
	{
		Editor editor = getEditor();
		if (null == editor || TextUtils.isEmpty(key)) {
			return false;
		}
		editor.putInt(key, value);
		return editor.commit();
	}
	
	/**
	 * 
	 * @param key
	 * @return 缺省返回-1
	 */
	public int getInt(String key)
	{
		SharedPreferences preferences = getSharedPreferences();
		final int defaultInt = -1;
		if (null == preferences || TextUtils.isEmpty(key)) {
			return defaultInt;
		}
		return preferences.getInt(key, defaultInt);
	}
	
	public boolean putLong(String key,long value)
	{
		Editor editor = getEditor();
		if (null == editor || TextUtils.isEmpty(key)) {
			return false;
		}
		editor.putLong(key, value);
		return editor.commit();
	}
	
	/**
	 * 
	 * @param key
	 * @return 缺省返回-1
	 */
	public long getLong(String key)
	{
		SharedPreferences preferences = getSharedPreferences();
		final int defaultInt = -1;
		if (null == preferences || TextUtils.isEmpty(key)) {
			return defaultInt;
		}
		return preferences.getLong(key, defaultInt);
	}
}
