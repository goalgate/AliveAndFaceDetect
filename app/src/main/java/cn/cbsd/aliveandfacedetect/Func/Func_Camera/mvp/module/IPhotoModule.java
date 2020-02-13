package cn.cbsd.aliveandfacedetect.Func.Func_Camera.mvp.module;

import android.content.Context;
import android.graphics.Bitmap;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.TextureView;

import cn.cbsd.aliveandfacedetect.Func.Func_Camera.mvp.presenter.PhotoPresenter;


/**
 * Created by zbsz on 2017/5/19.
 */


public interface IPhotoModule {

    void Init(SurfaceView surfaceView, TextureView textureView, IOnSetListener listener, PhotoPresenter.MyOrientation orientation);

//    void setParameter(SurfaceView surfaceView , TextureView textureView, IOnSetListener listener, PhotoPresenter.MyOrientation orientation);

    void setDisplay(SurfaceHolder surfaceHolder);

    void capture();//拍照按钮点击事件

    void getOneShut();

    void onActivityDestroy();


    FaceDetectTools OpenCVPrepare(Context context);

    void setMinFaceSize(int size);

    interface IOnSetListener {
        void onBtnText(String msg);//按完按钮后的回调接口

        void onGetPhoto(Bitmap bmp);
    }

}