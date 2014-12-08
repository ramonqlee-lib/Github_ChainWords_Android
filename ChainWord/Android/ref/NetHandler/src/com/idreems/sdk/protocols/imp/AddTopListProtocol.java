package com.idreems.sdk.protocols.imp;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import org.json.JSONObject;

import android.content.Context;

import com.idreems.sdk.protocols.Protocol;
import com.idreems.sdk.protocols.ProtocolUtils;
import com.yees.sdk.utils.Constants;
import com.yees.sdk.utils.Utils;

/**
 * 加入排行榜
 * 
 * @author ramonqlee
 * 
 */
public class AddTopListProtocol implements Protocol {
	private static final String PROTOCOL_URL = "/chainwords/addtoplist.php";

	private Context context;
	String playerName;
	String points;
	String versionString;

	public AddTopListProtocol(Context context, String version,
			String playerName, String points) {
		this.context = context;
		this.playerName = playerName;
		this.points = points;
		this.versionString = version;
	}

	@Override
	public byte[] getBytes() {
		Map<String, String> map = new HashMap<String, String>();
		map.put("version", versionString);
		map.put("playerName", playerName);
		map.put("points", points);

		String macAddress = Utils.getLocalWiFiMac(context);
		return ProtocolUtils.sign(map, macAddress, ProtocolUtils.SALT);
	}

	@Override
	public Object handleResponse(byte[] data) {
		if (null == data || 0 == data.length) {
			return false;
		}
		try {
			JSONObject obj = new JSONObject(new String(data));
			return (ProtocolUtils.HTTP_CODE_OK == ProtocolUtils.getJsonInt(obj,
					"statusCode"));
		} catch (Exception e) {
			e.printStackTrace();
		}
		return false;
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
