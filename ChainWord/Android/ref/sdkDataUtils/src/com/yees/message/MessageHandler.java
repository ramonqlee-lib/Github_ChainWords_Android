package com.yees.message;

import java.util.Vector;

import android.os.Handler;
import android.os.Looper;


public abstract class MessageHandler {
//	public static final String PUSH_ID_KEY = "MessageHandler_PUSH_ID_KEY";//push id
	private String tagString;
	Vector<MessageListener> listenerVector = new Vector<MessageListener>();
	public abstract boolean handle(Object obj);

	private void makesureInit() {
		if (null == listenerVector) {
			listenerVector = new Vector<MessageListener>();
		}
	}

	public void addListener(MessageListener listener) {
		if (null == listener) {
			return;
		}
		makesureInit();

		listenerVector.add(listener);
	}

	public void removeLinstener(MessageListener listener) {
		if (null == listener) {
			return;
		}
		makesureInit();

		listenerVector.remove(listener);
	}

	public void clearAllListeners() {
		makesureInit();
		listenerVector.clear();
	}

	protected void notifyListeners(final Object object) {
		// 确认转换到了主线程
		new Handler(Looper.myLooper()).post(new Runnable() {
			@Override
			public void run() {
				for (MessageListener listener : listenerVector) {
					if (null == listener) {
						continue;
					}
					listener.onNotify(object);
				}
			}
		});
	}
	
	public void setTag(String tag)
	{
		tagString = tag;
	}
	
	public String getTag()
	{
		return tagString;
	}
	
}
