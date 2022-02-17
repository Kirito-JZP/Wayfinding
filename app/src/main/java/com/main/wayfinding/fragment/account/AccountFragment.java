package com.main.wayfinding.fragment.account;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.main.wayfinding.R;
import com.main.wayfinding.databinding.FragmentAccountBinding;
import com.main.wayfinding.dto.UserDto;
import com.main.wayfinding.logic.AuthLogic;
import com.main.wayfinding.logic.DB.UserDBLogic;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

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

    public static Pattern p =
            Pattern.compile("\\w+([-+.]\\w+)*@\\w+([-.]\\w+)*\\.\\w+([-.]\\w+)*");

    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentAccountBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        return root;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        auth = FirebaseAuth.getInstance();
        accountLogic = new AuthLogic();
        System.out.println("CCC");
    }

    @Override
    public void onStart() {
        super.onStart();
        reload();
        System.out.println("SSS");
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }


    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        // jump by dialogue 登录检验
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
                        if (TextUtils.isEmpty(username) || TextUtils.isEmpty(password)){
                            AlertDialog dialogEmpty = new AlertDialog.Builder(getActivity()).
                                    setTitle("Empty! Please input").show();
                        }else if(!isEmail(username) || (password.length() < 6)){
                            AlertDialog dialogError = new AlertDialog.Builder(getActivity()).
                                    setTitle("Error Email Format or Password too short!").show();
                        } else{
                            accountLogic.login(username, password, new OnCompleteListener<AuthResult>() {
                                @Override
                                public void onComplete(@NonNull Task<AuthResult> task) {
                                    if(task.isSuccessful()){
                                        //关闭dialoglogin
                                        dialoglogin.dismiss();
                                        reload();
                                    }else {
                                        System.out.println(task.getException());
                                        System.out.println(task.getResult());
                                    }
                                }
                            });

                        }
                    }
                });


            }
        });

        view.findViewById(R.id.register).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                View signUpView = View.inflate(getContext(), R.layout.fragment_accountcreate, null);
                AlertDialog dialog = new AlertDialog.Builder(getActivity()).setView(signUpView).show();
                // set dialogue transparent
                WindowManager.LayoutParams lp = dialog.getWindow().getAttributes();
                lp.alpha = 1.0f;
                dialog.getWindow().setAttributes(lp);
                // 注册验证
                signUpView.findViewById(R.id.register).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        EditText usernameComponent = signUpView.findViewById(R.id.username);
                        EditText passwordComponent = signUpView.findViewById(R.id.password);
                        EditText firstNameComponent = signUpView.findViewById(R.id.firstName);
                        EditText surnameComponent = signUpView.findViewById(R.id.surname);
                        EditText countryComponent = signUpView.findViewById(R.id.country);
                        EditText phoneNumberComponent = signUpView.findViewById(R.id.phoneNumber);
                        String username = usernameComponent.getText().toString();
                        String password = passwordComponent.getText().toString();
                        UserDto userDto = new UserDto(
                                firstNameComponent.getText().toString(),
                                surnameComponent.getText().toString(),
                                countryComponent.getText().toString(),
                                phoneNumberComponent.getText().toString());
                        //获取单选框
                        CheckBox checkbox = signUpView.findViewById(R.id.checkBox);


                        //1.验证字符串规格（邮箱格式是否正确，密码最少6位等）
                        // 非空验证
                        if (TextUtils.isEmpty(username)||TextUtils.isEmpty(password)||
                                TextUtils.isEmpty(userDto.getFirstName())||
                                TextUtils.isEmpty(userDto.getSurname())||
                                TextUtils.isEmpty(userDto.getCountry())||
                                TextUtils.isEmpty(userDto.getPhoneNumber())){
                            AlertDialog dialogEmpty = new AlertDialog.Builder(getActivity()).
                                    setTitle("Empty! Please input").show();
                        }else if(!checkbox.isChecked()){
                            AlertDialog dialogCheckbox = new AlertDialog.Builder(getActivity()).
                                    setTitle("Please agree with terms.").show();
                        } else if(!isEmail(username)){
                            AlertDialog dialogError = new AlertDialog.Builder(getActivity()).
                                    setTitle("Error Email Format!").show();
                        } else if (password.length() < 6){
                            AlertDialog dialogError = new AlertDialog.Builder(getActivity()).
                                    setTitle("Password too short!").show();
                        }
                        else {
                            //登录
                            accountLogic.signUp(username, password, new OnCompleteListener<AuthResult>() {
                                @Override
                                public void onComplete(@NonNull Task<AuthResult> task) {
                                    if (task.isSuccessful()) {
                                        new UserDBLogic().insert(userDto);
                                        reload();
                                    } else {
                                        System.out.println(task.getException());
                                    }
                                }
                            });
                            //关闭dialoglogin
                            dialog.dismiss();
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
    //这里的text部分bu 注释掉
    @SuppressLint("SetTextI18n")
    public void reload() {
        FirebaseUser currentUser = auth.getCurrentUser();
        TextView status_firstname = getView().findViewById(R.id.firstName);
        TextView status_surname = getView().findViewById(R.id.surname);
        TextView status_country = getView().findViewById(R.id.country);
        TextView status_email = getView().findViewById(R.id.email);
        TextView status_phone = getView().findViewById(R.id.phoneNumber);
        if (currentUser != null) {
            // if logged in, query and render user information
            new UserDBLogic().select(new OnCompleteListener<DataSnapshot>() {
                @Override
                public void onComplete(@NonNull Task<DataSnapshot> task) {
                    if(task.isSuccessful()){
                        UserDto userDto = task.getResult().getValue(UserDto.class);
                        // 3 输出到layout
                        status_firstname.setText(userDto.getFirstName());
                        status_surname.setText(userDto.getSurname());
                        status_country.setText(userDto.getCountry());
                        status_email.setText(currentUser.getEmail());
                        status_phone.setText(userDto.getPhoneNumber());
                    }else {
                        System.out.println(task.getException().getMessage());
                    }
                }
            });
        } else {
            //如果没登录
            //...
            status_firstname.setText("First Name");
            status_surname.setText("Surname");
            status_country.setText("Country");
            status_email.setText("Email");
            status_phone.setText("Phone Number");
        }


    }

    public static boolean isEmail(String strEmail) {
        Pattern pattern = Pattern .compile("\\w+([-+.]\\w+)*@\\w+([-.]\\w+)*\\.\\w+([-.]\\w+)*");
        Matcher mc = pattern.matcher(strEmail);
        return mc.matches();
    }


}
