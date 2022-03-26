package com.main.wayfinding.logic;

import org.junit.Assert;
import org.junit.Test;

/**
 * Test class for @{AccountCheckLogic}
 *
 * @author Jia
 * @author Last Modified By Jia
 * @version Revision: 0
 * Date: 2022/3/26 13:49
 */
public class AccountCheckLogicTest {

    private AccountCheckLogic target = new AccountCheckLogic();

    @Test
    public void checkMailTestCase01(){
        String mail = "test@gmail.com";
        Assert.assertTrue(target.checkEmail(mail));
    }
}
