package com.main.wayfinding.logic.db;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.main.wayfinding.dto.EmergencyEventDto;

public class DisasterDBLogic {

    private DatabaseReference disasterNode;

    public DisasterDBLogic() {
        FirebaseDatabase database = FirebaseDatabase.getInstance("https://wayfinding-55555-default-rtdb.europe-west1.firebasedatabase.app/");
        this.disasterNode = database.getReference("disaster");
    }


    public void insert(EmergencyEventDto emergencyEventDto) {
        disasterNode.child(emergencyEventDto.getCode()).setValue(emergencyEventDto);
    }

    public void select(String disasterCode,OnCompleteListener<DataSnapshot> callback) {
        disasterNode.child(disasterCode).get().addOnCompleteListener(callback);
    }
}
