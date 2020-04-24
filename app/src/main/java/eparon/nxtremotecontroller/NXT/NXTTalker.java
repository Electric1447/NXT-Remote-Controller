package eparon.nxtremotecontroller.NXT;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.os.Handler;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.UUID;

public class NXTTalker {

    private static final String commUUID = "00001101-0000-1000-8000-00805F9B34FB";

    public static final int STATE_NONE = 0;
    public static final int STATE_CONNECTING = 1;
    public static final int STATE_CONNECTED = 2;

    private int mState;
    private Handler mHandler;
    private BluetoothAdapter mAdapter;

    private ConnectThread mConnectThread;
    private ConnectedThread mConnectedThread;

    public NXTTalker (Handler handler) {
        mAdapter = BluetoothAdapter.getDefaultAdapter();
        mHandler = handler;
        setState(STATE_NONE);
    }

    private synchronized void setState (int state) {
        mState = state;
        if (mHandler != null)
            mHandler.obtainMessage(2, state, -1).sendToTarget();
    }

    @SuppressWarnings("unused")
    public synchronized int getState () {
        return mState;
    }

    public synchronized void Connect (BluetoothDevice device) {
        if (mState == STATE_CONNECTING)
            dropConnectThread();
        dropConnectedThread();

        mConnectThread = new ConnectThread(device);
        mConnectThread.start();

        setState(STATE_CONNECTING);
    }

    public synchronized void Connected (BluetoothSocket socket) {
        dropConnectThread();
        dropConnectedThread();

        mConnectedThread = new ConnectedThread(socket);
        mConnectedThread.start();

        setState(STATE_CONNECTED);
    }

    public synchronized void Stop () {
        dropConnectThread();
        dropConnectedThread();
        setState(STATE_NONE);
    }

    private void dropConnectThread () {
        if (mConnectThread != null) {
            mConnectThread.cancel();
            mConnectThread = null;
        }
    }

    private void dropConnectedThread () {
        if (mConnectedThread != null) {
            mConnectedThread.cancel();
            mConnectedThread = null;
        }
    }

    @SuppressWarnings("unused")
    private void connectionFailed () {
        setState(STATE_NONE);
    }

    private void connectionLost () {
        setState(STATE_NONE);
    }

    //region Motor Commands

    public void Motor (int motor, byte power, boolean speedReg, boolean motorSync) {
        byte[] data = {0x0c, 0x00, (byte)0x80, 0x04, 0x00, 0x32, 0x07, 0x00, 0x00, 0x20, 0x00, 0x00, 0x00, 0x00};

        if (motor == 1)
            data[4] = 0x01;
        if (motor == 2)
            data[4] = 0x02;

        data[5] = power;

        if (speedReg)
            data[7] |= 0x01;

        if (motorSync)
            data[7] |= 0x02;

        Write(data);
    }

    public void Motors (byte l, byte r, boolean speedReg, boolean motorSync) {
        byte[] data = {0x0c, 0x00, (byte)0x80, 0x04, 0x02, 0x32, 0x07, 0x00, 0x00, 0x20, 0x00, 0x00, 0x00, 0x00,
                0x0c, 0x00, (byte)0x80, 0x04, 0x01, 0x32, 0x07, 0x00, 0x00, 0x20, 0x00, 0x00, 0x00, 0x00};

        data[5] = l;
        data[19] = r;

        if (speedReg) {
            data[7] |= 0x01;
            data[21] |= 0x01;
        }

        if (motorSync) {
            data[7] |= 0x02;
            data[21] |= 0x02;
        }

        Write(data);
    }

    public void Motors3 (byte l, byte r, byte action, boolean speedReg, boolean motorSync) {
        byte[] data = {0x0c, 0x00, (byte)0x80, 0x04, 0x02, 0x32, 0x07, 0x00, 0x00, 0x20, 0x00, 0x00, 0x00, 0x00,
                0x0c, 0x00, (byte)0x80, 0x04, 0x01, 0x32, 0x07, 0x00, 0x00, 0x20, 0x00, 0x00, 0x00, 0x00,
                0x0c, 0x00, (byte)0x80, 0x04, 0x00, 0x32, 0x07, 0x00, 0x00, 0x20, 0x00, 0x00, 0x00, 0x00};

        data[5] = l;
        data[19] = r;
        data[33] = action;

        if (speedReg) {
            data[7] |= 0x01;
            data[21] |= 0x01;
        }

        if (motorSync) {
            data[7] |= 0x02;
            data[21] |= 0x02;
        }

        Write(data);
    }

    //endregion

    private void Write (byte[] out) {
        ConnectedThread r;
        synchronized (this) {
            if (mState != STATE_CONNECTED)
                return;
            r = mConnectedThread;
        }
        r.write(out);
    }

    private class ConnectThread extends Thread {
        private BluetoothSocket mmSocket;
        private final BluetoothDevice mmDevice;

        ConnectThread (BluetoothDevice device) {
            mmDevice = device;
        }

        public void run () {
            setName("ConnectThread");
            mAdapter.cancelDiscovery();

            try {
                mmSocket = mmDevice.createRfcommSocketToServiceRecord(UUID.fromString(commUUID));
                mmSocket.connect();
            } catch (IOException e) {
                e.printStackTrace();
            }

            synchronized (NXTTalker.this) {
                mConnectThread = null;
            }

            Connected(mmSocket);
        }

        void cancel () {
            try {
                if (mmSocket != null)
                    mmSocket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

    }

    private class ConnectedThread extends Thread {
        private final BluetoothSocket mmSocket;
        private final InputStream mmInStream;
        private final OutputStream mmOutStream;

        public ConnectedThread (BluetoothSocket socket) {
            mmSocket = socket;
            InputStream tmpIn = null;
            OutputStream tmpOut = null;

            try {
                tmpIn = socket.getInputStream();
                tmpOut = socket.getOutputStream();
            } catch (IOException e) {
                e.printStackTrace();
            }

            mmInStream = tmpIn;
            mmOutStream = tmpOut;
        }

        @SuppressWarnings({"unused", "UnusedAssignment"})
        public void run () {
            byte[] buffer = new byte[1024];
            int bytes;

            while (true)
                try {
                    bytes = mmInStream.read(buffer);
                } catch (IOException e) {
                    e.printStackTrace();
                    connectionLost();
                    break;
                }
        }

        void write (byte[] buffer) {
            try {
                mmOutStream.write(buffer);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        void cancel () {
            try {
                mmSocket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

    }

}
