package eparon.nxtremotecontroller.View;

import android.content.Context;
import android.util.AttributeSet;

import androidx.appcompat.widget.AppCompatImageButton;

public class SquareImageButton extends AppCompatImageButton {

    public SquareImageButton (Context context) {
        super(context);
    }

    public SquareImageButton (Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public SquareImageButton (Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }


    @Override
    protected void onMeasure (int widthMeasureSpec, int heightMeasureSpec) {
        int width = MeasureSpec.getSize(widthMeasureSpec);
        int height = MeasureSpec.getSize(heightMeasureSpec);

        if (width > height)
            width = height;
        else
            height = width;

        super.onMeasure(
                MeasureSpec.makeMeasureSpec(width, MeasureSpec.EXACTLY),
                MeasureSpec.makeMeasureSpec(height, MeasureSpec.EXACTLY)
        );
    }

}
