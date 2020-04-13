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
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import java.util.Objects;

import eparon.nxtremotecontroller.NXT.NXTTalker;
import eparon.nxtremotecontroller.View.Tank3MotorView;
import eparon.nxtremotecontroller.View.TankView;
import eparon.nxtremotecontroller.View.TouchPadView;

@SuppressLint("ClickableViewAccessibility")
public class MainActivity extends AppCompatActivity {

    public String PREFS_NXT = "NXTPrefsFile";
    SharedPreferences prefs;

    static final int REQUEST_ENABLE_BT = 1, REQUEST_CONNECT_DEVICE = 2, REQUEST_SETTINGS = 3;
    static final int MODE_BUTTONS = 1, MODE_BUTTONS4WHEEL = 2, MODE_TOUCHPAD = 3, MODE_TANK = 4, MODE_TANK3MOTOR = 5;
    static final int BUTTONS_MODE_DPAD = 1, BUTTONS_MODE_STEERING = 2;
    static final byte INPUT_FORWARD = 0x18, INPUT_REVERSE = 0x19, INPUT_LEFT = 0x1f, INPUT_RIGHT = 0x20;

    BluetoothAdapter mBluetoothAdapter;
    NXTTalker mNXTTalker;

    int mState = NXTTalker.STATE_NONE, mSavedState = NXTTalker.STATE_NONE;
    private boolean mNewLaunch = true, NO_BT = false;
    String mDeviceAddress = null;

    TextView mStateText;
    Switch mButtonModeSwitch;
    Button mConnectionButton;
    Menu mMenu;

    TankView mTankView;
    TouchPadView mTouchPadView;
    Tank3MotorView mTank3MotorView;

    int mPower = 80, mTurningPower = 60;
    int mControlsMode = MODE_BUTTONS, mButtonControlsMode = BUTTONS_MODE_DPAD;

    boolean mReverse = false, mReverseLR = false, mRegulateSpeed = false, mSynchronizeMotors = false, mGamepad = true;

    @Override
    public void onCreate (Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        prefs = getSharedPreferences(PREFS_NXT, Context.MODE_PRIVATE);
        readSharedPreferences(prefs);

        if (savedInstanceState != null) {
            mNewLaunch = false;
            mDeviceAddress = savedInstanceState.getString("device_address");

            if (mDeviceAddress != null)
                mSavedState = NXTTalker.STATE_CONNECTED;

            if (savedInstanceState.containsKey("power"))
                mPower = savedInstanceState.getInt("power");

            if (savedInstanceState.containsKey("power_turning"))
                mTurningPower = savedInstanceState.getInt("power_turning");

            if (savedInstanceState.containsKey("controls_mode"))
                mControlsMode = savedInstanceState.getInt("controls_mode");

            if (savedInstanceState.containsKey("button_controls_mode"))
                mButtonControlsMode = savedInstanceState.getInt("button_controls_mode");
        }

        if (!NO_BT) {
            mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

            if (mBluetoothAdapter == null) {
                Toast.makeText(this, getString(R.string.error_bt_na), Toast.LENGTH_LONG).show();
                finish();
                return;
            }
        }

        initializeUI();
        mNXTTalker = new NXTTalker(mHandler);
    }

    //region Activity functions & methods

