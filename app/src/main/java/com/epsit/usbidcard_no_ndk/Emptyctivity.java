package com.epsit.usbidcard_no_ndk;

import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

public class Emptyctivity extends AppCompatActivity implements View.OnClickListener {
    UsbManager usbManager;
    private static final String ACTION_USB_PERMISSION = "com.android.example.USB_PERMISSION";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_emptyctivity);
        findViewById(R.id.button).setOnClickListener(this );
        IntentFilter filter = new IntentFilter(ACTION_USB_PERMISSION);
        registerReceiver(mUsbPermissionActionReceiver, filter);

        usbManager = (UsbManager) getSystemService(USB_SERVICE);

        PendingIntent mPermissionIntent = PendingIntent.getBroadcast(this, 0, new Intent(ACTION_USB_PERMISSION), 0);
        for (final UsbDevice usbDevice : usbManager.getDeviceList().values()) {
            if(usbManager.hasPermission(usbDevice)){
                afterGetUsbPermission(usbDevice);
            }else{
                usbManager.requestPermission(usbDevice, mPermissionIntent);
            }
        }
    }
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
                        //user choose NO for your previously popup window asking for grant perssion for this usb device
                        Toast.makeText(context, String.valueOf("Permission denied for device" + usbDevice), Toast.LENGTH_LONG).show();
                    }
                }
            }
        }
    };

    @Override
    public void onClick(View v) {
        PendingIntent mPermissionIntent = PendingIntent.getBroadcast(this, 0, new Intent(ACTION_USB_PERMISSION), 0);
        //here do emulation to ask all connected usb device for permission
        for (final UsbDevice usbDevice : usbManager.getDeviceList().values()) {
            if(usbManager.hasPermission(usbDevice)){
                afterGetUsbPermission(usbDevice);
            }else{
                usbManager.requestPermission(usbDevice, mPermissionIntent);
            }
        }
    }

    private void afterGetUsbPermission(UsbDevice usbDevice) {
        Log.e("empty","---有权限了");
        Intent intent = new Intent(this,MainActivity.class);
        intent.putExtra("device",usbDevice);
        startActivity(intent);
    }
    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(mUsbPermissionActionReceiver);
    }
}
