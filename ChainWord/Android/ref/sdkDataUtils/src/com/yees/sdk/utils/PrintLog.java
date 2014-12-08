package com.yees.sdk.utils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;

import android.text.TextUtils;

public class PrintLog {
	private String currentDate;
	private String tagName; // 区分TAG
	private HashMap<String, FileOutputStream> logFileHash;
	public static final String LOGDIR = FileUtils.getSdcardDir()+"/YeesLog/";
	private File logDir;
	private static PrintLog instance = new PrintLog();

	public static PrintLog get() {
		return instance;
	}

	
	private PrintLog() {
		if(null == logDir)
		{
			logDir = new File(LOGDIR);
		}
		logFileHash = new HashMap<String, FileOutputStream>();
	}

	/**
	 * 打log到 /YeesLog/tag/yyyyMMdd.log文件当中
	 * 
	 * @param tag
	 * @param info
	 */
	public void log(String tag, String info) {
		SimpleDateFormat mDateFormat = new SimpleDateFormat("yyyyMMdd HH:mm:ss");

		SimpleDateFormat mDayFormat = new SimpleDateFormat("yyyyMMdd");

		try {

			String today = mDayFormat.format(new Date(System
					.currentTimeMillis()));
			// 如果日期发生变化，关掉相关的tag文件
			if (TextUtils.isEmpty(tagName) || !tagName.equals(tag)
					|| TextUtils.isEmpty(currentDate)
					|| !currentDate.equals(today)) {
				currentDate = today;
				tagName = tag;

				File tagDir = new File(logDir + "/" + tagName);
				if (!tagDir.exists()) {
					tagDir.mkdirs();
				}
				File logFile = new File(tagDir, currentDate + ".log");
				if (!logFile.exists()) {
					logFile.createNewFile(); //创建新文件
					new Thread(new Runnable() {//执行删除已过期的文件
						@Override
						public void run() {
							//获取保存的时间
							long cycel = Config.sharedInstance(Constants.getAppContext()).getLong(Constants.CYCLE_KEY);
							Logger.d("Yees", "本地log日志保存的时间为 : "+cycel);
							//外置sd卡中保存的log
							FileUtils.delFile(new File(LOGDIR), cycel);
						}
					}).start();
				}
				FileOutputStream fos = new FileOutputStream(
						logFile.getAbsolutePath(), true);
				FileOutputStream old = logFileHash.put(tag, fos);
				if (old != null) {
					old.flush();
					old.close();
				}
			}

			String time = mDateFormat.format(new Date(System.currentTimeMillis()));
			FileOutputStream outStream = logFileHash.get(tag);
			if (outStream != null) {
				outStream.write((time + " " + info + "\n").getBytes());
				outStream.flush();
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void closeLogFile() {
		Collection<FileOutputStream> list = logFileHash.values();
		for (FileOutputStream os : list) {
			try {
				os.flush();
				os.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
}
