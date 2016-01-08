package com.audiovideo;

import android.app.Activity;
import android.graphics.PixelFormat;
import android.hardware.Camera;
import android.media.MediaRecorder;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.Toast;

import java.io.File;
import java.io.IOException;

public class VideoActivity extends Activity implements SurfaceHolder.Callback{
    private Button mVideoStartBtn;
    private Button mVideoStopBtn;
    private File mRecVideoFile; //录制的视频文件
    private File mRecVideoPath; //录制的视频文件
    private MediaRecorder mediarecorder; //MediaRecorder对象
    private SurfaceHolder surfaceHolder;
    private SurfaceView surfaceview;
    private String strTemp = "video_";//文件名前缀
    private Camera camera; //相机预览
    private static final String TAG = "CAMERA_TUTORIAL";
    private boolean previewRunning;
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);// 去掉标题栏
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);// 设置全屏
        // 选择支持半透明模式,在有surfaceview的activity中使用。
        getWindow().setFormat(PixelFormat.TRANSLUCENT);
        setContentView(R.layout.activity_video);
        initRecVideoPath(); //获取视频存储的路径
        initSurfaceView(); //surfaceView
        initButton();
    }
    private boolean initRecVideoPath() {
        String path;
        if (sdcardIsValid()) { //是否有SD卡
            path = Environment.getExternalStorageDirectory().toString()
                    + File.separator + "recordVideo";// 得到SD卡的路径，sd卡的record文件夹
        } else {
            path = Environment.getRootDirectory().toString()
                    + File.separator + "recordVideo";//手机内存路径
        }
        mRecVideoPath = new File(path);
        if (!mRecVideoPath.exists()) { //文件夹是否存在，否 则创建
            mRecVideoPath.mkdirs();
        }
        return mRecVideoPath != null;
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
    //initSurfaceView
    private void initSurfaceView() {
        surfaceview = (SurfaceView) this.findViewById(R.id.surfaceview);
        SurfaceHolder holder = surfaceview.getHolder();// 取得holder对象
        holder.addCallback(VideoActivity.this); // holder加入回调接口
        // 设置显示器类型，setType必须设置
        holder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
    }
    private void initButton(){
        mVideoStartBtn = (Button)findViewById(R.id.startIv);
        mVideoStopBtn = (Button)findViewById(R.id.stopIv);
        //开始按钮事件监听
        mVideoStartBtn.setOnClickListener(new Button.OnClickListener() {
            @Override
            public void onClick(View arg0) {
                //按钮状态,false让按钮失效，
                mVideoStartBtn.setEnabled(false);
                mVideoStopBtn.setEnabled(true);
                mHandler.sendEmptyMessage(MSG_RECORD);
            }
        });
        //停止按钮事件监听
        mVideoStopBtn.setOnClickListener(new Button.OnClickListener() {
            @Override
            public void onClick(View arg0) {
                mVideoStartBtn.setEnabled(true);
                mVideoStopBtn.setEnabled(false);
                mHandler.sendEmptyMessage(MSG_STOP);
            }
        });
        //初始的按钮状态
        mVideoStartBtn.setEnabled(true);
        mVideoStopBtn.setEnabled(false);
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
    /**
     * 录制视频
     */
    private void startRecord() {
        mediarecorder = new MediaRecorder();// 创建mediarecorder对象
        mediarecorder.reset();
        camera.unlock();
        mediarecorder.setCamera(camera);
        // 设置录制视频源为Camera(相机)
        mediarecorder.setVideoSource(MediaRecorder.VideoSource.CAMERA);
        // 设置录制完成后视频的封装格式THREE_GPP为3gp.MPEG_4为mp4
        mediarecorder
                .setOutputFormat(MediaRecorder.OutputFormat.MPEG_4);
        // 设置录制的视频编码h263 h264
        mediarecorder.setVideoEncoder(MediaRecorder.VideoEncoder.H264);
        mediarecorder.setPreviewDisplay(surfaceHolder.getSurface());
        // 设置视频文件输出的路径
        try {
            mRecVideoFile = File.createTempFile(strTemp, ".mp4", mRecVideoPath);
        } catch (IOException e) {
            e.printStackTrace();
        }
        mediarecorder.setOutputFile(mRecVideoFile.getAbsolutePath());
        try {
            // 准备录制
            mediarecorder.prepare();
            // 开始录制
            mediarecorder.start();
        } catch (IllegalStateException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }
    private void stopRecord() {
        if (mediarecorder != null) {
            // 停止录制
            mediarecorder.stop();
            // 释放资源
            mediarecorder.release();
            mediarecorder = null;
        }
    }
    /**
     * 实现SurfaceHolder.Callback接口
     * SurfaceView启动时or初次实例化、预览界面被创建时，该方法被调用
     */
    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        camera = Camera.open(); //开启摄像头，后摄像头
        try {
            Log.i(TAG, "SurfaceHolder.Callback：surface Created");
            //设置surface用于当前预览
            camera.setPreviewDisplay(surfaceHolder);
        } catch (Exception ex) {
            if(null != camera) {
                camera.release();
                camera = null;
            }
        }
        surfaceHolder = holder;
    }
    /**
     * 当SurfaceView预览界面的格式和大小发生改变时，该方法被调用
     */
    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width,
                               int height) {
        surfaceHolder = holder;
        if (previewRunning){
            camera.stopPreview(); //stop camera
        }
        //返回当前camera的settings
        Camera.Parameters p = camera.getParameters();
        p.set("orientation", "portrait");
        p.set("rotation", 90);//镜头角度转90度（默认摄像头是横拍
        camera.setDisplayOrientation(90);
        camera.setParameters(p); //打开预览画面
        try {
            camera.setPreviewDisplay(holder);
            camera.startPreview();
            previewRunning = true;
        } catch (IOException e) {
            Log.e(TAG, e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * SurfaceView销毁时，该方法被调用
     * @param holder
     */
    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        surfaceview = null;
        surfaceHolder = null;
        mediarecorder = null;
        camera.setPreviewCallback(null);//须设置在前
        camera.stopPreview();
        previewRunning = false;
        camera.release();
    }
}