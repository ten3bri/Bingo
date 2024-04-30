//package com.example.bingo;
//
//import android.content.Context;
//import android.content.IntentFilter;
//import android.net.wifi.p2p.WifiP2pManager;
//import android.net.wifi.p2p.WifiP2pDeviceList;
//import android.net.wifi.p2p.WifiP2pDevice;
//import java.util.List;
//import java.util.ArrayList;
//
//
//public class WifiDirectManager {
//
//    private Context context;
//    private WifiP2pManager manager;
//    private WifiP2pManager.Channel channel;
//    private WiFiDirectBroadcastReceiver receiver;
//    private IntentFilter intentFilter;
//
//    public WifiDirectManager(Context context) {
//        this.context = context;
//        manager = (WifiP2pManager) context.getSystemService(Context.WIFI_P2P_SERVICE);
//        channel = manager.initialize(context, context.getMainLooper(), null);
//        receiver = new WiFiDirectBroadcastReceiver(manager, channel, this);
//        intentFilter = new IntentFilter();
//        // Dodaj filtry dla interesujących cię akcji
//        intentFilter.addAction(WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION);
//        intentFilter.addAction(WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION);
//        intentFilter.addAction(WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION);
//        intentFilter.addAction(WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION);
//    }
//
//    public IntentFilter getIntentFilter() {
//        return intentFilter;
//    }
//
//    public WiFiDirectBroadcastReceiver getBroadcastReceiver() {
//        return receiver;
//    }
//
//    // Metoda do zwracania obiektu PeerListListener
//    public WifiP2pManager.PeerListListener getPeerListListener() {
//        return new WifiP2pManager.PeerListListener() {
//            @Override
//            public void onPeersAvailable(WifiP2pDeviceList peers) {
//                List<WifiP2pDevice> deviceList = new ArrayList<>(peers.getDeviceList());
//                // Aktualizacja listy urządzeń w interfejsie użytkownika
//                updateDeviceListView(deviceList);
//            }
//        };
//    }
//
//    // Metoda do połączenia z urządzeniem
//    public void connect(WifiP2pDevice device) {
//        // Tutaj możesz zaimplementować logikę połączenia z konkretnym urządzeniem
//    }
//
//
//    // Metoda do rozpoczęcia odkrywania urządzeń
//    public void discoverPeers() {
//        if (manager != null && channel != null) {
//            manager.discoverPeers(channel, new WifiP2pManager.ActionListener() {
//                @Override
//                public void onSuccess() {
//                    // Odkrywanie urządzeń rozpoczęte
//                }
//
//                @Override
//                public void onFailure(int reasonCode) {
//                    // Błąd podczas rozpoczynania odkrywania urządzeń
//                }
//            });
//        }
//    }
//}