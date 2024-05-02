package com.example.bingo;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.DataSnapshot;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Random;
import android.graphics.Color;

import androidx.annotation.NonNull;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.OnCompleteListener;


public class MultiplayerGameLogic extends Activity {

    private FirebaseAuth firebaseAuth;
    private DatabaseReference gamesRef;
    private ProgressBar progressBar;
    private TextView toolbarTitle;
    private ImageView loseMessageTextView;
    private ImageView bingoImage;
    private Button replayButton;
    private TextView randomNumberTextView;
    private CountDownTimer countDownTimer;
    private Random random;
    private int selectedNumber;
    private ArrayList<Integer> availableNumbers;
    private boolean gameActive = false;
    private final int MAX_ATTEMPTS = 50;
    private int attempts = 0;
    private String gamePassword;
    private FirebaseManager firebaseManager;

    private final int[] buttonIds = {
            R.id.button1, R.id.button2, R.id.button3, R.id.button4,
            R.id.button5, R.id.button6, R.id.button7, R.id.button8,
            R.id.button9, R.id.button10, R.id.button11, R.id.button12,
            R.id.button13, R.id.button14, R.id.button15, R.id.button16,
            R.id.button17, R.id.button18, R.id.button19, R.id.button20,
            R.id.button21, R.id.button22, R.id.button23, R.id.button24,
            R.id.button25
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        firebaseAuth = FirebaseAuth.getInstance();
        FirebaseUser currentUser = firebaseAuth.getCurrentUser();

        firebaseManager = new FirebaseManager(this); // Poprawka: zainicjowanie firebaseManager

        gamesRef = FirebaseDatabase.getInstance().getReference("games");

        progressBar = findViewById(R.id.progress_bar);
        toolbarTitle = findViewById(R.id.toolbar_title);
        randomNumberTextView = findViewById(R.id.randomNumberTextView);
        random = new Random();

        bingoImage = findViewById(R.id.bingoImage);
        loseMessageTextView=findViewById(R.id.loseMessage);
        replayButton = findViewById(R.id.replayButton);

        replayButton.setOnClickListener(replayButtonClickListener);

        bingoImage.setVisibility(View.GONE);
        replayButton.setVisibility(View.GONE);

        // Odbierz hasło pokoju z poprzedniej aktywności
        gamePassword = getIntent().getStringExtra("gamePassword");

        // Sprawdź, czy obaj gracze dołączyli do gry
        checkPlayersJoined(gamePassword);
    }

    private final View.OnClickListener replayButtonClickListener = v -> {
        finish(); // Poprawka: zakończenie gry
    };

    private void startGame() {
        if (!gameActive) {
            gameActive = true;
            initializeGame();
            startTimer();
            listenForGameResults(gamePassword);
        }
    }

    private void initializeGame() {
        availableNumbers = new ArrayList<>();
        for (int i = 1; i <= 50; i++) {
            availableNumbers.add(i);
        }

        ArrayList<Integer> randomNumbers = new ArrayList<>(availableNumbers);
        Collections.shuffle(randomNumbers, random);

        // Przypisz unikalne teksty do przycisków
        for (int i = 0; i < buttonIds.length; i++) {
            final Button button = findViewById(buttonIds[i]);
            button.setText(String.valueOf(randomNumbers.get(i)));
            button.setBackgroundColor(Color.GRAY);
            button.setOnClickListener(buttonClickListener);
        }
    }

