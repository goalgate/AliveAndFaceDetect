package cn.cbsd.FaceUitls.FaceVerifyFlow;

import android.graphics.Rect;

import java.util.ArrayList;

import cn.cbsd.FaceUitls.FaceDetectTools;


public abstract class VerifyStatus {

    public static VerifyStatus instance = null;

    //状态转变阈值
    int threshold = 50;

    int minFace = 50;

    int SuccessCount = 0;

    FaceDetectTools tools;

    boolean running = false;

    String describe_Text = null;

    MediaHelper.VoiceTemplate template = null;

    public VerifyStatus() {
        this.running = true;
        this.SuccessCount = 0;
    }

    public abstract void dealData(FaceVerifyContext context, byte[] data, int width, int height
            , ArrayList<Rect> rects);

    public void stop() {
        running = false;
    }


}
