package com.example.akoni.phonedrone.ConnectP2P;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.wifi.p2p.WifiP2pConfig;
import android.net.wifi.p2p.WifiP2pDevice;
import android.net.wifi.p2p.WifiP2pDeviceList;
import android.net.wifi.p2p.WifiP2pInfo;
import android.net.wifi.p2p.WifiP2pManager;

import com.example.akoni.phonedrone.Starter.ActionEntry;

import java.net.InetAddress;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Akoni on 2/1/2018.
 */

public class WiFiBroadcastReceiver extends BroadcastReceiver {

    private WifiP2pManager wifiP2pManager;
    private WifiP2pManager.Channel wifiChannel;
    private ConnectDevice device;
    private List<WifiP2pDevice> deviceList;
    private List<WifiP2pConfig> configList;
    private WifiP2pDevice wifiP2pDevice;


    public WiFiBroadcastReceiver(WifiP2pManager manager, WifiP2pManager.Channel channel, ConnectDevice device) {
        super();
        this.wifiP2pManager = manager;
        this.wifiChannel = channel;
        this.device = device;
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();

        if (WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION.equals(action)) {
            // Check to see if Wi-Fi is enabled and notify appropriate activity
            int state = intent.getIntExtra(WifiP2pManager.EXTRA_WIFI_STATE, -1);
            if(state == WifiP2pManager.WIFI_P2P_STATE_ENABLED){
                device.makeToast("WiFi-Direct: Enabled");
            }
            else {
                device.makeToast("WiFi-Direct: Disabled");
            }

        } else if (WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION.equals(action)) {
            // Call WifiP2pManager.requestPeers() to get a list of current peers

            deviceList = new ArrayList<>();
            configList = new ArrayList<>();

            if (wifiP2pManager != null){
                WifiP2pManager.PeerListListener peerListListener = new WifiP2pManager.PeerListListener() {
                    @Override
                    public void onPeersAvailable(WifiP2pDeviceList peers) {
                        deviceList.clear();
                        configList.clear();

                        deviceList.addAll(peers.getDeviceList());
                        device.displayPeers(peers);
                        deviceList.addAll(peers.getDeviceList());

                        for (int i = 0; i < peers.getDeviceList().size(); i++){
                            WifiP2pConfig config = new WifiP2pConfig();
                            config.deviceAddress = deviceList.get(i).deviceAddress;
                            configList.add(config);
                        }
                    }
                };

                wifiP2pManager.requestPeers(wifiChannel, peerListListener);
            }

        } else if (WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION.equals(action)) {
            // Respond to new connection or disconnections

        } else if (WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION.equals(action)) {
            // Respond to this device's wifi state changing
        }
    }

    public void connect(int position){
        WifiP2pConfig config = configList.get(position);
        wifiP2pDevice = deviceList.get(position);

        wifiP2pManager.connect(wifiChannel, config, new WifiP2pManager.ActionListener() {
            @Override
            public void onSuccess() {
                device.makeToast("Connected.");
                device.connectionSuccessful();
            }

            @Override
            public void onFailure(int reason) {
                device.makeToast("Connection Failed.");
            }
        });
    }

    WifiP2pManager.ConnectionInfoListener infoListener = new WifiP2pManager.ConnectionInfoListener() {
        @Override
        public void onConnectionInfoAvailable(WifiP2pInfo info) {
            InetAddress groupOwnerAddress = info.groupOwnerAddress;

            if(info.groupFormed){
                if(info.isGroupOwner){
                    device.makeToast("HOST");
                    device.play(groupOwnerAddress, true);
                }
                else {
                    device.makeToast("CLIENT");
                    device.play(groupOwnerAddress, false);
                }
            }
        }
    };

}
