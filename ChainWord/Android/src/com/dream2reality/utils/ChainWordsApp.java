package com.dream2reality.utils;

import android.content.Context;

import com.android.volley.RequestQueue;
import com.android.volley.toolbox.ImageLoader;
import com.android.volley.toolbox.Volley;
import com.idreems.sdk.cache.LruImageCache;

public class ChainWordsApp {
	private static ChainWordsApp sChainWordsApp = new ChainWordsApp();
	ImageLoader imageLoader;
	RequestQueue requestQueue;// 用完记得释放

	public static ChainWordsApp sharedInstance()
	{
		return sChainWordsApp;
	}
	
	private ChainWordsApp(){}
	/**
	 * 获取android volley用资源加载器
	 * 
	 * @param context
	 * @return
	 */
	public ImageLoader getImageLoader(Context context) {
		// 加载图片所需
		if (null == context) {
			return null;
		}
		if (null == requestQueue) {
			requestQueue = Volley.newRequestQueue(context
					.getApplicationContext());
		}
		if (null == imageLoader) {
			imageLoader = new ImageLoader(requestQueue,
					LruImageCache.instance());
		}
		return imageLoader;
	}

	/**
	 * 释放android volley用资源
	 */
	public void releaseImageLoader() {
		if (null != requestQueue) {
			requestQueue.stop();
		}
	}
}
