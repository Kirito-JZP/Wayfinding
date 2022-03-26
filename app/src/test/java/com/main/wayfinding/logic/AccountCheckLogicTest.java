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
    public void TestEvent1Case03checkEmail(){
        String email = "tom@Asgard";
        Assert.assertFalse(target.checkEmail(email));
    }
    @Test
    public void TestEvent1Case04checkEmail(){
        String email = "tom555";
        Assert.assertFalse(target.checkEmail(email));
    }
    @Test
    public void TestEvent1Case05checkPassword(){
        String password = "123";
        Assert.assertTrue(target.checkLength(password.length()));
    }
    @Test
    public void TestEvent2Case07checkEmail(){
        String email = "tom@";
        Assert.assertFalse(target.checkEmail(email));
    }
    @Test
    public void TestEvent2Case09checkEmail(){
        String email = "tester@gmail.com";
        Assert.assertTrue(target.checkEmail(email));
    }
}
