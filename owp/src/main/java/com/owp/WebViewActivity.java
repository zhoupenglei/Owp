package com.owp;

import android.content.Context;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.widget.LinearLayout;

import com.owp.webview.BaseWebChromeClient;
import com.owp.webview.BaseWebViewClient;

public class WebViewActivity extends AppCompatActivity {
    private String url ="";
    private String title ="";
    private WebView mWebView;
    protected LoadingDialog loadingDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_web_view);
        url = getIntent().getStringExtra("url");
        title = getIntent().getStringExtra("title");
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setTitle(title);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        loadingDialog = new LoadingDialog(this);
        initView();
        initData();

    }

    private void initView() {

        mWebView = (WebView) findViewById(R.id.webView);
    }

    private void initData() {
        WebSettings webSettings = mWebView.getSettings();
        webSettings.setJavaScriptEnabled(true);
        webSettings.setSavePassword(false);
        webSettings.setSaveFormData(false);
        webSettings.setAllowFileAccess(true);
        webSettings.setCacheMode(WebSettings.LOAD_NO_CACHE);
        webSettings.setSupportZoom(false);
        webSettings.setBuiltInZoomControls(false);
        webSettings.setBlockNetworkImage(false);
        webSettings.setBlockNetworkLoads(false);
        webSettings.setDomStorageEnabled(true);

        mWebView.setVerticalScrollBarEnabled(false);
        mWebView.setWebViewClient(new BaseWebViewClient(this));
        mWebView.setWebChromeClient(new MyWebChromeClient(this));
        mWebView.setOnLongClickListener(new View.OnLongClickListener() {

            @Override
            public boolean onLongClick(View v) {
                return true;
            }
        });
        loadingDialog.show();
        mWebView.loadUrl(url);
    }

    private class MyWebChromeClient extends BaseWebChromeClient {

        private MyWebChromeClient(Context context) {
            super(context);
        }

        @Override
        public void onProgressChanged(WebView view, int newProgress) {
            if (newProgress == 100) {
                loadingDialog.dismiss();
            }
            super.onProgressChanged(view, newProgress);
        }


    }

}
