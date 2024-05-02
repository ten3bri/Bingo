package com.example.bingo;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import android.util.Log;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;


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
                String nickname = editTextNickname.getText().toString().trim();
                if (!nickname.isEmpty()) {
                    // Dołącz do gry, przekazując także nick gracza
                    joinGame(gamePassword, nickname);
                } else {
                    Toast.makeText(this, "Please enter your nickname", Toast.LENGTH_SHORT).show();
                }
            } else {
                Toast.makeText(this, "Please enter a game password", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void joinGame(String gamePassword, String nickname) {
        firebaseManager.joinGame(gamePassword, nickname, new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()) {
                    // Pomyślnie dołączono do gry, rozpocznij odliczanie
                    startCountdown(gamePassword);
                    setInitialWinStatusForPlayers(gamePassword);
                } else {
                    // Jeśli dołączenie nie powiodło się, utwórz nowy pokój i dołącz do niego
                    createAndJoinGame(gamePassword, nickname);
                }
            }
        });
    }

    private void createAndJoinGame(String gamePassword, String nickname) {
        firebaseManager.createGameRoom(gamePassword, nickname, new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()) {
                    // Pomyślnie utworzono pokój, dołącz do niego
                    joinGame(gamePassword, nickname);
                } else {
                    Toast.makeText(MultiplayerActivity.this, "Failed to create the game room", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }
    private void startCountdown(String gamePassword) {
        new CountDownTimer(10000, 1000) {
            public void onTick(long millisUntilFinished) {
                // Odliczanie
            }

            public void onFinish() {
                // Rozpocznij grę po upływie 10 sekund
                startGame(gamePassword);
            }
        }.start();
    }

    private void setInitialWinStatusForPlayers(String gamePassword) {
        firebaseManager.setInitialWinStatusForPlayers(gamePassword, new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot playerSnapshot : dataSnapshot.getChildren()) {
                    String playerNickname = playerSnapshot.getKey();
                    if (playerNickname != null) {
                        // Ustaw domyślny status wygranej dla gracza na false
                        firebaseManager.setPlayerWinStatus(gamePassword, playerNickname, false, new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                if (task.isSuccessful()) {
                                    // Pomyślnie ustawiono status wygranej dla gracza
                                } else {
                                    // Nie udało się ustawić statusu wygranej dla gracza
                                }
                            }
                        });
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                // Obsługa błędu
            }
        });
    }


    private void startGame(String gamePassword) {
        Intent intent = new Intent(MultiplayerActivity.this, MultiplayerGameLogic.class);
        intent.putExtra("gamePassword", gamePassword);
        intent.putExtra("isMultiplayer", true);
        startActivity(intent);
    }
}
