package com.example.spotifyautoqueue;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import java.util.ArrayList;

public class ErrorLogActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_error_log);
    }

    static ArrayList<ErrorMessage> errors;

    public void backToSettings(View button) {
        Intent settings = new Intent(this, SettingsActivity.class);
        startActivity(settings);
    }
}