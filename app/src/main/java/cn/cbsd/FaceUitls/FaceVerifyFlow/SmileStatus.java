package cn.cbsd.FaceUitls.FaceVerifyFlow;

import android.content.Context;
import android.graphics.Rect;
import android.support.annotation.NonNull;
import android.util.Log;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;

import cn.cbsd.FaceUitls.FaceDetectTools;
import cn.cbsd.aliveandfacedetect.AppInit;
import cn.cbsd.aliveandfacedetect.R;

public class SmileStatus extends VerifyStatus {
    private static SmileStatus instance = null;

    public static SmileStatus getInstance() {
        if (instance == null)
            instance = new SmileStatus();
        return instance;
    }

    private SmileStatus() {
        super();
        describe_Text = "请正视屏幕微笑";
        template = MediaHelper.VoiceTemplate.SmileStatus;
        try {
            InputStream is = AppInit.getContext().getResources().openRawResource(R.raw.haarcascade_smile);
            File cascadeDir = AppInit.getContext().getDir("cascade", Context.MODE_PRIVATE);
            File mCascadeFile = new File(cascadeDir, "haarcascade_smile.xml");
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
            tools.start();
        } catch (IOException e) {
            Log.e("exception", e.toString());
        }
    }

    @Override
    public void dealData(FaceVerifyContext faceContext, byte[] data, int width, int height, ArrayList<Rect> rectS) {
        if (running) {
            tools.detectRects(data, width, height, rectS, 90, true);
            if (rectS.size() > 0) {
                SuccessCount++;
                Log.e("SmileCount", String.valueOf(SuccessCount));
            }
//            if (SuccessCount < threshold) {
//                tools.detectRectsRotate90(data, width, height, rectS);
//                if (rectS.size() > 0) {
//                    SuccessCount++;
//                    Log.e("SmileCount", String.valueOf(SuccessCount));
//                }
//            } else {
//                stop();
//                faceContext.setStatus(CompleteStatus.getInstance());
//                faceContext.getStatus().dealData(faceContext, data, width, height, rectS);
//            }
        } else {
            stop();

        }
    }


    public void release() {
        if (instance != null)
            instance = null;
    }


    @NonNull
    @Override
    public String toString() {
        return "LeftSideStatus";
    }
}
