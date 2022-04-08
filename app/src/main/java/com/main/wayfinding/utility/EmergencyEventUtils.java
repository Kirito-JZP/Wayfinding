package com.main.wayfinding.utility;

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
    public static EmergencyEventDto generateEmergencyEvent(GoogleMap map, RouteDto route) {
        // code use to add disaster even (define disaster code "A" "B" "C" ...)
        EmergencyEventDto emergencyEventDto = new EmergencyEventDto();
        emergencyEventDto.setCode("A");
        List<RouteDto.RouteStep> steps = route.getSteps();
        int currentIndex = route.getCurrentStepIndex();
        RouteDto.RouteStep step;
        step = steps.get(currentIndex < steps.size() ? ++currentIndex : currentIndex);
        emergencyEventDto.setLatitude(step.getStartLocation().getLatitude());
        emergencyEventDto.setLongitude(step.getStartLocation().getLongitude());
        emergencyEventDto.setRadius(100);
        emergencyEventDto.setType("Car Accident");
        emergencyEventDto.setStartTime(LocalTime.of(16, 0));
        emergencyEventDto.setEndTime(LocalTime.of(19, 0));
//        new DisasterDBLogic().insert(emergencyEventDto);
        map.addCircle(new CircleOptions().center(new LatLng(emergencyEventDto.getLatitude(),
                emergencyEventDto.getLongitude()))
                .radius(emergencyEventDto.getRadius())
                .fillColor(0x7F7F7F7F)
                .strokeWidth(0.0F));
        return emergencyEventDto;
    }
}
