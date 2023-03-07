package com.app.repotracker;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.SearchView;

public class UserReposActivity extends AppCompatActivity {

    private SearchView search;
    private SharedPreferences sharedPreferences;
    private final String PREFERENCES = "Preferences";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_repos);
        sharedPreferences = getSharedPreferences(PREFERENCES, 0);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.options_menu, menu);

        search = (SearchView) menu.findItem(R.id.search).getActionView();
        // Add listener to search button
        search.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String s) {
                // Perform action on click
                // TODO
                return true;
            }

            @Override
            public boolean onQueryTextChange(String s) {
                return false;
            }
        });

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        Context context = UserReposActivity.this;
        Intent intent;
        switch (item.getItemId()) {
            case R.id.user_repos:
                // Change to User Repos Activity
                intent = new Intent(context, UserReposActivity.class);
                startActivity(intent);
                return true;

            case R.id.set_user:
                // Change to Set User Activity
                intent = new Intent(context, SetUserActivity.class);
                startActivity(intent);
                return true;

            case R.id.reset:
                // Pass in this as the ListItemClickListener to the GreenAdapter constructor
//                recyclerView.setVisibility(View.INVISIBLE);
//                infoMessage.setVisibility(View.VISIBLE);
                SharedPreferences.Editor sharedPreferencesEditor = sharedPreferences.edit();
                sharedPreferencesEditor.clear();
                sharedPreferencesEditor.apply();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }
}