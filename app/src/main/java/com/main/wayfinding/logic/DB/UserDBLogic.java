package com.main.wayfinding.logic.DB;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.main.wayfinding.dto.UserDto;

import java.util.HashMap;

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
//        this.storage = FirebaseStorage.getInstance("gs://wayfinding-55555.appspot.com");
        this.avatarFolder = FirebaseStorage.getInstance().getReference("avatar");
    }

    private String getUid(){
        FirebaseUser user = auth.getCurrentUser();
        if(user!=null){
            return user.getUid();
        }else{
            System.out.println("haven't log in");
            return null;
        }

    }

    public void insert(UserDto userDto){
        String uid = getUid();
        if (uid!=null){
            userNode.child(uid).setValue(userDto);
        }
    }

    public void select(OnCompleteListener<DataSnapshot> callback){
        String uid = getUid();
        if (uid!=null){
            userNode.child(uid).get().addOnCompleteListener(callback);
        }
    }

    public void update(UserDto userDto){
        insert(userDto);
    }

    public void uploadAvatar(){
        System.out.println(avatarFolder.getPath());
        System.out.println(avatarFolder.getName());
        System.out.println(avatarFolder.getBucket());
    }

}
