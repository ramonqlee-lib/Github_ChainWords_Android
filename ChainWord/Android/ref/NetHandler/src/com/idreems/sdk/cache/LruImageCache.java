package com.idreems.sdk.cache;

import android.graphics.Bitmap;
import android.support.v4.util.LruCache;

import com.android.volley.toolbox.ImageLoader.ImageCache;
import com.yees.sdk.utils.Constants;
import com.yees.sdk.utils.Logger;

public class LruImageCache implements ImageCache {

	private static LruCache<String, Bitmap> mMemoryCache;

	private static LruImageCache lruImageCache;

	private LruImageCache() {
		// Get the Max available memory
		int maxMemory = (int) Runtime.getRuntime().maxMemory();
		int cacheSize = maxMemory / 8;
		mMemoryCache = new LruCache<String, Bitmap>(cacheSize) {
			@Override
			protected int sizeOf(String key, Bitmap bitmap) {
				return bitmap.getRowBytes() * bitmap.getHeight();
			}
		};
	}

	public static LruImageCache instance() {
		if (lruImageCache == null) {
			lruImageCache = new LruImageCache();
		}
		return lruImageCache;
	}

	@Override
	public Bitmap getBitmap(String arg0) {
		return mMemoryCache.get(arg0);
	}

	@Override
	public void putBitmap(String arg0, Bitmap arg1) {
		// FIXME 屏蔽内存缓存
		if (true) {
			Logger.d(Constants.LOG_TAG, "no image memory cache!");
			return;
		}		
		if (getBitmap(arg0) == null) {
			mMemoryCache.put(arg0, arg1);
		}
	}

	public void clear()
	{
		if(null == mMemoryCache)
		{
			return;
		}
		mMemoryCache.evictAll();
	}
}
