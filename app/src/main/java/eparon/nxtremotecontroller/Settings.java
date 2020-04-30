package eparon.nxtremotecontroller;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.CheckBox;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import java.util.Objects;

public class Settings extends AppCompatActivity {

    public String PREFS_NXT = "NXTPrefsFile";
    SharedPreferences prefs;

    public static String githubURL = "https://github.com/Electric1447/NXT-Remote-Controller";

    boolean swapFWDREV = false, swapLeftRight = false, regulateSpeed = false, syncMotors = false, gamepad = true;
    CheckBox fwdrevCB, swapLeftRightCB, regSpeedCB, syncMotorsCB, gamepadCB;

    @Override
    public void onBackPressed () {
        Save();
    }

    @Override
    public boolean onOptionsItemSelected (@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home)
            Save();
        return true;
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
            if (savedInstanceState.containsKey("set_rSpeed")) regulateSpeed = savedInstanceState.getBoolean("set_rSpeed");
            if (savedInstanceState.containsKey("set_syncMo")) syncMotors = savedInstanceState.getBoolean("set_syncMo");
            if (savedInstanceState.containsKey("set_gamepd")) gamepad = savedInstanceState.getBoolean("set_gamepd");
        }

        initializeViews();
    }

    //region UI & onClick functions

    private void initializeViews () {
        fwdrevCB = findViewById(R.id.cbFWDREV);
        fwdrevCB.setChecked(swapFWDREV);
        swapLeftRightCB = findViewById(R.id.cbLeftRight);
        swapLeftRightCB.setChecked(swapLeftRight);
        regSpeedCB = findViewById(R.id.cbRegSpeed);
        regSpeedCB.setChecked(regulateSpeed);
        syncMotorsCB = findViewById(R.id.cbSyncMotors);
        syncMotorsCB.setChecked(syncMotors);
        gamepadCB = findViewById(R.id.cbGamepad);
        gamepadCB.setChecked(gamepad);

        ((TextView)findViewById(R.id.ver)).setText(String.format("%s v%s\nCreated by Itai Levin (Electric1447).\n\nbased on nxt-remote-control\nby Jacek Fedoryński (jfedor2)\n", getString(R.string.app_name), BuildConfig.VERSION_NAME)); // Set version TextView.
    }

    public void cbPrefOnClick (View view) {
        switch (view.getId()) {
            case R.id.rlFWDREV:
                swapFWDREV = changeCheckBoxPref(fwdrevCB);
                break;
            case R.id.rlLeftRight:
                swapLeftRight = changeCheckBoxPref(swapLeftRightCB);
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
        }
    }

    private boolean changeCheckBoxPref (CheckBox cb) {
        cb.setChecked(!cb.isChecked());
        return cb.isChecked();
    }

    //endregion

    //region Save/Load

    private void Save () {
        SharedPreferences.Editor editor = prefs.edit();
        editor.putBoolean("swapFWDREV", swapFWDREV);
        editor.putBoolean("swapLeftRight", swapLeftRight);
        editor.putBoolean("regulateSpeed", regulateSpeed);
        editor.putBoolean("syncMotors", syncMotors);
        editor.putBoolean("gamepad", gamepad);
        editor.apply();
        startActivity(new Intent(Settings.this, MainActivity.class));
    }

    public void goBack (View view) {
        Save();
    }

    private void readSharedPreferences () {
        swapFWDREV = prefs.getBoolean("swapFWDREV", swapFWDREV);
        swapLeftRight = prefs.getBoolean("swapLeftRight", swapLeftRight);
        regulateSpeed = prefs.getBoolean("regulateSpeed", regulateSpeed);
        syncMotors = prefs.getBoolean("syncMotors", syncMotors);
        gamepad = prefs.getBoolean("gamepad", gamepad);
    }

    @Override
    protected void onSaveInstanceState (@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putBoolean("set_swapFR", swapFWDREV);
        outState.putBoolean("set_swapLR", swapLeftRight);
        outState.putBoolean("set_rSpeed", regulateSpeed);
        outState.putBoolean("set_syncMo", syncMotors);
        outState.putBoolean("set_gamepd", gamepad);
    }

    //endregion

    //region GOTO functions

    public void gotoSource (View view) {
        ((TextView)view).setTextColor(getResources().getColor(R.color.hyperlink_clicked));
        startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(githubURL)));
    }

    //endregion

}
