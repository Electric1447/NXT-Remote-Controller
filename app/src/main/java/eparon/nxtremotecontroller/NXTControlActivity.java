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
import android.view.InputDevice;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import java.util.Objects;

import eparon.nxtremotecontroller.NXT.Motor;
import eparon.nxtremotecontroller.NXT.NXTTalker;
import eparon.nxtremotecontroller.Util.GamepadUtils;
import eparon.nxtremotecontroller.Util.StateUtils;
import eparon.nxtremotecontroller.View.Tank3MotorView;
import eparon.nxtremotecontroller.View.TankView;
import eparon.nxtremotecontroller.View.TouchPadView;

import static eparon.nxtremotecontroller.Util.GamepadUtils.JOYSTICK_DEADZONE;
import static eparon.nxtremotecontroller.Util.GamepadUtils.TRIGGER_DEADZONE;

@SuppressLint("ClickableViewAccessibility")
public class NXTControlActivity extends AppCompatActivity {

    public static String PREFS_NXT = "NXTPrefsFile";
    SharedPreferences prefs;

    static final int REQUEST_ENABLE_BT = 1, REQUEST_CONNECT_DEVICE = 2;
    static final int MODE_DPAD_REGULAR = 1, MODE_DPAD_RACECAR = 2, MODE_DPAD_6BUTTON = 3, MODE_TOUCHPAD = 4, MODE_TANK = 5, MODE_TANK_3MOTOR = 6;
    static final int DPAD_MODE_REGULAR = 1, DPAD_MODE_STEERING = 2;

    static boolean isDpadModes (int controlsMode) {
        return controlsMode == MODE_DPAD_REGULAR || controlsMode == MODE_DPAD_RACECAR || controlsMode == MODE_DPAD_6BUTTON;
    }

    BluetoothAdapter mBluetoothAdapter;
    NXTTalker mNXTTalker;
    InputDevice mInputDevice;

    int mState = NXTTalker.STATE_NONE, mSavedState = NXTTalker.STATE_NONE;
    private boolean NO_BT = false, mNewLaunch = true;
    String mDeviceAddress = null;

    TextView mStateText;
    Switch mDpadModeSwitch;
    Button mConnectionButton;
    Menu mMenu;

    int mPower = 80, mPowerSecondary = 60;
    int mControlsMode = MODE_DPAD_REGULAR, mDpadControlsMode = DPAD_MODE_REGULAR;

    boolean mReverse = false, mReverseLR = false, mReverse6B = false, mRegulateSpeed = false, mSynchronizeMotors = false, mGamepad = true;

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

            if (savedInstanceState.containsKey("power")) mPower = savedInstanceState.getInt("power");
            if (savedInstanceState.containsKey("power_secondary")) mPowerSecondary = savedInstanceState.getInt("power_secondary");
            if (savedInstanceState.containsKey("controls_mode")) mControlsMode = savedInstanceState.getInt("controls_mode");
            if (savedInstanceState.containsKey("button_controls_mode")) mDpadControlsMode = savedInstanceState.getInt("button_controls_mode");
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

