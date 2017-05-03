package ua.com.natanao.rollncodenatanao;

import android.app.Service;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.IBinder;
import android.support.v4.content.LocalBroadcastManager;

import java.util.Date;
import java.util.concurrent.TimeUnit;

public class MyService extends Service {

    LocalBroadcastManager broadcaster;
    CounterRunnable counterRunnable;

    public MyService() { }

    @Override
    public void onCreate() {
        super.onCreate();
        broadcaster = LocalBroadcastManager.getInstance(this);
    }
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        Date date = new Date();
        SharedPreferences shared = getSharedPreferences("my_pref", MODE_PRIVATE);
        shared.edit().putLong("TIME", date.getTime()).apply();

        Intent i = new Intent("rollncodenatanao.RECEIVER");
        i.putExtra("rollncodenatanao.TIME", date.getTime());
        broadcaster.sendBroadcast(i);

        if (counterRunnable == null) {
            counterRunnable = new CounterRunnable();
        }
        return super.onStartCommand(intent, flags, startId);
    }
    @Override
    public void onDestroy() {
        super.onDestroy();
        counterRunnable.shutdown();
    }

    @Override
    public IBinder onBind(Intent intent) {
        throw new UnsupportedOperationException("Not yet implemented");
    }

    public void sendCount(Long counter) {
        Intent intent = new Intent("rollncodenatanao.RECEIVER");
        if(counter != null)
            intent.putExtra("rollncodenatanao.COUNTER", counter);
        broadcaster.sendBroadcast(intent);
    }



    private class CounterRunnable implements Runnable {
        Thread thread;
        Long counter;
        private volatile boolean shutdown;

        CounterRunnable() {
            thread = new Thread(this, "Counter thread");
            thread.start();
        }

        void shutdown() {
            shutdown = true;
            SharedPreferences shared = getSharedPreferences("my_pref", MODE_PRIVATE);
            shared.edit().putLong("COUNTER", counter).apply();
        }

        public void run() {
            SharedPreferences shared = getSharedPreferences("my_pref", MODE_PRIVATE);
            counter = shared.getLong("COUNTER", 0);

            while (!shutdown) {
                counter++;
                for (int i = 0; !shutdown && i < 5; i++) {
                    sendCount(counter);
                    try {
                        TimeUnit.SECONDS.sleep(1);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }
}
