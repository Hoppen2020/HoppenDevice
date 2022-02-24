package co.hoppen.devicelib;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewAnimationUtils;
import android.view.ViewGroup;
import android.view.animation.DecelerateInterpolator;
import android.widget.TextView;

import com.blankj.utilcode.util.LogUtils;
import com.blankj.utilcode.util.ScreenUtils;
import com.blankj.utilcode.util.Utils;

/**
 * Created by YangJianHui on 2021/9/27.
 */
class FloatingBall extends FloatingView implements View.OnClickListener {

    private View content,title,clean,debugText;
    private int maxWidth;
    private int maxHeight;
    private int minWidth;
    private int minHeight;
    private int ballRadius;
    private Animator unfoldAnimator;

    private boolean unfold = false;

    private int duration = 500 * 2;

    public FloatingBall(LayoutInflater inflater) {
        super(inflater);
        content = getFloatingView().findViewById(R.id.cv_content);
        title = getFloatingView().findViewById(R.id.ll_title);
        clean = getFloatingView().findViewById(R.id.tv_clean);
        debugText = getFloatingView().findViewById(R.id.tv_debug);
        clean.setOnClickListener(this);
        maxWidth = (int) (ScreenUtils.getScreenWidth() * 0.5);
        maxHeight = (int) (ScreenUtils.getScreenHeight() * 0.5);
        ballRadius = Utils.getApp().getResources().getDimensionPixelSize(R.dimen.floating_ball_radius);
        minWidth = minHeight = ballRadius * 2;
        getFloatingView().setOnClickListener(v -> {
            zoom();
        });
    }

    @Override
    protected int getInitialPositionX() {
        return 0;
    }

    @Override
    protected int getInitialPositionY() {
        return 0;
    }

    @Override
    protected int getGravity() {
        return Gravity.START|Gravity.TOP;
    }

    @Override
    public int layoutId() {
        return R.layout.floating_ball;
    }

    @Override
    protected boolean touchable() {
        return true;
    }

    private void zoom(){
        if (unfoldAnimator!=null&&unfoldAnimator.isRunning()) return;
        boolean tempUnfold = !unfold;
        int zoomRadius = (int) (Math.max(maxHeight, maxWidth)*1.3);
        int centerX = ballRadius;
        int centerY = ballRadius;
        unfoldAnimator = ViewAnimationUtils.createCircularReveal(content, centerX, centerY, tempUnfold?ballRadius:zoomRadius, tempUnfold?zoomRadius:ballRadius);
        unfoldAnimator.setInterpolator(new DecelerateInterpolator());
        unfoldAnimator.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                if (!tempUnfold){
                    ViewGroup.LayoutParams layoutParams = content.getLayoutParams();
                    layoutParams.width = minWidth;
                    layoutParams.height = minHeight;
                    content.setLayoutParams(layoutParams);
                    clean.setVisibility(View.GONE);
                    debugText.setVisibility(View.GONE);
                }
                unfold = !unfold;
            }

            @Override
            public void onAnimationStart(Animator animation) {
                if (tempUnfold){
                    ViewGroup.LayoutParams layoutParams = content.getLayoutParams();
                    layoutParams.height = maxHeight;
                    layoutParams.width = maxWidth;
                    content.setLayoutParams(layoutParams);
                    clean.setVisibility(View.VISIBLE);
                    debugText.setVisibility(View.VISIBLE);
                }
            }
        });
        unfoldAnimator.setDuration(duration);
        unfoldAnimator.start();
        ValueAnimator animator = ObjectAnimator.ofFloat(title, "alpha",tempUnfold?0.3f:1,tempUnfold?1:0.3f);
        animator.setDuration(duration);
        animator.start();
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.tv_clean) {
            LogUtils.e("clean");
        }
    }

    public void addText(String string){
        TextView tv = ((TextView)debugText);
        string = tv.getText().toString() + string + "\n";
        tv.setText(string);

    }
}
