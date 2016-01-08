package com.owp.api;

import android.content.Context;

import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.RequestParams;

/**
 * API客户端接口：用于访问网络数据
 */
public class ApiHttpClient {
	private static AsyncHttpClient client = new AsyncHttpClient();

	public static AsyncHttpClient getIntance() {
        if (client == null){
            client = new AsyncHttpClient();
        }
        client.setTimeout(10 * 1000);
        client.setMaxConnections(5);
		return client;
	}

	public static void post(RequestParams params,
			AsyncHttpResponseHandler responseHandler) {
		client.post(getAbsoluteUrl(), params, responseHandler);
	}

	public static void post(Context context, RequestParams params,
			AsyncHttpResponseHandler responseHandler) {
		client.post(context, getAbsoluteUrl(), params, responseHandler);
	}

    public static void get(Context context, String url,
                            AsyncHttpResponseHandler responseHandler) {
        client.get(context, url,responseHandler);
    }


	private static String getAbsoluteUrl() {
		client.setTimeout(10 * 1000);
		client.setMaxConnections(5);
		return InterfaceConstants.BASE_API;
	}
}
