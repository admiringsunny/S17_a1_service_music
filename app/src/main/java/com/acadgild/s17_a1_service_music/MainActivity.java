package com.acadgild.s17_a1_service_music;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.support.v4.app.NotificationCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {

    private BoundService serviceReference;
    private static final String TAG = "bound";
    public static final int NOTIFICATION_ID = 100;
    public static final int REQUEST_CODE = 101;
    private boolean isBound;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // start/intent service
        Log.i(TAG, "bounding service");
        Intent intent = new Intent(this, BoundService.class);
        startService(intent);
        sendNotification();

//        on click play icon -> play music
        Button startButton = (Button) findViewById(R.id.btn_play);
        startButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isBound) {
                    serviceReference.startPlay();
                }
            }
        });

//        on click pause icon -> pause music
        Button pauseButton = (Button) findViewById(R.id.btn_pause);
        pauseButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isBound) {
                    serviceReference.pausePlay();
                }
            }
        });
    }

//    activity starting -> onStart() -> bind to service
    @Override
    protected void onStart() {
        super.onStart();

        Log.i(TAG, "MainActivity - onStart - binding...");
        // bind to service
        doBindToService();
    }

//    bind to the service
    private void doBindToService() {
        Toast.makeText(this, "binding....", Toast.LENGTH_SHORT).show();
        if (!isBound) {
            Intent boundIntent = new Intent(this, BoundService.class);
            isBound = bindService(boundIntent, myConnection, Context.BIND_AUTO_CREATE);
        }
    }

//    activity stopping -> onStop() -> unbind from the service
    @Override
    protected void onStop() {
        super.onStop();
        Log.i(TAG, "onStop -> Unbinding From Service");
//        unbind from the service
        doUnbindFromService();
    }

//        unbind from the service
    private void doUnbindFromService() {
        Toast.makeText(this, "unbinding...", Toast.LENGTH_SHORT).show();
        unbindService(myConnection);
        isBound = false;
    }

    @Override
    public void onBackPressed() {
        // the activity pauses instead of finish
        moveTaskToBack(true);
    }

//    the activity is being destroyed -> onDestroy() -> stop service
    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.i(TAG, "destroying service");
        if (isFinishing()) {
            Log.i(TAG, "activity is finishing");

//            stop service as activity being destroyed and we won't use it any more
            Intent stopServiceIntent = new Intent(this, BoundService.class);
            stopService(stopServiceIntent);
        }
    }

//    interface for monitoring the state of the service
    private ServiceConnection myConnection = new ServiceConnection() {

        // when the connection with the service has been established
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            Log.i(TAG, "bound service connected");
            serviceReference = ((BoundService.MyLocalBinder) service).getService();
            isBound = true;
        }

        // when crash happen
        @Override
        public void onServiceDisconnected(ComponentName name) {
            Log.i(TAG, "bound service disconnected");
            serviceReference = null;
            isBound = false;
        }
    };

//      sends an ongoing notification notifying that service is running.
//      it's only dismissed when the service is destroyed
    private void sendNotification() {
//        we use the compatibility library
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this);
        builder.setSmallIcon(R.mipmap.ic_launcher)
                .setContentTitle("Service Running")
                .setTicker("Music Playing")
                .setWhen(System.currentTimeMillis())
                .setOngoing(true);

//        notify
        Intent startIntent = new Intent(this, MainActivity.class);
        PendingIntent contentIntent = PendingIntent.getActivity(this,
                REQUEST_CODE, startIntent, 0);
        builder.setContentIntent(contentIntent);

        Notification notification = builder.build();
        NotificationManager notificationManager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.notify(NOTIFICATION_ID, notification);
    }


}
