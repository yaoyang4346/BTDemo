package com.app.chenyang.bt;

import android.app.IntentService;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.os.Environment;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.net.Socket;
import java.util.UUID;

public class ReceiveService extends IntentService {

    public ReceiveService() {
        super("ReceiveService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        MainActivity.d("接受服务启动");
        BluetoothAdapter adapter = BluetoothAdapter.getDefaultAdapter();
        try (BluetoothServerSocket serverSocket = adapter.listenUsingRfcommWithServiceRecord("ReceiveService", MainActivity.uuid)){
            while (true){
                try (BluetoothSocket socket = serverSocket.accept()){
                    InputStream in = socket.getInputStream();
                    ObjectInputStream objectInputStream = new ObjectInputStream(in);
                    FileInfo info = (FileInfo) objectInputStream.readObject();
                    MainActivity.d(info.toString());
                    File dir = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS),"BTFile");
                    if(!dir.exists())
                        dir.mkdir();
                    File file = new File(dir,info.getName());
                    FileOutputStream fileOutputStream = new FileOutputStream(file);
                    int len;
                    byte[] buffer = new byte[1024*8];
                    long total = 0;
                    while((len = in.read(buffer))!=-1){
                        fileOutputStream.write(buffer,0,len);
                        total += len;
                        MainActivity.d("current : " + total + " / total : " + info.getLength());
                        if (total == info.getLength())
                            break;
                    }
                    MainActivity.d(info.getMd5().equals(Utils.getMD5(file)) ? "传输成功" : "传输失败，MD5不一致");
                    MainActivity.d("info:"+file.toString());
                }catch (Exception e){
                    MainActivity.d(e.getMessage());
                    e.printStackTrace();
                }
            }
        }catch (Exception e){
            MainActivity.d(e.getMessage());
            e.printStackTrace();
        }
    }

}
