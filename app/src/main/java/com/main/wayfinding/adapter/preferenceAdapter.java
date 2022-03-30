package com.main.wayfinding.adapter;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Fragment;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import java.util.ArrayList;
import java.util.List;

import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.main.wayfinding.MainActivity;
import com.main.wayfinding.R;
import com.main.wayfinding.dto.LocationDto;
import com.main.wayfinding.fragment.map.MapFragment;
import com.main.wayfinding.logic.db.LocationDBLogic;
import com.squareup.picasso.Picasso;

import org.apache.commons.lang3.StringUtils;

/**
 * Define the fragment used for displaying map and dynamic Sustainable way-finding
 *
 * @author Sahil
 * @author Last Modified By AN
 * @version Revision: 1
 * Date: 2022/3/29 01:50
 */
public class preferenceAdapter extends RecyclerView.Adapter<preferenceAdapter.ViewHolder>
{
    public class ViewHolder extends RecyclerView.ViewHolder
    {

        public ImageView imageImageView;
        public TextView nameTextView;
        public TextView addressTextView;
        public FloatingActionButton navigateBtn;
        public FloatingActionButton deleteBtn;

        public ViewHolder(View itemView)
        {
            super(itemView);
            imageImageView = (ImageView) itemView.findViewById(R.id.image);
            nameTextView = (TextView) itemView.findViewById(R.id.textName);
            addressTextView = (TextView) itemView.findViewById(R.id.textAddress);
            navigateBtn = (FloatingActionButton) itemView.findViewById(R.id.preference_nav);
            deleteBtn = (FloatingActionButton) itemView.findViewById(R.id.preference_delete);
        }
    }
    private List<LocationDto> locationDtoList;
    private Context context;
    public FirebaseUser currentUser;
    private LocationDBLogic locationDBLogic;

    public preferenceAdapter() {
        locationDtoList = new ArrayList<LocationDto>();
        currentUser = FirebaseAuth.getInstance().getCurrentUser();
        locationDBLogic = new LocationDBLogic();
    }

    public void setLocationList(List<LocationDto> list)
    {
        locationDtoList = list;
    }

    @Override
    public preferenceAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType)
    {
        context = parent.getContext();
        LayoutInflater inflater = LayoutInflater.from(context);

        View RecentView = inflater.inflate(R.layout.preference_item, parent, false);
        ViewHolder viewHolder = new ViewHolder(RecentView);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(preferenceAdapter.ViewHolder holder, @SuppressLint("RecyclerView") int position)
    {
        LocationDto locationDto = locationDtoList.get(position);
        if (StringUtils.isNotEmpty(locationDto.getGmImgUrl())) {
            Picasso.with(context).load(locationDto.getGmImgUrl()).into(holder.imageImageView);
        }
        holder.nameTextView.setText(locationDto.getName());
        holder.addressTextView.setText(locationDto.getAddress());

        // Set click event for buttons.
        holder.navigateBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //get navigation controller in mainActivity
                NavController navController = Navigation
                        .findNavController((Activity) context, R.id.nav_host_fragment_activity_main);
                //package data
                Bundle bundle = new Bundle();
                bundle.putString("name",locationDto.getName());
                //switch to map fragment and post data
                navController.navigate(R.id.navigation_map,bundle);

            }
        });

        holder.deleteBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                locationDtoList.remove(position);
                notifyItemRemoved(position);
                notifyItemRangeChanged(position,locationDtoList.size());
                notifyItemChanged(position);
                if (currentUser != null) {
                    locationDBLogic.delete(locationDto.getName());
                }
            }
        });

    }
    @Override
    public int getItemCount()
    {
        return locationDtoList.size();
    }
}

