package com.main.wayfinding.fragment.account;

import android.app.AlertDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.main.wayfinding.R;
import com.main.wayfinding.databinding.FragmentAccountBinding;
import com.main.wayfinding.logic.AuthLogic;

/**
 * Define the fragment used for displaying and changing user info
 *
 * @author Gang
 * @author Last Modified By hu
 * @version Revision: 0
 * Date: 2022/2/3 21:58
 */
public class AccountFragment extends Fragment {
    private FragmentAccountBinding binding;
    private FirebaseAuth auth;
    private AuthLogic accountLogic;


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
        accountLogic = new AuthLogic();

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
        view.findViewById(R.id.register).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                EditText usernameComponent = getView().findViewById(R.id.username);
                EditText passwordComponent = getView().findViewById(R.id.password);
                String username = usernameComponent.getText().toString();
                String password = passwordComponent.getText().toString();
                accountLogic.signUp(username, password);
            }
        });
        view.findViewById(R.id.logout).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                accountLogic.signOut();
                reload();
            }
        });
        // jump by dialogue
        view.findViewById(R.id.login).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                View view2 = View.inflate(getContext(), R.layout.fragment_accountlogin, null);
                new AlertDialog.Builder(getActivity()).setView(view2).show();
                view2.findViewById(R.id.confirm).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        EditText usernameComponent = view2.findViewById(R.id.username);
                        EditText passwordComponent = view2.findViewById(R.id.password);
                        String username = usernameComponent.getText().toString();
                        String password = passwordComponent.getText().toString();
                        //验证字符串规格（邮箱格式是否正确，密码最少多少位，复杂程度等）
                        if(username.length()>0&&password.length()>0){
                            accountLogic.login(username, password);
                            //关闭dialog
                        }else{
                            //反馈问题
                            //如请填写账号密码等
                        }
                    }
                });


            }
        });

        view.findViewById(R.id.register).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                View view2 = View.inflate(getContext(), R.layout.fragment_accountcreate, null);
                AlertDialog dialog = new AlertDialog.Builder(getActivity()).setView(view2).show();

                // set dialogue transparent
                WindowManager.LayoutParams lp = dialog.getWindow().getAttributes();
                lp.alpha = 1.0f;
                dialog.getWindow().setAttributes(lp);

            }
        });
    }

    public void reload() {
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
