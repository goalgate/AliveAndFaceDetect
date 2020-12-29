package cn.cbsd.FaceUitls.FaceVerifyFlow;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Rect;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import cn.cbsd.aliveandfacedetect.AppInit;
import cn.cbsd.aliveandfacedetect.FileUtils;
import cn.cbsd.aliveandfacedetect.R;
import cn.cbsd.network.RetrofitGenerator;
import io.reactivex.Observable;
import io.reactivex.Observer;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Consumer;
import io.reactivex.schedulers.Schedulers;
import okhttp3.ResponseBody;

public class FaceVerifyContext {

    private VerifyStatus status;

    private ConvertEvent event;

    private List<Bitmap> bitmapList;

    Disposable noticeDisposable;

    Disposable OutTimeDisposable;

    public FaceVerifyContext(ConvertEvent event) {
        bitmapList = new ArrayList<>();
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
                event.Voice(status.template);
                if (status.getClass().getName().equals(CompleteStatus.class.getName())) {
                    event.FaceGet(bitmapList);

                }
                Log.e("message", "当前状态为" + status.toString());
            }
        });
    }

    public void dealData(FaceVerifyContext faceContext, byte[] data, int width, int height,
                         ArrayList<Rect> rects) {
        faceContext.getStatus().dealData(faceContext, data, width, height, rects);
        if (status.getClass().getName().equals(FrontStatus.class.getName()) ||
                status.getClass().getName().equals(LeftSideStatus.class.getName()) ||
                status.getClass().getName().equals(RightSideStatus.class.getName())) {
            if (rects.size() == 0) {
                if (noticeDisposable == null) {
                    noticeDisposable = Observable.timer(5, TimeUnit.SECONDS).subscribe(new Consumer<Long>() {
                        @Override
                        public void accept(Long aLong) throws Exception {
                            event.Voice(MediaHelper.VoiceTemplate.adjust);
                            Log.e("Sdsd", "Ddsad");
                        }
                    });
                }
                if (OutTimeDisposable == null) {
                    OutTimeDisposable = Observable.timer(10, TimeUnit.SECONDS).subscribe(new Consumer<Long>() {
                        @Override
                        public void accept(Long aLong) throws Exception {
                            OutTime(FaceVerifyContext.this);
                        }
                    });
                }
            } else {
                if (noticeDisposable != null) {
                    noticeDisposable.dispose();
                    noticeDisposable = null;
                }
                if (OutTimeDisposable != null) {
                    OutTimeDisposable.dispose();
                    OutTimeDisposable = null;
                }
            }


        }
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
        if (noticeDisposable != null) {
            noticeDisposable.dispose();
            noticeDisposable = null;
        }
        if (OutTimeDisposable != null) {
            OutTimeDisposable.dispose();
            OutTimeDisposable = null;
        }
        faceContext.getStatus().stop();
        faceContext.setStatus(StopStatus.getInstance());
    }

    public void OutTime(FaceVerifyContext faceContext) {
        faceContext.getStatus().stop();
        faceContext.setStatus(OutTimeStatus.getInstance());

//        event.Failed();
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

        void FaceGet(List<Bitmap> bitmapList);

    }

    public void addBitmap(Bitmap mbitmap) {
        bitmapList.add(mbitmap);

    }


}
