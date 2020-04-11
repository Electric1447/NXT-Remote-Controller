package eparon.nxtremotecontroller.View;

import android.content.Context;
import android.util.AttributeSet;

public class TankViewVertical extends TankView {

    public TankViewVertical (Context context) {
        super(context);
        // TODO Auto-generated constructor stub
    }

    public TankViewVertical (Context context, AttributeSet attrs) {
        super(context, attrs);
        // TODO Auto-generated constructor stub
    }

    public TankViewVertical (Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        // TODO Auto-generated constructor stub
    }

    @Override
    protected float barWidth () {
        return 0.4f;
    }

}
