package com.example.android.desantapp;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Intent;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.ToneGenerator;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.provider.Settings;
import android.support.annotation.RequiresApi;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;

import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;


import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.TimeZone;
import java.util.concurrent.TimeUnit;

public class DominationActivity extends Activity {
    public final static int REQUEST_CODE = 10101;

    private TextView timer, textViewTeamRed, textViewTeamBlue;
    private Button btnRed, btnBlue;
    private MediaPlayer syrena, pikanie, alarm;
    private boolean buttonRedPressed = false, buttonBluePressed = false;
    private boolean startGame =false;
    private boolean GameOver = false;
    long mTouchDownTime;
    ToneGenerator toneGen1;
    long millisRedTeam = 0, millisBlueTeam = 0;
    public boolean redDominator = false;
    public boolean blueDominator = false;
    private int DEVICE_ID = 2;
    private DatabaseReference root;
    private DatabaseReference root2;
    private CounterClass timerMain;
    private CounterClass timerTeamBlue, timerTeamRed;
    public boolean returnRed = false, returnBlue = false;
    private long gameTime = 180000;
    private boolean timerResume = false;
    long millis;
    public DominationActivity() {
    }

    public boolean checkDrawOverlayPermission() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            return true;
        }
        if (!Settings.canDrawOverlays(this)) {
            /** if not construct intent to request permission */
            Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                    Uri.parse("package:" + getPackageName()));
            /** request permission via start activity for result */
            startActivityForResult(intent, REQUEST_CODE);
            return false;
        } else {
            return true;
        }
    }

    @Override
    @TargetApi(Build.VERSION_CODES.M)
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_CODE) {
            if (Settings.canDrawOverlays(this)) {
                startService(new Intent(this, PowerButtonService.class));
            }
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.domination_activity);
        textViewTeamBlue = (TextView) findViewById(R.id.textViewTeamBlue);
        textViewTeamRed = (TextView) findViewById(R.id.textViewTeamRed);
        btnRed = (Button) findViewById(R.id.btnRed);
        btnBlue = (Button) findViewById(R.id.btnBlue);
        root = FirebaseDatabase.getInstance().getReference().child("Game");
        timer = (TextView) findViewById(R.id.textViewMainTimer);

        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED);

        toneGen1 = new ToneGenerator(AudioManager.STREAM_MUSIC, 100);
        syrena = MediaPlayer.create(this, R.raw.syrena);
        alarm = MediaPlayer.create(this,R.raw.alarm);
        pikanie = MediaPlayer.create(this,R.raw.pikanie);


        // Przycisk druzyny czerwonej
        btnRed.setOnTouchListener(new View.OnTouchListener() {
            CounterClass timer_red = new CounterClass(5000,1000);
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if(buttonBluePressed==false) {
                    if (event.getAction() == MotionEvent.ACTION_DOWN) {
                        buttonRedPressed = true;
                        timer_red = new CounterClass(5000, 1000);
                        timer_red.setButton(btnRed);
                        timer_red.buttonRed = true;
                        timer_red.start();
                        Log.v("CustomDebug", "Message: DOWN" + mTouchDownTime);

                    }
                    if (event.getAction() == MotionEvent.ACTION_UP) {
                        buttonRedPressed = false;
                        timer_red.cancel();
                        if (timer_red.isFinished() == false) {
                            btnRed.setText("PRZYTRZYMAJ 5SEKUND");
                        }

                    }
                }
                return false;
            }
        });

        // Przycisk drużyny niebieskiej
        btnBlue.setOnTouchListener(new View.OnTouchListener() {
            CounterClass timer_blue = new CounterClass(5000,1000);
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if(buttonRedPressed==false) {
                    if (event.getAction() == MotionEvent.ACTION_DOWN) {
                        buttonBluePressed = true;
                        timer_blue = new CounterClass(5000, 1000);
                        timer_blue.setButton(btnBlue);
                        timer_blue.buttonBlue = true;
                        timer_blue.start();
                        Log.v("CustomDebug", "Message: DOWN" + mTouchDownTime);

                    }
                    if (event.getAction() == MotionEvent.ACTION_UP) {
                        buttonBluePressed = false;
                        timer_blue.cancel();
                        if (timer_blue.isFinished() == false) {
                            btnBlue.setText("PRZYTRZYMAJ 5SEKUND");
                        }

                    }
                }
                return false;
            }
        });

        root2 = FirebaseDatabase.getInstance().getReference().child("Devices");

        root.addChildEventListener(new ChildEventListener() {
            @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                append_chat_conversation(dataSnapshot);
            }

            @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {
                append_chat_conversation(dataSnapshot);
            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {

            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });


        if (checkDrawOverlayPermission()) {
            startService(new Intent(this, PowerButtonService.class));
        }


    }
    private String value;

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    private void append_chat_conversation(DataSnapshot dataSnapshot) {

        for(DataSnapshot child: dataSnapshot.getChildren()){
            if(child.getKey().equals("GameStart")) {
                value = (String) child.getValue();
            }
            if(child.getKey().equals("MainTime")) {
                gameTime = convertToMillis((String)child.getValue());
            }
            if(child.getKey().equals("CloseDomination")){
                if(child.getValue().equals("1")) {
                    DatabaseReference root3;
                    root3 = FirebaseDatabase.getInstance().getReference().child("Game");
                    root3.child("Start").child("CloseDomination").setValue("0");
                    Intent startMain = new Intent(Intent.ACTION_MAIN);
                    startMain.addCategory(Intent.CATEGORY_HOME);
                    startMain.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(startMain);
                }
            }
        }

        //value = (String) dataSnapshot.getValue();
        if (Integer.parseInt(value) == 1) {
            if(timerResume==true) {
                timerMain = new CounterClass(millis, 1000);
                timerMain.setTextView(timer);
                timerResume = false;
            }
            timerMain = new CounterClass(gameTime, 1000);
            timerMain.setTextView(timer);
            timerMain.start();
            startGame = true;
            syrena.start();
        }
        if (Integer.parseInt(value) == 0) {

            if(timerMain!=null&&startGame==true) {
                timerMain.onFinish();
                timerMain.cancel();
                startGame = false;
                Intent intent = getIntent();
                finish();
                startActivity(intent);
            }
        }
        if (Integer.parseInt(value) == 2) {
            timerResume = true;
            timerMain.cancel();
        }
    }
    private long convertToMillis(String time) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        sdf.setTimeZone(TimeZone.getTimeZone("UTC"));

        Date date = null;
        long time_millis=0;
        try {
            date = sdf.parse("1970-01-01 " + time);
            time_millis = date.getTime();

        } catch (ParseException e) {
            e.printStackTrace();
        }
        return time_millis;
    }
    @Override
    public void onAttachedToWindow() {
        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN |
                        WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD |
                        WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON |
                        WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED |
                        WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON,
                WindowManager.LayoutParams.FLAG_FULLSCREEN |
                        WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD |
                        WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED |
                        WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON |
                        WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON);
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        if (!hasFocus) {
            Intent closeDialog = new Intent(Intent.ACTION_CLOSE_SYSTEM_DIALOGS);
            sendBroadcast(closeDialog);
        }
    }

    @Override
    protected void onNewIntent(Intent intent) {
        //super.onNewIntent(intent);
        if (Intent.ACTION_MAIN.equals(intent.getAction())) {
            Log.i("MyLauncher", "onNewIntent: HOME Key");

        }
    }

    public class CounterClass extends CountDownTimer {

        /**
         * @param millisInFuture    The number of millis in the future from the call
         *                          to {@link #start()} until the countdown is done and {@link #onFinish()}
         *                          is called.
         * @param countDownInterval The interval along the way to receive
         *                          {@link #onTick(long)} callbacks.
         */

        TextView textView;
        Button button;
        private boolean finish = false;
        private long millisTeamRed, millisTeamBlue;

        public boolean buttonRed = false;
        public boolean buttonBlue = false;

        private long countUp = 0;
        public boolean timerUp = false;
        public CounterClass(long millisInFuture, long countDownInterval) {
            super(millisInFuture, countDownInterval);
        }

        public long getMillis(){
            return millisTeamRed;
        }
        public boolean isFinished(){
            return finish;
        }
        public void setTextView(TextView textView){
            this.textView = textView;
        }
        public TextView getTextView(){
            return textView;
        }
        public void setButton(Button button){
            this.button = button;
        }
        public TextView getButton(){
            return button;
        }
        @Override
        public void onTick(long millisUntilFinished) {
            millis = millisUntilFinished;

            // If timer Up
            if(textView!=null && timerUp == true){

                if(millisRedTeam!=0 && returnRed == false && countUp== 0 && redDominator){
                    countUp = millisRedTeam;
                    returnRed = true;
                }
                if(millisBlueTeam!=0 && returnBlue == false && countUp == 0 && blueDominator){
                    countUp = millisBlueTeam;
                    returnBlue = true;
                }

                if(redDominator) {
                    millisTeamRed = countUp;
                }
                if(blueDominator){
                    millisTeamBlue = countUp;
                }

                countUp = countUp + 1000;
                String hms = String.format("%02d:%02d:%02d", TimeUnit.MILLISECONDS.toHours(countUp),
                        TimeUnit.MILLISECONDS.toMinutes(countUp) - TimeUnit.HOURS.toMinutes(TimeUnit.MILLISECONDS.toHours(countUp)),
                        TimeUnit.MILLISECONDS.toSeconds(countUp) - TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(countUp))
                );
                getTextView().setText(hms);
            }
            // If timer down
            if(textView!=null && timerUp == false) {
                String hms = String.format("%02d:%02d:%02d", TimeUnit.MILLISECONDS.toHours(millis),
                        TimeUnit.MILLISECONDS.toMinutes(millis) - TimeUnit.HOURS.toMinutes(TimeUnit.MILLISECONDS.toHours(millis)),
                        TimeUnit.MILLISECONDS.toSeconds(millis) - TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(millis))
                );
                getTextView().setText(hms);
                toneGen1.startTone(ToneGenerator.TONE_CDMA_PIP,100);
            }
            if(button!=null){
                String seconds = String.valueOf(TimeUnit.MILLISECONDS.toSeconds(millis));
                button.setText(seconds);
            }

    }
        @Override
        public void onFinish() {
            if(textView!=null) {
                textView.setText("COMPLETE");
                syrena.start();
                if(timerTeamBlue!=null){
                    timerTeamBlue.cancel();
                }
                if(timerTeamRed!=null){
                    timerTeamRed.cancel();
                }
                Map<String, Object> start_state = new HashMap<String, Object>();

                start_state.put("RedTime"+String.valueOf(DEVICE_ID), textViewTeamRed.getText().toString());
                start_state.put("BlueTime"+String.valueOf(DEVICE_ID), textViewTeamBlue.getText().toString());
                if(redDominator==true) {
                    start_state.put("Domination"+String.valueOf(DEVICE_ID), "red");
                }
                if(blueDominator==true){
                    start_state.put("Domination"+String.valueOf(DEVICE_ID), "blue");
                }
                root2.child("dev"+String.valueOf(DEVICE_ID)).updateChildren(start_state);

                Map<String, Object> start_state2 = new HashMap<String, Object>();
                start_state2.put("MainTime","00:00:00");
                start_state2.put("GameStart", "0");
                root.child("Start").updateChildren(start_state2);


            }
            if(button!=null && buttonRed == true){
                finish = true;

                button.setText("DOMINACJA");
                btnBlue.setText("PRZYTRZYMAJ 5 SEKUND");
                timerTeamRed = new CounterClass(400000,1000);
                timerTeamRed.setTextView(textViewTeamRed);
                timerTeamRed.timerUp = true;

                if(timerTeamBlue!=null){
                    millisBlueTeam = timerTeamBlue.millisTeamBlue;
                    timerTeamBlue.cancel();
                }
                Map<String, Object> start_state = new HashMap<String, Object>();
                start_state.put("RedTime"+String.valueOf(DEVICE_ID), textViewTeamRed.getText().toString());
                start_state.put("BlueTime"+String.valueOf(DEVICE_ID), textViewTeamBlue.getText().toString());
                start_state.put("Domination"+String.valueOf(DEVICE_ID), "red");
                root2.child("dev"+String.valueOf(DEVICE_ID)).updateChildren(start_state);
                blueDominator = false;
                redDominator = true;
                returnRed = false;
                alarm.start();
                timerTeamRed.start();
            }
            if(button!=null && buttonBlue == true){
                finish = true;
                button.setText("DOMINACJA");
                btnRed.setText("PRZYTRZYMAJ 5 SEKUND");
                timerTeamBlue = new CounterClass(400000000,1000);
                timerTeamBlue.setTextView(textViewTeamBlue);
                timerTeamBlue.timerUp = true;

                if(timerTeamRed!=null){
                    millisRedTeam = timerTeamRed.millisTeamRed;
                    timerTeamRed.cancel();
                }
                Map<String, Object> start_state = new HashMap<String, Object>();

                start_state.put("RedTime"+String.valueOf(DEVICE_ID), textViewTeamRed.getText().toString());
                start_state.put("BlueTime"+String.valueOf(DEVICE_ID), textViewTeamBlue.getText().toString());
                start_state.put("Domination"+String.valueOf(DEVICE_ID), "blue");
                root2.child("dev"+String.valueOf(DEVICE_ID)).updateChildren(start_state);
                redDominator = false;
                blueDominator = true;
                alarm.start();
                returnBlue = false;
                timerTeamBlue.start();
            }

        }
    }
}
