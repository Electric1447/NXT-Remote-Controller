package eparon.nxtremotecontroller;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.net.Uri;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import java.util.Objects;

import eparon.nxtremotecontroller.Util.NetworkUtils;

public class Settings extends AppCompatActivity implements AdapterView.OnItemSelectedListener {

    public String PREFS_NXT = "NXTPrefsFile";
    SharedPreferences prefs;

    public static String sourcecodeURL = "https://github.com/Electric1447/NXT-Remote-Controller";
    public static String releasesURL = "https://github.com/Electric1447/NXT-Remote-Controller/releases";

    Menu mMenu;

    int defConMode = NXTControlActivity.MODE_DPAD_REGULAR;
    Spinner conModeSpinner;

    boolean swapFWDREV = false, swapLeftRight = false, reverse6B = false, regulateSpeed = false, syncMotors = false, gamepad = true, ev3 = false;
    CheckBox fwdrevCB, swapLeftRightCB, reverse6bCB, regSpeedCB, syncMotorsCB, gamepadCB, ev3CB;

    boolean mRestart = false;

    @Override
    public void onBackPressed () {
        Save();
    }

    @Override
    protected void onCreate (Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);

        prefs = getSharedPreferences(PREFS_NXT, Context.MODE_PRIVATE);
        readSharedPreferences();

        if (savedInstanceState != null) {
            if (savedInstanceState.containsKey("set_swapFR")) swapFWDREV = savedInstanceState.getBoolean("set_swapFR");
            if (savedInstanceState.containsKey("set_swapLR")) swapLeftRight = savedInstanceState.getBoolean("set_swapLR");
            if (savedInstanceState.containsKey("set_revr6B")) reverse6B = savedInstanceState.getBoolean("set_revr6B");
            if (savedInstanceState.containsKey("set_rSpeed")) regulateSpeed = savedInstanceState.getBoolean("set_rSpeed");
            if (savedInstanceState.containsKey("set_syncMo")) syncMotors = savedInstanceState.getBoolean("set_syncMo");
            if (savedInstanceState.containsKey("set_gamepd")) gamepad = savedInstanceState.getBoolean("set_gamepd");
            if (savedInstanceState.containsKey("set____ev3")) ev3 = savedInstanceState.getBoolean("set____ev3");
            if (savedInstanceState.containsKey("set_conmod")) defConMode = savedInstanceState.getInt("set_conmod");

            if (savedInstanceState.containsKey("restart")) mRestart = savedInstanceState.getBoolean("restart");
        }

