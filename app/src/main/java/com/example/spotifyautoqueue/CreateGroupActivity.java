package com.example.spotifyautoqueue;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;

import java.util.ArrayList;
import java.util.concurrent.ExecutionException;

public class CreateGroupActivity extends AppCompatActivity {

    RecyclerView parentSearchRecycler;
    View selectedParent;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_group);
        parentSearchRecycler = findViewById(R.id.searchedParentTrackRecycler);
        selectedParent = findViewById(R.id.selectedParentTrackLayout);
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

                parentSearchRecycler.setVisibility(View.VISIBLE);
                selectedParent.setVisibility(View.GONE);
            }

        } catch (Exception error) {
            error.printStackTrace();
        }
    }

    static ArrayList<SearchItem> childSearches;

    public void searchForChild(View button) {
        EditText inputChildSearch = findViewById(R.id.searchChildTrack);
        SearchSpotify searchSpotify = new SearchSpotify();
        searchSpotify.searchQuery = inputChildSearch.getText().toString();

        try {
            childSearches = searchSpotify.execute().get();

            if(childSearches != null) {
                System.out.println(childSearches);
            }

        } catch (Exception error) {
            error.printStackTrace();
        }
    }

    public void selectTrack(View track) {

        String name = ((TextView)track.findViewById(R.id.searchedTrackTitle)).getText().toString();
        String artist = ((TextView)track.findViewById(R.id.searchedTrackArtist)).getText().toString();
        String imageUrl = ((ImageView)track.findViewById(R.id.searchedTrackImage)).getTag().toString();
        String uri = track.getTag().toString();

        //tag of parent track will be "p"+ the track uri, child track will just be the uri
        if (uri.charAt(0) == 'p') {
            uri = uri.substring(1);

            ((TextView)findViewById(R.id.selectedParentTrackName)).setText(name);
            ((TextView)findViewById(R.id.selectedParentTrackArtist)).setText(artist);
            Glide.with(this).load(imageUrl).into((ImageView)findViewById(R.id.selectedParentTrackImage));

            selectedParent.setVisibility(View.VISIBLE);
            parentSearchRecycler.setVisibility(View.GONE);
        } else {
            System.out.println(name+" "+artist+" "+imageUrl+" "+uri);
        }
    }

    public void backToHome(View button) {
        Intent home = new Intent(this, MainActivity.class);
        startActivity(home);
    }
}