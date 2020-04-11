package eparon.nxtremotecontroller.View;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.View;

public class TouchPadView extends View {

    public TouchPadView (Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        // TODO Auto-generated constructor stub
    }

    public TouchPadView (Context context, AttributeSet attrs) {
        super(context, attrs);
        // TODO Auto-generated constructor stub
    }

    public TouchPadView (Context context) {
        super(context);
        // TODO Auto-generated constructor stub
    }

    public float mCx, mCy;
    public float mRadius;
    public float mOffset;

    @SuppressLint("DrawAllocation")
    @Override
    protected void onDraw (Canvas canvas) {
        canvas.drawRGB(0, 0, 0);
        Paint paint = new Paint();
        paint.setColor(0xff00ff00);
        paint.setStyle(Paint.Style.STROKE);

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
    protected void onSizeChanged (int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);

        mCx = (float)(w / 2);
        mCy = (float)(h / 2);
        if (h >= 1.2f * w)
            mRadius = 0.9f * w * 0.5f;
        else
            mRadius = 0.9f * h * 5f / 12f;
        mOffset = mRadius * 0.2f;
    }

}
