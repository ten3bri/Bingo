package com.example.bingo;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;
import android.os.CountDownTimer;


public class MultiplayerActivity extends AppCompatActivity {

    private EditText editTextGamePassword;
    private EditText editTextNickname;
    private Button buttonJoinGame;
    private FirebaseManager firebaseManager;
    private FirebaseAuth firebaseAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_multiplayer);

        firebaseAuth = FirebaseAuth.getInstance();

        editTextNickname = findViewById(R.id.editTextNickname);
        editTextGamePassword = findViewById(R.id.editTextGamePassword);
        buttonJoinGame = findViewById(R.id.buttonJoinGame);

        firebaseManager = new FirebaseManager(this);

        buttonJoinGame.setOnClickListener(v -> {
            buttonJoinGame.setEnabled(false);
            String gamePassword = editTextGamePassword.getText().toString().trim();
            if (!gamePassword.isEmpty()) {
                String nickname = editTextNickname.getText().toString().trim();
                if (!nickname.isEmpty()) {
                    // Rejestracja lub logowanie użytkownika
                    registerOrLogin(nickname);
                } else {
                    Toast.makeText(this, "Please enter your nickname", Toast.LENGTH_SHORT).show();
                }
            } else {
                Toast.makeText(this, "Please enter a game password", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void registerOrLogin(String nickname) {
        String email = editTextNickname.getText().toString().trim(); // Użyj pola nickname jako adresu e-mail
        String password = editTextGamePassword.getText().toString().trim(); // Użyj pola gamePassword jako hasła

        firebaseAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Rejestracja zakończona sukcesem, automatyczne logowanie
                            FirebaseUser user = firebaseAuth.getCurrentUser();
                            if (user != null) {
                                // Zalogowano użytkownika, pobierz jego UID
                                String uid = user.getUid();
                                // Użyj UID do identyfikacji użytkownika
                                // Możesz również zapisać nick użytkownika w bazie danych Firebase
                                joinGame(uid, email, nickname);
                            } else {
                                // Użytkownik jest null, obsłuż ten przypadek
                                Toast.makeText(MultiplayerActivity.this, "User is null", Toast.LENGTH_SHORT).show();
                            }
                        } else {
                            // Logowanie nie powiodło się, spróbuj ponownie zalogować użytkownika
                            loginUser(email, password, nickname);
                        }
                    }
                });
    }

    private void loginUser(String email, String password, String nickname) {
        firebaseAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Logowanie zakończone sukcesem
                            FirebaseUser user = firebaseAuth.getCurrentUser();
                            if (user != null) {
                                // Zalogowano użytkownika, pobierz jego UID
                                String uid = user.getUid();
                                // Użyj UID do identyfikacji użytkownika
                                // Możesz również zapisać nick użytkownika w bazie danych Firebase
                                joinGame(uid, email, nickname);
                            } else {
                                // Użytkownik jest null, obsłuż ten przypadek
                                Toast.makeText(MultiplayerActivity.this, "User is null", Toast.LENGTH_SHORT).show();
                            }
                        } else {
                            // Logowanie nie powiodło się, wyświetl komunikat o błędzie
                            Toast.makeText(MultiplayerActivity.this, "Authentication failed", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    private void joinGame(String uid, String email, String nickname) {
        // Tutaj możesz użyć UID do identyfikacji użytkownika i dołączenia go do gry
        String gamePassword = editTextGamePassword.getText().toString().trim();
        firebaseManager.joinGame(gamePassword, uid, nickname, new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()) {
                    // Pomyślnie dołączono do gry, rozpocznij odliczanie
                    startCountdown(gamePassword);
                    setInitialWinStatusForPlayers(gamePassword);
                } else {
                    // Jeśli dołączenie nie powiodło się, utwórz nowy pokój i dołącz do niego
                    createAndJoinGame(gamePassword, uid, nickname);
                }
            }
        });
    }

    private void createAndJoinGame(String gamePassword,String uid, String nickname) {
        firebaseManager.createGameRoom(gamePassword, nickname, new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()) {
                    // Pomyślnie utworzono pokój, dołącz do niego
                    joinGame(gamePassword, uid, nickname);
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
                buttonJoinGame.setText("Starting in "+(millisUntilFinished/1000 )+" seconds");
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
