package com.main.wayfinding.logic.db;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.FirebaseUserMetadata;
import com.google.firebase.database.DataSnapshot;

import org.junit.Test;

import java.util.concurrent.CountDownLatch;

/**
 * Test class for @{UserDBLogic.class}
 *
 * @author jia
 * @author Last Modified By jia
 * @version Revision: 0
 * Date: 2022/1/20 1:37
 */
public class UserDBLogicTest {

    UserDBLogic target = new UserDBLogic();

    @Test
    public final void userSelectTest01() throws Exception {

        // Thread lock
        final CountDownLatch mutex = new CountDownLatch(1);
        target.select(new OnCompleteListener<DataSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DataSnapshot> task) {

            }
        });
    }

}
