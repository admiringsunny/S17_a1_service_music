package com.acadgild.s17_a1_service_music;

import android.app.NotificationManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Binder;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.util.Log;
import android.widget.Toast;

public class BoundService extends Service {

    public static final String TAG = "bound";
    public static final int NOTIFICATION_ID = 100;
    private final IBinder myBinder = new MyLocalBinder();
    private Thread backgroundThread;
    private MediaPlayer player;


//    client is binding to the service with bindService()
    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return  myBinder;
    }

//    Called when the service is being created.
    @Override
    public void onCreate() {
        super.onCreate();

        // do the work in a separate thread so main thread is not blocked
        backgroundThread = new Thread(new Runnable() {
            @Override
            public void run() {
                Log.i(TAG, "backgroundThread running -> playMusic()");
                playMusic();
            }
        });
        backgroundThread.start();
    }

    //    called when the service starts from a call to startService()
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return START_STICKY;
    }

//    Called when The service is no longer used and is being destroyed
    @Override
    public void onDestroy() {
//      release thread and player
        Log.i(TAG, "destroying service");
        Toast.makeText(this, "destroying service", Toast.LENGTH_SHORT).show();
        player.release();
        player = null;

        Thread dummy = backgroundThread;
        backgroundThread = null;
        dummy.interrupt();

        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        Log.i(TAG, "canceling notification");
        notificationManager.cancel(NOTIFICATION_ID);
    }

//    play the sound clip
    private void playMusic() {
        player = new MediaPlayer();
        if (player != null) {
            player.release();
        }

        player = MediaPlayer.create(this, R.raw.saathiya);
        player.setLooping(true);
    }

//    start play music
    public void startPlay() {
        if (!player.isPlaying()) {
            player.start();
        }
    }

//    stop play music
    public void pausePlay() {
        if (player.isPlaying()) {
            player.pause();
        }
    }

//    the class used for the client Binder
    public class MyLocalBinder extends Binder {
        BoundService getService() {
            return BoundService.this;
        }
    }

}
