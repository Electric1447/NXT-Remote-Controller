package eparon.nxtremotecontroller;

import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Objects;

import io.noties.markwon.Markwon;

public class Changelog extends AppCompatActivity {

    public static String changelogURL = "https://raw.githubusercontent.com/Electric1447/NXT-Remote-Controller/master/changelog.md";
    TextView mTextView;

    @Override
    public boolean onOptionsItemSelected (@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home)
            onBackPressed();
        return true;
    }

    @Override
    protected void onCreate (Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_changelog);
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);

        mTextView = findViewById(R.id.changelog);

        GetChangelog();
    }

    private void GetChangelog () {
        new Thread(() -> {

            StringBuilder stringBuilder = new StringBuilder();

            try {
                // Getting Changelog file from URL.
                URL url = new URL(changelogURL);
                HttpURLConnection conn = (HttpURLConnection)url.openConnection();
                conn.setConnectTimeout(60000);

                // Defining the BufferedReader to read from the file.
                BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));

                String str;

                // Reading the entire text file
                while ((str = in.readLine()) != null)
                    stringBuilder.append(str).append(System.lineSeparator());

                in.close();

            } catch (Exception e) {
                Log.d("Changelog.d", e.toString());
            }

            // On finish
            runOnUiThread(() -> {
                final Markwon markwon = Markwon.create(getApplicationContext());
                markwon.setMarkdown(mTextView, stringBuilder.toString());
            });

        }).start();
    }

}
