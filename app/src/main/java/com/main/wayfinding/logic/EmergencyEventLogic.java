package com.main.wayfinding.logic;

import com.main.wayfinding.dto.EmergencyEventDto;

import java.util.HashMap;
import java.util.Map;

/**
 * Description
 *
 * @author Zehua Guo
 * @author Last Modified By Zehua Guo
 * @version Revision: 0
 * Date: 2022/3/31 17:17
 */
public class EmergencyEventLogic {
    public interface EmergencyEventCallback {
        void onEmergencyEventHappen(EmergencyEventDto event);
    }

    private static EmergencyEventLogic instance;
    private static int emergencyEventCallbackNum = 0;
    private static final Map<Integer, EmergencyEventCallback> emergencyEventCallbacks = new HashMap<>();

    public static int registerEmergencyEvent(EmergencyEventCallback callback) {
        emergencyEventCallbacks.put(emergencyEventCallbackNum, callback);
        emergencyEventCallbackNum += 1;
        return emergencyEventCallbackNum - 1;
    }

    public static void unregisterEmergencyEvent(int callbackNumber) {
        emergencyEventCallbacks.remove(callbackNumber);
        emergencyEventCallbackNum -= 1;
    }

    private static void broadcast(EmergencyEventDto event) {
        for (EmergencyEventCallback callback : emergencyEventCallbacks.values()) {
            callback.onEmergencyEventHappen(event);
        }
    }

    private static void listenOnNewEmergencyEvents() {
        // TODO: complete the logic. Change to a singleton if static methods fail to meet the demand
        // broadcast(...);
    }
}
