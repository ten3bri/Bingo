package com.example.bingo;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Random;
import android.os.CountDownTimer;
import android.graphics.Color;
import java.util.HashSet;
import android.widget.Toast;

public class MainActivity extends Activity {

    private static final Logger logger=LogManager.getLogger(MultiplayerGameLogic.class);
    private boolean isMultiplayerMode=false;
    private boolean isGameStarted=false;
    private String winnerID=null;
    private HashMap<String,Boolean> playerStatusMap;
    private final int[] buttonIds = {
            R.id.button1, R.id.button2, R.id.button3, R.id.button4,
            R.id.button5, R.id.button6, R.id.button7, R.id.button8,
            R.id.button9, R.id.button10, R.id.button11, R.id.button12,
            R.id.button13, R.id.button14, R.id.button15, R.id.button16,
            R.id.button17, R.id.button18, R.id.button19, R.id.button20,
            R.id.button21, R.id.button22, R.id.button23, R.id.button24,
            R.id.button25
    };

    private ProgressBar progressBar;
    private TextView toolbarTitle;
    private CountDownTimer countDownTimer;
    private Random random;
    private int selectedNumber;
    private ArrayList<Integer> availableNumbers;
    private boolean gameActive = true;
    private ImageView bingoImage;
    private Button replayButton;
    private TextView randomNumberTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        boolean isMultiplayer = getIntent().getBooleanExtra("isMultiplayer", false);


        bingoImage = findViewById(R.id.bingoImage);
        replayButton = findViewById(R.id.replayButton);

        replayButton.setOnClickListener(replayButtonClickListener);

        bingoImage.setVisibility(View.GONE);
        replayButton.setVisibility(View.GONE);

        progressBar = findViewById(R.id.progress_bar);
        toolbarTitle = findViewById(R.id.toolbar_title);
        randomNumberTextView=findViewById(R.id.randomNumberTextView);

        random = new Random();
        playerStatusMap=new HashMap<>();

        startGame();
    }

    private void startGame() {
        if (!isGameStarted) {
            isGameStarted = true;
            initializeGame();

            startTimer();
        } else {
            Toast.makeText(this, "Game already started!", Toast.LENGTH_SHORT).show();
        }
    }

    private void initializeGame() {
        availableNumbers = new ArrayList<>();
        for (int i = 1; i <= 50; i++) {
            availableNumbers.add(i);
        }

        ArrayList<Integer> randomNumbers = new ArrayList<>(availableNumbers);
        Collections.shuffle(randomNumbers, random);

        // Przypisanie unikalnych tekstów do przycisków
        for (int i = 0; i < buttonIds.length; i++) {
            final Button button = findViewById(buttonIds[i]);
            button.setText(String.valueOf(randomNumbers.get(i)));

            int color = Color.GRAY;
            button.setBackgroundColor(color);
            button.setOnClickListener(buttonClickListener);
        }
    }

    private void startTimer() {
        countDownTimer = new CountDownTimer(6000, 1000) {
            @SuppressLint("SetTextI18n")
            @Override
            public void onTick(long millisUntilFinished) {
                long seconds = millisUntilFinished / 1000;
                progressBar.setProgress((int) millisUntilFinished);
                toolbarTitle.setText("Time left: " + seconds + "s");
                randomNumberTextView.setText(("Select Number: "+selectedNumber));

                // Jeśli to pierwsza sekunda, generuj nową liczbę i wyświetl ją w toolbarze
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



    // Funkcja wyświetlająca BINGO i przycisk Replay oraz ukrywająca przyciski
    private void displayBingo() {
        gameActive=false;
        for (int buttonId : buttonIds) {
            Button button = findViewById(buttonId);
            button.setVisibility(View.GONE);
        }
        progressBar.setVisibility(View.GONE);
        toolbarTitle.setVisibility(View.GONE);
        bingoImage.setVisibility(View.VISIBLE);
        replayButton.setVisibility(View.VISIBLE);
    }


    private final View.OnClickListener replayButtonClickListener = v -> {
        // Przywróć przyciski do stanu początkowego


        gameActive = true;
        isGameStarted=false;

        for (int buttonId : buttonIds) {
            Button button = findViewById(buttonId);
            button.setVisibility(View.VISIBLE);
        }

        // Ukryj obrazek i przycisk Replay
        bingoImage.setVisibility(View.GONE);
        replayButton.setVisibility(View.GONE);


        initializeGame();
        startTimer();
    };

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
        if (checkBingo()){
            logger.info("Bingo game won");
            return;
        }
        ArrayList<Integer> numbersList = new ArrayList<>(availableNumbers);
        Collections.shuffle(numbersList, random);

        // Iteruj przez listę wylosowanych liczb, aż znajdziesz taką, która jest dostępna
        for (Integer number : numbersList) {
            if (availableNumbers.contains(number)) {
                selectedNumber = number;
                availableNumbers.remove(number);
                return; // Zakończ pętlę, gdy znajdziesz dostępną liczbę
            }
        }

        // Jeśli nie znaleziono dostępnej liczby, obsłuż tę sytuację
        logger.error("generateRandomNumber", "No available number found after shuffling.");
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
                return true;
            }
        }

        return false;
    }
}
