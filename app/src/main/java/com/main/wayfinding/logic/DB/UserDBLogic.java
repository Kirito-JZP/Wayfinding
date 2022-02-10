package com.main.wayfinding.logic.DB;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
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

    public UserDBLogic() {
        this.auth = FirebaseAuth.getInstance();
        FirebaseDatabase database = FirebaseDatabase.getInstance("https://wayfinding-90556-default-rtdb.europe-west1.firebasedatabase.app/");
        this.userNode = database.getReference("user");
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

}
