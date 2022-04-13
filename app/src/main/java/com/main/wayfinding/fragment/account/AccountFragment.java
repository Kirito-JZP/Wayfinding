package com.main.wayfinding.fragment.account;

import static com.main.wayfinding.utility.StaticStringUtils.*;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;;
import android.widget.Button;
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
import com.main.wayfinding.logic.AccountCheckLogic;
import com.main.wayfinding.logic.AuthLogic;
import com.main.wayfinding.logic.db.UserDBLogic;
import com.main.wayfinding.utility.NoticeUtils;
import com.main.wayfinding.utility.FileReaderUtils;
import com.main.wayfinding.utility.StaticStringUtils;

import org.apache.commons.lang3.StringUtils;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Objects;

/**
 * Define the fragment used for displaying and changing user info
 *
 * @author Gang
 * @author Last Modified By hu
 * @version Revision: 1
 * Date: 2022/3/26 21:58
 */

public class AccountFragment extends Fragment {
    private FragmentAccountBinding binding;
    private FirebaseAuth auth;
    private AuthLogic accountLogic;
    private ActivityResultLauncher<Intent> intentActivityResultLauncher;
    private Uri imageSelected;
    private boolean editing = false;
    private AccountCheckLogic accountCheckLogic;
    private UserDBLogic userDBLogic;
    private Button loginBtn;
    private Button signUpBtn;
    private Button editBtn;
    private Button signOutBtn;
    private Button backBtn;
    private Button confirmBtn;

    private enum BtnActions {
        LOGIN, EDIT, BACK, CONFIRM, SIGNOUT
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentAccountBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        auth = FirebaseAuth.getInstance();
        accountLogic = new AuthLogic();
        accountCheckLogic = new AccountCheckLogic();
        userDBLogic = new UserDBLogic();
        loginBtn = view.findViewById(R.id.login);
        signUpBtn = view.findViewById(R.id.sign_up);
        editBtn = view.findViewById(R.id.edit);
        signOutBtn = view.findViewById(R.id.sign_out);
        backBtn = view.findViewById(R.id.edit_back);
        confirmBtn = view.findViewById(R.id.edit_confirm);

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
                        ImageView imageView = getView().findViewById(R.id.avatar_login);
                        imageView.setImageBitmap(bitmap); // square image
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

