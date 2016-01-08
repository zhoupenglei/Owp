package com.owp.qrcode;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.RequestHandle;
import com.loopj.android.http.RequestParams;
import com.loopj.android.http.ResponseHandlerInterface;

import cz.msebera.android.httpclient.Header;
import cz.msebera.android.httpclient.HttpEntity;

import com.owp.LoadingDialog;
import com.owp.R;
import com.owp.api.ApiHttpClient;
import com.owp.api.InterfaceConstants;

import java.net.URLDecoder;

import butterknife.Bind;
import butterknife.ButterKnife;

public class ResultActivity extends AppCompatActivity {
    public static final String BUNDLE_KEY_SCAN_RESULT = "BUNDLE_KEY_SCAN_RESULT";
    @Bind(R.id.btn_save_result)
    Button btnSaveResult;

    private TextView resultContent;

    private String mResultStr;
    protected LoadingDialog loadingDialog;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_result);
        ButterKnife.bind(this);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        mResultStr = getIntent().getStringExtra("BUNDLE_KEY_SCAN_RESULT");
        resultContent = (TextView) findViewById(R.id.result_content);
        resultContent.setText(mResultStr);
        loadingDialog = new LoadingDialog(this);
        initListener();
    }

    private void initListener() {
        btnSaveResult.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                loadingDialog.show();
                RequestParams params = new RequestParams();
                params.put("uploadType", "3");
                params.put("codeResult", mResultStr);
                ApiHttpClient.post(params, responseHandler);
            }
        });
    }

    protected AsyncHttpResponseHandler responseHandler = new AsyncHttpResponseHandler() {

        @Override
        public void onSuccess(int statusCode, Header[] headers, byte[] responseBytes) {
            loadingDialog.dismiss();
            Toast.makeText(ResultActivity.this,"保存成功",Toast.LENGTH_SHORT).show();
            Intent intent = new Intent();
            intent.setAction(InterfaceConstants.success);
            sendBroadcast(intent);
            finish();
        }

        @Override
        public void onFailure(int arg0, Header[] arg1, byte[] arg2,
                              Throwable arg3) {
            loadingDialog.dismiss();
            Toast.makeText(ResultActivity.this,"保存失败，请重试！",Toast.LENGTH_SHORT).show();
            arg3.printStackTrace();
        }
    };
}
