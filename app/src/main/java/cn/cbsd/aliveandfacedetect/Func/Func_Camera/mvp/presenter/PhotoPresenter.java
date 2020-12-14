package cn.cbsd.aliveandfacedetect.Func.Func_Camera.mvp.presenter;

import android.content.Context;
import android.graphics.Bitmap;
import android.util.Log;
import android.view.SurfaceView;
import android.view.TextureView;

import cn.cbsd.FaceUitls.FaceDetectTools;
import cn.cbsd.aliveandfacedetect.Func.Func_Camera.mvp.module.Custom_MachinePhotoModuleImpl;
import cn.cbsd.aliveandfacedetect.Func.Func_Camera.mvp.module.Custom_MachinePhotoModuleImpl2;
import cn.cbsd.aliveandfacedetect.Func.Func_Camera.mvp.module.IPhotoModule;
import cn.cbsd.aliveandfacedetect.Func.Func_Camera.mvp.module.PhonePhotoModuleImpl;
import cn.cbsd.aliveandfacedetect.Func.Func_Camera.mvp.module.PhonePhotoModuleImpl2;
import cn.cbsd.aliveandfacedetect.Func.Func_Camera.mvp.module.PhonePhotoModuleImpl3;
import cn.cbsd.aliveandfacedetect.Func.Func_Camera.mvp.view.IPhotoView;


public class PhotoPresenter {

    private IPhotoView view;

    private static PhotoPresenter instance = null;

    private PhotoPresenter() {
    }

    public static PhotoPresenter getInstance() {
        if (instance == null)
            instance = new PhotoPresenter();
        return instance;
    }

    public enum EquipmentType {
        phone, Custom_machine
    }

    public void PhotoPresenterSetView(IPhotoView view) {
        this.view = view;
    }

    public static PhotoPresenter.EquipmentType equipmentType = EquipmentType.phone;

    IPhotoModule photoModule = camera_module();

    public static IPhotoModule camera_module() {
        if (equipmentType.equals(PhotoPresenter.EquipmentType.Custom_machine)) {
            return new Custom_MachinePhotoModuleImpl2();
        } else {
            return new PhonePhotoModuleImpl3();
        }
    }

    public void Init(SurfaceView ShowView, SurfaceView FaceDetectView, TextureView textureView) {
        try {

            if (FaceDetectView == null) {
                FaceDetectView = ShowView;
            }
            photoModule.Init(ShowView, FaceDetectView, textureView, new IPhotoModule.IOnSetListener() {
                @Override
                public void onBtnText(String msg) {
                    if (view != null) {
                        view.onCaremaText(msg);
                    }
                }

                @Override
                public void onGetPhoto(Bitmap bmp) {
                    view.onGetPhoto(bmp);
                }
            });
        } catch (NullPointerException e) {
            Log.e("setParameter", e.toString());
        }
    }


    public void setDisplay() {
        try {
            photoModule.setDisplay();
        } catch (NullPointerException e) {
            Log.e("setDisplay", e.toString());
        }
    }

    public void capture() {
        try {
            photoModule.capture();
        } catch (NullPointerException e) {
            Log.e("capture", e.toString());
        }

    }


    public void onActivityDestroy() {
        try {
            photoModule.onActivityDestroy();
        } catch (NullPointerException e) {
            Log.e("onActivityDestroy", e.toString());
        }
    }

    public void getOneShut() {
        try {
            photoModule.getOneShut();
        } catch (NullPointerException e) {
            Log.e("getOneShut", e.toString());
        }
    }

    public FaceDetectTools OpenCVPrepare(Context context) {
        return photoModule.OpenCVPrepare(context);
    }
}