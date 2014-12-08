package com.dream2reality.wordsmanager;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import android.content.Context;
import android.text.TextUtils;

import com.dream2reality.constants.AppConstants;
import com.yees.sdk.utils.Logger;
import com.yees.sdk.utils.Utils;

/**
 * 单词管理接口
 * 
 * @author ramonqlee
 * 
 */
public class WordsRepository {
	private static WordsRepository sWordsRepository = new WordsRepository();

//	private Context mContext;
	private String mFileName;

	private Classifier mCurrentClassifier;

	private List<String> mOriginalWordsList = new ArrayList<String>();
	private Map<String, List<String>> mClassifiedWordsMap = new HashMap<String, List<String>>();

	private WordsRepository() {
	}

	public static WordsRepository sharedInstance(Context context,
			String fileName) {
//		sWordsRepository.mContext = context;
		if (!TextUtils.equals(sWordsRepository.mFileName, fileName)) {
			sWordsRepository.mFileName = fileName;
			sWordsRepository.loadRep(fileName);
		}
		return sWordsRepository;
	}

	/**
	 * 使用单词分类器进行单词的处理
	 * 
	 * @param classifier
	 */
	public void applyClassifier(Classifier classifier) {
		if (null == classifier || classifier == mCurrentClassifier ) {
			return;
		}
		mCurrentClassifier = classifier;
		mClassifiedWordsMap = mCurrentClassifier.run(mOriginalWordsList);
	}

	/**
	 * 根据当前单词，获取下一个单词
	 * 
	 * @param current
	 * @param ignoreNextTime
	 * @return
	 */
	public String matchNext(String current, boolean ignoreNextTime) {
		// 根据规则，查找下一个
		if (null == mCurrentClassifier) {
			return "";
		}
		return mCurrentClassifier.matchNext(current, ignoreNextTime,
				mClassifiedWordsMap);
	}
	
	public String getRandom(boolean ignoreNextTime)
	{
		if (null == mCurrentClassifier) {
			return "";
		}
		return mCurrentClassifier.getRandom(ignoreNextTime,
				mClassifiedWordsMap);
	}

	//  将单词加载进数组中 mOriginalWordsList
	private void loadRep(String fileName) {
		// 按照行，将单词读取进来
		if (TextUtils.isEmpty(fileName) || !Utils.fileExists(fileName)) {
			return;
		}
		try {
			File file = new File(fileName);
			FileInputStream fis = new FileInputStream(file);
			BufferedReader input = new BufferedReader(
					new InputStreamReader(fis));
			String line = null;
			while ((line = input.readLine()) != null) {
				String word = getWord(line);
				// 忽略空的字符串或者仅仅有一个字母的字符串
				if (TextUtils.isEmpty(word) || 1 == word.length()) {
					continue;
				}
				Logger.d(AppConstants.LOG_TAG, word);
				mOriginalWordsList.add(word);
			}
//			fis.close();
			input.close();
			
			Logger.d(AppConstants.LOG_TAG, "word count: "+mOriginalWordsList.size());
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	//找出单词
	// 以字母开始，非字母结束时停止
	private static String getWord(String value)
	{
		if (TextUtils.isEmpty(value)) {
			return "";
		}
		value = value.toLowerCase(Locale.ENGLISH);
		StringBuilder ret = new StringBuilder();
		for (int i = 0; i < value.length(); i++) {
			char ch = value.charAt(i);
			
			// 找到单词开始字母:没添加过任何内容，并且当前为字母
			// 找到单词结束:添加过内容，并且当前不是字母了
			if (ch>='a' && ch <='z') {
				ret.append(ch);
			}
			else {
				if (ret.length() > 0) {
					break;
				}
			}
		}
		
		return ret.toString();
	}
}
