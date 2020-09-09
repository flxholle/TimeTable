/*
 * Copyright (c) 2020 Felix Hollederer
 *     This file is part of GymWenApp.
 *
 *     GymWenApp is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     GymWenApp is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with GymWenApp.  If not, see <https://www.gnu.org/licenses/>.
 */

package com.ulan.timetable.profiles;

import android.content.Context;
import android.content.SharedPreferences;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.preference.PreferenceManager;

import com.ulan.timetable.R;

import java.util.ArrayList;

public abstract class ProfileManagement {
    private final static char splitChar = '%';
    @NonNull
    private static ArrayList<Profile> profileList = new ArrayList<>();
    private static int preferredProfile;
    private static int selectedProfile;

    public static Profile getProfile(int pos) {
        return profileList.get(pos);
    }

    public static void addProfile(Profile k) {
        profileList.add(k);
    }

    public static void editProfile(int position, Profile newP) {
        profileList.remove(position);
        profileList.add(position, newP);
    }

    public static void removeProfile(int position) {
        profileList.remove(position);
    }

    public static int getSize() {
        return profileList.size();
    }

    @NonNull
    public static ArrayList<Profile> getProfileList() {
        return profileList;
    }

    private static void reload(Context context) {
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(context);
        String pref = sharedPref.getString("profiles", "");
        ArrayList<Profile> pList = new ArrayList<>();
        if (pref.trim().isEmpty()) {
            String name = context.getString(R.string.profile_default_name);
            pList.add(new Profile(name));
            save(context, true);
        } else {
            String[] profiles = pref.split("" + splitChar);
            for (String s : profiles) {
                try {
                    Profile p = new Profile(s);
                    pList.add(p);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
        profileList = pList;
        preferredProfile = sharedPref.getInt("preferred_position", 0);
        resetSelectedProfile();
    }

    private static boolean isUninit() {
        return getProfileList() == null || getProfileList().size() == 0;
    }

    public static void initProfiles(Context context) {
        if (isUninit())
            reload(context);
    }

    public static void save(Context context, boolean apply) {
        StringBuilder all = new StringBuilder();
        for (Profile p : profileList) {
            all.append(p.toString()).append(splitChar);
        }

        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putString("profiles", all.toString());
        editor.putInt("preferred_position", preferredProfile);
        editor.putInt("selected", selectedProfile);
        if (apply)
            editor.apply();
        else
            editor.commit();
    }

    public static boolean isMoreThanOneProfile() {
        return getSize() > 1;
    }

    @NonNull
    public static ArrayList<String> getProfileListNames() {
        ArrayList<String> a = new ArrayList<>();
        for (Profile p : profileList) {
            a.add(p.getName());
        }
        return a;
    }

    //Positions
    public static void setSelectedProfile(int position) {
        selectedProfile = position;
    }

    public static void resetSelectedProfile() {
        setSelectedProfile(loadPreferredProfilePosition());
    }

    public static Profile getSelectedProfile() {
        return getProfile(getSelectedProfilePosition());
    }

    public static int getSelectedProfilePosition() {
        return selectedProfile;
    }

    //Preferred Profile
    public static void checkPreferredProfile() {
        if (preferredProfile >= getSize()) {
            setPreferredProfilePosition(0);
        }
    }

    public static int getPreferredProfilePosition() {
        return preferredProfile;
    }

    public static void setPreferredProfilePosition(int value) {
        if (value == preferredProfile)
            preferredProfile = -1;
        else
            preferredProfile = value;
    }

    public static boolean isPreferredProfile() {
        return (preferredProfile < getSize() && preferredProfile >= 0) || !isMoreThanOneProfile();
    }

    public static int loadPreferredProfilePosition() {
        if (preferredProfile < 0 || preferredProfile >= getSize())
            return 0;
        return preferredProfile;
    }

    @Nullable
    public static Profile getPreferredProfile() {
        int pos = getPreferredProfilePosition();
        if (pos < 0 || pos >= getSize())
            return null;
        return getProfile(pos);
    }
}
