package com.ulan.timetable.fragments;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.drawable.Drawable;
import android.os.Bundle;

import androidx.core.graphics.drawable.DrawableCompat;
import androidx.preference.ListPreference;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;
import androidx.preference.PreferenceGroup;
import androidx.preference.PreferenceManager;

import com.ulan.timetable.R;
import com.ulan.timetable.activities.TimeSettingsActivity;
import com.ulan.timetable.utils.PreferenceUtil;

import java.util.Objects;


public class SettingsFragment extends PreferenceFragmentCompat {

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        setPreferencesFromResource(R.xml.settings, rootKey);

        tintIcons(getPreferenceScreen(), PreferenceUtil.getTextColorPrimary(requireContext()));

        setTurnOff();
        Preference myPref = findPreference("automatic_do_not_disturb");
        Objects.requireNonNull(myPref).setOnPreferenceClickListener((Preference p) -> {
            PreferenceUtil.setDoNotDisturb(requireActivity(), false);
            setTurnOff();
            return true;
        });

        ListPreference mp = findPreference("theme");
        Objects.requireNonNull(mp).setOnPreferenceChangeListener((preference, newValue) -> {
            mp.setValue(newValue + "");
            requireActivity().recreate();
            return false;
        });
        mp.setSummary(getThemeName());

        myPref = findPreference("time_settings");
        Objects.requireNonNull(myPref).setOnPreferenceClickListener(p -> {
            startActivity(new Intent(getActivity(), TimeSettingsActivity.class));
            return true;
        });

        showPreselectionElements();
        myPref = findPreference("is_preselection");
        Objects.requireNonNull(myPref).setOnPreferenceClickListener(p -> {
            showPreselectionElements();
            return true;
        });
    }

    private String getThemeName() {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(requireContext());

        String selectedTheme = sharedPreferences.getString("theme", "switch");
        String[] values = getResources().getStringArray(R.array.theme_array_values);

        String[] names = getResources().getStringArray(R.array.theme_array);

        for (int i = 0; i < values.length; i++) {
            if (values[i].equalsIgnoreCase(selectedTheme)) {
                return names[i];
            }
        }

        return "";
    }

    private void setTurnOff() {
        boolean show = PreferenceUtil.isAutomaticDoNotDisturb(requireContext());
        findPreference("do_not_disturb_turn_off").setVisible(show);
    }

    private void showPreselectionElements() {
        boolean show = PreferenceUtil.isPreselectionList(requireContext());
        findPreference("preselection_elements").setVisible(show);
    }

    private static void tintIcons(Preference preference, int color) {
        if (preference instanceof PreferenceGroup) {
            PreferenceGroup group = ((PreferenceGroup) preference);
            for (int i = 0; i < group.getPreferenceCount(); i++) {
                tintIcons(group.getPreference(i), color);
            }
        } else {
            Drawable icon = preference.getIcon();
            if (icon != null) {
                DrawableCompat.setTint(icon, color);
            }
        }
    }
}
