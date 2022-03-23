package com.main.wayfinding.fragment.account;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.RectF;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
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
import com.google.firebase.storage.UploadTask;
import com.main.wayfinding.R;
import com.main.wayfinding.databinding.FragmentAccountBinding;
import com.main.wayfinding.dto.UserDto;
import com.main.wayfinding.logic.AuthLogic;
import com.main.wayfinding.logic.DB.UserDBLogic;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
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
    private ActivityResultLauncher<Intent> intentActivityResultLauncher;
    private Uri imageSelected;

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
                    imageSelected = result.getData().getData();
                    System.out.println(imageSelected); // test

                    InputStream is = null;

                    try {
                        is = requireActivity().getContentResolver().openInputStream(imageSelected); // inputStream
                        Bitmap bitmap = BitmapFactory.decodeStream(is);
                        ImageView imageView = getView().findViewById(R.id.avatar);
                        //imageView.setImageBitmap(bitmap); // square image
                        //imageView.setImageBitmap(bitmapRound(bitmap,50)); // test round image method1
                        imageView.setImageBitmap(makeRoundCorner(bitmap)); // test round image method2
                    } catch (FileNotFoundException e) {
                        e.printStackTrace();
                    } finally {
                        try {
                            is.close();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }



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


            }
        });

        view.findViewById(R.id.sign_out).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                accountLogic.signOut();
                reload();


            }
        });

        view.findViewById(R.id.edit).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                EditText status_firstname = getView().findViewById(R.id.first_name);
                EditText status_surname = getView().findViewById(R.id.surname);
                EditText status_country = getView().findViewById(R.id.country);
                EditText status_email = getView().findViewById(R.id.email);
                EditText status_phone = getView().findViewById(R.id.phone_number);
                ImageView stauts_avatar = getView().findViewById(R.id.avatar);
                // 设置为可编辑状态
                status_firstname.setEnabled(true);
                status_surname.setEnabled(true);
                status_country.setEnabled(true);
                status_phone.setEnabled(true);
                stauts_avatar.setEnabled(true);


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
                        UserDto userDto = new UserDto();
                        userDto.setFisrtName(status_firstname.getText().toString());
                        userDto.setSurname(status_surname.getText().toString());
                        userDto.setCountry(status_country.getText().toString());
                        userDto.setPhoneNumber(status_phone.getText().toString());
                        // 更新数据
                        UserDBLogic userDBLogic = new UserDBLogic();
                        userDBLogic.update(userDto);

                        //这里confirm上传图片--------
                        if(imageSelected!=null){
                            try {
                                InputStream is = requireActivity().getContentResolver().openInputStream(imageSelected);
                                userDBLogic.uploadAvatar(is, new OnCompleteListener<UploadTask.TaskSnapshot>() {
                                    @Override
                                    public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                                        imageSelected = null;
                                        getView().findViewById(R.id.edit_back).setVisibility(View.GONE);
                                        getView().findViewById(R.id.edit).setVisibility(View.VISIBLE);
                                        getView().findViewById(R.id.confirm_edit).setVisibility(View.GONE);
                                        getView().findViewById(R.id.sign_out).setVisibility(View.VISIBLE);
                                    }
                                });

                                //未关闭，记得在回调里关闭流
                            } catch (FileNotFoundException e) {
                                e.printStackTrace();
                            }

                        }else{
                            getView().findViewById(R.id.edit_back).setVisibility(View.GONE);
                            getView().findViewById(R.id.edit).setVisibility(View.VISIBLE);
                            getView().findViewById(R.id.confirm_edit).setVisibility(View.GONE);
                            getView().findViewById(R.id.sign_out).setVisibility(View.VISIBLE);
                        }

                        reload();
                    }
                });

                // 修改头像
                stauts_avatar.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        View avatarView = View.inflate(getContext(), R.layout.fragment_avatar, null);
                        AlertDialog dialogAvatar = new AlertDialog.Builder(getActivity()).setView(avatarView).show();
                        //在这里优化？
                        TextView avatar_photo = (TextView) avatarView.findViewById(R.id.photo);//album
                        TextView avatar_photograph = (TextView) avatarView.findViewById(R.id.photograph);//take photo
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
                                // 待写------


                            }
                        });

                        avatar_cancel.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                dialogAvatar.dismiss();
                            }
                        });


                    }
                });

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
        ImageView stauts_avatar = getView().findViewById(R.id.avatar);

        status_firstname.setEnabled(false);
        status_surname.setEnabled(false);
        status_country.setEnabled(false);
        status_email.setEnabled(false);
        status_phone.setEnabled(false);
        stauts_avatar.setEnabled(false);

        if (currentUser != null) {
            // if logged in, query and render user information
            UserDBLogic userDBLogic = new UserDBLogic();
            userDBLogic.select(new OnCompleteListener<DataSnapshot>() {
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



                    } else {
                        System.out.println(task.getException().getMessage());
                    }
                }
            });

            userDBLogic.downloadAvatarInto(getContext(),stauts_avatar);



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
    // 设置bitmap 圆角
    public Bitmap bitmapRound(Bitmap mBitmap,float index){
        Bitmap bitmap = Bitmap.createBitmap(mBitmap.getWidth(), mBitmap.getHeight(), Bitmap.Config.ARGB_4444);

        Canvas canvas = new Canvas(bitmap);
        Paint paint = new Paint();
        paint.setAntiAlias(true);

        //设置矩形大小
        Rect rect = new Rect(0,0,mBitmap.getWidth(),mBitmap.getHeight());
        RectF rectf = new RectF(rect);

        // 相当于清屏
        canvas.drawARGB(0, 0, 0, 0);
        //画圆角
        canvas.drawRoundRect(rectf, index, index, paint);
        // 取两层绘制，显示上层
        paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_IN));

        // 把原生的图片放到这个画布上，使之带有画布的效果
        canvas.drawBitmap(mBitmap, rect, rect, paint);
        return bitmap;

    }
    // bitmap转为圆形
    protected Bitmap makeRoundCorner(Bitmap bitmap) {
        int width = bitmap.getWidth();
        int height = bitmap.getHeight();
        int left = 0, top = 0, right = width, bottom = height;
        float roundPx = height / 2;
        if (width > height) {
            left = (width - height) / 2;
            top = 0;
            right = left + height;
            bottom = height;
        } else if (height > width) {
            left = 0;
            top = (height - width) / 2;
            right = width;
            bottom = top + width;
            roundPx = width / 2;
        }

        Bitmap output = Bitmap.createBitmap(width, height,
                Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(output);
        int color = 0xff424242;
        Paint paint = new Paint();
        Rect rect = new Rect(left, top, right, bottom);
        RectF rectF = new RectF(rect);

        paint.setAntiAlias(true);
        canvas.drawARGB(0, 0, 0, 0);
        paint.setColor(color);
        canvas.drawRoundRect(rectF, roundPx, roundPx, paint);
        paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_IN));
        canvas.drawBitmap(bitmap, rect, rect, paint);
        return output;
    }

}
