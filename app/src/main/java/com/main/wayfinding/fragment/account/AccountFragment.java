package com.main.wayfinding.fragment.account;

import android.app.AlertDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.android.gms.maps.SupportMapFragment;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.main.wayfinding.R;
import com.main.wayfinding.databinding.FragmentAccountBinding;
import com.main.wayfinding.logic.AccountLogic;

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
    private AccountLogic accountLogic;

    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentAccountBinding.inflate(inflater, container, false);
        View root = binding.getRoot();
        accountLogic.setView(root);
        return root;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        auth = FirebaseAuth.getInstance();
        accountLogic = new AccountLogic();
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


    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        view.findViewById(R.id.login).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                EditText usernameComponent = getView().findViewById(R.id.username);
                EditText passwordComponent = getView().findViewById(R.id.password);
                String username = usernameComponent.getText().toString();
                String password = passwordComponent.getText().toString();
                accountLogic.login(username,password);
                //jump failed
                //getActivity().setContentView(R.layout.fragment_account3);
//                mainLayout = findViewById(R.id.account1);
                //LayoutInflater layoutInflater = LayoutInflater.from(getActivity());
                //View buttonLayout = layoutInflater.inflate(R.layout.fragment_account3, null);
                //container.addView(buttonLayout);
            }
        });
        view.findViewById(R.id.register).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                EditText usernameComponent = getView().findViewById(R.id.username);
                EditText passwordComponent = getView().findViewById(R.id.password);
                String username = usernameComponent.getText().toString();
                String password = passwordComponent.getText().toString();
                accountLogic.signUp(username,password);
            }
        });
        view.findViewById(R.id.logout).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                accountLogic.signOut();
                reload();
            }
        });
    }

    public void reload(){
        FirebaseUser currentUser = auth.getCurrentUser();
        if (currentUser != null) {
            TextView status = getView().findViewById(R.id.tip);
            status.setText(currentUser.getEmail());
        } else {
            TextView status = getView().findViewById(R.id.tip);
            status.setText("Not logged in");
        }
    }

}
