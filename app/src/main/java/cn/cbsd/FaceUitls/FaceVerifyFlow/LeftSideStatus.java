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
import cn.cbsd.aliveandfacedetect.Func.Func_Camera.mvp.presenter.PhotoPresenter;
import cn.cbsd.aliveandfacedetect.R;

public class LeftSideStatus extends VerifyStatus {
    private static LeftSideStatus instance = null;

    public static LeftSideStatus getInstance() {
        if (instance == null)
            instance = new LeftSideStatus();
        return instance;
    }

    private LeftSideStatus() {
        super();
        describe_Text = "向左摆头";
        template = MediaHelper.VoiceTemplate.LeftSideStatus;
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
            tools.setMinFaceSize(minFace);
            tools.start();


        } catch (IOException e) {
            Log.e("exception", e.toString());
        }
    }

    @Override
    public void dealData(FaceVerifyContext faceContext, byte[] data, int width, int height, ArrayList<Rect> rectS) {
        if (running) {
            if (SuccessCount < threshold) {
                if (PhotoPresenter.equipmentType.equals(PhotoPresenter.EquipmentType.phone)) {
                    tools.detectRects(data, width, height, rectS, 90, true);
                } else {
                    tools.detectRects(data, width, height, rectS, 0, false);
                }
                if (rectS.size() > 0) {
                    SuccessCount++;
                    Log.e("LeftSideCount", String.valueOf(SuccessCount));

                }
            } else {
                faceContext.setStatus(RightSideStatus.getInstance());
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
        return "LeftSideStatus";
    }
}
