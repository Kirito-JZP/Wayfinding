package com.main.wayfinding.fragment.account;

import android.app.AlertDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.main.wayfinding.R;
import com.main.wayfinding.databinding.FragmentAccountBinding;
import com.main.wayfinding.dto.UserDto;
import com.main.wayfinding.logic.AuthLogic;
import com.main.wayfinding.logic.DB.UserDBLogic;

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
        // jump by dialogue
        view.findViewById(R.id.login).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                View view2 = View.inflate(getContext(), R.layout.fragment_accountlogin, null);

                AlertDialog dialoglogin = new AlertDialog.Builder(getActivity()).setView(view2).show();


                view2.findViewById(R.id.confirm).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        EditText usernameComponent = view2.findViewById(R.id.username);
                        EditText passwordComponent = view2.findViewById(R.id.password);
                        String username = usernameComponent.getText().toString();
                        String password = passwordComponent.getText().toString();
                        //验证字符串规格（邮箱格式是否正确，密码最少多少位，复杂程度等）
                        if (username.length() > 0 && password.length() > 0) {
                            accountLogic.login(username, password);
                            //关闭dialoglogin
                            dialoglogin.dismiss();

                        } else {
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

                view2.findViewById(R.id.register).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        EditText usernameComponent = view2.findViewById(R.id.username);
                        EditText passwordComponent = view2.findViewById(R.id.password);
                        EditText firstNameComponent = view2.findViewById(R.id.firstName);
                        EditText surnameComponent = view2.findViewById(R.id.surname);
                        EditText countryComponent = view2.findViewById(R.id.country);
                        EditText phoneNumberComponent = view2.findViewById(R.id.phoneNumber);
                        String username = usernameComponent.getText().toString();
                        String password = passwordComponent.getText().toString();
                        UserDto userDto = new UserDto(
                                firstNameComponent.getText().toString(),
                                surnameComponent.getText().toString(),
                                countryComponent.getText().toString(),
                                phoneNumberComponent.getText().toString());
                        //验证字符串规格（邮箱格式是否正确，密码最少多少位，复杂程度等）
                        if (username.length() > 0 && password.length() > 0) {
                            accountLogic.signUp(username, password, userDto);
                            //关闭dialoglogin
                        } else {
                            //反馈问题
                            //如请填写账号密码等
                        }


                    }
                });


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

    public void reload() {
        FirebaseUser currentUser = auth.getCurrentUser();
        if (currentUser != null) {
            TextView status = getView().findViewById(R.id.tip);
            status.setText(currentUser.getEmail());
        } else {
            TextView status = getView().findViewById(R.id.tip);
            status.setText("Not logged in");
        }

        new UserDBLogic().select(new OnCompleteListener<DataSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DataSnapshot> task) {
                if(task.isSuccessful()){
                    UserDto userDto = task.getResult().getValue(UserDto.class);
                    System.out.println(userDto.getFirstName());
                    System.out.println(userDto.getSurname());
                    System.out.println(userDto.getCountry());
                    System.out.println(userDto.getPhoneNumber());
                }else {
                    System.out.println(task.getException());
                }
            }
        });
    }


}
