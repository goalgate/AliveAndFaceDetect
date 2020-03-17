package cn.cbsd.aliveandfacedetect.Func.Func_Camera.mvp.module;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ImageFormat;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.YuvImage;
import android.hardware.Camera;
import android.text.Layout;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.TextureView;
import android.view.ViewGroup;

import com.blankj.utilcode.util.ScreenUtils;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import cn.cbsd.aliveandfacedetect.R;
import io.reactivex.Observable;
import io.reactivex.ObservableSource;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.functions.Consumer;
import io.reactivex.functions.Function;
import io.reactivex.schedulers.Schedulers;

public class PhonePhotoModuleImpl implements IPhotoModule, Camera.PreviewCallback {

    Camera FaceDetect_cam;

    SurfaceView FaceDetect_sView;

    TextureView mTextureView;

    IOnSetListener callback;

    byte[] global_bytes;

    FaceDetectTools tools;

    int width;

    int height;

    int surfaceViewWidth;

    int surfaceViewHeight;

    private static final String TAG = PhonePhotoModuleImpl.class.getSimpleName();


    @Override
    public void Init(SurfaceView ShowView, final SurfaceView FaceDetectView, TextureView textureView, IOnSetListener listener) {
        this.FaceDetect_sView = FaceDetectView;
        this.callback = listener;
        this.mTextureView = textureView;
        FaceDetect_sView.getHolder().addCallback(new SurfaceHolder.Callback() {
            @Override
            public void surfaceCreated(SurfaceHolder surfaceHolder) {
                releaseCameraAndPreview(FaceDetect_cam);
                FaceDetect_cam = Camera.open(FaceConfig.FaceDetectCamera);
                FaceDetect_cam.setDisplayOrientation(90);
                Camera.Parameters parameters = FaceDetect_cam.getParameters();
                List<Camera.Size> sizes = parameters.getSupportedPreviewSizes();
                Camera.Size settingSize = ChooseBestCameraSize(sizes, FaceDetectView.getWidth(), FaceDetectView.getHeight());
                parameters.setPreviewSize(settingSize.width, settingSize.height);
                parameters.setPictureFormat(ImageFormat.JPEG);
                parameters.set("jpeg-quality", 100);
                FaceDetect_cam.setParameters(parameters);
                Camera.Size sizeResult = parameters.getPreviewSize();
                Log.e("width", String.valueOf(width = sizeResult.width));
                Log.e("height", String.valueOf(height = sizeResult.height));
                Log.e("surfaceViewWidth", String.valueOf(surfaceViewWidth = FaceDetectView.getWidth()));
                Log.e("surfaceViewHeight", String.valueOf(surfaceViewHeight = FaceDetectView.getHeight()));
                FaceDetect_cam.setPreviewCallbackWithBuffer(PhonePhotoModuleImpl.this);
                FaceDetect_cam.addCallbackBuffer(new byte[width * height * ImageFormat.getBitsPerPixel(ImageFormat.NV21) / 8]);
                FaceDetect_cam.setPreviewCallback(PhonePhotoModuleImpl.this);
                setDisplay(surfaceHolder, FaceConfig.FaceDetectCamera);
            }

            @Override
            public void surfaceChanged(SurfaceHolder surfaceHolder, int i, int i1, int i2) {

            }

            @Override
            public void surfaceDestroyed(SurfaceHolder surfaceHolder) {
                FaceDetect_cam.setPreviewCallback(null);
                releaseCamera(FaceDetect_cam);
            }
        });
    }

    @Override
    public void setDisplay() {
        setDisplay(FaceDetect_sView.getHolder(), FaceConfig.FaceDetectCamera);
    }

