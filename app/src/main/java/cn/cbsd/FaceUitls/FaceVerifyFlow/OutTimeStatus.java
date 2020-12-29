package cn.cbsd.FaceUitls.FaceVerifyFlow;

import android.graphics.Rect;

import java.util.ArrayList;

public class OutTimeStatus extends VerifyStatus {

    private static OutTimeStatus instance = null;

    public static OutTimeStatus getInstance() {
        if (instance == null)
            instance = new OutTimeStatus();
        return instance;
    }

    private OutTimeStatus() {
        super();
        describe_Text = "人脸识别已超时";
        template = MediaHelper.VoiceTemplate.OutTIME;
    }


    public void release() {
        if (instance != null)
            instance = null;
    }

    @Override
    public void dealData(FaceVerifyContext context, byte[] data, int width, int height, ArrayList<Rect> rectS) {

    }
}
