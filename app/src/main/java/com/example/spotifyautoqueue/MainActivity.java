package com.example.spotifyautoqueue;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.AppOpsManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.provider.Settings;
import android.view.View;
import android.widget.TextView;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;


public class MainActivity extends AppCompatActivity {
    final String TAG = "MainActivity";
    RecyclerView groupsRecycler;
    GroupsRecyclerAdapter groupsAdapter;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);

        groupsAdapter = new GroupsRecyclerAdapter(this);
        groupsRecycler = findViewById(R.id.groupsRecycler);
        groupsRecycler.setAdapter(groupsAdapter);
        groupsRecycler.setLayoutManager(new LinearLayoutManager(this));

        getGroups(this);

    }

    @Override
    protected void onStart() {
        super.onStart();
        ErrorLogActivity.logError("Started MainActivity","onStart called in MainActivity");



        AppOpsManager appOps = (AppOpsManager) getSystemService(Context.APP_OPS_SERVICE);
        int mode = appOps.checkOpNoThrow(AppOpsManager.OPSTR_GET_USAGE_STATS, android.os.Process.myUid(), getPackageName());
        if (mode != AppOpsManager.MODE_ALLOWED) {
            startActivity(new Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS));
        }

        System.out.println(SpotifyService.groups);
    }

    public void deleteGroup(View button) {
        long id = (long) button.getTag();
        ArrayList<AutoqueueGroup> groups = SpotifyService.groups;

        for(int i=0; i<groups.size(); i++) {
            if (groups.get(i).getId() == id) {
                groups.remove(i);
                groupsAdapter.notifyItemRemoved(i);

                saveGroups(this);
                break;
            }
        }
    }

    public void toggleGroupEnabled(View container) {
        long id = (long) container.getTag();
        ArrayList<AutoqueueGroup> groups = SpotifyService.groups;

        for(int i=0; i<groups.size(); i++) {
            AutoqueueGroup group = groups.get(i);
            if (group.getId() == id) {
                group.activeState = !group.activeState;

                TextView activeStateText = container.findViewById(R.id.activeState);
                activeStateText.setText(group.activeState ? "Enabled" : "Disabled");
                activeStateText.setTextColor(group.activeState ? Color.GREEN : Color.RED);

                saveGroups(this);
                break;
            }
        }
    }

    public static void saveGroups(Context context){
        try {
            FileOutputStream fos = context.openFileOutput("groups.txt", Context.MODE_PRIVATE);
            ObjectOutputStream oos = new ObjectOutputStream(fos);
            oos.writeObject(SpotifyService.groups);
            oos.close();
            fos.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void getGroups(Context context) {
        try {
            FileInputStream fis = context.openFileInput("groups.txt");
            ObjectInputStream ois = new ObjectInputStream(fis);
            SpotifyService.groups = (ArrayList<AutoqueueGroup>) ois.readObject();
            ois.close();
            fis.close();
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    public void openSettingsActivity(View button) {
        Intent openSettings = new Intent(this,SettingsActivity.class);
        startActivity(openSettings);
    }

    public void openCreateGroupActivity(View button) {
        Intent openCreateGroup = new Intent(this, CreateGroupActivity.class);
        startActivity(openCreateGroup);
    }
}