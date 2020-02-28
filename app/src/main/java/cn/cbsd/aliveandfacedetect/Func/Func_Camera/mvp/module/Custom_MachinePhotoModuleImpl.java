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
import cn.cbsd.aliveandfacedetect.R;
import io.reactivex.Observable;
import io.reactivex.ObservableSource;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.functions.Consumer;
import io.reactivex.functions.Function;
import io.reactivex.schedulers.Schedulers;


public class Custom_MachinePhotoModuleImpl implements IPhotoModule, Camera.PreviewCallback {

    Camera FaceDetect_cam;

    Camera Showing_cam;

    SurfaceView FaceDetect_sView;

    SurfaceView Showing_sView;

    TextureView mTextureView;

    IOnSetListener callback;

    byte[] global_bytes;

    FaceDetectTools tools;

    int width;

    int height;

    int surfaceViewWidth;

    int surfaceViewHeight;

    private static final String TAG = Custom_MachinePhotoModuleImpl.class.getSimpleName();

    @Override
    public void setDisplay() {
        if(FaceConfig.isBinocular){
            setDisplay(FaceDetect_sView.getHolder(),FaceConfig.FaceDetectCamera);
            setDisplay(Showing_sView.getHolder(),FaceConfig.ShowingCamera);
        }else {
            setDisplay(FaceDetect_sView.getHolder(),FaceConfig.FaceDetectCamera);
        }


    }

