package com.pulldownlistview;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.os.Handler;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;

/**
 * @description: 底部运动的自定义ImageView
 * @author: Administrator
 * @time: 2016/3/17 22:33
 */
public class YProgressView extends ImageView {

    private static final String TAG = "YProgressView";
    int TOP_BOTTOM_MARGIN_OUTER = 10;
    int TOP_BOTTOM_MARGIN_INNER = 10;
    float progress;
    Paint mPaint;
    float progress2;
    Handler mHandler = new Handler();
    boolean isAnimate;
    RectF outerRectF;
    RectF innerRectF;

    public YProgressView(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.setWillNotDraw(false);
        mPaint = new Paint();
        mPaint.setAntiAlias(true);
        progress = 0.0f;
        progress2 = 0.0f;
        isAnimate = false;

        outerRectF = new RectF();
        innerRectF = new RectF();

        setBackgroundColor(Color.BLUE);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);

        int marginSize = getWidth() / 4;
        Log.i(TAG, "marginSize = " + marginSize + " , Width = " + getWidth() + " , Height = " + getHeight());
        /**
         * @param left   The X coordinate of the left side of the rectangle
         * @param top    The Y coordinate of the top of the rectangle
         * @param right  The X coordinate of the right side of the rectangle
         * @param bottom The Y coordinate of the bottom of the rectangle
         */
        outerRectF.set(2, 2 + TOP_BOTTOM_MARGIN_OUTER, getWidth() - 2, getHeight() - 2 - TOP_BOTTOM_MARGIN_OUTER);

        innerRectF.set(marginSize, marginSize + TOP_BOTTOM_MARGIN_INNER, getWidth() - marginSize, getHeight() - marginSize - TOP_BOTTOM_MARGIN_INNER);
    }

    @Override
    public void onDraw(Canvas canvas) {

        Log.i(TAG, "onDraw = " + progress);

        mPaint.setColor(Color.WHITE);
        mPaint.setStyle(Paint.Style.STROKE);
        mPaint.setStrokeWidth(2);

        /**
         * 以时钟三点为0度； 完整角度以360°来计算；
         * oval 限定绘图区域
         * startAngle 为起始角度；
         * sweepAngle 表示划过的角度，如果为负值则逆时针画
         * useCenter 如果为true则画出扇形效果，否则只有一个圆弧
         */
        // 绘制270°~360°
        canvas.drawArc(outerRectF, 270, 90 * progress, false, mPaint);
        // 绘制270°~180°
        canvas.drawArc(outerRectF, 270, -90 * progress, false, mPaint);
        // 绘制90°~0°
        canvas.drawArc(outerRectF, 90, -90 * progress, false, mPaint);
        // 绘制90°~180°
        canvas.drawArc(outerRectF, 90, 90 * progress, false, mPaint);

        canvas.drawArc(innerRectF, 270, 90 * progress, false, mPaint);
        canvas.drawArc(innerRectF, 270, -90 * progress, false, mPaint);
        canvas.drawArc(innerRectF, 90, -90 * progress, false, mPaint);
        canvas.drawArc(innerRectF, 90, 90 * progress, false, mPaint);

        if (isAnimate) {
            mPaint.setColor(Color.BLACK);
            mPaint.setStrokeWidth(5);
            mPaint.setStrokeJoin(Paint.Join.ROUND); // 圆角
            mPaint.setStrokeCap(Paint.Cap.ROUND); // 圆角
            canvas.drawArc(innerRectF, 270 + 360 * progress2, 10, false, mPaint);
            canvas.drawArc(outerRectF, 270 - 360 * progress2, 5, false, mPaint);
        }

    }

    public void setProgress(float progress) {
        this.progress = progress;
        this.invalidate();
    }

    public void startAnimate() {
        if (!isAnimate) {
            isAnimate = true;
            mHandler.post(mRunnable);
        }

    }

    public void stopAnimate() {
        isAnimate = false;
        mHandler.removeCallbacks(mRunnable);

    }

    public Runnable mRunnable = new Runnable() {

        @Override
        public void run() {
            progress2 += 0.01;
            if (isAnimate) {
                mHandler.postDelayed(this, 10);
            }
            YProgressView.this.invalidate();
        }

    };


}
