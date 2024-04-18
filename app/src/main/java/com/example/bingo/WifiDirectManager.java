package com.example.bingo;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.wifi.p2p.WifiP2pConfig;
import android.net.wifi.p2p.WifiP2pDevice;
import android.net.wifi.p2p.WifiP2pManager;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

public class WifiDirectManager {
    private WifiP2pManager wifiP2pManager;
    private WifiP2pManager.Channel channel;
    private Context context;
    private IntentFilter intentFilter;
    private BroadcastReceiver broadcastReceiver;
    private List<WifiP2pDevice> peerList = new ArrayList<>();

    public WifiDirectManager(Context context) {
        this.context = context;
        wifiP2pManager = (WifiP2pManager) context.getSystemService(Context.WIFI_P2P_SERVICE);
        channel = wifiP2pManager.initialize(context, context.getMainLooper(), null);

        intentFilter = new IntentFilter();
        intentFilter.addAction(WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION);
        intentFilter.addAction(WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION);
        intentFilter.addAction(WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION);
        intentFilter.addAction(WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION);

        broadcastReceiver = new WiFiDirectBroadcastReceiver(wifiP2pManager, channel, this);
        context.registerReceiver(broadcastReceiver, intentFilter);
    }

    public void discoverPeers() {
        wifiP2pManager.discoverPeers(channel, new WifiP2pManager.ActionListener() {
            @Override
            public void onSuccess() {
                Log.d("WiFiDirectManager", "Peer discovery initiated");
            }

            @Override
            public void onFailure(int reasonCode) {
                Log.d("WiFiDirectManager", "Peer discovery failed with error code: " + reasonCode);
            }
        });
    }

    public List<WifiP2pDevice> getPeerList() {
        return peerList;
    }

    public void connect(WifiP2pDevice device) {
        WifiP2pConfig config = new WifiP2pConfig();
        config.deviceAddress = device.deviceAddress;
        wifiP2pManager.connect(channel, config, new WifiP2pManager.ActionListener() {
            @Override
            public void onSuccess() {
                Log.d("WiFiDirectManager", "Connection request successful");
            }

            @Override
            public void onFailure(int reason) {
                Log.d("WiFiDirectManager", "Connection request failed. Reason: " + reason);
            }
        });
    }

    public void disconnect() {
        wifiP2pManager.removeGroup(channel, new WifiP2pManager.ActionListener() {
            @Override
            public void onSuccess() {
                Log.d("WiFiDirectManager", "Disconnected successfully");
            }

            @Override
            public void onFailure(int reason) {
                Log.d("WiFiDirectManager", "Disconnect failed. Reason: " + reason);
            }
        });
    }

    public void stop() {
        context.unregisterReceiver(broadcastReceiver);
    }
}
