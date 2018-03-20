package com.sean.www.utils;

import android.content.Context;

/**
 * author: machenshuang
 * created on: 2018/03/20 20:49
 * description:
 */

public class DimensUtil {
    public static int dip2px(Context context, float dip) {
        if (context == null) {
            return dip2px(1.5f, dip);
        }
        return dip2px(context.getResources().getDisplayMetrics().density, dip);
    }

    public static int dip2px(float scale, float dip) {
        return (int)(dip * scale + 0.5f);
    }

    public static int sp2px(Context context, float sp) {
        if (context == null) {
            return sp2px(1.5f, sp);
        }
        return sp2px(context.getResources().getDisplayMetrics().scaledDensity, sp);
    }

    public static int sp2px(float scale, float sp) {
        return (int)(sp * scale + 0.5f);
    }

    public static int px2dip(Context context, float px) {
        if (context == null) {
            return px2dip(1.5f, px);
        }
        return px2dip(context.getResources().getDisplayMetrics().density, px);
    }

    public static int px2dip(float scale, float px) {
        return (int)(px / scale + 0.5f);
    }
}
