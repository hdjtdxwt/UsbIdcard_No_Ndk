package com.epsit.usbidcard_no_ndk;

import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbDeviceConnection;
import android.hardware.usb.UsbEndpoint;
import android.hardware.usb.UsbInterface;
import android.hardware.usb.UsbManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import java.io.UnsupportedEncodingException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {
    String TAG = "MainActivity";
    UsbDevice usbDevice;
    byte[] cmd_SAM = {(byte) 0xAA, (byte) 0xAA, (byte) 0xAA, (byte) 0x96, 0x69, 0x00, 0x03, 0x12, (byte) 0xFF, (byte) 0xEE  };//复位
    byte[] cmd_find  = {(byte) 0xAA, (byte) 0xAA, (byte) 0xAA, (byte) 0x96, 0x69, 0x00, 0x03, 0x20, 0x01, 0x22  };
    byte[] cmd_selt  = {(byte) 0xAA, (byte) 0xAA, (byte) 0xAA, (byte) 0x96, 0x69, 0x00, 0x03, 0x20, 0x02, 0x21  };
    byte[] cmd_read  = {(byte) 0xAA, (byte) 0xAA, (byte) 0xAA, (byte) 0x96, 0x69, 0x00, 0x03, 0x30, 0x01, 0x32 };
    byte[] cmd_sleep  = {(byte) 0xAA, (byte) 0xAA, (byte) 0xAA, (byte) 0x96, 0x69, 0x00, 0x02, 0x00, 0x02};
    byte[] cmd_weak  = {(byte) 0xAA, (byte) 0xAA, (byte) 0xAA, (byte) 0x96, 0x69, 0x00, 0x02, 0x01, 0x03 };
    byte[] recData = new byte[1500];

    UsbManager usbManager;
    private static final String ACTION_USB_PERMISSION = "com.android.example.USB_PERMISSION";

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
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        findViewById(R.id.test).setOnClickListener(this);
        usbManager = (UsbManager) getSystemService(Context.USB_SERVICE);

        IntentFilter filter = new IntentFilter(ACTION_USB_PERMISSION);
        registerReceiver(mUsbPermissionActionReceiver, filter);

        PendingIntent mPermissionIntent = PendingIntent.getBroadcast(this, 0, new Intent(ACTION_USB_PERMISSION), 0);
        //here do emulation to ask all connected usb device for permission
        for (final UsbDevice usbDevice : usbManager.getDeviceList().values()) {
            if(usbManager.hasPermission(usbDevice)){
                afterGetUsbPermission(usbDevice);
            }else{
                usbManager.requestPermission(usbDevice, mPermissionIntent);
            }
        }
        readWrite();
    }
    public void initDevice(){
        HashMap<String, UsbDevice> deviceList = usbManager.getDeviceList();
        Log.e(TAG, "get device list  = " + deviceList.size());
        Toast.makeText(this,
                "get device list  = " + deviceList.size(), Toast.LENGTH_SHORT).show();
        Iterator<UsbDevice> deviceIterator = deviceList.values().iterator();
        while (deviceIterator.hasNext()) {
            UsbDevice device = deviceIterator.next();
            Log.e(TAG, "device name = " + device.getDeviceName());
            usbDevice = device;
            break;
        }

        if(deviceList.size()>0 && usbDevice!=null){
            readWrite();
        }

    }
    String nations[] = { "解码错",		// 00
            "汉",			// 01
            "蒙古",			// 02
            "回",			// 03
            "藏",			// 04
            "维吾尔",		// 05
            "苗",			// 06
            "彝",			// 07
            "壮",			// 08
            "布依",			// 09
            "朝鲜",			// 10
            "满",			// 11
            "侗",			// 12
            "瑶",			// 13
            "白",			// 14
            "土家",			// 15
            "哈尼",			// 16
            "哈萨克",		// 17
            "傣",			// 18
            "黎",			// 19
            "傈僳",			// 20
            "佤",			// 21
            "畲",			// 22
            "高山",			// 23
            "拉祜",			// 24
            "水",			// 25
            "东乡",			// 26
            "纳西",			// 27
            "景颇",			// 28
            "柯尔克孜",		// 29
            "土",			// 30
            "达斡尔",		// 31
            "仫佬",			// 32
            "羌",			// 33
            "布朗",			// 34
            "撒拉",			// 35
            "毛南",			// 36
            "仡佬",			// 37
            "锡伯",			// 38
            "阿昌",			// 39
            "普米",			// 40
            "塔吉克",		// 41
            "怒",			// 42
            "乌孜别克",		// 43
            "俄罗斯",		// 44
            "鄂温克",		// 45
            "德昴",			// 46
            "保安",			// 47
            "裕固",			// 48
            "京",			// 49
            "塔塔尔",		// 50
            "独龙",			// 51
            "鄂伦春",		// 52
            "赫哲",			// 53
            "门巴",			// 54
            "珞巴",			// 55
            "基诺",			// 56
            "编码错",		// 57
            "其他",			// 97
            "外国血统"		// 98
    };
    UsbDeviceConnection connection;
    UsbEndpoint inEndpoint;
    UsbEndpoint outEndpoint;
    boolean shouldStop;
    boolean threadStop;
    int count ;
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
    Thread thread;

    /**
     * 获取usb权限之后调用这个方法
     */
    public void readWrite(){
        UsbInterface usbInterface = usbDevice.getInterface(0);
        //USBEndpoint为读写数据所需的节点
        inEndpoint = usbInterface.getEndpoint(0);  //读数据节点
        outEndpoint = usbInterface.getEndpoint(1); //写数据节点
        connection = usbManager.openDevice(usbDevice);
        connection.claimInterface(usbInterface, true);
        if(thread==null){
            thread = new ReadThread();
            thread.start();
        }

        //0复位
        /*connection.bulkTransfer(outEndpoint, cmd_SAM, cmd_SAM.length, 3000);
        samResult= new byte[15];
        ret = connection.bulkTransfer(inEndpoint, samResult, samResult.length, 3000);
        Log.e(TAG,"samResult="+DataUtils.bytesToHexString(samResult));*/

        /**
         * 身份证信息(文字+照片)结构：
         AA AA AA 96 69 05 08 00 00 90 01 00 04 00 +（ 256 字节文字信息 ） +（ 1024 字节
         照片信息） +（ 1 字节 CRC）

         身份证信息(文字+照片+指纹)结构：
         AA AA AA 96 69 09 0A 00 00 90 01 00 04 00 04 00 +（ 256 字节文字信息） +
         （ 1024 字节图片信息） +（ 1024 或 512 或 0 字节指纹信息） +1 字节校验位 指
         纹数据的具体大小由第十五和第十六字节判断 (04 00)=4*16*16=1024
         (02 00)=2*16*16=512
         */
    }

    /**
     * 线程中发送寻卡 选卡 读卡的命令
     */
    public void threadRead(){
        count++;
        /*//0复位
        connection.bulkTransfer(outEndpoint, cmd_SAM, cmd_SAM.length, 3000);
        byte[]samResult= new byte[15];
        int ret = connection.bulkTransfer(inEndpoint, samResult, samResult.length, 3000);
        Log.e(TAG,"samResult="+DataUtils.bytesToHexString(samResult));*/

        //1、寻卡，发送命令
        int out = connection.bulkTransfer(outEndpoint, cmd_find, cmd_find.length, 3000);
        byte[]findResult= new byte[15];
        int ret = connection.bulkTransfer(inEndpoint, findResult, findResult.length, 3000);
        Log.e("ret", "ret:"+ret);
        Log.e(TAG,"findResult="+DataUtils.bytesToHexString(findResult));
        Log.e(TAG,"findResult[7]="+DataUtils.bytesToHexString(new byte[]{findResult[7]}));
        Log.e(TAG,"findResult[8]="+DataUtils.bytesToHexString(new byte[]{findResult[8]}));
        Log.e(TAG,"findResult[9]="+DataUtils.bytesToHexString(new byte[]{findResult[9]}));

        if (findResult[7] == 0x00 && findResult[8] == 0x00 && findResult[9] == (byte)0x9f) {  //寻卡命令执行成功了 findResult[9]=0x80表示寻卡失败
            Log.e(TAG,"寻卡命令执行成功了");
        }else{
            Log.e(TAG,"寻卡命令执行失败");
        }

        byte[]selectResult = new byte[19];
        //2、选卡,发送选卡命令
        out = connection.bulkTransfer(outEndpoint, cmd_selt, cmd_selt.length, 3000);
        ret = connection.bulkTransfer(inEndpoint, selectResult, selectResult.length, 3000);
       // Log.e(TAG,"selectResult="+DataUtils.bytesToHexString(selectResult));
        Log.e(TAG,"selectResult[7]="+DataUtils.bytesToHexString(new byte[]{selectResult[7]}));
        Log.e(TAG,"selectResult[8]="+DataUtils.bytesToHexString(new byte[]{selectResult[8]}));
        Log.e(TAG,"selectResult[9]="+DataUtils.bytesToHexString(new byte[]{selectResult[9]}));
        if (selectResult[7] == 0 && selectResult[8] == 0 && selectResult[9] == (byte)0x90) { //选卡命令成了 selectResult[9]=0x81表示选卡失败
            Log.e(TAG,"选卡命令成功执行了");
        }else{
            Log.e(TAG,"选卡命令执行失败");
        }

        byte[]readResult = new byte[1295];
        //3、读取信息
        out = connection.bulkTransfer(outEndpoint, cmd_read, cmd_read.length, 3000);
        ret = connection.bulkTransfer(inEndpoint, readResult, readResult.length, 3000);
        Log.e(TAG,"readResult="+DataUtils.bytesToHexString(readResult));
        Log.e(TAG,"readResult[7]="+DataUtils.bytesToHexString(new byte[]{readResult[7]}));
        Log.e(TAG,"readResult[8]="+DataUtils.bytesToHexString(new byte[]{readResult[8]}));
        Log.e(TAG,"readResult[9]="+DataUtils.bytesToHexString(new byte[]{readResult[9]}));
        if (readResult[7] == 0 && readResult[8] == 0 && readResult[9] == (byte)0x90) { //读卡信息执行成功了，
            Log.e(TAG,"读卡信息执行成功了");
            shouldStop = true;
            count = 0;
            //byte[] data = DataUtils.hexStringToBytes(readResult);
            String dataStr = DataUtils.bytesToHexString(readResult);
            Log.e(TAG,readResult.length+"");
            if(readResult.length>=1295){
                //1.文字信息处理
                byte[] idWordbytes = Arrays.copyOfRange(readResult, 14, 270);
                //2.头像处理
                String headStr = dataStr.substring(540,2588);
                //idCard.headImage = hex2byte(headStr);//Arrays.copyOfRange(data,270, 1294);
                try {
                    String word_name = new String(Arrays.copyOfRange(idWordbytes,0, 30),"UTF-16LE").trim().trim();
                    String word_gender = new String(Arrays.copyOfRange(idWordbytes,30, 32),"UTF-16LE").trim();
                    String word_nation = new String(Arrays.copyOfRange(idWordbytes,32, 36),"UTF-16LE").trim();
                    String word_birthday = new String(Arrays.copyOfRange(idWordbytes,36, 52),"UTF-16LE").trim();
                    String word_address = new String(Arrays.copyOfRange(idWordbytes,52, 122),"UTF-16LE").trim();
                    String word_idCard = new String(Arrays.copyOfRange(idWordbytes,122, 158),"UTF-16LE").trim();
                    String word_issuingAuthority = new String(Arrays.copyOfRange(idWordbytes,158, 188),"UTF-16LE").trim();
                    String word_startTime = new String(Arrays.copyOfRange(idWordbytes,188, 204),"UTF-16LE").trim();
                    String word_startopTime = new String(Arrays.copyOfRange(idWordbytes,204, 220),"UTF-16LE").trim();
                    //名族的特殊处理
                    //
                    Log.e(TAG,"--------------start");
                    Log.e(TAG,"word_name="+word_name);
                    Log.e(TAG,"word_gender="+word_gender);
                    if(!TextUtils.isEmpty(word_nation)){
                        Log.e(TAG,"word_nation="+word_nation+"   民族："+nations[Integer.parseInt(word_nation)]);
                    }else{
                        Log.e(TAG,"word_nation="+word_nation );
                    }

                    Log.e(TAG,"word_birthday="+word_birthday);
                    Log.e(TAG,"word_address="+word_address);
                    Log.e(TAG,"word_idCard="+word_idCard);

                    Log.e(TAG,"word_issuingAuthority="+word_issuingAuthority);
                    Log.e(TAG,"word_startTime="+word_startTime);
                    Log.e(TAG,"word_startopTime="+word_startopTime);

                    Log.e(TAG,"--------------end");

                    //原本还有头像wlt文件的解析，需要用到so库，这里就没有去弄了
                } catch (UnsupportedEncodingException e) {
                    e.getMessage();
                }

                //最后复位一下，如果没有这个操作的话，调试的时候，读取成功后，下一次重新run这个项目，可能会异常退出一下，然后又可以读取身份证
                connection.bulkTransfer(outEndpoint, cmd_SAM, cmd_SAM.length, 3000);
                byte[] samResult= new byte[15];
                ret = connection.bulkTransfer(inEndpoint, samResult, samResult.length, 3000);
                Log.e(TAG,"samResult="+DataUtils.bytesToHexString(samResult));
            }
        }else{
            Log.e(TAG,"读卡信息执行失败");
        }
        if(count>=30){
            shouldStop = true;
        }
    }
    class ReadThread extends Thread{
        @Override
        public void run() {
            super.run();
            while(!threadStop){
                while (!shouldStop) {
                    threadRead(); //一直发命令读取
                }
            }
        }
    }
    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(mUsbPermissionActionReceiver);
        threadStop = true;
        if(connection!=null){
            connection.close();
        }
    }

    private void afterGetUsbPermission(UsbDevice usbDevice){
        //call method to set up device communication
        /*Toast.makeText(getApplicationContext(), String.valueOf("Got permission for usb device: " + usbDevice), Toast.LENGTH_LONG).show();
        */
        Toast.makeText(getApplicationContext(), String.valueOf("Found USB device: VID=" + usbDevice.getVendorId() + " PID=" + usbDevice.getProductId()), Toast.LENGTH_LONG).show();

        doYourOpenUsbDevice(usbDevice);
    }

    private void doYourOpenUsbDevice(UsbDevice device){
        usbDevice = device;
        shouldStop = false;
        //now follow line will NOT show: User has not given permission to device UsbDevice
        //UsbDeviceConnection connection = usbManager.openDevice(usbDevice);
        Log.e(TAG,"已经有权限了，要做自己的事情了--》shouldStop="+shouldStop);

        readWrite();

        //add your operation code here
    }
    public void readByThread(){
        if(thread==null){
            new ReadThread().start();
        }
        /*if(connection==null){
            Log.e(TAG,"connection==null");
        }else {
            if (shouldStop) { //
                shouldStop = false;
                count = 0;
            }
        }*/

    }
    public static byte[] hex2byte(String hex) {
        int len = (hex.length() / 2);
        byte[] result = new byte[len];
        char[] achar = hex.toCharArray();
        for (int i = 0; i < len; i++) {
            int pos = i * 2;
            result[i] = (byte) (toByte(achar[pos]) << 4 | toByte(achar[pos + 1]));
        }
        Log.e("test",result.length+"");
        return result;
    }

    private static byte toByte(char c) {
        byte b = (byte) "0123456789ABCDEF".indexOf(c);
        return b;
    }
}
