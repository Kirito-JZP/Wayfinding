package com.main.wayfinding.utility;

import com.main.wayfinding.dto.EmergencyEventDto;

public class StaticStringUtils {
    public static final String NULL_STRING = "";
    public static final String PRIVACY_TITLE = "Privacy Protocol";
    public static final String PRIVACY_TXT = "privacy.txt";
    // Dialog Message
    public static final String TERMS_AGREE_MSG = "Please agree with terms";
    public static final String INCORRECT_EMAIL_FORMAT = "Incorrect Email Format!\n";
    public static final String EMPTY_STRING = " is empty\n";
    public static final String INCORRECT_PASSWORD_FORMAT = "Incorrect Password Format!\n";
    public static final String AUTHORITY_FAIL_STRING = "The password is invalid or the user does " +
            "not have a password.";
    public static final String AUTHORITY_FAIL_MSG = "Incorrect username or password!";
    public static final String ADD_SUCCESS_MSG = "Added a destination!";
    public static final String NO_INPUT_MSG = "No inputs in destination!";
    public static final String GET_LATEST_LOCATION_FAIL = "Unable to get the latest location";
    public static final String START_NAVIGATION = "Navigation started";
    public static final String RESUME_NAVIGATION = "Navigation resumed";
    public static final String PAUSE_NAVIGATION = "Navigation paused";
    public static final String STOP_NAVIGATION = "Navigation stopped";
    public static final String NO_AVAILABLE_ROUTE = "No routes found for selected transporation " +
            "means";
    public static final String NO_ACCOUNT_FOUND = "We couldn't find an account with that email " +
            "address.";
    public static final String EMAIL_SENT = "Email sent!";
    public static final String displayEmergencyEvent(EmergencyEventDto event) {
        return "Emergency event: " + event.getType() + " centered at (" + event.getLatitude() +
                ", " + event.getLongitude() + "). Estimated radius of affected area :" + (int) event.getRadius() +
                "m. Estimated duration: until " + event.getEndTime();
    }
}
