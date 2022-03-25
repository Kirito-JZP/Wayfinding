package com.main.wayfinding.AuthLogic;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.database.DataSnapshot;
import com.main.wayfinding.logic.AuthLogic;

import org.junit.Assert;
import org.junit.Test;

import java.util.concurrent.CountDownLatch;

/**
 * Description
 *
 * @author jia
 * @author Last Modified By jia
 * @version Revision: 0
 * Date: 2022/3/25 15:30
 */
public class AuthLogicTest {

    AuthLogic target = new AuthLogic();

    @Test
    public final void loginTest01() throws Exception {

        boolean result;

        // Thread lock
        ///final CountDownLatch mutex = new CountDownLatch(1);
        target.login("tester@gmail.com", "test123", new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                Assert.assertTrue(task.isSuccessful());
            }
        });
    }
}
