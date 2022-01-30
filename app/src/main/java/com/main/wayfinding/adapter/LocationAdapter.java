package com.main.wayfinding.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.main.wayfinding.R;
import com.main.wayfinding.dto.LocationDto;

import java.util.List;

/**
 * Description
 *
 * @author Zehua Guo
 * @author Last Modified By Zehua Guo
 * @version Revision: 0
 * Date: 2022/1/30 18:02
 */
public class LocationAdapter extends ArrayAdapter<LocationDto> {
    private int resourceID;

    public LocationAdapter(@NonNull Context context, int resource, @NonNull List<LocationDto> objects) {
        super(context, resource, objects);
        resourceID = resource;
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        LocationDto location = getItem(position);
        View view = LayoutInflater.from(getContext()).inflate(resourceID, parent, false);
        TextView name = view.findViewById(R.id.autocomplete_location_name);
        TextView address = view.findViewById(R.id.autocomplete_location_address);
        name.setText(location.getName());
        address.setText(location.getAddress());
        return view;
    }
}
