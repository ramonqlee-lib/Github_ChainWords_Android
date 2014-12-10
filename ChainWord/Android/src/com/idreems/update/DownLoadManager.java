package com.idreems.update;

import java.io.File;
import java.io.IOException;
import java.util.Locale;

import android.app.DownloadManager;
import android.app.DownloadManager.Request;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.os.Environment;
import android.text.TextUtils;
import android.webkit.MimeTypeMap;

import com.yees.sdk.utils.Logger;
import com.yees.sdk.utils.Utils;

public class DownLoadManager {
	public static final String TAG = "DownLoadManager";
	private static final String DOWNLOAD_FOLDER_NAME = "ChainWords";
	private static String PREFERENCE_NAME = "VersionUpdate";
	private Context context;
	private String url;
	private String notificationTitle;
	private String notificationDescription;
	private DownloadManager downloadManager;
	private CompleteReceiver completeReceiver;
	private String apkDownloadId;// 将apkName作为id记录 例:driver.apk 其name = driver
	private String fileName; // 文件名 例:driver.apk
	private String md5Vaiue; // md5校验值

	/**
	 * 判断当前的状态是否是在更新中
	 */
	private OnDownLoadResultListener downLoadResultListener;
	private Request request;
	private String apkFilePath;

	public void setOnDownLoadResultListener(
			OnDownLoadResultListener downLoadResultListener) {
		this.downLoadResultListener = downLoadResultListener;
	}

	/**
	 * @param context
	 * @param url
	 *            下载apk的url
	 * @param md5Vaiue
	 *            MD5校验值
	 * @param notificationTitle
	 *            通知栏标题
	 * @param notificationDescription
	 *            通知栏描述
	 */
	public DownLoadManager(Context context, String url, String md5Vaiue,
			String notificationTitle, String notificationDescription) {
		super();
		this.context = context;
		this.url = url;
		this.md5Vaiue = TextUtils.isEmpty(md5Vaiue) ? "" : md5Vaiue;
		this.notificationTitle = notificationTitle;
		this.notificationDescription = notificationDescription;

		apkDownloadId = url.substring(url.lastIndexOf("/") + 1,
				url.lastIndexOf("."));
		fileName = url.substring(url.lastIndexOf("/") + 1);

		downloadManager = (DownloadManager) context
				.getSystemService(Context.DOWNLOAD_SERVICE);
		completeReceiver = new CompleteReceiver();

		/** register download success broadcast **/
		context.registerReceiver(completeReceiver, new IntentFilter(
				DownloadManager.ACTION_DOWNLOAD_COMPLETE));
		apkFilePath = new StringBuilder(Environment.getExternalStorageDirectory().getAbsolutePath())
    	.append(File.separator).append(DOWNLOAD_FOLDER_NAME).append(File.separator)
    	.append(fileName).toString();
	}

