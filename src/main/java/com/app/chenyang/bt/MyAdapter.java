package com.app.chenyang.bt;

import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.net.wifi.p2p.WifiP2pDevice;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

import java.util.ArrayList;

/**
 * Created by chenyang on 2018/4/18.
 */

public class MyAdapter extends BaseAdapter {
    private ArrayList<BluetoothDevice> list;
    private Context context;

    MyAdapter(Context context, ArrayList<BluetoothDevice> list){
        this.list = list;
        this.context = context;
    }

    @Override
    public int getCount() {
        return list.size();
    }

    @Override
    public BluetoothDevice getItem(int position) {
        return list.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder viewHold = null;
        if (convertView == null){
            convertView = View.inflate(context,R.layout.item,null);
            viewHold = new ViewHolder();
            viewHold.name = convertView.findViewById(R.id.name);
            viewHold.address = convertView.findViewById(R.id.address);
            convertView.setTag(viewHold);
        }else{
            viewHold = (ViewHolder) convertView.getTag();
        }
        BluetoothDevice device = getItem(position);
        viewHold.name.setText(TextUtils.isEmpty(device.getName()) ? "未命名" : device.getName());
        viewHold.address.setText(device.getAddress());
        return convertView;
    }
}
