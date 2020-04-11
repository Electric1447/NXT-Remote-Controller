package eparon.nxtremotecontroller;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.CheckBox;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

public class Settings extends AppCompatActivity {

    public String PREFS_NXT = "NXTPrefsFile";
    SharedPreferences prefs;

    boolean swapFWDREV = false, swapLeftRight = false, regulateSpeed = false, syncMotors = false, gamepad = true;
    CheckBox fwdrevCB, swapLeftRightCB, regSpeedCB, syncMotorsCB, gamepadCB;

    @Override
    public void onBackPressed () {
        Save();
    }

    @Override
    protected void onCreate (Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        prefs = getSharedPreferences(PREFS_NXT, Context.MODE_PRIVATE);

        swapFWDREV = prefs.getBoolean("swapFWDREV", swapFWDREV);
        swapLeftRight = prefs.getBoolean("swapLeftRight", swapLeftRight);
        regulateSpeed = prefs.getBoolean("regulateSpeed", regulateSpeed);
        syncMotors = prefs.getBoolean("syncMotors", syncMotors);
        gamepad = prefs.getBoolean("gamepad", gamepad);

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

        ((TextView)findViewById(R.id.ver)).setText(String.format("%s v%s\nCreated by Itai Levin.\n\nbased on nxt-remote-control\nby Jacek Fedoryński (jfedor2)", getString(R.string.app_name), BuildConfig.VERSION_NAME)); // Set version TextView.
    }

    public void setFWDREV (View view) {
        fwdrevCB.setChecked(!fwdrevCB.isChecked());
        swapFWDREV = fwdrevCB.isChecked();
    }

    public void setLeftRight (View view) {
        swapLeftRightCB.setChecked(!swapLeftRightCB.isChecked());
        swapLeftRight = swapLeftRightCB.isChecked();
    }

    public void setRegSpeed (View view) {
        regSpeedCB.setChecked(!regSpeedCB.isChecked());
        regulateSpeed = regSpeedCB.isChecked();
    }

    public void setSyncMotors (View view) {
        syncMotorsCB.setChecked(!syncMotorsCB.isChecked());
        syncMotors = syncMotorsCB.isChecked();
    }

    public void setGamepad (View view) {
        gamepadCB.setChecked(!gamepadCB.isChecked());
        gamepad = gamepadCB.isChecked();
    }

    public void goBack (View view) {
        Save();
    }

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

}
