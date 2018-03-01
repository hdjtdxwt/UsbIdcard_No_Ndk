package com.epsit.usbidcard_no_ndk;

import android.content.Context;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbDeviceConnection;
import android.hardware.usb.UsbEndpoint;
import android.hardware.usb.UsbInterface;
import android.hardware.usb.UsbManager;
import android.text.TextUtils;
import android.util.Log;

import java.io.UnsupportedEncodingException;
import java.util.Arrays;
import java.util.Date;

/**
 * Created by Administrator on 2018/3/1/001.
 */

public class UsbThread extends Thread{
    UsbDevice usbDevice;
    UsbDeviceConnection connection;
    UsbEndpoint inEndpoint;
    UsbEndpoint outEndpoint;
    IdCardInfo idInfo = new IdCardInfo();;
    UsbManager usbManager;
    boolean threadStop;
    OnThreadIdcardReadListener listener;
    String TAG = "UsbIdcardThread";

    byte[] cmd_SAM = {(byte) 0xAA, (byte) 0xAA, (byte) 0xAA, (byte) 0x96, 0x69, 0x00, 0x03, 0x12, (byte) 0xFF, (byte) 0xEE};//复位
    byte[] cmd_find = {(byte) 0xAA, (byte) 0xAA, (byte) 0xAA, (byte) 0x96, 0x69, 0x00, 0x03, 0x20, 0x01, 0x22};//寻卡命令
    byte[] cmd_selt = {(byte) 0xAA, (byte) 0xAA, (byte) 0xAA, (byte) 0x96, 0x69, 0x00, 0x03, 0x20, 0x02, 0x21};//选卡命令
    byte[] cmd_read = {(byte) 0xAA, (byte) 0xAA, (byte) 0xAA, (byte) 0x96, 0x69, 0x00, 0x03, 0x30, 0x01, 0x32};//读卡命令

    //读取到的身份证信息和对应的民族
    String nations[] = new String[]{"解码错",        // 00
            "汉",            // 01
            "蒙古",            // 02
            "回",            // 03
            "藏",            // 04
            "维吾尔",        // 05
            "苗",            // 06
            "彝",            // 07
            "壮",            // 08
            "布依",            // 09
            "朝鲜",            // 10
            "满",            // 11
            "侗",            // 12
            "瑶",            // 13
            "白",            // 14
            "土家",            // 15
            "哈尼",            // 16
            "哈萨克",        // 17
            "傣",            // 18
            "黎",            // 19
            "傈僳",            // 20
            "佤",            // 21
            "畲",            // 22
            "高山",            // 23
            "拉祜",            // 24
            "水",            // 25
            "东乡",            // 26
            "纳西",            // 27
            "景颇",            // 28
            "柯尔克孜",        // 29
            "土",            // 30
            "达斡尔",        // 31
            "仫佬",            // 32
            "羌",            // 33
            "布朗",            // 34
            "撒拉",            // 35
            "毛南",            // 36
            "仡佬",            // 37
            "锡伯",            // 38
            "阿昌",            // 39
            "普米",            // 40
            "塔吉克",        // 41
            "怒",            // 42
            "乌孜别克",        // 43
            "俄罗斯",        // 44
            "鄂温克",        // 45
            "德昴",            // 46
            "保安",            // 47
            "裕固",            // 48
            "京",            // 49
            "塔塔尔",        // 50
            "独龙",            // 51
            "鄂伦春",        // 52
            "赫哲",            // 53
            "门巴",            // 54
            "珞巴",            // 55
            "基诺",            // 56
            "编码错",        // 57
            "其他",            // 97
            "外国血统"        // 98
    };

    public static interface OnThreadIdcardReadListener{
        public void onReaded(IdCardInfo idCardInfo);
    }

    public void setListener(OnThreadIdcardReadListener listener){
        this.listener = listener;
    }

