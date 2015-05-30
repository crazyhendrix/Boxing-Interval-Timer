package com.eightbitdreams.boxingintervaltimer;

import android.os.Bundle;
import android.preference.PreferenceActivity;

/**
 * Created by "8-Bit Dreams" on 29/5/15.
 */
public class SettingsActivity extends PreferenceActivity {
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.preferences);
    }
}
