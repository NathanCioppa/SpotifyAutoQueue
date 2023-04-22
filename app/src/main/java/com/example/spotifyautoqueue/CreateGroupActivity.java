package com.example.spotifyautoqueue;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.TextView;

import com.bumptech.glide.Glide;

import java.io.File;
import java.util.ArrayList;
import java.util.Date;

public class CreateGroupActivity extends AppCompatActivity {

    RecyclerView parentSearchRecycler;
    RecyclerView childSearchRecycler;
    View selectedParent;
    View selectedChild;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_group);

        parentSearchRecycler = findViewById(R.id.searchedParentTrackRecycler);
        selectedParent = findViewById(R.id.selectedParentTrackLayout);

        childSearchRecycler = findViewById(R.id.searchedChildTrackRecycler);
        selectedChild = findViewById(R.id.selectedChildTrackLayout);

        findViewById(R.id.createGroupConditionNowButton).performClick();
    }

    static ArrayList<SearchItem> parentSearches;

    public void searchForParent(View button) {
        EditText inputParentSearch = findViewById(R.id.searchParentTrack);
        SearchSpotify searchSpotify = new SearchSpotify();
        searchSpotify.searchQuery = inputParentSearch.getText().toString();

        try{
            parentSearches = searchSpotify.execute().get();

            if(parentSearches == null && new RefreshAccessToken().execute().get()) {
                parentSearches = new SearchSpotify().execute().get();

                saveTokens();
            }

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

            if(childSearches == null && new RefreshAccessToken().execute().get()) {
                childSearches = new SearchSpotify().execute().get();
                saveTokens();
            }

            if(childSearches != null) {
                ChildTrackSearchesAdapter childAdapter = new ChildTrackSearchesAdapter(this);
                childSearchRecycler.setAdapter(childAdapter);
                childSearchRecycler.setLayoutManager(new LinearLayoutManager(this));

                childSearchRecycler.setVisibility(View.VISIBLE);
                selectedChild.setVisibility(View.GONE);
            }

        } catch (Exception error) {
            error.printStackTrace();
        }
    }

    public void selectTrack(View track) {

        String name = ((TextView)track.findViewById(R.id.searchedTrackTitle)).getText().toString();
        String artist = ((TextView)track.findViewById(R.id.searchedTrackArtist)).getText().toString();
        String imageUrl = track.findViewById(R.id.searchedTrackImage).getTag().toString();
        String uri = track.getTag().toString();

        //tag of parent track will be "p"+ the track uri, child track will just be the uri
        if (uri.charAt(0) == 'p') {
            uri = uri.substring(1);

            newGroup[PARENT_TITLE] = name;
            newGroup[PARENT_TRACK_URI] = uri;
            newGroup[PARENT_IMAGE_URL] = imageUrl;

            ((TextView)findViewById(R.id.selectedParentTrackName)).setText(name);
            ((TextView)findViewById(R.id.selectedParentTrackArtist)).setText(artist);
            Glide.with(this).load(imageUrl).into((ImageView)findViewById(R.id.selectedParentTrackImage));

            selectedParent.setVisibility(View.VISIBLE);
            parentSearchRecycler.setVisibility(View.GONE);

        } else {
            newGroup[CHILD_TITLE] = name;
            newGroup[CHILD_TRACK_URI] = uri;
            newGroup[CHILD_IMAGE_URL] = imageUrl;

            ((TextView)findViewById(R.id.selectedChildTrackName)).setText(name);
            ((TextView)findViewById(R.id.selectedChildTrackArtist)).setText(artist);
            Glide.with(this).load(imageUrl).into((ImageView)findViewById(R.id.selectedChildTrackImage));

            selectedChild.setVisibility(View.VISIBLE);
            childSearchRecycler.setVisibility(View.GONE);
        }
    }

    String[] newGroup = new String[7];
    final int PARENT_TITLE = 0;
    final int PARENT_TRACK_URI = 1;
    final int PARENT_IMAGE_URL = 2;
    final int CHILD_TITLE = 3;
    final int CHILD_TRACK_URI = 4;
    final int CHILD_IMAGE_URL = 5;
    final int CONDITION = 6;

    public void saveNewGroup(View button) {
        System.out.println("save");

        boolean allParametersSet = true;
        for (String s : newGroup) {
            if (s == null) {
                allParametersSet = false;
                break;
            }
        }

        if(allParametersSet) {
            SpotifyService.groups.add(new AutoqueueGroup(
                    newGroup[0],newGroup[1],newGroup[2],newGroup[3],newGroup[4],newGroup[5], newGroup[6],
                    true, new Date().getTime()
            ));

            MainActivity.saveGroups(this);

            backToHome(button);
        }
    }

    public void selectCurrentlyPlaying(View button) {
        newGroup[CONDITION] = "now";
    }

    public void selectNextInQueue(View button) {
        newGroup[CONDITION] = "next";
    }

    public void backToHome(View button) {
        Intent home = new Intent(this, MainActivity.class);
        startActivity(home);
    }

    public void saveTokens() {
        File externalDir = getExternalFilesDir(null);
        if (externalDir != null) {
            File file = new File(externalDir, "tokens.txt");
            ApiTokens.saveTokens(file);
        }
    }
}