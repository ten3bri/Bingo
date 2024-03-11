package com.example.bingo;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.os.Bundle;
import android.widget.Button;
import java.util.ArrayList;
import java.util.Collections;
import android.graphics.Color;
import android.view.View;

import java.util.List;
import java.util.Random;
import android.widget.ImageView;
import android.widget.Toast;

import android.graphics.Picture;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;

public class MainActivity extends Activity {

    //private DatabaseHelper databaseHelper;
    //private SQLiteDatabase database;

    private final String[] allTexts = {
            "Tekst 1", "Tekst 2", "Tekst 3", "Tekst 4",
            "Tekst 5", "Tekst 6", "Tekst 7", "Tekst 8",
            "Tekst 9", "Tekst 10", "Tekst 11", "Tekst 12",
            "Tekst 13", "Tekst 14", "Tekst 15", "Tekst 16",
            "Tekst 17", "Tekst 18", "Tekst 19", "Tekst 20",
    };
    private final int[] buttonIds = {
            R.id.button1, R.id.button2, R.id.button3, R.id.button4,
            R.id.button5, R.id.button6, R.id.button7, R.id.button8,
            R.id.button9, R.id.button10, R.id.button11, R.id.button12,
            R.id.button13, R.id.button14, R.id.button15, R.id.button16
    };


    private ImageView bingoImage;
    private Button replayButton;
    private ArrayList<String> availableTexts;
    private boolean gameActive = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        availableTexts = new ArrayList<>();
        Collections.addAll(availableTexts, allTexts);

        bingoImage = findViewById(R.id.bingoImage);
        replayButton = findViewById(R.id.replayButton);

        bingoImage.setVisibility(View.GONE);
        replayButton.setVisibility(View.GONE);

        replayButton.setOnClickListener(replayButtonClickListener);


        Random random=new Random();

        initializeButtons(random);
    }

    private void initializeButtons(Random random){
        // Przetasuj dostępne teksty
        Collections.shuffle(availableTexts,random);

        // Przypisz unikalne teksty do przycisków
        for (int i = 0; i <buttonIds.length; i++) {
            String buttonText = availableTexts.get(i);
            final Button button = findViewById(buttonIds[i]);
            button.setText(buttonText);
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
        Random random=new Random();
        availableTexts.clear();

        Collections.addAll(availableTexts, allTexts);

        // Przetasuj dostępne teksty
        Collections.shuffle(availableTexts,random);

        // Przypisz unikalne teksty do przycisków
        for (int i = 0; i <buttonIds.length; i++) {
            String buttonText = availableTexts.get(i);
            final Button button = findViewById(buttonIds[i]);
            button.setText(buttonText);
            int kolor = Color.GRAY;
            button.setBackgroundColor(kolor);
            button.setTag(0);
            // Dodaj Listener do przycisku
            button.setVisibility(View.VISIBLE);

        }
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
    @SuppressLint("UseCompatLoadingForDrawables")
    private boolean checkBingo() {
        // Check for horizontal BINGO
        for (int i = 0; i < 4; i++) {
            boolean isBingo = true;
            for (int j = 0; j < 4; j++) {
                Button button = findViewById(buttonIds[i * 4 + j]);
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
        for (int i = 0; i < 4; i++) {
            boolean isBingo = true;
            for (int j = 0; j < 4; j++) {
                Button button = findViewById(buttonIds[j * 4 + i]);
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
