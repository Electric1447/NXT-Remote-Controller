package eparon.nxtremotecontroller;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.ColorStateList;
import android.content.res.Configuration;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import java.util.Objects;

import eparon.nxtremotecontroller.NXT.EV3Talker;
import eparon.nxtremotecontroller.NXT.Motor;
import eparon.nxtremotecontroller.NXT.NXTTalker;
import eparon.nxtremotecontroller.Util.StateUtils;
import eparon.nxtremotecontroller.View.Tank4MotorView;

@SuppressLint("ClickableViewAccessibility")
public class EV3ControlActivity extends AppCompatActivity {

    SharedPreferences prefs;

    static final int REQUEST_ENABLE_BT = 1, REQUEST_CONNECT_DEVICE = 2;
    static final int MODE_ABCD = 1, MODE_R3PTAR = 2;

    BluetoothAdapter mBluetoothAdapter;
    EV3Talker mEV3Talker;
    StateUtils stateUtils;

    int mState = NXTTalker.STATE_NONE, mSavedState = NXTTalker.STATE_NONE;
    private boolean NO_BT = false, mNewLaunch = true;
    String mDeviceAddress = null;

    TextView mStateText;
    Button mConnectionButton;
    Menu mMenu;

    int mControlsMode = MODE_ABCD;
    int mPower = 80;

    boolean mReverse = false, mReverseLR = false;

    @Override
    public void onBackPressed () {
        closeApp();
    }

    @Override
    public void onCreate (Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        prefs = getSharedPreferences(NXTControlActivity.PREFS_NXT, Context.MODE_PRIVATE);
        readSharedPreferences(prefs);

        if (savedInstanceState != null) {
            mNewLaunch = false;
            mDeviceAddress = savedInstanceState.getString("device_address");

            if (mDeviceAddress != null)
                mSavedState = NXTTalker.STATE_CONNECTED;

            if (savedInstanceState.containsKey("controls_mode")) mControlsMode = savedInstanceState.getInt("controls_mode");
        }

        if (!NO_BT) {
            mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

            if (mBluetoothAdapter == null) {
                Toast.makeText(this, getString(R.string.error_bt_na), Toast.LENGTH_LONG).show();
                closeApp();
                return;
            }
        }

        if (mNewLaunch)
            mControlsMode = prefs.getInt("defconmode", NXTControlActivity.MODE_DPAD_REGULAR);

        mEV3Talker = new EV3Talker(mHandler);
        stateUtils = new StateUtils(getApplicationContext());
        initializeUI();
    }

    //region Activity functions & methods

    private void initializeUI () {
        ActionBar actionBar = getSupportActionBar();
        assert actionBar != null;
        if (this.getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE)
            actionBar.setBackgroundDrawable(new ColorDrawable((mControlsMode == MODE_ABCD) ? Color.BLACK : Color.TRANSPARENT));
        else
            actionBar.setBackgroundDrawable(new ColorDrawable(getResources().getColor(R.color.colorPrimaryDark)));

        if (mControlsMode == MODE_ABCD) {
            // ------------------------------------- Touchpad Mode ------------------------------------- //
            setContentView(R.layout.activity_ev3_abcd);
            Tank4MotorView mTank4MotorView = findViewById(R.id.tank4motor);
            mTank4MotorView.setOnTouchListener(this::Tank4MotorOnTouchListener);
        } else if (mControlsMode == MODE_R3PTAR) {
            // ---------------------------------- Dpad (Regular) Mode ---------------------------------- //
            setContentView(R.layout.activity_main);

            LinearLayout controlsContainer = findViewById(R.id.controls_container);
            LinearLayout inflatedControls = (LinearLayout)View.inflate(this, R.layout.r3ptar_controls, null);
            controlsContainer.addView(inflatedControls);

            findViewById(R.id.button_up).setOnTouchListener((v, event) -> EV3OnTouchListener(v, event, Motor.EV3_B, (byte)(mPower)));
            findViewById(R.id.button_down).setOnTouchListener((v, event) -> EV3OnTouchListener(v, event, Motor.EV3_B, (byte)(mPower * -1)));
            findViewById(R.id.button_left).setOnTouchListener((v, event) -> EV3OnTouchListener(v, event, Motor.EV3_A, (byte)-60));
            findViewById(R.id.button_right).setOnTouchListener((v, event) -> EV3OnTouchListener(v, event, Motor.EV3_A, (byte)60));

            findViewById(R.id.button_bite).setOnClickListener(this::EV3OnR3ptarBiteListener);

            LinearLayout steeringLL = findViewById(R.id.steering_layout);
            steeringLL.setVisibility(View.GONE);

            SeekBar powerSeekBar = findViewById(R.id.power_seekbar);
            powerSeekBar.setProgress(mPower);
            powerSeekBar.setOnSeekBarChangeListener(new PowerSliderChangeListener());
        }

        mStateText = findViewById(R.id.state_text);

        mConnectionButton = findViewById(R.id.connection_button);
        mConnectionButton.setOnClickListener(v -> {
            if (mState == NXTTalker.STATE_CONNECTED) {
                mEV3Talker.Stop();
            } else {
                if (!NO_BT)
                    findBrick();
                else
                    mState = NXTTalker.STATE_CONNECTED;
            }
            displayState();
        });

        updateMenu();
        displayState();
    }

