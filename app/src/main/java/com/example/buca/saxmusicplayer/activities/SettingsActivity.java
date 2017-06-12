package com.example.buca.saxmusicplayer.activities;

import android.app.Activity;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.preference.PreferenceScreen;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;

import com.example.buca.saxmusicplayer.MainActivity;
import com.example.buca.saxmusicplayer.R;

import java.util.Locale;

/**
 * Created by Stefan on 14/05/2017.
 */

public class SettingsActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.settings);

        Toolbar toolbar = (Toolbar) findViewById(R.id.settings_toolbar);
        setSupportActionBar(toolbar);

        getSupportActionBar().setHomeButtonEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        //Display the fragment as the main content
        SettingsFragment sf = new SettingsFragment();
        getFragmentManager().beginTransaction().replace(R.id.preference_fragment_container, sf).commit();
        getFragmentManager().executePendingTransactions();

        ListPreference lp = (ListPreference) sf.findPreference("language_preference");
        lp.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(Preference preference, Object newValue) {
                Intent mainIntent = new Intent(MainActivity.Broadcast_UPDATE_UI_MAIN_ACTIVITY);
                mainIntent.putExtra(MainActivity.Broadcast_RESET_MAIN_ACTIVITY, true);
                sendBroadcast(mainIntent);
                finish();
                return true;
            }
        });
    }

    public static class SettingsFragment extends PreferenceFragment {
        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            /*Izabran jezik ce biti smesten u Shared preference fajl*/
            PreferenceManager pm = getPreferenceManager();
            pm.setSharedPreferencesName("language_preference");
            addPreferencesFromResource(R.xml.preferences);
        }
    }

}
