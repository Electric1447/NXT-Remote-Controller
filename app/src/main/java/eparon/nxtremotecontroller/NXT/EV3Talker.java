package eparon.nxtremotecontroller.NXT;

import android.os.Handler;

public class EV3Talker extends NXTTalker {

    public EV3Talker (Handler handler) {
        super(handler);
    }

    //region Motor Commands

    public void EV3_Motors (byte port, byte power) {
        byte[] data = {(byte)13, (byte)0, (byte)0, (byte)0, Byte.MIN_VALUE, (byte)0, (byte)0, (byte)-92, (byte)0, port, (byte)-127, power, (byte)-90, (byte)0, port};

        Write(data);
    }

    public void EV3_StopMotors (byte port) {
        byte[] data = {(byte)9, (byte)0, (byte)0, (byte)0, Byte.MIN_VALUE, (byte)0, (byte)0, (byte)-93, (byte)0, port, (byte)0};

        Write(data);
    }

    //endregion

}
