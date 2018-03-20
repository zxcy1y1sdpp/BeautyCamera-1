package com.sean.www.helper;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.view.animation.OvershootInterpolator;

import com.sean.www.utils.DimensUtil;
import com.sean.www.view.FocusOverlay;

/**
 * author: machenshuang
 * created on: 2018/03/20 19:50
 * description:
 */

public class FocusHelper {
    private static final int FOCUS_WAITING = 0;
    private static final int FOCUS_SUCCESS = 1;
    public static final int FOCUS_FAILED = 2;
    public static final int FOCUS_DONE = 3;
    private int mAmplitude;
    private OvershootInterpolator mInterpolator;
    private static int MAX_STAY_TIME = 500;
    private static final int FOCUS_DURATION_MS = 500;
    private FocusOverlay mFocusOverlay;
    private float mScaledDensity;
    private boolean mHasFocusArea = false;
    private int mFocusScreenX = 0;
    private int mFocusScreenY = 0;
    private long mFocusCompleteTime = -1;
    private int mOuterCircleRadius;
    private int mInnerCircleRadius;
    private int mOuterCircleWidth;
    private int mInnerCircleWidth;
    private long mFocusStartTime;
    private int mFocusState = FOCUS_WAITING;
    private boolean mIsAutoFocus;
    private int mFocusWaitingState;
    private long mKeepTimestamp;

    public FocusHelper(Context context, FocusOverlay focusOverlay){
        mScaledDensity = context.getResources().getDisplayMetrics().scaledDensity;
        mOuterCircleRadius = DimensUtil.dip2px(mScaledDensity,40);
        mInnerCircleRadius = DimensUtil.dip2px(mScaledDensity,15);
        mOuterCircleWidth = DimensUtil.dip2px(mScaledDensity,2);
        mInnerCircleWidth = DimensUtil.dip2px(mScaledDensity,3);
        mAmplitude = DimensUtil.dip2px(mScaledDensity, 8);
        mInterpolator = new OvershootInterpolator();
        mFocusOverlay = focusOverlay;
    }

    public boolean hasFocusArea(){
        return mHasFocusArea;
    }

    public void setHasFocusArea(boolean hasFocusArea){
        mHasFocusArea = hasFocusArea;
    }

    public void setFocusScreen(int focusScreenX, int focusScreenY){

    }

    public void setFocusComplete(int focusSuccess, long focusCompleteTime){
        if (focusSuccess == FOCUS_WAITING){
            mFocusStartTime = System.currentTimeMillis();
        }
        mFocusCompleteTime = focusCompleteTime;
        setFocusState(focusSuccess);

    }


    public void setFocusState(int focusState) {
        this.mFocusState = focusState;
        mFocusOverlay.postInvalidate();
    }

    public int getFocusState(){
        return mFocusState;
    }

    public void clearFocusState() {
        if (mFocusState == FOCUS_SUCCESS || mFocusState == FOCUS_FAILED) {
            setFocusState(FOCUS_DONE);
        }
    }

    public boolean isFocusNone(){
        return mFocusState == FOCUS_DONE;
    }

    public boolean isFocusWaiting(){
        if (mFocusState == FOCUS_WAITING){
            return System.currentTimeMillis() - mFocusStartTime < 5000;
        }
        return false;
    }

    public int getFocusWaitingState(){
        return mFocusWaitingState;
    }

    public void setFocusWaitingState(int focusWaitingState) {
        mFocusWaitingState = focusWaitingState;
    }

    public boolean draw(Canvas canvas, Paint paint, Rect clipBounds, boolean keep){
        if (!isAutoFocus()){
            return false;
        }
        if (isFocusNone()){
            return false;
        }

        long currentTime = System.currentTimeMillis();
        long stayTime = currentTime - mFocusCompleteTime;

        if (keep && mKeepTimestamp == 0){
            mKeepTimestamp = currentTime;
        } else if (!keep && mKeepTimestamp!=0){
            mKeepTimestamp = 0;
        }

        if (!isFocusWaiting()
                && stayTime > MAX_STAY_TIME
                && !(keep && mFocusCompleteTime > mKeepTimestamp)) {
            return false;
        }

        canvas.save();
        int posX;
        int posY;
        if (mHasFocusArea){
            posX = mFocusScreenX;
            posY = mFocusScreenY;
        } else {
            posX = clipBounds.width()/2;
            posY = clipBounds.height()/2;
        }

        paint.setStyle(Paint.Style.STROKE);
        paint.setAntiAlias(true);
        paint.setColor(Color.WHITE);
        paint.setStrokeWidth(mOuterCircleWidth);
        canvas.drawCircle(posX + clipBounds.left, posY + clipBounds.top,
                mOuterCircleRadius, paint);

        float inerCircleR = mInnerCircleRadius;
        long focusTime = currentTime - mFocusStartTime;

        if (focusTime < FOCUS_DURATION_MS){
            inerCircleR = mInterpolator.getInterpolation(focusTime)
        }
    }

    public synchronized boolean isAutoFocus() {
        return mIsAutoFocus;
    }

    public synchronized void setAutoFocus(boolean isAutoFocus) {
        this.mIsAutoFocus = isAutoFocus;
    }
}
