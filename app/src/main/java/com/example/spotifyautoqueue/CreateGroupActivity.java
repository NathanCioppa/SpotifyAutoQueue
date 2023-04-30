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

import java.io.File;
import java.util.ArrayList;
import java.util.Date;

// Activity responsible for creating a new group
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

        // Set the condition to "now" by default and select the appropriate radio button
        findViewById(R.id.createGroupConditionNowButton).performClick();
    }



    static ArrayList<SearchItem> parentSearches; // List which contains search results for parent track

    public void searchForParent(View button) {
        EditText inputParentSearch = findViewById(R.id.searchParentTrack);
        SearchSpotify searchSpotify = new SearchSpotify();
        searchSpotify.searchQuery = inputParentSearch.getText().toString();

        try{
            parentSearches = searchSpotify.execute().get();

            // If the search returns null, the access token will be refreshed and will attempt to search again.
            if(parentSearches == null && new RefreshAccessToken().execute().get()) {
                parentSearches = new SearchSpotify().execute().get();

                saveTokens(); // Saves tokens since the access token was just refreshed
            }

            if (parentSearches != null) {
                // Setup adapter to display search results
                ParentTrackSearchesAdapter parentAdapter = new ParentTrackSearchesAdapter(this, parentSearches);
                parentSearchRecycler.setAdapter(parentAdapter);
                parentSearchRecycler.setLayoutManager(new LinearLayoutManager(this));

                // Show recycler containing searches, hide the previously selected track11
                parentSearchRecycler.setVisibility(View.VISIBLE);
                selectedParent.setVisibility(View.GONE);
            }

        } catch (Exception error) {
            ErrorLogActivity.logError("Error searching from CreateGroupActivity",error+"");
        }
    }

    static ArrayList<SearchItem> childSearches; // List which contains search results for child track track

    // Same structure as parent track search
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
            ErrorLogActivity.logError("Error searching from CreateGroupActivity",error+"");
        }
    }

    // Selects the track from either recycler view containing search results
    public void selectTrack(View track) {

        // Get the information from the selected track
        String name = ((TextView)track.findViewById(R.id.searchedTrackTitle)).getText().toString();
        String artist = ((TextView)track.findViewById(R.id.searchedTrackArtist)).getText().toString();
        String imageUrl = track.findViewById(R.id.searchedTrackImage).getTag().toString();
        String uri = track.getTag().toString();

        // Tag of parent track will be "p"+ the track uri, child track will just be the uri
        if (uri.charAt(0) == 'p') {
            uri = uri.substring(1);

            newGroup[PARENT_TITLE] = name;
            newGroup[PARENT_TRACK_URI] = uri;
            newGroup[PARENT_IMAGE_URL] = imageUrl;

            // Display the track which was selected
            ((TextView)findViewById(R.id.selectedParentTrackName)).setText(name);
            ((TextView)findViewById(R.id.selectedParentTrackArtist)).setText(artist);
            Glide.with(this).load(imageUrl).into((ImageView)findViewById(R.id.selectedParentTrackImage));

            // Hide the search results, show the selected track
            selectedParent.setVisibility(View.VISIBLE);
            parentSearchRecycler.setVisibility(View.GONE);

        } else {
            // same structure as above, but for the child track
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

    // Sets the condition of the new group, condition should only be "now" or "next"
    public void selectCurrentlyPlaying(View button) { newGroup[CONDITION] = "now"; }

    public void selectNextInQueue(View button) { newGroup[CONDITION] = "next"; }



    // Array which will contain all the information about the group which is being created
    String[] newGroup = new String[7];
    final int PARENT_TITLE = 0;
    final int PARENT_TRACK_URI = 1;
    final int PARENT_IMAGE_URL = 2;
    final int CHILD_TITLE = 3;
    final int CHILD_TRACK_URI = 4;
    final int CHILD_IMAGE_URL = 5;
    final int CONDITION = 6;

    public void saveNewGroup(View button) {
        // New group will only be created if all parameters are set.
        // If any index of the newGroup array is null, the group will not be created
        boolean allParametersSet = true;
        for (String s : newGroup) {
            if (s == null) {
                allParametersSet = false;
                break;
            }
        }

        if(allParametersSet) {
            // Add a new group to the groups list in SpotifyService
            SpotifyService.groups.add(new AutoqueueGroup(
                    newGroup[0],newGroup[1],newGroup[2],newGroup[3],newGroup[4],newGroup[5], newGroup[6],
                    true, new Date().getTime()
            ));

            // Save the groups since a new one was just added, then return to MainActivity
            MainActivity.saveGroups(this);
            backToHome(button);
        }
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