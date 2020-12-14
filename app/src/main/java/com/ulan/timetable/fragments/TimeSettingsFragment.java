package com.ulan.timetable.fragments;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.os.Bundle;
import android.widget.NumberPicker;

import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;

import com.afollestad.materialdialogs.MaterialDialog;
import com.ulan.timetable.R;
import com.ulan.timetable.utils.PreferenceUtil;
import com.ulan.timetable.utils.WeekUtils;

import java.util.Calendar;
import java.util.Date;
import java.util.Objects;


public class TimeSettingsFragment extends PreferenceFragmentCompat {

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        setPreferencesFromResource(R.xml.settings_time, rootKey);

        Preference myPref = findPreference("start_time");
        Objects.requireNonNull(myPref).setOnPreferenceClickListener((Preference p) -> {
            int[] oldTimes = PreferenceUtil.getStartTime(getContext());
            TimePickerDialog timePickerDialog = new TimePickerDialog(getActivity(),
                    (view, hourOfDay, minute) -> {
                        PreferenceUtil.setStartTime(getContext(), hourOfDay, minute, 0);
                        p.setSummary(hourOfDay + ":" + minute);
                    }, oldTimes[0], oldTimes[1], true);
            timePickerDialog.setTitle(R.string.start_of_school);
            timePickerDialog.show();
            return true;
        });
        int[] oldTimes = PreferenceUtil.getStartTime(getContext());
        myPref.setSummary(oldTimes[0] + ":" + oldTimes[1]);


        myPref = findPreference("set_period_length");
        Objects.requireNonNull(myPref).setOnPreferenceClickListener((Preference p) -> {
            NumberPicker numberPicker = new NumberPicker(getContext());
            numberPicker.setMaxValue(180);
            numberPicker.setMinValue(1);
            numberPicker.setValue(PreferenceUtil.getPeriodLength(getContext()));
            new MaterialDialog.Builder(requireContext())
                    .customView(numberPicker, false)
                    .positiveText(R.string.select)
                    .onPositive((d, w) -> {
                        int value = numberPicker.getValue();
                        PreferenceUtil.setPeriodLength(getContext(), value);
                        p.setSummary(value + " " + getString(R.string.minutes));
                    })
                    .show();
            return true;
        });
        myPref.setSummary(PreferenceUtil.getPeriodLength(getContext()) + " " + getString(R.string.minutes));

        myPref = findPreference("two_weeks");
        Objects.requireNonNull(myPref).setOnPreferenceClickListener((p) -> {
            setTermStartVisibility();
            return true;
        });

        setTermStartVisibility();
        myPref = findPreference("term_start");

        Calendar calendar = PreferenceUtil.getTermStart(requireContext());

        Objects.requireNonNull(myPref).setTitle(getString(R.string.start_of_term) + " (" + WeekUtils.localizeDate(requireContext(), new Date(calendar.getTimeInMillis())) + ")");
        myPref.setOnPreferenceClickListener((p) -> {
            Calendar calendar2 = PreferenceUtil.getTermStart(requireContext());
            int mYear2 = calendar2.get(Calendar.YEAR);
            int mMonth2 = calendar2.get(Calendar.MONTH);
            int mDayofMonth2 = calendar2.get(Calendar.DAY_OF_MONTH);

            DatePickerDialog datePickerDialog = new DatePickerDialog(requireActivity(), (view, year, month, dayOfMonth) -> {
                PreferenceUtil.setTermStart(requireContext(), year, month, dayOfMonth);
                Calendar cal = Calendar.getInstance();
                cal.set(Calendar.YEAR, year);
                cal.set(Calendar.MONTH, month);
                cal.set(Calendar.DAY_OF_MONTH, dayOfMonth);

                p.setTitle(getString(R.string.start_of_term) + " (" + WeekUtils.localizeDate(requireContext(), new Date(cal.getTimeInMillis())) + ")");
            }, mYear2, mMonth2, mDayofMonth2);

            datePickerDialog.setTitle(R.string.choose_date);
            datePickerDialog.show();
            return true;
        });
    }

    private void setTermStartVisibility() {
        findPreference("term_start").setVisible(PreferenceUtil.isTwoWeeksEnabled(requireContext()));
    }
}
