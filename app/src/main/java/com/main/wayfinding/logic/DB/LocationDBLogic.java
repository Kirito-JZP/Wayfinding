package com.main.wayfinding.logic.DB;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

/**
 * Database operation for location node
 *
 * @author Gang
 * @author Last Modified By Gang
 * @version Revision: 0
 * Date: 2022/02/08 19:48
 */
public class LocationDBLogic {
    private FirebaseAuth auth;
    private DatabaseReference locationNode;

    public LocationDBLogic() {
        this.auth = FirebaseAuth.getInstance();
        FirebaseDatabase database = FirebaseDatabase.getInstance("https://wayfinding-90556-default-rtdb.europe-west1.firebasedatabase.app/");
        this.locationNode = database.getReference("location");
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

    public void insert(){


    }

}
