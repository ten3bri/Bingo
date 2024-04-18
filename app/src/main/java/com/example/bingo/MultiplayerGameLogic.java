package com.example.bingo;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.graphics.Color;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Random;

public class MultiplayerGameLogic extends Activity {

    // Metoda ustawiająca flagę trybu wieloosobowego
    public void setIsMultiplayer(boolean isMultiplayer) {
        this.isMultiplayerMode = isMultiplayer;
    }
    private boolean isMultiplayerMode = false;
    private boolean isGameStarted = false;
    private boolean isWinner = false;
    private HashMap<String, Boolean> playerStatusMap;
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
    private ImageView loseMessageTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        boolean isMultiplayer = getIntent().getBooleanExtra("isMultiplayer", false);

        bingoImage = findViewById(R.id.bingoImage);
        replayButton = findViewById(R.id.replayButton);
        loseMessageTextView = findViewById(R.id.loseMessage);

        bingoImage.setVisibility(View.GONE);
        replayButton.setVisibility(View.GONE);
        loseMessageTextView.setVisibility(View.GONE);

        progressBar = findViewById(R.id.progress_bar);
        toolbarTitle = findViewById(R.id.toolbar_title);
        randomNumberTextView = findViewById(R.id.randomNumberTextView);

        random = new Random();
        playerStatusMap = new HashMap<>();

        startGame();
    }

    public void startGame() {
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

        // Assign unique texts to buttons
        for (int i = 0; i < buttonIds.length; i++) {
            final Button button = findViewById(buttonIds[i]);
            button.setText(String.valueOf(randomNumbers.get(i)));
            button.setTag(0); // Reset button tags

            int color = Color.GRAY;
            button.setBackgroundColor(color);
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
                randomNumberTextView.setText("Select Number: " + selectedNumber);

                // If it's the first second, generate a new number and display it in the toolbar
                if (seconds == 5) {
                    generateRandomNumber();
                    toolbarTitle.setText("Time left: " + seconds + "s");
                    randomNumberTextView.setText("Select Number: " + selectedNumber);

                }
            }

            @Override
            public void onFinish() {
                progressBar.setVisibility(View.GONE);
                toolbarTitle.setText("Time's up!");
                handleGameEnd();
                gameActive = false;
                progressBar.postDelayed(() -> {
                    progressBar.setVisibility(View.VISIBLE);
                    startTimer();
                    gameActive = true;

                }, 3000);
            }
        }.start();
    }

    private void generateRandomNumber() {
        ArrayList<Integer> numbersList = new ArrayList<>(availableNumbers);
        Collections.shuffle(numbersList, random);
        selectedNumber = numbersList.get(0);
        availableNumbers.remove((Integer) selectedNumber);

        // Update UI with selected number
        updateSelectedNumber(selectedNumber);
    }

    private void handleGameEnd() {
        gameActive = false;

        // Check if any player is the winner
        String winnerID = null;
        for (String playerID : playerStatusMap.keySet()) {
            if (playerStatusMap.get(playerID)) { // If the player is marked as a winner
                winnerID = playerID;
                break;
            }
        }

        if (winnerID != null) {
            // Display the win message for the winner
            showWinMessage(winnerID);
        } else {
            // Display the lose screen for other players
            showLoseScreen();
        }
    }

    private void showWinMessage(String winnerID) {
        displayBingo();
    }

    private void showLoseScreen() {
        gameActive = false;
        for (int buttonId : buttonIds) {
            Button button = findViewById(buttonId);
            button.setVisibility(View.GONE);
        }
        toolbarTitle.setVisibility(View.GONE);
        loseMessageTextView.setVisibility(View.VISIBLE);
        replayButton.setVisibility(View.VISIBLE);
    }
    public void setWinner(boolean winner) {
        isWinner = winner;
    }
    private void displayBingo() {
        gameActive = false;
        for (int buttonId : buttonIds) {
            Button button = findViewById(buttonId);
            button.setVisibility(View.GONE);
        }

        toolbarTitle.setVisibility(View.GONE);
        bingoImage.setVisibility(View.VISIBLE);
        replayButton.setVisibility(View.VISIBLE);
    }
    private final View.OnClickListener buttonClickListener = v -> {
        Button button= (Button) v;

        if (!gameActive) {
            return; // If time is up, stop handling the click
        }

        if (button.getText().toString().equals(String.valueOf(selectedNumber))) {
            if (button.getTag() == null || (int) button.getTag() == 0) {
                // Change color to yellow on click
                int color = Color.YELLOW;
                button.setBackgroundColor(color);
                button.setTag(1);

                // Check for win
                if (checkBingo()) {
                    if (isMultiplayerMode) {
                        // Mark the player as a winner
                        setWinner(true);
                    } else {
                        // Display "BINGO"
                        displayBingo();
                    }
                }
            } else {
                // Change back to original color on second click
                int color = Color.GRAY; // You can use the original color here
                button.setBackgroundColor(color);
                button.setTag(0);
            }
        }

        button.requestLayout();
    };

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

    private void updateSelectedNumber(int number) {
        // Update UI with the selected number
        randomNumberTextView.setText("Select Number: "+number);
    }
}
