package cn.cbsd.FaceUitls.FaceVerifyFlow;

import android.graphics.Rect;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import java.util.ArrayList;

public class FaceVerifyContext {

    private VerifyStatus status;

    private ConvertEvent event;

    public FaceVerifyContext(ConvertEvent event) {
        this.event = event;
    }

    public VerifyStatus getStatus() {
        return status;
    }

    private Handler handler = new Handler(Looper.getMainLooper());

    public void setStatus(final VerifyStatus status) {
        this.status = status;
        handler.post(new Runnable() {
            @Override
            public void run() {
                event.Message(status.describe_Text);
//                event.Voice(status.template);
                Log.e("message", "当前状态为" + status.toString());
            }
        });

    }

    public void dealData(FaceVerifyContext faceContext, byte[] data, int width, int height,
                         ArrayList<Rect> rects) {
        faceContext.getStatus().dealData(faceContext, data, width, height, rects);
    }

    public void prepare(FaceVerifyContext faceContext) {
        FrontStatus.getInstance();
        LeftSideStatus.getInstance();
        RightSideStatus.getInstance();
//        OpenMouthStatus.getInstance();
//        SmileStatus.getInstance();
        faceContext.setStatus(FrontStatus.getInstance());
    }

    public void stop(FaceVerifyContext faceContext) {
        faceContext.getStatus().stop();
        faceContext.setStatus(StopStatus.getInstance());
    }

    public void release() {
        FrontStatus.getInstance().release();
        LeftSideStatus.getInstance().release();
        RightSideStatus.getInstance().release();
//        SmileStatus.getInstance().release();
    }

    public interface ConvertEvent {

        void Message(String Text);

        void Voice(MediaHelper.VoiceTemplate temp);

    }


}
