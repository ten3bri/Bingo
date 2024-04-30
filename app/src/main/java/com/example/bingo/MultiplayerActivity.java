package com.example.bingo;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import android.util.Log;

import android.os.CountDownTimer;
import androidx.annotation.NonNull;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;

import com.google.firebase.FirebaseApp;

public class MultiplayerActivity extends AppCompatActivity {

    private EditText editTextGamePassword;
    private EditText editTextNickname;
    private Button buttonJoinGame;
    private FirebaseManager firebaseManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_multiplayer);

        editTextNickname = findViewById(R.id.editTextNickname);
        editTextGamePassword = findViewById(R.id.editTextGamePassword);
        buttonJoinGame = findViewById(R.id.buttonJoinGame);

        firebaseManager = new FirebaseManager(this);

        buttonJoinGame.setOnClickListener(v -> {
            String gamePassword = editTextGamePassword.getText().toString().trim();
            if (!gamePassword.isEmpty()) {
                joinGame(gamePassword);
            } else {
                Toast.makeText(this, "Please enter a game password", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void joinGame(String gamePassword) {
        firebaseManager.joinGame(gamePassword, new OnCompleteListener<Boolean>() {
            @Override
            public void onComplete(@NonNull Task<Boolean> task) {
                if (task.isSuccessful() && task.getResult()) {
                    // Pomyślnie dołączono do gry, rozpocznij odliczanie
                    new CountDownTimer(10000, 1000) {
                        public void onTick(long millisUntilFinished) {
                            // Odliczanie
                        }

                        public void onFinish() {
                            // Rozpocznij grę po upływie 10 sekund
                            startGame();
                            // Usuń pokój po rozpoczęciu gry
                            //firebaseManager.deleteGameRoom(gamePassword);
                        }
                    }.start();
                } else {
                    // Jeśli dołączenie nie powiodło się, utwórz nowy pokój i dołącz do niego
                    createAndJoinGame(gamePassword);
                }
            }
        });
    }

    private void createAndJoinGame(String gamePassword) {
        firebaseManager.createGameRoom(gamePassword, new OnCompleteListener<Boolean>() {
            @Override
            public void onComplete(@NonNull Task<Boolean> task) {
                if (task.isSuccessful() && task.getResult()) {
                    // Pomyślnie utworzono pokój, dołącz do niego
                    joinGame(gamePassword);
                } else {
                    Toast.makeText(MultiplayerActivity.this, "Failed to create the game room", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }


    private void startGame() {
        Intent intent = new Intent(MultiplayerActivity.this, MultiplayerGameLogic.class);
        intent.putExtra("isMultiplayer", true);
        startActivity(intent);
        finish();
    }
}
