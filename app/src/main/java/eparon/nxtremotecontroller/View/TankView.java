package eparon.nxtremotecontroller.View;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.View;

public class TankView extends View {

    private Paint borderPaint, touchPaint;
    private int[] dtIndex = new int[] {-1, -1};

    public int mWidth;
    public int mHeight;
    public float mZero;
    public float mRange;

    public static final int[] levelsColorArray = new int[] {0xBFB7FF00, 0xBFE5FF00, 0xBFFFEA00, 0xBFFFBB00, 0xBFFF8C00, 0xBFFF5E00, 0xBFFF2F00, 0xBFFF0000};

    public TankView (Context context) {
        super(context);
        borderPaint = new Paint();
        touchPaint = new Paint();
    }

    public TankView (Context context, AttributeSet attrs) {
        super(context, attrs);
        borderPaint = new Paint();
        touchPaint = new Paint();
    }

    public TankView (Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        borderPaint = new Paint();
        touchPaint = new Paint();
    }

    protected float barWidth () {
        return 0.25f;
    }

    protected float lineThickness () {
        return 4f;
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
        float x2 = mWidth * (1 - barWidth());
        float x3 = mWidth - 1 - (lineThickness() / 2);

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

        canvas.drawRect(x0, mZero - mRange, x1, mZero + mRange, borderPaint);
        canvas.drawRect(x2, mZero - mRange, x3, mZero + mRange, borderPaint);

        for (int i = 0; i < 4; i++) {
            canvas.drawLine(x0, mZero + i / 4f * mRange, x1, mZero + i / 4f * mRange, borderPaint);
            canvas.drawLine(x2, mZero + i / 4f * mRange, x3, mZero + i / 4f * mRange, borderPaint);
            canvas.drawLine(x0, mZero - i / 4f * mRange, x1, mZero - i / 4f * mRange, borderPaint);
            canvas.drawLine(x2, mZero - i / 4f * mRange, x3, mZero - i / 4f * mRange, borderPaint);
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
        this.dtIndex = new int[] {-1, -1};
        this.invalidate();
    }

}