    private void startTimer() {
        countDownTimer = new CountDownTimer(6000, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                long seconds = millisUntilFinished / 1000;
                progressBar.setProgress((int) millisUntilFinished);
                toolbarTitle.setText("Time left: " + seconds + "s");
                randomNumberTextView.setText(("Select Number: "+selectedNumber));

                // Generuj nową liczbę, jeśli to pierwsza sekunda
                if (seconds == 5) {
                    generateRandomNumber();
                    toolbarTitle.setText("Time left: " + seconds + "s");
                    randomNumberTextView.setText(("Select Number: "+selectedNumber));
                }
            }

            @Override
            public void onFinish() {
                progressBar.setVisibility(ProgressBar.GONE);
                toolbarTitle.setText("Time's up!");
                gameActive = false;
                progressBar.postDelayed(() -> {
                    progressBar.setVisibility(ProgressBar.VISIBLE);
                    startTimer();
                    gameActive = true;
                    listenForGameResults(gamePassword);
                }, 3000);
            }
        }.start();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (countDownTimer != null) {
            countDownTimer.cancel();
        }
    }

    private final View.OnClickListener buttonClickListener = v -> {
        Button button = (Button) v;

        if(!gameActive){
            return; //jesli czas uplynal zakoncz obsluge klikniecia
        }
        if(button.getText().toString().equals(String.valueOf(selectedNumber))){
            int tag = button.getTag() == null ? 0 : (int) button.getTag(); // Poprawka: ustawienie tagu na 0, jeśli nie jest ustawiony
            if (tag == 0) {
                // Zmiana koloru na żółty po kliknięciu
                int color = Color.YELLOW;
                button.setBackgroundColor(color);
                button.setTag(1);
            } else {
                // Powrót do pierwotnego koloru po drugim kliknięciu
                int color = Color.GRAY; // Tu możesz użyć koloru pierwotnego
                button.setBackgroundColor(color);
                button.setTag(0);
            }
        }
        button.requestLayout();

        // Sprawdź, czy wszystkie przyciski w pionie lub poziomie są kliknięte i żółte
        if (checkBingo()) {
            // Jeśli tak, wyświetl napis "BINGO"
            displayBingo();
        }
    };

    private void generateRandomNumber() {
        if (checkBingo() || attempts >= MAX_ATTEMPTS) {
            return;
        }
        ArrayList<Integer> numbersList = new ArrayList<>(availableNumbers);
        Collections.shuffle(numbersList, random);

        // Iteruj przez listę wylosowanych liczb, aż znajdziesz taką, która jest dostępna
        for (Integer number : numbersList) {
            if (availableNumbers.contains(number)) {
                selectedNumber = number;
                availableNumbers.remove(number);
                attempts++;
                return; // Zakończ pętlę, gdy znajdziesz dostępną liczbę
            }
        }

        // Jeśli nie znaleziono dostępnej liczby, obsłuż tę sytuację
        Log.e("generateRandomNumber", "No available number found after shuffling.");
    }

    @SuppressLint("UseCompatLoadingForDrawables")
    private boolean checkBingo() {
        // Check for horizontal BINGO
        for (int i = 0; i < 5; i++) {
            boolean isBingo = true;
            for (int j = 0; j < 5; j++) {
                Button button = findViewById(buttonIds[i * 5 + j]);
                int tag = button.getTag() == null ? 0 : (int) button.getTag(); // Poprawka: sprawdzenie tagu
                if (tag == 0) {
                    isBingo = false;
                    break;
                }
            }
            if (isBingo) {
                setWinningPlayerWonStatus(gamePassword);
                return true;
            }
        }

        // Check for vertical BINGO
        for (int i = 0; i < 5; i++) {
            boolean isBingo = true;
            for (int j = 0; j < 5; j++) {
                Button button = findViewById(buttonIds[j * 5 + i]);
                int tag = button.getTag() == null ? 0 : (int) button.getTag(); // Poprawka: sprawdzenie tagu
                if (tag == 0) {
                    isBingo = false;
                    break;
                }
            }
            if (isBingo) {
                setWinningPlayerWonStatus(gamePassword);
                return true;
            }
        }

        return false;
    }

    private void setWinningPlayerWonStatus(String gamePassword) {
        DatabaseReference playersRef = FirebaseDatabase.getInstance().getReference("games")
                .child(gamePassword).child("players");

        playersRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                String winningPlayerNickname = getPlayerNicknameForBingo(dataSnapshot);
                if (!winningPlayerNickname.isEmpty()) {
                    dataSnapshot.child(winningPlayerNickname).child("won").getRef().setValue(true);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                // Obsługa błędu
            }
        });
    }

    private void setPlayerWinStatus(String gamePassword, String nickname, boolean hasWon) {
        firebaseManager.setPlayerWinStatus(gamePassword, nickname, hasWon, new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()) {
                    // Status wygranej gracza został pomyślnie zaktualizowany
                } else {
                    // Wystąpił błąd podczas aktualizacji statusu wygranej gracza
                }
            }
        });
    }

    private void displayBingo() {
        gameActive=false;
        for (int buttonId : buttonIds) {
            Button button = findViewById(buttonId);
            button.setVisibility(View.GONE);
        }
        progressBar.setVisibility(View.GONE);
        toolbarTitle.setVisibility(View.GONE);
        randomNumberTextView.setVisibility(View.GONE);
        bingoImage.setVisibility(View.VISIBLE);
        replayButton.setVisibility(View.VISIBLE);
    }

    private void showLoseScreen() {
        try {
            for (int buttonId : buttonIds) {
                Button button = findViewById(buttonId);
                button.setVisibility(View.GONE);
            }
            toolbarTitle.setVisibility(View.GONE);
            loseMessageTextView.setVisibility(View.VISIBLE);
            replayButton.setVisibility(View.VISIBLE);
            Log.d("showLoseScreen", "Lose screen displayed"); // Poprawka: Wywołanie metody Log
        } catch (Exception e) {
            Log.e("showLoseScreen", "Exception caught: " + e.getMessage()); // Poprawka: Wywołanie metody Log
        }
    }

    private void deleteGameRoom(String gamePassword) {
        gamesRef.child(gamePassword).removeValue()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        // Usunięto pokój gry
                    } else {
                        // Nie udało się usunąć pokoju gry
                    }
                });
    }

    private void checkPlayersJoined(String gamePassword) {
        gamesRef.child(gamePassword).child("players").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.getChildrenCount() >= 2) {
                    // Obaj gracze dołączyli do gry, zacznij grę
                    startGame();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                // Obsługa błędu
            }
        });
    }

    private String getPlayerNicknameForBingo(DataSnapshot dataSnapshot) {
        String playerNickname = "";
        for (DataSnapshot playerSnapshot : dataSnapshot.getChildren()) {
            boolean hasWon = playerSnapshot.child("won").getValue(Boolean.class);
            if (hasWon) {
                // Gracz ten wygrał, zwróć jego nick
                playerNickname = playerSnapshot.getKey();
                break; // Jeśli chcesz pobrać tylko pierwszego gracza, możesz przerwać pętlę
            }
        }
        return playerNickname;
    }

    private void listenForGameResults(String gamePassword) {
        gamesRef.child(gamePassword).child("players").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot playerSnapshot : dataSnapshot.getChildren()) {
                    String nickname = playerSnapshot.getKey();
                    boolean won = playerSnapshot.child("won").getValue(Boolean.class);
                    if (won) {
                        showLoseScreen(); // Poprawka: wywołanie metody showLoseScreen()
                        return;
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                // Obsługa błędu
            }
        });
    }
}
