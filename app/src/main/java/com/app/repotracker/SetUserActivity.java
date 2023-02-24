package com.app.repotracker;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

public class SetUserActivity extends AppCompatActivity {

    private TextView username;
    private Button saveUser;
    private SharedPreferences sharedPreferences;
    private SharedPreferences.Editor sharedPrefEditor;
    private final String preferences = "Preferences";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_set_user);

        username = (TextView) findViewById(R.id.username_text);
        saveUser = (Button) findViewById(R.id.save_user);
        sharedPreferences = getSharedPreferences(preferences, 0);
        sharedPrefEditor = sharedPreferences.edit();

        if (sharedPreferences != null
                && sharedPreferences.contains("username")) {
            // object and key found, show all saved values
            applySavedPreferences();
        } else {
            Toast.makeText(getApplicationContext(),
                    "No Preferences found", Toast.LENGTH_LONG).show();
        }

        saveUser.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                sharedPrefEditor.putString("username", username.getText().toString());
                sharedPrefEditor.commit();
                Context context = SetUserActivity.this;
                Class activity = MainActivity.class;
                Intent intent = new Intent(context, activity);
                startActivity(intent);
            }
        });
    }

    public void applySavedPreferences() {
        username.setText(
            sharedPreferences.getString("username", "")
        );
    }
}