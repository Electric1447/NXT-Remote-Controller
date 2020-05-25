package eparon.nxtremotecontroller;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;

public class Splashscreen extends Activity {

    @Override
    protected void onCreate (Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        SharedPreferences prefs = getSharedPreferences(NXTControlActivity.PREFS_NXT, Context.MODE_PRIVATE);
        boolean EV3 = prefs.getBoolean("mode", false);

        if (!EV3)
            startActivity(new Intent(this, NXTControlActivity.class));
        else
            startActivity(new Intent(this, EV3ControlActivity.class));
    }

}
