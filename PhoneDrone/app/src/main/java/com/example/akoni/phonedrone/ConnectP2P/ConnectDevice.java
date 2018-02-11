package com.example.akoni.phonedrone.ConnectP2P;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.wifi.p2p.WifiP2pConfig;
import android.net.wifi.p2p.WifiP2pDevice;
import android.net.wifi.p2p.WifiP2pDeviceList;
import android.net.wifi.p2p.WifiP2pManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.akoni.phonedrone.R;
import com.example.akoni.phonedrone.Starter.ActionEntry;

import java.net.InetAddress;

public class ConnectDevice extends AppCompatActivity {
    private ListView listView;
    private IntentFilter intentFilter;
    private WifiP2pManager.Channel channel;
    private WifiP2pManager manager;
    private ArrayAdapter deviceAdapter;
    private TextView textView;
    private WiFiBroadcastReceiver receiver;
    private Intent dataIntent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_connect_device);

        intentFilter = new IntentFilter();

        //Indicates changes
        intentFilter.addAction(WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION);
        intentFilter.addAction(WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION);
        intentFilter.addAction(WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION);
        intentFilter.addAction(WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION);

        listView = findViewById(R.id.listView);
        textView = findViewById(R.id.searching);

        deviceAdapter = new ArrayAdapter(this, android.R.layout.simple_list_item_1);
        listView.setAdapter(deviceAdapter);

        manager = (WifiP2pManager)getSystemService(Context.WIFI_P2P_SERVICE);
        channel = manager.initialize(this, getMainLooper(), null);
        receiver = new WiFiBroadcastReceiver(manager, channel, this);

        searching();

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                receiver.connect(position);
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        registerReceiver(receiver, intentFilter);
    }

    @Override
    protected void onPause() {
        super.onPause();
        unregisterReceiver(receiver);
    }

    public void searching(){
        manager.discoverPeers(channel, new WifiP2pManager.ActionListener() {
            @Override
            public void onSuccess() {
                textView.setText("Searching...");
            }

            @Override
            public void onFailure(int reason) {
                textView.setText("Refresh");
            }
        });
    }

    public void makeToast(String string){
        Toast.makeText(this, string, Toast.LENGTH_LONG).show();
    }

    public void displayPeers(WifiP2pDeviceList deviceList){
        deviceAdapter.clear();

        for(WifiP2pDevice peer : deviceList.getDeviceList()){
            deviceAdapter.add(peer.deviceName + "\n" + peer.deviceAddress);
        }
    }

    public void play(InetAddress hostAddress, boolean host){
        dataIntent.putExtra("HostAddress", hostAddress.getHostAddress());
        dataIntent.putExtra("IsHost", host);
        dataIntent.putExtra("Connected", true);
        startActivity(dataIntent);
    }

    public void connectionSuccessful(){
        Intent i = new Intent(this, ActionEntry.class);
        startActivity(i);
    }
}
