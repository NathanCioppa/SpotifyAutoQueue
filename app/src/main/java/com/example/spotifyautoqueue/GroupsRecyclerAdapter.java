package com.example.spotifyautoqueue;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

public class GroupsRecyclerAdapter extends RecyclerView.Adapter<GroupsRecyclerAdapter.ThisViewHolder>{

    Context context;
    ArrayList<AutoqueueGroup> groups;

    @NonNull
    @Override
    public GroupsRecyclerAdapter.ThisViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(context);
        View view = inflater.inflate(R.layout.group_recycler_object, parent, false);

        return new GroupsRecyclerAdapter.ThisViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ThisViewHolder holder, int position) {

    }

    @Override
    public int getItemCount() { return SpotifyService.groups.size(); }

    public static class ThisViewHolder extends RecyclerView.ViewHolder{

        public ThisViewHolder(@NonNull View itemView) {
            super(itemView);
        }
    }
}
