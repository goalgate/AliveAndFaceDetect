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
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.TextureView;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import cn.cbsd.FaceUitls.FaceDetectTools;
import cn.cbsd.aliveandfacedetect.R;
import io.reactivex.Observable;
import io.reactivex.ObservableSource;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.functions.Consumer;
import io.reactivex.functions.Function;
import io.reactivex.schedulers.Schedulers;

public class PhonePhotoModuleImpl2 implements IPhotoModule, Camera.PreviewCallback {

    public static int FaceDetectCamera = Camera.CameraInfo.CAMERA_FACING_FRONT;

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

    private static final String TAG = PhonePhotoModuleImpl2.class.getSimpleName();


    @Override
    public void Init(SurfaceView ShowView, final SurfaceView FaceDetectView, TextureView textureView, IOnSetListener listener) {
        this.FaceDetect_sView = FaceDetectView;
        this.callback = listener;
        this.mTextureView = textureView;
        FaceDetect_sView.getHolder().addCallback(new SurfaceHolder.Callback() {
            @Override
            public void surfaceCreated(SurfaceHolder surfaceHolder) {
                releaseCameraAndPreview(FaceDetect_cam);
                FaceDetect_cam = Camera.open(FaceDetectCamera);
                FaceDetect_cam.setDisplayOrientation(90);
                Log.e("surfaceViewWidth", String.valueOf(surfaceViewWidth = FaceDetectView.getWidth()));
                Log.e("surfaceViewHeight", String.valueOf(surfaceViewHeight = FaceDetectView.getHeight()));
                Camera.Parameters parameters = FaceDetect_cam.getParameters();
                Camera.Size settingSize = ChooseBestCameraSize(parameters.getSupportedPreviewSizes(), surfaceViewWidth, surfaceViewHeight);
                parameters.setPreviewSize(width = settingSize.width, height = settingSize.height);
                parameters.setPictureFormat(ImageFormat.JPEG);
                parameters.set("jpeg-quality", 100);
                FaceDetect_cam.setParameters(parameters);
                Log.e("width", String.valueOf(width));
                Log.e("height", String.valueOf(height));
                FaceDetect_cam.setPreviewCallbackWithBuffer(PhonePhotoModuleImpl2.this);
                FaceDetect_cam.addCallbackBuffer(new byte[width * height * ImageFormat.getBitsPerPixel(ImageFormat.NV21) / 8]);
                FaceDetect_cam.setPreviewCallback(PhonePhotoModuleImpl2.this);
                setDisplay(surfaceHolder, FaceDetectCamera);
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
        setDisplay(FaceDetect_sView.getHolder(), FaceDetectCamera);
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
            InputStream is = context.getResources().openRawResource(R.raw.haarcascade_frontalface_default);
            File cascadeDir = context.getDir("cascade", Context.MODE_PRIVATE);
            File mCascadeFile = new File(cascadeDir, "haarcascade_frontalface_default.xml");
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


    long lastRecordTime = System.currentTimeMillis();

    //上次记录的索引
    int darkIndex = 0;
    //一个历史记录的数组，255是代表亮度最大值
    long[] darkList = new long[]{255, 255, 255, 255};
    //扫描间隔
    int waitScanTime = 300;

    //亮度低的阀值
    int darkValue = 60;


    @Override
    public void onPreviewFrame(byte[] data, Camera camera) {
        long currentTime = System.currentTimeMillis();
        if (currentTime - lastRecordTime < waitScanTime) {
            return;
        }
        lastRecordTime = currentTime;
        //像素点的总亮度
        long pixelLightCount = 0L;
        //像素点的总数
        long pixeCount = width * height;
        //采集步长，因为没有必要每个像素点都采集，可以跨一段采集一个，减少计算负担，必须大于等于1。
        int step = 10;
        //data.length - allCount * 1.5f的目的是判断图像格式是不是YUV420格式，只有是这种格式才相等
        //因为int整形与float浮点直接比较会出问题，所以这么比
        if (Math.abs(data.length - pixeCount * 1.5f) < 0.00001f) {
            for (int i = 0; i < pixeCount; i += step) {
                //如果直接加是不行的，因为data[i]记录的是色值并不是数值，byte的范围是+127到—128，
                // 而亮度FFFFFF是11111111是-127，所以这里需要先转为无符号unsigned long参考Byte.toUnsignedLong()
                pixelLightCount += ((long) data[i]) & 0xffL;
            }
            //平均亮度
            long cameraLight = pixelLightCount / (pixeCount / step);
            //更新历史记录
            int lightSize = darkList.length;
            darkList[darkIndex = darkIndex % lightSize] = cameraLight;
            darkIndex++;
            boolean isDarkEnv = true;
            //判断在时间范围waitScanTime * lightSize内是不是亮度过暗
            for (int i = 0; i < lightSize; i++) {
                if (darkList[i] > darkValue) {
                    isDarkEnv = false;
                }
            }
            callback.onBtnText("摄像头环境亮度为 ： " + cameraLight);
        }
        camera.addCallbackBuffer(data);
        global_bytes = data;
        CalTaskThreadExecutor.getInstance().submit(new Runnable() {
            @Override
            public void run() {
                ArrayList<Rect> rectS = new ArrayList<Rect>();
                tools.detectRects(global_bytes, width, height, rectS, 90, true);
                Log.e("rectS", String.valueOf(rectS.size()));
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
        left = list.get(0).left - ((surfaceViewHeight - surfaceViewWidth) / 2);
        top = (list.get(0).top) + ((surfaceViewHeight - surfaceViewWidth) / 2);
        right = list.get(0).right - ((surfaceViewHeight - surfaceViewWidth) / 2);
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
