package com.color.speechbubble;

import android.content.Context;
import android.text.TextUtils;

import com.dream2reality.wordsmanager.HeadTailClassifier;
import com.dream2reality.wordsmanager.WordsRepository;
import com.yees.sdk.utils.Config;
import com.yees.sdk.utils.Utils;

/**
 * Utility is a just an ordinary class to have some Utility methods
 * 
 * @author Adil Soomro
 * 
 */

public class Utility {
	public static final String FILE_NAME_KEY = "FILE_NAME_KEY";
	public static final String CURRENT_FILE_NAME = "defaultLevel.txt";

	public static final String[] sender = new String[] { "Lalit", "RobinHood",
			"Captain", "Vice Captain", "PurpleDroid", "HotVerySpicy",
			"Dharmendra", "PareshMayani", "Abhi", "SpK", "CapDroid" };

	/**
	 * TODO 获取单词解释(用户输入时，做校验用)
	 * @param word
	 * @return 正确的话，返回非空
	 */
	public static String getWordMeaning(String word)
	{
		return "";
	}
	/**
	 * 获取files目录
	 * 
	 * @param context
	 * @return
	 */
	private static String getFilesDir(Context context) {
		if (null == context) {
			return "";
		}
		return context.getFilesDir().getAbsolutePath();
	}

	/**
	 * 自动挑选一个单词
	 * 
	 * @return
	 */
	public static final String getNewWord(Context context) {
		// 自动挑选一个单词
		String fileName = getGameFileName(context);

		// 在此输入文本
		WordsRepository repository = WordsRepository.sharedInstance(context,
				fileName);
		repository.applyClassifier(HeadTailClassifier.sharedInstance());
		return repository.getRandom(true);
	}

	/**
	 * 根据传入的单词，自动挑选一个单词
	 * 
	 * @param firstWord
	 * @return
	 */
	public static final String getChainWord(Context context, String firstWord) {
		String fileName = getGameFileName(context);

		// 在此输入文本
		WordsRepository repository = WordsRepository.sharedInstance(context,
				fileName);
		repository.applyClassifier(HeadTailClassifier.sharedInstance());
		return repository.matchNext(firstWord, true);
	}

	private static String getGameFileName(Context context) {
		String fileName = Config.sharedInstance(context).getString(
				FILE_NAME_KEY);
		if (TextUtils.isEmpty(fileName) || !Utils.fileExists(fileName)) {
			fileName = setDefaultFile(context);
		}
		return fileName;
	}

	private static String setDefaultFile(Context context) {
		// 从当前assets目录下释放一个文件
		String dir = Utility.getFilesDir(context);
		Utils.extractAssetsFile(context, dir, CURRENT_FILE_NAME, true);
		String fileName = dir + "/" + CURRENT_FILE_NAME;
		Config.sharedInstance(context).putString(FILE_NAME_KEY, fileName);
		return fileName;
	}

}
