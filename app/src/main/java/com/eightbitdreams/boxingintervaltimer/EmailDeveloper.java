package com.eightbitdreams.boxingintervaltimer;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

/**
 * Created by "8-Bit Dreams" on 31/5/15.
 */
public class EmailDeveloper extends Activity {
    private static final int EMAIL_SUCCESS = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Intent email = new Intent(Intent.ACTION_SEND);
        email.putExtra(Intent.EXTRA_SUBJECT, "Boxing Interval Timer: Bugs");
        email.putExtra(Intent.EXTRA_EMAIL, new String[]{"eightbitnightmare@gmail.com"});
        email.putExtra(Intent.EXTRA_TEXT, "");
        email.setType("message/rfc822");
        startActivity(email);

        super.onCreate(savedInstanceState);
    }
}
