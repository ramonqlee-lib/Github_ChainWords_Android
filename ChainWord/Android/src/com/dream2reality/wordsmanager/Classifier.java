package com.dream2reality.wordsmanager;

import java.util.List;
import java.util.Map;
/**
 * 单词分类接口
 * @author ramonqlee
 *
 */
public interface Classifier {
	// 进行分类整理
	public Map<String, List<String>> run(List<String> data);
	
	//获取配对的
	public String matchNext(String current, boolean ignoreNextTime,Map<String, List<String>> map);
	
	// 随机提取一个
	public String getRandom( boolean ignoreNextTime,Map<String, List<String>> map);
}
