package com.idreems.sdk.protocols.imp;

import java.util.Locale;

import android.content.Context;

import com.idreems.sdk.netmodel.GetWordMeaningResp;
import com.idreems.sdk.protocols.Protocol;
import com.idreems.sdk.protocols.ProtocolUtils;
import com.yees.sdk.utils.Constants;

/**
 * 获取单词对应的图片
 * 
 * @author ramonqlee
 * 
 */
public class GetWordMeaningProtocol implements Protocol {
	private static final String PROTOCOL_URL = "/chainwords/word.php?w=";
	private Context context;
	private String versionString;
	private String word;

	// token，验签值，任务id
	public GetWordMeaningProtocol(Context context, String version,String word) {
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
		return new GetWordMeaningResp(data);
	}

	@Override
	public String getUrl() {
		if (null == this.context) {
			return "";
		}

		String r = ProtocolUtils.getProtocolUrl(context,
				Constants.isServerDebugEnv(), PROTOCOL_URL);
		r += word;
		String url = String.format(Locale.ENGLISH, "%s&version=%s", r,
				versionString);
		return ProtocolUtils.addExtraHeaders(context, url);
	}

}
