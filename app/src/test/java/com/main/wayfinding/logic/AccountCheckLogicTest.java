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
    public void TestEvent1Case05checkPassword(){
        String password = "123";
        Assert.assertTrue(target.checkLength(password.length()));
        Assert.assertEquals("Password is too short!\n",target.getErrorMessage());
    }
    @Test
    public void TestEvent1Case06checkLogin(){
        String email = "tester@gmail.com";
        String password = "123456";
        Assert.assertFalse(target.isEmpty("Email",email));
        Assert.assertFalse(target.isEmpty("Password",password));
        Assert.assertFalse(target.checkLength(password.length()));
        Assert.assertEquals(null,target.getErrorMessage()); // Login successful
    }
    //Event2 Signup
    @Test
    public void TestEvent2Case01checkEmail(){
        String email = "";
        Assert.assertTrue(target.isEmpty("Email",email));
        Assert.assertEquals("Email is empty\n",target.getErrorMessage());
    }
    @Test
    public void TestEvent2Case02checkPassword(){
        String password = "";
        Assert.assertTrue(target.isEmpty("Password",password));
        Assert.assertEquals("Password is empty\n",target.getErrorMessage());
    }
    @Test
    public void TestEvent2Case03checkFirstName(){
        String firstname = "";
        Assert.assertTrue(target.isEmpty("First Name", firstname));
        Assert.assertEquals("First Name is empty\n",target.getErrorMessage());
    }
    @Test
    public void TestEvent2Case04checkSurname(){
        String surname = "";
        Assert.assertTrue(target.isEmpty("Surname", surname));
        Assert.assertEquals("Surname is empty\n",target.getErrorMessage());
    }
    @Test
    public void TestEvent2Case05checkCountry(){
        String country = "";
        Assert.assertTrue(target.isEmpty("Country", country));
        Assert.assertEquals("Country is empty\n",target.getErrorMessage());
    }
    @Test
    public void TestEvent2Case06checkPhoneNO(){
        String phone = "";
        Assert.assertTrue(target.isEmpty("Phone NO.", phone));
        Assert.assertEquals("Phone NO. is empty\n",target.getErrorMessage());
    }
    @Test
    public void TestEvent2Case07checkEmail(){
        String email = "tom@";
        Assert.assertTrue(target.checkEmail(email));
        Assert.assertEquals("Error Email Format!\n",target.getErrorMessage());
    }
    @Test
    public void TestEvent2Case08checkPassword(){
        String password = "123";
        Assert.assertTrue(target.checkLength(password.length()));
        Assert.assertEquals("Password is too short!\n",target.getErrorMessage());
    }
    @Test
    public void TestEvent2Case09checkSignup(){
        String email = "tester@gmail.com";
        String password = "123456";
        String firstname = "Harry";
        String surname = "Potter";
        String country = "Ireland";
        String phone = "0873337564";

        Assert.assertFalse(target.isEmpty("Email",email));
        Assert.assertFalse(target.isEmpty("Password",password));
        Assert.assertFalse(target.isEmpty("First Name", firstname));
        Assert.assertFalse(target.isEmpty("Surname", surname));
        Assert.assertFalse(target.isEmpty("Country", country));
        Assert.assertFalse(target.isEmpty("Phone NO.", phone));
        Assert.assertFalse(target.checkEmail(email));
        Assert.assertFalse(target.checkLength(password.length()));
        Assert.assertEquals(null,target.getErrorMessage()); // Sign in successful & logged
    }

    //Event3 Confirm edit
    @Test
    public void TestEvent3Case01checkFirstname(){
        String firstname = "";
        Assert.assertTrue(target.isEmpty("First Name", firstname));
        Assert.assertEquals("First Name is empty\n",target.getErrorMessage());
    }
    @Test
    public void TestEvent3Case02checkSurname(){
        String surname = "";
        Assert.assertTrue(target.isEmpty("Surname", surname));
        Assert.assertEquals("Surname is empty\n",target.getErrorMessage());
    }
    @Test
    public void TestEvent3Case03checkCountry(){
        String country = "";
        Assert.assertTrue(target.isEmpty("Country", country));
        Assert.assertEquals("Country is empty\n",target.getErrorMessage());
    }
    @Test
    public void TestEvent3Case04checkPhoneNO(){
        String phone = "";
        Assert.assertTrue(target.isEmpty("Phone NO.", phone));
        Assert.assertEquals("Phone NO. is empty\n",target.getErrorMessage());
    }
    @Test
    public void TestEvent3Case05checkEdit(){
        String firstname = "Harry";
        String surname = "Potter";
        String country = "Ireland";
        String phone = "0873337564";
        Assert.assertFalse(target.isEmpty("First Name", firstname));
        Assert.assertFalse(target.isEmpty("Surname", surname));
        Assert.assertFalse(target.isEmpty("Country", country));
        Assert.assertFalse(target.isEmpty("Phone NO.", phone));
        Assert.assertEquals(null,target.getErrorMessage()); // Edit successful
    }






}
