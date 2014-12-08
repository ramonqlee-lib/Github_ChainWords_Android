package com.yees.sdk.utils;

import java.io.File;
import java.io.IOException;
import java.util.List;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.mime.MultipartEntity;
import org.apache.http.entity.mime.content.FileBody;
import org.apache.http.impl.client.DefaultHttpClient;

import android.util.Log;

import com.yees.sdk.netmodel.NetTaskConst;

public class HttpUpload {

	private static final String TAG = "Yees";
	public static final String HOST = "pilot.uboxol.com";
	public static final String UPLOADPATH = "";

//	上传指定文件{"title":"uploadlog","description":"","custom_content":{"method":"/YeesLog/yeesapp/20141127.log,/YeesLog/yeesapp/20141126.log"}}
//	上传文件夹  {"title":"uploadlog","description":"","custom_content":{"method":"/YeesLog/yeesapp/"}}
	
	
	/**
	 * 文件上传
	 * @param filePaths 将要上传文件的数据
	 * 注:1. 数组中可以包含文件夹目录,以及文件目录
	 *    2. 目录是文件的相对目录 例: /YeesLog/yeesapp/20141127.log
	 *    3. 上传到server上的名称为padID.zip
	 * @return true 成功 ; false 失败
	 */
	public static boolean upLoadByPost(String[] filePaths) {
		try {
			if(filePaths == null || filePaths.length <= 0){
				Log.e(TAG, "要上传的文件路径为空!");
				return false;
			}
			int padID = Config.sharedInstance(Constants.getAppContext()).getInt(NetTaskConst.PAD_ID_KEY);
			List<File> files = FileUtils.getFiles(filePaths);
			File zipFile = FileUtils.zipFiles(files, PrintLog.LOGDIR + padID + ".zip");
			String upLoadPath = UPLOADPATH;
			boolean upLoadFile = upLoadFile(upLoadPath, zipFile);
			if (upLoadFile) {
				Log.d(TAG, "文件上传成功");
				FileUtils.DeleteFile(zipFile);
			}
			return true;
		} catch (Exception e) {
			e.printStackTrace();
			Log.e(TAG, "文件上传失败");
			return false;
		}
	}

	/**
	 * 用DefaultHttpClient 进行文件名上传
	 */
	private static boolean upLoadFile(String path, File filename)
			throws IOException {
		// // 获得浏览器
		HttpClient client = new DefaultHttpClient();
		// 设置发送方式
		HttpPost httpPost = new HttpPost(path);
		httpPost.setHeader("HOST", HOST);

		// 构建请求实体
		MultipartEntity entity2 = new MultipartEntity();
		entity2.addPart(filename.getName(), new FileBody(filename));

		// 将请求实体封装数据到请求中
		httpPost.setEntity(entity2);

		// 执行请求,获得返回消息
		HttpResponse response = client.execute(httpPost);
		int code = response.getStatusLine().getStatusCode();
		Log.d(TAG, "code == " + code);
		// 判断返回码
		if (code == 200) {
			return true;
		} else {
			return false;
		}
	}
}
