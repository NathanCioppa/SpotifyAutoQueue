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

import java.util.ArrayList;

public class ChildTrackSearchesAdapter extends RecyclerView.Adapter<ChildTrackSearchesAdapter.ThisViewHolder> {

    Context context;

    public ChildTrackSearchesAdapter(Context context) {
        this.context = context;
    }

    @NonNull
    @Override
    public ChildTrackSearchesAdapter.ThisViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(context);
        View view = inflater.inflate(R.layout.track_search_recycler_row, parent, false);

        return new ChildTrackSearchesAdapter.ThisViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ThisViewHolder holder, int position) {
        ArrayList<SearchItem> childSearches = CreateGroupActivity.childSearches;

        holder.nameView.setText(childSearches.get(position).getName());
        holder.artistView.setText(childSearches.get(position).getArtist());

        Glide.with(context).load(childSearches.get(position).getImageUrl()).into(holder.imageView);
        holder.imageView.setTag(childSearches.get(position).getImageUrl());

        holder.itemView.setTag(childSearches.get(position).uri);
    }

    @Override
    public int getItemCount() { return CreateGroupActivity.childSearches.size(); }

    public static class ThisViewHolder extends RecyclerView.ViewHolder{

        TextView nameView;
        TextView artistView;
        ImageView imageView;

        public ThisViewHolder(@NonNull View itemView) {
            super(itemView);

            nameView = itemView.findViewById(R.id.searchedTrackTitle);
            artistView = itemView.findViewById(R.id.searchedTrackArtist);
            imageView = itemView.findViewById(R.id.searchedTrackImage);
        }
    }
}
