package com.yees.sdk.utils;

import android.util.Log;

/**
 * log日志管理 注:开发中,我们将 LOGLEVEL 的值设置为6,就可以完成所有日志的显示 产品发布时,我们将LOGLEVEL
 * 的值设置为0,就可以阻止所有日志的显示
 */
public class Logger {

	// 打印出所有日志
	private static int VERBOSE = 1;
	// debug日志
	private static int DEBUG = 2;
	// info日志
	private static int INFO = 3;
	// 警告日志
	private static int WARN = 4;
	// 异常日志
	private static int ERROR = 5;
	// 打印到文件
	private static int PRINT_TO_FILE = 6;

	//允许日志输出权限值
	private static int logLevel()
	{
		return (Constants.isLocalLogEnabled()?7:0);
	}
	
	public static void v(String tag, String msg) {
		if (logLevel() > VERBOSE) {
			Log.v(tag, msg);
		}
	}

	public static void d(String tag, String msg) {
		if (logLevel() > DEBUG) {
			Log.d(tag, msg);
		}
	}

	public static void i(String tag, String msg) {
		if (logLevel() > INFO) {
			Log.i(tag, msg);
		}
	}

	public static void w(String tag, String msg) {
		if (logLevel() > WARN) {
			Log.w(tag, msg);
		}
	}

	public static void e(String tag, String msg) {
		if (logLevel() > ERROR) {
			Log.e(tag, msg);
		}
	}
	
	/** 打印log到文件中*/
	public static void p(String msg) {
		if (logLevel() > PRINT_TO_FILE) {
			PrintLog.get().log("yeesapp", msg);
		}
	}
}
