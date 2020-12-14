package cn.cbsd.aliveandfacedetect;

import android.app.Activity;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.annotation.Nullable;

import cn.cbsd.FaceUitls.FaceVerifyFlow.FaceVerifyContext;
import cn.cbsd.FaceUitls.FaceVerifyFlow.MediaHelper;

public class TestActivity extends Activity {
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_test);
        FaceVerifyContext context = new FaceVerifyContext(new FaceVerifyContext.ConvertEvent() {
            @Override
            public void Message(String Text) {

            }

            @Override
            public void Voice(MediaHelper.VoiceTemplate temp) {

            }


        });
        context.prepare(context);

    }
}
