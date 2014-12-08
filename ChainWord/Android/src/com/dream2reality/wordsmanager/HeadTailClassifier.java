package com.dream2reality.wordsmanager;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Random;

import android.text.TextUtils;

import com.dream2reality.constants.AppConstants;
import com.yees.sdk.utils.Logger;

public class HeadTailClassifier implements Classifier {
	private static HeadTailClassifier sHeadClassifier = new HeadTailClassifier();

	private HeadTailClassifier() {
	}

	public static HeadTailClassifier sharedInstance() {
		return sHeadClassifier;
	}

	@Override
	public Map<String, List<String>> run(List<String> data) {
		Map<String, List<String>> retMap = new HashMap<String, List<String>>();
		// 按照字母进行归类
		// 遍历每个单词，按照首字母，将其归类到相应的map中.map采用字母做key，值是一个单词的列表
		for (String wordString : data) {
			if (TextUtils.isEmpty(wordString)) {
				continue;
			}

			String firstLetter = wordString.substring(0, 1);
			firstLetter = firstLetter.toUpperCase(Locale.ENGLISH);
			if ("A".compareToIgnoreCase(firstLetter) > 0
					|| "Z".compareToIgnoreCase(firstLetter) < 0) {
				continue;
			}

			List<String> valList = retMap.get(firstLetter);
			if (null == valList) {
				valList = new ArrayList<String>();
				retMap.put(firstLetter, valList);
			}
			valList.add(wordString);
		}
		return retMap;
	}

	/**
	 * 得到下一个单词
	 */
	public String matchNext(String current, boolean ignoreNextTime,
			Map<String, List<String>> map) {
		// 得到下一个单词
		// 获取当前单词的最后一个字母，然后从map中获取值的列表，随机取出一个，并根据ignoreNextTime设置是否删除
		if (TextUtils.isEmpty(current) || null == map || map.isEmpty()) {
			return "";
		}
		String lastLetter = current.substring(current.length() - 1,
				current.length());
		lastLetter = lastLetter.toUpperCase(Locale.ENGLISH);
		List<String> vaList = map.get(lastLetter);
		if (null == vaList || vaList.isEmpty()) {
			// 没有了，则删除该项目
			map.remove(lastLetter);
			Logger.d(AppConstants.LOG_TAG, "no word found");
			return "";
		}

		int index = new Random().nextInt(vaList.size()) % vaList.size();
		String ret = vaList.remove(index);
		if (ignoreNextTime) {
			// 没有了，则删除该项目
			if (vaList.isEmpty()) {
				Logger.d(AppConstants.LOG_TAG,
						"current word list is removed and no item left");
				map.remove(lastLetter);
			}
		}
		return ret;
	}

	@Override
	public String getRandom(boolean ignoreNextTime,
			Map<String, List<String>> map) {
		//  从当前的此表中，随机获取一个单词返回
		if (null == map || map.isEmpty()) {
			return "";
		}

		Iterator<String> iter = map.keySet().iterator();
		while (iter.hasNext()) {
			String key = iter.next();
			if (TextUtils.isEmpty(key)) {
				continue;
			}
			List<String> vaList = map.get(key);
			if (null == vaList || vaList.isEmpty()) {
				continue;
			}
			
			int index = new Random().nextInt(vaList.size()) % vaList.size();
			String ret = vaList.remove(index);
			if (ignoreNextTime) {
				// 没有了，则删除该项目
				if (vaList.isEmpty()) {
					Logger.d(AppConstants.LOG_TAG,
							"current word list is removed and no item left");
					map.remove(key);
				}
			}
			return ret;
		}
		return "";
	}
}
