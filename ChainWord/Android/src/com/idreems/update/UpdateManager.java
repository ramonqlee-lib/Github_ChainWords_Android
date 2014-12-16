package com.idreems.update;

import android.content.Context;
import android.text.TextUtils;
import android.widget.Toast;

import com.dream2reality.chainwords.R;
import com.dream2reality.utils.AppConstants;
import com.idreems.sdk.common.runners.CheckUpdateRunner;
import com.idreems.sdk.netmodel.ParsedTaskReponse;
import com.idreems.sdk.netmodel.UpdateModel;
import com.idreems.update.DownLoadManager.OnDownLoadResultListener;
import com.yees.sdk.lightvolley.TaskListener;
import com.yees.sdk.lightvolley.TaskResponse;
import com.yees.sdk.utils.Config;
import com.yees.sdk.utils.Logger;
import com.yees.sdk.utils.Utils;

/**
 * 升级
 */
public class UpdateManager {

	private static final String TAG = "UpdateManager";

	private UpdateManager() {
	}
	private static UpdateManager manager = new UpdateManager();

	public static UpdateManager getIntance() {
		return manager;
	}
	
	/**
	 * 
	 * @param context
	 * @param newestMessgeTip 已经是最新版本时的提示
	 */
	public void checkNewVersion(final Context context,final String newestMessgeTip) {
		// 检查新版本，并尝试进行安装
		Logger.d(TAG, "start checking new version");
		String updateServerUrl = Utils.getJsonValue(context,AppConstants.CHECK_NEW_VERSION_URL_KEY);
		CheckUpdateRunner runner = new CheckUpdateRunner(context,updateServerUrl);
		runner.setTaskListener(new TaskListener() {

			@Override
			public void onSuccess(TaskResponse response) {
				if (null == response || !(response instanceof ParsedTaskReponse)) {
					return;
				}

				ParsedTaskReponse parsedTaskReponse = (ParsedTaskReponse) response;
				Object object = parsedTaskReponse.parsedObject;
				if (!(object instanceof UpdateModel)) {
					return;
				}

				UpdateModel model = (UpdateModel) object;
				// 发现新版本，尝试升级
				if (model.newerVersion(context)) {
					Logger.d(TAG,"found new version,start downloading right now!");

					DownLoadManager dowApkUpdateManager = new DownLoadManager(context, model.url, model.fileMd5,context.getString(R.string.app_name), "新版本下载中...");
					dowApkUpdateManager.setOnDownLoadResultListener(new OnDownLoadResultListener() {

								@Override
								public void onSuccess(String filePath) {
									// 保存文件名，升级完毕后，可以删除他
									Config.sharedInstance(context).putString(AppConstants.APK_FILE_NAME, filePath);
									
									// 升级代码(转移到下载apk完毕后，弹出安装提示的地方)
									ConditionalApkInstaller installer = new ConditionalApkInstaller(context, filePath);
									installer.checkAndInstall(true,new ConditionalApkInstaller.InstallChecker() {

														@Override
														public boolean installable() {
															return true;
														}
													});
								}
								//md5校验失败
								@Override
								public void onFailed(String filePath) {

								}
								//下载失败
								@Override
								public void onDownloadError(String filePath) {
									String apkFilePath = Config.sharedInstance(context).getString(AppConstants.APK_FILE_NAME);
									Utils.deleteFile(apkFilePath);
								}
							});
					dowApkUpdateManager.execute();
				} else { //清除下载文件夹
					Logger.d(TAG, "newest version already!");
					if (!TextUtils.isEmpty(newestMessgeTip)) {
						Toast.makeText(context, newestMessgeTip, Toast.LENGTH_LONG).show();
					}
					// remove apk file
					Utils.deleteFile(Config.sharedInstance(context).getString(AppConstants.APK_FILE_NAME));
				}
			}

			@Override
			public void onFailure(int arg0, String arg1) {
			}
		});
		runner.run();
	}
}
