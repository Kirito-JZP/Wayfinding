package com.main.wayfinding.logic.DB;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

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

    public LocationDBLogic() {
        this.auth = FirebaseAuth.getInstance();
    }

    public void insert(){
        FirebaseUser user = auth.getCurrentUser();
        if(user==null){
            System.out.println("haven't log in");
            return;
        }
        System.out.println(user.getUid());

    }


}
