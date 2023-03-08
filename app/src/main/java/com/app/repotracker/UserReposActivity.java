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
        super.onCreate(savedInstanceState);

        infoMessage = (TextView) findViewById(R.id.info);
        loadingSymbol = (ProgressBar) findViewById(R.id.loading_symbol);
        recyclerView = (RecyclerView) findViewById(R.id.recycler_view);

        showDefault();

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
        super.onCreateOptionsMenu(menu);
        // Hide Search View in this activity
        MenuItem search = (MenuItem) menu.findItem(R.id.search);
        search.setVisible(false);

        return true;
    }

    public void getUserRepos(String username) {
        // If search is empty, display empty message
        if (TextUtils.isEmpty(username)) {
            infoMessage.setText(R.string.empty_username);
            return;
        }

        URL githubUserUrl = Network.buildUrl("/users/" + username + "/repos");
        performSearch(githubUserUrl);
    }

    @Override
    public void onLoadFinished(Loader<String> loader, String data) {

        // Hide loading symbol and info message
        loadingSymbol.setVisibility(View.INVISIBLE);

        // Show error message if no data, or show results
        if (data == null) {
            recyclerView.setVisibility(View.INVISIBLE);
            infoMessage.setText(R.string.error_message);
            infoMessage.setVisibility(View.VISIBLE);
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
        String formattedData = "{\"items\": " + data + "}";
        super.loadData(formattedData);
    }

    @Override
    public void showDefault() {
        super.showDefault();
        infoMessage.setText(R.string.empty_username);
    }
}