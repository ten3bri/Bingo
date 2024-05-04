package com.example.bingo;

import android.content.Context;
import android.widget.Toast;
import android.os.AsyncTask;
import android.util.Log;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Tasks;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import java.util.HashMap;
import com.google.firebase.database.ValueEventListener;

import java.util.concurrent.CompletableFuture;

public class FirebaseManager {

    private Context context;
    private FirebaseAuth firebaseAuth;
    private DatabaseReference gamesRef;
    FirebaseDatabase database = FirebaseDatabase.getInstance();




    public FirebaseManager(Context context) {
        this.context = context;
        String databaseURL = database.getReference().toString();
        Log.d("FirebaseURL", databaseURL);
        firebaseAuth = FirebaseAuth.getInstance();
        gamesRef = FirebaseDatabase.getInstance().getReference("games");
    }

    public void joinGame(String gamePassword,String uid, String nickname, OnCompleteListener<Void> onCompleteListener) {
        DatabaseReference playersRef = gamesRef.child(gamePassword).child("players");

        // Ustaw początkowy status wygranej na "false" dla nowo dołączonego gracza
        HashMap<String, Object> playerData = new HashMap<>();
        playerData.put("won", false);

        // Dodaj gracza do listy graczy w pokoju gry
        playersRef.child(uid).setValue(playerData)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        // Jeśli dodanie gracza zakończyło się sukcesem, zwróć sukces do metody wywołującej
                        onCompleteListener.onComplete(task);
                    } else {
                        // W przypadku niepowodzenia dodania gracza, zwróć niepowodzenie do metody wywołującej
                        onCompleteListener.onComplete(task);
                    }
                });
    }


    public void deleteGameRoom(String gamePassword, final OnCompleteListener<Boolean> onCompleteListener) {
        gamesRef.child(gamePassword).removeValue()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        onCompleteListener.onComplete(Tasks.<Boolean>forResult(true));
                    } else {
                        onCompleteListener.onComplete(Tasks.<Boolean>forResult(false));
                    }
                });
    }

    public void createGameRoom(String gamePassword, String nickname, OnCompleteListener<Void> onCompleteListener) {
        HashMap<String, Object> roomData = new HashMap<>();
        roomData.put("status", "waiting");
        HashMap<String, Object> players = new HashMap<>();
        players.put(nickname, true);
        roomData.put("players", players);
        gamesRef.child(gamePassword).setValue(roomData)
                .addOnCompleteListener(onCompleteListener);
    }
    public void setInitialWinStatusForPlayers(String gamePassword, ValueEventListener listener) {
        DatabaseReference playersRef = FirebaseDatabase.getInstance().getReference("games")
                .child(gamePassword).child("players");

        playersRef.addListenerForSingleValueEvent(listener);
    }
    public void setPlayerWinStatus(String gamePassword, String uid, boolean hasWon, OnCompleteListener<Void> onCompleteListener) {
        // Ustawienie statusu wygranej gracza w bazie danych
        gamesRef.child(gamePassword).child("players").child(uid).child("won").setValue(hasWon)
                .addOnCompleteListener(onCompleteListener);
    }

}
