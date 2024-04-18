package com.example.bingo;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.wifi.p2p.WifiP2pDevice;
import android.net.wifi.p2p.WifiP2pDeviceList;
import android.net.wifi.p2p.WifiP2pManager;

import java.util.ArrayList;
import java.util.List;

public class WiFiDirectBroadcastReceiver extends BroadcastReceiver {
    private WifiP2pManager wifiP2pManager;
    private WifiP2pManager.Channel channel;
    private WifiDirectManager WifiDirectManager;

    public WiFiDirectBroadcastReceiver(WifiP2pManager wifiP2pManager, WifiP2pManager.Channel channel, WifiDirectManager wiFiDirectManager) {
        this.wifiP2pManager = wifiP2pManager;
        this.channel = channel;
        this.WifiDirectManager = WifiDirectManager;
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();

        if (WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION.equals(action)) {
            int state = intent.getIntExtra(WifiP2pManager.EXTRA_WIFI_STATE, -1);
            if (state == WifiP2pManager.WIFI_P2P_STATE_ENABLED) {
                // WiFi Direct is enabled
            } else {
                // WiFi Direct is not enabled
            }
        } else if (WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION.equals(action)) {
            if (wifiP2pManager != null) {
                wifiP2pManager.requestPeers(channel, new WifiP2pManager.PeerListListener() {
                    @Override
                    public void onPeersAvailable(WifiP2pDeviceList peers) {
                        List<WifiP2pDevice> refreshedPeers = new ArrayList<>(peers.getDeviceList());
                        if (!refreshedPeers.equals(WifiDirectManager.getPeerList())) {
                            WifiDirectManager.getPeerList().clear();
                            WifiDirectManager.getPeerList().addAll(refreshedPeers);
                        }
                    }
                });
            }
        } else if (WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION.equals(action)) {
            // Handle connection changes
        } else if (WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION.equals(action)) {
            // Handle this device's configuration changes
        }
    }
}

