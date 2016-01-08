package com.owp;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.RequestParams;
import com.owp.api.ApiHttpClient;
import com.owp.api.InterfaceConstants;
import com.owp.audio.VoiceCallBack;
import com.owp.audio.VoiceManager;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;

import cn.finalteam.galleryfinal.model.PhotoInfo;
import cz.msebera.android.httpclient.Header;

public class RecorderActivity extends AppCompatActivity {
    private LinearLayout mLayoutRecord;
    private RelativeLayout mLayoutPlay;
    private Button mBtRec;
//    private Button mBtPlay;
    private Button mBtSave;
    private ListView list;
    private ArrayAdapter<String> adapter;
    private List<String> pathList = new ArrayList<>();

    private VoiceManager voiceManager;
    private String mRecPath = "";
    protected LoadingDialog loadingDialog;
    int  position = 0;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recorder);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);


        mLayoutRecord = (LinearLayout) findViewById(R.id.layout_record);
        mLayoutPlay = (RelativeLayout) findViewById(R.id.layout_play);
        list = (ListView) findViewById(R.id.list);
        adapter = new ArrayAdapter<String>(this,android.R.layout.simple_list_item_1,pathList);
        list.setAdapter(adapter);
        mBtRec = (Button) findViewById(R.id.button_rec);
//        mBtPlay = (Button) findViewById(R.id.button_play);
        mBtSave = (Button) findViewById(R.id.button_save);
        loadingDialog = new LoadingDialog(this);
        voiceManager = new VoiceManager(RecorderActivity.this, "/com.youmu.voicemanager/audio");

        voiceManager.setVoiceListener(new VoiceCallBack() {
            @Override
            public void voicePath(String path) {
                mRecPath = path;
                pathList.add(mRecPath);
                adapter.notifyDataSetChanged();
            }

            @Override
            public void recFinish() {
//                mBtPlay.setVisibility(View.VISIBLE);
                mBtSave.setVisibility(View.VISIBLE);
//                pathList.add(mRecPath);
//                adapter.notifyDataSetChanged();
            }
        });


        mBtRec.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                mLayoutRecord.setVisibility(View.VISIBLE);
                mLayoutPlay.setVisibility(View.GONE);
//                mBtPlay.setVisibility(View.INVISIBLE);
                mBtSave.setVisibility(View.INVISIBLE);

                voiceManager.sessionRecord(true);
            }
        });

//        mBtPlay.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                mLayoutRecord.setVisibility(View.GONE);
//                mLayoutPlay.setVisibility(View.VISIBLE);
//                voiceManager.sessionPlay(true, mRecPath);
//            }
//        });
        list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                mLayoutRecord.setVisibility(View.GONE);
                mLayoutPlay.setVisibility(View.VISIBLE);
                voiceManager.sessionPlay(true, pathList.get(position));
            }
        });

        mBtSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                loadingDialog.show();
//                File myFile = new File(mRecPath);
//                RequestParams params = new RequestParams();
//                params.put("uploadType", "2");
//                try {
//                    params.put("files", myFile);
//                } catch(FileNotFoundException e) {
//                    Toast.makeText(RecorderActivity.this,"文件不存在！",Toast.LENGTH_SHORT).show();
//                }
//                ApiHttpClient.post(params, responseHandler);
                if (pathList.size() > 0) {
                    loadingDialog.show();
                    for (String path : pathList){
                        position++;
                        File myFile = new File(path);
                        RequestParams params = new RequestParams();
                        params.put("uploadType", "2");
                        try {
                            params.put("files", myFile);
                        } catch(FileNotFoundException e) {
                            Toast.makeText(RecorderActivity.this,"录音不存在！",Toast.LENGTH_SHORT).show();
                        }
                        ApiHttpClient.post(params, responseHandler);
                    }
                }
            }
        });
    }
    protected AsyncHttpResponseHandler responseHandler = new AsyncHttpResponseHandler() {

        @Override
        public void onSuccess(int statusCode, Header[] headers, byte[] responseBytes) {
//            loadingDialog.dismiss();
//            Toast.makeText(RecorderActivity.this, "保存成功", Toast.LENGTH_SHORT).show();
//            Intent intent = new Intent();
//            intent.setAction(InterfaceConstants.success);
//            sendBroadcast(intent);
//            finish();
            if (position == pathList.size()){
                position = 0;
                loadingDialog.dismiss();
                Toast.makeText(RecorderActivity.this, "保存成功", Toast.LENGTH_SHORT).show();
                Intent intent = new Intent();
                intent.setAction(InterfaceConstants.success);
                sendBroadcast(intent);
                finish();
            }
        }

        @Override
        public void onFailure(int arg0, Header[] arg1, byte[] arg2,
                              Throwable arg3) {
            loadingDialog.dismiss();
            Toast.makeText(RecorderActivity.this,"保存失败，请重试！",Toast.LENGTH_SHORT).show();
            arg3.printStackTrace();
        }
    };
}
