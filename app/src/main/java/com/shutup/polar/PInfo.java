package com.shutup.polar;

import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

/**
 * Created by shutup on 2017/4/29.
 */

public class PInfo extends RealmObject{
    @PrimaryKey
    private int id;
    private String appName;
    private String pkgName;
    private boolean isEnable;
    private int use_counts;

    public PInfo() {
    }

    public PInfo(String appName, String pkgName, boolean isEnable, int use_counts) {
        this.appName = appName;
        this.pkgName = pkgName;
        this.isEnable = isEnable;
        this.use_counts = use_counts;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public PInfo(String appName, String pkgName) {
        this(appName,pkgName,true,0);
    }

    public String getAppName() {
        return appName;
    }

    public void setAppName(String appName) {
        this.appName = appName;
    }

    public String getPkgName() {
        return pkgName;
    }

    public void setPkgName(String pkgName) {
        this.pkgName = pkgName;
    }

    public boolean isEnable() {
        return isEnable;
    }

    public void setEnable(boolean enable) {
        isEnable = enable;
    }
    
    public int getUse_counts() {
        return use_counts;
    }

    public void setUse_counts(int use_counts) {
        this.use_counts = use_counts;
    }
}
