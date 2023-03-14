package com.example.spotifyautoqueue;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

public class CreateGroupActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_group);
    }

    public void searchForParent(View button) {
        EditText inputParentSearch = findViewById(R.id.searchParentTrack);
        SearchSpotify searchSpotify = new SearchSpotify();
        searchSpotify.searchQuery = inputParentSearch.getText().toString();

        try{
            boolean response = searchSpotify.execute().get();

        } catch (Exception error) {
            System.out.println(error);
        }

    }

    public void backToHome(View button) {
        Intent home = new Intent(this, MainActivity.class);
        startActivity(home);
    }
}