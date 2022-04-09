package com.main.wayfinding.utility;

import android.util.Log;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.main.wayfinding.dto.EmergencyEventDto;
import com.main.wayfinding.dto.RouteDto;
import com.main.wayfinding.logic.db.DisasterDBLogic;

import java.time.LocalTime;
import java.util.List;

/**
 * Description
 *
 * @author Zehua Guo
 * @author Last Modified By Zehua Guo
 * @version Revision: 0
 * Date: 2022/4/7 17:30
 */
public class EmergencyEventUtils {
    public static EmergencyEventDto generateEmergencyEvent(RouteDto route) {
        // code use to add disaster even (define disaster code "A" "B" "C" ...)
        EmergencyEventDto emergencyEventDto = new EmergencyEventDto();
//        List<RouteDto.RouteStep> steps = route.getSteps();
//        RouteDto.RouteStep step;
//        step = steps.get(Math.min(steps.size() - 10, steps.size() - 1));
//        emergencyEventDto.setLatitude(step.getStartLocation().getLatitude());
//        emergencyEventDto.setLongitude(step.getStartLocation().getLongitude());

//        emergencyEventDto.setCode("1");
//        emergencyEventDto.setLatitude(53.3538627);
//        emergencyEventDto.setLongitude(-6.2585369);
//        emergencyEventDto.setRadius(100);
//        emergencyEventDto.setType("Car Accident");
//        emergencyEventDto.setStartTime("16:00");
//        emergencyEventDto.setEndTime("19:00");

//        emergencyEventDto.setCode("2");
//        emergencyEventDto.setLatitude(53.3442178);
//        emergencyEventDto.setLongitude(-6.2765006);
//        emergencyEventDto.setRadius(100);
//        emergencyEventDto.setType("Road Maintenance");
//        emergencyEventDto.setStartTime("13:00");
//        emergencyEventDto.setEndTime("13:25");
//
        emergencyEventDto.setCode("3");
        emergencyEventDto.setLatitude(53.3593287);
        emergencyEventDto.setLongitude(-6.2587037);
        emergencyEventDto.setRadius(1000);
        emergencyEventDto.setType("Extreme Weather");
        emergencyEventDto.setStartTime("8:43");
        emergencyEventDto.setEndTime("8:46");
        new DisasterDBLogic().insert(emergencyEventDto);
        return emergencyEventDto;
    }

    public static LocalTime convertToLocalTime(String time) {
        String[] split = time.split(":");
        return LocalTime.of(Integer.parseInt(split[0]), Integer.parseInt(split[1]));
    }
}
