package com.eightbitdreams.boxingintervaltimer;

import android.content.Intent;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceActivity;

/**
 * Created by "8-Bit Dreams" on 29/5/15.
 */
public class SettingsActivity extends PreferenceActivity {
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.preferences);
        Preference mailTo = (Preference) findPreference("mailTo");

        mailTo.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                Intent mailto = new Intent(Intent.ACTION_SEND);
                mailto.setType("message/rfc822") ; // use from live device
                mailto.putExtra(Intent.EXTRA_EMAIL, new String[]{"eightbitnightmare@gmail.com"});
                mailto.putExtra(Intent.EXTRA_SUBJECT,"Boxing Interval Timer");
                mailto.putExtra(Intent.EXTRA_TEXT,"");
                startActivity(Intent.createChooser(mailto, "Select email application."));
                return true;
            }
        });
    }

    @Override
    protected void onStop() {
        Intent i = new Intent(this, MainActivity.class);
        startActivity(i);
        super.onStop();
    }
}
