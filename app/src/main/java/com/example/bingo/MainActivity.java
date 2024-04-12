package com.example.bingo;

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
import java.util.Random;
import android.os.CountDownTimer;
import android.graphics.Color;
import java.util.HashSet;


public class MainActivity extends Activity {


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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        bingoImage = findViewById(R.id.bingoImage);
        replayButton = findViewById(R.id.replayButton);

        replayButton.setOnClickListener(replayButtonClickListener);

        bingoImage.setVisibility(View.GONE);
        replayButton.setVisibility(View.GONE);

        progressBar = findViewById(R.id.progress_bar);
        toolbarTitle = findViewById(R.id.toolbar_title);

        random = new Random();
        initializeButtons(random);
        generateRandomNumber(); // Losujemy pierwszą liczbę na początku

        startTimer();
    }

    private void startTimer() {
        countDownTimer = new CountDownTimer(5000, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                long seconds = millisUntilFinished / 1000;
                progressBar.setProgress((int) millisUntilFinished);
                toolbarTitle.setText("Time left: " + seconds + "s");

                // Jeśli to pierwsza sekunda, generuj nową liczbę i wyświetl ją w toolbarze
                if (seconds == 6) {
                    generateRandomNumber();
                    toolbarTitle.setText(toolbarTitle.getText() + " - Selected Number: " + selectedNumber);
                }
            }

            @Override
            public void onFinish() {
                progressBar.setVisibility(ProgressBar.GONE);
                toolbarTitle.setText("Time's up!");
                gameActive = false;
                displayBingo();
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

    private void initializeButtons(){
        availableNumbers = new HashSet<>();
        for (int i = 1; i <= 50; i++) {
            availableNumbers.add(i);
        }

        ArrayList<Integer> randomNumbers = new ArrayList<>(availableNumbers);
        Collections.shuffle(randomNumbers, random);

        // Przypisz unikalne teksty do przycisków
        for (int i = 0; i <buttonIds.length; i++) {
            final Button button=findViewById(buttonIds[i]);
            button.setText(String.valueOf(randomNumbers.get(i)));

            int kolor = Color.GRAY;
            button.setBackgroundColor(kolor);
            // Dodaj Listener do przycisku
            button.setOnClickListener(buttonClickListener);
        }
    }



    // Funkcja wyświetlająca BINGO i przycisk Replay oraz ukrywająca przyciski
    private void displayBingo() {
        gameActive=false;
        for (int buttonId : buttonIds) {
            Button button = findViewById(buttonId);
            button.setVisibility(View.GONE);
        }

        bingoImage.setVisibility(View.VISIBLE);
        replayButton.setVisibility(View.VISIBLE);
    }

    // Funkcja do resetowania przycisków
    private void resetButtons() {
        initializeButtons();
    }

    private final View.OnClickListener replayButtonClickListener = v -> {
        // Przywróć przyciski do stanu początkowego


        gameActive = true;

        for (int buttonId : buttonIds) {
            Button button = findViewById(buttonId);
            button.setVisibility(View.VISIBLE);
        }

        // Ukryj obrazek i przycisk Replay
        bingoImage.setVisibility(View.GONE);
        replayButton.setVisibility(View.GONE);

        resetButtons();


    };

    private final View.OnClickListener buttonClickListener = v -> {
        Button button = (Button) v;


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
        button.requestLayout();

        // Sprawdź, czy wszystkie przyciski w pionie lub poziomie są kliknięte i żółte
        if (checkBingo()) {
            // Jeśli tak, wyświetl napis "BINGO"
            displayBingo();

        }
    };


    private void generateRandomNumber() {
        ArrayList<Integer> numbersList = new ArrayList<>(availableNumbers);
        Collections.shuffle(numbersList, random);
        selectedNumber = numbersList.get(0);
        availableNumbers.remove(selectedNumber);
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
