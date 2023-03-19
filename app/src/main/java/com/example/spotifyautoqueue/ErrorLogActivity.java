package com.example.spotifyautoqueue;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.icu.util.LocaleData;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

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

    static ArrayList<ErrorMessage> errors = new ArrayList<>();

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