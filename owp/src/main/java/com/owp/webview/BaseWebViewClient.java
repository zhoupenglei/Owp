package com.owp.webview;

import android.content.Context;
import android.webkit.WebView;
import android.webkit.WebViewClient;


public class BaseWebViewClient extends WebViewClient {
    private Context context;



    public BaseWebViewClient(Context context) {
        this.context = context;
    }
    @Override
    public boolean shouldOverrideUrlLoading(WebView view, String url) {
        view.loadUrl(url);
        return true;
    }

    @Override
    public void onReceivedError(WebView view, int errorCode, String description, String failingUrl) {
    }




}
