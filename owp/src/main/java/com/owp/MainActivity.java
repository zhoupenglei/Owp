package com.owp;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.owp.api.InterfaceConstants;
import com.owp.qrcode.CaptureActivity;
import com.owp.webview.BaseWebChromeClient;
import com.owp.webview.BaseWebViewClient;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {
    private LinearLayout photo_ll, scan_ll, record_ll;
    private WebView mWebView;
    //    private String webUrl="http://122.114.53.130:8070/webtebm/upload/list/1";
    private String webUrl = "http://218.31.33.114:7200/dmdisp/";
    private String aboutUrl = "http://218.31.33.114:7200/dmdisp/web/page/footer.html";
    private String contactUrl = "http://218.31.33.114:7200/dmdisp/web/page/contact.html";


    protected LoadingDialog loadingDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setLogo(R.drawable.top_logo);
        toolbar.setTitle("");
        setSupportActionBar(toolbar);
        BroadcastListener.registerListener(this, mBroadcastReceiver);
        loadingDialog = new LoadingDialog(this);
        initView();
        initData();
        initListener();

    }

    private void initView() {
        photo_ll = (LinearLayout) findViewById(R.id.photo_ll);
        scan_ll = (LinearLayout) findViewById(R.id.scan_ll);
        record_ll = (LinearLayout) findViewById(R.id.record_ll);
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
        mWebView.loadUrl(webUrl);
    }

    private void initListener() {
        photo_ll.setOnClickListener(this);
        scan_ll.setOnClickListener(this);
        record_ll.setOnClickListener(this);
    }


    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.photo_ll:
                toPhoto();
                break;
            case R.id.scan_ll:
                toScan();
                break;
            case R.id.record_ll:
                toRecord();
                break;
        }
    }

    private void toPhoto() {
        Intent intent = new Intent(MainActivity.this, PhotoActivity.class);
        startActivity(intent);
    }

    private void toScan() {
        Intent intent = new Intent(MainActivity.this, CaptureActivity.class);
        startActivity(intent);
    }

    private void toRecord() {
        Intent intent = new Intent(MainActivity.this, RecorderActivity.class);
        startActivity(intent);
    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
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

    @Override
    protected void onDestroy() {
        super.onDestroy();
        BroadcastListener.unRegisterListener(this, mBroadcastReceiver);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.about) {
            Intent intent = new Intent(MainActivity.this,WebViewActivity.class);
            intent.putExtra("url",aboutUrl);
            intent.putExtra("title","关于");
            startActivity(intent);
            return true;
        }else if (id == R.id.contact){
            Intent intent = new Intent(MainActivity.this,WebViewActivity.class);
            intent.putExtra("url",contactUrl);
            intent.putExtra("title","联系我们");
            startActivity(intent);
        }

        return super.onOptionsItemSelected(item);
    }

    private BroadcastReceiver mBroadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (InterfaceConstants.success.equals(intent.getAction())) {
                loadingDialog.show();
                mWebView.loadUrl(webUrl);
            }
        }
    };

}
