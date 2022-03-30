package com.main.wayfinding.logic;

import org.junit.Assert;
import org.junit.Test;

/**
 * Description Unit Test AccountCheckLogic
 * Event01
 *
 * @author HU
 * @author Last Modified By HU
 * version Revision:0
 * Date:2022/3/30 19:30
 */
public class AccountCheckLogicEvent01Test {
    private AccountCheckLogic target = new AccountCheckLogic();

    //Event1 Login
    @Test
    public void TestEvent1Case01checkEmail(){
        String email = "";
        String key = "Email";
        Assert.assertTrue(target.isEmpty(key,email));
        Assert.assertEquals("Email is empty\n",target.getErrorMessage());
    }
    @Test
    public void TestEvent1Case02checkPassword(){
        String key = "Password";
        String password = "";
        Assert.assertTrue(target.isEmpty(key,password));
        Assert.assertEquals("Password is empty\n",target.getErrorMessage());
    }
    @Test
    public void TestEvent1Case03checkEmail(){
        String email = "tom@Asgard";
        Assert.assertTrue(target.checkEmail(email));
        Assert.assertEquals("Error Email Format!\n",target.getErrorMessage());
    }
    @Test
    public void TestEvent1Case04checkEmail(){
        String email = "tom555";
        Assert.assertTrue(target.checkEmail(email));
        Assert.assertEquals("Error Email Format!\n",target.getErrorMessage());
    }
    @Test
    public void TestEvent1Case06checkLogin(){
        String email = "tester@gmail.com";
        String password = "123456";
        Assert.assertFalse(target.isEmpty("Email",email));
        Assert.assertFalse(target.isEmpty("Password",password));
        Assert.assertEquals(null,target.getErrorMessage()); // Login successful
    }
}
