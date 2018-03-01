package com.epsit.usbidcard_no_ndk;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

/**
 * Created by Administrator on 2018/3/1/001.
 */

public class ReadActivity extends AppCompatActivity implements UsbHelper.onIdcardReadedListener {
    UsbHelper usbHelper;
    String TAG = "ReadActivity";
    String idcardNumber;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        usbHelper = UsbHelper.getInstance();

        //第一步设置回调方法
        usbHelper.setListener(this);
        //第二步初始化，会有usb权限弹框的
        usbHelper.initContext(getApplicationContext());

    }

    @Override
    public void onReaded(IdCardInfo idCardInfo) {
        if(idCardInfo!=null){
            Log.e(TAG,idCardInfo.toString());
        }
    }

    @Override
    public void onPermissionNotGranted() {
        Log.e(TAG,"usb权限没有给予");
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if(usbHelper!=null){
            usbHelper.onDestory();
        }
    }
}
