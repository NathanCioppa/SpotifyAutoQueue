package com.example.spotifyautoqueue;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import org.w3c.dom.Text;

import java.util.ArrayList;
import java.util.Objects;

public class GroupsRecyclerAdapter extends RecyclerView.Adapter<GroupsRecyclerAdapter.ThisViewHolder>{

    Context context;

    public GroupsRecyclerAdapter(Context context) {
        this.context = context;
    }

    @NonNull
    @Override
    public GroupsRecyclerAdapter.ThisViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(context);
        View view = inflater.inflate(R.layout.group_recycler_object, parent, false);

        return new GroupsRecyclerAdapter.ThisViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ThisViewHolder holder, int position) {
        AutoqueueGroup here = SpotifyService.groups.get(position);

        holder.parentTitle.setText(here.getParentTitle());
        holder.childTitle.setText(here.getChildTitle());

        Glide.with(context).load(here.getParentImageUrl()).into(holder.parentImage);
        if(!Objects.equals(here.getChildImageUrl(), here.getParentImageUrl()))
            Glide.with(context).load(here.getChildImageUrl()).into(holder.childImage);

        holder.condition.setText(here.getCondition().equals("now") ? "Playing now" : "Up next");
    }

    @Override
    public int getItemCount() { return SpotifyService.groups.size(); }

    public static class ThisViewHolder extends RecyclerView.ViewHolder{

        TextView parentTitle;
        TextView childTitle;
        TextView condition;
        TextView activeState;
        ImageView parentImage;
        ImageView childImage;

        public ThisViewHolder(@NonNull View itemView) {
            super(itemView);

            parentTitle = itemView.findViewById(R.id.parentTitle);
            childTitle = itemView.findViewById(R.id.childTitle);
            condition = itemView.findViewById(R.id.condition);
            activeState = itemView.findViewById(R.id.activeState);
            parentImage = itemView.findViewById(R.id.parentImage);
            childImage = itemView.findViewById(R.id.childImage);
        }
    }
}
