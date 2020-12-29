package cn.cbsd.aliveandfacedetect;

import android.Manifest;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Log;

import com.blankj.utilcode.util.ActivityUtils;
import com.blankj.utilcode.util.ToastUtils;
import com.cbsd.mvphelper.mvplibrary.Tools.ActivityCollector;
import com.cbsd.mvphelper.mvplibrary.mvpforView.MVPBaseActivity;
import com.mining.app.zxing.activity.MipcaActivityCapture;

import butterknife.OnClick;

import cn.cbsd.FaceUitls.FaceVerifyFlow.MediaHelper;
import io.reactivex.functions.Consumer;


public class MainActivity extends MVPBaseActivity {

    private final static int SCANNIN_GREQUEST_CODE = 1;

    private final static int FACE_DETECT_CODE = 2;

    String[] permissions = new String[]{
            Manifest.permission.CAMERA,
            Manifest.permission.INTERNET,
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
    };

    @OnClick(R.id.btn_scan)
    void Scan() {
        Intent intent = new Intent();
        intent.setClass(this, MipcaActivityCapture.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivityForResult(intent, SCANNIN_GREQUEST_CODE);
    }

    @Override
    public int getLayoutId() {
        return R.layout.activity_main;
    }

    @Override
    public void initData(Bundle savedInstanceState) {
        MediaHelper.mediaOpen();

        getRxPermissions().request(permissions).subscribe(new Consumer<Boolean>() {
            @Override
            public void accept(Boolean granted) throws Exception {
                if (granted) {
                } else {
                    ToastUtils.showLong("应用未能获取全部权限");
                }
            }
        });
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
    public void onBackPressed() {
        ActivityCollector.getInstance().finishAll();
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        MediaHelper.mediaRealese();

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case SCANNIN_GREQUEST_CODE:
                if (resultCode == RESULT_OK) {
                    Bundle bundle = data.getExtras();
                    Intent intent = new Intent();
                    intent.putExtras(bundle);
                    intent.setClass(this, FaceActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    startActivityForResult(intent, FACE_DETECT_CODE);
                }
                break;
            case FACE_DETECT_CODE:
                if (resultCode == RESULT_OK) {
                    Bundle bundle = data.getExtras();
                    String result = bundle.getString("result");
                    String value = bundle.getString("value");
                    if (result.equals("true")) {
                        ToastUtils.showLong("人脸认证成功，识别分数" + value);
                    } else {
                        ToastUtils.showLong("人脸认证失败，识别分数" + value);
                    }
                }

                break;

            default:
                break;
        }
    }

}
