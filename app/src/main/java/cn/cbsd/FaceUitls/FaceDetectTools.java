package cn.cbsd.FaceUitls;

import android.graphics.Rect;

import java.util.ArrayList;


public class FaceDetectTools {

    static {
        System.loadLibrary("FaceDetectTools-jni");
    }

    private long mNativeObj = 0;

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

    public void detectRects(byte[] inputImage, int w, int h, ArrayList<Rect> rectS, int rotation,
                            boolean mirror) {
        nativeDetectRects(mNativeObj, inputImage, w, h, rectS, rotation, mirror);
    }

    public void release() {
        nativeDestroyObject(mNativeObj);
        mNativeObj = 0;
    }

    private static native long nativeCreateObject(String cascadeName, int minFaceSize);

    private static native void nativeDestroyObject(long thiz);

    private static native void nativeStart(long thiz);

    private static native void nativeStop(long thiz);

    private static native void nativeSetFaceSize(long thiz, int size);

    private static native void nativeDetectRects(long thiz, byte[] inputImage, int w, int h, ArrayList<Rect> rectS, int rotation,
                                                 boolean mirror);
}
