package ru.yandex.droidparty.tensorflow;

import android.annotation.TargetApi;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.os.Build;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

public class DrawView extends View {

    private Paint mPaint;

    private Paint mBgPaint;

    private Path mPath;

    private int mWidth;

    private int mHeight;

    public interface OnDrawFinishedListener {
        void onBitmapReady(final Bitmap bmp);
    }

    private OnDrawFinishedListener mListener;

    private Runnable notifyDrawingFinished = new Runnable() {
        @Override
        public void run() {
            if (mListener != null) {
                Bitmap b = Bitmap.createBitmap(mWidth, mHeight, Bitmap.Config.ARGB_8888);
                Canvas c = new Canvas(b);
                DrawView.this.draw(c);
                Bitmap scaledBmp = Bitmap.createScaledBitmap(b, 28, 28, false);
                mListener.onBitmapReady(scaledBmp);
            }
        }
    };

    public DrawView(Context context) {
        super(context);
        init();
    }

    public DrawView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public DrawView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public DrawView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init();
    }

    private void init() {
        mPaint = new Paint();
        mPaint.setColor(Color.BLACK);
        mPaint.setStyle(Paint.Style.STROKE);
        mPaint.setStrokeWidth(90);
        mPath = new Path();

        mBgPaint = new Paint();
        mBgPaint.setColor(Color.WHITE);
        mBgPaint.setStyle(Paint.Style.FILL_AND_STROKE);
    }

    public void setListener(OnDrawFinishedListener listener) {
        mListener = listener;
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        mWidth = w;
        mHeight = h;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        canvas.drawRect(0, 0, mWidth, mHeight, mBgPaint);
        canvas.drawPath(mPath, mPaint);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        final int action = event.getAction();
        switch (action) {
            case MotionEvent.ACTION_DOWN:
                handleActionDown(event);
                break;
            case MotionEvent.ACTION_MOVE:
                handleActionMove(event);
                break;
            case MotionEvent.ACTION_CANCEL:
            case MotionEvent.ACTION_UP:
                handleActionUp(event);
        }
        return true;
    }

    private void handleActionUp(final MotionEvent action) {
        postDelayed(notifyDrawingFinished, 1000);
    }

    private void handleActionMove(final MotionEvent action) {
        removeCallbacks(notifyDrawingFinished);
        mPath.lineTo(action.getX(), action.getY());
        invalidate();
    }

    private void handleActionDown(final MotionEvent action) {
        removeCallbacks(notifyDrawingFinished);
        mPath.reset();
        mPath.moveTo(action.getX(), action.getY());
        invalidate();
    }
}
