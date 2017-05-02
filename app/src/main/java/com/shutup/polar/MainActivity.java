package com.shutup.polar;

import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnClick;
import io.realm.Realm;

public class MainActivity extends AppCompatActivity implements Constants {

    @InjectView(R.id.recyclerView)
    RecyclerView mRecyclerView;
    @InjectView(R.id.swipeRefresh)
    SwipeRefreshLayout mSwipeRefresh;
    @InjectView(R.id.addQuestionFAB)
    FloatingActionButton mAddQuestionFAB;
    private PackageManager pm;
    private ArrayList<PInfo> mPInfos;
    private PkgListAdapter mPkgListAdapter;
    private int current_status = ACTIVITY_NORMAL;
    private HandlerThread mHandlerThread;
    private Handler mHandler;
    private ArrayList<PInfo> disableAppLists = new ArrayList<>();
    private ArrayList<PInfo> enableApplists = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.inject(this);
        checkRootGiven();
        initRecyclerView();
        initRefresh();
        initHandler();
        initDaemonServer();
    }

    @Override
    protected void onDestroy() {
        saveStatusToLocal();
        initDaemonServer();
        super.onDestroy();
    }

    private void checkRootGiven() {
        if (CmdUtils.isRootGiven()) {

        } else {
            Toast.makeText(this, "本App需要root权限才可正常工作！", Toast.LENGTH_SHORT).show();
        }
    }

    private void initRecyclerView() {
        mPInfos = new ArrayList<>();
        mPkgListAdapter = new PkgListAdapter(mPInfos, this);
        mRecyclerView.addOnItemTouchListener(new RecyclerTouchListener(getApplicationContext(), mRecyclerView, new RecyclerTouchListener.ClickListener() {
            @Override
            public void onClick(View view, int position) {
                PInfo pInfo = mPInfos.get(position);
                if (current_status == ACTIVITY_NORMAL) {
                    //enable & start app
                    if (pInfo.isEnable()) {
                        lanuchApp(pInfo.getPkgName());
                    } else {
                        Message message = mHandler.obtainMessage(MSG_RUN_ENABLE_CMD, pInfo.getPkgName());
                        message.arg1 = position;
                        mHandler.sendMessage(message);
                    }

                } else if (current_status == ACTIVITY_EDIT) {
                    //select & disable app

                    pInfo.setEnable(!pInfo.isEnable());
                    if (pInfo.isEnable()) {
                        disableAppLists.remove(pInfo);
                        enableApplists.add(pInfo);
                    } else {
                        enableApplists.remove(pInfo);
                        disableAppLists.add(pInfo);
                    }
                    mPkgListAdapter.notifyItemChanged(position);
                }
            }

            @Override
            public void onLongClick(View view, int position) {

            }
        }));
        mRecyclerView.setLayoutManager(new GridLayoutManager(getApplicationContext(), getColumnNum()));
//        mRecyclerView.setLayoutManager(new LinearLayoutManager(mRecyclerView.getContext()));
        mRecyclerView.setItemAnimator(new DefaultItemAnimator());
        mRecyclerView.setAdapter(mPkgListAdapter);
    }

    private void initRefresh() {
        mSwipeRefresh.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                mHandler.sendEmptyMessage(MSG_REFRESH_UI);
                mSwipeRefresh.setRefreshing(false);
            }
        });
    }

    private void initHandler() {
        mHandlerThread = new HandlerThread("runner");
        mHandlerThread.start();
        mHandler = new Handler(mHandlerThread.getLooper(), new Handler.Callback() {
            @Override
            public boolean handleMessage(Message message) {
                if (message.what == MSG_LOAD_LOCAL) {
                    initLocalData();
                } else if (message.what == MSG_RUN_CMD) {
                    disableAllSelectedApp();
                } else if (message.what == MSG_RUN_ENABLE_CMD) {
                    enableSelectedAppAndRun((String) message.obj, message.arg1);
                } else if (message.what == MSG_REFRESH_UI) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            refreshUI();
                        }
                    });
                }
                return false;
            }
        });
        mHandler.sendEmptyMessage(MSG_LOAD_LOCAL);
    }

    private void initDaemonServer() {
        startService(new Intent(this, MyDaemonService.class));
    }

    private void lanuchApp(String pkgName) {
        Intent launchIntent = getPackageManager().getLaunchIntentForPackage(pkgName);
        if (launchIntent != null) {
            startActivity(launchIntent);//null pointer check in case package name was not found
        }
    }

    private void initLocalData() {
        pm = getPackageManager();
        List<PackageInfo> packageInfos = pm.getInstalledPackages(0);
        final List<PInfo> pInfos = new ArrayList<>();
        String self = getPackageName();
        for (PackageInfo p : packageInfos) {
            if (!isSystemPackage(p)) {
                String appName = p.applicationInfo.loadLabel(getPackageManager()).toString();
                PInfo pinfo = new PInfo(appName, p.packageName);
                pinfo.setEnable(p.applicationInfo.enabled);
                if (p.packageName.contentEquals(self)) {

                } else {
                    pInfos.add(pinfo);
                }
            }
        }
        mPInfos.clear();
        mPInfos.addAll(pInfos);

        saveStatusToLocal();
        mPkgListAdapter.setPInfos(mPInfos);
        mHandler.sendEmptyMessage(MSG_REFRESH_UI);
    }

    private void saveStatusToLocal() {
        Realm.getDefaultInstance().executeTransaction(new Realm.Transaction() {
            @Override
            public void execute(Realm realm) {
                realm.deleteAll();
                for (int i = 0; i < mPInfos.size(); i++) {
                    mPInfos.get(i).setId(i);
                }
                realm.insertOrUpdate(mPInfos);
            }
        });
    }

    private void refreshUI() {
        Collections.sort(mPInfos, new Comparator<PInfo>() {
            @Override
            public int compare(PInfo p1, PInfo p2) {
                int b1 = p1.isEnable() ? 1 : 0;
                int b2 = p2.isEnable() ? 1 : 0;
                return b1 - b2;
            }
        });
        mPkgListAdapter.setPInfos(mPInfos);
        mPkgListAdapter.notifyDataSetChanged();
    }

    private boolean isSystemPackage(PackageInfo pkgInfo) {
        return ((pkgInfo.applicationInfo.flags & ApplicationInfo.FLAG_SYSTEM) != 0) ? true
                : false;
    }

    private int getColumnNum() {
        WindowManager wm = (WindowManager) this
                .getSystemService(Context.WINDOW_SERVICE);
        int width = wm.getDefaultDisplay().getWidth();

        int item_width = getResources().getDimensionPixelSize(R.dimen.item_width);

        return width / item_width > 0 ? width / item_width : 1;
    }

    @OnClick(R.id.addQuestionFAB)
    public void onClick() {
        if (current_status == ACTIVITY_NORMAL) {
            current_status = ACTIVITY_EDIT;
            mAddQuestionFAB.setImageResource(R.drawable.ic_ok);
            mPkgListAdapter.setCurrent_status(current_status);
            mPkgListAdapter.notifyDataSetChanged();
        } else {
            current_status = ACTIVITY_NORMAL;
            //disable all selected
            mHandler.sendEmptyMessage(MSG_RUN_CMD);
            mAddQuestionFAB.setImageResource(R.drawable.ic_create_white);
            mPkgListAdapter.setCurrent_status(current_status);
            mPkgListAdapter.notifyDataSetChanged();
        }
    }

    private void disableAllSelectedApp() {
        ArrayList<String> pkgs = new ArrayList<>();
        for (PInfo p : disableAppLists) {
            pkgs.add("pm disable " + p.getPkgName());
        }
        for (PInfo p : enableApplists) {
            pkgs.add("pm enable " + p.getPkgName());
            if (BuildConfig.DEBUG) Log.d("MainActivity", p.getPkgName());
        }
        disableApps(pkgs);
        saveStatusToLocal();
        mHandler.sendEmptyMessage(MSG_REFRESH_UI);
        disableAppLists.clear();
        enableApplists.clear();
    }

    private void enableSelectedAppAndRun(String pkgName, int index) {
        enableApp(pkgName);
        mHandler.sendEmptyMessage(MSG_REFRESH_UI);
        lanuchApp(pkgName);
    }

    private void enableApp(String pkgName) {
        CmdUtils.runCmd("pm enable " + pkgName);
    }

    private void disableApps(ArrayList<String> pkgNames) {
        CmdUtils.runCmdArray(pkgNames);
    }
}
