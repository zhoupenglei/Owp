package com.owp.qrcode;

import android.animation.Animator;
import android.animation.ObjectAnimator;
import android.animation.PropertyValuesHolder;
import android.animation.ValueAnimator;
import android.content.Intent;
import android.graphics.Rect;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.animation.DecelerateInterpolator;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.RadioGroup;
import android.widget.RelativeLayout;

import com.afollestad.materialdialogs.MaterialDialog;
import com.nineoldandroids.view.ViewHelper;
import com.owp.R;
import com.owp.qrcode.camera.CameraManager;
import com.owp.qrcode.decode.DecodeUtils;
import com.owp.qrcode.utils.BeepManager;
import com.owp.qrcode.utils.InactivityTimer;

import java.io.IOException;

public class CaptureActivity extends AppCompatActivity implements SurfaceHolder.Callback {

    public static final int IMAGE_PICKER_REQUEST_CODE = 100;

    SurfaceView capturePreview;
    ImageView captureErrorMask;
    ImageView captureScanMask;
    FrameLayout captureCropView;
    Button capturePictureBtn;
    Button captureLightBtn;
    RadioGroup captureModeGroup;
    RelativeLayout captureContainer;

    private CameraManager cameraManager;
    private CaptureActivityHandler handler;

    private boolean hasSurface;
    private boolean isLightOn;

    private InactivityTimer mInactivityTimer;
    private BeepManager mBeepManager;

    private int mQrcodeCropWidth = 0;
    private int mQrcodeCropHeight = 0;
    private int mBarcodeCropWidth = 0;
    private int mBarcodeCropHeight = 0;

    private ObjectAnimator mScanMaskObjectAnimator = null;

