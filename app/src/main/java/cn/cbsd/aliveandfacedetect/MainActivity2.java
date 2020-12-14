package cn.cbsd.aliveandfacedetect;

import android.Manifest;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.Rect;
import android.graphics.RectF;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.util.DisplayMetrics;
import android.view.SurfaceView;
import android.view.TextureView;
import android.view.View;
import android.view.WindowManager;

import com.blankj.utilcode.util.BarUtils;
import com.blankj.utilcode.util.BrightnessUtils;
import com.blankj.utilcode.util.ScreenUtils;

import java.util.List;

import cn.cbsd.aliveandfacedetect.Func.Func_Camera.mvp.UI.FaceRoundView;
import cn.cbsd.aliveandfacedetect.Func.Func_Camera.mvp.presenter.PhotoPresenter;
import cn.cbsd.aliveandfacedetect.Func.Func_Camera.mvp.view.IPhotoView;

public class MainActivity2 extends BaseActivity implements IPhotoView {

    private static final String TAG = MainActivity2.class.getSimpleName();

    private int mScreenW;
    private int mScreenH;

    private int OriginBrightness ;

    private boolean isAutoBrightnessEnabled;

    private PhotoPresenter pp = PhotoPresenter.getInstance();

    private SurfaceView FaceDetect_sView;

    private SurfaceView Showing_sView;

    private TextureView textureView;

//    private FaceRoundView rectView;
//
//    private View mInitView;

    String[] permissions = new String[]{
            Manifest.permission.CAMERA,
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_SETTINGS
    };


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        BarUtils.setStatusBarVisibility(this,false);
        setContentView(R.layout.activity_main2);
        FaceDetect_sView = (SurfaceView) findViewById(R.id.FaceDetect_sView);
        Showing_sView = (SurfaceView) findViewById(R.id.Showing_sView);
        textureView = findViewById(R.id.texture_view);
//        rectView = findViewById(R.id.rect_view);
//        mInitView = findViewById(R.id.camera_layout);
//        mInitView.setVisibility(View.INVISIBLE);
        mScreenW = ScreenUtils.getScreenWidth();
        mScreenH = ScreenUtils.getScreenHeight();
        requestRunPermisssion(permissions, new PermissionListener() {
            @Override
            public void onGranted() {

            }

            @Override
            public void onDenied(List<String> deniedPermission) {


            }
        });
        pp.OpenCVPrepare(this);
        pp.Init(Showing_sView,FaceDetect_sView, textureView);
        ScreenBrightnessSet();

    }


    private void ScreenBrightnessSet(){
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (!Settings.System.canWrite(this)) {
                Intent intent = new Intent(Settings.ACTION_MANAGE_WRITE_SETTINGS);
                intent.setData(Uri.parse("package:" + this.getPackageName()));
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                this.startActivity(intent);
            } else {
                if (!BrightnessUtils.isAutoBrightnessEnabled()){
                    BrightnessUtils.setAutoBrightnessEnabled(true);
                    isAutoBrightnessEnabled = false;
                }else {
                    isAutoBrightnessEnabled = true;
                }
                OriginBrightness = BrightnessUtils.getBrightness();
                BrightnessUtils.setWindowBrightness(getWindow(),255);
            }
        }

    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    @Override
    protected void onResume() {
        super.onResume();
        pp.PhotoPresenterSetView(this);
    }

    @Override
    protected void onPause() {
        super.onPause();
        pp.PhotoPresenterSetView(null);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        pp.onActivityDestroy();
        BrightnessUtils.setWindowBrightness(getWindow(),OriginBrightness);
        BrightnessUtils.setAutoBrightnessEnabled(isAutoBrightnessEnabled);
    }

    @Override
    public void onCaremaText(String s) {

    }

    @Override
    public void onGetPhoto(Bitmap bmp) {
    }





}
