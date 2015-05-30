package com.eightbitdreams.boxingintervaltimer;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;
import android.os.Vibrator;

/**
 * Created by "8-Bit Dreams" on 30/5/15.
 */
public class VibrateService extends Service {

    @Override
    public void onStart(Intent intent, int startId) {
        super.onStart(intent, startId);

        Vibrator v = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
        // pass the number of milliseconds to vibrate the phone
        // if want to vibrate in a pattern
        // long pattern[] = {0, 800, 200, 800}
        // Second argument is for repeition pass -1 if do not want to repeat.
        // v.vibrate(pattern, -1);
        v.vibrate(1000);

        // Point to note:
        // first 0 means silent for 0 millis
        // 800 means vibrate for 800 millis
        // 200 means silent for 200 millis
        // 800 means vibrate for 800 millis
        // So on. 0 and Even index means silent. Odd = vibrate
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
