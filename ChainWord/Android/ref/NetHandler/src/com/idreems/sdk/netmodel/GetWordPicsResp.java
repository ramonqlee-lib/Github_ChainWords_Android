package com.idreems.sdk.netmodel;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONObject;

import android.text.TextUtils;

import com.idreems.sdk.protocols.ProtocolUtils;

public class GetWordPicsResp {
	List<String> urls = new ArrayList<String>();
	
	public GetWordPicsResp(byte[] data)
	{
		if (null == data || 0 == data.length) {
			return;
		}
		
		//  解析数据
		try {
			JSONObject obj = new JSONObject(new String(data));
			obj = ProtocolUtils.getJsonObject(obj, "responseData");
			if(null == obj)
			{
				return;
			}
			JSONArray array = obj.getJSONArray("results");
			if (null == array) {
				return;
			}
			for (int i = 0; i < array.length(); i++) {
				JSONObject tmp = array.getJSONObject(i);
				String url = ProtocolUtils.getJsonString(tmp, "url");
				if(TextUtils.isEmpty(url))
				{
					continue;
				}
				urls.add(url);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	/**
	 *{
    "responseData": {
        "results": [
            {
                "GsearchResultClass": "GimageSearch",
                "width": "2492",
                "height": "3159",
                "imageId": "ANd9GcTVU58hz0L-3AIhaplT5AjE4AlG9B2UqkwRcy4Zz_Tz3pZBcv5_belbkBoL",
                "tbWidth": "118",
                "tbHeight": "150",
                "unescapedUrl": "http://kontrolmag.com/blog/wp-content/uploads/2014/11/Beauty-girl.jpg",
                "url": "http://kontrolmag.com/blog/wp-content/uploads/2014/11/Beauty-girl.jpg",
                "visibleUrl": "www.kontrolmag.com",
                "title": "Weird Yet effective <b>Beauty</b> Secrets! Shhhh...keep this information <b>...</b>",
                "titleNoFormatting": "Weird Yet effective Beauty Secrets! Shhhh...keep this information ...",
                "originalContextUrl": "http://www.kontrolmag.com/weird-yet-effective-beauty-secrets-shhhh-keep-information-confidental/",
                "content": "Weird Yet effective <b>Beauty</b>",
                "contentNoFormatting": "Weird Yet effective Beauty",
                "tbUrl": "http://t1.gstatic.com/images?q=tbn:ANd9GcTVU58hz0L-3AIhaplT5AjE4AlG9B2UqkwRcy4Zz_Tz3pZBcv5_belbkBoL"
            },
            {
                "GsearchResultClass": "GimageSearch",
                "width": "2544",
                "height": "2193",
                "imageId": "ANd9GcRRfUH9F0mSKkt96HBnjKEWoy8wnuY1FPUTyx-dlo1310w0Ylww6dAaCiPyZw",
                "tbWidth": "150",
                "tbHeight": "129",
                "unescapedUrl": "http://wallpapersinhq.com/images/big/beauty_face-1456083.jpg",
                "url": "http://wallpapersinhq.com/images/big/beauty_face-1456083.jpg",
                "visibleUrl": "makeupandbeauty.com",
                "title": "Top 30 <b>Beauty</b> Tips For Women",
                "titleNoFormatting": "Top 30 Beauty Tips For Women",
                "originalContextUrl": "http://makeupandbeauty.com/top-30-beauty-tips-for-women/",
                "content": "Image Sources:",
                "contentNoFormatting": "Image Sources:",
                "tbUrl": "http://t3.gstatic.com/images?q=tbn:ANd9GcRRfUH9F0mSKkt96HBnjKEWoy8wnuY1FPUTyx-dlo1310w0Ylww6dAaCiPyZw"
            },
	 */
}
