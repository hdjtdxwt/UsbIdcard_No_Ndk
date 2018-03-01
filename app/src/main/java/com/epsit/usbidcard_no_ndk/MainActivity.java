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
import android.view.KeyEvent;
import android.view.View;
import android.widget.Toast;

import java.io.UnsupportedEncodingException;
import java.util.Arrays;
import java.util.Date;
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
        if(threadStop){//线程停了
            Log.e(TAG,"------0");
            if(connection!=null){
                count = 0;
                threadStop = false;
                Log.e(TAG,"----1");
                new Thread(){
                    @Override
                    public void run() {
                        super.run();
                        while(!threadStop){
                            threadRead();
                        }
                    }
                }.start();
            }else{
                readWrite();
                count = 0;
                threadStop = false;
                Log.e(TAG,"----2");
                new Thread(){
                    @Override
                    public void run() {
                        super.run();
                        while(!threadStop){
                            threadRead();
                        }
                    }
                }.start();
            }
        }else{
            Log.e(TAG,"线程没停止");
            count=0;
        }
    }

    /**
     * 获取usb权限之后调用这个方法
     */
    public void readWrite(){
        if(usbDevice!=null){
            UsbInterface usbInterface = usbDevice.getInterface(0);
            //USBEndpoint为读写数据所需的节点
            inEndpoint = usbInterface.getEndpoint(0);  //读数据节点
            outEndpoint = usbInterface.getEndpoint(1); //写数据节点
            connection = usbManager.openDevice(usbDevice);
            connection.claimInterface(usbInterface, true);
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        findViewById(R.id.test).setOnClickListener(this);
        usbManager = (UsbManager) getSystemService(Context.USB_SERVICE);

        usbDevice = getIntent().getParcelableExtra("device");
        if(usbDevice!=null){
            readWrite();
            new Thread(){
                @Override
                public void run() {
                    super.run();
                    while(!threadStop){
                        threadRead();
                    }
                }
            }.start();
        }
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
    /**
     * 线程中发送寻卡 选卡 读卡的命令
     */
    public void threadRead() {
        count++;
        Log.e(TAG, "count----" + count);
        //1、寻卡，发送命令
        Log.e(TAG, "-----------date=>" + new Date().toLocaleString());
        int out = connection.bulkTransfer(outEndpoint, cmd_find, cmd_find.length, 3000);
        byte[] findResult = new byte[15];
        int ret = connection.bulkTransfer(inEndpoint, findResult, findResult.length, 3000);
        Log.e("ret", "ret:" + ret);
        Log.e(TAG, "findResult=" + DataUtils.bytesToHexString(findResult));
        Log.e(TAG, "findResult[7]=" + DataUtils.bytesToHexString(new byte[]{findResult[7]}));
        Log.e(TAG, "findResult[8]=" + DataUtils.bytesToHexString(new byte[]{findResult[8]}));
        Log.e(TAG, "findResult[9]=" + DataUtils.bytesToHexString(new byte[]{findResult[9]}));
        Log.e(TAG, "-----------date=>" + new Date().toLocaleString());
        if (findResult[7] == 0x00 && findResult[8] == 0x00 && findResult[9] == (byte) 0x9f) {  //寻卡命令执行成功了 findResult[9]=0x80表示寻卡失败
            Log.e(TAG, "寻卡命令执行成功了");
        } else {
            Log.e(TAG, "寻卡命令执行失败");
        }
        Log.e(TAG, "-----------date=>" + new Date().toLocaleString());
        byte[] selectResult = new byte[19];
        //2、选卡,发送选卡命令
        out = connection.bulkTransfer(outEndpoint, cmd_selt, cmd_selt.length, 3000);
        ret = connection.bulkTransfer(inEndpoint, selectResult, selectResult.length, 3000);
       // Log.e(TAG,"selectResult.length="+selectResult.length);
        if (selectResult.length >= 19) {
            Log.e(TAG, "selectResult=" + DataUtils.bytesToHexString(selectResult));
            Log.e(TAG, "selectResult[7]=" + DataUtils.bytesToHexString(new byte[]{selectResult[7]}));
            Log.e(TAG, "selectResult[8]=" + DataUtils.bytesToHexString(new byte[]{selectResult[8]}));
            Log.e(TAG, "selectResult[9]=" + DataUtils.bytesToHexString(new byte[]{selectResult[9]}));
            if (selectResult[7] == 0 && selectResult[8] == 0 && selectResult[9] == (byte) 0x90) { //选卡命令成了 selectResult[9]=0x81表示选卡失败
                Log.e(TAG, "选卡命令成功执行了");
            } else {
                Log.e(TAG, "选卡命令执行失败");
            }
            Log.e(TAG, "-----------date=>" + new Date().toLocaleString());
            byte[] readResult = new byte[1295];
            //3、读取信息
            out = connection.bulkTransfer(outEndpoint, cmd_read, cmd_read.length, 3000);
            ret = connection.bulkTransfer(inEndpoint, readResult, readResult.length, 3000);

            Log.e(TAG, "readResult[9]=" + DataUtils.bytesToHexString(new byte[]{readResult[9]}));
            Log.e(TAG,"selectResult.length="+readResult.length);
            if (readResult != null && readResult.length >= 19) {
                Log.e(TAG, "readResult=" + DataUtils.bytesToHexString(readResult));
                Log.e(TAG, "readResult[7]=" + DataUtils.bytesToHexString(new byte[]{readResult[7]}));
                Log.e(TAG, "readResult[8]=" + DataUtils.bytesToHexString(new byte[]{readResult[8]}));
                if (readResult[7] == 0 && readResult[8] == 0 && readResult[9] == (byte) 0x90) { //读卡信息执行成功了，
                    Log.e(TAG, "读卡信息执行成功了");
                    threadStop = true;
                    shouldStop = true;
                    count = 0;
                    //byte[] data = DataUtils.hexStringToBytes(readResult);
                    String dataStr = DataUtils.bytesToHexString(readResult);
                    Log.e(TAG, readResult.length + "");
                    Log.e(TAG, "-----------date=>" + new Date().toLocaleString());
                    if (readResult.length >= 1295) {
                        //1.文字信息处理
                        byte[] idWordbytes = Arrays.copyOfRange(readResult, 14, 270);
                        //2.头像处理
                        String headStr = dataStr.substring(540, 2588);
                        //idCard.headImage = hex2byte(headStr);//Arrays.copyOfRange(data,270, 1294);
                        try {
                            String word_name = new String(Arrays.copyOfRange(idWordbytes, 0, 30), "UTF-16LE").trim().trim();
                            String word_gender = new String(Arrays.copyOfRange(idWordbytes, 30, 32), "UTF-16LE").trim();
                            String word_nation = new String(Arrays.copyOfRange(idWordbytes, 32, 36), "UTF-16LE").trim();
                            String word_birthday = new String(Arrays.copyOfRange(idWordbytes, 36, 52), "UTF-16LE").trim();
                            String word_address = new String(Arrays.copyOfRange(idWordbytes, 52, 122), "UTF-16LE").trim();
                            String word_idCard = new String(Arrays.copyOfRange(idWordbytes, 122, 158), "UTF-16LE").trim();
                            String word_issuingAuthority = new String(Arrays.copyOfRange(idWordbytes, 158, 188), "UTF-16LE").trim();
                            String word_startTime = new String(Arrays.copyOfRange(idWordbytes, 188, 204), "UTF-16LE").trim();
                            String word_startopTime = new String(Arrays.copyOfRange(idWordbytes, 204, 220), "UTF-16LE").trim();
                            //名族的特殊处理
                            //
                            Log.e(TAG, "--------------start");
                            Log.e(TAG, "word_name=" + word_name);
                            Log.e(TAG, "word_gender=" + word_gender);
                            if (!TextUtils.isEmpty(word_nation)) {
                                Log.e(TAG, "word_nation=" + word_nation + "   民族：" + nations[Integer.parseInt(word_nation)]);
                            } else {
                                Log.e(TAG, "word_nation=" + word_nation);
                            }

                            Log.e(TAG, "word_birthday=" + word_birthday);
                            Log.e(TAG, "word_address=" + word_address);
                            Log.e(TAG, "word_idCard=" + word_idCard);

                            Log.e(TAG, "word_issuingAuthority=" + word_issuingAuthority);
                            Log.e(TAG, "word_startTime=" + word_startTime);
                            Log.e(TAG, "word_startopTime=" + word_startopTime);

                            Log.e(TAG, "--------------end");

                            //原本还有头像wlt文件的解析，需要用到so库，这里就没有去弄了
                        } catch (UnsupportedEncodingException e) {
                            e.getMessage();
                        }
                        Log.e(TAG, "-----------date=>" + new Date().toLocaleString());
                        //最后复位一下，如果没有这个操作的话，调试的时候，读取成功后，下一次重新run这个项目，可能会异常退出一下，然后又可以读取身份证
                        connection.bulkTransfer(outEndpoint, cmd_SAM, cmd_SAM.length, 3000);
                        byte[] samResult = new byte[15];
                        ret = connection.bulkTransfer(inEndpoint, samResult, samResult.length, 3000);
                    }
                }
            }

        } else {
            Log.e(TAG, "读卡信息执行失败");
        }
        if (count >= 30) {
            threadStop = true;
            shouldStop = true;
        }
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if(keyCode==KeyEvent.KEYCODE_BACK){
            threadStop = true;
        }
        return super.onKeyDown(keyCode, event);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        threadStop = true;
        if(connection!=null){
            connection.close();
        }
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
