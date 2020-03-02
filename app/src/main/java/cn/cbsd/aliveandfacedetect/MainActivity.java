package cn.cbsd.aliveandfacedetect;

import android.Manifest;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.SurfaceView;
import android.view.TextureView;
import android.widget.ImageView;

import com.blankj.utilcode.util.BarUtils;

import java.util.List;

import cn.cbsd.aliveandfacedetect.Func.Func_Camera.mvp.presenter.PhotoPresenter;
import cn.cbsd.aliveandfacedetect.Func.Func_Camera.mvp.view.IPhotoView;

public class MainActivity extends BaseActivity implements IPhotoView {

    private static final String TAG = MainActivity.class.getSimpleName();

    PhotoPresenter pp = PhotoPresenter.getInstance();

    SurfaceView FaceDetect_sView;

    SurfaceView Showing_sView;

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
        FaceDetect_sView = (SurfaceView) findViewById(R.id.FaceDetect_sView);
        Showing_sView = (SurfaceView) findViewById(R.id.Showing_sView);
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
        pp.OpenCVPrepare(this);
        pp.Init(Showing_sView,FaceDetect_sView, textureView);
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
        pp.setDisplay();
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
