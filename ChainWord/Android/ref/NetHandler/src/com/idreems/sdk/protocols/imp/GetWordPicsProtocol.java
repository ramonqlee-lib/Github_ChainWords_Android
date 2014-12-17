package com.idreems.sdk.protocols.imp;

import android.content.Context;

import com.idreems.sdk.netmodel.GetWordPicsResp;
import com.idreems.sdk.protocols.Protocol;

/**
 * 获取单词对应的图片
 * 
 * @author ramonqlee
 * 
 */
public class GetWordPicsProtocol implements Protocol {
//	private static final String PROTOCOL_URL = "http://ajax.googleapis.com/ajax/services/search/images?v=1.0&start=0&q=";//&q=horse
	private static final String PROTOCOL_URL = "http://image.baidu.com/i?tn=baiduimagejson&ie=utf-8&ic=0&rn=20&pn=0&word=";
	private Context context;
	private String versionString;
	private String word;

	// token，验签值，任务id
	public GetWordPicsProtocol(Context context, String version,String word) {
		this.context = context;
		this.word = word;
		versionString = version;
	}

	@Override
	public byte[] getBytes() {
		return "".getBytes();
	}

	@Override
	public Object handleResponse(byte[] data) {
		return new GetWordPicsResp(data);
	}

	@Override
	public String getUrl() {
		return PROTOCOL_URL+word;
	}

}
