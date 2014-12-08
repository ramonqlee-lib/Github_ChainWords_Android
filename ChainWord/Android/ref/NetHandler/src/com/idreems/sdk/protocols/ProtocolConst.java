package com.idreems.sdk.protocols;

public interface ProtocolConst {
	public static final String NET_LOG_TAG = "NetComm";
	// 客户端的类型（用于注册pad时，区分不同的客户端用）
	// 1：司机app，2：候车小姐app，3：车辆调度员app
	public static final int APP_TYPE_DRIVER = 1;
	public static final int APP_TYPE_RECEPTIONIST = 2;
	public static final int APP_TYPE_DISPATCHER = 3;
	
}
