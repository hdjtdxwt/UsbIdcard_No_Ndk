package com.epsit.usbidcard_no_ndk;

import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbDeviceConnection;
import android.hardware.usb.UsbEndpoint;
import android.hardware.usb.UsbManager;
import android.util.Log;
import android.widget.Toast;

import static android.content.Context.USB_SERVICE;

/**
 * 1、第一步，初始化，需要传入ApplicationContext，用来初始化UsbManager
 * 2、第二步获取usb权限（会弹框的那个），会得到UsbDevice对象，需要将这个UsbDevice传入到这个UsbHelper线程里
 * 3、获取UsbDeviceConnection对象，同时获取输入输出流
 * 4、线程开启，进行一直发命令，读返回结果（3个命令发和读这三个命令的结果）
 * 5、提供一个方法，返回身份证信息(暂时不考虑身份证头像)，调用usbHelper的方法就可以获取到读到的身份证的信息（如果没有得到就返回null）
 * 6、提供停止线程的方法，在程序退出时调用
 * <p>
 * <p>
 * 顶层调用的只需要，初始传入ApplicationContext 获取身份证信息的方法，关闭的方法
 * <p>
 * Created by Administrator on 2018/3/1/001.
 */

public class UsbHelper implements UsbThread.OnThreadIdcardReadListener {
    private static UsbHelper instance;
    String TAG = "MainActivity";
    UsbManager usbManager;
    private static final String ACTION_USB_PERMISSION = "com.android.example.USB_PERMISSION";
    Context context;
    UsbThread thread;

    private UsbHelper() {
    }
    public static UsbHelper getInstance( ) {
        if (instance == null) {
            synchronized (UsbHelper.class) {
                if (instance == null) {
                    instance = new UsbHelper();
                }
            }
        }
        return instance;
    }

    /**
     * 1、第一步，初始化，需要传入ApplicationContext，用来初始化UsbManager
     */
    public void initContext(Context context) {
        this.context = context;
        if(context==null){ return; }
        IntentFilter filter = new IntentFilter(ACTION_USB_PERMISSION);
        context.registerReceiver(mUsbPermissionActionReceiver, filter);
        usbManager = (UsbManager) context.getSystemService(USB_SERVICE);

        //申请usb权限
        PendingIntent mPermissionIntent = PendingIntent.getBroadcast(context, 0, new Intent(ACTION_USB_PERMISSION), 0);
        for (final UsbDevice usbDevice : usbManager.getDeviceList().values()) {
            if(usbManager.hasPermission(usbDevice)){
                afterGetUsbPermission(usbDevice);
            }else{
                usbManager.requestPermission(usbDevice, mPermissionIntent);
            }
        }
    }
    //usb权限点击确定或者不给予权限的回调
    private final BroadcastReceiver mUsbPermissionActionReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (ACTION_USB_PERMISSION.equals(action)) {
                synchronized (this) {
                    UsbDevice usbDevice = (UsbDevice)intent.getParcelableExtra(UsbManager.EXTRA_DEVICE);
                    if (intent.getBooleanExtra(UsbManager.EXTRA_PERMISSION_GRANTED, false)) {
                        //user choose YES for your previously popup window asking for grant perssion for this usb device
                        if(null != usbDevice){
                            afterGetUsbPermission(usbDevice);
                        }
                    }
                    else {

                    }
                }
            }
        }
    };
    private void afterGetUsbPermission(UsbDevice usbDevice) {
        Log.e("empty","---有权限了");
        if(thread==null){
            thread = new UsbThread(usbManager,usbDevice);
            thread.setListener(this);
            thread.start();
        }
    }

    public void stopThread(){
        if(thread!=null) {
            thread.stopAndClose();
        }
        if(context!=null){
            context.unregisterReceiver(mUsbPermissionActionReceiver);
        }
        if(listener!=null){
            listener=null;
        }
    }

    public void onDestory(){
        stopThread();
    }


    @Override
    public void onReaded(IdCardInfo idCardInfo) { //读取到了新的身份证信息
        if(idCardInfo!=null){ //有新的人的身份证信息
            Log.e(TAG,"新的身份证信息-->");
            if(listener!=null){
                listener.onReaded(idCardInfo);
            }
        } else { //没读取到身份证信息
            if(listener!=null){
                listener.onReaded(null);
            }
        }
    }
    private onIdcardReadedListener listener;

    public void setListener(onIdcardReadedListener listener) {
        this.listener = listener;
    }

    public static interface onIdcardReadedListener{
        void onReaded(IdCardInfo idCardInfo);
        //usb权限没有获取到
        void onPermissionNotGranted();
    }
}
