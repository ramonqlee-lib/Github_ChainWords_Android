package com.idreems.sdk.netmodel;

import com.yees.sdk.lightvolley.TaskResponse;
/**
 * 已经解析过的任务对象类
 * @author ramonqlee
 *
 */
public class ParsedTaskReponse implements TaskResponse{
	public ParsedTaskReponse(Object object) {
		parsedObject = object;
	}
	public Object parsedObject;
}
