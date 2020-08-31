package com.ulan.timetable.fragments;

import android.app.AlarmManager;
import android.app.TimePickerDialog;
import android.os.Bundle;
import android.widget.NumberPicker;

import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;
import androidx.preference.PreferenceManager;
import androidx.preference.SwitchPreferenceCompat;

import com.afollestad.materialdialogs.MaterialDialog;
import com.ulan.timetable.R;
import com.ulan.timetable.activities.SettingsActivity;
import com.ulan.timetable.profiles.ProfileManagement;
import com.ulan.timetable.receivers.DailyReceiver;
import com.ulan.timetable.utils.PreferenceUtil;

import java.util.Objects;


public class NotificationSettingsFragment extends PreferenceFragmentCompat {

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        setPreferencesFromResource(R.xml.settings_notification, rootKey);

        ((SettingsActivity) requireActivity()).loadedFragments++;

        setNotif();
        Preference myPref = findPreference("timetableNotif");
        Objects.requireNonNull(myPref).setOnPreferenceClickListener((Preference preference) -> {
            setNotif();
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

    }

    private void setNotif() {
        boolean show = PreferenceManager.getDefaultSharedPreferences(requireContext()).getBoolean("timetableNotif", true);
        findPreference("alwaysNotification").setVisible(show);
        findPreference("alarm").setVisible(show);
        findPreference("reminder").setVisible(show && ProfileManagement.isPreferredProfile());
        findPreference("notification_end").setVisible(show && ProfileManagement.isPreferredProfile());
    }
}
