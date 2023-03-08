package com.app.repotracker;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.loader.app.LoaderManager;
import androidx.loader.content.AsyncTaskLoader;
import androidx.loader.content.Loader;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.app.repotracker.utilities.Adapter;
import com.app.repotracker.utilities.Network;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.net.URL;

public class BaseActivity extends AppCompatActivity implements
        LoaderManager.LoaderCallbacks<String>, Adapter.ListItemClickListener {
    private TextView infoMessage;
    private ProgressBar loadingSymbol;
    private RecyclerView recyclerView;
    private Adapter adapter;
    private SharedPreferences sharedPreferences;
    private final String PREFERENCES = "Preferences";
    private static final String COMPLETE_URL_KEY = "completeUrl";
    private static final int GITHUB_SEARCH_LOADER = 22;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Set layout of activity
        setContentView(R.layout.activity_main);

        // If no username is saved to shared preferences, launch Set User activity
        sharedPreferences = getSharedPreferences(PREFERENCES, 0);
        if (sharedPreferences == null || !sharedPreferences.contains("username")) {
            Context context = BaseActivity.this;
            Intent intent = new Intent(context, SetUserActivity.class);
            startActivity(intent);
        }

        infoMessage = (TextView) findViewById(R.id.info);
        recyclerView = (RecyclerView) findViewById(R.id.recycler_view);
        loadingSymbol = (ProgressBar) findViewById(R.id.loading_symbol);

        // Set layout of recycler view to be vertical list
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);

        // Initialize background loader
        LoaderManager.getInstance(this).initLoader(GITHUB_SEARCH_LOADER, null, this);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Set layout of options menu
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.options_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Use explicit intents to launch activities based on options menu choice
        Context context = BaseActivity.this;
        Intent intent;
        switch (item.getItemId()) {
            case R.id.home:
                // Change to Main Activity
                intent = new Intent(context, MainActivity.class);
                startActivity(intent);
                return true;
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

            case R.id.toggle_dark:
                // Toggle dark mode on or off
                if (isDarkModeEnabled()) {
                    disableDarkMode();
                } else {
                    enableDarkMode();
                }
                return true;

            case R.id.reset:
                // Clear stored shared preferences and launch Set User Activity
                SharedPreferences.Editor sharedPreferencesEditor = sharedPreferences.edit();
                sharedPreferencesEditor.clear();
                sharedPreferencesEditor.apply();
                infoMessage.setText(R.string.search_info);
                intent = new Intent(context, SetUserActivity.class);
                startActivity(intent);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void enableDarkMode() {
        // Enable dark mode
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
        recreate();
    }

    private void disableDarkMode() {
        // Disable dark mode
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        recreate();
    }

    public boolean isDarkModeEnabled() {
        // Check if dark mode is enabled
        int nightMode = AppCompatDelegate.getDefaultNightMode();
        return nightMode == AppCompatDelegate.MODE_NIGHT_YES;
    }

    /**
     * Make a search on github from a provided URL through an AsyncTaskLoader.
     *
     * @param url The URL to be searched
     */
    public void performSearch(URL url) {

        // Save search URL to bundle
        Bundle bundle = new Bundle();
        bundle.putString(COMPLETE_URL_KEY, url.toString());

        // Get existing loader or create new loader from loader manager
        LoaderManager loaderManager = LoaderManager.getInstance(this);
        Loader<String> githubSearchLoader = loaderManager.getLoader(GITHUB_SEARCH_LOADER);
        if (githubSearchLoader == null) {
            loaderManager.initLoader(GITHUB_SEARCH_LOADER, bundle, this);
        } else {
            loaderManager.restartLoader(GITHUB_SEARCH_LOADER, bundle, this);
        }
    }

    @SuppressLint("StaticFieldLeak")
    @NonNull
    @Override
    public Loader<String> onCreateLoader(int id, final Bundle args) {
        return new AsyncTaskLoader<String>(this) {

            @Override
            protected void onStartLoading() {
                // Do nothing if no args provided
                if (args == null) {
                    return;
                }
                // Display loading symbol
                loadingSymbol.setVisibility(View.VISIBLE);
                // Force a load
                forceLoad();
            }

            @Override
            public String loadInBackground() {

                // Get URL to search from bundle as string
                String searchQueryUrlString = args.getString(COMPLETE_URL_KEY);
                if (searchQueryUrlString == null || TextUtils.isEmpty(searchQueryUrlString)) {
                    return null;
                }

                // Make HTTP request and return response
                try {
                    URL githubUrl = new URL(searchQueryUrlString);
                    return Network.getResponseFromHttpUrl(githubUrl);
                } catch (IOException e) {
                    e.printStackTrace();
                    return null;
                }
            }
        };
    }

    /**
     * Parse received data into json and load data into RecyclerView
     *
     * @param data The data to parse into RecyclerView
     */
    public void loadData(String data) {
        try {
            // Parse provided string into Json format
            JSONArray dataJson = new JSONObject(data).getJSONArray("items");
            int numItems = dataJson.length();

            // Check if data is empty
            if (numItems < 1) {
                showEmpty();
            }
            else {
                // Load data into adapter for displaying items in recycler view
                adapter = new Adapter(this, numItems, this, dataJson);
                recyclerView.setAdapter(adapter);
                infoMessage.setVisibility(View.INVISIBLE);
                recyclerView.setVisibility(View.VISIBLE);
            }
        } catch (JSONException e) {
            // Show error message if data could not be parsed properly
            showError();
            Log.d("JSONError", e.toString());
        }
    }

    public void showError() {
        // Display message that an error occurred when attempting to search
        recyclerView.setVisibility(View.INVISIBLE);
        infoMessage.setText(R.string.error_message);
        infoMessage.setVisibility(View.VISIBLE);
    }

    public void showEmpty() {
        // Display message that result was empty
        recyclerView.setVisibility(View.INVISIBLE);
        infoMessage.setText(R.string.error_empty);
        infoMessage.setVisibility(View.VISIBLE);
    }

    public void showDefault() {
        // Display default information message and hide recycler
        recyclerView.setVisibility(View.INVISIBLE);
        infoMessage.setText(R.string.search_info);
        infoMessage.setVisibility(View.VISIBLE);
    }

    @Override
    public void onLoadFinished(Loader<String> loader, String data) {
        // Must be overridden
    }

    @Override
    public void onLoaderReset(Loader<String> loader) {
        // Must be overridden
    }

    @Override
    public void onListItemClick(int clickedItemIndex) {
        // Must be overridden
    }
}