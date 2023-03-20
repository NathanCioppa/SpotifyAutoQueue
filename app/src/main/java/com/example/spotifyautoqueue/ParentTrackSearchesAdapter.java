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

public class ParentTrackSearchesAdapter extends RecyclerView.Adapter<ParentTrackSearchesAdapter.ThisViewHolder> {
    Context context;
    ArrayList<SearchItem> searchItems;

    public ParentTrackSearchesAdapter(Context context, ArrayList<SearchItem> searchItems){
        this.context = context;
        this.searchItems = searchItems;
    }

    @NonNull
    @Override
    public ParentTrackSearchesAdapter.ThisViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        LayoutInflater inflater = LayoutInflater.from(context);
        View view = inflater.inflate(R.layout.track_search_recycler_row, parent, false);

        return new ParentTrackSearchesAdapter.ThisViewHolder(view);
    }
    @Override
    public void onBindViewHolder(@NonNull ThisViewHolder holder, int position) {
        ArrayList<SearchItem> parentSearches = CreateGroupActivity.parentSearches;

        holder.nameView.setText(parentSearches.get(position).getName());
        holder.artistView.setText(parentSearches.get(position).getArtist());
        Glide.with(context).load(parentSearches.get(position).getImageUrl()).into(holder.imageView);
        holder.imageView.setTag(parentSearches.get(position).getImageUrl());
        holder.itemView.setTag("p"+parentSearches.get(position).uri);
    }

    @Override
    public int getItemCount() {
        return CreateGroupActivity.parentSearches.size();
    }

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
