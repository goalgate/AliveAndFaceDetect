package cn.cbsd.FaceUitls.FaceVerifyFlow;

import android.graphics.Rect;
import android.support.annotation.NonNull;

import java.util.ArrayList;

public class CompleteStatus extends VerifyStatus {
    private static CompleteStatus instance = null;

    public static CompleteStatus getInstance() {
        if (instance == null)
            instance = new CompleteStatus();
        return instance;
    }

    private CompleteStatus() {
        super();
        describe_Text = "人脸信息采集完毕";
        template = MediaHelper.VoiceTemplate.CompleteStatus;
    }

    @Override
    public void dealData(FaceVerifyContext context, byte[] data, int width, int height, ArrayList<Rect> rectS) {

    }

    @NonNull
    @Override
    public String toString() {
        return "CompleteStatus";
    }
}
