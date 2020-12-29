package com.cbsd.mvphelper.mvplibrary.mvpforView;

import android.content.Context;
import android.content.res.Resources;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;

import com.cbsd.mvphelper.mvplibrary.Tools.ActivityCollector;
import com.cbsd.mvphelper.mvplibrary.Tools.KnifeUtils;
import com.tbruyelle.rxpermissions2.RxPermissions;
import com.trello.rxlifecycle2.components.support.RxAppCompatActivity;

import org.greenrobot.eventbus.EventBus;


public abstract class MVPBaseActivity<P extends IPresent> extends RxAppCompatActivity implements IView<P> {

    private RxPermissions rxPermissions;

    private P p;

    private static Context mContext;

    public MVPBaseActivity() {
        mContext = this;
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setStatusBarVisibility(this.getWindow(),false);
        ActivityCollector.getInstance().addActivity(this);
        if (getLayoutId() > 0) {
            setContentView(getLayoutId());

            bindUI(null);
            if (useEventBus()) {
                EventBus.getDefault().register(this);
            }
            bindEvent();
        }
        initData(savedInstanceState);
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (getP() != null) {
            getP().onStart();
        }

    }

    @Override
    protected void onResume() {
        super.onResume();
        if (getP() != null) {
            getP().onResume();
        }
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        if (getP() != null) {
            getP().onRestart();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (getP() != null) {
            getP().onPause();
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (getP() != null) {
            getP().onStop();
        }
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (useEventBus()) {
            EventBus.getDefault().unregister(this);
        }
        if (getP() != null) {
            getP().detachV();
            getP().onDestroy();
        }
        ActivityCollector.getInstance().removeActivity(this);
    }

    @Override
    public void bindUI(View rootView) {
        KnifeUtils.bind(this);
    }

    @Override
    public void bindEvent() {
        if (getP() != null) {
            getP().onCreate();
        }
    }


    @Override
    public boolean useEventBus() {
        return false;
    }

    protected RxPermissions getRxPermissions() {
        rxPermissions = new RxPermissions(this);
        return rxPermissions;
    }

    @Override
    public void showLoading() {

    }

    @Override
    public void hideLoading() {

    }

    protected P getP() {
        if (p == null) {
            p = newP();
            if (p != null) {
                p.attachV(this);
            }
        }
        return p;
    }


    private static final String TAG_STATUS_BAR = "TAG_STATUS_BAR";

    private static final String TAG_OFFSET = "TAG_OFFSET";

    private static final int KEY_OFFSET = -123;

    public static void setStatusBarVisibility(@NonNull final Window window,
                                              final boolean isVisible) {
        if (isVisible) {
            window.clearFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
            showStatusBarView(window);
            addMarginTopEqualStatusBarHeight(window);
        } else {
            window.addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
            hideStatusBarView(window);
            subtractMarginTopEqualStatusBarHeight(window);
        }
    }

    private static void showStatusBarView(final Window window) {
        ViewGroup decorView = (ViewGroup) window.getDecorView();
        View fakeStatusBarView = decorView.findViewWithTag(TAG_STATUS_BAR);
        if (fakeStatusBarView == null) return;
        fakeStatusBarView.setVisibility(View.VISIBLE);
    }

    private static void hideStatusBarView(final Window window) {
        ViewGroup decorView = (ViewGroup) window.getDecorView();
        View fakeStatusBarView = decorView.findViewWithTag(TAG_STATUS_BAR);
        if (fakeStatusBarView == null) return;
        fakeStatusBarView.setVisibility(View.GONE);
    }

    private static void addMarginTopEqualStatusBarHeight(final Window window) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.KITKAT) return;
        View withTag = window.getDecorView().findViewWithTag(TAG_OFFSET);
        if (withTag == null) return;
        addMarginTopEqualStatusBarHeight(withTag);
    }

    private static void subtractMarginTopEqualStatusBarHeight(final Window window) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.KITKAT) return;
        View withTag = window.getDecorView().findViewWithTag(TAG_OFFSET);
        if (withTag == null) return;
        subtractMarginTopEqualStatusBarHeight(withTag);
    }


    public static void addMarginTopEqualStatusBarHeight(@NonNull View view) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.KITKAT) return;
        view.setTag(TAG_OFFSET);
        Object haveSetOffset = view.getTag(KEY_OFFSET);
        if (haveSetOffset != null && (Boolean) haveSetOffset) return;
        ViewGroup.MarginLayoutParams layoutParams = (ViewGroup.MarginLayoutParams) view.getLayoutParams();
        layoutParams.setMargins(layoutParams.leftMargin,
                layoutParams.topMargin + getStatusBarHeight(),
                layoutParams.rightMargin,
                layoutParams.bottomMargin);
        view.setTag(KEY_OFFSET, true);
    }

    /**
     * Subtract the top margin size equals status bar's height for view.
     *
     * @param view The view.
     */
    public static void subtractMarginTopEqualStatusBarHeight(@NonNull View view) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.KITKAT) return;
        Object haveSetOffset = view.getTag(KEY_OFFSET);
        if (haveSetOffset == null || !(Boolean) haveSetOffset) return;
        ViewGroup.MarginLayoutParams layoutParams = (ViewGroup.MarginLayoutParams) view.getLayoutParams();
        layoutParams.setMargins(layoutParams.leftMargin,
                layoutParams.topMargin - getStatusBarHeight(),
                layoutParams.rightMargin,
                layoutParams.bottomMargin);
        view.setTag(KEY_OFFSET, false);
    }

    public static int getStatusBarHeight() {
        Resources resources = mContext.getResources();
        int resourceId = resources.getIdentifier("status_bar_height", "dimen", "android");
        return resources.getDimensionPixelSize(resourceId);
    }
}
