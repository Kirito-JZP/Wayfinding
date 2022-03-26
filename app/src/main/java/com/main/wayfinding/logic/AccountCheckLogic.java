package com.main.wayfinding.logic;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class AccountCheckLogic {
    private String errorMessage;
    private boolean isSuccess;

    public boolean isEmail(String strEmail) {
        Pattern pattern = Pattern.compile("\\w+([-+.]\\w+)*@\\w+([-.]\\w+)*\\.\\w+([-.]\\w+)*");
        Matcher mc = pattern.matcher(strEmail);
        return mc.matches();
    }
}
