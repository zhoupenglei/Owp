package com.audiovideo;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.Button;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.Toast;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@SuppressLint({ "SimpleDateFormat", "DefaultLocale" })
public class AudioActivity extends Activity {
    private Button mAudioStartBtn;
    private Button mAudioStopBtn;
    private File mRecAudioFile; // 录制的音频文件
    private File mRecAudioPath; // 录制的音频文件路徑
    private MediaRecorder mMediaRecorder;// MediaRecorder对象
    private String strTempFile = "radio_";// 音频文件名的前缀
    private ListView mList;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_audio);
        initRecAudioPath();//初始化，获取path
        initList();//录音文件列表
        initButton();//录音按钮点击录制音频
    }
    private boolean initRecAudioPath() {
        String path;
        if (sdcardIsValid()) { //是否有SD卡
            path = Environment.getExternalStorageDirectory().toString()
                    + File.separator + "recordAudio";// 得到SD卡的路径，sd卡的record文件夹
        } else {
            path = Environment.getRootDirectory().toString()
                    + File.separator + "recordAudio";//手机内存路径
        }
        mRecAudioPath = new File(path);
        if (!mRecAudioPath.exists()) { //文件夹是否存在，否 则创建
            mRecAudioPath.mkdirs();
        }
        return mRecAudioPath != null;
    }
    //是否存在SD卡
    private boolean sdcardIsValid() {
        if (Environment.getExternalStorageState().equals(
                android.os.Environment.MEDIA_MOUNTED)) {
            return true;
        } else {
            Toast.makeText(getBaseContext(), "没有SD卡", Toast.LENGTH_LONG).show();
        }
        return false;
    }

    private void initList() {
        mList = (ListView) findViewById(R.id.simplelist);
        setListEmptyView(); //当列表为空时
        setOnItemClickListener(); //列表item点击事件
        setOnItemLongClickListener(); //长按item删除或上传
        musicList();
    }
    private void setListEmptyView() {
        View emptyView = findViewById(R.id.empty);
        mList.setEmptyView(emptyView);
    }
    //点击播放
    private void setOnItemClickListener() {
        mList.setOnItemClickListener(new OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> l, View v, int position, long id) {
                List<Map<String, Object>> listdata = (List<Map<String, Object>>) mList.getTag();
                Map<String, Object> map = listdata.get(position);//获取位置
                String name = (String) map.get("text"); //文件名
                File playfile = new File(mRecAudioPath.getAbsolutePath()
                        + File.separator + name); //文件路径
                playMusic(playfile);
            }
        });
    }
    //播放录音文件
    private void playMusic(File file) {
        Intent intent = new Intent();
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.setAction(android.content.Intent.ACTION_VIEW);
        intent.setDataAndType(Uri.fromFile(file), "audio"); //文件类型
        startActivity(intent);
    }
    //长按删除或上传
    private void setOnItemLongClickListener() {
        mList.setOnItemLongClickListener(new OnItemLongClickListener() {
            String mName = null;//文件名
            int mPosition = 0; //item位置

            @Override
            public boolean onItemLongClick(AdapterView<?> l, View v, int position, long id) {
                List<Map<String, Object>> listdata = (List<Map<String, Object>>) l.getTag();
                Map<String, Object> map = listdata.get(position);
                mName = (String) map.get("text");//文件名
                mPosition = position;
                new AlertDialog.Builder(AudioActivity.this)
                        .setIcon(android.R.drawable.ic_dialog_alert)
                        .setTitle("选择操作").setMessage("请选择删除或上传\n\"" + mName + "\"")
                        .setNegativeButton("删除", new OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int whichButton) {
                                new File(mRecAudioPath.getAbsolutePath()
                                        + File.separator + mName).delete();
                                deleteItem(mPosition);//删除item
                            }
                        }).setPositiveButton("上传", new OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int whichButton) {
                        File file = new File(mRecAudioPath.getAbsolutePath()
                                + File.separator + mName);
                    }
                }).show();
                return true;
            }
        });
    }
    //添加列表item
    private void addItem(File item) {
        List<Map<String, Object>> tag = (List<Map<String, Object>>) mList.getTag();
        List<Map<String, Object>> listdata = tag;
        listdata.add(getOneItem(item));
        setListAdapter(listdata);
    }
    //删除列表item
    public void deleteItem(int position) {
        List<Map<String, Object>> listdata = (List<Map<String, Object>>) mList.getTag();
        listdata.remove(position);
        setListAdapter(listdata);
    }
    //set adapter
    private void setListAdapter(List<Map<String, Object>> listdata) {
        SimpleAdapter adapter = new SimpleAdapter(getBaseContext(), listdata,
                R.layout.audio_item, new String[] { "text","text_length","text_time" },
                new int[] { R.id.text,R.id.text_length,R.id.text_time });
        mList.setAdapter(adapter);
    }
    //获取文件信息填入item
    private Map<String, Object> getOneItem(File file) {
        Map<String, Object> map = new HashMap<String, Object>();
        map.put("text", file.getName()); //文件名
        map.put("text_length", GetFileBuildTime(file));//文件大小
        map.put("text_time", GetFilePlayTime(file)); //文件时间长度
        return map;
    }
    //获取时间长度
    private String GetFilePlayTime(File file){
        Date date;
        SimpleDateFormat sy1;
        String dateFormat = "error";
        try {
            sy1 = new SimpleDateFormat("HH:mm:ss");//设置为时分秒的格式
            MediaPlayer mediaPlayer;//使用媒体库获取播放时间
            mediaPlayer = MediaPlayer.create(getBaseContext(), Uri.parse(file.toString()));
            //使用Date格式化播放时间mediaPlayer.getDuration()
            date = sy1.parse("00:00:00");
            date.setTime(mediaPlayer.getDuration() + date.getTime());//用消除date.getTime()时区差
            dateFormat = sy1.format(date);

            mediaPlayer.release();

        } catch (ParseException e) {
            e.printStackTrace();
        }
        return dateFormat;
    }
    private String GetFileBuildTime(File file) {
        Date date = new Date(file.lastModified());//最后更新的时间
        String t;
        SimpleDateFormat sy2 = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");//设置年月日时分秒
        t = sy2.format(date);
        return t;
    }
    //播放列表显示
    public void musicList() {
        List<Map<String, Object>> listdata = getData();
        setListAdapter(listdata);
        mList.setTag(listdata);
    }
    //过滤文件类型
    class MusicFilter implements FilenameFilter {
        public boolean accept(File dir, String name) {
            return (name.endsWith(".amr"));//amr文件
        }
    }
    private MusicFilter mFilter = new MusicFilter();
    private List<Map<String, Object>> getData() {
        List<Map<String, Object>> list = new ArrayList<Map<String, Object>>();
        File home = mRecAudioPath;//存储路径
        if (home != null) {
            File[] files = home.listFiles(mFilter); //存储路径里的amr文件
            if (files != null && files.length > 0) {
                for (File file : files) {
                    list.add(getOneItem(file));
                }
            }
        }
        return list;
    }

    //点击按钮录音
    private void initButton() {
        mAudioStartBtn = (Button) findViewById(R.id.AudioStartBtn);
        mAudioStopBtn = (Button) findViewById(R.id.AudioStopBtn);
        //开始按钮事件监听
        mAudioStartBtn.setOnClickListener(new Button.OnClickListener() {
            @Override
            public void onClick(View arg0) {
                //按钮状态,false让按钮失效，
                mAudioStartBtn.setEnabled(false);
                mAudioStopBtn.setEnabled(true);
                mHandler.sendEmptyMessage(MSG_RECORD);
            }
        });
        //停止按钮事件监听
        mAudioStopBtn.setOnClickListener(new Button.OnClickListener() {
            @Override
            public void onClick(View arg0) {
                mAudioStartBtn.setEnabled(true);
                mAudioStopBtn.setEnabled(false);
                mHandler.sendEmptyMessage(MSG_STOP);
            }
        });
        //初始的按钮状态
        mAudioStartBtn.setEnabled(true);
        mAudioStopBtn.setEnabled(false);
    }
    private static final int MSG_RECORD = 0;
    private static final int MSG_STOP = 1;
    private Handler mHandler = new Handler() {
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case MSG_RECORD:
                    startRecord();
                    break;
                case MSG_STOP:
                    stopRecord();
                    break;
                default:
                    break;
            }
        };
    };

    //开始录音
    private void startRecord() {
        try {
            mAudioStartBtn.setEnabled(false);
            mAudioStopBtn.setEnabled(true);
            //实例化MediaRecorder对象
            mMediaRecorder = new MediaRecorder();
            mMediaRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);// 设置麦克风
            //设置编码格式
            mMediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.DEFAULT);
            //设置音频文件的编码
            mMediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.DEFAULT);
            //设置输出文件的路径
            try {
                mRecAudioFile = File.createTempFile(strTempFile, ".amr", mRecAudioPath);
            } catch (Exception e) {
                e.printStackTrace();
            }
            mMediaRecorder.setOutputFile(mRecAudioFile.getAbsolutePath());
            mMediaRecorder.prepare();//准备
            mMediaRecorder.start();//开始
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    //停止录音
    private void stopRecord() {
        if (mRecAudioFile != null) {
            mAudioStartBtn.setEnabled(true);
            mAudioStopBtn.setEnabled(false);
            mMediaRecorder.stop();
            //将录音文件添加到List中
            addItem(mRecAudioFile);
            //释放MediaRecorder
            mMediaRecorder.release();
            mMediaRecorder = null;
        }
    }
}
