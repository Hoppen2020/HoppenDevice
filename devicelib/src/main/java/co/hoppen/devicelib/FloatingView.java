package co.hoppen.devicelib;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.PixelFormat;
import android.os.Build;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.WindowManager;
import android.view.animation.DecelerateInterpolator;

import com.blankj.utilcode.util.LogUtils;
import com.blankj.utilcode.util.PermissionUtils;
import com.blankj.utilcode.util.ScreenUtils;
import com.blankj.utilcode.util.Utils;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by YangJianHui on 2021/9/27.
 */
abstract class FloatingView implements View.OnTouchListener {
    private final WindowManager windowManager;
    private final WindowManager.LayoutParams layoutParams;
    private View floatingView;

    private boolean showing;
    private boolean firstShow = true;


    private float lastX, lastY, changeX, changeY;
    private int newX, newY;
    private float downX,downY;
    private float upX,upY;

    private float slideMargin = 100;
    private long duration = 300;

    private AnimatorSet animatorSet;
    private List<Animator> animatorList;
    private boolean click;
    private int slop;



    public FloatingView(LayoutInflater inflater){
        windowManager = (WindowManager) Utils.getApp().getSystemService(Context.WINDOW_SERVICE);
        layoutParams = new WindowManager.LayoutParams();
        layoutParams.format = PixelFormat.RGBA_8888;
        layoutParams.flags = WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL
                | WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
                | WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS;
        layoutParams.windowAnimations = 0;
        layoutParams.width = WindowManager.LayoutParams.WRAP_CONTENT;
        layoutParams.height = WindowManager.LayoutParams.WRAP_CONTENT;
        layoutParams.gravity = getGravity();
        layoutParams.x = getInitialPositionX();
        layoutParams.y = getInitialPositionY();
        floatingView = inflater.inflate(layoutId(),null);
        if (touchable())floatingView.setOnTouchListener(this);

        animatorSet = new AnimatorSet();
        animatorList = new ArrayList<>();
        animatorSet.setDuration(duration);
        slop = ViewConfiguration.get(Utils.getApp().getApplicationContext()).getScaledTouchSlop();

    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        switch (event.getAction()){
            case MotionEvent.ACTION_DOWN:
                downX = event.getRawX();
                downY = event.getRawY();
                lastX = event.getRawX();
                lastY = event.getRawY();
                cancelAnimator();
                break;
            case MotionEvent.ACTION_MOVE:
                changeX = event.getRawX() - lastX;
                changeY = event.getRawY() - lastY;
//                LogUtils.e(changeX,changeY);
                newX = (int) (getX() + changeX);
                newY = (int) (getY() + changeY);
                updateXY(newX,newY);
                lastX = event.getRawX();
                lastY = event.getRawY();
                break;
            case MotionEvent.ACTION_UP:
                upX = event.getRawX();
                upY = event.getRawY();

                click = (Math.abs(upX - downX) > slop) || (Math.abs(upY - downY) > slop);

                int startX = newX;
                int endX = newX;
                if (newX<=slideMargin){
                    endX = 0;
                }else if (ScreenUtils.getScreenWidth() - slideMargin - v.getWidth() <=newX){
                    endX = ScreenUtils.getScreenWidth() - v.getWidth();
                }else {
                    endX = newX;
                }

                int startY = newY;
                int endY = newY;
                if (newY<=slideMargin){
                    endY = 0;
                }else if (ScreenUtils.getScreenHeight() - slideMargin - v.getHeight() <=newY){
                    endY = ScreenUtils.getScreenHeight() - v.getHeight();
                }else {
                    endY = newY;
                }
                if (startX!=endX){
                    createAnimatorListener(ObjectAnimator.ofInt(startX, endX), animation -> {
                        int x = (int) animation.getAnimatedValue();
                        updateX(x);
                    });
                }
                if (startY!=endY){
                    createAnimatorListener(ObjectAnimator.ofInt(startY, endY), animation -> {
                        int y = (int) animation.getAnimatedValue();
                        updateY(y);
                    });
                }
                startAnimator();
                break;
        }
        return click;
    }

    private void createAnimatorListener(ValueAnimator valueAnimator, ValueAnimator.AnimatorUpdateListener updateListener){
        valueAnimator.addUpdateListener(updateListener);
        valueAnimator.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                ValueAnimator valueAnimator =((ValueAnimator)animation);
                valueAnimator.removeAllUpdateListeners();
                valueAnimator.removeAllListeners();
                animatorList.remove(valueAnimator);
            }
        });
        animatorList.add(valueAnimator);
    }

    private void startAnimator() {
        if (animatorList.size()>0){
            animatorSet.setInterpolator(new DecelerateInterpolator());
            animatorSet.setDuration(duration).playTogether(animatorList);
            animatorSet.start();
        }
    }

    private void cancelAnimator() {
        if (animatorSet!=null && animatorSet.isRunning()){
            animatorSet.cancel();
        }
    }


    public View getFloatingView() {
        return floatingView;
    }

    public WindowManager getWindowManager() {
        return windowManager;
    }

    public void show() {
        if (isShowing())return;
        if (firstShow){
            layoutParams.type = Build.VERSION.SDK_INT >= Build.VERSION_CODES.O?
                    WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY:
                    WindowManager.LayoutParams.TYPE_PHONE;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                if (!PermissionUtils.isGrantedDrawOverlays()){
                    PermissionUtils.requestDrawOverlays(null);
                }else  {
                    windowManager.addView(floatingView,layoutParams);
                    setShowing(true);
                }
            }else {
                windowManager.addView(floatingView,layoutParams);
                setShowing(true);
            }
        }else {
            floatingView.setVisibility(View.VISIBLE);
            setShowing(true);
        }
    }

    public void gone() {
        floatingView.setVisibility(View.GONE);
        setShowing(false);
    }

    public void dismiss() {
        windowManager.removeView(floatingView);
        setShowing(false);
    }

    public boolean isShowing() {
        return showing;
    }

    private void setShowing(boolean showing) {
        this.showing = showing;
        firstShow = false;
    }

    public int getX() {
        return (floatingView!=null&&layoutParams!=null)?layoutParams.x:0;
    }

    public int getY() {
        return (floatingView!=null&&layoutParams!=null)?layoutParams.y:0;
    }

    protected abstract int getInitialPositionX();

    protected abstract int getInitialPositionY();

    protected abstract int getGravity();

    public abstract int layoutId();

    protected void updateXY(int x, int y) {
        if (layoutParams!=null){
            layoutParams.x = x;
            layoutParams.y = y;
            windowManager.updateViewLayout(floatingView, layoutParams);
        }
    }

    protected void updateX(int x) {
        if (layoutParams!=null){
            layoutParams.x = x;
            windowManager.updateViewLayout(floatingView, layoutParams);
        }
    }

    protected void updateY(int y) {
        if (layoutParams!=null){
            layoutParams.y = y;
            windowManager.updateViewLayout(floatingView, layoutParams);
        }
    }

    protected abstract boolean touchable();

    protected void updateWH(int width,int height){
        if (layoutParams!=null){
            layoutParams.width = width;
            layoutParams.height = height;
            windowManager.updateViewLayout(floatingView, layoutParams);
        }
    }

}
