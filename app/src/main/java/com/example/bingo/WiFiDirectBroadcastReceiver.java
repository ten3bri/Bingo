//package com.example.bingo;
//
//import android.app.Activity;
//import android.content.BroadcastReceiver;
//import android.content.Context;
//import android.content.Intent;
//import android.content.IntentFilter;
//import android.net.wifi.p2p.WifiP2pDevice;
//import android.net.wifi.p2p.WifiP2pManager;
//import android.net.wifi.p2p.WifiP2pManager.Channel;
//
//public class WiFiDirectBroadcastReceiver extends BroadcastReceiver {
//
//    private WifiP2pManager manager;
//    private Channel channel;
//    private WifiDirectManager wifiDirectManager;
//    private Activity activity;
//    private IntentFilter intentFilter;
//
//    public WiFiDirectBroadcastReceiver(WifiP2pManager manager, Channel channel, WifiDirectManager wifiDirectManager, IntentFilter intentFilter) {
//        super();
//        this.manager = manager;
//        this.channel = channel;
//        this.wifiDirectManager = wifiDirectManager;
//        this.intentFilter = intentFilter;
//    }
//
//    public void setActivity(Activity activity) {
//        this.activity = activity;
//    }
//
//    @Override
//    public void onReceive(Context context, Intent intent) {
//        String action = intent.getAction();
//        if (WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION.equals(action)) {
//            // Sprawdź, czy Wi-Fi P2P jest włączone
//            int state = intent.getIntExtra(WifiP2pManager.EXTRA_WIFI_STATE, -1);
//            if (state == WifiP2pManager.WIFI_P2P_STATE_ENABLED) {
//                // Wi-Fi P2P jest włączone
//                if (activity != null) {
//                    ((MultiplayerActivity) activity).setIsWifiP2pEnabled(true);
//                }
//            } else {
//                // Wi-Fi P2P jest wyłączone
//                if (activity != null) {
//                    ((MultiplayerActivity) activity).setIsWifiP2pEnabled(false);
//                }
//            }
//        } else if (WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION.equals(action)) {
//            // Zmiana listy urządzeń Wi-Fi P2P
//            // The peer list has changed! We should probably do something about
//            // that.
//        } else if (WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION.equals(action)) {
//            // Zmiana stanu połączenia Wi-Fi P2P
//            // Connection state changed! We should probably do something about
//            // that.
//
//            // Sprawdź, czy połączenie zostało zmienione
//            // Jeśli tak, wywołaj odpowiednie metody w klasie WifiDirectManager
//        } else if (WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION.equals(action)) {
//            // Zmiana informacji o tym urządzeniu
//            // Jeśli potrzebujesz zaktualizować informacje o tym urządzeniu, możesz to zrobić tutaj
//            if (activity != null) {
//                DeviceListFragment fragment = ((MultiplayerActivity) activity).getDeviceListFragment();
//                fragment.updateThisDevice((WifiP2pDevice) intent.getParcelableExtra(WifiP2pManager.EXTRA_WIFI_P2P_DEVICE));
//            }
//        }
//    }
//
//    /** register the BroadcastReceiver with the intent values to be matched */
//    public void onResume() {
//        ((MultiplayerActivity) activity).registerReceiver(this, intentFilter);
//    }
//
//    public void onPause() {
//        ((MultiplayerActivity) activity).unregisterReceiver(this);
//    }
//}
