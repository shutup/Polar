package com.shutup.polar;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import static com.shutup.polar.MyIntentService.ACTION_SCREEN_OFF;
import static com.shutup.polar.MyIntentService.ACTION_SCREEN_ON;

public class MyReceiver extends BroadcastReceiver {
    public MyReceiver() {
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent.getAction().equals(Intent.ACTION_SCREEN_OFF)) {
            Intent intent1 = new Intent(context, MyIntentService.class);
            intent1.setAction(ACTION_SCREEN_OFF);
            context.startService(intent1);
            if (BuildConfig.DEBUG) Log.d("MyReceiver", "screen off");
        }else if (intent.getAction().equals(Intent.ACTION_SCREEN_ON)) {
            Intent intent1 = new Intent(context, MyIntentService.class);
            intent1.setAction(ACTION_SCREEN_ON);
            context.startService(intent1);
            if (BuildConfig.DEBUG) Log.d("MyReceiver", "screen on");
        }
    }
}
