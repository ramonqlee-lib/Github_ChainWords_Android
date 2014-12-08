package com.idreems.sdk.common.runners;

import com.yees.sdk.lightvolley.TaskListener;

public class BaseRunner {
	protected TaskListener listener;

	public void setTaskListener(TaskListener listener) {
		this.listener = listener;
	}
}
