package cn.cbsd.aliveandfacedetect.Func.Func_Camera.mvp.module;

import android.graphics.Bitmap;
import android.graphics.Rect;
import android.graphics.RectF;
import android.util.Log;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;


public class FaceDetectTools {

    static {
        System.loadLibrary("FaceDetectTools-jni");
    }
    private long mNativeObj = 0;

    public FaceDetectTools() {
    }

    public FaceDetectTools(String cascadeName, int minFaceSize) {
        mNativeObj = nativeCreateObject(cascadeName, minFaceSize);
    }

    public void start() {
        nativeStart(mNativeObj);
    }

    public void stop() {
        nativeStop(mNativeObj);
    }

    public void setMinFaceSize(int size) {
        nativeSetFaceSize(mNativeObj, size);
    }

    public void detectRects(byte[] inputImage, int w, int h, ArrayList<Rect> rectS) {
        nativeDetectRects(mNativeObj, inputImage, w, h, rectS);
    }

    public void detectRectsRotate90(byte[] inputImage, int w, int h, ArrayList<Rect> rectS) {
        nativeDetectRectsRotate90(mNativeObj, inputImage, w, h, rectS);
    }

    public void release() {
        nativeDestroyObject(mNativeObj);
        mNativeObj = 0;
    }

    public byte[] drawKeypoints(Bitmap bitmap) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
        byte[] data = baos.toByteArray();
        nativeDrawKeypoints(mNativeObj,data,bitmap.getWidth(),bitmap.getHeight());
        return data;
    }

    private static native long nativeCreateObject(String cascadeName, int minFaceSize);

    private static native void nativeDestroyObject(long thiz);

    private static native void nativeStart(long thiz);

    private static native void nativeStop(long thiz);

    private static native void nativeSetFaceSize(long thiz, int size);

    private static native void nativeDetectRects(long thiz, byte[] inputImage, int w, int h, ArrayList<Rect> rectS);

    private static native void nativeDetectRectsRotate90(long thiz, byte[] inputImage, int w, int h, ArrayList<Rect> rectS);

    private static native void nativeDrawKeypoints(long thiz,byte[] inputImage, int w, int h);
}
