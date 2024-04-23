package com.example.bingo;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.wifi.p2p.WifiP2pConfig;
import android.net.wifi.p2p.WifiP2pDevice;
import android.net.wifi.p2p.WifiP2pManager;
import android.net.wifi.p2p.WifiP2pManager.Channel;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import java.util.ArrayList;
import java.util.List;

public class MultiplayerActivity extends AppCompatActivity {

    private static final int PERMISSIONS_REQUEST_CODE = 123;

    private EditText editTextNickname;
    private ListView listViewDevices;
    private Button buttonStartGame;

    private WifiP2pManager wifiP2pManager;
    private Channel channel;
    private WifiDirectManager wifiDirectManager;
    private List<WifiP2pDevice> peerList = new ArrayList<>();

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_multiplayer);

        wifiDirectManager = new WifiDirectManager(this);

        editTextNickname = findViewById(R.id.editTextNickname);
        listViewDevices = findViewById(R.id.listViewDevices);
        buttonStartGame = findViewById(R.id.buttonStartGame);

        wifiP2pManager = (WifiP2pManager) getSystemService(Context.WIFI_P2P_SERVICE);
        channel = wifiP2pManager.initialize(this, getMainLooper(), null);

        buttonStartGame.setOnClickListener(v -> {
            startGame();
            buttonStartGame.setEnabled(false);
        });

        if (checkPermissions()) {
            // Rejestracja odbiornika Wi-Fi Direct
            registerReceiver(wifiDirectManager.getBroadcastReceiver(), wifiDirectManager.getIntentFilter());
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // Wyrejestrowanie odbiornika Wi-Fi Direct
        unregisterReceiver(wifiDirectManager.getBroadcastReceiver());
    }

    private boolean checkPermissions() {

        return true;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PERMISSIONS_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Rejestracja odbiornika Wi-Fi Direct
                wifiDirectManager.discoverPeers();
                ;
                registerReceiver(wifiDirectManager.getBroadcastReceiver(), wifiDirectManager.getIntentFilter());
            } else {
                Toast.makeText(this, "Permissions are required for Wi-Fi Direct functionality",
                        Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void startGame() {
        sendStartGameMessage();
        new CountDownTimer(10000, 1000) {
            public void onTick(long millisUntilFinished) {
                buttonStartGame.setText("Start Game in " + millisUntilFinished / 1000);
            }

            public void onFinish() {
                Intent intent = new Intent(MultiplayerActivity.this, MultiplayerGameLogic.class);
                intent.putExtra("is Multiplayer", true);
                startActivity(intent);
            }
        }.start();
    }

    private void sendStartGameMessage() {
        if (wifiP2pManager != null && channel != null) {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED || ActivityCompat.checkSelfPermission(this, Manifest.permission.NEARBY_WIFI_DEVICES) != PackageManager.PERMISSION_GRANTED) {
                // Brak wymaganych uprawnień, należy je zwrócić
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.NEARBY_WIFI_DEVICES},
                        PERMISSIONS_REQUEST_CODE);
                return;
            }
            // Mamy wymagane uprawnienia, więc kontynuujemy z operacją Wi-Fi Direct
            wifiP2pManager.requestPeers(channel, peers -> {
                peerList.clear();
                peerList.addAll(peers.getDeviceList());
                if (!peerList.isEmpty()) {
                    for (WifiP2pDevice device : peerList) {
                        WifiP2pConfig config = new WifiP2pConfig();
                        config.deviceAddress = device.deviceAddress;
                        wifiP2pManager.connect(channel, config, new WifiP2pManager.ActionListener() {
                            @Override
                            public void onSuccess() {
                                // Pomyślnie połączono z urządzeniem
                            }

                            @Override
                            public void onFailure(int reason) {
                                // Nie udało się połączyć z urządzeniem
                            }
                        });
                    }
                } else {
                    Toast.makeText(MultiplayerActivity.this, "No devices found", Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

}
