package com.eightbitdreams.boxingintervaltimer;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.media.AudioManager;
import android.media.SoundPool;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Vibrator;
import android.preference.PreferenceManager;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import java.util.concurrent.TimeUnit;


public class MainActivity extends Activity implements SensorEventListener{
    private TextView textViewTime;
    private TextView textViewRounds;
    private Button buttonRun, buttonReset, buttonSettings;
    private boolean running = false, prep;
    private String runTime, restTime;
    private long workTimeMillis, restTimeMillis, intervalMillis, prepTimeMillis, endRoundWarnMillis;

    private enum State {
        REST, WORK, PREP, DONE
    };
    private State state = null;
    private int roundCurrent, roundsTotal;

    private Counter workTimer, restTimer, prepTimer;

    private SoundPool soundPool;
    private boolean loaded, alertPlayed, mute, proximity;
    private int soundAlertId, soundBellId;

    SharedPreferences sp;
    private Vibrator v;
    private long[] vPattern = {0, 300, 100, 300, 100, 300};
    private SensorManager sensorManager;
    private Sensor proximitySensor;

    // TempTimer object for pausing and resuming
    private long tempMillisLeft = 0;
    // SensorEventListener Override Methods
    @Override
    public void onSensorChanged(SensorEvent event) {
        if (event.values[0] == 0 && proximity) {
            if (prepTimer == null && roundCurrent == 1 && state == State.PREP) {
                prepTimer = new Counter(prepTimeMillis, intervalMillis);
                running = true;
                prepTimer.start();
                setTextViewTimeColor();
                buttonRun.setText(R.string.pause);
            } else if (workTimer == null && roundCurrent == 1 && state == State.WORK) {
                workTimer = new Counter(workTimeMillis, intervalMillis);
                running = true;
                workTimer.start();
                setTextViewTimeColor();
                buttonRun.setText(R.string.pause);
                playBellSoundVibrate();
            } else {
                if (!running) {
                    if (state == State.WORK) {
                        workTimerResume();
                    } else if (state == State.REST){
                        restTimerResume();
                    } else if (state == State.PREP) {
                        prepTimerResume();
                    }
                } else {
                    if (state == State.WORK) {
                        workTimerPause();
                    } else if (state == State.REST) {
                        restTimerPause();
                    } else if (state == State.PREP) {
                        prepTimerPause();
                    }
                }
            }
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        sp = PreferenceManager.getDefaultSharedPreferences(this);

        // Sets the hardware button to control music volume
        this.setVolumeControlStream(AudioManager.STREAM_MUSIC);

        soundPool = new SoundPool(10, AudioManager.STREAM_MUSIC, 0);
        soundPool.setOnLoadCompleteListener(new SoundPool.OnLoadCompleteListener() {
            @Override
            public void onLoadComplete(SoundPool soundPool, int sampleId, int status) {
                loaded = true;
            }
        });
        soundAlertId = soundPool.load(this, R.raw.bit_alert, 1);
        soundBellId = soundPool.load(this, R.raw.bit_bell, 1);

        roundCurrent = 1;

        // Load shared preferences
        loadPreferences(sp);

        // Initialize Vibration on phone
        v = (Vibrator) this.getSystemService(VIBRATOR_SERVICE);

        textViewTime = (TextView) findViewById(R.id.time_left_textView);
        textViewTime.setText(runTime);
        textViewRounds = (TextView) findViewById(R.id.round_number);
        setRoundTextView();
        buttonRun = (Button) findViewById(R.id.run_button);
        buttonRun.setText(R.string.work);
        buttonRun.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (prepTimer == null && roundCurrent == 1 && state == State.PREP) {
                    prepTimer = new Counter(prepTimeMillis, intervalMillis);
                    running = true;
                    prepTimer.start();
                    setTextViewTimeColor();
                    buttonRun.setText(R.string.pause);
                } else if (workTimer == null && roundCurrent == 1 && state == State.WORK) {
                    workTimer = new Counter(workTimeMillis, intervalMillis);
                    running = true;
                    workTimer.start();
                    setTextViewTimeColor();
                    buttonRun.setText(R.string.pause);
                    playBellSoundVibrate();
                } else {
                    if (!running) {
                        if (state == State.WORK) {
                            workTimerResume();
                        } else if (state == State.REST){
                            restTimerResume();
                        } else if (state == State.PREP) {
                            prepTimerResume();
                        }
                    } else {
                        if (state == State.WORK) {
                            workTimerPause();
                        } else if (state == State.REST) {
                            restTimerPause();
                        } else if (state == State.PREP) {
                            prepTimerPause();
                        }
                    }
                }
            }
        });
        buttonReset = (Button) findViewById(R.id.reset_button);
        buttonReset.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (running) {
                    Toast.makeText(MainActivity.this, R.string.stop_toast, Toast.LENGTH_SHORT).show();
                } else {
                    if (restTimer != null) {
                        restTimer.cancel();
                        restTimer = null;
                    }
                    if (workTimer != null) {
                        workTimer.cancel();
                        workTimer = null;
                    }
                    if (prepTimer != null) {
                        prepTimer.cancel();
                        prepTimer = null;
                    }
                    tempMillisLeft = workTimeMillis;
                    roundCurrent = 1;
                    setRoundTextView();
                    textViewTime.setText(runTime);
                    if (prep) {
                        state = State.PREP;
                    } else {
                        state = State.WORK;
                    }
                    buttonRun.setText(R.string.work);
                    running = false;
                    setTextViewTimeColor();
                }
            }
        });
        buttonSettings = (Button) findViewById(R.id.settings_button);
        buttonSettings.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (running) {
                    Toast.makeText(MainActivity.this, R.string.stop_toast, Toast.LENGTH_SHORT).show();
                } else {
                    Intent i = new Intent(MainActivity.this, SettingsActivity.class);
                    startActivity(i);
                    finish();
                }
            }
        });
    }

    @Override
    protected void onResume() {
        // TODO Buggy here. If home button pressed, Timer is reseted
        // Better to put this under when SettingsActivity is called.
        super.onResume();
//        if (proximity) {
//            sensorManager.registerListener(this, proximitySensor, SensorManager.SENSOR_DELAY_NORMAL);
//        }
//        if (workTimer != null) {
//            workTimer.cancel();
//            workTimer = null;
//        }
//        if (restTimer != null) {
//            restTimer.cancel();
//            restTimer = null;
//        }
//        if (prepTimer != null) {
//            prepTimer.cancel();
//            prepTimer = null;
//        }
//        loadPreferences(sp);
//        roundCurrent = 1;
//        setRoundTextView();
//        running = false;
//        buttonRun.setText(R.string.work);
//        textViewTime.setText(runTime);
//        setTextViewTimeColor();
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (proximity) {
            sensorManager.unregisterListener(this);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void playAlertSound() {
        AudioManager audioManager = (AudioManager) getSystemService(AUDIO_SERVICE);
        float actualVolume = (float) audioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
        float maxVolume = (float) audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
        float volume = actualVolume / maxVolume;
        if (loaded && !mute) {
            soundPool.play(soundAlertId, volume, volume, 1, 0, 1f);
        }
    }

    private void playBellSoundVibrate() {
        AudioManager audioManager = (AudioManager) getSystemService(AUDIO_SERVICE);
        float actualVolume = (float) audioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
        float maxVolume = (float) audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
        float volume = actualVolume / maxVolume;
        if (loaded && !mute) {
            soundPool.play(soundBellId, volume, volume, 1, 0, 1f);
        }
        v.vibrate(vPattern, -1);
    }
    // TODO Clean up code : set restTimer/WorkTimer to null

    private void restTimerPause() {
        tempMillisLeft = restTimer.getMillisLeft();
        restTimer.cancel();
        buttonRun.setText(R.string.work);
        running = false;
        setTextViewTimeColor();
    }

    private void restTimerResume() {
        restTimer = new Counter(tempMillisLeft, intervalMillis);
        running = true;
        buttonRun.setText(R.string.pause);
        restTimer.start();
        setTextViewTimeColor(tempMillisLeft);
    }

    private void prepTimerPause() {
        tempMillisLeft = prepTimer.getMillisLeft();
        prepTimer.cancel();
        buttonRun.setText(R.string.work);
        running = false;
        setTextViewTimeColor();
    }

    private void prepTimerResume() {
        prepTimer = new Counter(tempMillisLeft, intervalMillis);
        running = true;
        buttonRun.setText(R.string.pause);
        prepTimer.start();
        setTextViewTimeColor();
    }

    private void workTimerPause() {
        tempMillisLeft = workTimer.getMillisLeft();
        workTimer.cancel();
        buttonRun.setText(R.string.work);
        running = false;
        setTextViewTimeColor();
    }

    private void workTimerResume() {
        workTimer = new Counter(tempMillisLeft, intervalMillis);
        running = true;
        buttonRun.setText(R.string.pause);
        workTimer.start();
        setTextViewTimeColor(tempMillisLeft);
    }

    private void setRoundTextView() {
        textViewRounds.setText(" " + roundCurrent + "/" + roundsTotal);
    }

    private void setTextViewTimeColor(long millis) {
        if (!running) {
            textViewTime.setTextColor(Color.WHITE);
        } else if (running && state == State.WORK) {
            if (millis <= endRoundWarnMillis) {
                textViewTime.setTextColor(Color.YELLOW);
            } else
                textViewTime.setTextColor(Color.GREEN);
        } else if (running && state == State.REST) {
            if (millis <= prepTimeMillis) {
                textViewTime.setTextColor(Color.YELLOW);
            } else
                textViewTime.setTextColor(Color.RED);
        }
    }

    private void setTextViewTimeColor() {
        if (!running) {
            textViewTime.setTextColor(Color.WHITE);
        } else if (running && state == State.WORK) {
            textViewTime.setTextColor(Color.GREEN);
        } else if (running && state == State.REST) {
            textViewTime.setTextColor(Color.RED);
        } else if (running && state == State.PREP) {
            textViewTime.setTextColor(Color.YELLOW);
        }
    }

    private void setTextViewTimeWarnPrep(long millis) {
        if (millis <= endRoundWarnMillis && state == State.WORK) {
            textViewTime.setTextColor(Color.YELLOW);
        }
        if (millis <= prepTimeMillis && state == State.REST) {
            textViewTime.setTextColor(Color.YELLOW);
        }
    }

    private void loadPreferences(SharedPreferences sp) {
        proximity = sp.getBoolean("proximity_sensor_key", true);
        if (proximity) {
            sensorManager = (SensorManager) this.getSystemService(SENSOR_SERVICE);
            proximitySensor = sensorManager.getDefaultSensor(Sensor.TYPE_PROXIMITY);
        }
        roundsTotal = Integer.parseInt(sp.getString("number_rounds_key", "12"));
        workTimeMillis = TimePreference.getMillis(sp.getString("round_time_key", "3:00"));
        restTimeMillis = TimePreference.getMillis(sp.getString("rest_time_key", "1:00"));
        mute = sp.getBoolean("mute_key", false);
        intervalMillis = 1000;
        alertPlayed = false;
        prep = sp.getBoolean("prep_time_key", true);
        prepTimeMillis = 10000;
        if (prep) {
            state = State.PREP;
        } else {
            state = State.WORK;
        }
        endRoundWarnMillis = Long.parseLong(sp.getString("warn_time_key", "20")) * 1000;
        runTime = String.format("%02d:%02d",
                TimeUnit.MILLISECONDS.toMinutes(workTimeMillis),
                TimeUnit.MILLISECONDS.toSeconds(workTimeMillis) % 60);
        restTime = String.format("%02d:%02d",
                TimeUnit.MILLISECONDS.toMinutes(restTimeMillis),
                TimeUnit.MILLISECONDS.toSeconds(restTimeMillis) % 60);
    }

    // Counter CLASS Section

    private class Counter extends CountDownTimer {
        private String ms;
        private long millisLeft;
        private long mins, secs;

        public Counter(long millisInFuture, long countDownInterval) {
            super(millisInFuture, countDownInterval);
        }

        @Override
        public void onFinish() {
            if (state == State.PREP) {
                if (prepTimer != null) {
                    prepTimer = null;
                }
                state = State.WORK;
                ms = runTime;
                workTimer = new Counter(workTimeMillis, intervalMillis);
                workTimer.start();
                playBellSoundVibrate();
                setTextViewTimeColor();
            } else if (state == State.REST && roundCurrent < roundsTotal) {
                ++roundCurrent;
                if (restTimer != null) {
                    restTimer.cancel();
                    restTimer = null;
                }
                state = State.WORK;
                ms = runTime;
                setRoundTextView();
                setTextViewTimeColor();
                workTimer = new Counter(workTimeMillis, intervalMillis);
                workTimer.start();
                alertPlayed = false;
                playBellSoundVibrate();
            } else if (state == State.WORK && roundCurrent < roundsTotal) {
                if (workTimer != null) {
                    workTimer.cancel();
                    workTimer = null;
                }
                if (restTimeMillis > 0) {
                    state = State.REST;
                    setRoundTextView();
                    setTextViewTimeColor();
                    ms = restTime;
                    restTimer = new Counter(restTimeMillis, intervalMillis);
                    restTimer.start();
                    alertPlayed = false;
                    playBellSoundVibrate();
                } else {
                    ++roundCurrent;
                    state = State.WORK;
                    ms = runTime;
                    setRoundTextView();
                    setTextViewTimeColor();
                    workTimer = new Counter(workTimeMillis, intervalMillis);
                    workTimer.start();
                    alertPlayed = false;
                    playBellSoundVibrate();
                }
            } else if (roundCurrent == roundsTotal) {
                textViewTime.setText("DONE!");
                state = State.DONE;
                alertPlayed = false;
                playBellSoundVibrate();
                setRoundTextView();
                setTextViewTimeColor();
                running = false;
                if (restTimer != null) {
                    restTimer.cancel();
                    restTimer = null;
                }
                if (restTimer != null) {
                    workTimer.cancel();
                    workTimer = null;
                }
                ms = runTime;
            }
        }

        @Override
        public void onTick(long millisUntilFinished) {
            millisLeft = millisUntilFinished;
            mins = TimeUnit.MILLISECONDS.toMinutes(millisLeft);
            secs = TimeUnit.MILLISECONDS.toSeconds(millisLeft) % 60;
            ms = String.format("%02d:%02d", mins, secs);
            setTextViewTimeWarnPrep(millisLeft);
            // System.out.println(ms);
            if (millisLeft <= endRoundWarnMillis && !alertPlayed && state == State.WORK
                    || (millisLeft <= prepTimeMillis && state == State.REST && !alertPlayed)) {
                playAlertSound();
                alertPlayed = true;
            }
            textViewTime.setText(ms);
        }

        public long getMillisLeft() {
            return millisLeft;
        }
    }

}
