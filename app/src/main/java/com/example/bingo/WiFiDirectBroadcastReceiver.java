package com.example.bingo;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.wifi.p2p.WifiP2pManager;
import android.net.wifi.p2p.WifiP2pManager.Channel;

public class WiFiDirectBroadcastReceiver extends BroadcastReceiver {

    private WifiP2pManager manager;
    private WifiP2pManager.Channel channel;
    private WifiDirectManager wifiDirectManager;

    public WiFiDirectBroadcastReceiver(WifiP2pManager manager, Channel channel, WifiDirectManager wifiDirectManager) {
        super();
        this.manager = manager;
        this.channel = channel;
        this.wifiDirectManager = wifiDirectManager;
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        if (WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION.equals(action)) {
            // Sprawdź, czy Wi-Fi P2P jest włączone
            int state = intent.getIntExtra(WifiP2pManager.EXTRA_WIFI_STATE, -1);
            if (state == WifiP2pManager.WIFI_P2P_STATE_ENABLED) {
                // Wi-Fi P2P jest włączone
            } else {
                // Wi-Fi P2P jest wyłączone
            }
        } else if (WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION.equals(action)) {
            // Zmiana listy urządzeń Wi-Fi P2P
            if (manager != null) {
                manager.requestPeers(channel, wifiDirectManager.getPeerListListener());
            }
        } else if (WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION.equals(action)) {
            // Zmiana stanu połączenia Wi-Fi P2P
            if (manager == null) {
                return;
            }
            // Sprawdź, czy połączenie zostało zmienione
            // Jeśli tak, wywołaj odpowiednie metody w klasie WifiDirectManager
        } else if (WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION.equals(action)) {
            // Zmiana informacji o tym urządzeniu
            // Jeśli potrzebujesz zaktualizować informacje o tym urządzeniu, możesz to zrobić tutaj
        }
    }
}
