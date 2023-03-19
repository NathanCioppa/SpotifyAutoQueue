package com.example.spotifyautoqueue;

import android.content.Context;
import android.util.Log;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;

public class ApiTokens {

    static final String CLIENT_ID = "1435a3ff53c84332b17feb17f4cb0cb8";
    static final String REDIRECT_URI = "https://nathancioppa.github.io/spotify-app.html";

    public static String authCode ="";
    public static String accessToken = "";
    public static String refreshToken ="";

    public static void saveTokens(File file) {
        try {
            FileWriter fileWriter = new FileWriter(file);
            fileWriter.write(accessToken);
            fileWriter.write("\n");
            fileWriter.write(refreshToken);
            fileWriter.close();

            Log.d("saveTokens", "access "+accessToken);
            Log.d("saveTokens", "refresh "+refreshToken);
        } catch (IOException error){
            ErrorLogActivity.logError("Error saving tokens","Access tokens failed to save.");
            error.printStackTrace();
        }
    }

    public static void getTokens(File file) {
        try {
            BufferedReader reader = new BufferedReader(new FileReader(file));

            accessToken = reader.readLine();
            refreshToken = reader.readLine();
            reader.close();

            System.out.println("Access token: " + accessToken);
            System.out.println("Refresh token: " + refreshToken);
        } catch (IOException e) {
            ErrorLogActivity.logError("Error retrieving tokens","Access tokens could not be found. You may need to submit a new auth code.");
            e.printStackTrace();
        }
    }

}
