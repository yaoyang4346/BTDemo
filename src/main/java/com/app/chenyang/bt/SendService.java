package com.app.chenyang.bt;

import android.app.IntentService;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.os.SystemClock;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.OutputStream;


public class SendService extends IntentService {

    public SendService() {
        super("SendService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        MainActivity.d("发送服务启动");
        BluetoothDevice device = intent.getParcelableExtra(MainActivity.EXTRA_DEVICE);
        FileInfo fileinfo = (FileInfo) intent.getSerializableExtra(MainActivity.EXTRA_INFO);
        File file = new File(fileinfo.getPath());
        fileinfo.setMd5(Utils.getMD5(file));
        try (BluetoothSocket socket = device.createRfcommSocketToServiceRecord(MainActivity.uuid)){
            socket.connect();
            OutputStream out = socket.getOutputStream();
            ObjectOutputStream objectOutputStream = new ObjectOutputStream(out);
            objectOutputStream.writeObject(fileinfo);
            FileInputStream fileInputStream = new FileInputStream(file);
            int len = 0;
            byte[] buffer = new byte[1024*8];
            long total = 0;
            while((len = fileInputStream.read(buffer))!=-1) {
                out.write(buffer, 0, len);
                total += len;
                MainActivity.d("current : " + total + " / total : " + fileinfo.getLength());
            }
            MainActivity.d("发送完毕");
        } catch (IOException e) {
            e.printStackTrace();
            MainActivity.d(e.getMessage());
        }
    }

}
