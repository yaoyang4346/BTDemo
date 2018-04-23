package com.app.chenyang.bt;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.net.Uri;
import android.os.Process;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.lang.reflect.Method;
import java.security.Permission;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.UUID;

import butterknife.BindView;
import butterknife.ButterKnife;

public class MainActivity extends AppCompatActivity {
    private static final String[] PERMISSION = new String[]{Manifest.permission.ACCESS_FINE_LOCATION,Manifest.permission.ACCESS_COARSE_LOCATION,Manifest.permission.READ_EXTERNAL_STORAGE,Manifest.permission.WRITE_EXTERNAL_STORAGE};
    private BluetoothAdapter bluetoothAdapter;
    private ArrayList<BluetoothDevice> listBonded;
    private ArrayList<BluetoothDevice> listNearby;
    private MyAdapter adapterBonded;
    private MyAdapter adapterNearby;
    private Intent receive;
    public static final UUID uuid = UUID.nameUUIDFromBytes("my_bt_service".getBytes());
    public static final String EXTRA_DEVICE = "extra_device";
    public static final String EXTRA_INFO = "file_info";
    private BluetoothDevice selectDevice;

    @BindView(R.id.status)
    TextView status;
    @BindView(R.id.lv_bonded)
    ListView lvBonded;
    @BindView(R.id.discover)
    Button discover;
    @BindView(R.id.lv_nearby)
    ListView lvNearby;
    @BindView(R.id.info)
    TextView info;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);
        checkPermission();
        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (bluetoothAdapter == null)
            showInfo("设备不支持蓝牙，应用即将退出","确定", null,(dialog,witch)->Process.killProcess(Process.myPid()),null);

        info.setText("本机信息："+bluetoothAdapter.getName());

        updateState(bluetoothAdapter.getState());
        updateButton();

        listBonded = new ArrayList<>();
        listBonded.addAll(bluetoothAdapter.getBondedDevices());
        adapterBonded = new MyAdapter(this,listBonded);
        lvBonded.setAdapter(adapterBonded);
        lvBonded.setOnItemClickListener((parent, view, position, id)->bondedItemClick(position));

        listNearby = new ArrayList<>();
        adapterNearby = new MyAdapter(this,listNearby);
        lvNearby.setAdapter(adapterNearby);
        lvNearby.setOnItemClickListener((parent, view, position, id)->nearbyItemClick(position));

        IntentFilter filter = new IntentFilter();
        filter.addAction(BluetoothAdapter.ACTION_STATE_CHANGED);
        filter.addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
        filter.addAction(BluetoothAdapter.ACTION_DISCOVERY_STARTED);
        filter.addAction(BluetoothDevice.ACTION_FOUND);
        filter.addAction(BluetoothDevice.ACTION_BOND_STATE_CHANGED);
        registerReceiver(receiver,filter);

        receive = new Intent(this,ReceiveService.class);
        startService(receive);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(receiver);
        stopService(receive);
    }

    private void checkPermission(){
        ArrayList<String> permissionList = new ArrayList<>();
        for (String str : PERMISSION)
            if(ActivityCompat.checkSelfPermission(this, str) != PackageManager.PERMISSION_GRANTED)
                permissionList.add(str);
        if (permissionList.size()!=0)
            ActivityCompat.requestPermissions(this, permissionList.toArray(new String[permissionList.size()]),1);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 1){
            for (int result : grantResults)
                if (result!= PackageManager.PERMISSION_GRANTED)
                    Process.killProcess(Process.myPid());
        }
    }

    public void discover(View view) {
        if (bluetoothAdapter.isDiscovering())
            bluetoothAdapter.cancelDiscovery();
        else
            bluetoothAdapter.startDiscovery();
    }

    public void settings(View view) {
        Intent intent = new Intent(Settings.ACTION_BLUETOOTH_SETTINGS);
        startActivity(intent);
    }

    public void open(View view) {
        if (bluetoothAdapter.isEnabled())
            bluetoothAdapter.disable();
        else{
            Intent intent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivity(intent);
        }
    }

    private BroadcastReceiver receiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            switch (action){
                case BluetoothAdapter.ACTION_STATE_CHANGED:
                    int state = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE,BluetoothAdapter.STATE_OFF);
                    updateState(state);
                    break;
                case BluetoothAdapter.ACTION_DISCOVERY_FINISHED:
                    updateButton();
                    break;
                case BluetoothAdapter.ACTION_DISCOVERY_STARTED:
                    listNearby.clear();
                    adapterNearby.notifyDataSetChanged();
                    updateButton();
                    break;
                case BluetoothDevice.ACTION_FOUND:
                    BluetoothDevice deviceFound = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                    if (!listNearby.contains(deviceFound)) {
                        listNearby.add(deviceFound);
                        adapterNearby.notifyDataSetChanged();
                    }
                    break;
                case BluetoothDevice.ACTION_BOND_STATE_CHANGED:
                    BluetoothDevice deviceBond = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                    if (deviceBond.getBondState() == BluetoothDevice.BOND_BONDING)
                        break;
                    listBonded.clear();
                    listBonded.addAll(bluetoothAdapter.getBondedDevices());
                    adapterBonded.notifyDataSetChanged();
                    break;
            }
        }
    };

    private void updateButton(){
        if (bluetoothAdapter.isDiscovering())
            discover.setText("关闭搜索");
        else
            discover.setText("开启搜索");
    }

    public static void d(String msg){
        Log.d("mmmbt",msg);
    }

    private void updateState(int state) {
        switch (state){
            case BluetoothAdapter.STATE_ON:
                status.setEnabled(true);
                status.setText("蓝牙已开启,点击此处关闭");
                status.setTextColor(Color.BLUE);
                discover.setEnabled(true);
                break;
            case BluetoothAdapter.STATE_TURNING_OFF:
                status.setEnabled(false);
                status.setText("蓝牙关闭中");
                status.setTextColor(Color.GRAY);
                discover.setEnabled(false);
                break;
            case BluetoothAdapter.STATE_TURNING_ON:
                status.setEnabled(false);
                status.setText("蓝牙开启中");
                status.setTextColor(Color.GRAY);
                discover.setEnabled(false);
                break;
            case BluetoothAdapter.STATE_OFF:
            default:
                status.setEnabled(true);
                status.setText("蓝牙未开启,点击此处开启");
                status.setTextColor(Color.RED);
                discover.setEnabled(false);
                break;
        }
    }

    private void showInfo(String msg, String pt,String nt,DialogInterface.OnClickListener positiveListener,DialogInterface.OnClickListener negativeListener){
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("提示：");
        builder.setMessage(msg);
        builder.setPositiveButton(pt,positiveListener);
        if (nt!=null)
            builder.setNegativeButton(nt,negativeListener);
        builder.show();
    }

    public void discoverable(View view) {
        Intent intent = new Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
        intent.putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION,3000);
        startActivity(intent);
    }

    private void nearbyItemClick(int position){
        BluetoothDevice device = listNearby.get(position);
        if (device.getBondState() == BluetoothDevice.BOND_NONE)
            device.createBond();
        else
            showInfo("已配对或正在配对","确定",null,null,null);
    }

    private void bondedItemClick(int position){
        BluetoothDevice device = listBonded.get(position);
        showInfo("请选择操作：","取消配对", "发送文件",(dialog,which)->removeBond(device),(dialog,which)->sendFile(device));
    }

    private void removeBond(BluetoothDevice device) {
        try {
            Method method = BluetoothDevice.class.getMethod("removeBond");
            method.invoke(device);
        } catch (Exception e) {
            d(e.getMessage());
            e.printStackTrace();
        }
    }

    private void sendFile(BluetoothDevice device){
        bluetoothAdapter.cancelDiscovery();
        selectDevice = device;
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("*/*");
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        startActivityForResult(intent, 3);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 3){
            if (resultCode == RESULT_OK){
                Uri uri = data.getData();
                if (uri != null) {
                    String path = Utils.getPath(this,uri);
                    if (path != null) {
                        final File file = new File(path);
                        if (!file.exists() ) {
                            d("找不到文件");
                            return;
                        }
                        FileInfo fileinfo = new FileInfo(file.getName(), file.length(), "",file.getAbsolutePath());
                        Intent intent = new Intent();
                        intent.putExtra(EXTRA_DEVICE,selectDevice);
                        intent.putExtra(EXTRA_INFO,fileinfo);
                        intent.setClass(this,SendService.class);
                        startService(intent);
                    }
                }
            }
        }
    }


}