        mNXTTalker = new NXTTalker(mHandler);
        initializeUI();
    }

    //region Activity functions & methods

    private void initializeUI () {
        ActionBar actionBar = getSupportActionBar();
        assert actionBar != null;
        if (this.getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE)
            actionBar.setBackgroundDrawable(new ColorDrawable(isDpadModes(mControlsMode) ? Color.TRANSPARENT : Color.BLACK));
        else
            actionBar.setBackgroundDrawable(new ColorDrawable(getResources().getColor(R.color.colorPrimaryDark)));

        if (mControlsMode == MODE_DPAD_REGULAR) {
            // ---------------------------------- Dpad (Regular) Mode ---------------------------------- //
            setContentView(R.layout.activity_main);

            LinearLayout controlsContainer = findViewById(R.id.controls_container);
            int layoutResID = ((mDpadControlsMode == DPAD_MODE_REGULAR) ? R.layout.dpad_controls_regular : R.layout.dpad_controls_steering);
            LinearLayout inflatedControls = (LinearLayout)View.inflate(this, layoutResID, null);
            controlsContainer.addView(inflatedControls);

            findViewById(R.id.button_up).setOnTouchListener((v, event) -> DirectionButtonOnTouchListener(v, event, 1, 1));
            findViewById(R.id.button_down).setOnTouchListener((v, event) -> DirectionButtonOnTouchListener(v, event, -1, -1));
            findViewById(R.id.button_left).setOnTouchListener((v, event) -> DirectionButtonOnTouchListener(v, event, -0.6, 0.6));
            findViewById(R.id.button_right).setOnTouchListener((v, event) -> DirectionButtonOnTouchListener(v, event, 0.6, -0.6));

            SeekBar powerSeekBar = findViewById(R.id.power_seekbar);
            powerSeekBar.setProgress(mPower);
            powerSeekBar.setOnSeekBarChangeListener(new PowerSliderChangeListener(true));
        } else if (mControlsMode == MODE_DPAD_RACECAR) {
            // ---------------------------------- Dpad (Racecar) Mode ---------------------------------- //
            setContentView(R.layout.activity_main);

            LinearLayout controlsContainer = findViewById(R.id.controls_container);
            int layoutResID = ((mDpadControlsMode == DPAD_MODE_REGULAR) ? R.layout.dpad_controls_regular : R.layout.dpad_controls_steering);
            LinearLayout inflatedControls = (LinearLayout)View.inflate(this, layoutResID, null);
            controlsContainer.addView(inflatedControls);

            findViewById(R.id.button_up).setOnTouchListener((v, event) -> DirectionButtonOnTouchListener(v, event, 1, 1));
            findViewById(R.id.button_down).setOnTouchListener((v, event) -> DirectionButtonOnTouchListener(v, event, -1, -1));
            findViewById(R.id.button_left).setOnTouchListener((v, event) -> DirectionButtonSecondaryOnTouchListener(v, event, 1));
            findViewById(R.id.button_right).setOnTouchListener((v, event) -> DirectionButtonSecondaryOnTouchListener(v, event, -1));

            SeekBar powerSeekBar = findViewById(R.id.power_seekbar);
            powerSeekBar.setProgress(mPower);
            powerSeekBar.setOnSeekBarChangeListener(new PowerSliderChangeListener(true));

            findViewById(R.id.power_secondary_layout).setVisibility(View.VISIBLE);
            ((TextView)findViewById(R.id.power_secondary_text)).setText(getString(R.string.power_turning));

            SeekBar turningPowerSeekBar = findViewById(R.id.power_secondary_seekbar);
            turningPowerSeekBar.setProgress(mPowerSecondary);
            turningPowerSeekBar.setOnSeekBarChangeListener(new PowerSliderChangeListener(false));
        } else if (mControlsMode == MODE_DPAD_6BUTTON) {
            // --------------------------------- Dpad (6 Buttons) Mode --------------------------------- //
            setContentView(R.layout.activity_main);

            LinearLayout controlsContainer = findViewById(R.id.controls_container);
            int layoutResID = ((mDpadControlsMode == DPAD_MODE_REGULAR) ? R.layout.dpad_controls_6button : R.layout.dpad_controls_6button_steering);
            LinearLayout inflatedControls = (LinearLayout)View.inflate(this, layoutResID, null);
            controlsContainer.addView(inflatedControls);

            findViewById(R.id.button_up).setOnTouchListener((v, event) -> DirectionButtonOnTouchListener(v, event, 1, 1));
            findViewById(R.id.button_down).setOnTouchListener((v, event) -> DirectionButtonOnTouchListener(v, event, -1, -1));
            findViewById(R.id.button_left).setOnTouchListener((v, event) -> DirectionButtonOnTouchListener(v, event, -0.6, 0.6));
            findViewById(R.id.button_right).setOnTouchListener((v, event) -> DirectionButtonOnTouchListener(v, event, 0.6, -0.6));
            findViewById(R.id.button_pos).setOnTouchListener((v, event) -> DirectionButtonSecondaryOnTouchListener(v, event, 1));
            findViewById(R.id.button_neg).setOnTouchListener((v, event) -> DirectionButtonSecondaryOnTouchListener(v, event, -1));

            SeekBar powerSeekBar = findViewById(R.id.power_seekbar);
            powerSeekBar.setProgress(mPower);
            powerSeekBar.setOnSeekBarChangeListener(new PowerSliderChangeListener(true));

            findViewById(R.id.power_secondary_layout).setVisibility(View.VISIBLE);
            ((TextView)findViewById(R.id.power_secondary_text)).setText(getString(R.string.power_action));

            SeekBar turningPowerSeekBar = findViewById(R.id.power_secondary_seekbar);
            turningPowerSeekBar.setProgress(mPowerSecondary);
            turningPowerSeekBar.setOnSeekBarChangeListener(new PowerSliderChangeListener(false));
        } else if (mControlsMode == MODE_TOUCHPAD) {
            // ------------------------------------- Touchpad Mode ------------------------------------- //
            setContentView(R.layout.activity_main_touchpad);
            TouchPadView mTouchPadView = findViewById(R.id.touchpad);
            mTouchPadView.setOnTouchListener(this::TouchpadOnTouchListener);
        } else if (mControlsMode == MODE_TANK) {
            // --------------------------------------- Tank Mode --------------------------------------- //
            setContentView(R.layout.activity_main_tank);
            TankView mTankView = findViewById(R.id.tank);
            mTankView.setOnTouchListener(this::TankOnTouchListener);
        } else if (mControlsMode == MODE_TANK_3MOTOR) {
            // ------------------------------------ Tank3Motor Mode ------------------------------------ //
            setContentView(R.layout.activity_main_tank3motor);
            Tank3MotorView mTank3MotorView = findViewById(R.id.tank3motor);
            mTank3MotorView.setOnTouchListener(this::Tank3MotorOnTouchListener);
        }

        mStateText = findViewById(R.id.state_text);

        mDpadModeSwitch = findViewById(R.id.steering_toggle);
        if (isDpadModes(mControlsMode))
            mDpadModeSwitch.setChecked(mDpadControlsMode == DPAD_MODE_STEERING);

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

        updateMenu();
        displayState();
    }

    public void changeSteeringMode (View view) {
        if (mState == NXTTalker.STATE_CONNECTING) {
            Toast.makeText(this, getString(R.string.error_please_wait), Toast.LENGTH_SHORT).show();
            return;
        }

        if (mDpadModeSwitch == null || !isDpadModes(mControlsMode)) {
            Toast.makeText(this, getString(R.string.error_generic), Toast.LENGTH_LONG).show();
            return;
        }

        mDpadModeSwitch.setChecked(!mDpadModeSwitch.isChecked());
        mDpadControlsMode = mDpadModeSwitch.isChecked() ? DPAD_MODE_STEERING : DPAD_MODE_REGULAR;
        initializeUI();
    }

    private void displayState () {
        mStateText.setText(StateUtils.getStateText(mState, getApplicationContext()));
        mStateText.setTextColor(StateUtils.getStateTextColor(mState));
        mConnectionButton.setEnabled(StateUtils.ConnectionButtonState(mState));
        mConnectionButton.setText(StateUtils.getConnectionButtonText(mState, getApplicationContext()));
    }

    private void updateMenu () {
        if (mMenu != null) {
            mMenu.findItem(R.id.menu_item_dpad).setEnabled(mControlsMode != MODE_DPAD_REGULAR).setVisible(mControlsMode != MODE_DPAD_REGULAR);
            mMenu.findItem(R.id.menu_item_dpad_racecar).setEnabled(mControlsMode != MODE_DPAD_RACECAR).setVisible(mControlsMode != MODE_DPAD_RACECAR);
            mMenu.findItem(R.id.menu_item_dpad_6button).setEnabled(mControlsMode != MODE_DPAD_6BUTTON).setVisible(mControlsMode != MODE_DPAD_6BUTTON);
            mMenu.findItem(R.id.menu_item_touchpad).setEnabled(mControlsMode != MODE_TOUCHPAD).setVisible(mControlsMode != MODE_TOUCHPAD);
            mMenu.findItem(R.id.menu_item_tank).setEnabled(mControlsMode != MODE_TANK).setVisible(mControlsMode != MODE_TANK);
            mMenu.findItem(R.id.menu_item_tank_3motor).setEnabled(mControlsMode != MODE_TANK_3MOTOR).setVisible(mControlsMode != MODE_TANK_3MOTOR);
        }
    }

    private void readSharedPreferences (SharedPreferences prefs) {
        mReverse = prefs.getBoolean("swapFWDREV", mReverse);
        mReverseLR = prefs.getBoolean("swapLeftRight", mReverseLR);
        mReverse6B = prefs.getBoolean("reverse6B", mReverse6B);
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
                    closeApp();
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
        // Back button code
        if (event.getKeyCode() == KeyEvent.KEYCODE_BACK)
            closeApp();

        if (!mGamepad)
            return false;

        int action = event.getAction();
        boolean handled = false;

        if ((event.getRepeatCount() <= 10) || (event.getRepeatCount() % 10 == 0))
            switch (event.getKeyCode()) {
                case KeyEvent.KEYCODE_BUTTON_A:
                case KeyEvent.KEYCODE_DPAD_UP:
                    dpadMovement(action, 1, 1, findViewById(R.id.button_up));
                    handled = true;
                    break;
                case KeyEvent.KEYCODE_BUTTON_B:
                case KeyEvent.KEYCODE_DPAD_DOWN:
                    dpadMovement(action, -1, -1, findViewById(R.id.button_down));
                    handled = true;
                    break;
                case KeyEvent.KEYCODE_BUTTON_L1:
                case KeyEvent.KEYCODE_DPAD_LEFT:
                    if (mControlsMode == MODE_DPAD_RACECAR)
                        dpadSecondaryMovement(action, 1, findViewById(R.id.button_left));
                    else
                        dpadMovement(action, -0.6, 0.6, findViewById(R.id.button_left));
                    handled = true;
                    break;
                case KeyEvent.KEYCODE_BUTTON_R1:
                case KeyEvent.KEYCODE_DPAD_RIGHT:
                    if (mControlsMode == MODE_DPAD_RACECAR)
                        dpadSecondaryMovement(action, -1, findViewById(R.id.button_right));
                    else
                        dpadMovement(action, 0.6, -0.6, findViewById(R.id.button_right));
                    handled = true;
                    break;
                case KeyEvent.KEYCODE_BUTTON_Y:
                    if (mControlsMode == MODE_DPAD_6BUTTON) {
                        dpadSecondaryMovement(action, 1, findViewById(R.id.button_pos));
                        handled = true;
                    }
                    break;
                case KeyEvent.KEYCODE_BUTTON_X:
                    if (mControlsMode == MODE_DPAD_6BUTTON) {
                        dpadSecondaryMovement(action, -1, findViewById(R.id.button_neg));
                        handled = true;
                    }
                    break;
            }

        return handled;
    }

    @Override
    public boolean dispatchGenericMotionEvent (MotionEvent event) {
        mInputDevice = event.getDevice();
        int action = event.getAction();

        if ((event.getSource() & InputDevice.SOURCE_JOYSTICK) == InputDevice.SOURCE_JOYSTICK && action == MotionEvent.ACTION_MOVE) {
            processJoystickInput(event);
            return true;
        }

        return super.dispatchGenericMotionEvent(event);
    }

    private void processJoystickInput (MotionEvent event) {
        InputDevice inputDevice = event.getDevice();

        float joy_y = GamepadUtils.getCenteredAxis(event, inputDevice, MotionEvent.AXIS_Y) * -1f;
        float joy_x = GamepadUtils.getCenteredAxis(event, inputDevice, MotionEvent.AXIS_X) * -1f;
        float hat_y = GamepadUtils.getCenteredAxis(event, inputDevice, MotionEvent.AXIS_HAT_Y) * -1f;
        float hat_x = GamepadUtils.getCenteredAxis(event, inputDevice, MotionEvent.AXIS_HAT_X) * -1f;

        float triggers = GamepadUtils.getCenteredAxis(event, inputDevice, MotionEvent.AXIS_RTRIGGER) - GamepadUtils.getCenteredAxis(event, inputDevice, MotionEvent.AXIS_LTRIGGER);

        if (isDpadModes(mControlsMode)) {
            findViewById(R.id.button_up).setBackgroundTintList(ColorStateList.valueOf((((joy_y >= JOYSTICK_DEADZONE) || (hat_y == 1.0f) || (triggers >= TRIGGER_DEADZONE)) ? getResources().getColor(R.color.dpad_button_pressed) : getResources().getColor(R.color.dpad_button_idle_primary))));
            findViewById(R.id.button_down).setBackgroundTintList(ColorStateList.valueOf((((joy_y <= -JOYSTICK_DEADZONE) || (hat_y == -1.0f) || (triggers <= -TRIGGER_DEADZONE)) ? getResources().getColor(R.color.dpad_button_pressed) : getResources().getColor(R.color.dpad_button_idle_primary))));
            findViewById(R.id.button_left).setBackgroundTintList(ColorStateList.valueOf((((joy_x >= JOYSTICK_DEADZONE) || (hat_x == 1.0f)) ? getResources().getColor(R.color.dpad_button_pressed) : getResources().getColor(R.color.dpad_button_idle_primary))));
            findViewById(R.id.button_right).setBackgroundTintList(ColorStateList.valueOf((((joy_x <= -JOYSTICK_DEADZONE) || (hat_x == -1.0f)) ? getResources().getColor(R.color.dpad_button_pressed) : getResources().getColor(R.color.dpad_button_idle_primary))));
        }

        // ------------------------ JOYSTICK (JOY) ------------------------ //
        if (Math.abs(joy_y) >= JOYSTICK_DEADZONE) {
            float power = joy_y * 100f;
            if (mReverse)
                power *= -1;

            mNXTTalker.Motors(Motor.NXT_C, Motor.NXT_B, (byte)power, (byte)power, mRegulateSpeed, mSynchronizeMotors);
        }

        if (Math.abs(joy_x) >= JOYSTICK_DEADZONE) {
            if (mControlsMode != MODE_DPAD_RACECAR) {
                float power = joy_x * -60f;
                if (mReverseLR)
                    power *= -1;

                mNXTTalker.Motors(Motor.NXT_C, Motor.NXT_B, (byte)(-1 * power), (byte)(power), mRegulateSpeed, mSynchronizeMotors);
            } else {
                float power = joy_x * 60f;
                if (mReverseLR)
                    power *= -1;

                mNXTTalker.Motor(Motor.NXT_A, (byte)power, mRegulateSpeed, mSynchronizeMotors);
            }
        }
        // ---------------------------------------------------------------- //

        // -------------------------- DPAD (HAT) -------------------------- //
        if (Math.abs(hat_y) == 1.0f) {
            float power = hat_y * mPower;
            if (mReverse)
                power *= -1;

            mNXTTalker.Motors(Motor.NXT_C, Motor.NXT_B, (byte)power, (byte)power, mRegulateSpeed, mSynchronizeMotors);
        }

        if (Math.abs(hat_x) == 1.0f) {
            if (mControlsMode != MODE_DPAD_RACECAR) {
                float power = hat_x * -0.6f * mPower;
                if (mReverseLR)
                    power *= -1;

                mNXTTalker.Motors(Motor.NXT_C, Motor.NXT_B, (byte)(-1 * power), (byte)(power), mRegulateSpeed, mSynchronizeMotors);
            } else {
                float power = hat_x * mPowerSecondary;
                if (mReverseLR)
                    power *= -1;

                mNXTTalker.Motor(Motor.NXT_A, (byte)power, mRegulateSpeed, mSynchronizeMotors);
            }
        }
        // ---------------------------------------------------------------- //

        // --------------------------- TRIGGERS --------------------------- //
        if (Math.abs(triggers) >= TRIGGER_DEADZONE) {
            float power = triggers * 100f;
            if (mReverse)
                power *= -1;

            mNXTTalker.Motors(Motor.NXT_C, Motor.NXT_B, (byte)power, (byte)power, mRegulateSpeed, mSynchronizeMotors);
        }
        // ---------------------------------------------------------------- //

        if ((Math.abs(joy_y) < JOYSTICK_DEADZONE) && (hat_y == 0f) && (Math.abs(triggers) < TRIGGER_DEADZONE)) {
            mNXTTalker.StopMotors(Motor.NXT_B, Motor.NXT_C, mRegulateSpeed, mSynchronizeMotors);
        }

        if ((Math.abs(joy_x) < JOYSTICK_DEADZONE) && (hat_x == 0f)) {
            if (mControlsMode != MODE_DPAD_RACECAR)
                mNXTTalker.StopMotors(Motor.NXT_B, Motor.NXT_C, mRegulateSpeed, mSynchronizeMotors);
            else
                mNXTTalker.StopMotor(Motor.NXT_A, mRegulateSpeed, mSynchronizeMotors);
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
        outState.putInt("power_secondary", mPowerSecondary);
        outState.putInt("controls_mode", mControlsMode);
        outState.putInt("button_controls_mode", mDpadControlsMode);
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
        getMenuInflater().inflate(R.menu.menu_main, menu);
        mMenu = menu;
        updateMenu();
        return true;
    }

    @Override
    public boolean onOptionsItemSelected (@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_item_dpad:
                mControlsMode = MODE_DPAD_REGULAR;
                initializeUI();
                break;
            case R.id.menu_item_dpad_racecar:
                mControlsMode = MODE_DPAD_RACECAR;
                initializeUI();
                break;
            case R.id.menu_item_dpad_6button:
                mControlsMode = MODE_DPAD_6BUTTON;
                initializeUI();
                break;
            case R.id.menu_item_touchpad:
                mControlsMode = MODE_TOUCHPAD;
                initializeUI();
                break;
            case R.id.menu_item_tank:
                mControlsMode = MODE_TANK;
                initializeUI();
                break;
            case R.id.menu_item_tank_3motor:
                mControlsMode = MODE_TANK_3MOTOR;
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

    //region DpadMovement

    private void dpadMovement (int action, double leftModifier, double rightModifier, View view) {
        if (action == MotionEvent.ACTION_DOWN) {
            // Highlight Dpad Keys
            if (isDpadModes(mControlsMode)) view.setBackgroundTintList(ColorStateList.valueOf(getResources().getColor(R.color.dpad_button_pressed)));

            byte power = (byte)mPower;
            if (mReverse)
                power *= -1;

            byte l = (byte)(power * leftModifier);
            byte r = (byte)(power * rightModifier);

            if (!mReverseLR)
                mNXTTalker.Motors(Motor.NXT_C, Motor.NXT_B, l, r, mRegulateSpeed, mSynchronizeMotors);
            else
                mNXTTalker.Motors(Motor.NXT_B, Motor.NXT_C, l, r, mRegulateSpeed, mSynchronizeMotors);

        } else if ((action == MotionEvent.ACTION_UP) || (action == MotionEvent.ACTION_CANCEL)) {
            mNXTTalker.StopMotors(Motor.NXT_B, Motor.NXT_C, mRegulateSpeed, mSynchronizeMotors);
            // Remove highlight for Dpad Keys
            if (isDpadModes(mControlsMode)) view.setBackgroundTintList(ColorStateList.valueOf(getResources().getColor(R.color.dpad_button_idle_primary)));
        }
    }

    private void dpadSecondaryMovement (int action, double actionModifier, View view) {
        if (action == MotionEvent.ACTION_DOWN) {
            view.setBackgroundTintList(ColorStateList.valueOf(getResources().getColor(R.color.dpad_button_pressed)));

            byte power = (byte)mPowerSecondary;
            if (((mControlsMode != MODE_DPAD_6BUTTON) && mReverseLR) || ((mControlsMode == MODE_DPAD_6BUTTON) && mReverse6B))
                power *= -1;

            byte a = (byte)(power * actionModifier);

            mNXTTalker.Motor(Motor.NXT_A, a, mRegulateSpeed, mSynchronizeMotors);

        } else if ((action == MotionEvent.ACTION_UP) || (action == MotionEvent.ACTION_CANCEL)) {
            mNXTTalker.StopMotor(Motor.NXT_A, mRegulateSpeed, mSynchronizeMotors);
            view.setBackgroundTintList(ColorStateList.valueOf(getResources().getColor((mControlsMode == MODE_DPAD_6BUTTON) ? R.color.dpad_button_idle_secondary : R.color.dpad_button_idle_primary)));
        }
    }

    //endregion

    //region Listeners

    private boolean DirectionButtonOnTouchListener (View v, MotionEvent event, double leftModifier, double rightModifier) {
        dpadMovement(event.getAction(), leftModifier, rightModifier, v);
        return true;
    }

    private boolean DirectionButtonSecondaryOnTouchListener (View v, MotionEvent event, double actionModifier) {
        dpadSecondaryMovement(event.getAction(), actionModifier, v);
        return true;
    }

    private boolean TouchpadOnTouchListener (View v, MotionEvent event) {
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
            if (power > 1.0f)
                power = 1.0f;

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
                mNXTTalker.Motors(Motor.NXT_C, Motor.NXT_B, (byte)(100 * l), (byte)(100 * r), mRegulateSpeed, mSynchronizeMotors);
            else
                mNXTTalker.Motors(Motor.NXT_B, Motor.NXT_C, (byte)(100 * l), (byte)(100 * r), mRegulateSpeed, mSynchronizeMotors);

        } else if ((action == MotionEvent.ACTION_UP) || (action == MotionEvent.ACTION_CANCEL)) {
            mNXTTalker.StopMotors(Motor.NXT_B, Motor.NXT_C, mRegulateSpeed, mSynchronizeMotors);
        }

        return true;
    }

    private boolean TankOnTouchListener (View v, MotionEvent event) {
        TankView tv = (TankView)v;
        float x, y;
        int action = event.getAction();

        if ((action == MotionEvent.ACTION_DOWN) || (action == MotionEvent.ACTION_MOVE)) {
            int[] positionsIndex = new int[] {-1, -1};
            byte l = 0, r = 0;

            for (int i = 0; i < event.getPointerCount(); i++) {
                x = event.getX(i);
                y = -1.0f * (event.getY(i) - tv.mZero) / tv.mRange;
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
                if (positionsIndex[cHeld] > 8)
                    positionsIndex[cHeld] = 8;
            }

            if (mReverse) {
                l *= -1;
                r *= -1;
            }

            if (!mReverseLR)
                mNXTTalker.Motors(Motor.NXT_C, Motor.NXT_B, l, r, mRegulateSpeed, mSynchronizeMotors);
            else
                mNXTTalker.Motors(Motor.NXT_B, Motor.NXT_C, l, r, mRegulateSpeed, mSynchronizeMotors);

            tv.drawTouchAction(positionsIndex);

        } else if ((action == MotionEvent.ACTION_UP) || (action == MotionEvent.ACTION_CANCEL)) {
            mNXTTalker.StopMotors(Motor.NXT_B, Motor.NXT_C, mRegulateSpeed, mSynchronizeMotors);
            tv.resetTouchActions();
        }

        return true;
    }

    private boolean Tank3MotorOnTouchListener (View v, MotionEvent event) {
        Tank3MotorView t3v = (Tank3MotorView)v;
        float x, y;
        int action = event.getAction();

        if ((action == MotionEvent.ACTION_DOWN) || (action == MotionEvent.ACTION_MOVE)) {
            int[] positionsIndex = new int[] {-1, -1, -1};
            byte l = 0, r = 0, a = 0;

            for (int i = 0; i < event.getPointerCount(); i++) {
                x = event.getX(i);
                y = -1.0f * (event.getY(i) - t3v.mZero) / t3v.mRange;
                int cHeld;

                if (y > 1.0f)
                    y = 1.0f;
                if (y < -1.0f)
                    y = -1.0f;

                if (x < t3v.mWidth / 3f) {
                    l = (byte)(y * 100);
                    cHeld = 0;
                } else if (x > 2 * t3v.mWidth / 3f) {
                    r = (byte)(y * 100);
                    cHeld = 2;
                } else {
                    a = (byte)(y * 100);
                    cHeld = 1;
                }

                positionsIndex[cHeld] = (int)(y * 4 + 5);
                if (positionsIndex[cHeld] < 1)
                    positionsIndex[cHeld] = 1;
                if (positionsIndex[cHeld] > 8)
                    positionsIndex[cHeld] = 8;
            }

            if (mReverse) {
                l *= -1;
                r *= -1;
                a *= -1;
            }

            if (!mReverseLR)
                mNXTTalker.Motors(Motor.NXT_C, Motor.NXT_B, Motor.NXT_A, l, r, a, mRegulateSpeed, mSynchronizeMotors);
            else
                mNXTTalker.Motors(Motor.NXT_B, Motor.NXT_C, Motor.NXT_A, l, r, a, mRegulateSpeed, mSynchronizeMotors);

            t3v.drawTouchAction(positionsIndex);

        } else if ((action == MotionEvent.ACTION_UP) || (action == MotionEvent.ACTION_CANCEL)) {
            mNXTTalker.StopMotors(Motor.NXT_A, Motor.NXT_B, Motor.NXT_C, mRegulateSpeed, mSynchronizeMotors);
            t3v.resetTouchActions();
        }

        return true;
    }

    private class PowerSliderChangeListener implements SeekBar.OnSeekBarChangeListener {

        private boolean powerType;

        public PowerSliderChangeListener (boolean primaryPower) {
            this.powerType = primaryPower;
        }

        @Override
        public void onProgressChanged (SeekBar seekBar, int progress, boolean fromUser) {
            if (this.powerType)
                mPower = progress;
            else
                mPowerSecondary = progress;
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