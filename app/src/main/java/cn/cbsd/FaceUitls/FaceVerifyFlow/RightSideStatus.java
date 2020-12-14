package cn.cbsd.FaceUitls.FaceVerifyFlow;

import android.graphics.Rect;
import android.support.annotation.NonNull;
import android.util.Log;

import java.util.ArrayList;

import cn.cbsd.aliveandfacedetect.Func.Func_Camera.mvp.presenter.PhotoPresenter;

public class RightSideStatus extends VerifyStatus {
    private static RightSideStatus instance = null;

    public static RightSideStatus getInstance() {
        if (instance == null)
            instance = new RightSideStatus();
        return instance;
    }

    private RightSideStatus() {
        super();
        describe_Text = "向右摆头";
        template = MediaHelper.VoiceTemplate.RightSideStatus;
        tools = LeftSideStatus.getInstance().tools;
    }


    @Override
    public void dealData(FaceVerifyContext faceContext, byte[] data, int width, int height, ArrayList<Rect> rectS) {
        if (running) {
            if (SuccessCount < threshold) {
                if (PhotoPresenter.equipmentType.equals(PhotoPresenter.EquipmentType.phone)) {
                    tools.detectRects(data, width, height, rectS, 90, false);
                    if (rectS.size() > 0) {
                        rectS.get(0).left = height - rectS.get(0).left;
                        rectS.get(0).right = height - rectS.get(0).right;
                    }

                } else {
                    tools.detectRects(data, width, height, rectS, 0, true);
                    if (rectS.size() > 0) {
                        rectS.get(0).left = width - rectS.get(0).left;
                        rectS.get(0).right = width - rectS.get(0).right;
                    }
                }
                if (rectS.size() > 0) {
                    SuccessCount++;
                    Log.e("RightSideCount", String.valueOf(SuccessCount));

                }
            } else {
                stop();
                faceContext.setStatus(CompleteStatus.getInstance());
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
        return "RightSideStatus";
    }


}
