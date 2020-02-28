package cn.cbsd.aliveandfacedetect.Func.Func_Camera.mvp.module;

import android.content.Context;
import android.hardware.Camera;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.TextureView;

public class PhonePhotoModuleImpl implements IPhotoModule, Camera.PreviewCallback {
    @Override
    public void onPreviewFrame(byte[] bytes, Camera camera) {

    }

    @Override
    public void Init(SurfaceView ShowView, SurfaceView FaceDetectView, TextureView textureView, IOnSetListener listener) {

    }

    @Override
    public void setDisplay() {

    }

    public void setDisplay(SurfaceHolder surfaceHolder, int camera_id) {

    }

    @Override
    public void capture() {

    }

    @Override
    public void getOneShut() {

    }

    @Override
    public void onActivityDestroy() {

    }

    @Override
    public FaceDetectTools OpenCVPrepare(Context context) {
        return null;
    }

    @Override
    public void setMinFaceSize(int size) {

    }
}
