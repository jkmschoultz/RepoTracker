package com.app.repotracker;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.loader.content.Loader;
import androidx.recyclerview.widget.RecyclerView;

import com.app.repotracker.utilities.Network;

import java.net.URL;

public class UserReposActivity extends BaseActivity {
    private TextView infoMessage;
    private ProgressBar loadingSymbol;
    private RecyclerView recyclerView;
    private SharedPreferences sharedPreferences;
    private final String PREFERENCES = "Preferences";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // Use BaseActivity's onCreate() method
        super.onCreate(savedInstanceState);

        infoMessage = (TextView) findViewById(R.id.info);
        loadingSymbol = (ProgressBar) findViewById(R.id.loading_symbol);
        recyclerView = (RecyclerView) findViewById(R.id.recycler_view);

        // Show default message for this activity
        showDefault();

        // Get existing Shared Preferences
        sharedPreferences = getSharedPreferences(PREFERENCES, 0);
        if (sharedPreferences != null
                && sharedPreferences.contains("username")) {
            // Search GitHub for user's public repositories
            String username = sharedPreferences.getString("username", "");
            getUserRepos(username);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Use BaseActivity's onCreateOptionsMenu() method
        super.onCreateOptionsMenu(menu);
        // Hide Search View in this activity
        MenuItem search = (MenuItem) menu.findItem(R.id.search);
        search.setVisible(false);
        return true;
    }

    /**
     * Make a search on github to retrieve all repositories for a given user.
     *
     * @param username The GitHub username to retrieve repositories for
     */
    public void getUserRepos(String username) {
        // If search is empty, display empty message
        if (TextUtils.isEmpty(username)) {
            infoMessage.setText(R.string.empty_username);
            return;
        }

        // Build URL from provided username and perform search
        URL githubUserUrl = Network.buildUrl("/users/" + username + "/repos");
        performSearch(githubUserUrl);
    }

    @Override
    public void onLoadFinished(Loader<String> loader, String data) {
        // Override onLoadFinished() inherited from BaseActivity
        // Hide loading symbol and info message
        loadingSymbol.setVisibility(View.INVISIBLE);

        // Show error message if no data, or show results
        if (data == null) {
            showError();
        } else {
            loadData(data);
        }
    }

    /**
     * Parse received data into json and load data into RecyclerView
     *
     * @param data The data to parse into RecyclerView
     */
    @Override
    public void loadData(String data) {
        // Override loadData() method inherited from BaseActivity
        // Format string of data to match required form in parent loadData() method
        String formattedData = "{\"items\": " + data + "}";
        super.loadData(formattedData);
    }

    @Override
    public void showDefault() {
        // Display default information message and hide recycler
        super.showDefault();
        // Override default information message
        infoMessage.setText(R.string.empty_username);
    }
}