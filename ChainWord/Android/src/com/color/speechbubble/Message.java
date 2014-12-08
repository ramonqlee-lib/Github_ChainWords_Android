package com.color.speechbubble;

import android.graphics.Color;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.TextUtils;
import android.text.style.BackgroundColorSpan;
import android.text.style.ForegroundColorSpan;

/**
 * Message is a Custom Object to encapsulate message information/fields
 * 
 * @author Adil Soomro
 *
 */
public class Message {
	/**
	 * The content of the message
	 */
	CharSequence message;
	/**
	 * boolean to determine, who is sender of this message
	 */
	boolean isMine;
	/**
	 * boolean to determine, whether the message is a status message or not.
	 * it reflects the changes/updates about the sender is writing, have entered text etc
	 */
	public boolean isStatusMessage;
	
	/**
	 * Constructor to make a Message object
	 */
	public Message(CharSequence message, boolean isMine) {
		super();
		this.message = message;
		this.isMine = isMine;
		this.isStatusMessage = false;
	}
	/**
	 * Constructor to make a status Message object
	 * consider the parameters are swaped from default Message constructor,
	 *  not a good approach but have to go with it.
	 */
	public Message(boolean status, CharSequence message) {
		super();
		this.message = message;
		this.isMine = false;
		this.isStatusMessage = status;
	}
	public CharSequence getMessage() {
		return message;
	}
	public void setMessage(String message) {
		this.message = message;
	}
	public boolean isMine() {
		return isMine;
	}
	public void setMine(boolean isMine) {
		this.isMine = isMine;
	}
	public boolean isStatusMessage() {
		return isStatusMessage;
	}
	public void setStatusMessage(boolean isStatusMessage) {
		this.isStatusMessage = isStatusMessage;
	}
	
	public void setHeadSpan()
	{
		setSpan(true, false);
	}
	
	public void setTailSpan()
	{
		setSpan(false, true);
	}
	
	// 设置span属性:目前主要是上色功能
	private void setSpan(boolean start,boolean end)
	{
		if (TextUtils.isEmpty(message)) {
			return;
		}
		
		SpannableStringBuilder style=new SpannableStringBuilder(message);
		final ForegroundColorSpan span = new ForegroundColorSpan(Color.BLUE);
		if (start) {
			style.setSpan(span,0,1,Spannable.SPAN_INCLUSIVE_EXCLUSIVE);             
		}
		if (end) {
			style.setSpan(span,message.length()-1,message.length(),Spannable.SPAN_INCLUSIVE_EXCLUSIVE);
		}
		message = style;
	}
	
}
