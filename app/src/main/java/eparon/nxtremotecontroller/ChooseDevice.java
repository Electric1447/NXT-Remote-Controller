package eparon.nxtremotecontroller;

import java.util.Set;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothClass;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.AdapterView.OnItemClickListener;

public class ChooseDevice extends Activity {

    public static String EXTRA_DEVICE_ADDRESS = "device_address";

    private ArrayAdapter<String> mPairedDevicesArrayAdapter, mNewDevicesArrayAdapter;
    private BluetoothAdapter mBtAdapter;

    Button scanButton;

    @Override
    protected void onCreate (Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.device_list);
        setResult(Activity.RESULT_CANCELED);

        scanButton = findViewById(R.id.button_scan);
        scanButton.setOnClickListener(v -> {
            doDiscovery();
            scanButton.setText(getString(R.string.scan_scanning));
            scanButton.setEnabled(false);
        });

        mPairedDevicesArrayAdapter = new ArrayAdapter<>(this, R.layout.device_name);
        mNewDevicesArrayAdapter = new ArrayAdapter<>(this, R.layout.device_name);

        ListView pairedListView = findViewById(R.id.paired_devices);
        pairedListView.setAdapter(mPairedDevicesArrayAdapter);
        pairedListView.setOnItemClickListener(mDeviceClickListener);

        ListView newDevicesListView = findViewById(R.id.new_devices);
        newDevicesListView.setAdapter(mNewDevicesArrayAdapter);
        newDevicesListView.setOnItemClickListener(mDeviceClickListener);

        this.registerReceiver(mReceiver, new IntentFilter(BluetoothDevice.ACTION_FOUND));
        this.registerReceiver(mReceiver, new IntentFilter(BluetoothAdapter.ACTION_DISCOVERY_FINISHED));

        mBtAdapter = BluetoothAdapter.getDefaultAdapter();

        Set<BluetoothDevice> pairedDevices = mBtAdapter.getBondedDevices();

        boolean empty = true;

        if (pairedDevices.size() > 0) for (BluetoothDevice device : pairedDevices)
            if ((device.getBluetoothClass() != null) && (device.getBluetoothClass().getDeviceClass() == BluetoothClass.Device.TOY_ROBOT)) {
                mPairedDevicesArrayAdapter.add(device.getName() + "\n" + device.getAddress());
                empty = false;
            }

        if (!empty) {
            findViewById(R.id.title_paired_devices).setVisibility(View.VISIBLE);
            findViewById(R.id.no_devices).setVisibility(View.GONE);
        }
    }

    @Override
    protected void onDestroy () {
        super.onDestroy();

        if (mBtAdapter != null)
            mBtAdapter.cancelDiscovery();

        this.unregisterReceiver(mReceiver);
    }

    private void doDiscovery () {
        if (mBtAdapter.isDiscovering())
            mBtAdapter.cancelDiscovery();

        mBtAdapter.startDiscovery();

        mNewDevicesArrayAdapter.clear();
        findViewById(R.id.title_new_devices).setVisibility(View.GONE);
        if (mPairedDevicesArrayAdapter.getCount() == 0)
            findViewById(R.id.no_devices).setVisibility(View.VISIBLE);
    }

    private OnItemClickListener mDeviceClickListener = new OnItemClickListener() {
        @Override
        public void onItemClick (AdapterView<?> adapterView, View view, int position, long id) {
            mBtAdapter.cancelDiscovery();

            String info = ((TextView)view).getText().toString();
            String address = info.substring(info.length() - 17);

            Intent intent = new Intent();
            intent.putExtra(EXTRA_DEVICE_ADDRESS, address);

            setResult(Activity.RESULT_OK, intent);
            finish();
        }
    };

    private final BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive (Context context, Intent intent) {
            String action = intent.getAction();

            if (BluetoothDevice.ACTION_FOUND.equals(action)) {
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                assert device != null;
                if ((device.getBondState() != BluetoothDevice.BOND_BONDED) && (device.getBluetoothClass().getDeviceClass() == BluetoothClass.Device.TOY_ROBOT)) {
                    mNewDevicesArrayAdapter.add(device.getName() + "\n" + device.getAddress());
                    findViewById(R.id.title_new_devices).setVisibility(View.VISIBLE);
                    findViewById(R.id.no_devices).setVisibility(View.GONE);
                }
            } else if (BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(action)) {
                scanButton.setEnabled(true);
                scanButton.setText(getString(R.string.scan_idle));
            }
        }
    };

}
