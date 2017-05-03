package ua.com.natanao.rollncodenatanao;

import android.app.ActivityManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import java.sql.Date;
import java.text.SimpleDateFormat;

import butterknife.BindView;
import butterknife.ButterKnife;

public class MainActivity extends AppCompatActivity {

    @BindView(R.id.tvTime) TextView tvTime;
    @BindView(R.id.tvCounter) TextView tvCounter;
    @BindView(R.id.tvServerStatus) TextView tvServerStatus;

    BroadcastReceiver receiver;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);

        SharedPreferences shared = getSharedPreferences("my_pref", MODE_PRIVATE);

        if (shared.contains("TIME")) {
            Date date =  new Date(shared.getLong("TIME", 0));
            tvTime.setText(new SimpleDateFormat("HH:mm:ss").format(date));
        } else {
            tvTime.setText(getResources().getString(R.string.first_run));
        }
        if (shared.contains("COUNTER")) {
            tvCounter.setText(String.valueOf(shared.getLong("COUNTER", 0)));
        } else {
            tvCounter.setText(getResources().getString(R.string.first_run));
        }

        receiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                if (intent.hasExtra("rollncodenatanao.COUNTER")) {
                    Long counter = intent.getLongExtra("rollncodenatanao.COUNTER", -1L);
                    tvCounter.setText(String.valueOf(counter));
                }
                if (intent.hasExtra("rollncodenatanao.TIME")) {
                    Long time = intent.getLongExtra("rollncodenatanao.TIME", -1L);
                    tvTime.setText(new SimpleDateFormat("HH:mm:ss").format(new Date(time)));
                }
            }
        };
    }
    @Override
    protected void onStart() {
        super.onStart();
        LocalBroadcastManager.getInstance(this).registerReceiver((receiver), new IntentFilter("rollncodenatanao.RECEIVER"));
        if (isMyServiceRunning(MyService.class)) {
            tvServerStatus.setText(getResources().getString(R.string.server_is_running));
            tvServerStatus.setBackgroundColor(Color.GREEN);
        } else {
            tvServerStatus.setText(getResources().getString(R.string.server_is_stopped));
            tvServerStatus.setBackgroundColor(Color.RED);
        }
    }
    @Override
    protected void onStop() {
        super.onStop();
        LocalBroadcastManager.getInstance(this).unregisterReceiver(receiver);

    }
    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (isFinishing()) {
            StopService(null);
        }
    }

    public void StartService(View view) {
        tvServerStatus.setText(getResources().getString(R.string.server_is_running));
        tvServerStatus.setBackgroundColor(Color.GREEN);
        startService(new Intent(this, MyService.class));
    }

    public void StopService(View view) {
        tvServerStatus.setText(getResources().getString(R.string.server_is_stopped));
        tvServerStatus.setBackgroundColor(Color.RED);
        stopService(new Intent(this, MyService.class));
    }

    private boolean isMyServiceRunning(Class<?> serviceClass) {
        ActivityManager manager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (serviceClass.getName().equals(service.service.getClassName())) {
                return true;
            }
        }
        return false;
    }
}
