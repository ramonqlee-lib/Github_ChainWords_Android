package com.idreems.sdk.NetHandler;

import java.util.Map;

import android.content.Context;

import com.yees.sdk.lightvolley.HttpPostDataSource;
import com.yees.sdk.lightvolley.TaskListener;

/**
 * 数据上报模块
 * 
 * @author liming11
 * 
 */
public interface AnalyticsAgent {
  /**
   * 开始一个session，用于记录数据
   * 
   * @param context
   */
  public void startSession(Context context);

  /**
   * 结束记录数据session
   * 
   * @param context
   */
  public void endSession(Context context);

  /**
   * 记录数据
   * 
   * @param Key
   * @param value
   */
  public void logEvent(String key, String value);

  /**
   * 记录数据
   * 
   * @param events
   */
  public void logEvent(Map<String, String> events);
  
  public void postBinary(byte[] binary, TaskListener listener);
  
  public void getBinary(TaskListener listener);

  public void post(HttpPostDataSource dataSource,final TaskListener listener);
}
