package com.yees.message;

import java.util.Vector;

import android.text.TextUtils;


public class MessageCenter {
	private Vector<MessageHandler> handlerVector = new Vector<MessageHandler>();
	private boolean ignoreMode;
	private Object synObject = new Object();//同步用

	/**
	 * 返回消息推送通知中心的单例
	 * 
	 * @param context
	 * @param ignoreMode
	 *            : 是否忽略后续的处理器
	 * @return
	 */
	public  MessageCenter( boolean ignoreMode) {
		this.ignoreMode = ignoreMode;
	}

	private void makesureInit() {
		if (null == handlerVector) {
			handlerVector = new Vector<MessageHandler>();
			return;
		}
	}

	/**
	 * 添加push处理器
	 * 
	 * @param handler
	 */
	public void addObserver(MessageHandler handler) {
		if (null == handler) {
			return;
		}

		makesureInit();
		synchronized (synObject) {
			handlerVector.add(handler);
		}
	}

	/**
	 * 移除push处理器
	 * 
	 * @param handler
	 */
	public void removeObserver(MessageHandler handler) {
		if (null == handler) {
			return;
		}
		if (null == handlerVector || 0 == handlerVector.size()) {
			return;
		}
		synchronized (synObject) {
			handlerVector.remove(handler);
		}
	}

	/**
	 * 清除所有tag标记的push 处理器
	 * @param tag
	 */
	public void removeAllObserversWithTag(String tag)
	{
		if ( TextUtils.isEmpty(tag) || null == handlerVector || 0 == handlerVector.size()) {
			return;
		}
		synchronized (synObject) {
			//  移除包含tag的handler
			Vector<MessageHandler> keepHandler = new Vector<MessageHandler>();
			for (int i = 0; i < handlerVector.size(); i++) {
				MessageHandler msg = handlerVector.get(i);
				if (null == msg || TextUtils.equals(tag, msg.getTag())) {
					continue;
				}
				keepHandler.add(msg);
			}
			handlerVector.clear();
			handlerVector.addAll(keepHandler);
		}
	}
	/**
	 * 清除所有的push处理器
	 */
	public void clearAllObservers() {
		if (null == handlerVector || 0 == handlerVector.size()) {
			return;
		}
		synchronized (synObject) {
			handlerVector.clear();
		}
	}

	/**
	 * 将收到的消息分发到各个handler 按照顺序尝试分发，直至找到合适的handler为止
	 */
	public void dispatch(Object obj) {
		if (null == handlerVector || 0 == handlerVector.size()) {
			return;
		}
		synchronized (synObject) {
			for (int i = handlerVector.size() - 1; i >= 0; --i) {
				MessageHandler handler = handlerVector.get(i);
				// oopse, this handler is a placeholder only,remove it
				if (null == handler) {
					handlerVector.remove(i);
					return;
				}
				// 找到了能处理该数据的处理器，处理后直接跳出循环就ok了
				if (handler.handle(obj)) {
					if (ignoreMode) {
						break;
					}
				}
			}
		}
	}
	
	public int size() {
		return (null == handlerVector) ? 0 : handlerVector.size();
	}
}
