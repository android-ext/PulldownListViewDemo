package com.pulldownlistview;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuff.Mode;
import android.graphics.PorterDuffXfermode;
import android.graphics.Paint.Style;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.util.AttributeSet;
import android.util.Log;
import android.widget.ImageView;

public class EyeView extends ImageView {

    Paint mPaint;
    float progress;
    boolean isAnimate;
    int rotateProgress;
    Handler mHandler = new Handler();

    public EyeView(Context context, AttributeSet attrs) {
        super(context, attrs);
        mPaint = new Paint();
        mPaint.setAntiAlias(true);
        rotateProgress = 0;
        progress = 0.0f;
    }

    @Override
    public void onDraw(Canvas canvas) {
        int minWidth = (int) (this.getWidth() * progress);
        int minHeight = (int) (this.getHeight() * progress);
        if (minWidth > 1 && minHeight > 1) {
            Bitmap bitmap = getBitmap();
            if (bitmap != null)  {
                canvas.drawBitmap(bitmap, 0, 0, null);
                bitmap.recycle();
            }
        }
    }

    public Bitmap getBitmap() {
        Bitmap origin1 = null;
        Bitmap origin2 = null;

        if (progress >= 1.0) {
            origin1 = getBitmapFromRes(R.drawable.eye_light1);
            if (origin1 == null)
                return null;
            if ((origin2 = getBitmapFromRes(R.drawable.eye_light2)) == null) {
                return null;
            }
        } else {
            origin1 = getBitmapFromRes(R.drawable.eye_gray_1);
            if (origin1 == null)
                return null;
            if ((origin2 = getBitmapFromRes(R.drawable.eye_gray_2)) == null) {
                return null;
            }
        }

        Paint paint = new Paint();
        paint.setAntiAlias(true);
        float scale = (float) origin1.getWidth() / (float) getWidth();
        int maxWidth = (int) (origin1.getWidth() / scale);
        int maxHeight = (int) (origin1.getHeight() / scale);

//        int maskSize = 1;
//
//        if (progress > 0.3f) {
//            maskSize = (int) (maxHeight * (progress - 0.3) / 0.7);
//        }

        // 创建一个缩放的位图
        Bitmap temp1 = Bitmap.createScaledBitmap(origin1, maxWidth, maxHeight, true);
        Canvas canvas = new Canvas();
//         创建一个可变的位图
//        Bitmap mask = Bitmap.createBitmap(temp1.getWidth(), temp1.getWidth(), Config.ARGB_8888);
//        canvas.setBitmap(mask);
//        canvas.drawCircle(mask.getWidth() / 2, mask.getHeight() / 2,  maskSize, mPaint);

        Bitmap bitmap = Bitmap.createBitmap(getWidth(), getHeight(), Config.ARGB_8888);
        canvas.setBitmap(bitmap);
        canvas.drawBitmap(temp1, (getWidth() - temp1.getWidth()) / 2, (getHeight() - temp1.getHeight()) / 2, paint);

//        paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.DST_IN)); // 取两层绘制交集, 显示下层。
//        canvas.drawBitmap(mask, (getWidth() - mask.getWidth()) / 2, (getHeight() - mask.getHeight()) / 2, paint);
//        paint.setXfermode(null);

        float scaleProgress = progress / 0.3f;
        if (scaleProgress > 1.0f) {
            scaleProgress = 1.0f;
        }

        Bitmap temp2 = Bitmap.createScaledBitmap(origin2, (int) (maxWidth * scaleProgress), (int) (maxHeight * scaleProgress), true);
        Matrix matrix = new Matrix();
        matrix.postRotate(rotateProgress);
        temp2 = Bitmap.createBitmap(temp2, 0, 0, temp2.getWidth(), temp2.getHeight(), matrix, false);
        canvas.drawBitmap(temp2, (getWidth() - temp2.getWidth()) / 2, (getHeight() - temp2.getHeight()) / 2, paint);

        temp1.recycle();
        temp2.recycle();
//        mask.recycle();
        return bitmap;
    }

    private Bitmap getBitmapFromRes(int res) {

        BitmapDrawable drawable = (BitmapDrawable) this.getResources().getDrawable(res);
        return drawable.getBitmap();
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
        rotateProgress = 0;

    }

    public Runnable mRunnable = new Runnable() {

        @Override
        public void run() {
            rotateProgress += 10;
            if (rotateProgress > 360) {
                rotateProgress = 0;
            }

            if (isAnimate) {
                mHandler.postDelayed(this, 10);
            }
            EyeView.this.invalidate();
        }

    };

}
