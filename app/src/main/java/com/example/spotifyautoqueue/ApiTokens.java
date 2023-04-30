package com.example.spotifyautoqueue;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

// This class contains all information necessary to make API calls to the Spotify Web API.
// Update the information in this class when making changes to the access tokens.
// Information such as the Client ID, Redirect Uri, and access/refresh tokens is kept here for the app to use.
public class ApiTokens {

    static final String CLIENT_ID = "1435a3ff53c84332b17feb17f4cb0cb8";
    static final String REDIRECT_URI = "https://nathancioppa.github.io/spotify-app.html";

    public static String authCode ="";
    public static String accessToken = "";
    public static String refreshToken ="";

    // Tokens should be saved whenever they are changed, such as submitting an auth code, or refreshing an access token.
    public static void saveTokens(File file) {
        try {
            FileWriter fileWriter = new FileWriter(file);
            fileWriter.write(accessToken);
            fileWriter.write("\n");
            fileWriter.write(refreshToken);
            fileWriter.close();

        } catch (IOException e) {
            ErrorLogActivity.logError("Error saving tokens","ERROR: "+e);
        }
    }

    // Tokens should be gotten whenever the app is completely restarted.
    public static void getTokens(File file) {
        try {
            BufferedReader reader = new BufferedReader(new FileReader(file));

            accessToken = reader.readLine();
            refreshToken = reader.readLine();
            reader.close();

        } catch (IOException e) {
            ErrorLogActivity.logError("Error retrieving tokens","Access tokens could not be found. You may need to submit a new auth code. ERROR: "+e);
        }
    }

}
