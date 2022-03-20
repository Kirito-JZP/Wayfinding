package com.main.wayfinding.logic.DB;

import android.content.Context;
import android.net.Uri;
import android.widget.ImageView;

import androidx.annotation.NonNull;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageException;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.main.wayfinding.dto.UserDto;

import java.io.IOException;
import java.io.InputStream;

/**
 * Database operation for user node
 *
 * @author Gang
 * @author Last Modified By Gang
 * @version Revision: 0
 * Date: 2022/2/08 19:49
 */
public class UserDBLogic {
    private FirebaseAuth auth;
    private DatabaseReference userNode;
    private StorageReference avatarFolder;

    public UserDBLogic() {
        this.auth = FirebaseAuth.getInstance();
        FirebaseDatabase database = FirebaseDatabase.getInstance("https://wayfinding-55555-default-rtdb.europe-west1.firebasedatabase.app/");
        this.userNode = database.getReference("user");
        this.avatarFolder = FirebaseStorage.getInstance().getReference("avatar");
    }

    private String getUid() {
        FirebaseUser user = auth.getCurrentUser();
        if (user != null) {
            return user.getUid();
        } else {
            System.out.println("haven't log in");
            return null;
        }

    }

    public void insert(UserDto userDto) {
        String uid = getUid();
        if (uid != null) {
            userNode.child(uid).setValue(userDto);
        }
    }

    public void select(OnCompleteListener<DataSnapshot> callback) {
        String uid = getUid();
        if (uid != null) {
            userNode.child(uid).get().addOnCompleteListener(callback);
        }
    }

    public void update(UserDto userDto) {
        insert(userDto);
    }

    public void uploadAvatar(InputStream avatarInputStream, OnCompleteListener<UploadTask.TaskSnapshot> callback) {
        String uid = getUid();
        if (uid != null) {
            avatarFolder.child(uid).putStream(avatarInputStream).addOnCompleteListener(callback);
        }
    }

    public void downloadAvatarInto(Context context, ImageView imageView) {
        String uid = getUid();
        if (uid != null) {
            avatarFolder.child(uid).getDownloadUrl().addOnCompleteListener(new OnCompleteListener<Uri>() {
                @Override
                public void onComplete(@NonNull Task<Uri> task) {

                    if (task.isSuccessful()) {
                        Glide.with(context).load(task.getResult()).into(imageView);
                    }
                }
            });

        }
    }

}
