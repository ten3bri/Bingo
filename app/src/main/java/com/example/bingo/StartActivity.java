package com.example.bingo;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;


public class StartActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_start);


        Button singlePlayerButton = findViewById(R.id.singlePlayerButton);
        Button multiplayerButton = findViewById(R.id.multiplayerButton);

        singlePlayerButton.setOnClickListener(v -> startMainActivity(false));
        multiplayerButton.setOnClickListener(v -> startMultiplayerActivity(true));
    }

    private void startMainActivity(boolean isMultiplayer) {
        Intent intent = new Intent(this, MainActivity.class);
        intent.putExtra("isMultiplayer", isMultiplayer);
        startActivity(intent);
    }
    private void startMultiplayerActivity(boolean isMultiplayer) {
        Intent intent = new Intent(this, MultiplayerActivity.class);
        intent.putExtra("isMultiplayer", isMultiplayer);
        startActivity(intent);


    }
}
