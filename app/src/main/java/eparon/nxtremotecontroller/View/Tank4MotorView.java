package eparon.nxtremotecontroller.View;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.View;

import static eparon.nxtremotecontroller.View.TankView.levelsColorArray;

public class Tank4MotorView extends View {

    private Paint borderPaint, touchPaint;
    private int[] dtIndex = new int[] {-1, -1, -1, -1};

    public int mWidth;
    public int mHeight;
    public float mZero;
    public float mRange;

    public Tank4MotorView (Context context) {
        super(context);
        borderPaint = new Paint();
        touchPaint = new Paint();
    }

    public Tank4MotorView (Context context, AttributeSet attrs) {
        super(context, attrs);
        borderPaint = new Paint();
        touchPaint = new Paint();
    }

    public Tank4MotorView (Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        borderPaint = new Paint();
        touchPaint = new Paint();
    }

    protected float barWidth () {
        return 0.2f;
    }

    protected float lineThickness () {
        return 3f;
    }

    @Override
    protected void onDraw (Canvas canvas) {
        canvas.drawARGB(0, 0, 0, 0);
        borderPaint.setColor(Color.GREEN);
        borderPaint.setStyle(Paint.Style.STROKE);
        borderPaint.setStrokeWidth(lineThickness());
        touchPaint.setStyle(Paint.Style.FILL);

        float x0 = lineThickness() / 2;
        float x1 = mWidth * barWidth();
        float x2 = mWidth * ((11.0f / 30.0f) - barWidth() / 2.0f);
        float x3 = mWidth * ((11.0f / 30.0f) + barWidth() / 2.0f);
        float x4 = mWidth * ((19.0f / 30.0f) - barWidth() / 2.0f);
        float x5 = mWidth * ((19.0f / 30.0f) + barWidth() / 2.0f);
        float x6 = mWidth * (1 - barWidth());
        float x7 = mWidth - 1 - (lineThickness() / 2);

        if (dtIndex[0] != -1) {
            float topDrawPosition = dtIndex[0] * -1f + 4;
            touchPaint.setColor(levelsColorArray[dtIndex[0] - 1]);
            canvas.drawRect(x0, mZero + topDrawPosition / 4f * mRange, x1, mZero + mRange, touchPaint);
        }
        if (dtIndex[1] != -1) {
            float topDrawPosition = dtIndex[1] * -1f + 4;
            touchPaint.setColor(levelsColorArray[dtIndex[1] - 1]);
            canvas.drawRect(x2, mZero + topDrawPosition / 4f * mRange, x3, mZero + mRange, touchPaint);
        }
        if (dtIndex[2] != -1) {
            float topDrawPosition = dtIndex[2] * -1f + 4;
            touchPaint.setColor(levelsColorArray[dtIndex[2] - 1]);
            canvas.drawRect(x4, mZero + topDrawPosition / 4f * mRange, x5, mZero + mRange, touchPaint);
        }
        if (dtIndex[3] != -1) {
            float topDrawPosition = dtIndex[3] * -1f + 4;
            touchPaint.setColor(levelsColorArray[dtIndex[3] - 1]);
            canvas.drawRect(x6, mZero + topDrawPosition / 4f * mRange, x7, mZero + mRange, touchPaint);
        }

        canvas.drawRect(x0, mZero - mRange, x1, mZero + mRange, borderPaint);
        canvas.drawRect(x2, mZero - mRange, x3, mZero + mRange, borderPaint);
        canvas.drawRect(x4, mZero - mRange, x5, mZero + mRange, borderPaint);
        canvas.drawRect(x6, mZero - mRange, x7, mZero + mRange, borderPaint);

        for (int i = 0; i < 4; i++) {
            canvas.drawLine(x0, mZero + i / 4f * mRange, x1, mZero + i / 4f * mRange, borderPaint);
            canvas.drawLine(x2, mZero + i / 4f * mRange, x3, mZero + i / 4f * mRange, borderPaint);
            canvas.drawLine(x4, mZero + i / 4f * mRange, x5, mZero + i / 4f * mRange, borderPaint);
            canvas.drawLine(x6, mZero + i / 4f * mRange, x7, mZero + i / 4f * mRange, borderPaint);
            canvas.drawLine(x0, mZero - i / 4f * mRange, x1, mZero - i / 4f * mRange, borderPaint);
            canvas.drawLine(x2, mZero - i / 4f * mRange, x3, mZero - i / 4f * mRange, borderPaint);
            canvas.drawLine(x4, mZero - i / 4f * mRange, x5, mZero - i / 4f * mRange, borderPaint);
            canvas.drawLine(x6, mZero - i / 4f * mRange, x7, mZero - i / 4f * mRange, borderPaint);
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

    public void drawTouchAction (int[] positions) {
        this.dtIndex = positions;
        this.invalidate();
    }

    public void resetTouchActions () {
        this.dtIndex = new int[] {-1, -1, -1, -1};
        this.invalidate();
    }

}

