package cn.cbsd.aliveandfacedetect;

import android.Manifest;
import android.content.Context;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.util.Log;
import android.view.Surface;
import android.view.SurfaceView;
import android.view.TextureView;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;

import com.blankj.utilcode.util.BarUtils;

import java.util.List;
import java.util.concurrent.TimeUnit;

import cn.cbsd.aliveandfacedetect.Func.Func_Camera.mvp.presenter.PhotoPresenter;
import cn.cbsd.aliveandfacedetect.Func.Func_Camera.mvp.view.IPhotoView;
import io.reactivex.Observable;
import io.reactivex.functions.Consumer;
import io.reactivex.schedulers.Schedulers;

public class MainActivity extends BaseActivity implements IPhotoView {

    private static final String TAG = MainActivity.class.getSimpleName();

    PhotoPresenter pp = PhotoPresenter.getInstance();

    SurfaceView surfaceView;

    TextureView textureView;

    ImageView imageView;

    String[] permissions = new String[]{
            Manifest.permission.CAMERA,
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
    };


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        BarUtils.hideStatusBar(this);
        setContentView(R.layout.activity_main);
        surfaceView = findViewById(R.id.mSurfaceView);
        textureView = findViewById(R.id.texture_view);
        imageView = findViewById(R.id.image);
        requestRunPermisssion(permissions, new PermissionListener() {
            @Override
            public void onGranted() {

            }

            @Override
            public void onDenied(List<String> deniedPermission) {


            }
        });
        surfaceView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                pp.getOneShut();
            }
        });
        pp.OpenCVPrepare(this);
        pp.Init(surfaceView, textureView, PhotoPresenter.EquipmentType.phone);
        pp.setMinFaceSize(50);
    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    @Override
    protected void onResume() {
        super.onResume();
        pp.PhotoPresenterSetView(this);
        pp.setDisplay(surfaceView.getHolder());
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


    }

    @Override
    public void onCaremaText(String s) {

    }

    @Override
    public void onGetPhoto(Bitmap bmp) {
        imageView.setImageBitmap(bmp);
    }


}
