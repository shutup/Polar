package com.shutup.polar;

import android.app.IntentService;
import android.content.Intent;
import android.util.Log;

import java.util.ArrayList;

import io.realm.Realm;
import io.realm.RealmResults;


public class MyIntentService extends IntentService {
    public static final String ACTION_SCREEN_OFF = "com.shutup.polar.action.SCREEN_OFF";
    public static final String ACTION_SCREEN_ON = "com.shutup.polar.action.SCREEN_ON";

    public MyIntentService() {
        super("MyIntentService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        if (intent != null) {
            final String action = intent.getAction();
            if (ACTION_SCREEN_OFF.equals(action)) {
                disableApps();
            } else if (ACTION_SCREEN_ON.equals(action)) {
                disableApps();
            }
        }
    }

    private void disableApps() {
        RealmResults<PInfo> results = Realm.getDefaultInstance().where(PInfo.class).equalTo("isEnable",false).findAll();
        ArrayList<String> pkgs = new ArrayList<>();
        for (PInfo p : results) {
            if (p.isEnable()) {

            }else {
                pkgs.add("pm disable "+p.getPkgName());
                if (BuildConfig.DEBUG) Log.d("MyIntentService", "intent service "+p.getPkgName());
            }
        }
        CmdUtils.runCmdArray( pkgs);
    }

}
