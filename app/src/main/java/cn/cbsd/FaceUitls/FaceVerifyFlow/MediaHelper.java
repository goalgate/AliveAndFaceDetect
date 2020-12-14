package cn.cbsd.FaceUitls.FaceVerifyFlow;

import android.content.Context;
import android.content.res.AssetFileDescriptor;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Handler;
import android.util.Log;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;

import cn.cbsd.aliveandfacedetect.AppInit;


public class MediaHelper {
    public enum VoiceTemplate {
        FrontStatus, LeftSideStatus,
        RightSideStatus, SmileStatus,
        OpenMouthStatus, StopStatus,
        CompleteStatus
    }

    private static MediaPlayer mediaPlayer;

    public static void mediaOpen() {
        mediaPlayer = new MediaPlayer();
        AudioManager audioMgr = (AudioManager) AppInit.getContext().getSystemService(Context.AUDIO_SERVICE);
        int maxVolume = audioMgr.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
        audioMgr.setStreamVolume(AudioManager.STREAM_MUSIC, maxVolume,
                AudioManager.FLAG_PLAY_SOUND);
        Log.e("信息提示", "打开音量");
        try {
            Class<?> cMediaTimeProvider = Class.forName("android.media.MediaTimeProvider");
            Class<?> cSubtitleController = Class.forName("android.media.SubtitleController");
            Class<?> iSubtitleControllerAnchor = Class.forName("android.media.SubtitleController$Anchor");
            Class<?> iSubtitleControllerListener = Class.forName("android.media.SubtitleController$Listener");
            Constructor constructor = cSubtitleController.getConstructor(
                    new Class[]{Context.class, cMediaTimeProvider, iSubtitleControllerListener});
            Object subtitleInstance = constructor.newInstance(AppInit.getContext(), null, null);
            Field f = cSubtitleController.getDeclaredField("mHandler");
            f.setAccessible(true);
            try {
                f.set(subtitleInstance, new Handler());
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            } finally {
                f.setAccessible(false);
            }
            Method setsubtitleanchor = mediaPlayer.getClass().getMethod("setSubtitleAnchor",
                    cSubtitleController, iSubtitleControllerAnchor);
            setsubtitleanchor.invoke(mediaPlayer, subtitleInstance, null);
        } catch (Exception e) {
            e.printStackTrace();
        }


    }

    public static void play(VoiceTemplate text) {
        try {
            AssetFileDescriptor fileDescriptor;
            switch (text) {
                case FrontStatus:
                    fileDescriptor = AppInit.getContext().getAssets()
                            .openFd("mp3" + File.separator + "FrontStatus.mp3");
                    play(fileDescriptor);
                    break;

                case LeftSideStatus:
                    fileDescriptor = AppInit.getContext().getAssets()
                            .openFd("mp3" + File.separator + "LeftSideStatus.mp3");
                    play(fileDescriptor);
                    break;
                case RightSideStatus:
                    fileDescriptor = AppInit.getContext().getAssets()
                            .openFd("mp3" + File.separator + "RightSideStatus.mp3");
                    play(fileDescriptor);
                    break;
                case OpenMouthStatus:
                    fileDescriptor = AppInit.getContext().getAssets()
                            .openFd("mp3" + File.separator + "OpenMouthStatus.mp3");
                    play(fileDescriptor);
                    break;
                case SmileStatus:
                    fileDescriptor = AppInit.getContext().getAssets()
                            .openFd("mp3" + File.separator + "SmileStatus.mp3");
                    play(fileDescriptor);
                    break;
                case StopStatus:
                    fileDescriptor = AppInit.getContext().getAssets()
                            .openFd("mp3" + File.separator + "StopStatus.mp3");
                    play(fileDescriptor);
                    break;
                case CompleteStatus:
                    fileDescriptor = AppInit.getContext().getAssets()
                            .openFd("mp3" + File.separator + "CompleteStatus.mp3");
                    play(fileDescriptor);
                    break;
                default:
                    break;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    private static void play(AssetFileDescriptor fileDescriptor) {
        try {
            mediaPlayer.reset();
            mediaPlayer.setDataSource(fileDescriptor.getFileDescriptor(), fileDescriptor.getStartOffset(), fileDescriptor.getLength());
            mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
            mediaPlayer.prepareAsync();
            mediaPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
                @Override
                public void onPrepared(MediaPlayer mp) {
                    mediaPlayer.start();
                }
            });
        } catch (IOException e) {
            e.printStackTrace();
        }


    }

    public static void mediaRealese() {
        if (mediaPlayer != null) {
            if (mediaPlayer.isPlaying()) {
                mediaPlayer.stop();
            }
            Log.e("信息提示", "mediaPlayer解除函数被触发");
            mediaPlayer.release();
            mediaPlayer = null;
        }
    }
}



