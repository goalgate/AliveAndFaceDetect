//package cn.cbsd.aliveandfacedetect;
//
//import android.app.Activity;
//import android.graphics.Bitmap;
//import android.graphics.BitmapFactory;
//import android.graphics.drawable.BitmapDrawable;
//import android.os.Bundle;
//import android.support.annotation.Nullable;
//import android.widget.ImageView;
//import android.widget.Toast;
//
//import cn.cbsd.aliveandfacedetect.Func.Func_Camera.mvp.module.FaceDetectTools;
//
//public class xfeaturesActivity extends Activity {
//
//
//    ImageView iv_org;
//    ImageView iv_result;
//    FaceDetectTools tools ;
//
//    @Override
//    protected void onCreate(@Nullable Bundle savedInstanceState) {
//        super.onCreate(savedInstanceState);
//        setContentView(R.layout.xfeatures_activity);
//        iv_org = (ImageView) findViewById(R.id.iv_org);
//        iv_result =(ImageView) findViewById(R.id.iv_result);
//
//        tools = new FaceDetectTools();
//        Bitmap bitmap = ((BitmapDrawable)iv_org.getDrawable()).getBitmap();
//        byte[] resultBytes = tools.drawKeypoints(bitmap);
//        iv_result.setImageBitmap(BitmapFactory.decodeByteArray(resultBytes, 0, resultBytes.length));
//
//
//    }
//
//
//
//
//}
