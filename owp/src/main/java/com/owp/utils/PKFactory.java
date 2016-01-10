package com.owp.utils;

import android.content.Context;
import android.telephony.TelephonyManager;

/**
 * Created by xy on 2016/1/10.
 */
public class PKFactory {
    public static String getPKID(Context context){
        String id = getIMEI(context)+System.currentTimeMillis()+(Math.random()*9000+1000);
        return id;

    }

    /**
     * 获取IMEI
     *
     * @return
     */
    public static String getIMEI(Context context) {
        String imeiStr =((TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE)).getDeviceId();
        return imeiStr;
    }

}
