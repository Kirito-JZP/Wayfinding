package com.main.wayfinding.logic;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;

/**
 * Logics for account fragment
 *
 * @author Gang
 * @author Last Modified By Gang
 * @version Revision: 0
 * Date: 2022/1/28 18:40
 */
public class AuthLogic {
    private FirebaseAuth auth;

    public AuthLogic() {
        this.auth = FirebaseAuth.getInstance();
    }

    public void signUp(String username, String password, OnCompleteListener<AuthResult> callback) {
        auth.createUserWithEmailAndPassword(username, password).addOnCompleteListener(callback);
    }

    public void login(String username, String password, OnCompleteListener<AuthResult> callback) {
        auth.signInWithEmailAndPassword(username,password).addOnCompleteListener(callback);
    }

    public void signOut() {
        FirebaseAuth.getInstance().signOut();
    }
}