	public void execute() {
		// putDownLoadStatus(true);

		// 清除已下载的内容重新下载
		long downloadId = getLong(context, apkDownloadId);
		Logger.d(TAG, "downloadId == " + downloadId);
		int status = queryDownloadStatus(downloadManager, downloadId);
		Logger.d(TAG, "execute status = " + status);
		if (status == DownloadManager.STATUS_PENDING
				|| status == DownloadManager.STATUS_RUNNING
				|| status == DownloadManager.STATUS_PAUSED) { // 说明正在下载
			return;
		}

		if (status == DownloadManager.STATUS_SUCCESSFUL || downloadId == -1) { // 已经下载成功的话
			if (status == DownloadManager.STATUS_SUCCESSFUL) {
				Logger.i(TAG, "已经下载成功!");
			} else {
				Logger.i(TAG, "检查本地是否有最新的文件!");
			}
			String fileMD5;
			try {
				fileMD5 = Utils.fileMD5(apkFilePath);
				Logger.i(TAG, "本地文件生成的MD5值 : " + fileMD5);
				Logger.i(TAG, "server端获取的MD5值 : " + md5Vaiue);
				fileMD5 = TextUtils.isEmpty(fileMD5) ? "" : fileMD5;
				if (fileMD5.toLowerCase(Locale.ENGLISH).equals(md5Vaiue.toLowerCase(Locale.ENGLISH))) {
					Logger.d(TAG, "MD5检验成功!");
					if (downLoadResultListener != null) {
						downLoadResultListener.onSuccess(apkFilePath);
						// clear downloadId
						removeSharedPreferenceByKey(context, apkDownloadId);
					}
					return;
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

		Logger.d(TAG, "downloadId == " + downloadId);
		if (downloadId != -1) {
			downloadManager.remove(downloadId);
			removeSharedPreferenceByKey(context, apkDownloadId);
		}

		request = new Request(Uri.parse(url));
		// 设置Notification中显示的文字
		request.setTitle(notificationTitle);
		request.setDescription(notificationDescription);
		// 设置可用的网络类型
		request.setAllowedNetworkTypes(Request.NETWORK_MOBILE
				| Request.NETWORK_WIFI);
		// 设置状态栏中显示Notification
		// request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
		// 不显示下载界面
		request.setVisibleInDownloadsUi(false);
		// 设置下载后文件存放的位置
		File folder = Environment
				.getExternalStoragePublicDirectory(DOWNLOAD_FOLDER_NAME);
		if (!folder.exists() || !folder.isDirectory()) {
			folder.mkdirs();
		}
		request.setDestinationInExternalPublicDir(DOWNLOAD_FOLDER_NAME,
				fileName);
		// 设置文件类型
		MimeTypeMap mimeTypeMap = MimeTypeMap.getSingleton();
		String mimeString = mimeTypeMap.getMimeTypeFromExtension(MimeTypeMap
				.getFileExtensionFromUrl(url));
		if(!TextUtils.isEmpty(mimeString))
		{
			request.setMimeType(mimeString);
		}
		// 保存返回唯一的downloadId
		putLong(context, apkDownloadId, downloadManager.enqueue(request));
		Logger.d(TAG, "开始下载...");
	}

	class CompleteReceiver extends BroadcastReceiver {

		@Override
		public void onReceive(Context context, Intent intent) {
			/**
			 * get the id of download which have download success, if the id is
			 * my id and it's status is successful, then install it
			 **/
			long downloadId = getLong(context, apkDownloadId);
			Logger.i(TAG, "接收到下载完成的广播");
			int status = queryDownloadStatus(downloadManager, downloadId);
			Logger.d(TAG, "CompleteReceiver status = " + status);
			switch (status) {
			case DownloadManager.STATUS_SUCCESSFUL:// 下载成功!
				Logger.d(TAG, "下载成功!");
				// unregisterReceiver
				context.unregisterReceiver(completeReceiver);

				Logger.d(TAG, "下载文件的路径 : " + apkFilePath);
				try {
					String fileMD5 = Utils.fileMD5(apkFilePath);
					Logger.i(TAG, "本地文件生成的MD5值 : " + fileMD5);
					Logger.i(TAG, "server端获取的MD5值 : " + md5Vaiue);
					fileMD5 = TextUtils.isEmpty(fileMD5) ? "" : fileMD5;
					if (fileMD5.toLowerCase(Locale.ENGLISH).equals(md5Vaiue.toLowerCase(Locale.ENGLISH))) {
						Logger.d(TAG, "MD5检验成功!");
						if (downLoadResultListener != null) {
							downLoadResultListener.onSuccess(apkFilePath);
							// clear downloadId
							removeSharedPreferenceByKey(context, apkDownloadId);
						}
					} else {
						Logger.e(TAG, "MD5检验失败!");
						if (downLoadResultListener != null) {
							downLoadResultListener.onFailed(apkFilePath);
						}
					}
				} catch (IOException e) {
					e.printStackTrace();
					Logger.e(TAG, "获取下载apk的MD5值失败");
					// putDownLoadStatus(false);
				}
				break;
			case DownloadManager.STATUS_FAILED: // 下载失败!
				Logger.e(TAG, "下载失败,尝试重新下载");
				downloadManager.remove(downloadId);
				// putDownLoadStatus(false);
				if (downLoadResultListener != null) {
					downLoadResultListener.onFailed(apkFilePath);
				}
				// execute();
				break;
			}
		}
	};

	/** 查询下载状态 */
	private int queryDownloadStatus(DownloadManager downloadManager,
			long downloadId) {
		int result = -1;
		DownloadManager.Query query = new DownloadManager.Query()
				.setFilterById(downloadId);
		Cursor c = null;
		try {
			c = downloadManager.query(query);
			if (c != null && c.moveToFirst()) {
				result = c.getInt(c
						.getColumnIndex(DownloadManager.COLUMN_STATUS));
			}
		} finally {
			if (c != null) {
				c.close();
			}
		}
		return result;
	}

	/**
	 * install app
	 * 
	 * @param context
	 * @param filePath
	 * @return whether apk exist
	 */
	public static boolean install(Context context, String filePath) {
		// putDownLoadStatus(false);
		Intent i = new Intent(Intent.ACTION_VIEW);
		File file = new File(filePath);
		if (file != null && file.length() > 0 && file.exists() && file.isFile()) {
			i.setDataAndType(Uri.parse("file://" + filePath),
					"application/vnd.android.package-archive");
			i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			context.startActivity(i);
			return true;
		}
		return false;
	}

	/**
	 * 删除指定文件夹下所有文件
	 * 
	 * @return
	 */
	public static void DeleteFile(File file) {
		Logger.i(TAG, "DeleteFile -> " + file.getAbsolutePath());
		if (!file.exists()) {
			return;
		} else {
			if (file.isFile()) {
				file.delete();
				return;
			}
			if (file.isDirectory()) {
				File[] childFile = file.listFiles();
				if (childFile == null || childFile.length == 0) {
					file.delete();
					return;
				}
				for (File f : childFile) {
					DeleteFile(f);
				}
				file.delete();
			}
		}
	}

	private boolean putLong(Context context, String key, long value) {
		Logger.i(TAG, "putLong key = " + key + " , value = " + value);
		SharedPreferences settings = context.getSharedPreferences(
				PREFERENCE_NAME, Context.MODE_PRIVATE);
		SharedPreferences.Editor editor = settings.edit();
		editor.putLong(key, value);
		return editor.commit();
	}

	private long getLong(Context context, String key) {
		return getLong(context, key, -1);
	}

	private long getLong(Context context, String key, long defaultValue) {
		SharedPreferences settings = context.getSharedPreferences(
				PREFERENCE_NAME, Context.MODE_PRIVATE);
		return settings.getLong(key, defaultValue);
	}

	private boolean removeSharedPreferenceByKey(Context context, String key) {
		Logger.i(TAG, "removeSharedPreferenceByKey : " + key);
		SharedPreferences settings = context.getSharedPreferences(
				PREFERENCE_NAME, Context.MODE_PRIVATE);
		SharedPreferences.Editor editor = settings.edit();
		editor.remove(key);
		return editor.commit();
	}

	// private static boolean putDownLoadStatus(boolean isDownLoad){
	// SharedPreferences settings = context.getSharedPreferences(UPDATESTATUS,
	// Context.MODE_PRIVATE);
	// SharedPreferences.Editor editor = settings.edit();
	// editor.putBoolean(DOWNLOADSTATUS, isDownLoad);
	// return editor.commit();
	// }
	//
	// public static boolean getDownLoadStatus(Context context){
	// SharedPreferences settings = context.getSharedPreferences(UPDATESTATUS,
	// Context.MODE_PRIVATE);
	// return settings.getBoolean(DOWNLOADSTATUS, false);
	// }

	public static interface OnDownLoadResultListener {
		void onSuccess(String filePath);

		void onFailed(String filePath);

		void onDownloadError(String filePath);
	}

	public String getUrl() {
		return url;
	}
}