    public void setDisplay(SurfaceHolder sHolder, int camera_id) {
        try {
            if(camera_id == FaceConfig.FaceDetectCamera){
                if (FaceDetect_cam != null) {
                    FaceDetect_cam.setPreviewDisplay(sHolder);
                    FaceDetect_cam.startPreview();
                }
            }else if (camera_id == FaceConfig.ShowingCamera){
                if (Showing_cam != null) {
                    Showing_cam.setPreviewDisplay(sHolder);
                    Showing_cam.startPreview();
                }
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void Init(SurfaceView ShowView, SurfaceView FaceDetectView, TextureView textureView, IOnSetListener listener) {
        this.callback = listener;
        this.mTextureView = textureView;

        tools.start();
        if(FaceConfig.isBinocular){
            this.Showing_sView = ShowView;
            this.FaceDetect_sView = FaceDetectView;
            Showing_sView.getHolder().addCallback(new SurfaceHolder.Callback() {
                @Override
                public void surfaceCreated(SurfaceHolder surfaceHolder) {
                    releaseCameraAndPreview(Showing_cam);
                    Showing_cam = Camera.open(FaceConfig.ShowingCamera);
                    Camera.Parameters parameters = Showing_cam.getParameters();
                    parameters.setPictureFormat(ImageFormat.JPEG);
                    parameters.set("jpeg-quality", 100);
                    parameters.setPreviewSize(640,480);
                    Showing_cam.setParameters(parameters);
                    Showing_cam.addCallbackBuffer(new byte[width * height * ImageFormat.getBitsPerPixel(ImageFormat.NV21) / 8]);
                    Showing_cam.setPreviewCallback(new Camera.PreviewCallback() {
                        @Override
                        public void onPreviewFrame(byte[] bytes, Camera camera) {
                            global_bytes = bytes;
                        }
                    });
                    setDisplay(surfaceHolder,FaceConfig.ShowingCamera);
                }

                @Override
                public void surfaceChanged(SurfaceHolder surfaceHolder, int i, int i1, int i2) {

                }

                @Override
                public void surfaceDestroyed(SurfaceHolder surfaceHolder) {
                    releaseCamera(Showing_cam);

                }
            });
            FaceDetect_sView.getHolder().addCallback(new SurfaceHolder.Callback() {
                @Override
                public void surfaceCreated(SurfaceHolder surfaceHolder) {
                    releaseCameraAndPreview(FaceDetect_cam);
                    FaceDetect_cam = Camera.open(FaceConfig.FaceDetectCamera);
//                    safeCameraOpen(FaceDetect_cam, FaceConfig.FaceDetectCamera);
                    Camera.Parameters parameters = FaceDetect_cam.getParameters();
                    Camera.Size size = FaceDetect_cam.getParameters().getPreviewSize(); //获取预览大小
                    parameters.setPreviewSize(640,480);
                    Log.e("width", String.valueOf(width = size.width));
                    Log.e("height", String.valueOf(height = size.height));
                    Log.e("surface_width", String.valueOf(surfaceViewWidth = FaceDetect_sView.getWidth()));
                    Log.e("surface_height", String.valueOf(surfaceViewHeight = FaceDetect_sView.getHeight()));
                    parameters.setPictureFormat(ImageFormat.JPEG);
                    parameters.set("jpeg-quality", 100);
                    FaceDetect_cam.setParameters(parameters);
                    FaceDetect_cam.setPreviewCallbackWithBuffer(Custom_MachinePhotoModuleImpl.this);
                    FaceDetect_cam.addCallbackBuffer(new byte[width * height * ImageFormat.getBitsPerPixel(ImageFormat.NV21) / 8]);
                    FaceDetect_cam.setPreviewCallback(Custom_MachinePhotoModuleImpl.this);
                    setDisplay(surfaceHolder, FaceConfig.FaceDetectCamera);
                }

                @Override
                public void surfaceChanged(SurfaceHolder surfaceHolder, int i, int i1, int i2) {

                }

                @Override
                public void surfaceDestroyed(SurfaceHolder surfaceHolder) {
                    releaseCamera(FaceDetect_cam);

                }
            });

        }else{
            this.FaceDetect_sView = FaceDetectView;
            FaceDetect_sView.getHolder().addCallback(new SurfaceHolder.Callback() {
                @Override
                public void surfaceCreated(SurfaceHolder surfaceHolder) {
                    releaseCameraAndPreview(FaceDetect_cam);
                    FaceDetect_cam = Camera.open(FaceConfig.FaceDetectCamera);
                    Camera.Parameters parameters = FaceDetect_cam.getParameters();
                    Camera.Size size = FaceDetect_cam.getParameters().getPreviewSize(); //获取预览大小
                    parameters.setPreviewSize(640,480);
                    Log.e("width", String.valueOf(width = size.width));
                    Log.e("height", String.valueOf(height = size.height));
                    Log.e("surface_width", String.valueOf(surfaceViewWidth = FaceDetect_sView.getWidth()));
                    Log.e("surface_height", String.valueOf(surfaceViewHeight = FaceDetect_sView.getHeight()));
                    parameters.setPictureFormat(ImageFormat.JPEG);
                    parameters.set("jpeg-quality", 100);
                    FaceDetect_cam.setParameters(parameters);
                    FaceDetect_cam.setPreviewCallbackWithBuffer(Custom_MachinePhotoModuleImpl.this);
                    FaceDetect_cam.addCallbackBuffer(new byte[width * height * ImageFormat.getBitsPerPixel(ImageFormat.NV21) / 8]);
                    FaceDetect_cam.setPreviewCallback(Custom_MachinePhotoModuleImpl.this);
                    setDisplay(surfaceHolder, FaceConfig.FaceDetectCamera);
                }

                @Override
                public void surfaceChanged(SurfaceHolder surfaceHolder, int i, int i1, int i2) {

                }

                @Override
                public void surfaceDestroyed(SurfaceHolder surfaceHolder) {
                    releaseCamera(FaceDetect_cam);
                }
            });

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

    private void setCameraParemeter(Camera camera,SurfaceView surfaceView) {

    }


    @Override
    public void onActivityDestroy() {
        FaceDetect_sView = null;
        Showing_sView = null;
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
        Showing_cam.takePicture(new Camera.ShutterCallback() {
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

    private void safeCameraOpen(Camera camera ,int id) {
        try {
            releaseCameraAndPreview(camera);
            camera = Camera.open(id);
            Log.e("fdfd","Fdfdf");
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    private void releaseCameraAndPreview(Camera camera) {
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
//        getOneShut();
        CalTaskThreadExecutor.getInstance().submit(new Runnable() {
            @Override
            public void run() {
                ArrayList<Rect> rectS = new ArrayList<Rect>();
                tools.detectRects(data, width, height, rectS);
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
        left =  surfaceViewWidth - list.get(0).left * ((float) surfaceViewWidth / width);
        top = list.get(0).top * ((float) surfaceViewHeight / height);
        right = surfaceViewWidth - list.get(0).right * ((float) surfaceViewWidth / width);
        bottom = list.get(0).bottom * ((float) surfaceViewHeight / height);


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


