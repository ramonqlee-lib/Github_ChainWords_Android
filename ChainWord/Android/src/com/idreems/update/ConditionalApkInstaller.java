package com.idreems.update;

import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.content.IntentFilter;
import android.text.TextUtils;

import com.dream2reality.chainwords.R;
import com.yees.sdk.utils.Constants;
import com.yees.sdk.utils.Logger;

/**
 * 有条件的安装下载的apk包
 * @author ramonqlee
 *
 */
public class ConditionalApkInstaller {
	/**
	 * 条件安装的接口
	 * @author ramonqlee
	 *
	 */
	public static interface InstallChecker
	{
		public boolean installable();//是否可安装
	}
	
	Context appContext;
	String apkPath;
	
	public ConditionalApkInstaller(Context context,String apkFilePath)
	{
		this.apkPath = apkFilePath;
		appContext = (null != context)?context:null;
	}
	
	/**
	 * 退出当前应用，并弹出系统安装提示
	 * 有可能受到一些条件限制，暂时不能进行安装。遇到这种情况，每隔1分钟进行一次检查，当满足条件时进行安装
	 */
	public void checkAndInstall(final boolean killProcesss,final InstallChecker checker)
	{
		if (null == checker || checker.installable()) {
			execInstall(killProcesss);
			return;
		}
		
		//  当前条件不满足，暂时不能安装，注册一个时钟监听，每分钟查看一次，满足条件了再安装
		if (null == appContext) {
			Logger.e(Constants.LOG_TAG, "context is null for apk installer");
			return;
		}
		
		IntentFilter filter = new IntentFilter();
		filter.addAction(Intent.ACTION_TIME_TICK);
		appContext.registerReceiver(new BroadcastReceiver() {

			@Override
			public void onReceive(Context context, Intent intent) {
				if (null == intent) {
					return;
				}
				if (TextUtils.equals(Intent.ACTION_TIME_TICK,intent.getAction())) {
					if (!checker.installable()) {
						if (true) {
							Logger.d(Constants.LOG_TAG, "wait for apk update with timetick");
						}
						return;
					}
					
					if (true) {
						Logger.d(Constants.LOG_TAG, "try to start apk update with timetick");
					}
					appContext.unregisterReceiver(this);
					 
					execInstall(killProcesss);
				}
			}

		}, filter);
	}
	
	private void execInstall(final boolean killProcesss)
	{
		new AlertDialog.Builder(appContext).setTitle(R.string.app_name)
		.setMessage(R.string.found_new_version)
		.setPositiveButton(R.string.ok, new OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				// 直接调用系统安装器，进行安装
				DownLoadManager.install(appContext, apkPath);
				android.os.Process.killProcess(android.os.Process.myPid());
			}
		}).show();
	}
}
