package eparon.nxtremotecontroller.View;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.View;

public class TouchPadView extends View {

    private Paint paint;

    public float mCx, mCy;
    public float mRadius;
    public float mOffset;

    public TouchPadView (Context context) {
        super(context);
        paint = new Paint();
    }

    public TouchPadView (Context context, AttributeSet attrs) {
        super(context, attrs);
        paint = new Paint();
    }

    public TouchPadView (Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        paint = new Paint();
    }

    protected float lineThickness () {
        return 4f;
    }

    @SuppressLint("DrawAllocation")
    @Override
    protected void onDraw (Canvas canvas) {
        canvas.drawARGB(0, 0, 0, 0);
        paint.setColor(0xff00ff00);
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(lineThickness());

        for (int i = 1; i <= 6; i++) {
            canvas.drawArc(new RectF(mCx - mRadius * i / 6.0f, mCy - mOffset - mRadius * i / 6.0f, mCx + mRadius * i / 6.0f, mCy - mOffset + mRadius * i / 6.0f), 180f, 180f, false, paint);
            canvas.drawArc(new RectF(mCx - mRadius * i / 6.0f, mCy + mOffset - mRadius * i / 6.0f, mCx + mRadius * i / 6.0f, mCy + mOffset + mRadius * i / 6.0f), 0f, 180f, false, paint);
        }

        canvas.drawLine(mCx + 0.16666f * mRadius, mCy - mOffset, mCx + mRadius, mCy - mOffset, paint);
        canvas.drawLine(mCx - 0.16666f * mRadius, mCy - mOffset, mCx - mRadius, mCy - mOffset, paint);
        canvas.drawLine(mCx + 0.16666f * mRadius, mCy + mOffset, mCx + mRadius, mCy + mOffset, paint);
        canvas.drawLine(mCx - 0.16666f * mRadius, mCy + mOffset, mCx - mRadius, mCy + mOffset, paint);
        canvas.drawLine(mCx, mCy + mOffset + 0.16666f * mRadius, mCx, mCy + mOffset + mRadius, paint);
        canvas.drawLine(mCx, mCy - mOffset - 0.16666f * mRadius, mCx, mCy - mOffset - mRadius, paint);
        canvas.drawLine(mCx + 0.16666f * mRadius * 0.70710f, mCy + mOffset + 0.16666f * mRadius * 0.70710f, mCx + mRadius * 0.70710f, mCy + mOffset + mRadius * 0.70710f, paint);
        canvas.drawLine(mCx - 0.16666f * mRadius * 0.70710f, mCy + mOffset + 0.16666f * mRadius * 0.70710f, mCx - mRadius * 0.70710f, mCy + mOffset + mRadius * 0.70710f, paint);
        canvas.drawLine(mCx + 0.16666f * mRadius * 0.70710f, mCy - mOffset - 0.16666f * mRadius * 0.70710f, mCx + mRadius * 0.70710f, mCy - mOffset - mRadius * 0.70710f, paint);
        canvas.drawLine(mCx - 0.16666f * mRadius * 0.70710f, mCy - mOffset - 0.16666f * mRadius * 0.70710f, mCx - mRadius * 0.70710f, mCy - mOffset - mRadius * 0.70710f, paint);
    }

    @Override
    protected void onSizeChanged (int width, int height, int oldWidth, int oldHeight) {
        super.onSizeChanged(width, height, oldWidth, oldHeight);

        mCx = (float)(width / 2);
        mCy = (float)(height / 2);

        if (height >= 1.2f * width)
            mRadius = 0.9f * width * 0.5f;
        else
            mRadius = 0.9f * height * 5f / 12f;

        mOffset = mRadius * 0.2f;
    }

}
