package com.app.repotracker;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

public class SetUserActivity extends AppCompatActivity {

    private TextView username;
    private Button saveUser;
    private SharedPreferences sharedPreferences;
    private SharedPreferences.Editor sharedPrefEditor;
    private final String PREFERENCES = "Preferences";
    private Toast toast;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Set layout of activity
        setContentView(R.layout.activity_set_user);

        username = (TextView) findViewById(R.id.username_text);
        saveUser = (Button) findViewById(R.id.save_user);
        sharedPreferences = getSharedPreferences(PREFERENCES, 0);
        sharedPrefEditor = sharedPreferences.edit();

        // Apply any existing username saved to Shared Preferences
        if (sharedPreferences != null
                && sharedPreferences.contains("username")) {
            applySavedPreferences();
        }

        // Add Listener to Button to save username
        saveUser.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String input = username.getText().toString();

                // Display toast message if input is empty
                if (TextUtils.isEmpty(input)) {
                    // Cancel any existing toast
                    if (toast != null) {
                        toast.cancel();
                    }

                    String toastMessage = "Username field cannot be empty";
                    toast = Toast.makeText(SetUserActivity.this, toastMessage, Toast.LENGTH_SHORT);
                    toast.show();
                    return;

                }

                // Store username in Shared Preferences
                sharedPrefEditor.putString("username", input);
                sharedPrefEditor.commit();

                // Redirect to Main Activity
                Context context = SetUserActivity.this;
                Class activity = MainActivity.class;
                Intent intent = new Intent(context, activity);
                startActivity(intent);
            }
        });
    }

    public void applySavedPreferences() {
        // Fill username field with stored Shared Preference
        username.setText(
            sharedPreferences.getString("username", "")
        );
    }
}