        initializeViews();
    }

    //region UI & onClick functions

    private void initializeViews () {
        conModeSpinner = findViewById(R.id.conmode_spinner);
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this, R.array.conmode_array, R.layout.spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        conModeSpinner.setAdapter(adapter);
        conModeSpinner.setOnItemSelectedListener(this);
        conModeSpinner.setSelection(defConMode - 1);

        fwdrevCB = findViewById(R.id.cbFWDREV);
        fwdrevCB.setChecked(swapFWDREV);
        swapLeftRightCB = findViewById(R.id.cbLeftRight);
        swapLeftRightCB.setChecked(swapLeftRight);
        reverse6bCB = findViewById(R.id.cbReverse6B);
        reverse6bCB.setChecked(reverse6B);
        regSpeedCB = findViewById(R.id.cbRegSpeed);
        regSpeedCB.setChecked(regulateSpeed);
        syncMotorsCB = findViewById(R.id.cbSyncMotors);
        syncMotorsCB.setChecked(syncMotors);
        gamepadCB = findViewById(R.id.cbGamepad);
        gamepadCB.setChecked(gamepad);
        ev3CB = findViewById(R.id.cbEV3);
        ev3CB.setChecked(ev3);

        ((TextView)findViewById(R.id.ver)).setText(String.format("%s v%s\nCreated by Itai Levin (Electric1447).\n\nbased on nxt-remote-control\nby Jacek Fedoryński (jfedor2)", getString(R.string.app_name), BuildConfig.VERSION_NAME)); // Set version TextView.

        if (this.getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
            LinearLayout sideLinearLayout = findViewById(R.id.side_ll);
            sideLinearLayout.measure(0, 0);
            findViewById(R.id.main_ll).setPadding((int)(sideLinearLayout.getMeasuredWidth() * 0.04f), 0, (int)(sideLinearLayout.getMeasuredWidth() * 1.2f), 0);
        }
    }

    public void cbPrefOnClick (View view) {
        switch (view.getId()) {
            case R.id.rlFWDREV:
                swapFWDREV = changeCheckBoxPref(fwdrevCB);
                break;
            case R.id.rlLeftRight:
                swapLeftRight = changeCheckBoxPref(swapLeftRightCB);
                break;
            case R.id.rlReverse6B:
                reverse6B = changeCheckBoxPref(reverse6bCB);
                break;
            case R.id.rlRegSpeed:
                regulateSpeed = changeCheckBoxPref(regSpeedCB);
                break;
            case R.id.rlSyncMotors:
                syncMotors = changeCheckBoxPref(syncMotorsCB);
                break;
            case R.id.rlGamepad:
                gamepad = changeCheckBoxPref(gamepadCB);
                break;
            case R.id.rlEV3:
                ev3 = changeCheckBoxPref(ev3CB);
                mRestart = true;
                Toast.makeText(this, R.string.alert_restart, Toast.LENGTH_LONG).show();
                break;
        }
    }

    private boolean changeCheckBoxPref (CheckBox cb) {
        cb.setChecked(!cb.isChecked());
        return cb.isChecked();
    }

    @Override
    public void onItemSelected (AdapterView<?> parent, View view, int position, long id) {
        defConMode = position + 1;
    }

    @Override
    public void onNothingSelected (AdapterView<?> parent) {
    }

    //endregion

    //region Save/Load

    private void Save () {
        SharedPreferences.Editor editor = prefs.edit();
        editor.putBoolean("swapFWDREV", swapFWDREV);
        editor.putBoolean("swapLeftRight", swapLeftRight);
        editor.putBoolean("reverse6B", reverse6B);
        editor.putBoolean("regulateSpeed", regulateSpeed);
        editor.putBoolean("syncMotors", syncMotors);
        editor.putBoolean("gamepad", gamepad);
        editor.putBoolean("mode", ev3);
        editor.putInt("defconmode", defConMode);
        editor.apply();
        if (!mRestart) {
            startActivity(new Intent(Settings.this, ev3 ? EV3ControlActivity.class : NXTControlActivity.class));
        } else {
            Intent i = getBaseContext().getPackageManager().getLaunchIntentForPackage(getBaseContext().getPackageName());
            assert i != null;
            i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(i);
            finish();
        }
    }

    public void goBack (View view) {
        Save();
    }

    private void readSharedPreferences () {
        swapFWDREV = prefs.getBoolean("swapFWDREV", swapFWDREV);
        swapLeftRight = prefs.getBoolean("swapLeftRight", swapLeftRight);
        reverse6B = prefs.getBoolean("reverse6B", reverse6B);
        regulateSpeed = prefs.getBoolean("regulateSpeed", regulateSpeed);
        syncMotors = prefs.getBoolean("syncMotors", syncMotors);
        gamepad = prefs.getBoolean("gamepad", gamepad);
        ev3 = prefs.getBoolean("mode", ev3);
        defConMode = prefs.getInt("defconmode", defConMode);
    }

    @Override
    protected void onSaveInstanceState (@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putBoolean("set_swapFR", swapFWDREV);
        outState.putBoolean("set_swapLR", swapLeftRight);
        outState.putBoolean("set_revr6B", reverse6B);
        outState.putBoolean("set_rSpeed", regulateSpeed);
        outState.putBoolean("set_syncMo", syncMotors);
        outState.putBoolean("set_gamepd", gamepad);
        outState.putBoolean("set____ev3", ev3);
        outState.putInt("set_conmod", defConMode);

        outState.putBoolean("restart", mRestart);
    }

    //endregion

    //region Options Menu

    @Override
    public boolean onCreateOptionsMenu (Menu menu) {
        getMenuInflater().inflate(R.menu.menu_settings, menu);
        mMenu = menu;
        return true;
    }

    @Override
    public boolean onOptionsItemSelected (@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                Save();
                break;
            case R.id.menu_ab_sourcecode:
            case R.id.menu_item_sourcecode:
                startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(sourcecodeURL)));
                break;
            case R.id.menu_ab_changelog:
            case R.id.menu_item_changelog:
                if (NetworkUtils.isNetworkAvailable(getApplicationContext()))
                    startActivity(new Intent(Settings.this, Changelog.class));
                else
                    Toast.makeText(this, R.string.error_internet_connection, Toast.LENGTH_SHORT).show();
                break;
            case R.id.menu_ab_release:
            case R.id.menu_item_release:
                startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(releasesURL)));
            default:
                return false;
        }
        return true;
    }

    //endregion

}
