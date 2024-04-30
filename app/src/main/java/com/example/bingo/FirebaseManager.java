package com.example.bingo;

import android.content.Context;
import android.widget.Toast;

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

    public FirebaseManager(Context context) {
        this.context = context;
        //firebaseAuth = FirebaseAuth.getInstance();
        gamesRef = FirebaseDatabase.getInstance().getReference("games");
    }

    public void joinGame(String gamePassword, final OnCompleteListener<Boolean> onCompleteListener) {
        //FirebaseUser currentUser = firebaseAuth.getCurrentUser();
        //if (currentUser != null) {
            gamesRef.child(gamePassword).child("status").setValue("in_progress")
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            onCompleteListener.onComplete(Tasks.<Boolean>forResult(true));
                        } else {
                            onCompleteListener.onComplete(Tasks.<Boolean>forResult(false));
                        }
                    });
        //} else {
        //    Toast.makeText(context, "User not authenticated", Toast.LENGTH_SHORT).show();
        //   onCompleteListener.onComplete(Tasks.<Boolean>forResult(false));
        //}
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
        gamesRef.child(gamePassword).child("status").setValue("waiting_for_players")
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        onCompleteListener.onComplete(Tasks.<Boolean>forResult(true));
                    } else {
                        onCompleteListener.onComplete(Tasks.<Boolean>forResult(false));
                    }
                });
    }
}