    private Rect cropRect;
    private int dataMode = DecodeUtils.DECODE_DATA_MODE_QRCODE;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_capture);
        initView();
    }

    private void initView() {
        capturePreview = (SurfaceView) findViewById(R.id.capture_preview);
        captureErrorMask = (ImageView) findViewById(R.id.capture_error_mask);
        captureScanMask = (ImageView) findViewById(R.id.capture_scan_mask);
        captureCropView = (FrameLayout) findViewById(R.id.capture_crop_view);
        capturePictureBtn = (Button) findViewById(R.id.capture_picture_btn);
        captureLightBtn = (Button) findViewById(R.id.capture_light_btn);
        captureModeGroup = (RadioGroup) findViewById(R.id.capture_mode_group);
        captureContainer = (RelativeLayout) findViewById(R.id.capture_container);

        hasSurface = false;
        mInactivityTimer = new InactivityTimer(this);
        mBeepManager = new BeepManager(this);

        mQrcodeCropWidth = getResources().getDimensionPixelSize(R.dimen.qrcode_crop_width);
        mQrcodeCropHeight = getResources().getDimensionPixelSize(R.dimen.qrcode_crop_height);
        mBarcodeCropWidth = getResources().getDimensionPixelSize(R.dimen.barcode_crop_width);
        mBarcodeCropHeight = getResources().getDimensionPixelSize(R.dimen.barcode_crop_height);

        capturePictureBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //打开图片选择器
//                readyGoForResult(CommonImagePickerListActivity.class, IMAGE_PICKER_REQUEST_CODE);
            }
        });

        captureLightBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isLightOn) {
                    cameraManager.setTorch(false);
                    captureLightBtn.setSelected(false);
                } else {
                    cameraManager.setTorch(true);
                    captureLightBtn.setSelected(true);
                }
                isLightOn = !isLightOn;
            }
        });

        captureModeGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                if (checkedId == R.id.capture_mode_barcode) {
                    PropertyValuesHolder qr2barWidthVH = PropertyValuesHolder.ofFloat("width",
                            1.0f, (float) mBarcodeCropWidth / mQrcodeCropWidth);
                    PropertyValuesHolder qr2barHeightVH = PropertyValuesHolder.ofFloat("height",
                            1.0f, (float) mBarcodeCropHeight / mQrcodeCropHeight);
                    ValueAnimator valueAnimator = ValueAnimator.ofPropertyValuesHolder(qr2barWidthVH, qr2barHeightVH);
                    valueAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                        @Override
                        public void onAnimationUpdate(ValueAnimator animation) {
                            Float fractionW = (Float) animation.getAnimatedValue("width");
                            Float fractionH = (Float) animation.getAnimatedValue("height");

                            RelativeLayout.LayoutParams parentLayoutParams = (RelativeLayout.LayoutParams) captureCropView.getLayoutParams();
                            parentLayoutParams.width = (int) (mQrcodeCropWidth * fractionW);
                            parentLayoutParams.height = (int) (mQrcodeCropHeight * fractionH);
                            captureCropView.setLayoutParams(parentLayoutParams);
                        }
                    });
                    valueAnimator.addListener(new Animator.AnimatorListener() {
                        @Override
                        public void onAnimationStart(Animator animation) {

                        }

                        @Override
                        public void onAnimationEnd(Animator animation) {
                            initCrop();
                            setDataMode(DecodeUtils.DECODE_DATA_MODE_BARCODE);
                        }

                        @Override
                        public void onAnimationCancel(Animator animation) {

                        }

                        @Override
                        public void onAnimationRepeat(Animator animation) {

                        }
                    });
                    valueAnimator.start();


                } else if (checkedId == R.id.capture_mode_qrcode) {
                    PropertyValuesHolder bar2qrWidthVH = PropertyValuesHolder.ofFloat("width",
                            1.0f, (float) mQrcodeCropWidth / mBarcodeCropWidth);
                    PropertyValuesHolder bar2qrHeightVH = PropertyValuesHolder.ofFloat("height",
                            1.0f, (float) mQrcodeCropHeight / mBarcodeCropHeight);
                    ValueAnimator valueAnimator = ValueAnimator.ofPropertyValuesHolder(bar2qrWidthVH, bar2qrHeightVH);
                    valueAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                        @Override
                        public void onAnimationUpdate(ValueAnimator animation) {
                            Float fractionW = (Float) animation.getAnimatedValue("width");
                            Float fractionH = (Float) animation.getAnimatedValue("height");

                            RelativeLayout.LayoutParams parentLayoutParams = (RelativeLayout.LayoutParams) captureCropView.getLayoutParams();
                            parentLayoutParams.width = (int) (mBarcodeCropWidth * fractionW);
                            parentLayoutParams.height = (int) (mBarcodeCropHeight * fractionH);
                            captureCropView.setLayoutParams(parentLayoutParams);
                        }
                    });
                    valueAnimator.addListener(new Animator.AnimatorListener() {
                        @Override
                        public void onAnimationStart(Animator animation) {

                        }

                        @Override
                        public void onAnimationEnd(Animator animation) {
                            initCrop();
                            setDataMode(DecodeUtils.DECODE_DATA_MODE_QRCODE);
                        }

                        @Override
                        public void onAnimationCancel(Animator animation) {

                        }

                        @Override
                        public void onAnimationRepeat(Animator animation) {

                        }
                    });
                    valueAnimator.start();
                }
            }
        });
    }

    public Handler getHandler() {
        return handler;
    }

    public CameraManager getCameraManager() {
        return cameraManager;
    }
    public void initCrop() {
        int cameraWidth = cameraManager.getCameraResolution().y;
        int cameraHeight = cameraManager.getCameraResolution().x;

        int[] location = new int[2];
        captureCropView.getLocationInWindow(location);

        int cropLeft = location[0];
        int cropTop = location[1];

        int cropWidth = captureCropView.getWidth();
        int cropHeight = captureCropView.getHeight();

        int containerWidth = captureContainer.getWidth();
        int containerHeight = captureContainer.getHeight();

        int x = cropLeft * cameraWidth / containerWidth;
        int y = cropTop * cameraHeight / containerHeight;

        int width = cropWidth * cameraWidth / containerWidth;
        int height = cropHeight * cameraHeight / containerHeight;

        setCropRect(new Rect(x, y, width + x, height + y));
    }

    public Rect getCropRect() {
        return cropRect;
    }

    public void setCropRect(Rect cropRect) {
        this.cropRect = cropRect;
    }

    public int getDataMode() {
        return dataMode;
    }

    public void setDataMode(int dataMode) {
        this.dataMode = dataMode;
    }
    @Override
    protected void onResume() {
        super.onResume();
        // CameraManager must be initialized here, not in onCreate(). This is necessary because we don't
        // want to open the camera driver and measure the screen size if we're going to show the help on
        // first launch. That led to bugs where the scanning rectangle was the wrong size and partially
        // off screen.
        cameraManager = new CameraManager(getApplication());

        handler = null;

        if (hasSurface) {
            // The activity was paused but not stopped, so the surface still exists. Therefore
            // surfaceCreated() won't be called, so init the camera here.
            initCamera(capturePreview.getHolder());
        } else {
            // Install the callback and wait for surfaceCreated() to init the camera.
            capturePreview.getHolder().addCallback(this);
        }

        mInactivityTimer.onResume();
    }

    @Override
    protected void onPause() {
        if (handler != null) {
            handler.quitSynchronously();
            handler = null;
        }

        mBeepManager.close();
        mInactivityTimer.onPause();
        cameraManager.closeDriver();

        if (!hasSurface) {
            capturePreview.getHolder().removeCallback(this);
        }

        if (null != mScanMaskObjectAnimator && mScanMaskObjectAnimator.isStarted()) {
            mScanMaskObjectAnimator.cancel();
        }
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        mInactivityTimer.shutdown();
        super.onDestroy();
    }


    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        if (holder == null) {
            Log.e(CaptureActivity.class.getSimpleName(), "*** WARNING *** surfaceCreated() gave us a null surface!");
        }
        if (!hasSurface) {
            hasSurface = true;
//            initCamera(holder);
        }
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        hasSurface = false;
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
        initCamera(holder);
    }

    private void initCamera(SurfaceHolder surfaceHolder) {
        if (surfaceHolder == null) {
            throw new IllegalStateException("No SurfaceHolder provided");
        }
        if (cameraManager.isOpen()) {
            Log.w(CaptureActivity.class.getSimpleName(), "initCamera() while already open -- late SurfaceView callback?");
            return;
        }
        try {
            cameraManager.openDriver(surfaceHolder);
            // Creating the handler starts the preview, which can also throw a RuntimeException.
            if (handler == null) {
                handler = new CaptureActivityHandler(this, cameraManager);
            }

            onCameraPreviewSuccess();
        } catch (IOException ioe) {
            Log.w(CaptureActivity.class.getSimpleName(), ioe);
            displayFrameworkBugMessageAndExit();
        } catch (RuntimeException e) {
            // Barcode Scanner has seen crashes in the wild of this variety:
            // java.?lang.?RuntimeException: Fail to connect to camera service
            Log.w(CaptureActivity.class.getSimpleName(), "Unexpected error initializing camera", e);
            displayFrameworkBugMessageAndExit();
        }

    }
    private void onCameraPreviewSuccess() {
        initCrop();
        captureErrorMask.setVisibility(View.GONE);

        ViewHelper.setPivotX(captureScanMask, 0.0f);
        ViewHelper.setPivotY(captureScanMask, 0.0f);

        mScanMaskObjectAnimator = ObjectAnimator.ofFloat(captureScanMask, "scaleY", 0.0f, 1.0f);
        mScanMaskObjectAnimator.setDuration(2000);
        mScanMaskObjectAnimator.setInterpolator(new DecelerateInterpolator());
        mScanMaskObjectAnimator.setRepeatCount(-1);
        mScanMaskObjectAnimator.setRepeatMode(ObjectAnimator.RESTART);
        mScanMaskObjectAnimator.start();
    }

    private void displayFrameworkBugMessageAndExit() {
        captureErrorMask.setVisibility(View.VISIBLE);
        final MaterialDialog.Builder builder = new MaterialDialog.Builder(this);
        builder.cancelable(true);
        builder.title(R.string.app_name);
        builder.content(R.string.tips_open_camera_error);
        builder.positiveText(R.string.btn_ok);
        builder.callback(new MaterialDialog.ButtonCallback() {
            @Override
            public void onPositive(MaterialDialog dialog) {
                super.onPositive(dialog);
                finish();
            }
        });
        builder.show();
    }

    public void handleDecode(String result, Bundle bundle) {
        mInactivityTimer.onActivity();
        mBeepManager.playBeepSoundAndVibrate();
        Intent intent = new Intent(this, ResultActivity.class);
        bundle.putString(ResultActivity.BUNDLE_KEY_SCAN_RESULT, result);
        bundle.putInt(ResultActivity.BUNDLE_KEY_SCAN_TYPE_RESULT,getDataMode());
        if (null != bundle) {
            intent.putExtras(bundle);
        }
        startActivity(intent);
        finish();
    }
}
