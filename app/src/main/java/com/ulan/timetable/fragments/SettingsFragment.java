package com.ulan.timetable.fragments;

import android.app.AlarmManager;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.widget.NumberPicker;

import androidx.core.graphics.drawable.DrawableCompat;
import androidx.preference.ListPreference;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;
import androidx.preference.PreferenceGroup;
import androidx.preference.PreferenceManager;
import androidx.preference.SwitchPreferenceCompat;

import com.afollestad.materialdialogs.MaterialDialog;
import com.ulan.timetable.R;
import com.ulan.timetable.activities.TimeSettingsActivity;
import com.ulan.timetable.receivers.DailyReceiver;
import com.ulan.timetable.utils.PreferenceUtil;

import java.util.Objects;


public class SettingsFragment extends PreferenceFragmentCompat {

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        setPreferencesFromResource(R.xml.settings, rootKey);

        tintIcons(getPreferenceScreen(), PreferenceUtil.getTextColorPrimary(requireContext()));

        setNotif();

        Preference myPref = findPreference("timetableNotif");
        Objects.requireNonNull(myPref).setOnPreferenceClickListener((Preference preference) -> {
            setNotif();
            return true;
        });

        myPref = findPreference("alarm");
        Objects.requireNonNull(myPref).setOnPreferenceClickListener((Preference p) -> {
            int[] oldTimes = PreferenceUtil.getAlarmTime(getContext());
            TimePickerDialog timePickerDialog = new TimePickerDialog(getActivity(),
                    (view, hourOfDay, minute) -> {
                        PreferenceUtil.setAlarmTime(requireContext(), hourOfDay, minute, 0);
                        PreferenceUtil.setRepeatingAlarm(requireContext(), DailyReceiver.class, hourOfDay, minute, 0, DailyReceiver.DailyReceiverID, AlarmManager.INTERVAL_DAY);
                        p.setSummary(hourOfDay + ":" + minute);
                    }, oldTimes[0], oldTimes[1], true);
            timePickerDialog.setTitle(R.string.choose_time);
            timePickerDialog.show();
            return true;
        });
        int[] oldTimes = PreferenceUtil.getAlarmTime(getContext());
        myPref.setSummary(oldTimes[0] + ":" + oldTimes[1]);

        setTurnOff();
        myPref = findPreference("automatic_do_not_disturb");
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


        SwitchPreferenceCompat reminderSwitch = findPreference("reminder");
        Objects.requireNonNull(reminderSwitch).setSummary(getString(R.string.notification_reminder_settings_desc, "" + PreferenceUtil.getReminderTime(requireContext())));
        Objects.requireNonNull(reminderSwitch).setOnPreferenceClickListener((Preference p) -> {
            if (reminderSwitch.isChecked()) {
                NumberPicker numberPicker = new NumberPicker(getContext());
                int max = PreferenceUtil.getPeriodLength(requireContext()) - 2;
                numberPicker.setMaxValue(max <= 0 ? 2 : max);
                numberPicker.setMinValue(1);
                numberPicker.setValue(PreferenceUtil.getReminderTime(requireContext()));
                new MaterialDialog.Builder(requireContext())
                        .customView(numberPicker, false)
                        .positiveText(R.string.select)
                        .onPositive((d, w) -> {
                            int value = numberPicker.getValue();
                            PreferenceUtil.setReminderTime(requireContext(), value);
                            reminderSwitch.setSummary(getString(R.string.notification_reminder_settings_desc, "" + value));
                        })
                        .show();
            }
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

    private void setNotif() {
        boolean show = PreferenceManager.getDefaultSharedPreferences(requireContext()).getBoolean("timetableNotif", true);
        findPreference("alwaysNotification").setVisible(show);
        findPreference("alarm").setVisible(show);
    }

    private void setTurnOff() {
        boolean show = PreferenceManager.getDefaultSharedPreferences(requireContext()).getBoolean("automatic_do_not_disturb", true);
        findPreference("do_not_disturb_turn_off").setVisible(show);
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
