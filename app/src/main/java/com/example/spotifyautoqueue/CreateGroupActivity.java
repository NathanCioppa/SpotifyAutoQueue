package com.example.spotifyautoqueue;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import com.bumptech.glide.Glide;

import java.util.ArrayList;

public class CreateGroupActivity extends AppCompatActivity {

    RecyclerView parentSearchRecycler;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_group);
        parentSearchRecycler = findViewById(R.id.searchedParentTrackRecycler);
    }

    static ArrayList<SearchItem> parentSearches;

    public void searchForParent(View button) {
        EditText inputParentSearch = findViewById(R.id.searchParentTrack);
        SearchSpotify searchSpotify = new SearchSpotify();
        searchSpotify.searchQuery = inputParentSearch.getText().toString();

        try{
            parentSearches = searchSpotify.execute().get();

            if (parentSearches != null) {
                ParentTrackSearchesAdapter parentAdapter = new ParentTrackSearchesAdapter(this, parentSearches);
                parentSearchRecycler.setAdapter(parentAdapter);
                parentSearchRecycler.setLayoutManager(new LinearLayoutManager(this));
            }

        } catch (Exception error) {
            error.printStackTrace();
        }
    }

    public void backToHome(View button) {
        Intent home = new Intent(this, MainActivity.class);
        startActivity(home);
    }
}