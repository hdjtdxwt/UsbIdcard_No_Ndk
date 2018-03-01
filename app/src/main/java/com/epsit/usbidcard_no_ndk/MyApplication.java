package com.epsit.usbidcard_no_ndk;

import android.app.Application;

import com.tencent.bugly.crashreport.CrashReport;

/**
 * Created by Administrator on 2018/2/28/028.
 */

public class MyApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        CrashReport.initCrashReport(getApplicationContext(), "7e979d91a7", false);
    }
}
