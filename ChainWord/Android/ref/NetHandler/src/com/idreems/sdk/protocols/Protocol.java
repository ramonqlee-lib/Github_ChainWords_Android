package com.idreems.sdk.protocols;


public interface Protocol {
	
	/**
	 * 获取网络传输的二进制字节流
	 * 
	 * @return
	 */
	byte[] getBytes();

	/**
	 * 处理网络返回的二进制字节流
	 * 
	 * @param data
	 * @return
	 */
	Object handleResponse(byte[] data);

	/**
	 * 返回协议请求的url
	 * 
	 * @return
	 */
	String getUrl();
	
}
