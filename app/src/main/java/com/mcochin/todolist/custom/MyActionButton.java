package com.mcochin.todolist.custom;

import android.content.Context;
import android.util.AttributeSet;
import android.view.animation.TranslateAnimation;
import android.widget.RelativeLayout;

import com.software.shell.fab.ActionButton;

/**
 * Created by Marco on 5/31/2015.
 */
public class MyActionButton extends ActionButton {
    //private AnimationListener mAnimationListener;
    private float mDistance = 0;

    public MyActionButton(Context context) {
        super(context);
    }

    public MyActionButton(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public MyActionButton(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    /*public interface AnimationListener {
        void onAnimationStart();
        void onAnimationEnd();
    }

    public void setAnimationListener(final AnimationListener listener) {
        mAnimationListener = listener;
    }*/

    private enum TranslateDirection{
        DOWN, UP
    }

    @Override
    protected void onAnimationStart() {
        super.onAnimationStart();
        setEnabled(false);
    }

    @Override
    protected void onAnimationEnd() {
        super.onAnimationEnd();
        setEnabled(true);

        if(mDistance != 0) {
            RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) getLayoutParams();
            params.bottomMargin -= mDistance;
            setLayoutParams(params);
            mDistance = 0;
        }
    }

    /***
     *
     * @param distance The amount you want translated in pixels.
     * @param duration The length of the animation.
     */
    public void translateUp(float distance, long duration){
        translate(TranslateDirection.UP, distance, duration);
    }

    /***
     *
     * @param distance The amount you want translated in pixels.
     * @param duration The length of the animation.
     */
    public void translateDown(float distance, long duration){
        translate(TranslateDirection.DOWN, distance, duration);
    }

    private void translate(TranslateDirection direction, float distance, long duration){
        if(direction == TranslateDirection.DOWN) {
            mDistance = distance;
        } else {
            mDistance = -distance;
        }
        //distances are relative to the button itself
        TranslateAnimation anim = new TranslateAnimation(0, 0, 0, mDistance);
        anim.setDuration(duration);
        startAnimation(anim);
    }
}
