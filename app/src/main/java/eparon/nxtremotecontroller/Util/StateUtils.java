package eparon.nxtremotecontroller.Util;

import android.content.Context;
import android.graphics.Color;

import eparon.nxtremotecontroller.NXT.NXTTalker;
import eparon.nxtremotecontroller.R;

public class StateUtils {

    public static String getStateText (int state, Context context) {
        switch (state) {
            case NXTTalker.STATE_NONE:
                return context.getString(R.string.conn_state_not_connected);
            case NXTTalker.STATE_CONNECTING:
                return context.getString(R.string.conn_state_connecting);
            case NXTTalker.STATE_CONNECTED:
                return context.getString(R.string.conn_state_connected);
            default:
                throw new IllegalStateException("Unexpected value: " + state);
        }
    }

    public static int getStateTextColor (int state) {
        switch (state) {
            case NXTTalker.STATE_NONE:
                return Color.RED;
            case NXTTalker.STATE_CONNECTING:
                return Color.YELLOW;
            case NXTTalker.STATE_CONNECTED:
                return Color.GREEN;
            default:
                throw new IllegalStateException("Unexpected value: " + state);
        }
    }

    public static String getConnectionButtonText (int state, Context context) {
        switch (state) {
            case NXTTalker.STATE_NONE:
                return context.getString(R.string.conn_btn_connect);
            case NXTTalker.STATE_CONNECTING:
                return context.getString(R.string.conn_state_connecting);
            case NXTTalker.STATE_CONNECTED:
                return context.getString(R.string.conn_btn_disconnect);
            default:
                throw new IllegalStateException("Unexpected value: " + state);
        }
    }

    public static boolean ConnectionButtonState (int state) {
        return state != NXTTalker.STATE_CONNECTING;
    }

}
