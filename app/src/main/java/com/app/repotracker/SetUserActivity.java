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
        setContentView(R.layout.activity_set_user);

        username = (TextView) findViewById(R.id.username_text);
        saveUser = (Button) findViewById(R.id.save_user);
        sharedPreferences = getSharedPreferences(PREFERENCES, 0);
        sharedPrefEditor = sharedPreferences.edit();

        if (sharedPreferences != null
                && sharedPreferences.contains("username")) {
            // object and key found, show all saved values
            applySavedPreferences();
        }

        saveUser.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String input = username.getText().toString();
                System.out.println(input);
                if (TextUtils.isEmpty(input)) {
                    // Cancel any existing toast
                    if (toast != null) {
                        toast.cancel();
                    }

                    // Display toast when a list item has been pressed
                    String toastMessage = "Username field cannot be empty";
                    toast = Toast.makeText(SetUserActivity.this, toastMessage, Toast.LENGTH_LONG);
                    toast.show();
                    return;

                }
                sharedPrefEditor.putString("username", input);
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