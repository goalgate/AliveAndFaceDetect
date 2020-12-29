package cn.cbsd.aliveandfacedetect;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.SurfaceView;
import android.view.TextureView;
import android.widget.ImageView;
import android.widget.TextView;

import com.blankj.utilcode.util.BrightnessUtils;
import com.blankj.utilcode.util.ToastUtils;
import com.cbsd.mvphelper.mvplibrary.mvpforView.MVPBaseActivity;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.List;

import butterknife.BindView;
import cn.cbsd.FaceUitls.FaceVerifyFlow.MediaHelper;
import cn.cbsd.aliveandfacedetect.Func.Func_Camera.mvp.presenter.PhotoPresenter;
import cn.cbsd.aliveandfacedetect.Func.Func_Camera.mvp.view.IPhotoView;
import cn.cbsd.network.RetrofitGenerator;
import io.reactivex.Observer;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;
import okhttp3.ResponseBody;

public class FaceActivity extends MVPBaseActivity implements IPhotoView {

    PhotoPresenter pp = PhotoPresenter.getInstance();

    private SensorManager sensorManager;

    @BindView(R.id.FaceDetect_sView)
    SurfaceView FaceDetect_sView;

    @BindView(R.id.Showing_sView)
    SurfaceView Showing_sView;

    @BindView(R.id.texture_view)
    TextureView textureView;

    @BindView(R.id.image)
    ImageView image;

    @BindView(R.id.tv_light)
    TextView tv_light;

    @BindView(R.id.tv_back)
    TextView tv_back;

    String msg;

    @Override
    public int getLayoutId() {
        return R.layout.activity_face;
    }

    @Override
    public void initData(Bundle savedInstanceState) {

        Bundle bundle = this.getIntent().getExtras();
        msg = bundle.getString("result");
        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        Sensor sensor = sensorManager.getDefaultSensor(Sensor.TYPE_LIGHT);
        sensorManager.registerListener(listener, sensor, SensorManager.SENSOR_DELAY_NORMAL);
        pp.Init(Showing_sView, FaceDetect_sView, textureView);
        ScreenBrightnessSet();
    }

    @Override
    public int getOptionsMenuId() {
        return 0;
    }

    @Override
    public Object newP() {
        return null;
    }

    @Override
    protected void onResume() {
        super.onResume();
        pp.PhotoPresenterSetView(this);
    }

    @Override
    protected void onPause() {
        super.onPause();
        pp.PhotoPresenterSetView(null);
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        pp.onActivityDestroy();
        if (sensorManager != null) {
            sensorManager.unregisterListener(listener);
        }
        BrightnessUtils.setWindowBrightness(getWindow(), OriginBrightness);
        BrightnessUtils.setAutoBrightnessEnabled(isAutoBrightnessEnabled);
    }

    @Override
    public void onCaremaText(String s) {
        tv_back.setText(s);
    }

    @Override
    public void onGetPhoto(Bitmap bmp) {
        image.setImageBitmap(bmp);
    }

    @Override
    public void onFaceGet(List<Bitmap> bitmapList) {
        if(bitmapList ==null){
            finish();
        }
        sendData(msg, bitmapList.get(0));
    }

    private SensorEventListener listener = new SensorEventListener() {

        @Override
        public void onSensorChanged(SensorEvent event) {
            // values数组中第一个下标的值就是当前的光照强度
            float value = event.values[0];
            tv_light.setText("当前亮度为" + value + " lx");
        }

        @Override
        public void onAccuracyChanged(Sensor sensor, int accuracy) {

        }

    };
    boolean isAutoBrightnessEnabled;

    int OriginBrightness;

    private void ScreenBrightnessSet() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (!Settings.System.canWrite(this)) {
                Intent intent = new Intent(android.provider.Settings.ACTION_MANAGE_WRITE_SETTINGS);
                intent.setData(Uri.parse("package:" + this.getPackageName()));
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                this.startActivity(intent);
            } else {
                if (!BrightnessUtils.isAutoBrightnessEnabled()) {
                    BrightnessUtils.setAutoBrightnessEnabled(true);
                    isAutoBrightnessEnabled = false;
                } else {
                    isAutoBrightnessEnabled = true;
                }
                OriginBrightness = BrightnessUtils.getBrightness();
                BrightnessUtils.setWindowBrightness(getWindow(), 255);
            }
        }

    }

    @Override
    public void onBackPressed() {
        pp.FaceStop();
        finish();
    }


    private void sendData(String msg, Bitmap userBmp) {

        Bitmap uploadBitmap = rotateBitmap(userBmp,270);
        RetrofitGenerator.getConnectApi().faceCompare(msg, FileUtils.bitmapToBase64(uploadBitmap))
                .subscribeOn(Schedulers.io())
                .unsubscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Observer<ResponseBody>() {
                    @Override
                    public void onSubscribe(Disposable d) {

                    }

                    @Override
                    public void onNext(ResponseBody responseBody) {
                        try {
                            JSONObject jsonObject = new JSONObject(responseBody.string().toString());
                            Intent resultIntent = new Intent();
                            Bundle bundle = new Bundle();
                            bundle.putString("result", jsonObject.getString("result"));
                            bundle.putString("value", jsonObject.getString("value"));
                            resultIntent.putExtras(bundle);
                            FaceActivity.this.setResult(RESULT_OK, resultIntent);
                            FaceActivity.this.finish();
                        } catch (IOException e) {
                            e.printStackTrace();
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }


                    }

                    @Override
                    public void onError(Throwable e) {

                    }

                    @Override
                    public void onComplete() {

                    }
                });
    }

    private Bitmap rotateBitmap(Bitmap origin, float alpha) {
        if (origin == null) {
            return null;
        }
        int width = origin.getWidth();
        int height = origin.getHeight();
        Matrix matrix = new Matrix();
        matrix.setRotate(alpha);
        // 围绕原地进行旋转
        Bitmap newBM = Bitmap.createBitmap(origin, 0, 0, width, height, matrix, false);
        if (newBM.equals(origin)) {
            return newBM;
        }
        origin.recycle();
        return newBM;
    }

}
