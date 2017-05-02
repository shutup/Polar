package com.shutup.polar;

import android.app.Application;

import io.realm.Realm;

/**
 * Created by shutup on 2017/4/30.
 */

public class PolarApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        Realm.init(getApplicationContext());
    }
}
