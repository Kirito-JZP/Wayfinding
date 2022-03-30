package com.main.wayfinding.logic;

import org.junit.Assert;
import org.junit.Test;

/**
 * Description Unit Test AccountCheckLogic
 * Event02
 *
 * @author Hu
 * @author Last Modified By Hu
 * version Revision:0
 * Date:2022/3/30 21:11
 */
public class AccountCheckLogicEvent03Test {
    private AccountCheckLogic target = new AccountCheckLogic();
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
