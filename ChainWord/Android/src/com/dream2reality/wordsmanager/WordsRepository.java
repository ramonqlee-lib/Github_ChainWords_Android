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

import com.dream2reality.utils.AppConstants;
import com.yees.sdk.utils.Logger;
import com.yees.sdk.utils.Utils;

/**
 * 单词管理接口
 * 
 * @author ramonqlee
 * 
 */
public class WordsRepository {
	private static final int MIN_WORD_LENGTH = 4;// 最短word的长度
	private static WordsRepository sWordsRepository = new WordsRepository();

	// private Context mContext;
	private String mFileName;

	private Classifier mCurrentClassifier;

	private List<String> mOriginalWordsList = new ArrayList<String>();
	private Map<String, List<String>> mClassifiedWordsMap = new HashMap<String, List<String>>();

	private WordsRepository() {
	}

	public static WordsRepository sharedInstance(Context context,
			String fileName) {
		// sWordsRepository.mContext = context;
		sWordsRepository.changeFileName(fileName);
		return sWordsRepository;
	}

	/**
	 * 切换使用的文件
	 * 
	 * @param fileName
	 */
	public void changeFileName(String fileName) {
		if (TextUtils.equals(sWordsRepository.mFileName, fileName)) {
			return;
		}

		sWordsRepository.mFileName = fileName;
		sWordsRepository.loadRep(fileName);

		// 对单词进行分类
		if (null != mCurrentClassifier) {
			mClassifiedWordsMap = mCurrentClassifier.run(mOriginalWordsList);
		}
	}

	/**
	 * 使用单词分类器进行单词的处理
	 * 
	 * @param classifier
	 */
	public void applyClassifier(Classifier classifier) {
		if (null == classifier || classifier == mCurrentClassifier) {
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

	public String getRandom(boolean ignoreNextTime) {
		if (null == mCurrentClassifier) {
			return "";
		}
		return mCurrentClassifier
				.getRandom(ignoreNextTime, mClassifiedWordsMap);
	}

	// 将单词加载进数组中 mOriginalWordsList
	private void loadRep(String fileName) {
		// 按照行，将单词读取进来
		if (TextUtils.isEmpty(fileName) || !Utils.fileExists(fileName)) {
			return;
		}
		mOriginalWordsList = new ArrayList<String>();// reset word list
		try {
			File file = new File(fileName);
			FileInputStream fis = new FileInputStream(file);
			BufferedReader input = new BufferedReader(
					new InputStreamReader(fis));
			String line = null;
			while ((line = input.readLine()) != null) {
				List<String> wordList = getWords(line);
				if (null == wordList || wordList.isEmpty()) {
					continue;
				}
				for (String word : wordList) {
					// 忽略空的字符串或者仅仅有一个字母的字符串
					if (TextUtils.isEmpty(word)
							|| word.length() < MIN_WORD_LENGTH) {
						continue;
					}
					// Logger.d(AppConstants.LOG_TAG, word);
					mOriginalWordsList.add(word);
				}
			}
			// fis.close();
			input.close();

			Logger.d(AppConstants.LOG_TAG, "word file name: " + fileName);
			Logger.d(AppConstants.LOG_TAG,
					"word count: " + mOriginalWordsList.size());
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	// 找出单词
	// 以字母开始，非字母结束时停止
	private static List<String> getWords(String value) {
		List<String> ret = new ArrayList<String>();
		if (TextUtils.isEmpty(value)) {
			return ret;
		}

		value = value.toLowerCase(Locale.ENGLISH);
		StringBuilder word = new StringBuilder();
		for (int i = 0; i < value.length(); i++) {
			char ch = value.charAt(i);

			// 找到单词开始字母:没添加过任何内容，并且当前为字母
			// 找到单词结束:添加过内容，并且当前不是字母了
			if (ch >= 'a' && ch <= 'z') {
				word.append(ch);
			} else {//包含不止一个单词
				if (word.length() > 0) {
					ret.add(word.toString());
					word = new StringBuilder();// reset
					continue;
				}
			}
		}
		
		// 是否最后一个还没有添加
		if (word.length()>0) {
			if (ret.isEmpty() || !TextUtils.equals(ret.get(ret.size()-1), word.toString())) {
				ret.add(word.toString());
			}
		}

		return ret;
	}
}
