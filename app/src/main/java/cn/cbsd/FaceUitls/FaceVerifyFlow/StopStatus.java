package cn.cbsd.FaceUitls.FaceVerifyFlow;

import android.graphics.Rect;

import java.util.ArrayList;

public class StopStatus extends VerifyStatus {
    private static StopStatus instance = null;

    public static StopStatus getInstance() {
        if (instance == null)
            instance = new StopStatus();
        return instance;
    }

    private StopStatus() {
        super();
        describe_Text = "人脸识别已停止";
        template = MediaHelper.VoiceTemplate.StopStatus;
    }


    public void release() {
        if (instance != null)
            instance = null;
    }

    @Override
    public void dealData(FaceVerifyContext context, byte[] data, int width, int height, ArrayList<Rect> rectS) {

    }
}
