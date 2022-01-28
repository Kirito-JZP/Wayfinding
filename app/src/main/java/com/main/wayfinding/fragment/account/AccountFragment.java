package com.main.wayfinding.fragment.account;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.main.wayfinding.R;
import com.main.wayfinding.databinding.FragmentAccountBinding;

import java.util.concurrent.Executor;


/**
 * Define the fragment used for displaying and changing user info
 *
 * @author Gang
 * @author Last Modified By Gang
 * @version Revision: 0
 * Date: 2022/1/18 16:06
 */
public class AccountFragment extends Fragment {
    private FragmentAccountBinding binding;
    private FirebaseAuth auth;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentAccountBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        return root;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        auth = FirebaseAuth.getInstance();
    }

    @Override
    public void onStart() {
        super.onStart();
        reload();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    public void reload(){
        FirebaseUser currentUser = auth.getCurrentUser();
        if (currentUser != null) {
            TextView status = getView().findViewById(R.id.tip);
            status.setText("Y");
        } else {
            TextView status = getView().findViewById(R.id.tip);
            status.setText("N");
        }
    }
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        view.findViewById(R.id.login).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                EditText username = getView().findViewById(R.id.username);
                System.out.println(username.getText().toString());
            }
        });
        view.findViewById(R.id.register).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                EditText usernameComponent = getView().findViewById(R.id.username);
                EditText passwordComponent = getView().findViewById(R.id.password);
                String username = usernameComponent.getText().toString();
                String password = passwordComponent.getText().toString();
                signUp(username,password);
            }
        });
    }

    public void signUp(String username,String password){
        auth.createUserWithEmailAndPassword(username,password)
                .addOnCompleteListener((Executor) this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if(task.isSuccessful()){
                            reload();
                        }else{
                            TextView status = getView().findViewById(R.id.tip);
                            status.setText("Sign up failed");
                        }
                    }
                });
    }

}
