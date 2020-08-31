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

import com.afollestad.materialdialogs.MaterialDialog;
import com.ulan.timetable.R;
import com.ulan.timetable.activities.TimeSettingsActivity;
import com.ulan.timetable.profiles.ProfileManagement;
import com.ulan.timetable.utils.PreferenceUtil;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
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
        myPref.setVisible(ProfileManagement.isPreferredProfile());

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

        myPref = findPreference("preselection_elements");
        Objects.requireNonNull(myPref).setOnPreferenceClickListener(p -> {
            ArrayList<String> preselectedValues = new ArrayList<>(Arrays.asList(requireContext().getResources().getStringArray(R.array.preselected_subjects_values)));

            String[] preselected = PreferenceUtil.getPreselectionElements(requireContext());
            List<Integer> preselectedIndices = new ArrayList<>();

            for (int i = 0; i < preselected.length; i++) {
                preselectedIndices.add(preselectedValues.indexOf(preselected[i]));
            }


            new MaterialDialog.Builder(requireContext())
                    .title(R.string.set_preselection_elements)
                    .items(R.array.preselected_subjects)
                    .itemsCallbackMultiChoice(preselectedIndices.toArray(new Integer[]{}), (dialog, which, text) -> {
                        List<String> selection = new ArrayList<>();
                        for (int i = 0; i < which.length; i++) {
                            selection.add(preselectedValues.get(which[i]));
                        }
                        PreferenceUtil.setPreselectionElements(requireContext(), selection.toArray(new String[]{}));
                        return true;
                    })
                    .positiveText(R.string.ok)
                    .onPositive(((dialog, which) -> dialog.dismiss()))
                    .negativeText(R.string.cancel)
                    .onNegative((dialog, action) -> dialog.dismiss())
                    .neutralText(R.string.de_select_all)
                    .onNeutral((dialog, action) -> {
                        Integer[] selection = dialog.getSelectedIndices();
                        if (Objects.requireNonNull(selection).length == 0) {
                            Integer[] select = new Integer[preselectedValues.size()];
                            for (int i = 0; i < select.length; i++) {
                                select[i] = i;
                            }
                            dialog.setSelectedIndices(select);
                        } else {
                            dialog.setSelectedIndices(new Integer[]{});
                        }
                    })
                    .autoDismiss(false)
                    .show();
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
        boolean show = PreferenceUtil.isAutomaticDoNotDisturb(requireContext()) && ProfileManagement.isPreferredProfile();
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
