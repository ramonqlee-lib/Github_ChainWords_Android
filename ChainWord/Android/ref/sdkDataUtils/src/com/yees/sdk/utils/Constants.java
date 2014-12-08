package com.yees.sdk.utils;

import android.content.Context;
import android.text.TextUtils;

/**
 * 内部使用的常量
 */
public class Constants {
	private static final String SERVER_ENV_KEY = "Constants_SERVER_ENV_KEY";
	private static final String TEST_DATA_KEY = "Constants_TEST_DATA_KEY";
	private static final String LOG_ENABLE_KEY = "Constants_LOG_ENABLE_KEY";
	private static final String UMENG_LOG_ENABLE_KEY = "Constants_UMENG_LOG_ENABLE_KEY";

	public static final String LOG_TAG = "Yees";// 控制台日志的tag
	// 版本
	public static final String VERSOIN_STRING = "1.0.0";
	// revision
	public static final String REVISION_FILE = "revision.txt";
	public static final String REVISION_KEY = "revision";
	public static final int MAX_THREAD_POOL_CACHE_SIZE = 2;// 线程池最大线程数
	private static Context sAppContext = null;
	
	//保存文件的key
	public static final String CYCLE_KEY = "Cycel_key";
	

	/**
	 * 使用其他api前，需要设置context
	 * @param context
	 */
	public static void setAppContext(Context context) {
		if (null == context) {
			return;
		}
		sAppContext = context;
	}

	public static Context getAppContext() {
		return sAppContext;
	}
	
	// 输出调试日志
	// 影响范围：服务器将访问测试服务器
	public static boolean isServerDebugEnv() {
		return getBoolean(SERVER_ENV_KEY);//
	}

	// 是否使用测试数据
	public static boolean isTestDataEnabled() {
		// 正式环境下，自动屏蔽该选项
		if (!isServerDebugEnv()) {
			Logger.i(LOG_TAG, "disable test data under formal env");
			return false;
		}
		return getBoolean(TEST_DATA_KEY);
	}

	// 是否在控制台输出日志
	public static boolean isLocalLogEnabled() {
		return getBoolean(LOG_ENABLE_KEY);
	}

	public static boolean isUmengLogEnabled() {
		return getBoolean(UMENG_LOG_ENABLE_KEY);
	}

	// 设置开关api
	// 环境设置
	public static void enableServerDebugEnv(boolean value) {
		setBoolean(SERVER_ENV_KEY, value);
	}

	public static void enableTestData(boolean value) {
		setBoolean(TEST_DATA_KEY, value);
	}

	public static void enableLocalLog(boolean value) {
		setBoolean(LOG_ENABLE_KEY, value);
	}

	public static void enableUmengLog(boolean value) {
		setBoolean(UMENG_LOG_ENABLE_KEY, value);
	}

	// 公共辅助函数
	private static void setBoolean(String key, boolean value) {
		if (null == getAppContext() || TextUtils.isEmpty(key)) {
			return;
		}
		Config.sharedInstance(sAppContext).putBoolean(key, value);
	}

	private static boolean getBoolean(String key) {
		if (null == getAppContext() || TextUtils.isEmpty(key)) {
			return false;
		}
		return Config.sharedInstance(sAppContext).getBoolean(key);
	}
}
