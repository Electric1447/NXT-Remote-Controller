package eparon.nxtremotecontroller;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

public class Splashscreen extends AppCompatActivity {

    public String PREFS_NXT = "NXTPrefsFile";
    SharedPreferences prefs;

    @Override
    protected void onCreate (Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        prefs = getSharedPreferences(PREFS_NXT, Context.MODE_PRIVATE);
        boolean EV3 = prefs.getBoolean("mode", false);

        if (!EV3)
            startActivity(new Intent(this, NXTControlActivity.class));
        else
            startActivity(new Intent(this, EV3ControlActivity.class));
    }

}
