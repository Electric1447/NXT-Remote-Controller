package eparon.nxtremotecontroller.View;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.View;

public class Tank3MotorView extends View {

    private Paint borderPaint;

    public int mWidth;
    public int mHeight;
    public float mZero;
    public float mRange;

    public Tank3MotorView (Context context) {
        super(context);
        borderPaint = new Paint();
    }

    public Tank3MotorView (Context context, AttributeSet attrs) {
        super(context, attrs);
        borderPaint = new Paint();
    }

    public Tank3MotorView (Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        borderPaint = new Paint();
    }

    protected float barWidth () {
        return 0.3f;
    }

    protected float lineThickness () {
        return 4f;
    }

    @Override
    protected void onDraw (Canvas canvas) {
        canvas.drawRGB(0, 0, 0);
        borderPaint.setColor(Color.GREEN);
        borderPaint.setStyle(Paint.Style.STROKE);
        borderPaint.setStrokeWidth(lineThickness());

        float x0 = lineThickness() / 2;
        float x1 = mWidth * barWidth();
        float x2 = mWidth * (0.5f - barWidth() / 2.0f);
        float x3 = mWidth * (0.5f + barWidth() / 2.0f);
        float x4 = mWidth * (1 - barWidth());
        float x5 = mWidth - 1 - (lineThickness() / 2);

        canvas.drawRect(x0, mZero - mRange, x1, mZero + mRange, borderPaint);
        canvas.drawRect(x2, mZero - mRange, x3, mZero + mRange, borderPaint);
        canvas.drawRect(x4, mZero - mRange, x5, mZero + mRange, borderPaint);

        for (int i = 0; i < 4; i++) {
            canvas.drawLine(x0, mZero + i / 4f * mRange, x1, mZero + i / 4f * mRange, borderPaint);
            canvas.drawLine(x2, mZero + i / 4f * mRange, x3, mZero + i / 4f * mRange, borderPaint);
            canvas.drawLine(x4, mZero + i / 4f * mRange, x5, mZero + i / 4f * mRange, borderPaint);
            canvas.drawLine(x0, mZero - i / 4f * mRange, x1, mZero - i / 4f * mRange, borderPaint);
            canvas.drawLine(x2, mZero - i / 4f * mRange, x3, mZero - i / 4f * mRange, borderPaint);
            canvas.drawLine(x4, mZero - i / 4f * mRange, x5, mZero - i / 4f * mRange, borderPaint);
        }
    }

    @Override
    protected void onSizeChanged (int width, int height, int oldWidth, int oldHeight) {
        super.onSizeChanged(width, height, oldWidth, oldHeight);

        mWidth = width;
        mHeight = height;
        mZero = mHeight / 2f;
        mRange = 0.85f * mHeight / 2f;
    }

}
