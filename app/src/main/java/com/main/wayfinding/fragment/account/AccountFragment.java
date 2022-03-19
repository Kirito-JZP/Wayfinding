package com.main.wayfinding.fragment.account;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.TextView;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContract;
import androidx.activity.result.contract.ActivityResultContracts;
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

import java.io.File;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.net.URI;
import java.net.URL;
import java.util.Objects;
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
    ActivityResultLauncher<Intent> intentActivityResultLauncher;
    //调取系统摄像头的请求码
    private static final int MY_ADD_CASE_CALL_PHONE = 6;
    //打开相册的请求码
    private static final int MY_ADD_CASE_CALL_PHONE2 = 7;

    private static Bitmap bitmap;

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

        intentActivityResultLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), new ActivityResultCallback<ActivityResult>() {
            @Override
            public void onActivityResult(ActivityResult result) {
                if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
                    Uri imageSelected = result.getData().getData();
                    imageSelected = result.getData().getData();
                    System.out.println(imageSelected); // test

                    InputStream is = null;
                    //Uri.parse("content://media/external/images/media/113769")
                    try {
                        is = requireActivity().getContentResolver().openInputStream(imageSelected); // inputStream
                    } catch (FileNotFoundException e) {
                        e.printStackTrace();
                    }



                    View signUpView = View.inflate(getContext(), R.layout.fragment_accountcreate, null);
                    EditText editText = signUpView.findViewById(R.id.username);
                    System.out.println(editText);

                    // set ImageView --- bug
                    // 获取到signupview&createAccountView 修改头像
                    editText.setText("888888888888");
                    ImageView imageView = signUpView.findViewById(R.id.avatar);
                    Bitmap bitmap = BitmapFactory.decodeStream(is);

                    imageView.setImageBitmap(bitmap); // 是否有效？

                    System.out.println("set finished");

                    Log.d("bitmap", bitmap.getPixel(25,35)+""); //test


                }
                System.out.println(result.getResultCode());
            }
        });
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
        // 登录检验 jump by dialogue
        view.findViewById(R.id.login).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                View loginView = View.inflate(getContext(), R.layout.fragment_accountlogin, null);

                AlertDialog dialogLogin = new AlertDialog.Builder(getActivity()).setView(loginView).show();


                loginView.findViewById(R.id.confirm).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        EditText usernameComponent = loginView.findViewById(R.id.username);
                        EditText passwordComponent = loginView.findViewById(R.id.password);
                        String username = usernameComponent.getText().toString();
                        String password = passwordComponent.getText().toString();

                        //登录验证 字符串规格（邮箱格式是否正确，密码最少多少位，复杂程度等）
                        if (TextUtils.isEmpty(username) || TextUtils.isEmpty(password)) {
                            AlertDialog dialogEmpty = new AlertDialog.Builder(getActivity()).
                                    setTitle("Empty! Please input").show();
                        } else if (!isEmail(username)) {
                            AlertDialog dialogError = new AlertDialog.Builder(getActivity()).
                                    setTitle("Error Email Format!").show();
                        } else {
                            accountLogic.login(username, password, new OnCompleteListener<AuthResult>() {
                                @Override
                                public void onComplete(@NonNull Task<AuthResult> task) {
                                    if (task.isSuccessful()) {
                                        //关闭dialoglogin
                                        dialogLogin.dismiss();
                                        reload();

                                    } else {
                                        //如果密码输入错误
                                        String msg = task.getException().getMessage();
                                        System.out.println(msg);
                                        if (msg.equals("The password is invalid or the user does " +
                                                "not have a password.")) {
                                            AlertDialog dialogErrorPassword = new AlertDialog.Builder(getActivity()).
                                                    setTitle("Error Password!").show();
                                        }

                                    }
                                }
                            });

                        }
                    }
                });


            }
        });

        view.findViewById(R.id.sign_up).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                View signUpView = View.inflate(getContext(), R.layout.fragment_accountcreate, null);
                AlertDialog dialogCreate = new AlertDialog.Builder(getActivity()).setView(signUpView).show();
                // set dialogue transparent
                WindowManager.LayoutParams lp = dialogCreate.getWindow().getAttributes();
                lp.alpha = 1.0f;
                dialogCreate.getWindow().setAttributes(lp);
                // 注册验证
                signUpView.findViewById(R.id.create_account).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        EditText usernameComponent = signUpView.findViewById(R.id.username);
                        EditText passwordComponent = signUpView.findViewById(R.id.password);
                        EditText firstNameComponent = signUpView.findViewById(R.id.first_name);
                        EditText surnameComponent = signUpView.findViewById(R.id.surname);
                        EditText countryComponent = signUpView.findViewById(R.id.country);
                        EditText phoneNumberComponent = signUpView.findViewById(R.id.phone_number);
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
                        if (TextUtils.isEmpty(username) || TextUtils.isEmpty(password) ||
                                TextUtils.isEmpty(userDto.getFirstName()) ||
                                TextUtils.isEmpty(userDto.getSurname()) ||
                                TextUtils.isEmpty(userDto.getCountry()) ||
                                TextUtils.isEmpty(userDto.getPhoneNumber())) {
                            AlertDialog dialogEmpty = new AlertDialog.Builder(getActivity()).
                                    setTitle("Empty! Please input").show();
                        } else if (!checkbox.isChecked()) {
                            AlertDialog dialogCheckbox = new AlertDialog.Builder(getActivity()).
                                    setTitle("Please agree with terms.").show();
                        } else if (!isEmail(username)) {
                            AlertDialog dialogError = new AlertDialog.Builder(getActivity()).
                                    setTitle("Error Email Format!").show();
                        } else if (password.length() < 6) {
                            AlertDialog dialogError = new AlertDialog.Builder(getActivity()).
                                    setTitle("Password too short!").show();
                        } else {
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
                            dialogCreate.dismiss();
                        }


                    }
                });

                // 添加头像 这里怎么优化代码？重复
                signUpView.findViewById(R.id.avatar).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        View avatarView = View.inflate(getContext(), R.layout.fragment_avatar, null);
                        AlertDialog dialogAvatar = new AlertDialog.Builder(getActivity()).setView(avatarView).show();
                        //在这里优化？
                        TextView avatar_photo = (TextView) avatarView.findViewById(R.id.photo);
                        TextView avatar_photograph = (TextView) avatarView.findViewById(R.id.photograph);
                        TextView avatar_cancel = (TextView) avatarView.findViewById(R.id.cancel);

                        avatar_photo.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                System.out.println("从相册选择");
                                Intent album = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                                intentActivityResultLauncher.launch(album);



                            }
                        });

                        // 拍照
                        avatar_photograph.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                System.out.println("拍照");
                                Handler imgHandler = new Handler();
                                // set imageView test
                                try {
                                    // resolving the string into url

                                    URL url = new URL("https://p0.ssl.img.360kuai.com/t01630a753af82c625f.jpg?size=640x410");
                                    // Open the input stream
                                    InputStream inputStream = url.openStream();
                                    // Convert the online source to bitmap picture
                                    Bitmap bitmap = BitmapFactory.decodeStream(inputStream);
                                    System.out.println("set Image!!!");
                                    ImageView imageView = signUpView.findViewById(R.id.avatar);
                                    imgHandler.post(() -> {
                                        imageView.setImageBitmap(bitmap);

                                    });
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }


                            }
                        });

                        avatar_cancel.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                dialogAvatar.dismiss();
                            }
                        });
                        //
                    }
                });


            }
        });

        view.findViewById(R.id.sign_out).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                accountLogic.signOut();
                reload();


            }
        });


    }

    //when click the AccountFragment page, execute
    @SuppressLint("SetTextI18n")
    public void reload() {
        System.out.println("reload!!!!!!");
        FirebaseUser currentUser = auth.getCurrentUser();
        EditText status_firstname = getView().findViewById(R.id.first_name);
        EditText status_surname = getView().findViewById(R.id.surname);
        EditText status_country = getView().findViewById(R.id.country);
        EditText status_email = getView().findViewById(R.id.email);
        EditText status_phone = getView().findViewById(R.id.phone_number);

        status_firstname.setEnabled(false);
        status_surname.setEnabled(false);
        status_country.setEnabled(false);
        status_email.setEnabled(false);
        status_phone.setEnabled(false);

        if (currentUser != null) {
            // if logged in, query and render user information
            new UserDBLogic().select(new OnCompleteListener<DataSnapshot>() {
                @Override
                public void onComplete(@NonNull Task<DataSnapshot> task) {
                    if (task.isSuccessful()) {
                        UserDto userDto = task.getResult().getValue(UserDto.class);
                        // 3 输出到layout
                        status_firstname.setText(userDto.getFirstName());
                        status_surname.setText(userDto.getSurname());
                        status_country.setText(userDto.getCountry());
                        status_email.setText(currentUser.getEmail());
                        status_phone.setText(userDto.getPhoneNumber());


                        getView().findViewById(R.id.edit).setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                // 设置为可编辑状态
                                status_firstname.setEnabled(true);
                                status_surname.setEnabled(true);
                                status_country.setEnabled(true);
                                status_phone.setEnabled(true);

                                // 有值的情况下点edit 修改两个隐藏按钮back&confirm为可见，
                                // 修改两个按钮(edit&signout)为gone,并设置点击事件
                                getView().findViewById(R.id.edit_back).setVisibility(View.VISIBLE);
                                getView().findViewById(R.id.edit).setVisibility(View.GONE);
                                // 设置back按钮的点击事件....
                                getView().findViewById(R.id.edit_back).setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View view) {
                                        // 回复两个按钮可见和两个不可见
                                        getView().findViewById(R.id.edit_back).setVisibility(View.GONE);
                                        getView().findViewById(R.id.edit).setVisibility(View.VISIBLE);
                                        getView().findViewById(R.id.confirm_edit).setVisibility(View.GONE);
                                        getView().findViewById(R.id.sign_out).setVisibility(View.VISIBLE);
                                        reload();
                                    }
                                });

                                // 设置confirm事件
                                View editView = getView();
                                editView.findViewById(R.id.confirm_edit).setVisibility(View.VISIBLE);
                                editView.findViewById(R.id.sign_out).setVisibility(View.GONE);

                                editView.findViewById(R.id.confirm_edit).setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View view) {
                                        userDto.setFisrtName(status_firstname.getText().toString());
                                        userDto.setSurname(status_surname.getText().toString());
                                        userDto.setCountry(status_country.getText().toString());
                                        userDto.setPhoneNumber(status_phone.getText().toString());
                                        // 更新数据
                                        new UserDBLogic().update(userDto);
                                        getView().findViewById(R.id.edit_back).setVisibility(View.GONE);
                                        getView().findViewById(R.id.edit).setVisibility(View.VISIBLE);
                                        getView().findViewById(R.id.confirm_edit).setVisibility(View.GONE);
                                        getView().findViewById(R.id.sign_out).setVisibility(View.VISIBLE);
                                        reload();
                                    }
                                });

                                // 修改头像
                                //editAvatar();

                            }
                        });

                    } else {
                        System.out.println(task.getException().getMessage());
                    }
                }
            });

            //如果roald时currentUser里有值 这只登录 和 注册 按钮为隐藏，登出显示
            getView().findViewById(R.id.login).setVisibility(View.GONE); //可以不要?
            getView().findViewById(R.id.sign_up).setVisibility(View.GONE);
            getView().findViewById(R.id.sign_out).setVisibility(View.VISIBLE);
            getView().findViewById(R.id.edit).setVisibility(View.VISIBLE);


        } else {
            //如果没登录 currentUser == null
            //...
            status_firstname.setText("First Name");
            status_surname.setText("Surname");
            status_country.setText("Country");
            status_email.setText("Email");
            status_phone.setText("Phone Number");


            //如果roald时currentUser里无值 这只login和sign_up button为显示，登出隐藏
            getView().findViewById(R.id.sign_out).setVisibility(View.GONE);
            getView().findViewById(R.id.edit).setVisibility(View.GONE);
            getView().findViewById(R.id.login).setVisibility(View.VISIBLE);
            getView().findViewById(R.id.sign_up).setVisibility(View.VISIBLE);


        }


    }

    public static boolean isEmail(String strEmail) {
        Pattern pattern = Pattern.compile("\\w+([-+.]\\w+)*@\\w+([-.]\\w+)*\\.\\w+([-.]\\w+)*");
        Matcher mc = pattern.matcher(strEmail);
        return mc.matches();
    }

    // 修改头像 先不用这个
//    public void editAvatar(){
//        getView().findViewById(R.id.avatar).setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                View avatarView = View.inflate(getContext(), R.layout.fragment_avatar, null);
//                AlertDialog dialogAvatar = new AlertDialog.Builder(getActivity()).setView(avatarView).show();
//
//                switch (avatarView.getId()){
//                    case R.id.photo:
//                        System.out.println("拍照");
//
//                        break;
//                    case R.id.photograph:
//                        System.out.println("从相册选择");
//                        break;
//                    case R.id.cancel:
//                        dialogAvatar.dismiss();
//                        break;
//                    default:break;
//                }
//            }
//        });
//    }


}