    private void initializeUI () {
        if (mControlsMode == MODE_BUTTONS || mControlsMode == MODE_BUTTONS4WHEEL) {
            setContentView(R.layout.activity_main);

            LinearLayout controlsContainer = findViewById(R.id.controls_container);
            int layoutResID = ((mButtonControlsMode == BUTTONS_MODE_DPAD) ? R.layout.controls_dpad : R.layout.controls_steering);
            LinearLayout inflatedControls = (LinearLayout)View.inflate(this, layoutResID, null);
            controlsContainer.addView(inflatedControls);

            findViewById(R.id.button_up).setOnTouchListener(new DirectionButtonOnTouchListener(1, 1));
            findViewById(R.id.button_down).setOnTouchListener(new DirectionButtonOnTouchListener(-1, -1));

            if (mControlsMode == MODE_BUTTONS) {
                updateMenu(R.id.menuitem_buttons);

                findViewById(R.id.button_left).setOnTouchListener(new DirectionButtonOnTouchListener(-0.6, 0.6));
                findViewById(R.id.button_right).setOnTouchListener(new DirectionButtonOnTouchListener(0.6, -0.6));
            } else if (mControlsMode == MODE_BUTTONS4WHEEL) {
                updateMenu(R.id.menuitem_buttons_4wheel);

                findViewById(R.id.button_left).setOnTouchListener(new DirectionButton4WheelOnTouchListener(1));
                findViewById(R.id.button_right).setOnTouchListener(new DirectionButton4WheelOnTouchListener(-1));

                findViewById(R.id.power_4wheel_layout).setVisibility(View.VISIBLE);

                SeekBar turningPowerSeekBar = findViewById(R.id.power_4wheel_seekbar);
                turningPowerSeekBar.setProgress(mTurningPower);
                turningPowerSeekBar.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {
                    @Override
                    public void onProgressChanged (SeekBar seekBar, int progress, boolean fromUser) {
                        mTurningPower = progress;
                    }

                    @Override
                    public void onStartTrackingTouch (SeekBar seekBar) {
                    }

                    @Override
                    public void onStopTrackingTouch (SeekBar seekBar) {
                    }
                });
            }

            SeekBar powerSeekBar = findViewById(R.id.power_seekbar);
            powerSeekBar.setProgress(mPower);
            powerSeekBar.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {
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
            });
        } else if (mControlsMode == MODE_TOUCHPAD) {
            setContentView(R.layout.activity_main_touchpad);
            updateMenu(R.id.menuitem_touchpad);

            mTouchPadView = findViewById(R.id.touchpad);
            mTouchPadView.setOnTouchListener(new TouchpadOnTouchListener());
        } else if (mControlsMode == MODE_TANK) {
            setContentView(R.layout.activity_main_tank);
            updateMenu(R.id.menuitem_tank);

            mTankView = findViewById(R.id.tank);
            mTankView.setOnTouchListener(new TankOnTouchListener());
        } else if (mControlsMode == MODE_TANK3MOTOR) {
            setContentView(R.layout.activity_main_tank3motor);
            updateMenu(R.id.menuitem_tank3motor);

            mTank3MotorView = findViewById(R.id.tank3motor);
            mTank3MotorView.setOnTouchListener(new Tank3MotorOnTouchListener());
        }

        mStateText = findViewById(R.id.state_text);

        mButtonModeSwitch = findViewById(R.id.steering_toggle);
        if (mControlsMode == MODE_BUTTONS || mControlsMode == MODE_BUTTONS4WHEEL)
            mButtonModeSwitch.setChecked(mButtonControlsMode == BUTTONS_MODE_STEERING);

        mConnectionButton = findViewById(R.id.connection_button);
        mConnectionButton.setOnClickListener(v -> {
            if (mState == NXTTalker.STATE_CONNECTED) {
                mNXTTalker.Stop();
            } else {
                if (!NO_BT)
                    findBrick();
                else
                    mState = NXTTalker.STATE_CONNECTED;
            }
            displayState();
        });

        displayState();
    }

    public void changeSteeringDpad (View view) {
        if (mState == NXTTalker.STATE_CONNECTING) {
            Toast.makeText(this, getString(R.string.error_please_wait), Toast.LENGTH_SHORT).show();
            return;
        }

        if (mButtonModeSwitch == null) {
            Toast.makeText(this, getString(R.string.error_generic), Toast.LENGTH_LONG).show();
            return;
        }

        if (mControlsMode == MODE_BUTTONS || mControlsMode == MODE_BUTTONS4WHEEL) {
            mButtonModeSwitch.setChecked(!mButtonModeSwitch.isChecked());
            mButtonControlsMode = mButtonModeSwitch.isChecked() ? BUTTONS_MODE_STEERING : BUTTONS_MODE_DPAD;
            initializeUI();
        }
    }

    private void displayState () {
        String stateStr = "", btnStr = "";
        int textColor = 0xFFFFFFFF;

        switch (mState) {
            case NXTTalker.STATE_NONE:
                stateStr = getString(R.string.conn_state_not_connected);
                textColor = Color.RED;
                btnStr = getString(R.string.conn_btn_connect);
                mConnectionButton.setEnabled(true);
                break;
            case NXTTalker.STATE_CONNECTING:
                stateStr = getString(R.string.conn_state_connecting);
                textColor = Color.YELLOW;
                btnStr = getString(R.string.conn_state_connecting);
                mConnectionButton.setEnabled(false);
                break;
            case NXTTalker.STATE_CONNECTED:
                stateStr = getString(R.string.conn_state_connected);
                textColor = Color.GREEN;
                btnStr = getString(R.string.conn_btn_disconnect);
                mConnectionButton.setEnabled(true);
                break;
        }

        mStateText.setText(stateStr);
        mStateText.setTextColor(textColor);
        mConnectionButton.setText(btnStr);
    }

    private void updateMenu (int disabled) {
        if (mMenu != null) {
            mMenu.findItem(R.id.menuitem_buttons).setEnabled(disabled != R.id.menuitem_buttons).setVisible(disabled != R.id.menuitem_buttons);
            mMenu.findItem(R.id.menuitem_buttons_4wheel).setEnabled(disabled != R.id.menuitem_buttons_4wheel).setVisible(disabled != R.id.menuitem_buttons_4wheel);
            mMenu.findItem(R.id.menuitem_touchpad).setEnabled(disabled != R.id.menuitem_touchpad).setVisible(disabled != R.id.menuitem_touchpad);
            mMenu.findItem(R.id.menuitem_tank).setEnabled(disabled != R.id.menuitem_tank).setVisible(disabled != R.id.menuitem_tank);
            mMenu.findItem(R.id.menuitem_tank3motor).setEnabled(disabled != R.id.menuitem_tank3motor).setVisible(disabled != R.id.menuitem_tank3motor);
        }
    }

    private void readSharedPreferences (SharedPreferences prefs) {
        mReverse = prefs.getBoolean("swapFWDREV", mReverse);
        mReverseLR = prefs.getBoolean("swapLeftRight", mReverseLR);
        mRegulateSpeed = prefs.getBoolean("regulateSpeed", mRegulateSpeed);
        mSynchronizeMotors = prefs.getBoolean("syncMotors", mSynchronizeMotors);
        mGamepad = prefs.getBoolean("gamepad", mGamepad);
        if (!mRegulateSpeed)
            mSynchronizeMotors = false;
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
                    finish();
                }
                break;
            case REQUEST_CONNECT_DEVICE:
                if (resultCode == Activity.RESULT_OK) {
                    String address = Objects.requireNonNull(data.getExtras()).getString(ChooseDevice.EXTRA_DEVICE_ADDRESS);
                    BluetoothDevice device = mBluetoothAdapter.getRemoteDevice(address);
                    mDeviceAddress = address;
                    mNXTTalker.Connect(device);
                }
                break;
            case REQUEST_SETTINGS:
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

    //region Gamepad Input

    @Override
    public boolean dispatchKeyEvent (KeyEvent event) {
        if (!mGamepad)
            return false;

        boolean handled = false;

        if (event.getRepeatCount() <= 10 || (event.getRepeatCount() % 10 == 0))
            switch (event.getKeyCode()) {
                case KeyEvent.KEYCODE_BUTTON_A:
                case KeyEvent.KEYCODE_BUTTON_R2:
                case KeyEvent.KEYCODE_DPAD_UP:
                    inputHandler(INPUT_FORWARD, event.getAction());
                    handled = true;
                    break;
                case KeyEvent.KEYCODE_BUTTON_B:
                case KeyEvent.KEYCODE_BUTTON_L2:
                case KeyEvent.KEYCODE_DPAD_DOWN:
                    inputHandler(INPUT_REVERSE, event.getAction());
                    handled = true;
                    break;
                case KeyEvent.KEYCODE_BUTTON_L1:
                case KeyEvent.KEYCODE_DPAD_LEFT:
                    inputHandler(INPUT_LEFT, event.getAction());
                    handled = true;
                    break;
                case KeyEvent.KEYCODE_BUTTON_R1:
                case KeyEvent.KEYCODE_DPAD_RIGHT:
                    inputHandler(INPUT_RIGHT, event.getAction());
                    handled = true;
                    break;
            }

        return handled;
    }

    private void inputHandler (byte input, int action) {
        boolean dcm = (mControlsMode == MODE_BUTTONS || mControlsMode == MODE_BUTTONS4WHEEL);

        switch (input) {
            case INPUT_FORWARD:
                if (action == MotionEvent.ACTION_DOWN) {
                    if (dcm) findViewById(R.id.button_up).setBackgroundTintList(ColorStateList.valueOf(getResources().getColor(R.color.dpad_button_background_pressed)));
                    dpadMovement(false, 1, 1);
                } else if ((action == MotionEvent.ACTION_UP) || (action == MotionEvent.ACTION_CANCEL)) {
                    if (dcm) findViewById(R.id.button_up).setBackgroundTintList(ColorStateList.valueOf(getResources().getColor(R.color.dpad_button_background_idle)));
                    dpadMovement(true, 1, 1);
                }
                break;
            case INPUT_REVERSE:
                if (action == MotionEvent.ACTION_DOWN) {
                    if (dcm) findViewById(R.id.button_down).setBackgroundTintList(ColorStateList.valueOf(getResources().getColor(R.color.dpad_button_background_pressed)));
                    dpadMovement(false, -1, -1);
                } else if ((action == MotionEvent.ACTION_UP) || (action == MotionEvent.ACTION_CANCEL)) {
                    if (dcm) findViewById(R.id.button_down).setBackgroundTintList(ColorStateList.valueOf(getResources().getColor(R.color.dpad_button_background_idle)));
                    dpadMovement(true, -1, -1);
                }
                break;
            case INPUT_LEFT:
                if (mControlsMode == MODE_BUTTONS4WHEEL) {
                    if (action == MotionEvent.ACTION_DOWN) {
                        findViewById(R.id.button_left).setBackgroundTintList(ColorStateList.valueOf(getResources().getColor(R.color.dpad_button_background_pressed)));
                        dpad4WheelMovement(false, 1);
                    } else if ((action == MotionEvent.ACTION_UP) || (action == MotionEvent.ACTION_CANCEL)) {
                        findViewById(R.id.button_left).setBackgroundTintList(ColorStateList.valueOf(getResources().getColor(R.color.dpad_button_background_idle)));
                        dpad4WheelMovement(false, 1);
                    }
                } else {
                    if (action == MotionEvent.ACTION_DOWN) {
                        if (dcm) findViewById(R.id.button_left).setBackgroundTintList(ColorStateList.valueOf(getResources().getColor(R.color.dpad_button_background_pressed)));
                        dpadMovement(false, -0.6, 0.6);
                    } else if ((action == MotionEvent.ACTION_UP) || (action == MotionEvent.ACTION_CANCEL)) {
                        if (dcm) findViewById(R.id.button_left).setBackgroundTintList(ColorStateList.valueOf(getResources().getColor(R.color.dpad_button_background_idle)));
                        dpadMovement(true, -0.6, 0.6);
                    }
                }
                break;
            case INPUT_RIGHT:
                if (mControlsMode == MODE_BUTTONS4WHEEL) {
                    if (action == MotionEvent.ACTION_DOWN) {
                        findViewById(R.id.button_right).setBackgroundTintList(ColorStateList.valueOf(getResources().getColor(R.color.dpad_button_background_pressed)));
                        dpad4WheelMovement(false, -1);
                    } else if ((action == MotionEvent.ACTION_UP) || (action == MotionEvent.ACTION_CANCEL)) {
                        findViewById(R.id.button_right).setBackgroundTintList(ColorStateList.valueOf(getResources().getColor(R.color.dpad_button_background_idle)));
                        dpad4WheelMovement(true, -1);
                    }
                } else {
                    if (action == MotionEvent.ACTION_DOWN) {
                        if (dcm) findViewById(R.id.button_right).setBackgroundTintList(ColorStateList.valueOf(getResources().getColor(R.color.dpad_button_background_pressed)));
                        dpadMovement(false, 0.6, -0.6);
                    } else if ((action == MotionEvent.ACTION_UP) || (action == MotionEvent.ACTION_CANCEL)) {
                        if (dcm) findViewById(R.id.button_right).setBackgroundTintList(ColorStateList.valueOf(getResources().getColor(R.color.dpad_button_background_idle)));
                        dpadMovement(true, 0.6, -0.6);
                    }
                }
                break;
            default:
                break;
        }
    }

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
                mNXTTalker.Connect(device);
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
        mNXTTalker.Stop();
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
        outState.putInt("power_turning", mTurningPower);
        outState.putInt("controls_mode", mControlsMode);
        outState.putInt("button_controls_mode", mButtonControlsMode);
    }

    @Override
    public void onConfigurationChanged (@NonNull Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        initializeUI();
    }

    //endregion

    //region onOptions

    @Override
    public boolean onCreateOptionsMenu (Menu menu) {
        getMenuInflater().inflate(R.menu.options_menu, menu);
        mMenu = menu;
        return true;
    }

    @Override
    public boolean onOptionsItemSelected (MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menuitem_buttons:
                mControlsMode = MODE_BUTTONS;
                initializeUI();
                break;
            case R.id.menuitem_buttons_4wheel:
                mControlsMode = MODE_BUTTONS4WHEEL;
                initializeUI();
                break;
            case R.id.menuitem_touchpad:
                mControlsMode = MODE_TOUCHPAD;
                initializeUI();
                break;
            case R.id.menuitem_tank:
                mControlsMode = MODE_TANK;
                initializeUI();
                break;
            case R.id.menuitem_tank3motor:
                mControlsMode = MODE_TANK3MOTOR;
                initializeUI();
                break;
            case R.id.menuitem_settings:
                startActivity(new Intent(this, Settings.class));
                break;
            default:
                return false;
        }
        return true;
    }

    //endregion

    //region DpadMovement

    public void dpadMovement (boolean dispatch, double leftModifier, double rightModifier) {
        if (!dispatch) {
            byte power = (byte)mPower;
            if (mReverse)
                power *= -1;

            byte l = (byte)(power * leftModifier);
            byte r = (byte)(power * rightModifier);

            if (!mReverseLR)
                mNXTTalker.Motors(l, r, mRegulateSpeed, mSynchronizeMotors);
            else
                mNXTTalker.Motors(r, l, mRegulateSpeed, mSynchronizeMotors);

        } else {
            mNXTTalker.Motors((byte)0, (byte)0, mRegulateSpeed, mSynchronizeMotors);
        }
    }

    public void dpad4WheelMovement (boolean dispatch, double turningModifier) {
        if (!dispatch) {
            byte power = (byte)mTurningPower;
            if (mReverseLR)
                power *= -1;

            byte a = (byte)(power * turningModifier);

            mNXTTalker.Motor(0, a, mRegulateSpeed, mSynchronizeMotors);

        } else {
            mNXTTalker.Motor(0, (byte)0, mRegulateSpeed, mSynchronizeMotors);
        }
    }

    //endregion

    //region Listeners

    private class DirectionButtonOnTouchListener implements OnTouchListener {

        private double leftModifier;
        private double rightModifier;

        DirectionButtonOnTouchListener (double l, double r) {
            leftModifier = l;
            rightModifier = r;
        }

        @Override
        public boolean onTouch (View v, MotionEvent event) {
            int action = event.getAction();

            if (action == MotionEvent.ACTION_DOWN) {
                v.setBackgroundTintList(ColorStateList.valueOf(getResources().getColor(R.color.dpad_button_background_pressed)));

                dpadMovement(false, leftModifier, rightModifier);

            } else if ((action == MotionEvent.ACTION_UP) || (action == MotionEvent.ACTION_CANCEL)) {
                v.setBackgroundTintList(ColorStateList.valueOf(getResources().getColor(R.color.dpad_button_background_idle)));
                dpadMovement(true, leftModifier, rightModifier);
            }

            return true;
        }
    }

    private class DirectionButton4WheelOnTouchListener implements OnTouchListener {

        private double turningModifier;

        DirectionButton4WheelOnTouchListener (double a) {
            turningModifier = a;
        }

        @Override
        public boolean onTouch (View v, MotionEvent event) {
            int action = event.getAction();

            if (action == MotionEvent.ACTION_DOWN) {
                v.setBackgroundTintList(ColorStateList.valueOf(getResources().getColor(R.color.dpad_button_background_pressed)));

                dpad4WheelMovement(false, turningModifier);

            } else if ((action == MotionEvent.ACTION_UP) || (action == MotionEvent.ACTION_CANCEL)) {
                v.setBackgroundTintList(ColorStateList.valueOf(getResources().getColor(R.color.dpad_button_background_idle)));
                dpad4WheelMovement(true, turningModifier);
            }

            return true;
        }
    }

    private class TouchpadOnTouchListener implements OnTouchListener {

        @Override
        public boolean onTouch (View v, MotionEvent event) {
            TouchPadView tpv = (TouchPadView)v;
            float x, y, power;
            int action = event.getAction();

            if ((action == MotionEvent.ACTION_DOWN) || (action == MotionEvent.ACTION_MOVE)) {
                x = (event.getX() - tpv.mCx) / tpv.mRadius;
                y = -1.0f * (event.getY() - tpv.mCy);

                if (y > 0f) {
                    y -= tpv.mOffset;
                    if (y < 0f)
                        y = 0.01f;
                } else if (y < 0f) {
                    y += tpv.mOffset;
                    if (y > 0f)
                        y = -0.01f;
                }

                y /= tpv.mRadius;

                float sqrt_p5 = 0.707106781f;
                float nx = x * sqrt_p5 + y * sqrt_p5;
                float ny = -x * sqrt_p5 + y * sqrt_p5;

                power = (float)Math.sqrt(nx * nx + ny * ny);
                if (power > 1.0f) {
                    //nx /= power;
                    //ny /= power;
                    power = 1.0f;
                }

                float angle = (float)Math.atan2(y, x);
                float l, r;

                if (angle > 0f && angle <= Math.PI / 2f) {
                    l = 1.0f;
                    r = (float)(2.0f * angle / Math.PI);
                } else if (angle > Math.PI / 2f && angle <= Math.PI) {
                    l = (float)(2.0f * (Math.PI - angle) / Math.PI);
                    r = 1.0f;
                } else if (angle < 0f && angle >= -Math.PI / 2f) {
                    l = -1.0f;
                    r = (float)(2.0f * angle / Math.PI);
                } else if (angle < -Math.PI / 2f && angle > -Math.PI) {
                    l = (float)(-2.0f * (angle + Math.PI) / Math.PI);
                    r = -1.0f;
                } else {
                    l = r = 0f;
                }

                l *= power;
                r *= power;

                if (mReverse) {
                    l *= -1;
                    r *= -1;
                }

                if (!mReverseLR)
                    mNXTTalker.Motors((byte)(100 * l), (byte)(100 * r), mRegulateSpeed, mSynchronizeMotors);
                else
                    mNXTTalker.Motors((byte)(100 * r), (byte)(100 * l), mRegulateSpeed, mSynchronizeMotors);

            } else if ((action == MotionEvent.ACTION_UP) || (action == MotionEvent.ACTION_CANCEL)) {
                mNXTTalker.Motors((byte)0, (byte)0, mRegulateSpeed, mSynchronizeMotors);
            }

            return true;
        }
    }

    private class TankOnTouchListener implements OnTouchListener {

        @Override
        public boolean onTouch (View v, MotionEvent event) {
            TankView tv = (TankView)v;
            float y, x;
            int action = event.getAction();

            if ((action == MotionEvent.ACTION_DOWN) || (action == MotionEvent.ACTION_MOVE)) {
                int[] positionsIndex = new int[] {-1, -1};
                byte l = 0, r = 0;

                for (int i = 0; i < event.getPointerCount(); i++) {
                    y = -1.0f * (event.getY(i) - tv.mZero) / tv.mRange;
                    x = event.getX(i);
                    int cHeld;

                    if (y > 1.0f)
                        y = 1.0f;
                    if (y < -1.0f)
                        y = -1.0f;

                    if (x < tv.mWidth / 2f) {
                        l = (byte)(y * 100);
                        cHeld = 0;
                    } else {
                        r = (byte)(y * 100);
                        cHeld = 1;
                    }

                    positionsIndex[cHeld] = (int)(y * 4 + 5);
                    if (positionsIndex[cHeld] < 1)
                        positionsIndex[cHeld] = 1;
                    else if (positionsIndex[cHeld] > 8)
                        positionsIndex[cHeld] = 8;
                }

                if (mReverse) {
                    l *= -1;
                    r *= -1;
                }

                if (!mReverseLR)
                    mNXTTalker.Motors(l, r, mRegulateSpeed, mSynchronizeMotors);
                else
                    mNXTTalker.Motors(r, l, mRegulateSpeed, mSynchronizeMotors);

                tv.drawTouchAction(positionsIndex);

            } else if ((action == MotionEvent.ACTION_UP) || (action == MotionEvent.ACTION_CANCEL)) {
                mNXTTalker.Motors((byte)0, (byte)0, mRegulateSpeed, mSynchronizeMotors);
                tv.resetTouchActions();
            }

            return true;
        }
    }

    private class Tank3MotorOnTouchListener implements OnTouchListener {

        @Override
        public boolean onTouch (View v, MotionEvent event) {
            Tank3MotorView t3v = (Tank3MotorView)v;
            float x, y;
            int action = event.getAction();

            if ((action == MotionEvent.ACTION_DOWN) || (action == MotionEvent.ACTION_MOVE)) {
                byte l = 0, r = 0, a = 0;

                for (int i = 0; i < event.getPointerCount(); i++) {
                    y = -1.0f * (event.getY(i) - t3v.mZero) / t3v.mRange;

                    if (y > 1.0f)
                        y = 1.0f;
                    if (y < -1.0f)
                        y = -1.0f;

                    x = event.getX(i);

                    if (x < t3v.mWidth / 3f)
                        l = (byte)(y * 100);
                    else if (x > 2 * t3v.mWidth / 3f)
                        r = (byte)(y * 100);
                    else
                        a = (byte)(y * 100);
                }

                if (mReverse) {
                    l *= -1;
                    r *= -1;
                    a *= -1;
                }

                if (!mReverseLR)
                    mNXTTalker.Motors3(l, r, a, mRegulateSpeed, mSynchronizeMotors);
                else
                    mNXTTalker.Motors3(r, l, a, mRegulateSpeed, mSynchronizeMotors);

            } else if ((action == MotionEvent.ACTION_UP) || (action == MotionEvent.ACTION_CANCEL)) {
                mNXTTalker.Motors3((byte)0, (byte)0, (byte)0, mRegulateSpeed, mSynchronizeMotors);
            }

            return true;
        }
    }

    //endregion

}