package com.semye.scratchcard;

import android.graphics.Color;
import android.graphics.Paint;

/**
 * Created by yesheng on 16/11/9.
 */
public class PaintUtils {

    public static Paint getEarser() {
        Paint paint = new Paint();
        paint.setColor(Color.parseColor("#c0c0c0"));
        paint.setAntiAlias(true);// 抗锯齿
        paint.setDither(true);// 防抖动
        paint.setStrokeJoin(Paint.Join.ROUND);// 设置连接方式为圆角
        paint.setStrokeCap(Paint.Cap.ROUND);// 设置画笔笔刷类型
        paint.setStyle(Paint.Style.FILL);
        paint.setStrokeWidth(200);// 设置画笔的一个宽度
        return paint;
    }

    public static Paint getTextPaint(int mTextColor, float mTextSize) {
        Paint paint = new Paint();
        paint.setColor(mTextColor);// 画笔颜色
        paint.setAntiAlias(true);// 抗锯齿
        paint.setStrokeJoin(Paint.Join.ROUND);// 设置连接方式为圆角
        paint.setDither(true);// 防抖动
        paint.setStyle(Paint.Style.FILL);// 设置画笔的填充方式为实心
        paint.setTextSize(mTextSize);// 设置文本字体大小
        return paint;
    }

}
