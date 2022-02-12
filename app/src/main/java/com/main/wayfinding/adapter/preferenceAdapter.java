package com.main.wayfinding.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import java.util.List;

import androidx.recyclerview.widget.RecyclerView;

import com.main.wayfinding.R;
import com.main.wayfinding.dto.LocationDto;

public class preferenceAdapter extends RecyclerView.Adapter<preferenceAdapter.ViewHolder>
{
    public class ViewHolder extends RecyclerView.ViewHolder
    {
        public TextView nameTextView;
        public TextView addressTextView;
        public TextView countryTextView;

        public ViewHolder(View itemView)
        {
            super(itemView);
            nameTextView = (TextView) itemView.findViewById(R.id.textName);
            addressTextView = (TextView) itemView.findViewById(R.id.textAddress);
            countryTextView = (TextView) itemView.findViewById(R.id.textCountry);
        }
    }
    private List<LocationDto> mRecentlySaved;

    public preferenceAdapter(List<LocationDto> RecentlySaved)
    {
        mRecentlySaved = RecentlySaved;
    }
    @Override
    public preferenceAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType)
    {
        Context context = parent.getContext();
        LayoutInflater inflater = LayoutInflater.from(context);

        View RecentView = inflater.inflate(R.layout.preference_item, parent, false);
        ViewHolder viewHolder = new ViewHolder(RecentView);
        return viewHolder;
    }
    @Override
    public void onBindViewHolder(preferenceAdapter.ViewHolder holder, int position)
    {
        LocationDto recentlySaved = mRecentlySaved.get(position);

        TextView textView = holder.nameTextView;
        textView.setText(recentlySaved.getName());
        TextView textView2 = holder.addressTextView;
        textView2.setText(recentlySaved.getAddress());
        TextView textView3 = holder.countryTextView;
        textView3.setText(recentlySaved.getCountry());
    }
    @Override
    public int getItemCount()
    {
        return mRecentlySaved.size();
    }
}

