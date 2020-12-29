package cn.cbsd.FaceUitls.FaceVerifyFlow;

import android.content.Context;
import android.graphics.Rect;
import android.support.annotation.NonNull;
import android.util.Log;

import com.blankj.utilcode.util.FileUtils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;

import cn.cbsd.FaceUitls.FaceDetectTools;
import cn.cbsd.aliveandfacedetect.AppInit;
import cn.cbsd.aliveandfacedetect.Func.Func_Camera.mvp.presenter.PhotoPresenter;
import cn.cbsd.aliveandfacedetect.R;

public class FrontStatus extends VerifyStatus {

    private static FrontStatus instance = null;

    public static FrontStatus getInstance() {
        if (instance == null)
            instance = new FrontStatus();
        return instance;
    }

    private FrontStatus() {
        super();
        describe_Text = "请正视屏幕";
        template = MediaHelper.VoiceTemplate.FrontStatus;
        try {
            InputStream is = AppInit.getContext().getResources().openRawResource(R.raw.haarcascade_frontalface_default);
            File cascadeDir = AppInit.getContext().getDir("cascade", Context.MODE_PRIVATE);
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
                    if (SuccessCount == threshold/2) {
                        faceContext.addBitmap(ByteToBMPUtils.byteToBitmap(data,width,height));
                    }
//                    else if (SuccessCount >= (threshold / 2 - 1) && SuccessCount <= (threshold / 2 + 1)) {
//                        faceContext.addBitmap(ByteToBMPUtils.byteToBitmap(data,width,height));
//                    }
                    Log.e("FrontStatusCount", String.valueOf(SuccessCount));
                }
            } else {
                try {
                    faceContext.setStatus(LeftSideStatus.getInstance());
                } catch (Exception e) {
                    e.printStackTrace();
                }
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
        return "FrontStatus";
    }
}
