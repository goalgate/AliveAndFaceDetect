package cn.cbsd.aliveandfacedetect.Func.Func_Camera.mvp.presenter;
import android.content.Context;
import android.graphics.Bitmap;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.TextureView;

import cn.cbsd.aliveandfacedetect.Func.Func_Camera.mvp.module.FaceDetectTools;
import cn.cbsd.aliveandfacedetect.Func.Func_Camera.mvp.module.IPhotoModule;
import cn.cbsd.aliveandfacedetect.Func.Func_Camera.mvp.module.PhotoModuleImpl3;
import cn.cbsd.aliveandfacedetect.Func.Func_Camera.mvp.view.IPhotoView;




public class PhotoPresenter {

    private IPhotoView view;

    private static PhotoPresenter instance=null;
    private PhotoPresenter(){}
    public static PhotoPresenter getInstance() {
        if(instance==null)
            instance=new PhotoPresenter();
        return instance;
    }

    public enum EquipmentType {
        phone, Custom_machine
    }



    public void PhotoPresenterSetView(IPhotoView view) {
        this.view = view;
    }

    IPhotoModule photoModule = new PhotoModuleImpl3();


    public void Init(SurfaceView surfaceView, TextureView textureView,EquipmentType equipmentType){
        try {
            photoModule.Init(surfaceView, textureView, new IPhotoModule.IOnSetListener() {
                @Override
                public void onBtnText(String msg) {
                    view.onCaremaText(msg);
                }

                @Override
                public void onGetPhoto(Bitmap bmp) {
                    view.onGetPhoto(bmp);
                }
            },equipmentType);
        }catch (NullPointerException e){
            Log.e("setParameter",e.toString());
        }
    }


    public void setDisplay(SurfaceHolder surfaceHolder){
        try {
            photoModule.setDisplay(surfaceHolder);
        }catch (NullPointerException e){
            Log.e("setDisplay",e.toString());
        }
    }

    public void capture(){
        try {
            photoModule.capture();
        }catch (NullPointerException e){
            Log.e("capture",e.toString());
        }

    }


    public void onActivityDestroy(){
        try {
            photoModule.onActivityDestroy();
        }catch (NullPointerException e){
            Log.e("onActivityDestroy",e.toString());
        }
    }
    public void getOneShut(){
        try {
            photoModule.getOneShut();
        }catch (NullPointerException e){
            Log.e("getOneShut",e.toString());
        }
    }

    public void setMinFaceSize(int size){
        try {
            photoModule.setMinFaceSize(size);
        }catch (NullPointerException e){
            Log.e("setMinFaceSize",e.toString());
        }
    }


    public FaceDetectTools OpenCVPrepare(Context context){
        return photoModule.OpenCVPrepare(context);
    }
}