    private void displayState () {
        mStateText.setText(stateUtils.getStateText(mState));
        mStateText.setTextColor(stateUtils.getStateTextColor(mState));
        mConnectionButton.setEnabled(stateUtils.ConnectionButtonState(mState));
        mConnectionButton.setText(stateUtils.getConnectionButtonText(mState));
    }

    private void updateMenu () {
        if (mMenu != null) {
            mMenu.findItem(R.id.menu_item_tank_4motor).setEnabled(mControlsMode != MODE_ABCD).setVisible(mControlsMode != MODE_ABCD);
            mMenu.findItem(R.id.menu_item_r3ptar).setEnabled(mControlsMode != MODE_R3PTAR).setVisible(mControlsMode != MODE_R3PTAR);
        }
    }

    private void readSharedPreferences (SharedPreferences prefs) {
        mReverse = prefs.getBoolean("swapFWDREV", mReverse);
        mReverseLR = prefs.getBoolean("swapLeftRight", mReverseLR);
    }

    //endregion

    //region Bluetooth

    private void findBrick () {
        startActivityForResult(new Intent(this, ChooseDevice.class), REQUEST_CONNECT_DEVICE);
    }

    @Override
    protected void onActivityResult (int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case REQUEST_ENABLE_BT:
                if (resultCode == Activity.RESULT_OK) {
                    findBrick();
                } else {
                    Toast.makeText(this, getString(R.string.error_bt_not_enabled), Toast.LENGTH_LONG).show();
                    closeApp();
                }
                break;
            case REQUEST_CONNECT_DEVICE:
                if (resultCode == Activity.RESULT_OK) {
                    String address = Objects.requireNonNull(data.getExtras()).getString(ChooseDevice.EXTRA_DEVICE_ADDRESS);
                    BluetoothDevice device = mBluetoothAdapter.getRemoteDevice(address);
                    mDeviceAddress = address;
                    mEV3Talker.Connect(device);
                }
                break;
        }
    }

    //endregion

    //region Handler

    @SuppressLint("HandlerLeak")
    private final Handler mHandler = new Handler() {
        @Override
        public void handleMessage (Message msg) {
            if (msg.what == 2) {
                mState = msg.arg1;
                displayState();
            }
        }
    };

    //endregion

    //region onStart/Stop/Resume

    @Override
    protected void onStart () {
        super.onStart();
        if (!NO_BT)
            if (!mBluetoothAdapter.isEnabled()) {
                startActivityForResult(new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE), REQUEST_ENABLE_BT);
            } else if (mSavedState == NXTTalker.STATE_CONNECTED) {
                BluetoothDevice device = mBluetoothAdapter.getRemoteDevice(mDeviceAddress);
                mEV3Talker.Connect(device);
            } else {
                if (mNewLaunch) {
                    mNewLaunch = false;
                    findBrick();
                }
            }
    }

    @Override
    protected void onStop () {
        super.onStop();
        mSavedState = mState;
        mEV3Talker.Stop();
    }

    @Override
    protected void onResume () {
        super.onResume();
        readSharedPreferences(prefs);
    }

    //endregion

    //region onConfig

    @Override
    protected void onSaveInstanceState (@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        if (mState == NXTTalker.STATE_CONNECTED)
            outState.putString("device_address", mDeviceAddress);
        outState.putInt("power", mPower);
        outState.putInt("controls_mode", mControlsMode);
    }

    @Override
    public void onConfigurationChanged (@NonNull Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        initializeUI();
    }

    //endregion

    //region Options Menu

    @Override
    public boolean onCreateOptionsMenu (Menu menu) {
        getMenuInflater().inflate(R.menu.menu_ev3, menu);
        mMenu = menu;
        updateMenu();
        return true;
    }

    @Override
    public boolean onOptionsItemSelected (@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_item_tank_4motor:
                mControlsMode = MODE_ABCD;
                initializeUI();
                break;
            case R.id.menu_item_r3ptar:
                mControlsMode = MODE_R3PTAR;
                initializeUI();
                break;
            case R.id.menu_ab_settings:
            case R.id.menu_item_settings:
                startActivity(new Intent(this, Settings.class));
                break;
            default:
                return false;
        }
        return true;
    }

    //endregion

    //region Listeners

    private boolean EV3OnTouchListener (View v, MotionEvent event, byte port, byte power) {
        int action = event.getAction();

        if (action == MotionEvent.ACTION_DOWN) {
            if (mControlsMode == MODE_R3PTAR) v.setBackgroundTintList(ColorStateList.valueOf(getResources().getColor(R.color.dpad_button_pressed)));
            mEV3Talker.EV3_Motors(port, power);
        } else if ((action == MotionEvent.ACTION_UP) || (action == MotionEvent.ACTION_CANCEL)) {
            mEV3Talker.EV3_StopMotors(port);
            if (mControlsMode == MODE_R3PTAR) v.setBackgroundTintList(ColorStateList.valueOf(getResources().getColor(R.color.dpad_button_idle_primary)));
        }

        return true;
    }

    private boolean Tank4MotorOnTouchListener (View v, MotionEvent event) {
        Tank4MotorView t4v = (Tank4MotorView)v;
        float x, y;
        int action = event.getAction();

        if ((action == MotionEvent.ACTION_DOWN) || (action == MotionEvent.ACTION_MOVE)) {
            int[] positionsIndex = new int[] {-1, -1, -1, -1};
            byte a = 0, b = 0, c = 0, d = 0;

            for (int i = 0; i < event.getPointerCount(); i++) {
                x = event.getX(i);
                y = -1.0f * (event.getY(i) - t4v.mZero) / t4v.mRange;
                int cHeld;

                if (y > 1.0f)
                    y = 1.0f;
                if (y < -1.0f)
                    y = -1.0f;

                if (x < t4v.mWidth * 0.25f) {
                    a = (byte)(y * 100);
                    cHeld = 0;
                } else if (x > t4v.mWidth * 0.75f) {
                    d = (byte)(y * 100);
                    cHeld = 3;
                } else if ((x >= t4v.mWidth * 0.25f) && (x < t4v.mWidth * 0.5f)) {
                    b = (byte)(y * 100);
                    cHeld = 1;
                } else {
                    c = (byte)(y * 100);
                    cHeld = 2;
                }

                positionsIndex[cHeld] = (int)(y * 4 + 5);
                if (positionsIndex[cHeld] < 1)
                    positionsIndex[cHeld] = 1;
                if (positionsIndex[cHeld] > 8)
                    positionsIndex[cHeld] = 8;
            }

            if (mReverse) {
                a *= -1;
                c *= -1;
                b *= -1;
                d *= -1;
            }

            mEV3Talker.EV3_Motors(Motor.EV3_A, a);
            mEV3Talker.EV3_Motors(Motor.EV3_B, b);
            mEV3Talker.EV3_Motors(Motor.EV3_C, c);
            mEV3Talker.EV3_Motors(Motor.EV3_D, d);

            t4v.drawTouchAction(positionsIndex);

        } else if ((action == MotionEvent.ACTION_UP) || (action == MotionEvent.ACTION_CANCEL)) {
            mEV3Talker.EV3_StopMotors((byte)(Motor.EV3_A + Motor.EV3_B + Motor.EV3_C + Motor.EV3_D));
            t4v.resetTouchActions();
        }

        return true;
    }

    private void EV3OnR3ptarBiteListener (View v) {
        mEV3Talker.EV3_Motors(Motor.EV3_D, (byte)80);
        new Handler().postDelayed(() -> mEV3Talker.EV3_StopMotors(Motor.EV3_D), 600);
        new Handler().postDelayed(() -> mEV3Talker.EV3_Motors(Motor.EV3_D, (byte)-70), 600);
    }

    private class PowerSliderChangeListener implements SeekBar.OnSeekBarChangeListener {

        @Override
        public void onProgressChanged (SeekBar seekBar, int progress, boolean fromUser) {
            mPower = progress;
        }

        @Override
        public void onStartTrackingTouch (SeekBar seekBar) {
        }

        @Override
        public void onStopTrackingTouch (SeekBar seekBar) {
        }

    }

    //endregion

    private void closeApp () {
        this.finishAffinity();
        this.finishAndRemoveTask();
    }

}
