package com.main.wayfinding.logic;

import org.apache.commons.lang3.StringUtils;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class AccountCheckLogic {

    private String errorMessage;

    public String getErrorMessage() {
        return errorMessage;
    }

    public boolean checkEmail(String email) {
        Pattern pattern = Pattern.compile("\\w+([-+.]\\w+)*@\\w+([-.]\\w+)*\\.\\w+([-.]\\w+)*");
        Matcher mc = pattern.matcher(email);
        if (!mc.matches()) {
            this.errorMessage = "Error Email Format!\n";
            return true;
        } else {
            return false;
        }
    }

    public boolean isEmpty(String key, String str) {
        if (StringUtils.isEmpty(str)) {
            this.errorMessage = key + " is empty\n";
            return true;
        } else {
            return false;
        }
    }

    public boolean checkLength(int n){
        if(n > 0 && n < 6){
            this.errorMessage = "Password is too short!\n";
            return true;
        }else {
            return false;
        }
    }


}
