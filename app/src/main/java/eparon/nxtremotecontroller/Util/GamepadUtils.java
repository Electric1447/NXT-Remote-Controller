package eparon.nxtremotecontroller.Util;

import android.view.InputDevice;
import android.view.MotionEvent;

public class GamepadUtils {

    public static final float JOYSTICK_DEADZONE = 0.2f, TRIGGER_DEADZONE = 0.1f;

    public static float getCenteredAxis (MotionEvent event, InputDevice device, int axis) {
        final InputDevice.MotionRange range = device.getMotionRange(axis, event.getSource());
        if (range != null) {
            final float flat = range.getFlat();
            final float value = event.getAxisValue(axis);

            // Ignore axis values that are within the 'flat' region of the joystick axis center.
            // A joystick at rest does not always report an absolute position of (0,0).
            if (Math.abs(value) > flat)
                return value;
        }
        return 0;
    }

}
