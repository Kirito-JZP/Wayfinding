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
    public static LocalTime convertToLocalTime(String time) {
        String[] split = time.split(":");
        return LocalTime.of(Integer.parseInt(split[0]), Integer.parseInt(split[1]));
    }
}
