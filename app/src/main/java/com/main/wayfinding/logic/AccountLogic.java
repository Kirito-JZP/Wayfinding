package com.main.wayfinding.logic;

import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.main.wayfinding.R;

import java.util.concurrent.Executor;

public class AccountLogic {
    private FirebaseAuth auth;
    private View view;

    public AccountLogic() {
        this.auth = FirebaseAuth.getInstance();
    }

    public void setView(View view) {
        this.view = view;
    }

    public void signUp(String username, String password) {
        auth.createUserWithEmailAndPassword(username, password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if (task.isSuccessful()) {
                    FirebaseUser currentUser = auth.getCurrentUser();
                    TextView status = view.findViewById(R.id.tip);
                    status.setText(currentUser.getEmail());
                } else {
                    TextView status = view.findViewById(R.id.tip);
                    status.setText("Sign up failed");
                }
            }
        });
    }

    public void login(String username, String password) {
        auth.signInWithEmailAndPassword(username,password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if(task.isSuccessful()){
                    FirebaseUser currentUser = auth.getCurrentUser();
                    TextView status = view.findViewById(R.id.tip);
                    status.setText(currentUser.getEmail());
                }else {
                    TextView status = view.findViewById(R.id.tip);
                    status.setText("Authentication failed.");
                }
            }
        });
    }
}
