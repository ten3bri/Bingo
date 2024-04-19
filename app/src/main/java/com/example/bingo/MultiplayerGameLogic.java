package com.example.bingo;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.graphics.Color;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import android.os.Handler;
import android.content.Intent;


import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Random;


public class MultiplayerGameLogic extends Activity {

    private static final Logger logger=LogManager.getLogger(MultiplayerGameLogic.class);
    private boolean isMultiplayerMode = true;
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

        try {
            boolean isMultiplayer = getIntent().getBooleanExtra("isMultiplayer", false);
            logger.info("onCreate: isMultiplayer = {}", isMultiplayer);

            bingoImage = findViewById(R.id.bingoImage);
            replayButton = findViewById(R.id.replayButton);
            loseMessageTextView = findViewById(R.id.loseMessage);

            replayButton.setOnClickListener(replayButtonClickListener);


            bingoImage.setVisibility(View.GONE);
            replayButton.setVisibility(View.GONE);
            loseMessageTextView.setVisibility(View.GONE);

            randomNumberTextView=findViewById(R.id.randomNumberTextView);

            toolbarTitle = findViewById(R.id.toolbar_title);

            random = new Random();
            playerStatusMap = new HashMap<>();

            startGame();
        } catch (Exception e) {
            logger.error("onCreate: Exception caught: {}", e.getMessage());
        }
    }

    private void startGame() {
        try {
            if (!isGameStarted) {
                isGameStarted = true;
                logger.info("startGame: Game started");
                initializeGame();
                startTimer();
            } else {
                Toast.makeText(this, "Game already started!", Toast.LENGTH_SHORT).show();
                logger.info("startGame: Game already started");
            }
        } catch (Exception e) {
            logger.error("startGame: Exception caught: {}", e.getMessage());
        }
    }

    private void initializeGame() {
        try {
            availableNumbers = new ArrayList<>();
            for (int i = 1; i <= 50; i++) {
                availableNumbers.add(i);
            }

            ArrayList<Integer> randomNumbers = new ArrayList<>(availableNumbers);
            Collections.shuffle(randomNumbers, random);

            for (int i = 0; i < buttonIds.length; i++) {
                final Button button = findViewById(buttonIds[i]);
                button.setText(String.valueOf(randomNumbers.get(i)));
                button.setTag(0);
                int color = Color.GRAY;
                button.setBackgroundColor(color);
                button.setOnClickListener(buttonClickListener);
            }
            logger.info("initializeGame: Game initialized");
        } catch (Exception e) {
            logger.error("initializeGame: Exception caught: {}", e.getMessage());
        }
    }

    private void startTimer() {
        countDownTimer = new CountDownTimer(6000, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                long seconds=millisUntilFinished/1000;
                toolbarTitle.setText("Time left: " + seconds + "s");
                randomNumberTextView.setText(("Select Number: " + selectedNumber));
                // Jeśli to pierwsza sekunda, generuj nową liczbę i wyświetl ją w toolbarze
                if (millisUntilFinished / 1000 == 5) {
                    generateRandomNumber();
                    toolbarTitle.setText("Time left: " + seconds + "s");
                    randomNumberTextView.setText(("Select Number: " + selectedNumber));
                }
            }


            @Override
            public void onFinish() {
                toolbarTitle.setText("Time's up!");
                gameActive = false;
                new Handler().postDelayed(() -> {
                    startTimer(); // Uruchom ponownie licznik czasu
                    gameActive = true;
                }, 3000);
            }

        }.start();
    }


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

    private void handleGameEnd() {
        try {
            String winnerID = null;
            for (String playerID : playerStatusMap.keySet()) {
                if (playerStatusMap.get(playerID)) {
                    winnerID = playerID;
                    break;
                }
            }

            if (winnerID != null) {
                // Przekaż informację o wygranej do MultiplayerActivity
                Intent intent = new Intent(this, MultiplayerActivity.class);
                intent.putExtra("winnerID", winnerID);
                startActivity(intent);
            } else {
                showLoseScreen();
            }
            logger.info("handleGameEnd: Game ended");
        } catch (Exception e) {
            logger.error("handleGameEnd: Exception caught: {}", e.getMessage());
        }
    }


    private void showWinMessage(String winnerID) {
        try {
            displayBingo();
            logger.info("showWinMessage: Winner ID: {}", winnerID);
        } catch (Exception e) {
            logger.error("showWinMessage: Exception caught: {}", e.getMessage());
        }
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
            logger.info("showLoseScreen: Lose screen displayed");
        } catch (Exception e) {
            logger.error("showLoseScreen: Exception caught: {}", e.getMessage());
        }
    }

    private void displayBingo() {
        try {
            for (int buttonId : buttonIds) {
                Button button = findViewById(buttonId);
                button.setVisibility(View.GONE);
            }
            toolbarTitle.setVisibility(View.GONE);
            bingoImage.setVisibility(View.VISIBLE);
            replayButton.setVisibility(View.VISIBLE);
            logger.info("displayBingo: Bingo displayed");
        } catch (Exception e) {
            logger.error("displayBingo: Exception caught: {}", e.getMessage());
        }
    }

    public void setWinner(boolean winner) {
        isWinner = winner;
        logger.info("setWinner: Winner set to {}", winner);
    }

    private void updateSelectedNumber(int number) {
        randomNumberTextView.setText("Select Number: "+number);
        logger.info("updateSelectedNumber: Selected number updated to {}", number);
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
        Button button= (Button) v;

        if (!gameActive) {
            return;
        }

        if (button.getText().toString().equals(String.valueOf(selectedNumber))) {
            if (button.getTag() == null || (int) button.getTag() == 0) {
                int color = Color.YELLOW;
                button.setBackgroundColor(color);
                button.setTag(1);

                if (checkBingo()) {
                    if (isMultiplayerMode) {
                        setWinner(true);
                    } else {
                        displayBingo();
                    }
                }
            } else {
                int color = Color.GRAY;
                button.setBackgroundColor(color);
                button.setTag(0);
            }
        }

        button.requestLayout();
        logger.info("buttonClickListener: Button clicked");
    };
    @SuppressLint("UseCompatLoadingForDrawables")
    private boolean checkBingo() {
        try {
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
            logger.info("checkBingo: Bingo checked");
        } catch (Exception e) {
            logger.error("checkBingo: Exception caught: {}", e.getMessage());
        }
        return false;
    }
    public void setIsMultiplayer(boolean isMultiplayer) {
        this.isMultiplayerMode = isMultiplayer;
    }
}
