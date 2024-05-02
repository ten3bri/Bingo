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

        FirebaseManager firebaseManager = new FirebaseManager(this);


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
        // Przywróć przyciski do stanu początkowego


        gameActive = true;
        //isGameStarted=false;

        for (int buttonId : buttonIds) {
            Button button = findViewById(buttonId);
            button.setVisibility(View.VISIBLE);
        }

        // Ukryj obrazek i przycisk Replay
        bingoImage.setVisibility(View.GONE);
        replayButton.setVisibility(View.GONE);

        finish();

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
            if (button.getTag() == null || (int) button.getTag() == 0) {
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
        // Generuj losową liczbę
        if (checkBingo()){
            //logger.info("Bingo game won");
            return;
        }
        if(attempts>=MAX_ATTEMPTS){
            showLoseScreen();
        };
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
                if (button.getTag() == null || (int) button.getTag() == 0) {
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
                if (button.getTag() == null || (int) button.getTag() == 0) {
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
                for (DataSnapshot playerSnapshot : dataSnapshot.getChildren()) {
                    boolean hasWon = playerSnapshot.child("won").getValue(Boolean.class);
                    if (hasWon) {
                        // Znaleziono zwycięskiego gracza
                        String winningPlayerNickname = playerSnapshot.getKey();
                        // Ustaw flagę "won" na true dla zwycięskiego gracza
                        setPlayerWinStatus(gamePassword, winningPlayerNickname, true);
                        // Możesz wykorzystać nick zwycięzcy w inny sposób w swojej grze
                        // Na przykład wyświetl go na ekranie końcowym lub wyslij powiadomienie do pozostałych graczy
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


    private void setPlayerWinStatus(String gamePassword, String nickname, boolean hasWon) {
        // Ustawienie statusu wygranej gracza w bazie danych
        firebaseManager.setPlayerWinStatus(gamePassword, nickname, hasWon, new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()) {
                    // Status wygranej gracza został pomyślnie zaktualizowany
                    // Tutaj możesz umieścić kod do obsługi dalszych działań po aktualizacji statusu
                } else {
                    // Wystąpił błąd podczas aktualizacji statusu wygranej gracza
                    // Tutaj możesz obsłużyć błąd, np. poprzez wyświetlenie komunikatu użytkownikowi
                }
            }
        });
    }

    // Funkcja wyświetlająca ekran przegranego gracza


    // Funkcja wyświetlająca ekran wygranego gracza
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
            Log.d("showLoseScreen",": Lose screen displayed");
        } catch (Exception e) {
            Log.e("showLoseScreen: Exception caught: {}", e.getMessage());
        }
    }

    // Usuń pokój gry z bazy danych po zakończeniu gry
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

    // Sprawdź, czy obaj gracze dołączyli do gry
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
            // Tutaj pobieramy nick gracza, może to być np. nazwa węzła, jeśli nicki są przechowywane jako klucze
            String nickname = playerSnapshot.getKey();
            // Tutaj możesz przeprowadzić dodatkowe sprawdzenia, czy nick spełnia warunki, które potrzebujesz
            playerNickname = nickname;
            break; // Jeśli chcesz pobrać tylko pierwszego gracza, możesz przerwać pętlę
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
                        // Gracz o nicku 'nickname' wygrał, pokaż ekran przegranego
                        showLoseScreen();
                        return; // Zakończ pętlę po znalezieniu jednego gracza, który wygrał
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
