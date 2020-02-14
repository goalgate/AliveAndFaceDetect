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
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.TextureView;

import com.blankj.utilcode.util.ScreenUtils;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import cn.cbsd.aliveandfacedetect.AppInit;
import cn.cbsd.aliveandfacedetect.Func.Func_Camera.mvp.presenter.PhotoPresenter;
import cn.cbsd.aliveandfacedetect.R;
import cn.cbsd.aliveandfacedetect.TESTActivity;
import io.reactivex.Observable;
import io.reactivex.ObservableSource;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.functions.Consumer;
import io.reactivex.functions.Function;
import io.reactivex.schedulers.Schedulers;

/**
 * Created by zbsz on 2017/5/19.
 */

public class PhotoModuleImpl3 implements IPhotoModule, Camera.PreviewCallback {

    int CameraNum = Camera.CameraInfo.CAMERA_FACING_FRONT;

    Camera camera;

    SurfaceView mSurfaceView;

    TextureView mTextureView;

    IOnSetListener callback;

    byte[] global_bytes;

    FaceDetectTools tools;

    int width;

    int height;

    int surfaceViewWidth;

    int surfaceViewHeight;

    float paramWidth;

    float paramHeight;

    PhotoPresenter.EquipmentType equipmentType = PhotoPresenter.EquipmentType.phone;

    private static final String TAG = PhotoModuleImpl3.class.getSimpleName();

    public enum Direction {
        up, left
    }

    private Direction direction = Direction.up;

    @Override
    public void setDisplay(final SurfaceHolder sHolder) {
        try {
            if (camera != null) {
                camera.setPreviewDisplay(sHolder);
                camera.startPreview();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void Init(SurfaceView surfaceView, TextureView textureView, IOnSetListener listener, PhotoPresenter.EquipmentType equipmentType) {
        this.callback = listener;
        this.mTextureView = textureView;
        this.mSurfaceView = surfaceView;
        this.equipmentType = equipmentType;
        tools.start();
        mSurfaceView.getHolder().addCallback(new SurfaceHolder.Callback() {
            @Override
            public void surfaceCreated(SurfaceHolder surfaceHolder) {
                safeCameraOpen(CameraNum);
                setCameraParemeter();
                setDisplay(surfaceHolder);
            }

            @Override
            public void surfaceChanged(SurfaceHolder surfaceHolder, int i, int i1, int i2) {

            }

            @Override
            public void surfaceDestroyed(SurfaceHolder surfaceHolder) {
                releaseCamera();

            }
        });
    }

    private void releaseCamera() {
        if (camera != null) {
            camera.setPreviewCallback(null);
            camera.stopPreview();
            camera.release();
            camera = null;
        }

    }

    private void setCameraParemeter() {
        Camera.Parameters parameters = camera.getParameters();
        Camera.Size size = camera.getParameters().getPreviewSize(); //获取预览大小
        Log.e("width", String.valueOf(width = size.width));
        Log.e("height", String.valueOf(height = size.height));
        Log.e("surface_width", String.valueOf(surfaceViewWidth = mSurfaceView.getWidth()));
        Log.e("surface_height", String.valueOf(surfaceViewHeight = mSurfaceView.getHeight()));
        switch (equipmentType) {
            case phone:
                break;
            case Custom_machine:
                paramWidth = (float) surfaceViewWidth / width;
                paramHeight = (float) surfaceViewHeight / height;
                break;
        }

        parameters.setPictureFormat(ImageFormat.JPEG);
        parameters.set("jpeg-quality", 100);
        camera.setParameters(parameters);
        switch (equipmentType) {
            case Custom_machine:
                break;
            case phone:
                camera.setDisplayOrientation(90);
                break;
            default:
                break;
        }
        camera.setPreviewCallbackWithBuffer(PhotoModuleImpl3.this);
        camera.addCallbackBuffer(new byte[width * height * ImageFormat.getBitsPerPixel(ImageFormat.NV21) / 8]);
        camera.setPreviewCallback(PhotoModuleImpl3.this);
    }


    @Override
    public void onActivityDestroy() {
        mSurfaceView = null;
        mTextureView = null;
        tools.stop();
        tools.release();
    }

    @Override
    public void setMinFaceSize(int size) {
        tools.setMinFaceSize(size);
    }

    @Override
    public void capture() {
        camera.takePicture(new Camera.ShutterCallback() {
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

    private void safeCameraOpen(int id) {
        try {
            releaseCameraAndPreview();
            camera = Camera.open(id);
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    private void releaseCameraAndPreview() {
        if (camera != null) {
            camera.release();
        }
    }

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
    public void onPreviewFrame(final byte[] data, Camera camera) {
        camera.addCallbackBuffer(data);
        global_bytes = data;
        getOneShut();
        CalTaskThreadExecutor.getInstance().submit(new Runnable() {
            @Override
            public void run() {
                ArrayList<Rect> rectS = new ArrayList<Rect>();
                tools.detectRects(global_bytes, width, height, rectS);
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
        switch (equipmentType) {
            case phone:
                switch (CameraNum) {
                    case Camera.CameraInfo.CAMERA_FACING_BACK:
                        left = height - (list.get(0).top);
                        top = list.get(0).left * ((float) surfaceViewHeight / width);
                        right = height - list.get(0).bottom;
                        bottom = list.get(0).right * ((float) surfaceViewHeight / width);
                        break;
                    case Camera.CameraInfo.CAMERA_FACING_FRONT:
                        left = height - (list.get(0).top);
                        top = (width - list.get(0).left) * ((float) surfaceViewHeight / width);
                        right = height - list.get(0).bottom;
                        bottom = (width - list.get(0).right) * ((float) surfaceViewHeight / width);
                        break;
                }
                break;
            case Custom_machine:
                left = list.get(0).left * paramWidth;
                top = list.get(0).top * paramHeight;
                right = list.get(0).right * paramWidth;
                bottom = list.get(0).bottom * paramHeight;
                break;

        }
        rectF = new RectF(left, top, right, bottom);
        canvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR);
        canvas.drawRect(rectF, paint);
        mTextureView.unlockCanvasAndPost(canvas);

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
}

class CalTaskThreadExecutor {

    private static final ExecutorService instance = new ThreadPoolExecutor(1, 3,
            0L, TimeUnit.MILLISECONDS,
            new SynchronousQueue<Runnable>(),
            new ThreadFactory() {
                private final AtomicInteger mCount = new AtomicInteger(1);

                public Thread newThread(Runnable r) {
                    return new Thread(r, "SingleTaskPoolThread #" + mCount.getAndIncrement());
                }
            },
            new RejectedExecutionHandler() {
                @Override
                public void rejectedExecution(Runnable r, ThreadPoolExecutor executor) {
                    Log.e("TAG", "超了");
                    executor.remove(r);
                }
            });

    public static ExecutorService getInstance() {
        return instance;
    }
}




