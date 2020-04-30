package eparon.nxtremotecontroller.View;

import android.content.Context;
import android.util.AttributeSet;

public class TankViewVertical extends TankView {

    public TankViewVertical (Context context) {
        super(context);
    }

    public TankViewVertical (Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public TankViewVertical (Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    @Override
    protected float barWidth () {
        return 0.4f;
    }

    @Override
    protected float lineThickness () {
        return 6f;
    }

}
