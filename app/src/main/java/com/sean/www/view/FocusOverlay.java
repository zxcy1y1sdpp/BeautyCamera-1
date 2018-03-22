package com.sean.www.view;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.view.MotionEvent;
import android.view.View;

import com.sean.www.activity.CameraActivity;

/**
 * author: machenshuang
 * created on: 2018/03/20 20:30
 * description:
 */

public class FocusOverlay extends View {

    private CameraActivity mActivity;
    private Paint mPaint = new Paint(Paint.FILTER_BITMAP_FLAG | Paint.ANTI_ALIAS_FLAG);

    public FocusOverlay(Context context) {
        super(context);
        mActivity = (CameraActivity) context;
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent event) {
        return super.dispatchTouchEvent(event);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (mActivity != null) {
            return mActivity.touchEvent(event);
        }
        return false;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        mActivity.draw(canvas, mPaint);
    }
}
