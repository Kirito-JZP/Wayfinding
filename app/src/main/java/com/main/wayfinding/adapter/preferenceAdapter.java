package com.main.wayfinding.adapter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.main.wayfinding.R;
import com.main.wayfinding.dto.LocationDto;
import com.squareup.picasso.Picasso;

import org.apache.commons.lang3.StringUtils;

public class preferenceAdapter extends RecyclerView.Adapter<preferenceAdapter.ViewHolder>
{
    public class ViewHolder extends RecyclerView.ViewHolder
    {

        public ImageView imageImageView;
        public TextView nameTextView;
        public TextView addressTextView;
        public TextView countryTextView;
        public FloatingActionButton floatingActionButton;

        public ViewHolder(View itemView)
        {
            super(itemView);
            imageImageView = (ImageView) itemView.findViewById(R.id.image);
            nameTextView = (TextView) itemView.findViewById(R.id.textName);
            addressTextView = (TextView) itemView.findViewById(R.id.textAddress);
            countryTextView = (TextView) itemView.findViewById(R.id.textCountry);
            floatingActionButton = (FloatingActionButton) itemView.findViewById(R.id.preference_delete);
        }
    }
    private List<LocationDto> mRecentlySaved;
    private Context context;

    public preferenceAdapter() {
        mRecentlySaved = new ArrayList<LocationDto>();
    }
    public void setLocationList(List<LocationDto> RecentlySaved)
    {
        mRecentlySaved = RecentlySaved;
    }

    @Override
    public preferenceAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType)
    {
        mRecentlySaved.sort((o1, o2)->o1.getDate().compareTo(o2.getDate()));
        context = parent.getContext();
        LayoutInflater inflater = LayoutInflater.from(context);

        View RecentView = inflater.inflate(R.layout.preference_item, parent, false);
        ViewHolder viewHolder = new ViewHolder(RecentView);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(preferenceAdapter.ViewHolder holder, @SuppressLint("RecyclerView") int position)
    {
        LocationDto recentlySaved = mRecentlySaved.get(position);
        if (StringUtils.isNotEmpty(recentlySaved.getGmImgUrl())) {
            Picasso.with(context).load(recentlySaved.getGmImgUrl()).into(holder.imageImageView);
        }
        TextView textView = holder.nameTextView;
        textView.setText(recentlySaved.getName());
        TextView textView2 = holder.addressTextView;
        textView2.setText(recentlySaved.getAddress());
        TextView textView3 = holder.countryTextView;
        textView3.setText(recentlySaved.getCountry());

        FloatingActionButton f1 = holder.floatingActionButton;

//        //delete cardview
//        holder.itemView.setOnLongClickListener(new View.OnLongClickListener() {
//            @Override
//            public boolean onLongClick(View view) {
//                mRecentlySaved.remove(holder.getAdapterPosition());
//                notifyItemRemoved(position);
//                return false;
//            }
//        });

        f1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mRecentlySaved.remove(position);
                notifyItemRemoved(position);
                notifyItemRangeChanged(position,mRecentlySaved.size());
                notifyItemChanged(position);
            }
        });




    }
    @Override
    public int getItemCount()
    {
        return mRecentlySaved.size();
    }
}