        reload();
        view.findViewById(R.id.sign_up).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                View signUpView = View.inflate(getContext(), R.layout.fragment_accountcreate, null);
                AlertDialog dialogCreate = new AlertDialog.Builder(getActivity()).setView(signUpView).show();
                // set dialogue transparent
                WindowManager.LayoutParams lp = dialogCreate.getWindow().getAttributes();
                lp.alpha = 1.0f;
                dialogCreate.getWindow().setAttributes(lp);
                // protocol
                TextView protocol = signUpView.findViewById(R.id.protocol);
                protocol.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        String str = FileReaderUtils.initAssets(getContext(), PRIVACY_TXT);
                        final View inflate = LayoutInflater.from(getActivity()).inflate(R.layout.dialog_protocol, null);
                        TextView tv_title = (TextView) inflate.findViewById(R.id.tv_title);
                        tv_title.setText(PRIVACY_TITLE);
                        TextView tv_content = (TextView) inflate.findViewById(R.id.tv_content);
                        tv_content.setText(str);
                        final AlertDialog dialog = new AlertDialog
                                .Builder(getActivity())
                                .setView(inflate)
                                .show();
                        final WindowManager.LayoutParams params = dialog.getWindow().getAttributes();
                        params.width = 800;
                        params.height = 1200;
                        dialog.getWindow().setAttributes(params);
                        dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
                    }
                });

                // Signup
                signUpView.findViewById(R.id.create_account).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        EditText emailSignup = signUpView.findViewById(R.id.email_signup);
                        EditText passwordSignup = signUpView.findViewById(R.id.password_signup);
                        EditText firstNameSignup = signUpView.findViewById(R.id.first_name_signup);
                        EditText surnameSignup = signUpView.findViewById(R.id.surname_signup);
                        EditText countrySignup = signUpView.findViewById(R.id.country_signup);
                        EditText phoneNoSignup = signUpView.findViewById(R.id.phone_no_signup);
                        String email = emailSignup.getText().toString();
                        String password = passwordSignup.getText().toString();
                        UserDto userDto = new UserDto(
                                firstNameSignup.getText().toString(),
                                surnameSignup.getText().toString(),
                                countrySignup.getText().toString(),
                                phoneNoSignup.getText().toString());
                        //get checkbox
                        CheckBox checkbox = signUpView.findViewById(R.id.checkBox);

                        // check dataFormat
                        String errorMsg = NULL_STRING;
                        if (accountCheckLogic.isEmpty(getString(R.string.email), email)) {
                            errorMsg += accountCheckLogic.getErrorMessage();
                            System.out.println(errorMsg);
                        } else if (accountCheckLogic.checkEmail(email)) {
                            errorMsg += accountCheckLogic.getErrorMessage();
                        }

                        if (accountCheckLogic.isEmpty(getString(R.string.password), password)) {
                            errorMsg += accountCheckLogic.getErrorMessage();
                        } else if (accountCheckLogic.checkLength(password.length())) {
                            errorMsg += accountCheckLogic.getErrorMessage();
                        }

                        if (accountCheckLogic.isEmpty(getString(R.string.first_name), userDto.getFirstName())) {
                            errorMsg += accountCheckLogic.getErrorMessage();
                        }
                        if (accountCheckLogic.isEmpty(getString(R.string.surname), userDto.getSurname())) {
                            errorMsg += accountCheckLogic.getErrorMessage();
                        }
                        if (accountCheckLogic.isEmpty(getString(R.string.country), userDto.getCountry())) {
                            errorMsg += accountCheckLogic.getErrorMessage();
                        }
                        if (accountCheckLogic.isEmpty(getString(R.string.phone_number), userDto.getPhoneNumber())) {
                            errorMsg += accountCheckLogic.getErrorMessage();
                        }
                        if (!checkbox.isChecked()) {
                            errorMsg += TERMS_AGREE_MSG;
                        }


                        if (StringUtils.isNotEmpty(errorMsg)) {
                            NoticeUtils.createAlertDialog(getContext(), errorMsg);
                        } else {
                            //signup
                            accountLogic.signUp(email, password, new OnCompleteListener<AuthResult>() {
                                @Override
                                public void onComplete(@NonNull Task<AuthResult> task) {
                                    if (task.isSuccessful()) {
                                        userDBLogic.insert(userDto);
                                        dialogCreate.dismiss();
                                        reload();
                                    } else {
                                        if ("The email address is already in use by another account"
                                                .equals(Objects.requireNonNull(task.getException()).toString())) {
                                            NoticeUtils.createAlertDialog(getContext(), SIGNUP_ERROR);
                                        } else {
                                            NoticeUtils.createAlertDialog(getContext(), ERROR);
                                        }
                                    }
                                }
                            });
                        }
                    }
                });
            }
        });

        view.findViewById(R.id.login).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                View loginView = View.inflate(getContext(), R.layout.fragment_accountlogin, null);
                AlertDialog dialogLogin = new AlertDialog.Builder(getActivity()).setView(loginView).show();
                loginView.findViewById(R.id.confirm).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        EditText usernameLogin = loginView.findViewById(R.id.email_login);
                        EditText passwordLogin = loginView.findViewById(R.id.password_login);
                        String email = usernameLogin.getText().toString();
                        String password = passwordLogin.getText().toString();

                        String errorMsg = NULL_STRING;
                        if (accountCheckLogic.isEmpty(getString(R.string.email), email)) {
                            errorMsg += accountCheckLogic.getErrorMessage();
                            System.out.println(errorMsg);
                        } else if (accountCheckLogic.checkEmail(email)) {
                            errorMsg += accountCheckLogic.getErrorMessage();
                        }

                        if (accountCheckLogic.isEmpty(getString(R.string.password), password)) {
                            errorMsg += accountCheckLogic.getErrorMessage();
                        }

                        if (StringUtils.isNotEmpty(errorMsg)) {
                            NoticeUtils.createAlertDialog(getContext(), errorMsg);
                        } else {
                            accountLogic.login(email, password, new OnCompleteListener<AuthResult>() {
                                @Override
                                public void onComplete(@NonNull Task<AuthResult> task) {
                                    if (task.isSuccessful()) {
                                        dialogLogin.dismiss();
                                        reload();
                                    } else {
                                        //Event1Case05
                                        String msg = task.getException().getMessage();
                                        System.out.println(msg);
                                        if (StringUtils.equals(msg, AUTHORITY_FAIL_STRING)) {
                                            NoticeUtils.createAlertDialog(getContext(), AUTHORITY_FAIL_MSG);
                                        }
                                    }
                                }
                            });
                        }

                    }
                });
                loginView.findViewById(R.id.forget).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        // ForgetPassword and Reset()
                        EditText usernameLogin = loginView.findViewById(R.id.email_login);
                        String email = usernameLogin.getText().toString();
                        String errorMsg = NULL_STRING;
                        if (accountCheckLogic.isEmpty(getString(R.string.email), email)) {
                            errorMsg += accountCheckLogic.getErrorMessage();
                            NoticeUtils.createAlertDialog(getContext(), errorMsg);
                        } else {
                            // reset()
                            accountLogic.resetPassword(email, new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    if (task.isSuccessful()) {
                                        NoticeUtils.createAlertDialog(getContext(), EMAIL_SENT);
                                    } else {
                                        if ("no user record corresponding to this identifier".equals(Objects.requireNonNull(task.getException()).toString())) {
                                            NoticeUtils.createAlertDialog(getContext(), NO_ACCOUNT_FOUND);
                                        }
                                    }
                                }
                            });
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
                View editView = getView();
                EditText firstname = editView.findViewById(R.id.first_name);
                EditText surname = editView.findViewById(R.id.surname);
                EditText country = editView.findViewById(R.id.country);
                EditText phone = editView.findViewById(R.id.phone_no);
                ImageView avatar = editView.findViewById(R.id.avatar_login);

                //enable edit state
                firstname.setEnabled(true);
                surname.setEnabled(true);
                country.setEnabled(true);
                phone.setEnabled(true);
                avatar.setEnabled(true);

                // set color black
                firstname.setTextColor(Color.BLACK);
                surname.setTextColor(Color.BLACK);
                country.setTextColor(Color.BLACK);
                phone.setTextColor(Color.BLACK);

                //Btn
                buttonActions(BtnActions.EDIT);

                // set backBtn ClickActions.
                editView.findViewById(R.id.edit_back).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        //Btn
                        buttonActions(BtnActions.BACK);
                        editing = false;
                        reload();
                    }
                });

                editView.findViewById(R.id.edit_confirm).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        UserDto userDto = new UserDto();
                        userDto.setFisrtName(firstname.getText().toString());
                        userDto.setSurname(surname.getText().toString());
                        userDto.setCountry(country.getText().toString());
                        userDto.setPhoneNumber(phone.getText().toString());

                        //editView check data
                        String errorMsg = NULL_STRING;
                        if (accountCheckLogic.isEmpty(getString(R.string.first_name), firstname.getText().toString())) {
                            errorMsg += accountCheckLogic.getErrorMessage();
                            System.out.println(errorMsg);
                        }
                        if (accountCheckLogic.isEmpty(getString(R.string.surname), surname.getText().toString())) {
                            errorMsg += accountCheckLogic.getErrorMessage();
                        }
                        if (accountCheckLogic.isEmpty(getString(R.string.country), country.getText().toString())) {
                            errorMsg += accountCheckLogic.getErrorMessage();
                        }
                        if (accountCheckLogic.isEmpty(getString(R.string.phone_number), phone.getText().toString())) {
                            errorMsg += accountCheckLogic.getErrorMessage();
                        }
                        if (StringUtils.isNotEmpty(errorMsg)) {
                            NoticeUtils.createAlertDialog(getContext(), errorMsg);
                        } else {
                            // upgrade data
                            userDBLogic.update(userDto);
                            //Btn
                            buttonActions(BtnActions.CONFIRM);
                            //upload avatar image
                            if (imageSelected != null) {
                                try {
                                    InputStream is = requireActivity().getContentResolver().openInputStream(imageSelected);
                                    userDBLogic.uploadAvatar(is, new OnCompleteListener<UploadTask.TaskSnapshot>() {
                                        @Override
                                        public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                                            imageSelected = null;
                                        }
                                    });
                                } catch (FileNotFoundException e) {
                                    e.printStackTrace();
                                }
                            }
                            editing = false;
                            reload();
                        }

                    }
                });

                // edit avatar
                avatar.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        View avatarView = View.inflate(getContext(), R.layout.fragment_avatar, null);
                        AlertDialog dialogAvatar = new AlertDialog.Builder(getActivity()).setView(avatarView).show();
                        TextView avatarPhoto = (TextView) avatarView.findViewById(R.id.album);//album
                        TextView avatarCancel = (TextView) avatarView.findViewById(R.id.cancel);

                        avatarPhoto.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                Intent album = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                                intentActivityResultLauncher.launch(album);
                                dialogAvatar.dismiss();
                            }
                        });

                        avatarCancel.setOnClickListener(new View.OnClickListener() {
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
    public void reload() {
        FirebaseUser currentUser = auth.getCurrentUser();
        View view = requireView();
        ImageView avatar = view.findViewById(R.id.avatar_login);
        EditText email = view.findViewById(R.id.email);
        EditText firstName = view.findViewById(R.id.first_name);
        EditText surname = view.findViewById(R.id.surname);
        EditText country = view.findViewById(R.id.country);
        EditText phoneNo = view.findViewById(R.id.phone_no);

        if (currentUser != null) {
            // if logged in, query and render user information
            if (!editing) {
                firstName.setEnabled(false);
                surname.setEnabled(false);
                country.setEnabled(false);
                email.setEnabled(false);
                phoneNo.setEnabled(false);
                avatar.setEnabled(false);
                //set color gray
                firstName.setTextColor(Color.GRAY);
                surname.setTextColor(Color.GRAY);
                country.setTextColor(Color.GRAY);
                phoneNo.setTextColor(Color.GRAY);
            }
            //Btn
            buttonActions(BtnActions.LOGIN);

            userDBLogic.select(new OnCompleteListener<DataSnapshot>() {
                @Override
                public void onComplete(@NonNull Task<DataSnapshot> task) {
                    if (task.isSuccessful()) {
                        UserDto userDto = task.getResult().getValue(UserDto.class);
                        // output to layout
                        firstName.setText(userDto.getFirstName());
                        surname.setText(userDto.getSurname());
                        country.setText(userDto.getCountry());
                        email.setText(currentUser.getEmail());
                        phoneNo.setText(userDto.getPhoneNumber());
                    } else {
                        System.out.println(task.getException().getMessage());
                    }
                }
            });
            userDBLogic.downloadAvatarInto(getContext(), avatar);

        } else {
            // currentUser == null
            firstName.setText(getString(R.string.first_name));
            surname.setText(getString(R.string.surname));
            country.setText(getString(R.string.country));
            email.setText(getString(R.string.email));
            phoneNo.setText(getString(R.string.phone_number));

            //Btn
            buttonActions(BtnActions.SIGNOUT);
            //set default avatar
            avatar.setImageResource(R.drawable.ic_avatar_white);
        }
    }

    public void buttonActions(BtnActions btnActions) {
        switch (btnActions) {
            case LOGIN:
                // show editBtn signoutBtn
                signOutBtn.setVisibility(View.VISIBLE);
                editBtn.setVisibility(View.VISIBLE);
                // hide loginBtn和sinupBtn
                loginBtn.setVisibility(View.GONE);
                signUpBtn.setVisibility(View.GONE);
                break;
            case SIGNOUT:
                // show loginBtn & sinupBtn
                loginBtn.setVisibility(View.VISIBLE);
                signUpBtn.setVisibility(View.VISIBLE);
                // hide editBtn & signoutBtn
                signOutBtn.setVisibility(View.GONE);
                editBtn.setVisibility(View.GONE);
                break;
            case BACK:
            case CONFIRM:
                // show editBtn & signoutBtn
                signOutBtn.setVisibility(View.VISIBLE);
                editBtn.setVisibility(View.VISIBLE);
                //hide backBtn confirmBtn
                backBtn.setVisibility(View.GONE);
                confirmBtn.setVisibility(View.GONE);
                break;
            case EDIT:
                //show backBtn confirmBtn
                backBtn.setVisibility(View.VISIBLE);
                confirmBtn.setVisibility(View.VISIBLE);
                // hide editBtn signoutBtn
                editBtn.setVisibility(View.GONE);
                signOutBtn.setVisibility(View.GONE);
                break;
        }
    }

}
