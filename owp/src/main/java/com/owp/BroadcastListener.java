/*
 * 文 件 名:  EditorBodyInfoBroadcastListener.java
 * 版    权:  玄云网络科技有限公司 Ltd. Copyright 2015-09-28,  All rights reserved
 * 描    述:  <描述>
 * 修 改 人:  王琪
 * 修改时间:  2015年11月21日
 */
package com.owp;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.IntentFilter;

import com.owp.api.InterfaceConstants;



public class BroadcastListener {
	public static void registerListener(Context context,BroadcastReceiver mBroadcastReceiver){
		IntentFilter myIntentFilter = new IntentFilter();
		myIntentFilter.addAction(InterfaceConstants.success);
		context.registerReceiver(mBroadcastReceiver, myIntentFilter);
	}

	public static void unRegisterListener(Context context,BroadcastReceiver mBroadcastReceiver){
		context.unregisterReceiver(mBroadcastReceiver);
	}
}
