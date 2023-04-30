package com.example.spotifyautoqueue;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import java.util.ArrayList;
import java.util.Date;

// Displays errors to the user
public class ErrorLogActivity extends AppCompatActivity {

    RecyclerView errorLogRecycler;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_error_log);

        errorLogRecycler = findViewById(R.id.errorLogRecycler);
        ErrorMessageAdapter adapter = new ErrorMessageAdapter(this, errors);
        errorLogRecycler.setAdapter(adapter);
        errorLogRecycler.setLayoutManager(new LinearLayoutManager(this));

    }

    static ArrayList<ErrorMessage> errors = new ArrayList<>(); // Arraylist containing the error messages

    // Adds a message to the error log
    // The tag will show in bold red letters, message will show in plain text below that
    public static void logError(String tag, String message) {
        String time = new Date().toString();
        time = time.substring(time.indexOf(":")-2, time.lastIndexOf(":")+3);

        ErrorMessage errorMessage = new ErrorMessage(time, tag, message);
        errors.add(errorMessage);
    }

    public void backToSettings(View button) {
        Intent settings = new Intent(this, SettingsActivity.class);
        startActivity(settings);
    }
}