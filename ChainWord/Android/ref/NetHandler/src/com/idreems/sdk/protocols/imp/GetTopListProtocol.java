package com.idreems.sdk.protocols.imp;

import java.util.Locale;

import android.content.Context;

import com.idreems.sdk.netmodel.GetToplistResp;
import com.idreems.sdk.protocols.Protocol;
import com.idreems.sdk.protocols.ProtocolUtils;
import com.yees.sdk.utils.Constants;

/**
 * 获取排行榜
 * 
 * @author ramonqlee
 * 
 */
public class GetTopListProtocol implements Protocol {
	private static final String PROTOCOL_URL = "/chainwords/gettoplist.php";
	private Context context;
	private String versionString;

	// token，验签值，任务id
	public GetTopListProtocol(Context context, String version) {
		this.context = context;
		versionString = version;
	}

	@Override
	public byte[] getBytes() {
		return "".getBytes();
	}

	@Override
	public Object handleResponse(byte[] data) {
		return new GetToplistResp(data);
	}

	@Override
	public String getUrl() {
		if (null == this.context) {
			return "";
		}

		String r = ProtocolUtils.getProtocolUrl(context,
				Constants.isServerDebugEnv(), PROTOCOL_URL);
		String url = String.format(Locale.ENGLISH, "%s?version=%s", r,
				versionString);
		return ProtocolUtils.addExtraHeaders(context, url);
	}

}
