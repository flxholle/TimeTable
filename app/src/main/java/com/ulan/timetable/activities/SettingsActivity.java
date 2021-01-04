package com.ulan.timetable.activities;

import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;

import com.ulan.timetable.R;
import com.ulan.timetable.fragments.SettingsFragment;
import com.ulan.timetable.utils.PreferenceUtil;

import java.util.Objects;

public class SettingsActivity extends AppCompatActivity implements PreferenceFragmentCompat.OnPreferenceStartFragmentCallback {
    public static final String KEY_SEVEN_DAYS_SETTING = "sevendays";
    public static final String KEY_SCHOOL_WEBSITE_SETTING = "schoolwebsite";
    public static final String KEY_START_WEEK_ON_SUNDAY = "start_sunday";

    public int loadedFragments = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setTheme(PreferenceUtil.getGeneralTheme(this));
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        getSupportFragmentManager().beginTransaction()
                .replace(R.id.settings, new SettingsFragment())
                .commit();
    }

    @Override
    public boolean onPreferenceStartFragment(PreferenceFragmentCompat caller, Preference pref) {
        // Instantiate the new Fragment
        final Bundle args = pref.getExtras();
        final Fragment fragment = getSupportFragmentManager().getFragmentFactory().instantiate(
                getClassLoader(),
                pref.getFragment());
        fragment.setArguments(args);
        fragment.setTargetFragment(caller, 0);
        // Replace the existing Fragment with the new Fragment
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.settings, fragment)
                .addToBackStack(null)
                .commit();

        try {
            Objects.requireNonNull(getSupportActionBar()).setTitle(pref.getTitle());
        } catch (Exception ignore) {
        }
        return true;
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        if (loadedFragments == 0) {
            startActivity(new Intent(this, MainActivity.class));
            finish();
        } else {
            loadedFragments--;
            try {
                Objects.requireNonNull(getSupportActionBar()).setTitle(R.string.settings);
            } catch (Exception ignore) {
            }
        }
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }
}