    public void setDisplay(SurfaceHolder surfaceHolder, int camera_id) {
        try {
            if (FaceDetect_cam != null) {
                FaceDetect_cam.setPreviewDisplay(surfaceHolder);
                FaceDetect_cam.startPreview();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void capture() {
        FaceDetect_cam.takePicture(new Camera.ShutterCallback() {
            public void onShutter() {
                // 按下快门瞬间会执行此处代码
            }
        }, new Camera.PictureCallback() {
            public void onPictureTaken(byte[] data, Camera c) {
                // 此处代码可以决定是否需要保存原始照片信息
            }
        }, myJpegCallback);
    }

    Camera.PictureCallback myJpegCallback = new Camera.PictureCallback() {
        @Override
        public void onPictureTaken(byte[] data, final Camera camera) {
            camera.stopPreview();
            Bitmap bmp = BitmapFactory.decodeByteArray(data, 0, data.length);
            callback.onBtnText("拍照成功");
            callback.onGetPhoto(bmp);
        }
    };

    @Override
    public void getOneShut() {
        Observable.just(global_bytes)
                .subscribeOn(Schedulers.computation())
                .unsubscribeOn(Schedulers.computation())
                .flatMap(new Function<byte[], ObservableSource<Bitmap>>() {
                    @Override
                    public ObservableSource<Bitmap> apply(byte[] bytes) throws Exception {
                        YuvImage image = new YuvImage(global_bytes, ImageFormat.NV21, width, height, null);
                        ByteArrayOutputStream os = new ByteArrayOutputStream(global_bytes.length);
                        if (!image.compressToJpeg(new Rect(0, 0, width, height), 100, os)) {
                            return null;
                        }
                        byte[] tmp = os.toByteArray();
                        Bitmap bmp = BitmapFactory.decodeByteArray(tmp, 0, tmp.length);
                        return Observable.just(bmp);
                    }
                }).observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Consumer<Bitmap>() {
                    @Override
                    public void accept(Bitmap bitmap) throws Exception {
                        callback.onGetPhoto(bitmap);
                    }
                });
    }

    @Override
    public void onActivityDestroy() {

        FaceDetect_sView = null;
        mTextureView = null;
        tools.stop();
        tools.release();
    }

    @Override
    public FaceDetectTools OpenCVPrepare(Context context) {
        try {
            // load cascade file from application resources
            InputStream is = context.getResources().openRawResource(R.raw.lbpcascade_frontalface);
            File cascadeDir = context.getDir("cascade", Context.MODE_PRIVATE);
            File mCascadeFile = new File(cascadeDir, "lbpcascade_frontalface.xml");
            FileOutputStream os = new FileOutputStream(mCascadeFile);
            byte[] buffer = new byte[4096];
            int bytesRead;
            while ((bytesRead = is.read(buffer)) != -1) {
                os.write(buffer, 0, bytesRead);
            }
            is.close();
            os.close();
            tools = new FaceDetectTools(mCascadeFile.getAbsolutePath(), 0);
            cascadeDir.delete();
            return tools;
        } catch (IOException e) {
            e.printStackTrace();
            Log.e(TAG, "Failed to load cascade. Exception thrown: " + e);
        }
        return null;
    }

    @Override
    public void setMinFaceSize(int size) {
        tools.setMinFaceSize(size);
    }

    @Override
    public void onPreviewFrame(byte[] bytes, Camera camera) {
        camera.addCallbackBuffer(bytes);
        global_bytes = bytes;
        CalTaskThreadExecutor.getInstance().submit(new Runnable() {
            @Override
            public void run() {
                ArrayList<Rect> rectS = new ArrayList<Rect>();
                tools.detectRectsRotate90(global_bytes, width, height, rectS);
                showFrame(rectS);
            }
        });
    }

    private RectF rectF = new RectF();
    private Paint paint = new Paint();

    {
        paint.setColor(Color.GREEN);
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(5);
    }

    float left;
    float top;
    float right;
    float bottom;

    private void showFrame(ArrayList<Rect> list) {
        Canvas canvas = mTextureView.lockCanvas();
        if (canvas == null) {
            mTextureView.unlockCanvasAndPost(canvas);
            return;
        }
        if (list == null || list.size() == 0) {
            // 清空canvas
            canvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR);
            mTextureView.unlockCanvasAndPost(canvas);
            return;
        }

//        switch (FaceConfig.FaceDetectCamera) {
//            case Camera.CameraInfo.CAMERA_FACING_BACK:
//                left = surfaceViewHeight - (list.get(0).top);
//                top = list.get(0).left * ((float) surfaceViewHeight / width);
//                right = surfaceViewHeight - list.get(0).bottom;
//                bottom = list.get(0).right * ((float) surfaceViewHeight / width);
//                break;
//            case Camera.CameraInfo.CAMERA_FACING_FRONT:
//                left = surfaceViewHeight - (list.get(0).top);
//                top = (surfaceViewWidth - list.get(0).left) * ((float) surfaceViewHeight / width);
//                right = surfaceViewHeight - list.get(0).bottom;
//                bottom = (surfaceViewWidth - list.get(0).right) * ((float) surfaceViewHeight / width);
//                break;
//        }
//        left = ((surfaceViewWidth + width) / 2) - list.get(0).left;
        left = surfaceViewHeight - list.get(0).left - ((surfaceViewHeight - surfaceViewWidth) / 2);
        top = (list.get(0).top) + ((surfaceViewHeight - surfaceViewWidth) / 2);
        right = surfaceViewHeight - list.get(0).right - ((surfaceViewHeight - surfaceViewWidth) / 2);
        bottom = (list.get(0).bottom) + ((surfaceViewHeight - surfaceViewWidth) / 2);

        rectF = new RectF(left, top, right, bottom);
        canvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR);
        canvas.drawRect(rectF, paint);
        mTextureView.unlockCanvasAndPost(canvas);

    }


    private void releaseCameraAndPreview(Camera camera) {
        if (camera != null) {
            camera.release();
        }
    }

    private void releaseCamera(Camera camera) {
        if (camera != null) {
            camera.setPreviewCallback(null);
            camera.stopPreview();
            camera.release();
            camera = null;
        }
    }

    private Camera.Size ChooseBestCameraSize(List<Camera.Size> sizes, int surfaceWidth, int surfaceHeight) {
        Camera.Size sizeBack = null;
        float slope = (float) surfaceHeight / (float) surfaceWidth;
        float deviation = 100;
        for (Camera.Size size : sizes) {
            float size_slope = (float) size.width / (float) size.height;
            if (Math.abs(size_slope - slope) < deviation) {
                deviation = Math.abs(size_slope - slope);
                sizeBack = size;
            }
        }
        return sizeBack;
    }


}
