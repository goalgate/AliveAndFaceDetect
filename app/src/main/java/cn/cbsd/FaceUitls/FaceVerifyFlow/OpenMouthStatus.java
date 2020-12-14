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

public class OpenMouthStatus extends VerifyStatus {
    private static OpenMouthStatus instance = null;

    public static OpenMouthStatus getInstance() {
        if (instance == null)
            instance = new OpenMouthStatus();
        return instance;
    }

    private OpenMouthStatus() {
        super();
        describe_Text = "请正视屏幕张开嘴巴";
        template = MediaHelper.VoiceTemplate.OpenMouthStatus;
        try {
            InputStream is = AppInit.getContext().getResources().openRawResource(R.raw.haarcascade_profileface);
            File cascadeDir = AppInit.getContext().getDir("cascade", Context.MODE_PRIVATE);
            File mCascadeFile = new File(cascadeDir, "haarcascade_profileface.xml");
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
            if (SuccessCount < threshold) {
                tools.detectRects(data, width, height, rectS,90,true);
                if (rectS.size() > 0) {
                    SuccessCount++;
                    Log.e("OpenMouthStatusCount", String.valueOf(SuccessCount));
                }
            } else {
                stop();
                faceContext.setStatus(SmileStatus.getInstance());
//                faceContext.getStatus().dealData(faceContext, data, width, height, rectS);
            }
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
        return "OpenMouthStatus";
    }
}
