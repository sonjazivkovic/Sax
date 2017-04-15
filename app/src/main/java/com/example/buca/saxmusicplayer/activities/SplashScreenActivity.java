package com.example.buca.saxmusicplayer.activities;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;

import com.example.buca.saxmusicplayer.MainActivity;
import com.example.buca.saxmusicplayer.R;


/**
 * Created by Buca on 4/15/2017.
 */

public class SplashScreenActivity extends Activity {
    private static int SPLASH_DISPLAY_LENGTH = 3000;
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.splash);
        new Handler().postDelayed(new Runnable() {
            public void run() {
                startActivity(new Intent(SplashScreenActivity.this, MainActivity.class));
                finish();
            }
        }, SPLASH_DISPLAY_LENGTH);
    }
}
