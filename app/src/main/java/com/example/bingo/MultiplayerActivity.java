package com.example.bingo;

import android.content.Intent;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

public class MultiplayerActivity extends AppCompatActivity {

    private EditText editTextNickname;
    private ListView listViewDevices;
    private Button buttonStartGame;
    MultiplayerGameLogic multiplayerGameLogic;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_multiplayer);

        editTextNickname = findViewById(R.id.editTextNickname);
        listViewDevices = findViewById(R.id.listViewDevices);
        buttonStartGame = findViewById(R.id.buttonStartGame);

        multiplayerGameLogic = new MultiplayerGameLogic();

        // Tutaj możesz implementować obsługę zdarzeń, np. kliknięcie przycisku Start Game
        buttonStartGame.setOnClickListener(v -> startGame());
    }

    private void startGame() {
        // Tutaj możesz implementować logikę startu gry, np. wysłanie sygnału startu do innych graczy

        // Początkowe odliczanie 10 sekund
        new CountDownTimer(10000, 1000) {
            public void onTick(long millisUntilFinished) {
                buttonStartGame.setText("Start Game in " + millisUntilFinished / 1000);
            }

            public void onFinish() {
                 Intent intent = new Intent(MultiplayerActivity.this, MultiplayerGameLogic.class);
                 intent.putExtra("is Multiplayer",true);
                 startActivity(intent);
            }
        }.start();
    }

}
