package com.example.bingo;

import android.content.Context;
import android.widget.Toast;
import android.util.Log;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

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

    public void joinGame(String gamePassword, final OnCompleteListener<Boolean> onCompleteListener) {
        FirebaseUser currentUser = firebaseAuth.getCurrentUser();
        if (currentUser != null) {
            gamesRef.child(gamePassword).child("status").setValue("in_progress")
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            Log.d("FirebaseManager","joinGame isSuccessful()");
                            onCompleteListener.onComplete(Tasks.<Boolean>forResult(true));
                        } else {
                            onCompleteListener.onComplete(Tasks.<Boolean>forResult(false));
                        }
                    });
        } else {
            Toast.makeText(context, "User not authenticated", Toast.LENGTH_SHORT).show();
           onCompleteListener.onComplete(Tasks.<Boolean>forResult(false));
        }
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

    public void createGameRoom(String gamePassword, final OnCompleteListener<Boolean> onCompleteListener) {
        Log.d("FirebaseManager", "trwfilo doZrobilo gameroom: ");
        gamesRef.child(gamePassword).child("status").setValue("waiting_for_players")
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Log.d("FirebaseManager", "Zrobilo gameroom: ");

                        onCompleteListener.onComplete(Tasks.<Boolean>forResult(true));
                    } else {
                        Log.d("FirebaseManager", "Failed to join game: " + task.getException());

                        onCompleteListener.onComplete(Tasks.<Boolean>forResult(false));
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e("FirebaseManager", "Błąd podczas próby utworzenia pokoju gry: " + e.getMessage());
                    onCompleteListener.onComplete(Tasks.<Boolean>forResult(false));
                })
                .addOnCanceledListener(() -> {
                    Log.e("FirebaseManager", "Operacja anulowana podczas próby utworzenia pokoju gry");
                    onCompleteListener.onComplete(Tasks.<Boolean>forResult(false));
                });
    }
}
