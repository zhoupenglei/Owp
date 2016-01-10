package com.owp;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.GridView;
import android.widget.Toast;

import com.baoyz.actionsheet.ActionSheet;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.RequestParams;
import com.nostra13.universalimageloader.cache.disc.naming.Md5FileNameGenerator;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.nostra13.universalimageloader.core.assist.QueueProcessingType;
import com.owp.adapter.ChoosePhotoListAdapter;
import com.owp.api.ApiHttpClient;
import com.owp.api.InterfaceConstants;
import com.owp.loader.UILImageLoader;
import com.owp.utils.PKFactory;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;
import cn.finalteam.galleryfinal.CoreConfig;
import cn.finalteam.galleryfinal.FunctionConfig;
import cn.finalteam.galleryfinal.GalleryFinal;
import cn.finalteam.galleryfinal.ThemeConfig;
import cn.finalteam.galleryfinal.model.PhotoInfo;
import cz.msebera.android.httpclient.Header;

public class PhotoActivity extends AppCompatActivity {
    @Bind(R.id.btn_open_gallery)
    Button mBtnOpenGallery;
    @Bind(R.id.picture_gv)
    GridView pictureGv;
    @Bind(R.id.btn_save_gallery)
    Button btnSaveGallery;
    private ChoosePhotoListAdapter mChoosePhotoListAdapter;
    private List<PhotoInfo> mPhotoList;

    private final int REQUEST_CODE_CAMERA = 1000;
    private final int REQUEST_CODE_GALLERY = 1001;
    protected LoadingDialog loadingDialog;
    int  position = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_photo);
        ButterKnife.bind(this);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        loadingDialog = new LoadingDialog(this);
        initData();
        initListener();
        initImageLoader(this);

    }

    private void initData() {
        mPhotoList = new ArrayList<>();
        mChoosePhotoListAdapter = new ChoosePhotoListAdapter(this, mPhotoList);
        pictureGv.setAdapter(mChoosePhotoListAdapter);

    }

    private void initListener() {
        mBtnOpenGallery.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ThemeConfig themeConfig = ThemeConfig.DEFAULT;

                FunctionConfig.Builder functionConfigBuilder = new FunctionConfig.Builder();
                cn.finalteam.galleryfinal.ImageLoader imageLoader = new UILImageLoader();
                functionConfigBuilder.setMutiSelectMaxSize(4);
                functionConfigBuilder.setEnableEdit(false);
                functionConfigBuilder.setEnableCrop(false);
                functionConfigBuilder.setEnableRotate(false);
                functionConfigBuilder.setEnableCamera(true);
                functionConfigBuilder.setEnablePreview(true);
                functionConfigBuilder.setSelected(mPhotoList);//添加过滤集合
                final FunctionConfig functionConfig = functionConfigBuilder.build();
                CoreConfig coreConfig = new CoreConfig.Builder(PhotoActivity.this, imageLoader, themeConfig)
                        .setDebug(BuildConfig.DEBUG)
                        .setFunctionConfig(functionConfig)
                        .build();
                GalleryFinal.init(coreConfig);
                ActionSheet.createBuilder(PhotoActivity.this, getSupportFragmentManager())
                        .setCancelButtonTitle("取消")
                        .setOtherButtonTitles("相册", "拍照")
                        .setCancelableOnTouchOutside(true)
                        .setListener(new ActionSheet.ActionSheetListener() {
                            @Override
                            public void onDismiss(ActionSheet actionSheet, boolean isCancel) {

                            }

                            @Override
                            public void onOtherButtonClick(ActionSheet actionSheet, int index) {
                                switch (index) {
                                    case 0:
                                        GalleryFinal.openGalleryMuti(REQUEST_CODE_GALLERY, functionConfig, mOnHanlderResultCallback);
                                        break;
                                    case 1:
                                        GalleryFinal.openCamera(REQUEST_CODE_CAMERA, functionConfig, mOnHanlderResultCallback);
                                        break;
                                    default:
                                        break;
                                }
                            }
                        })
                        .show();
            }
        });
        btnSaveGallery.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mPhotoList.size() > 0) {
                    loadingDialog.show();
                    RequestParams params = new RequestParams();
                    params.put("uploadType", "1");
                    params.put("pk", PKFactory.getPKID(PhotoActivity.this));
                    for (PhotoInfo  photoInfo : mPhotoList){
                        position++;
                        File myFile = new File(photoInfo.getPhotoPath());
                        try {
                            params.put("files", myFile);
                        } catch(FileNotFoundException e) {
                            Toast.makeText(PhotoActivity.this,"照片不存在！",Toast.LENGTH_SHORT).show();
                        }
                        ApiHttpClient.post(params, responseHandler);
                    }


                } else {
                    Toast.makeText(PhotoActivity.this, "请先选择照片！", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    protected AsyncHttpResponseHandler responseHandler = new AsyncHttpResponseHandler() {

        @Override
        public void onSuccess(int statusCode, Header[] headers, byte[] responseBytes) {

            if (position == mPhotoList.size()){
                position = 0;
                loadingDialog.dismiss();
                Toast.makeText(PhotoActivity.this, "保存成功", Toast.LENGTH_SHORT).show();
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
            Toast.makeText(PhotoActivity.this,"保存失败，请重试！",Toast.LENGTH_SHORT).show();
            arg3.printStackTrace();
        }
    };

    private void initImageLoader(Context context) {
        ImageLoaderConfiguration.Builder config = new ImageLoaderConfiguration.Builder(context);
        config.threadPriority(Thread.NORM_PRIORITY - 2);
        config.denyCacheImageMultipleSizesInMemory();
        config.diskCacheFileNameGenerator(new Md5FileNameGenerator());
        config.diskCacheSize(50 * 1024 * 1024); // 50 MiB
        config.tasksProcessingOrder(QueueProcessingType.LIFO);

        ImageLoader.getInstance().init(config.build());
    }

    private GalleryFinal.OnHanlderResultCallback mOnHanlderResultCallback = new GalleryFinal.OnHanlderResultCallback() {
        @Override
        public void onHanlderSuccess(int reqeustCode, List<PhotoInfo> resultList) {
            if (resultList != null) {

                mPhotoList.addAll(resultList);
                HashSet h  =   new  HashSet(mPhotoList);
                mPhotoList.clear();
                mPhotoList.addAll(h);

                if (mPhotoList.size()>0){
                    btnSaveGallery.setVisibility(View.VISIBLE);
                } else {
                    btnSaveGallery.setVisibility(View.INVISIBLE);
                }
                mChoosePhotoListAdapter.notifyDataSetChanged();
            }
        }

        @Override
        public void onHanlderFailure(int requestCode, String errorMsg) {
            Toast.makeText(PhotoActivity.this, errorMsg, Toast.LENGTH_SHORT).show();
        }
    };

    public long getFileSizes(File f) throws Exception{//取得文件大小
        long s=0;
        if (f.exists()) {
            FileInputStream fis = null;
            fis = new FileInputStream(f);
            s= fis.available();
        }
        return s;
    }

    public String FormetFileSize(long fileS) {//转换文件大小
        DecimalFormat df = new DecimalFormat("#.00");
        String fileSizeString = "";
        if (fileS < 1024) {
            fileSizeString = df.format((double) fileS) + "B";
        } else if (fileS < 1048576) {
            fileSizeString = df.format((double) fileS / 1024) + "K";
        } else if (fileS < 1073741824) {
            fileSizeString = df.format((double) fileS / 1048576) + "M";
        } else {
            fileSizeString = df.format((double) fileS / 1073741824) +"G";
        }
        return fileSizeString;
    }

}