    public UsbThread(UsbManager usbManager, UsbDevice usbDevice){
        this.usbDevice = usbDevice;
        this.usbManager = usbManager;
        initStream();

    }
    public void initStream(){
        if (usbDevice != null) {
            UsbInterface usbInterface = usbDevice.getInterface(0);
            //USBEndpoint为读写数据所需的节点
            inEndpoint = usbInterface.getEndpoint(0);  //读数据节点
            outEndpoint = usbInterface.getEndpoint(1); //写数据节点
            connection = usbManager.openDevice(usbDevice);
            connection.claimInterface(usbInterface, true);
        }
    }
    public void stopAndClose(){
        threadStop = true;//线程停止
        if (connection != null) { //关闭流
            connection.close();
            connection = null;
        }
        if(listener!=null){
            listener = null;
        }
    }
    @Override
    public void run() {
        super.run();
        while (!threadStop) {
            if(connection==null){
                initStream();
            }
            threadRead();
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }


    /**
     * 线程中发送寻卡 选卡 读卡的命令
     */
    public IdCardInfo threadRead() {
        if(BuildConfig.DEBUG){
            Log.e(TAG,"threadRead--执行了");
        }
        //1、寻卡，发送命令
        try {
            Thread.sleep(100);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        int out = connection.bulkTransfer(outEndpoint, cmd_find, cmd_find.length, 3000);
        byte[] findResult = new byte[16];
        int ret = connection.bulkTransfer(inEndpoint, findResult, findResult.length - 1, 3000);

        byte[] selectResult = new byte[20];
        //2、选卡,发送选卡命令
        out = connection.bulkTransfer(outEndpoint, cmd_selt, cmd_selt.length, 3000);
        ret = connection.bulkTransfer(inEndpoint, selectResult, selectResult.length - 1, 3000);

        byte[] readResult = new byte[1295 + 10];

        //3、读取信息
        out = connection.bulkTransfer(outEndpoint, cmd_read, cmd_read.length, 3000);
        ret = connection.bulkTransfer(inEndpoint, readResult, readResult.length - 10, 3000);

        if (readResult != null && readResult.length >= 19) {
            if (readResult[7] == 0 && readResult[8] == 0 && readResult[9] == (byte) 0x90) { //读卡信息执行成功了，
                Log.e(TAG, "读卡信息执行成功了");
                String dataStr = DataUtils.bytesToHexString(readResult);
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
                        if(!TextUtils.isEmpty(word_idCard)){ //身份证号不是空
                            if(!TextUtils.isEmpty(word_gender)){
                                if(word_gender.equals("1")){
                                    idInfo.word_gender ="男";
                                }else {
                                    idInfo.word_gender ="女";
                                }
                            }else{
                                idInfo.word_gender ="";
                            }
                            //idInfo.word_gender = word_gender;

                            if (!TextUtils.isEmpty(word_nation)) {
                                idInfo.word_nation = nations[Integer.parseInt(word_nation)];
                            } else {
                                idInfo.word_nation="";
                            }
                            idInfo.word_birthday = word_birthday;
                            idInfo.word_address=word_address;
                            idInfo.word_idCard =word_idCard;
                            idInfo.word_issuingAuthority =word_issuingAuthority;
                            idInfo.word_startTime =word_startTime;
                            idInfo.word_startopTime =word_startopTime;

                            if(listener!=null){
                                listener.onReaded(idInfo);
                            }
                        }
                        if(BuildConfig.DEBUG){
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
                        }

                        //原本还有头像wlt文件的解析，需要用到so库，这里就没有去弄了
                    } catch (UnsupportedEncodingException e) {
                        e.getMessage();
                    }
                    if(BuildConfig.DEBUG){
                        Log.e(TAG, "-----------date=>" + new Date().toLocaleString());
                    }

                    try {
                        Thread.sleep(100);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }else{ //没有读取到身份证信息
                if(listener!=null){
                    listener.onReaded(null);
                }
            }
        }

        if (connection != null) {
            connection.close();
            connection = null;
        }
        return idInfo;
    }
}
