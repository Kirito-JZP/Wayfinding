package com.main.wayfinding.logic.db;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.main.wayfinding.dto.LocationDto;

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
        FirebaseDatabase database = FirebaseDatabase.getInstance("https://wayfinding-55555-default-rtdb.europe-west1.firebasedatabase.app/");
        this.locationNode = database.getReference("location");
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

    public void insert(LocationDto locationDto){
        String uid = getUid();
        if (uid!=null){
//            locationDto.setLocationId(locationNode.child(uid).push().getKey());
//            locationNode.child(uid).child(locationDto.getLocationId()).setValue(locationDto);
            locationNode.child(uid).child(locationDto.getName()).setValue(locationDto);
        }
    }

    public void select(OnCompleteListener<DataSnapshot> callback) {
        String uid = getUid();
        if (uid != null) {
            locationNode.child(uid).get().addOnCompleteListener(callback);
        }
    }

    public void delete(String locationID) {
        String uid = getUid();
        if (uid != null) {
            locationNode.child(uid).child(locationID).removeValue();
        }
    }
}
